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
package quebecmrnfutility.predictor.stemtaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;

import repicea.math.Matrix;
import repicea.simulation.ModelBasedSimulator;
import repicea.stats.Distribution;
import repicea.stats.StatisticalUtility;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

/**
 * The StemTaperEquation class predicts the taper and the volume of different height section in trees.
 * The class must be used as follows
 * </br>
 * </br>
 * {@code StemTaperEquation ste = new StemTaperEquation();} </br>
 * {@code ste.setTree(}<i>{@code myTree}</i>{@code );}</br>
 * {@code ste.predictTaperForTheseHeights(}<i>{@code myRowMatrixThatContainsTheHeightSections}</i>{@code );} </br>
 * {@code ste.predictVolume(}<i>{@code myRowMatrixThatContainsTheHeightSections}</i>{@code );} </br>
 * 
 * @author Mathieu Fortin - September 2011
 */
@Deprecated
@SuppressWarnings({"rawtypes","unchecked"})
public final class StemTaperEquation extends ModelBasedSimulator {
	
	private static final long serialVersionUID = 20120110L;

	public static enum EstimationMethod {FirstOrder, SecondOrder, MonteCarlo}

	private double rho;
	private double varFunctionParm1;
	private double varFunctionParm2;
	private Double residualStdDev;

	private InternalLinearExpressions linearExpressions;
	
	protected double treeHeight;
	protected double treeDbh;
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
	private double deltaH;
	
	private StemTaperTree tree;
	
	private EstimationMethod estimationMethod = EstimationMethod.SecondOrder;
		
	/**
	 * Constructor.
	 * @throws Exception
	 */
	public StemTaperEquation() throws Exception {
		super(false, false, false);
		init();
		setNumberOfMonteCarloRealizations(1); 		// default is deterministic
	}

	/**
	 * This method sets the number of Monte Carlo realizations. By default, this number is set to 1, which means the
	 * model is to be used in a deterministic manner. 
	 * @param numberOfMonteCarloRealizations an Integer
	 */
	public void setNumberOfMonteCarloRealizations(int numberOfMonteCarloRealizations) {
		this.numberOfMonteCarloRealizations = numberOfMonteCarloRealizations;
		isParametersVariabilityEnabled = numberOfMonteCarloRealizations > 1;
		isRandomEffectsVariabilityEnabled = numberOfMonteCarloRealizations > 1;
		isResidualVariabilityEnabled = numberOfMonteCarloRealizations > 1;
	}
	
	/**
	 * This method computes the stem taper.
	 * @param heights a Matrix that contains the height section
	 * @return a MonteCarloEstimate instance
	 * @throws Exception 
	 */
	public MonteCarloEstimate predictTaperForTheseHeights(Matrix heights) throws Exception {
		setHeights(heights);
		Matrix pred;
		
		double randomEffects0 = 0d;
		double randomEffects1 = 0d;
		
		int numberOfRuns = 1;
		if (estimationMethod == EstimationMethod.MonteCarlo) {
			numberOfRuns = this.numberOfMonteCarloRealizations;
		}
		MonteCarloEstimate prediction = new MonteCarloEstimate();

		for (int iter = 0; iter < numberOfRuns; iter++) {
			pred = new Matrix(heights.m_iRows, 1);
			if (estimationMethod == EstimationMethod.MonteCarlo) {
				tree.setMonteCarloRealizationId(iter);
				tree.getStand().setMonteCarloRealizationId(iter);
				randomizeCoefficients();
				randomEffects0 = plotRandomEffects.m_afData[0][0] + treeRandomEffects.m_afData[0][0];
				randomEffects1 = plotRandomEffects.m_afData[1][0] + treeRandomEffects.m_afData[1][0];
			} else {
				resetCoefficients();
				if (estimationMethod == EstimationMethod.SecondOrder) {
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
				if (estimationMethod == EstimationMethod.SecondOrder) {
					correctionFactor = correctionMatrix.m_afData[i][0];
				}
				pred.m_afData[i][0] = alpha * treeDbh * treeDbh * coreExpression.m_afData[i][0] * Math.pow(heightsSectionRespectToDbh.m_afData[i][0], 2 - exponent) 
						+ correctionFactor + residualErrors.m_afData[i][0];
			}
			
			prediction.addRealization(pred);
		}
		return prediction;
	}

	/**
	 * This method generates random deviates for all the Monte Carlo components of the model.
	 */
	private void randomizeCoefficients() throws Exception {
		plotRandomEffects = getRandomEffectsForThisSubject(tree.getStand());
		treeRandomEffects = getRandomEffectsForThisSubject(tree);
		Matrix beta = getParametersForThisRealization(tree.getStand());
		linearExpressions.setParameters(beta);
		residualErrors = rMatrixChol.multiply(StatisticalUtility.drawRandomVector(heights.m_iRows, Distribution.Type.GAUSSIAN, random));
	}
	
	
	/**
	 * This method resets all the Monte Carlo components to 0.
	 */
	private void resetCoefficients() {
		plotRandomEffects = defaultRandomEffects.get(HierarchicalLevel.Plot).getMean();
		treeRandomEffects = defaultRandomEffects.get(HierarchicalLevel.Tree).getMean();
		linearExpressions.setParameters(defaultBeta.getMean());
		residualErrors.resetMatrix();
	}

	
	/**
	 * This method sets the tree height and dbh. The StemTaperTree object provides these
	 * two variables.
	 * @param tree a StemTaperTree instance
	 */
	public void setTree(StemTaperTree tree) {
		this.tree = tree;
		treeHeight = tree.getHeight();
		treeDbh = tree.getDbh();
	}
	
	/**
	 * This method sets the estimation method. It is eighcorrection factor for marginal predictions. BY DEFAULT, this method is 
	 * set to the second order estimation method.
	 * @param estimationMethod a Mode enum variable 
	 */
	public void setEstimationMethod(EstimationMethod estimationMethod) {this.estimationMethod = estimationMethod;}

	
	/**
	 * This method sets the height sections.
	 * @param heights a Matrix (a column vector) that contains the location of the height sections (m)
	 * @throws Exception
	 */
	private void setHeights(Matrix heights) throws Exception {
		if (!heights.equals(this.heights)) {
			for (int i = 0; i < heights.m_iRows; i++) {
				heights.m_afData[i][0] = Math.round(heights.m_afData[i][0] * 1000) * 0.001;
			}
			if (heights.m_afData[heights.m_iRows - 1][0] >= treeHeight) {
				heights.m_afData[heights.m_iRows - 1][0] = treeHeight - 1E-4;
			}
			this.heights = heights;
			relativeHeights = heights.scalarMultiply(1d / treeHeight);
			Matrix treeHeightMatrix = new Matrix(heights.m_iRows, 1, treeHeight, 0d);
			coreExpression =  treeHeightMatrix.subtract(heights).scalarMultiply(1d / (treeHeight - 1.3));
			heightsSectionRespectToDbh = heights.scalarMultiply(1d / 1.3);
			setRMatrix();
			residualErrors = new Matrix(heights.m_iRows, 1);
		}
	}
	
	/**
	 * This method initializes all the parameters of the stem taper model.
	 * @throws Exception
	 */
	private void init() throws Exception {
		linearExpressions = new InternalLinearExpressions(this);
		
		String path = ObjectUtility.getRelativePackagePath(getClass());
		InputStream isBeta = ClassLoader.getSystemResourceAsStream(path + "parameters.txt");
		InputStream isOmega = ClassLoader.getSystemResourceAsStream(path + "omega.txt");
		InputStream isPlotRandomEffects = ClassLoader.getSystemResourceAsStream(path + "plotRandomEffects.txt");
		InputStream isTreeRandomEffects = ClassLoader.getSystemResourceAsStream(path + "treeRandomEffects.txt");
		InputStream isCorrelationStructure = ClassLoader.getSystemResourceAsStream(path + "correlationStructure.txt");
		InputStream isVarianceFunction = ClassLoader.getSystemResourceAsStream(path + "varianceFunction.txt");
		InputStream isResidualStdDev = ClassLoader.getSystemResourceAsStream(path + "residualStdDev.txt");
		
		// fixed-effects parameters		
		Matrix defaultBetaMean = new Matrix(readParameters(isBeta));
		int numberOfParameters = defaultBetaMean.m_iRows;
		
		// variance-covariance matrix of the fixed-effects parameters
		Vector<Double> omegaElement = readParameters(isOmega);
		if (omegaElement.size() != numberOfParameters * numberOfParameters) {
			throw new IOException("Mismatch between the vector of parameters and the variance-covariance matrix: the number of elements is inappropriate!");
		}
		Matrix tmp = new Matrix(omegaElement);
		Matrix omega = null;
		for (int i = 0; i < numberOfParameters; i++) {
			if (omega == null) {
				omega = tmp.getSubMatrix(i * 7, (i + 1) * 7 - 1, 0, 0).transpose();
			} else {
				omega = omega.matrixStack(tmp.getSubMatrix(i * 7, (i + 1) * 7 - 1, 0, 0).transpose(), true);
			}
		}
		defaultBeta = new GaussianEstimate(defaultBetaMean, omega);
		linearExpressions.setParameters(defaultBeta.getMean());
		
		// plot random effects variances
		Vector<Double> randomEffectsParameters = readParameters(isPlotRandomEffects);
		randomEffectsParameters.remove(2);
		Matrix gPlot = new Matrix(randomEffectsParameters).squareSym();
		Matrix defaultPlotBlups = new Matrix(gPlot.m_iRows, 1);
		defaultRandomEffects.put(HierarchicalLevel.Plot, new GaussianEstimate(defaultPlotBlups, gPlot));

		// tree random effects variances
		randomEffectsParameters = readParameters(isTreeRandomEffects);
		randomEffectsParameters.remove(2);
		Matrix gTree = new Matrix(randomEffectsParameters).squareSym();
		Matrix defaultTreeBlups = new Matrix(gTree.m_iRows, 1);
		defaultRandomEffects.put(HierarchicalLevel.Tree, new GaussianEstimate(defaultTreeBlups, gTree));
		
		// variance function parameters
		Vector<Double> varianceParameters = readParameters(isVarianceFunction);
		this.varFunctionParm1 = Math.exp(varianceParameters.get(0));
		this.varFunctionParm2 = varianceParameters.get(1);
		
		// correlation function parameters
		Vector<Double> correlationParameters = readParameters(isCorrelationStructure);
		this.rho = Math.exp(correlationParameters.get(0));
		
		Vector<Double> residualStdDev = readParameters(isResidualStdDev);
		this.residualStdDev = residualStdDev.get(0);
	}
	
	/**
	 * A private method that reads a input stream and converts it into a Vector of Double instances
	 * @param is an InputStream object
	 * @return a Vector of Double instance
	 * @throws IOException
	 */
	private Vector<Double> readParameters(InputStream is) throws IOException {
		Vector<Double> parameters = new Vector<Double>();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String str = in.readLine();
		StringTokenizer tokenizer;
		while (str != null) {
			// comment / blank line : goes to next line
			if (!str.startsWith("#") && str.trim().length() != 0) {
				// System.err.println (str);

				tokenizer = new StringTokenizer(str, " ");

				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					try {
						parameters.add(Double.parseDouble(token));
					} catch (Exception e) {}
				}
			}
			str = in.readLine();
		}
		return parameters;
	}
	
	
	/**
	 * This method expands a matrix by copying all the lines and column except the first and the last one.
	 * @param m the input Matrix instance
	 * @return a new expanded matrix
	 */
	private Matrix expandMatrix(Matrix m) {
		Matrix firstTmpMatrix = null;
		Matrix tmp;
		
		for (int i = 0; i < m.m_iRows; i++) {
			if (i > 0 && i < m.m_iRows - 1) {
				firstTmpMatrix = firstTmpMatrix.matrixStack(m.getSubMatrix(i, i, 0, m.m_iCols - 1), true);
			}
			tmp = m.getSubMatrix(i, i, 0, m.m_iCols - 1);
			if (firstTmpMatrix == null) {
				firstTmpMatrix = tmp;
			} else {
				firstTmpMatrix = firstTmpMatrix.matrixStack(tmp, true);
			}
		}

		Matrix secondTmpMatrix = null;
		for (int j = 0; j < m.m_iCols; j++) {
			if (j > 0 && j < m.m_iCols - 1) {
				secondTmpMatrix = secondTmpMatrix.matrixStack(firstTmpMatrix.getSubMatrix(0, firstTmpMatrix.m_iRows - 1, j, j), false);
			}
			tmp = firstTmpMatrix.getSubMatrix(0, firstTmpMatrix.m_iRows - 1, j, j);
			if (secondTmpMatrix == null) {
				secondTmpMatrix = tmp;
			} else {
				secondTmpMatrix = secondTmpMatrix.matrixStack(tmp, false);
			}
		}
		
		return secondTmpMatrix;
	}
	
	/**
	 * This method estimates the volume of all the height sections.
	 * @param heights a Matrix of heights
	 * @return an Estimate instance
	 * @throws Exception
	 */
	public Estimate predictVolume(Matrix heights) throws Exception {
		deltaH = getHeightSectionLength(heights);
		double factor = Math.PI / 8 * deltaH * 1E-3; 							// 1E-3 is a factor to express the result in dm3
		
		Estimate result;
		
		int numberOfRuns = 1;
		if (estimationMethod == EstimationMethod.MonteCarlo) {
			numberOfRuns = numberOfMonteCarloRealizations;
			result = new MonteCarloEstimate();
		} else {
			result = new GaussianEstimate();
		}	

		MonteCarloEstimate<Matrix> taperEstimate = predictTaperForTheseHeights(heights);
		
		Matrix volumeEstim;
		for (int iter = 0; iter < numberOfRuns; iter++) {
			Matrix expandedTaper = expandMatrix(taperEstimate.getRealizations().get(iter));
			volumeEstim = expandedTaper.scalarMultiply(factor);
			if (estimationMethod == EstimationMethod.MonteCarlo) {
				((MonteCarloEstimate) result).addRealization(volumeEstim);
			} else {
				((GaussianEstimate) result).setMean(volumeEstim);
				((GaussianEstimate) result).setVariance(getVolumeVariance());
			}
		}
		return result;
	}
	

	/**
	 * This method returns the volume of a series of height sections. The height sections are first sorted and the 
	 * volume is calculated between the first and the last one by summing the volume of the log between the successive 
	 * height sections. The volume is calculated using Smalian's formula.
	 * @param tree a StemTaperTree instance
	 * @return the volume in dm3 or -1 if the volume cannot be calculated
	 */
	public static double getVolumeThroughSmalianFormula(StemTaperTree tree) {
		Vector<StemTaperHeightSection> heightSections = tree.getHeightSections();
		
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
	private Matrix getVolumeVariance() {
		Matrix gradients = linearExpressions.getGradients(); 
		Matrix gPlot = defaultRandomEffects.get(HierarchicalLevel.Plot).getVariance();
		Matrix gTree = defaultRandomEffects.get(HierarchicalLevel.Tree).getVariance();
		Matrix z = gradients.getSubMatrix(0, gradients.m_iRows - 1, 0, 1);
		
		Matrix fixedEffectParameterPart = gradients.multiply(defaultBeta.getVariance()).multiply(gradients.transpose());
		Matrix v = z.multiply(gPlot).multiply(z.transpose()).add(
				z.multiply(gTree).multiply(z.transpose()));
		Matrix w = v.add(rMatrix).add(fixedEffectParameterPart);		// variance under the first-order expansion 
		if (estimationMethod == EstimationMethod.SecondOrder) {
//			w = w.add(correctionMatrix.multiply(correctionMatrix.transpose()));
			w = w.subtract(correctionMatrix.multiply(correctionMatrix.transpose()));		// c*cT
			Matrix isserlisComponent;
			try {
				isserlisComponent = getIsserlisVarianceComponents();	
				w = w.add(isserlisComponent);			// W=E[ddT] in the manuscript
			} catch (Exception e) {
				System.out.println("StemTaperEquation.getVolumeVariance(): Unable to calculate the isserlisComponent Matrix, will assume this matrix is null!");
				e.printStackTrace();
			}
		}
		w = w.scalarMultiply(Math.PI * Math.PI / 64 * deltaH * deltaH * 1E-6);		// 1E-6 for conversion factor to dm3
		return expandMatrix(w);
	}
	
	
	/**
	 * This method sets the correction matrix.
	 */
	private void setCorrectionMatrix() {
		Matrix correctionFactor = new Matrix(heights.m_iRows, 1);
		Matrix hessians = linearExpressions.getHessians();
		Matrix zPrime;
		Matrix xPrime;
		Matrix gPlot = defaultRandomEffects.get(HierarchicalLevel.Plot).getVariance();
		Matrix gTree = defaultRandomEffects.get(HierarchicalLevel.Tree).getVariance();
		Matrix variances = gPlot.add(gTree);
		for (int i = 0; i < correctionFactor.m_iRows; i++) {
//			zPrime = hessians.getSubMatrix(i, i, 0, hessians.m_iCols - 1).transpose().squareSym().getSubMatrix(0, 1, 0, 1);
//			correctionFactor.m_afData[i][0] = zPrime.elementWiseMultiply(variances).getSumOfElements() * .5;
			xPrime = hessians.getSubMatrix(i, i, 0, hessians.m_iCols - 1).transpose().squareSym();
			zPrime = xPrime.getSubMatrix(0, 1, 0, 1);
			correctionFactor.m_afData[i][0] = zPrime.elementWiseMultiply(variances).getSumOfElements() * .5 + xPrime.elementWiseMultiply(defaultBeta.getVariance()).getSumOfElements() * .5;
		}
		correctionMatrix = correctionFactor;
	}

	/**
	 * This method returns the variance component that is due to the second term of the Taylor expansion. It
	 * relies on Isserlis' theorem.
	 * @return a Matrix instance
	 * @throws Exception 
	 */
	private Matrix getIsserlisVarianceComponents() throws Exception {
		Matrix output = new Matrix(heights.m_iRows, heights.m_iRows);
		Matrix hessians = linearExpressions.getHessians();
		Matrix xPrimeI;
		Matrix zPrimeI;
		Matrix xPrimeJ;
		Matrix zPrimeJ;
		double result;
//		Matrix isserlisPlot = gMatrices.get(HierarchicalLevel.Plot).getIsserlisMatrix();
//		Matrix isserlisTree = gMatrices.get(HierarchicalLevel.Tree).getIsserlisMatrix();
		Matrix isserlisPlot = defaultRandomEffects.get(HierarchicalLevel.Plot).getVariance().getIsserlisMatrix();
		Matrix isserlisTree = defaultRandomEffects.get(HierarchicalLevel.Tree).getVariance().getIsserlisMatrix();
		Matrix isserlisCombine = isserlisPlot.add(isserlisTree);
		Matrix isserlisOmega = defaultBeta.getVariance().getIsserlisMatrix();
		for (int i = 0; i < heights.m_iRows; i++) {
			xPrimeI = hessians.getSubMatrix(i, i, 0, hessians.m_iCols - 1).transpose().squareSym();
			zPrimeI = xPrimeI.getSubMatrix(0, 1, 0, 1);
			for (int j = i; j < heights.m_iRows; j++) {
				xPrimeJ = hessians.getSubMatrix(j, j, 0, hessians.m_iCols - 1).transpose().squareSym();
				zPrimeJ = xPrimeJ.getSubMatrix(0, 1, 0, 1);
				// a .25 factor has been added MF20110919 (had been forgotten in the first version). Needed because
				// the second order term is always multiplied by .5 and consequently the product of
				// two second order term is always affected by a .25 constant.
				result = zPrimeI.getKroneckerProduct(zPrimeJ).elementWiseMultiply(isserlisCombine).getSumOfElements() * .25 + xPrimeI.getKroneckerProduct(xPrimeJ).elementWiseMultiply(isserlisOmega).getSumOfElements() * .25;
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
	 * This method checks if the section length is constant.
	 * @param heights a Matrix of height section
	 * @return the section length
	 * @throws Exception if the section are unevenly spaced along the bole
	 */
	private double getHeightSectionLength(Matrix heights) throws Exception {
		Vector<Double> lengths = new Vector<Double>();
		double length;
		for (int i = 1; i < heights.m_iRows; i++) {
			length = heights.m_afData[i][0] - heights.m_afData[i - 1][0];
			lengths.add(length);
		}

		double mean = (heights.m_afData[heights.m_iRows - 1][0] - heights.m_afData[0][0]) / lengths.size();
		for (int i = 0; i < lengths.size(); i++) {
			if (Math.abs(lengths.get(i) - mean) > 1E-3) {
				throw new Exception("Height sections are not evenly spaced!");
			}
		}
		return mean;
	}
	
	/**
	 * This method returns the R matrix.
	 * @return a Matrix instance
	 * @throws Exception 
	 */
	private void setRMatrix() throws Exception {
		Matrix distance = heights.repeat(1, heights.m_iRows).subtract(heights.transpose().repeat(heights.m_iRows, 1)).getAbsoluteValue();
		Matrix correlations = distance.powMatrix(rho);			
		Matrix x = heights.scalarAdd(-1.3).scalarMultiply(1d / (treeHeight - 1.3)).getAbsoluteValue();
		Matrix l = heights.scalarMultiply(-1d).scalarAdd(treeHeight);
		Matrix stdDevMatrix = l.elementWiseMultiply(x).elementWiseMultiply(x.scalarMultiply(-1d).scalarAdd(1d).elementwisePower(3d)).elementwisePower(varFunctionParm2)
								.scalarAdd(varFunctionParm1).matrixDiagonal();
		rMatrix = stdDevMatrix.multiply(correlations).multiply(stdDevMatrix).scalarMultiply(residualStdDev * residualStdDev);
		rMatrixChol = rMatrix.getLowerCholTriangle();
	}
	
	
	
	
	
//	@SuppressWarnings("unused")
//	public static void main(String[] args) {
//		try {
//			StemTaperEquation taperEq = new StemTaperEquation();
//			taperEq.setTreeHeight(10.64);
//			taperEq.setTreeDbh(95);
//			Matrix ht = new Matrix(20,1,1,1).scalarMultiply(taperEq.treeHeight/20);
//			taperEq.setNumberOfMonteCarloRealizations(1);
//			Estimate volume = taperEq.predictVolume(ht);
//
//			double meanVolume = volume.getMean().m_afData[0][0];
//			double varVolume = volume.getVariance().getSumOfElements();
//			
//			int u = 0;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}


}
