/*
 * English version follows
 * 
 * Ce fichier fait partie de la biblioth�que mrnf-foresttools.
 * Il est prot�g� par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du minist�re des Ressources naturelles et de la Faune du Gouvernement du 
 * Qu�bec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Qu�bec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.predictor.stemtaper.schneiderequations;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperEquationSettings.ModelType;
import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperTree.StemTaperTreeSpecies;
import repicea.math.Matrix;
import repicea.math.MatrixUtility;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ParameterLoader;
import repicea.simulation.stemtaper.StemTaperCrossSection;
import repicea.simulation.stemtaper.StemTaperEstimate;
import repicea.simulation.stemtaper.StemTaperModel;
import repicea.simulation.stemtaper.StemTaperSegmentList;
import repicea.stats.Distribution;
import repicea.stats.StatisticalUtility;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

/**
 * The StemTaperEquation class predicts the taper and the volume of different height section in trees.
 * The class must be used as follows
 * </br>
 * </br>
 * {@code StemTaperPredictor ste = new StemTaperPredictor();} </br>
 * {@code ste.setTree(}<i>{@code myTree}</i>{@code );}</br>
 * {@code StemTaperEstimate te = ste.predictTaperForTheseHeights(}<i>{@code myRowMatrixThatContainsTheHeightSections}</i>{@code );} </br>
 * {@code Estimate volumeEstimate = te.getVolumeEstimate()} </br>
 * </br>
 * 
 * NOTE: this method is not synchronized!
 * 
 * @author Mathieu Fortin - September 2011
 */
public final class StemTaperPredictor extends StemTaperModel {
	
	@SuppressWarnings("serial")
	public static class SchneiderStemTaperEstimate extends StemTaperEstimate {
		
		public SchneiderStemTaperEstimate(boolean isMonteCarlo, List<Double> computedHeights) {
			super(isMonteCarlo, computedHeights);
		}

		@Override
		protected Matrix getSquaredDiameters(Matrix predictedDiameters) {
			return predictedDiameters;		// already squared
		}

		@Override
		protected Matrix getVarianceOfSquaredDiameter(Matrix variancePredictedDiameters) {
			return variancePredictedDiameters; // already squared
		}

		@Override
		protected double getScalingFactor() {
			return Math.PI *.25 * 1E-3;
		}
		
	}
	
	
	
	private static final long serialVersionUID = 20120110L;
	
	public static int[] timer = new int[15]; 

	private static boolean loaded;
	
	private static Map<ModelType, Map<StemTaperTreeSpecies, GaussianEstimate>>	betaMatrixReferenceMap			= new HashMap<ModelType, Map<StemTaperTreeSpecies, GaussianEstimate>>();
	private static Map<ModelType, Map<StemTaperTreeSpecies, GaussianEstimate>>	plotRandomEffectReferenceMap	= new HashMap<ModelType, Map<StemTaperTreeSpecies, GaussianEstimate>>();
	private static Map<ModelType, Map<StemTaperTreeSpecies, GaussianEstimate>>	treeRandomEffectReferenceMap	= new HashMap<ModelType, Map<StemTaperTreeSpecies, GaussianEstimate>>();
	private static Map<ModelType, Map<StemTaperTreeSpecies, Matrix>>		varianceParamReferenceMap		= new HashMap<ModelType, Map<StemTaperTreeSpecies, Matrix>>();
	private static Map<ModelType, Map<StemTaperTreeSpecies, Matrix>>		corrParamReferenceMap			= new HashMap<ModelType, Map<StemTaperTreeSpecies, Matrix>>();
	private static Map<ModelType, Map<StemTaperTreeSpecies, Matrix>>		resStdDevReferenceMap			= new HashMap<ModelType, Map<StemTaperTreeSpecies, Matrix>>();
	
	public static enum EstimationMethod {FirstOrder, 
		SecondOrder, 
		MonteCarlo,
		FirstOrderMeanOnly,
		SecondOrderMeanOnly}

	private double rho;
	private double varFunctionParm1;
	private double varFunctionParm2;
	private Double residualStdDev;

	private InternalStatisticalExpressions linearExpressions;
	
	protected Matrix heights;
	
	protected Matrix relativeHeights;
	protected Matrix coreExpression;
	protected Matrix heightsSectionRespectToDbh;
	
	private Matrix plotRandomEffects;
	private Matrix treeRandomEffects;
	
	private Matrix rMatrix;
	private Matrix rMatrixChol;
	private Matrix residualErrors;
	
	private Matrix correctionMatrix;
	private int numberOfMonteCarloRealizations;
	
	private StemTaperTree tree;
	private StemTaperTreeSpecies species;
	
	private EstimationMethod estimationMethod;
	private boolean isResetNeeded;
	protected ModelType typeModel;	//the type of equation model to use Tree or Hybrid
		
	/**
	 * Constructor.
	 * @throws Exception
	 */
	public StemTaperPredictor() {
		super(false, false, false);
		
		init();
	
		estimationMethod = EstimationMethod.SecondOrder;
		setNumberOfMonteCarloRealizations(1); 	
		typeModel = ModelType.HYBRIDMODEL;	//default model
	}

	/**
	 * This method sets the number of Monte Carlo realizations. By default, this number is set to 1, which means the
	 * model is to be used in a deterministic manner. 
	 * @param numberOfMonteCarloRealizations an Integer
	 */
	private void setNumberOfMonteCarloRealizations(int numberOfMonteCarloRealizations) {
		this.numberOfMonteCarloRealizations = numberOfMonteCarloRealizations;
		isParametersVariabilityEnabled = estimationMethod == EstimationMethod.MonteCarlo;
		isRandomEffectsVariabilityEnabled = estimationMethod == EstimationMethod.MonteCarlo;
		isResidualVariabilityEnabled = estimationMethod == EstimationMethod.MonteCarlo;
	}
	
	@Override
	protected final void init() {
		if (!loaded) {
			loadDefaultParameters();
		}
	}
	
	
	
	/**
	 * This method computes the stem taper in mm2.
	 * @param heightMeasures a List of Double that represent the height (m)
	 * @return a StemTaperEstimate instance with the cross section squared diameter
	 */
	public StemTaperEstimate getPredictedTaperForTheseHeights(BasicStemTaperTree t, List<Double> heightMeasures) {
		if (!(t instanceof StemTaperTree)) {
			throw new InvalidParameterException("The StemTaperPredictor class is designed to work with StemTaperTree instances only!"); 
		}
		StemTaperTree tree = (StemTaperTree) t;
		setTree(tree);
		setHeights(new Matrix(heightMeasures));
		Matrix pred;
		
		double randomEffects0 = 0d;
		double randomEffects1 = 0d;
		
		int numberOfRuns = 1;
		if (estimationMethod == EstimationMethod.MonteCarlo) {
			numberOfRuns = numberOfMonteCarloRealizations;
		}
		
		StemTaperEstimate prediction = new SchneiderStemTaperEstimate(estimationMethod == EstimationMethod.MonteCarlo, heightMeasures);
		
		for (int iter = 0; iter < numberOfRuns; iter++) {
			pred = new Matrix(heights.m_iRows, 1);
			if (estimationMethod == EstimationMethod.MonteCarlo) {
//				tree.setMonteCarloRealizationId(iter);
//				tree.getStand().setMonteCarloRealizationId(iter);
				randomizeCoefficients();
				randomEffects0 = plotRandomEffects.m_afData[0][0] + treeRandomEffects.m_afData[0][0];
				randomEffects1 = plotRandomEffects.m_afData[1][0] + treeRandomEffects.m_afData[1][0];
			} else {
				if (isResetNeeded) {
					resetCoefficients();
				}
				if (estimationMethod == EstimationMethod.SecondOrder || estimationMethod == EstimationMethod.SecondOrderMeanOnly) {
					setCorrectionMatrix();
				} 
			}
			
			Matrix parameters = linearExpressions.getValues();
			double alpha;
			double exponent;
			double correctionFactor = 0;
			for (int i = 0; i < heights.m_iRows; i++) {
				alpha = parameters.m_afData[i][0] + randomEffects0;
				exponent = parameters.m_afData[i][1] + randomEffects1; 
				if (estimationMethod == EstimationMethod.SecondOrder || estimationMethod == EstimationMethod.SecondOrderMeanOnly) {
					correctionFactor = correctionMatrix.m_afData[i][0];
				}
				pred.m_afData[i][0] = alpha * tree.getSquaredDbhCm() * 100 * coreExpression.m_afData[i][0] * Math.pow(heightsSectionRespectToDbh.m_afData[i][0], 2 - exponent) 
						+ correctionFactor + residualErrors.m_afData[i][0];
			}
			
			if (estimationMethod == EstimationMethod.MonteCarlo) {
				prediction.addRealization(pred);
			} else {
				prediction.setMean(pred);
			}
		}
		
		if (estimationMethod == EstimationMethod.FirstOrder || estimationMethod == EstimationMethod.SecondOrder) {
			prediction.setVariance(getStemTaperVariance());
		}
		
		return prediction;

	}
	
	
	/**
	 * This method computes the stem taper in mm2.
	 * @param stemTaperSegments a List of StemTaperSegment instances
	 * @return a StemTaperEstimate instance with the cross section diameter in mm2
	 */
	public StemTaperEstimate getPredictedTaperForTheseSegments(StemTaperTree tree, StemTaperSegmentList stemTaperSegments) {		
		List<Double> currentHeightsToEvaluate = stemTaperSegments.getHeightsWithoutReplicates();	
		return getPredictedTaperForTheseHeights(tree, currentHeightsToEvaluate);		
	}
	
	

	/**
	 * This method generates random deviates for all the Monte Carlo components of the model.
	 */
	private void randomizeCoefficients() {
		plotRandomEffects = getRandomEffectsForThisSubject(tree.getStand());
		treeRandomEffects = getRandomEffectsForThisSubject(tree);
		Matrix beta = getParametersForThisRealization(tree.getStand());
		linearExpressions.setParameters(beta);
		residualErrors = rMatrixChol.multiply(StatisticalUtility.drawRandomVector(heights.m_iRows, Distribution.Type.GAUSSIAN));
		isResetNeeded = true;
	}
	
	
	/**
	 * This method resets all the Monte Carlo components to 0.
	 */
	private void resetCoefficients() {
		plotRandomEffects = getDefaultRandomEffects(HierarchicalLevel.PLOT).getMean();
		treeRandomEffects = getDefaultRandomEffects(HierarchicalLevel.TREE).getMean();
		linearExpressions.setParameters(getParameterEstimates().getMean());
		residualErrors.resetMatrix();
		isResetNeeded = false;
	}

	
	/**
	 * This method sets the tree height and dbh. The StemTaperTree object provides these
	 * two variables.
	 * @param tree a StemTaperTree instance
	 */
	private void setTree(StemTaperTree tree) {
		ModelType typeModelNew = StemTaperEquationSettings.getModelTypeEquation(tree);
		if (species == null || !species.equals(tree.getStemTaperTreeSpecies()) || typeModelNew != typeModel) {	// if the species is different update the parameters of the equation
			species = tree.getStemTaperTreeSpecies();
			typeModel = typeModelNew;
			setVersion(species);
			isResetNeeded = true;
		}
		this.tree = tree;
	}
	
	/**
	 * This method sets the estimation method. It is the correction factor for marginal predictions. BY DEFAULT, this method is 
	 * set to the second order estimation method.
	 * @param estimationMethod a Mode enum variable 
	 */
	public void setEstimationMethod(EstimationMethod estimationMethod) {this.estimationMethod = estimationMethod;}

	
	/**
	 * This method sets the height sections.
	 * @param heights a Matrix (a column vector) that contains the location of the height sections (m)
	 * @throws Exception
	 */
	private void setHeights(Matrix heights) {
		if (!heights.equals(this.heights)) {
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
			if (estimationMethod == EstimationMethod.FirstOrder || estimationMethod == EstimationMethod.SecondOrder) {
				setRMatrix();
			}
			residualErrors = new Matrix(heights.m_iRows, 1);
		}
	}
	
	
	private synchronized void loadDefaultParameters() {
		if (!loaded) {
			try {
				for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
					for (ModelType modelType : ModelType.values()) {
						if (species == StemTaperTreeSpecies.PIB && modelType == ModelType.HYBRIDMODEL) {
							break;//no hybrid model for PIB
						}
						String path = ObjectUtility.getRelativePackagePath(getClass());
						String prefix = modelType + "param" + "/" + species.name().toLowerCase() + "/";
						prefix = prefix.toLowerCase();
						String suffix = species.name().toUpperCase().concat(".csv");

						String parameterFilename = path + prefix + "parameters".concat(suffix);
						String omegaFilename = path + prefix + "omega".concat(suffix);
						String plotRandomEffectsFilename = path + prefix + "plotRandomEffects".concat(suffix);
						String treeRandomEffectsFilename = path + prefix + "treeRandomEffects".concat(suffix);
						String correlationStructureFilename = path + prefix + "corrStruct".concat(suffix);
						String varFunctFilename = path + prefix + "varFunct".concat(suffix);
						String residStdDevFilename = path + prefix + "residualStdDev".concat(suffix);

						Matrix beta = ParameterLoader.loadVectorFromFile(parameterFilename).get();
						Matrix omega = ParameterLoader.loadMatrixFromFile(omegaFilename);

						if (StemTaperPredictor.betaMatrixReferenceMap.get(modelType) == null) {
							StemTaperPredictor.betaMatrixReferenceMap.put(modelType, new HashMap<StemTaperTreeSpecies, GaussianEstimate>());
						}
						StemTaperPredictor.betaMatrixReferenceMap.get(modelType).put(species, new GaussianEstimate(beta, omega));

						Matrix g = ParameterLoader.loadMatrixFromFile(plotRandomEffectsFilename);
						if (StemTaperPredictor.plotRandomEffectReferenceMap.get(modelType) == null) {
							StemTaperPredictor.plotRandomEffectReferenceMap.put(modelType, new HashMap<StemTaperTreeSpecies, GaussianEstimate>());
						}
						StemTaperPredictor.plotRandomEffectReferenceMap.get(modelType).put(species, new GaussianEstimate(new Matrix(g.m_iRows, 1), g));

						g = ParameterLoader.loadMatrixFromFile(treeRandomEffectsFilename);
						if (StemTaperPredictor.treeRandomEffectReferenceMap.get(modelType) == null) {
							StemTaperPredictor.treeRandomEffectReferenceMap.put(modelType, new HashMap<StemTaperTreeSpecies, GaussianEstimate>());
						}
						StemTaperPredictor.treeRandomEffectReferenceMap.get(modelType).put(species, new GaussianEstimate(new Matrix(g.m_iRows, 1), g));

						Matrix oVec = ParameterLoader.loadVectorFromFile(varFunctFilename).get();
						if (StemTaperPredictor.varianceParamReferenceMap.get(modelType) == null) {
							StemTaperPredictor.varianceParamReferenceMap.put(modelType, new HashMap<StemTaperTreeSpecies, Matrix>());
						}
						StemTaperPredictor.varianceParamReferenceMap.get(modelType).put(species, oVec);

						oVec = ParameterLoader.loadVectorFromFile(correlationStructureFilename).get();
						double currentValue, expCurrentValue;
						for (int i = 0; i < oVec.m_iRows; i++) {		// link function following the implementation in nlme
							currentValue = oVec.m_afData[i][0];
							expCurrentValue = Math.exp(currentValue);
							oVec.m_afData[i][0] = expCurrentValue / (1 + expCurrentValue);
						}
						if (StemTaperPredictor.corrParamReferenceMap.get(modelType) == null) {
							StemTaperPredictor.corrParamReferenceMap.put(modelType, new HashMap<StemTaperTreeSpecies, Matrix>());
						}
						StemTaperPredictor.corrParamReferenceMap.get(modelType).put(species, oVec);

						oVec = ParameterLoader.loadVectorFromFile(residStdDevFilename).get();
						if (StemTaperPredictor.resStdDevReferenceMap.get(modelType) == null) {
							StemTaperPredictor.resStdDevReferenceMap.put(modelType, new HashMap<StemTaperTreeSpecies, Matrix>());
						}
						StemTaperPredictor.resStdDevReferenceMap.get(modelType).put(species, oVec);
					}
				}
				loaded = true;
				
			} catch (IOException e) {
				System.out.println("Error while reading parameters in StemTaperPredictor class");
			}
		}
	}
	
	
	
	/**
	 * This method initializes all the parameters of the stem taper model.
	 * 
	 * @param species the tree species
	 * @param pModelType the equation model type
	 * @throws Exception
	 */
	private void setVersion(StemTaperTreeSpecies species) {
		linearExpressions = new InternalStatisticalExpressions(this, species);

		setParameterEstimates(StemTaperPredictor.betaMatrixReferenceMap.get(typeModel).get(species));
		linearExpressions.setParameters(getParameterEstimates().getMean());

//		defaultRandomEffects.clear();
		setDefaultRandomEffects(HierarchicalLevel.PLOT, StemTaperPredictor.plotRandomEffectReferenceMap.get(typeModel).get(species));
		setDefaultRandomEffects(HierarchicalLevel.TREE, StemTaperPredictor.treeRandomEffectReferenceMap.get(typeModel).get(species));
		
		Matrix varianceParameters = StemTaperPredictor.varianceParamReferenceMap.get(typeModel).get(species);
		if (varianceParameters.m_iRows > 1) {
			this.varFunctionParm1 = Math.exp(varianceParameters.m_afData[0][0]);
			this.varFunctionParm2 = varianceParameters.m_afData[1][0];
		} else {
			this.varFunctionParm1 = 1d;
			this.varFunctionParm2 = varianceParameters.m_afData[0][0];
		}
		
		Matrix correlationParameters = StemTaperPredictor.corrParamReferenceMap.get(typeModel).get(species);
		this.rho = correlationParameters.m_afData[0][0];
		
		Matrix residualStdDev = StemTaperPredictor.resStdDevReferenceMap.get(typeModel).get(species);
		this.residualStdDev = residualStdDev.m_afData[0][0];
		
	}
	
	
	

	/**
	 * This method returns the volume of a series of height sections. The height sections are first sorted and the 
	 * volume is calculated between the first and the last one by summing the volume of the log between the successive 
	 * height sections. The volume is calculated using Smalian's formula.
	 * @param tree a StemTaperTree instance
	 * @return the volume in dm3 or -1 if the volume cannot be calculated
	 */
	@SuppressWarnings("unchecked")
	public static double getVolumeThroughSmalianFormula(StemTaperTree tree) {
		List<StemTaperCrossSection> heightSections = tree.getCrossSections();
		
		if (heightSections != null && !heightSections.isEmpty() && heightSections.size() > 1) {
			Collections.sort(heightSections);
			
			double largeDiameter;
			double smallDiameter;
			double logLength;
			double logVolume;
			double volume = 0d;
			double factor = 1d / (8 * 1000);	// to express the volume in dm3
			for (int i = 1; i < heightSections.size(); i++) {
				largeDiameter = heightSections.get(i - 1).getSectionDiameter();
				smallDiameter = heightSections.get(i).getSectionDiameter();
				logLength = heightSections.get(i).getSectionHeight() - heightSections.get(i - 1).getSectionHeight();
				logVolume = Math.PI * logLength * factor * (largeDiameter * largeDiameter + smallDiameter * smallDiameter);
				volume += logVolume;
			}
			return volume;
			
		} else {
			return -1d;
		}
		
	}
	
	
	/**
	 * This method computes the analytical variance according to the analytical estimator.
	 * @return a Matrix instance
	 */
	private Matrix getStemTaperVariance() {
		Matrix gradients = linearExpressions.getGradients(); 
		Matrix gPlot = getDefaultRandomEffects(HierarchicalLevel.PLOT).getVariance();
		Matrix gTree = getDefaultRandomEffects(HierarchicalLevel.TREE).getVariance();
		Matrix z = gradients.getSubMatrix(0, gradients.m_iRows - 1, 0, 1);
		
		Matrix fixedEffectParameterPart = gradients.multiply(getParameterEstimates().getVariance()).multiply(gradients.transpose());
		Matrix zGPlotzT = z.multiply(gPlot).multiply(z.transpose());
		Matrix zGTreezT = z.multiply(gTree).multiply(z.transpose());
		Matrix v = zGPlotzT;
		MatrixUtility.add(v, zGTreezT);
		MatrixUtility.add(v, rMatrix);
		MatrixUtility.add(v, fixedEffectParameterPart);
		Matrix w = v;
		
		if (estimationMethod == EstimationMethod.SecondOrder) {
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
	 * This method sets the correction matrix.
	 */
	private void setCorrectionMatrix() {
		Matrix correctionFactor = new Matrix(heights.m_iRows, 1);
		Matrix hessians = linearExpressions.getHessians();
		StemTaperTreeSpecies species = tree.getStemTaperTreeSpecies();
		int index = StemTaperEquationSettings.getInterceptLocation(species, typeModel); // we need to know where is the intercept for the z matrix +1 for the alpha parameter 
		List<Integer> indices = new ArrayList<Integer>();		// this variable serves to extract the proper zMatrix from the xMatrix
		indices.add(0);											// for the alpha parameters
		if (species == StemTaperTreeSpecies.PEG) {	// species is a little bit different because two parameters applied for a single variable
			indices.add(index + 2);
		} else {
			indices.add(index + 1);
		}
		Matrix zPrime;
		Matrix xPrime;
		Matrix gPlot = getDefaultRandomEffects(HierarchicalLevel.PLOT).getVariance();
		Matrix gTree = getDefaultRandomEffects(HierarchicalLevel.TREE).getVariance();
		Matrix variances = gPlot.add(gTree);
		for (int i = 0; i < correctionFactor.m_iRows; i++) {
			xPrime = hessians.getSubMatrix(i, i, 0, hessians.m_iCols - 1).transpose().squareSym();
//			zPrime = xPrime.getSubMatrix(0,1,0,1);
			zPrime = xPrime.getSubMatrix(indices, indices);
			
			MatrixUtility.elementWiseMultiply(zPrime, variances);
			MatrixUtility.elementWiseMultiply(xPrime, getParameterEstimates().getVariance());
			
			correctionFactor.m_afData[i][0] = zPrime.getSumOfElements() * .5 + xPrime.getSumOfElements() * .5;
		}
		correctionMatrix = correctionFactor;
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
		Matrix isserlisPlot = getDefaultRandomEffects(HierarchicalLevel.PLOT).getVariance().getIsserlisMatrix();
		Matrix isserlisTree = getDefaultRandomEffects(HierarchicalLevel.TREE).getVariance().getIsserlisMatrix();
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
	
	
	/**
	 * This method returns the R matrix.
	 * @return a Matrix instance
	 */
	private void setRMatrix() {
		Matrix distance = heights.repeat(1, heights.m_iRows).subtract(heights.transpose().repeat(heights.m_iRows, 1)).getAbsoluteValue();
		Matrix correlations = distance.powMatrix(rho);			
		Matrix x = heights.scalarAdd(-1.3).scalarMultiply(1d / (tree.getHeightM() - 1.3)).getAbsoluteValue();
		Matrix l = heights.scalarMultiply(-1d).scalarAdd(tree.getHeightM());
		Matrix stdDevMatrix = l.elementWiseMultiply(x).elementWiseMultiply(x.scalarMultiply(-1d).scalarAdd(1d).elementWisePower(3d)).elementWisePower(varFunctionParm2)
								.scalarAdd(varFunctionParm1).matrixDiagonal();
		rMatrix = stdDevMatrix.multiply(correlations).multiply(stdDevMatrix).scalarMultiply(residualStdDev * residualStdDev);
		rMatrixChol = rMatrix.getLowerCholTriangle();
	}
	
	
	/**
	 * This method returns the tree whose taper is being predicted.
	 * @return a StemTaperTree instance
 	 */
	protected StemTaperTree getTree() {return tree;}
	
}
