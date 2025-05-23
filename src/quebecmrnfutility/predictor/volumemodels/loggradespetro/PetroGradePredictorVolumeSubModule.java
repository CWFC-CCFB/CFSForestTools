/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.volumemodels.loggradespetro;

import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradePredictor.PetroGradePredictorVersion;
import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradeTree.PetroGradeSpecies;
import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradeTree.PetroGradeType;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.math.utility.MatrixUtility;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.SASParameterEstimates;
import repicea.stats.distributions.ChiSquaredDistribution;
import repicea.stats.estimates.GaussianErrorTermEstimate;

@SuppressWarnings("serial")
class PetroGradePredictorVolumeSubModule extends PetroGradePredictorSubModule {

	private Matrix m_matDBHCorrFact;
	
	PetroGradePredictorVolumeSubModule(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled, PetroGradePredictorVersion version) {
		super(isParametersVariabilityEnabled, isResidualVariabilityEnabled, version);
	}

	protected void setDefaultResidualError(GaussianErrorTermEstimate estimate) {
		super.setDefaultResidualError(ErrorTermGroup.Default, estimate);
	}

	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.REpiceaPredictor#setParameterEstimates(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	protected void setParameterEstimates(ModelParameterEstimates gaussianEstimate) {
		super.setParameterEstimates(gaussianEstimate);
		Matrix betaVolume = gaussianEstimate.getMean();
		Matrix dbhParam = betaVolume.getSubMatrix(betaVolume.m_iRows-5, betaVolume.m_iRows-1, 0, 0);
		m_matDBHCorrFact = dbhParam.elementWiseMultiply(dbhParam);
	}

	
	/**
	 * This method computes a matrix that contains the 
	 * volumes for each Petro grade conditional on their presence
	 * @param tree a PetroLoggableTree instance
	 * @return the resulting matrix
	 */
	protected Matrix getPredictedGradeVolumes(PetroGradeTree tree) {
		PetroGradeSpecies species = tree.getPetroGradeSpecies();
		double dbh = tree.getDbhCm();
		Matrix beta = getParametersForThisRealization(tree);

		Matrix oMat = new Matrix(PetroGradeType.values().length, 1);
		Matrix dummySpecies = species.getDummy();
		
		for (PetroGradeType productType : PetroGradeType.values()) {
			oXVector.resetMatrix();
			int pointer = 0;
			Matrix dummyProduct = productType.getDummy();
			
			if (version != PetroGradePredictorVersion.WITH_NO_VARIABLE) {
				Matrix dummyVersion = getDummyVsSelectedVersion(dummyProduct, tree);
				oXVector.setSubMatrix(dummyVersion, 0, pointer);
				pointer += dummyVersion.m_iCols;
			}

			Matrix dummyProductSpecies = MatrixUtility.combineMatrices(dummyProduct, dummySpecies);
			oXVector.setSubMatrix(dummyProductSpecies, 0, pointer);
			pointer += dummyProductSpecies.m_iCols;
			
			oXVector.setSubMatrix(dummyProduct.scalarMultiply(dbh), 0, pointer);
			pointer += dummyProduct.m_iCols;
			
			double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
			oMat.setValueAt(productType.ordinal(), 0, xBeta);
		}
		
		if (isResidualVariabilityEnabled) {
			oMat = oMat.add(getResidualError());
		} else {
			Matrix correctionFactor;
			Matrix rMatrix = getDefaultResidualError(ErrorTermGroup.Default).getVariance();
			if (tree.isModelStochastic()) {
				correctionFactor = rMatrix.diagonalVector().scalarMultiply(0.5);
			} else {
				double dbhVariance = tree.getDbhCmVariance();		
				correctionFactor = m_matDBHCorrFact.scalarMultiply(dbhVariance).add(rMatrix.diagonalVector()).scalarMultiply(0.5);
			}
			oMat = oMat.add(correctionFactor);
		}

		Matrix conditionalVolumes = oMat.expMatrix();
		
		return conditionalVolumes;
	}

	/*
	 * For manuscript purposes.
	 */
	void replaceModelParameters() {
		int totalDegreesOfFreedom = 602; // average number of trees in each log category
		int numberParameters = getParameterEstimates().getMean().m_iRows / 5;
		int residualDegreesOfFreedom = totalDegreesOfFreedom - numberParameters;
//		Matrix currentMean = getParameterEstimates().getMean();
		Matrix newMean = getParameterEstimates().getRandomDeviate();
		SymmetricMatrix variance = getParameterEstimates().getVariance();
		if (distributionForVCovRandomDeviates == null) {
			distributionForVCovRandomDeviates = new ChiSquaredDistribution(residualDegreesOfFreedom, variance);
		}
		SymmetricMatrix newVariance = distributionForVCovRandomDeviates.getRandomRealization();
		setParameterEstimates(new SASParameterEstimates(newMean, newVariance));
		
		SymmetricMatrix currentResidualVariance = getDefaultResidualError(ErrorTermGroup.Default).getVariance();
		ChiSquaredDistribution residualVarianceDistribution = new ChiSquaredDistribution(residualDegreesOfFreedom, currentResidualVariance);
		SymmetricMatrix newResidualVariance = residualVarianceDistribution.getRandomRealization();
		setDefaultResidualError(new GaussianErrorTermEstimate(newResidualVariance));
	}

}
