/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package quebecmrnfutility.predictor.volumemodels.wbirchloggrades;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import quebecmrnfutility.simulation.covariateproviders.treelevel.QcTreeQualityProvider.QcTreeQuality;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.Distribution.Type;
import repicea.stats.StatisticalUtility;
import repicea.stats.distributions.ChiSquaredDistribution;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public class WBirchLogGradesPredictor extends REpiceaPredictor {
	
	protected boolean isTestPurpose = false;
	
	protected static enum Version {
		/**
		 * For A,B,C qualities with dbh > 29 and h20Pred >= 5
		 */
		Full, 
		/**
		 * For A and B qualities whose dbh <= 29 or h20Pred < 5
		 */
		FullTruncated,
		/**
		 * For C quality whose dbh <= 29 or h20Pred < 5 and D quality
		 */
		DClass, 
		/**
		 * For no quality trees
		 */
		NCClass;
	}

	
	private double sigma2Res;
	private Matrix weightExponentCoefficients;
	private Matrix corrMatrix;
	private Map<Double, Matrix> cholMatrices;

	private ChiSquaredDistribution distributionForVCovRandomDeviates;
	private Matrix variancesWeights;
//	private Matrix varianceCorrCoefficient;

	/**
	 * Constructor.
	 * @param isVariabilityEnabled a boolean instance
	 */
	public WBirchLogGradesPredictor(boolean isVariabilityEnabled) {	
		this(isVariabilityEnabled, isVariabilityEnabled);
	}

	WBirchLogGradesPredictor(boolean isParameterVariabilityEnabled, boolean isResidualVariabiltyEnabled) {		
		super(isParameterVariabilityEnabled, false, isResidualVariabiltyEnabled);
		cholMatrices = new HashMap<Double, Matrix>();
		init();
	}

	
	@Override
	protected final void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_beta.csv";
			String omegaFilename = path + "0_omega.csv";
			String rMatrixFilename = path + "0_rMatrix.csv";
			String varParmsFilename = path + "0_varParams.csv";
			String varWeightsFilename = path + "0_varianceWeights.csv";
//			String varCorrCoefFilename = path + "0_varianceCorrCoef.csv";

			Matrix beta = ParameterLoader.loadVectorFromFile(betaFilename).get();
			SymmetricMatrix omega = SymmetricMatrix.convertToSymmetricIfPossible(ParameterLoader.loadMatrixFromFile(omegaFilename));
			corrMatrix = ParameterLoader.loadMatrixFromFile(rMatrixFilename);
			Matrix varParms = ParameterLoader.loadVectorFromFile(varParmsFilename).get();
			sigma2Res = varParms.getValueAt(varParms.m_iRows - 1, 0);
			weightExponentCoefficients = varParms.getSubMatrix(0, varParms.m_iRows - 2, 0, 0); 

			setParameterEstimates(new ModelParameterEstimates(beta, omega));
			
			variancesWeights = ParameterLoader.loadVectorFromFile(varWeightsFilename).get();
//			varianceCorrCoefficient = ParameterLoader.loadVectorFromFile(varCorrCoefFilename).get();
		
		} catch (Exception e) {
			System.out.println("Unable to load parameters!");
		}
	}
		
	private Matrix getCholMatrixForThisTree(WBirchLogGradesTree tree) {
		double dbhCm = tree.getDbhCm();

		if (!cholMatrices.containsKey(dbhCm)) {
			Matrix weightMat = weightExponentCoefficients.powMatrix(dbhCm).matrixDiagonal();  
			SymmetricMatrix vMat = SymmetricMatrix.convertToSymmetricIfPossible(
					weightMat.multiply(corrMatrix).multiply(weightMat).scalarMultiply(sigma2Res));
			cholMatrices.put(dbhCm, vMat.getLowerCholTriangle());
		}

		return cholMatrices.get(dbhCm);
	}
	
	private double getH20Prediction(WBirchLogGradesStand stand, WBirchLogGradesTree tree, Matrix beta) {
		double dbhCm = tree.getDbhCm();
		double elevationM = stand.getElevationM();
		double isC = 0d;
		if (tree.getTreeQuality() == QcTreeQuality.C) {
			isC = 1d;
		}
		double isD = 0d;
		if (tree.getTreeQuality() == QcTreeQuality.D) {
			isD = 1d;
		}
		double isNC = 0d;
		if (tree.getTreeQuality() == null) {
			isNC = 1d;
		}
		double z1 = beta.getValueAt(4, 0);
		double z1C = beta.getValueAt(5, 0);
		double z1D = beta.getValueAt(6, 0);
		double z1NC = beta.getValueAt(7, 0);
		double z2 = beta.getValueAt(8, 0);
		double z3 = beta.getValueAt(9, 0);
		double z4 = beta.getValueAt(10, 0);
		
		double b1 = z1 + z1C * isC + z1D * isD + z1NC * isNC + z4 * elevationM;
		return b1 * Math.pow((1 - Math.exp(z2 * (dbhCm - 15d))), z3) * dbhCm;
	}

	
	private double getMerPrediction(WBirchLogGradesStand stand, WBirchLogGradesTree tree, Matrix beta, double h20Pred) {
		double dbhCm = tree.getDbhCm();
		
		double isAB = 0d;
		if (tree.getTreeQuality() == QcTreeQuality.A || tree.getTreeQuality() == QcTreeQuality.B) {
			isAB = 1d;
		}
		
		double a0 = beta.getValueAt(11, 0);
		double a1 = beta.getValueAt(12, 0);
		double a3 = beta.getValueAt(13, 0);
		return a0 + a1 * (dbhCm - 9d) * h20Pred + a3 * isAB;
	}
	
	private double getPulpPrediction(WBirchLogGradesStand stand, WBirchLogGradesTree tree, Matrix beta, double h20Pred, double merVol) {
		double dbhCm = tree.getDbhCm();
		double isC = 0d;
		if (tree.getTreeQuality() == QcTreeQuality.C) {
			isC = 1d;
		}
		double isD = 0d;
		if (tree.getTreeQuality() == QcTreeQuality.D) {
			isD = 1d;
		}
		double isNC = 0d;
		if (tree.getTreeQuality() == null) {
			isNC = 1d;
		}
		double b1 = beta.getValueAt(0, 0);
		double b1C = beta.getValueAt(1, 0);
		double b1D = beta.getValueAt(2, 0);
		double b1NC = beta.getValueAt(3, 0);
		double b2 = beta.getValueAt(14, 0);
		
		return merVol / (1 + Math.exp(b1 + b1C * isC + b1D * isD + b1NC * isNC - b2 * h20Pred / dbhCm));
	}
	
	private double getSawlogPrediction(WBirchLogGradesStand stand, WBirchLogGradesTree tree, Matrix beta, double merVol, double pulpVol) {
		double dbhCm = tree.getDbhCm();
		double c1 = beta.getValueAt(15, 0);
		double c2 = beta.getValueAt(16, 0);
		
		return (merVol - pulpVol) / (1 + Math.exp(c1 - c2 * dbhCm));
	}
	
	private double getLowGradeVeneerPrediction(WBirchLogGradesStand stand, WBirchLogGradesTree tree, Matrix beta, double merVol, double pulpVol, double sawlogVol) {
		double dbhCm = tree.getDbhCm();
		double d2 = beta.getValueAt(17, 0);
		
		return (merVol - pulpVol - sawlogVol) / (1 + d2 * dbhCm);
	}

	/**
	 * This method returns the predictions for each log grade. NOTE this is 
	 * the underbark volume.
	 * @param stand a WBirchProdVolStand instance
	 * @param tree a WBirchProdVolTree instance
	 * @return a Matrix instance 
	 * <ul>
	 * <li> slot 0 : predicted height at d = 20 cm (m) </li>
	 * <li> slot 1 : commercial volume (m3)</li>
	 * <li> slot 2 : pulp wood volume (m3) </li>
	 * <li> slot 3 : sawlog volume (m3) </li>
	 * <li> slot 4 : low grade veneer volume (m3) </li>
	 * <li> slot 5 : veneer volume (m3) </li>
	 * <li> slot 6 : low grade sawlog volume (m3) </li>
	 * </ul>
	 */
	public Matrix getLogGradeUnderbarkVolumePredictions(WBirchLogGradesStand stand, WBirchLogGradesTree tree) {
		Matrix modelParameters = getParametersForThisRealization(stand);
		double h20Pred = getH20Prediction(stand, tree, modelParameters);
		double merVol = getMerPrediction(stand, tree, modelParameters, h20Pred);
		double pulpVol = getPulpPrediction(stand, tree, modelParameters, h20Pred, merVol);
		
		Matrix residualDeviates = new Matrix(corrMatrix.m_iRows, 1);
		if (isResidualVariabilityEnabled) {	// should be run after estimating merchantable and pulp volume
			residualDeviates = getCholMatrixForThisTree(tree).multiply(StatisticalUtility.drawRandomVector(corrMatrix.m_iRows, Type.GAUSSIAN));
		}

		h20Pred += residualDeviates.getValueAt(0, 0);		// add the deviate

		Version version;
		if (isTestPurpose) { // for test purpose
			try {
				Method methodH20 = tree.getClass().getDeclaredMethod("getH20Obs", new Class[]{});
				double h20Obs = (Double) methodH20.invoke(tree, new Object[]{});
				version = getVersion(tree, h20Obs);
			} catch (Exception e) {
				version = null;
				throw new InvalidParameterException("Unable to find the method getH20Obs in the WBirchProdVolTreeImpl instance");
			}
		} else  {
			version = getVersion(tree, h20Pred);
		}
		
		Matrix logGradePred = new Matrix(7,1);
		logGradePred.setValueAt(0, 0, h20Pred);
		if (version == Version.Full) {
			double sawlogVol = getSawlogPrediction(stand, tree, modelParameters, merVol, pulpVol);
			double lowGradeVeneer = getLowGradeVeneerPrediction(stand, tree, modelParameters, merVol, pulpVol, sawlogVol);
			merVol += residualDeviates.getValueAt(1, 0);
			pulpVol += residualDeviates.getValueAt(2, 0);
			sawlogVol += residualDeviates.getValueAt(3, 0);
			lowGradeVeneer += residualDeviates.getValueAt(4, 0);
			logGradePred.setValueAt(1, 0, merVol);
			logGradePred.setValueAt(2, 0, pulpVol);
			logGradePred.setValueAt(3, 0, sawlogVol);
			logGradePred.setValueAt(4, 0, lowGradeVeneer);
			double veneer = merVol - pulpVol - sawlogVol - lowGradeVeneer;
			logGradePred.setValueAt(5, 0, veneer);
		} else {
			if (version == Version.FullTruncated) {
				double sawlogVol = getSawlogPrediction(stand, tree, modelParameters, merVol, pulpVol);
				merVol += residualDeviates.getValueAt(1, 0);
				sawlogVol += residualDeviates.getValueAt(3, 0);
				logGradePred.setValueAt(1, 0, merVol);
				logGradePred.setValueAt(2, 0, merVol - sawlogVol);
				logGradePred.setValueAt(3, 0, sawlogVol);
			} else {
				merVol += residualDeviates.getValueAt(1, 0);
				pulpVol += residualDeviates.getValueAt(2, 0);
				double lastProduct = merVol - pulpVol;
				logGradePred.setValueAt(1, 0, merVol);
				logGradePred.setValueAt(2, 0, pulpVol);
				if (version == Version.DClass) {
					logGradePred.setValueAt(3, 0, lastProduct);
				} else {
					logGradePred.setValueAt(6, 0, lastProduct);
				}
			}
		}
		
		
		logGradePred.setValueAt(1, 0, logGradePred.getValueAt(1, 0) * .001);
		logGradePred.setValueAt(2, 0, logGradePred.getValueAt(2, 0) * .001);
		logGradePred.setValueAt(3, 0, logGradePred.getValueAt(3, 0) * .001);
		logGradePred.setValueAt(4, 0, logGradePred.getValueAt(4, 0) * .001);
		logGradePred.setValueAt(5, 0, logGradePred.getValueAt(5, 0) * .001);
		logGradePred.setValueAt(6, 0, logGradePred.getValueAt(6, 0) * .001);
		
		return logGradePred;		// to get the result in m3 and not in dm3
	}
	
	
	
	protected Version getVersion(WBirchLogGradesTree tree, double h20Pred) {
		if (tree.getTreeQuality() != null) {
			if (tree.getTreeQuality().ordinal() < 3) {
				if (h20Pred >= 5d && tree.getDbhCm() > 29d) {
					return Version.Full;
				} else {
					if (tree.getTreeQuality().ordinal() < 2) {
						return Version.FullTruncated;
					} else {
						return Version.DClass;
					}
				}
			} else {
				return Version.DClass;
			}
		} else {
			return Version.NCClass;
		}
	}

	/*
	 * For manuscript purposes.
	 */
	void replaceModelParameters() {
		int degreesOfFreedom = 607;		// according to simul.gnls.3 in the file resolution-simultanee-v8-mathieu
		Matrix newMean = getParameterEstimates().getRandomDeviate();
		SymmetricMatrix variance = getParameterEstimates().getVariance();
		if (distributionForVCovRandomDeviates == null) {
			distributionForVCovRandomDeviates = new ChiSquaredDistribution(degreesOfFreedom, variance);
		}
		SymmetricMatrix newVariance = distributionForVCovRandomDeviates.getRandomRealization();
		setParameterEstimates(new ModelParameterEstimates(newMean, newVariance));
		
		ChiSquaredDistribution residualVarianceDistribution = new ChiSquaredDistribution(degreesOfFreedom, sigma2Res);
		double newSigma2Res = residualVarianceDistribution.getRandomRealization().getValueAt(0, 0);
		sigma2Res = newSigma2Res;
		
		Matrix errorWeights = StatisticalUtility.drawRandomVector(weightExponentCoefficients.m_iRows, Type.GAUSSIAN);
		Matrix newWeights = weightExponentCoefficients.add(variancesWeights.elementWisePower(0.5).elementWiseMultiply(errorWeights));
		weightExponentCoefficients = newWeights;
		
		// These errors in the correlation parameter leads to exception in the Cholesky decomposition
//		Matrix errorCorrCoef = varianceCorrCoefficient.elementWisePower(0.5).elementWiseMultiply(StatisticalUtility.drawRandomVector(varianceCorrCoefficient.m_iRows, Type.GAUSSIAN));
//		Matrix output = new Matrix(5,5);
//		int nbElem = 0;
//		for (int i = 0; i < output.m_iRows - 1; i++) {
//			for (int j = i + 1; j < output.m_iCols; j++) {
//				output.m_afData[i][j] = errorCorrCoef.m_afData[nbElem][0];
//				output.m_afData[j][i] = errorCorrCoef.m_afData[nbElem][0];
//				nbElem++;
//			}
//		}
//		
//		Matrix newCorrelationMatrix = this.corrMatrix.add(output);
//		corrMatrix = newCorrelationMatrix;
	}

}
