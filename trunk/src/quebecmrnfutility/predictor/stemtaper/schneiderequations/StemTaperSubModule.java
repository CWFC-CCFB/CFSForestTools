package quebecmrnfutility.predictor.stemtaper.schneiderequations;

import java.util.ArrayList;
import java.util.List;

import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperEquationSettings.ModelType;
import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictor.EstimationMethodInDeterministicMode;
import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictor.SchneiderStemTaperEstimate;
import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperTree.StemTaperTreeSpecies;
import repicea.math.Matrix;
import repicea.math.MatrixUtility;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.stemtaper.AbstractStemTaperEstimate;
import repicea.simulation.stemtaper.AbstractStemTaperPredictor;
import repicea.stats.Distribution;
import repicea.stats.StatisticalUtility;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianEstimate;

@SuppressWarnings("serial")
final class StemTaperSubModule extends AbstractStemTaperPredictor {

	private static final Matrix FakeMatrixForMissingRandomEffects = new Matrix(2,1);
	private static final Matrix FakeVarianceMatrixForMissingRandomEffects = new Matrix(2,2);
	
	protected final StemTaperTreeSpecies species;
	
	private final InternalStatisticalExpressions linearExpressions;

	protected Matrix heights;
	
	protected Matrix relativeHeights;
	protected Matrix coreExpression;
	protected Matrix heightsSectionRespectToDbh;

	protected final ModelType modelType;	//the type of equation model to use Tree or Hybrid

	private Matrix residualErrors;
	
	private Matrix rMatrix;
	private Matrix rMatrixChol;
	private double rho;
	private double varFunctionParm1;
	private double varFunctionParm2;
	private Double residualStdDev;
	private StemTaperTree tree;
	private Matrix correctionMatrix;

	/**
	 * Constructor.
	 * @param modelType
	 * @param species
	 * @param isParametersVariabilityEnabled
	 * @param isRandomEffectsVariabilityEnabled
	 * @param isResidualVariabilityEnabled
	 */
	protected StemTaperSubModule(ModelType modelType, StemTaperTreeSpecies species, boolean isParametersVariabilityEnabled, boolean isRandomEffectsVariabilityEnabled,	boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		this.modelType = modelType;
		this.species = species;
		linearExpressions = new InternalStatisticalExpressions(this, species);
	}
	
	protected final void setVarianceParameters(Matrix varianceParameters) {
		if (varianceParameters.m_iRows > 1) {
			this.varFunctionParm1 = Math.exp(varianceParameters.m_afData[0][0]);
			this.varFunctionParm2 = varianceParameters.m_afData[1][0];
		} else {
			this.varFunctionParm1 = 1d;
			this.varFunctionParm2 = varianceParameters.m_afData[0][0];
		}
	}
	
	protected final void setCorrelationParameter(double rho) {
		this.rho = rho;
	}
	
	protected final void setResidualStdDev(double residualStdDev) {
		this.residualStdDev = residualStdDev;
	}
	
	/**
	 * This method returns the R matrix.
	 * @return a Matrix instance
	 */
	private void setRMatrix() {
		Matrix distance = heights.repeat(1, heights.m_iRows).subtract(heights.transpose().repeat(heights.m_iRows, 1)).getAbsoluteValue();
		Matrix correlations = distance.powMatrix(rho);			
		Matrix x = heights.scalarAdd(-1.3).scalarMultiply(1d / (tree.getHeightM() - 1.3)).getAbsoluteValue();
		Matrix l = heights.scalarMultiply(-1d).scalarAdd(tree.getHeightM());
		Matrix stdDevMatrix = l.elementWiseMultiply(x).elementWiseMultiply(x.scalarMultiply(-1d).scalarAdd(1d).elementWisePower(3d)).getAbsoluteValue()
				.elementWisePower(varFunctionParm2).scalarAdd(varFunctionParm1).matrixDiagonal();
		rMatrix = stdDevMatrix.multiply(correlations).multiply(stdDevMatrix).scalarMultiply(residualStdDev * residualStdDev);
		rMatrixChol = rMatrix.getLowerCholTriangle();
	}
	

	/**
	 * This method sets the height sections.
	 * @param heights a Matrix (a column vector) that contains the location of the height sections (m)
	 * @param estimationMethod
	 * @throws Exception
	 */
	private void setHeights(Matrix heights, EstimationMethodInDeterministicMode estimationMethod) {
		for (int i = 0; i < heights.m_iRows; i++) {
			heights.m_afData[i][0] = Math.round(heights.m_afData[i][0] * 1000) * 0.001;
		}
		if (heights.m_afData[heights.m_iRows - 1][0] >= tree.getHeightM()) {
			heights.m_afData[heights.m_iRows - 1][0] = tree.getHeightM() - 1E-4;
		}
		this.heights = heights;
		relativeHeights = heights.scalarMultiply(1d / tree.getHeightM());
		Matrix treeHeightMatrix = new Matrix(heights.m_iRows, 1, tree.getHeightM(), 0d);

		MatrixUtility.subtract(treeHeightMatrix, heights);
		MatrixUtility.scalarMultiply(treeHeightMatrix, 1d / (tree.getHeightM() - 1.3));
		coreExpression =  treeHeightMatrix;

		heightsSectionRespectToDbh = heights.scalarMultiply(1d / 1.3);
		if (isResidualVariabilityEnabled) { // stochastic implementation
			setRMatrix();
		} else { // deterministic implementation then
			if (estimationMethod == EstimationMethodInDeterministicMode.FirstOrder || estimationMethod == EstimationMethodInDeterministicMode.SecondOrder) {
				setRMatrix();	// must be also set because we need it for the variance
			}
		}
		residualErrors = new Matrix(heights.m_iRows, 1); // TODO find a way to not create this object every time
	}
	
	@Override
	public synchronized AbstractStemTaperEstimate getPredictedTaperForTheseHeights(BasicStemTaperTree t, List<Double> heightMeasures, Object... additionalParameters) {
		this.tree = (StemTaperTree) t;
		Matrix parametersForThisRealization = getParametersForThisRealization(tree);
		linearExpressions.setParameters(parametersForThisRealization);
		EstimationMethodInDeterministicMode estimationMethod = EstimationMethodInDeterministicMode.SecondOrder;
		if (additionalParameters != null && additionalParameters.length >= 1) {
			if (additionalParameters[0] instanceof EstimationMethodInDeterministicMode) {
				estimationMethod = (EstimationMethodInDeterministicMode) additionalParameters[0];
			}
		}
		setHeights(new Matrix(heightMeasures), estimationMethod);
		
		
		AbstractStemTaperEstimate prediction = new SchneiderStemTaperEstimate(heightMeasures);
		
		Matrix pred = new Matrix(heights.m_iRows, 1);
		Matrix plotRandomEffects = getPlotRandomEffects(tree.getStand());
		Matrix treeRandomEffects = getTreeRandomEffects(tree);
		
		Matrix sumOfRandomEffects = plotRandomEffects.add(treeRandomEffects);
		double randomEffects0 = sumOfRandomEffects.m_afData[0][0];
		double randomEffects1 = sumOfRandomEffects.m_afData[1][0];
		if (!isResidualVariabilityEnabled) {
			if (estimationMethod == EstimationMethodInDeterministicMode.SecondOrder || estimationMethod == EstimationMethodInDeterministicMode.SecondOrderMeanOnly) {
				setCorrectionMatrix();
			} 
			residualErrors.resetMatrix(); 
		} else {
			residualErrors = rMatrixChol.multiply(StatisticalUtility.drawRandomVector(heights.m_iRows, Distribution.Type.GAUSSIAN));
		}
			
		Matrix parameters = linearExpressions.getValues();
		double alpha;
		double exponent;
		double correctionFactor = 0;
		for (int i = 0; i < heights.m_iRows; i++) {
			alpha = parameters.m_afData[i][0] + randomEffects0;
			exponent = parameters.m_afData[i][1] + randomEffects1;
			if (!isResidualVariabilityEnabled) {
				if (estimationMethod == EstimationMethodInDeterministicMode.SecondOrder || estimationMethod == EstimationMethodInDeterministicMode.SecondOrderMeanOnly) {
					correctionFactor = correctionMatrix.m_afData[i][0];
				}
			}	
			pred.m_afData[i][0] = alpha * tree.getSquaredDbhCm() * 100 * coreExpression.m_afData[i][0] * Math.pow(heightsSectionRespectToDbh.m_afData[i][0], 2 - exponent) 
					+ correctionFactor + residualErrors.m_afData[i][0];
		}
		
		prediction.setMean(pred);
		
		if (!isResidualVariabilityEnabled) {
			if (estimationMethod == EstimationMethodInDeterministicMode.FirstOrder || estimationMethod == EstimationMethodInDeterministicMode.SecondOrder) {
				prediction.setVariance(getStemTaperVariance(estimationMethod));
			}
		}
		
		return prediction;
	}

	private Matrix getTreeRandomEffects(StemTaperTree tree) {
		if (getDefaultRandomEffects(tree.getHierarchicalLevel()) != null) {
			return getRandomEffectsForThisSubject(tree); 
		}
		return FakeMatrixForMissingRandomEffects;
	}

	private Matrix getPlotRandomEffects(StemTaperStand stand) {
		if (getDefaultRandomEffects(stand.getHierarchicalLevel()) != null) {
			return getRandomEffectsForThisSubject(stand); 
		}
		return FakeMatrixForMissingRandomEffects;
	}

	/**
	 * This method sets the correction matrix.
	 */
	private void setCorrectionMatrix() {
		Matrix correctionFactor = new Matrix(heights.m_iRows, 1);
		Matrix hessians = linearExpressions.getHessians();
		StemTaperTreeSpecies species = tree.getStemTaperTreeSpecies();
		int index = StemTaperEquationSettings.getInterceptLocation(species, modelType); // we need to know where is the intercept for the z matrix +1 for the alpha parameter 
		List<Integer> indices = new ArrayList<Integer>();		// this variable serves to extract the proper zMatrix from the xMatrix
		indices.add(0);											// for the alpha parameters
		if (species == StemTaperTreeSpecies.PEG) {	// species is a little bit different because two parameters applied for a single variable
			indices.add(index + 2);
		} else {
			indices.add(index + 1);
		}
		Matrix zPrime;
		Matrix xPrime;
//		Matrix gPlot = getDefaultRandomEffects(HierarchicalLevel.PLOT).getVariance();
//		Matrix gTree = getDefaultRandomEffects(HierarchicalLevel.TREE).getVariance();
		Matrix gPlot = getRandomEffectVariance(HierarchicalLevel.PLOT);
		Matrix gTree = getRandomEffectVariance(HierarchicalLevel.TREE);
		Matrix variances = gPlot.add(gTree);
		for (int i = 0; i < correctionFactor.m_iRows; i++) {
			xPrime = hessians.getSubMatrix(i, i, 0, hessians.m_iCols - 1).transpose().squareSym();
			zPrime = xPrime.getSubMatrix(indices, indices);
			
			MatrixUtility.elementWiseMultiply(zPrime, variances);
			MatrixUtility.elementWiseMultiply(xPrime, getParameterEstimates().getVariance());
			
			correctionFactor.m_afData[i][0] = zPrime.getSumOfElements() * .5 + xPrime.getSumOfElements() * .5;
		}
		correctionMatrix = correctionFactor;
	}

	private Matrix getRandomEffectVariance(HierarchicalLevel level) {
		if (getDefaultRandomEffects(level) != null) {
			return getDefaultRandomEffects(level).getVariance();
		} else {
			return FakeVarianceMatrixForMissingRandomEffects;
		}
	}

	/**
	 * This method computes the analytical variance according to the analytical estimator.
	 * @return a Matrix instance
	 */
	private Matrix getStemTaperVariance(EstimationMethodInDeterministicMode estimationMethod) {
		Matrix gradients = linearExpressions.getGradients(); 
		Matrix gPlot = getRandomEffectVariance(HierarchicalLevel.PLOT);
		Matrix gTree = getRandomEffectVariance(HierarchicalLevel.TREE);
		Matrix z = gradients.getSubMatrix(0, gradients.m_iRows - 1, 0, 1);
		
		Matrix fixedEffectParameterPart = gradients.multiply(getParameterEstimates().getVariance()).multiply(gradients.transpose());
		Matrix zGPlotzT = z.multiply(gPlot).multiply(z.transpose());
		Matrix zGTreezT = z.multiply(gTree).multiply(z.transpose());
		Matrix v = zGPlotzT;
		MatrixUtility.add(v, zGTreezT);
		MatrixUtility.add(v, rMatrix);
		MatrixUtility.add(v, fixedEffectParameterPart);
		Matrix w = v;
		
		if (estimationMethod == EstimationMethodInDeterministicMode.SecondOrder) {
			MatrixUtility.subtract(w, correctionMatrix.multiply(correctionMatrix.transpose()));		// c*cT
			Matrix isserlisComponent;
			try {
				isserlisComponent = getIsserlisVarianceComponents();
				MatrixUtility.add(w, isserlisComponent);
			} catch (Exception e) {
				System.out.println("StemTaperEquation.getVolumeVariance(): Unable to calculate the isserlisComponent Matrix, will assume this matrix is null!");
				e.printStackTrace();
			}
		}
		return w;
	}

	
	/**
	 * This method returns the variance component that is due to the second term of the Taylor expansion. It
	 * relies on Isserlis' theorem.
	 * @return a Matrix instance
	 * @throws Exception 
	 */
	private Matrix getIsserlisVarianceComponents() {
		Matrix output = new Matrix(heights.m_iRows, heights.m_iRows);
		Matrix hessians = linearExpressions.getHessians();
		Matrix xPrimeI;
		Matrix zPrimeI;
		Matrix xPrimeJ;
		Matrix zPrimeJ;
		double result;
		Matrix isserlisPlot = getRandomEffectVariance(HierarchicalLevel.PLOT).getIsserlisMatrix();
		Matrix isserlisTree = getRandomEffectVariance(HierarchicalLevel.TREE).getIsserlisMatrix();
		MatrixUtility.add(isserlisPlot, isserlisTree);
		Matrix isserlisCombine = isserlisPlot;
		Matrix isserlisOmega = getParameterEstimates().getVariance().getIsserlisMatrix();
		for (int i = 0; i < heights.m_iRows; i++) {
			xPrimeI = hessians.getSubMatrix(i, i, 0, hessians.m_iCols - 1).transpose().squareSym();
			zPrimeI = xPrimeI.getSubMatrix(0, 1, 0, 1);
			for (int j = i; j < heights.m_iRows; j++) {
				xPrimeJ = hessians.getSubMatrix(j, j, 0, hessians.m_iCols - 1).transpose().squareSym();
				zPrimeJ = xPrimeJ.getSubMatrix(0, 1, 0, 1);
				// a .25 factor has been added MF20110919 (had been forgotten in the first version). Needed because
				// the second order term is always multiplied by .5 and consequently the product of
				// two second order term is always affected by a .25 constant.
				Matrix zPKronzP = zPrimeI.getKroneckerProduct(zPrimeJ);
				MatrixUtility.elementWiseMultiply(zPKronzP, isserlisCombine);
				
				Matrix xPKronxP = xPrimeI.getKroneckerProduct(xPrimeJ);
				MatrixUtility.elementWiseMultiply(xPKronxP, isserlisOmega);
				
				result = zPKronzP.getSumOfElements() * .25 + xPKronxP.getSumOfElements() * .25;
//				result = zPrimeI.getKroneckerProduct(zPrimeJ).elementWiseMultiply(isserlisCombine).getSumOfElements() + xPrimeI.getKroneckerProduct(xPrimeJ).elementWiseMultiply(isserlisOmega).getSumOfElements();
				output.m_afData[i][j] = result;
				if (i != j) {
					output.m_afData[j][i] = result;		// to ensure the symmetry and not to have to calculate again
				}
			}
		}
		return output;
	}

	
	/*
	 * For extended visibility in this package (non-Javadoc)
	 * @see repicea.simulation.REpiceaPredictor#setParameterEstimates(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	protected final void setParameterEstimates(GaussianEstimate gaussianEstimate) {
		super.setParameterEstimates(gaussianEstimate);
	}

	/*
	 * For extended visibility in this package (non-Javadoc)
	 * @see repicea.simulation.REpiceaPredictor#setParameterEstimates(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	protected final void setDefaultRandomEffects(HierarchicalLevel level, Estimate<? extends StandardGaussianDistribution> newEstimate) {
		super.setDefaultRandomEffects(level, newEstimate);
	}

	
	/*
	 * Useless. Everything is read in the StemTaperPredictor class (non-Javadoc)
	 * @see repicea.simulation.REpiceaPredictor#init()
	 */
	@Override
	protected void init() {}

	/**
	 * This method returns the tree whose taper is being predicted.
	 * @return a StemTaperTree instance
 	 */
	protected StemTaperTree getTree() {return tree;}

	@Override
	public void clear() {
		clearDeviates();
	}

	
}
