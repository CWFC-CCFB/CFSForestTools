/*
 * This file is part of the mrnf-foresttool- library.
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
import repicea.simulation.SASParameterEstimates;
import repicea.stats.StatisticalUtility;
import repicea.stats.distributions.ChiSquaredDistribution;

class PetroGradePredictorPresenceSubModule extends PetroGradePredictorSubModule {

	PetroGradePredictorPresenceSubModule(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled, PetroGradePredictorVersion version) {
		super(isParametersVariabilityEnabled, isResidualVariabilityEnabled, version);
	}
	
	/**
	 * This method computes a matrix that contains the probabilities of observing
	 * the Petro grade in a TreePetroProductable type object
	 * @param tree a PetroLoggableTree instance
	 * @return the resulting matrix
	 */
	protected Matrix getPredictedGradePresences(PetroGradeTree tree) {
		Matrix beta = getParametersForThisRealization(tree);

		PetroGradeSpecies species = tree.getPetroGradeSpecies();
		double dbh = tree.getDbhCm();
		double dbh2 = tree.getSquaredDbhCm();

		Matrix oMat = new Matrix(PetroGradeType.values().length,1);
		Matrix dummySpecies = species.getDummy();

		//		List<PetroTreeLogCategory> logCategories = getTreeLoggerParameters().getSpeciesLogCategories(species.name());

		for (PetroGradeType productType : PetroGradeType.values()) {
			oXVector.resetMatrix();
			int pointer = 0;
			Matrix dummyProduct = productType.getDummy();

			if (version != PetroGradePredictorVersion.WITH_NO_VARIABLE) {
				Matrix dummyVersion = getDummyVsSelectedVersion(dummyProduct, tree);
				oXVector.setSubMatrix(dummyVersion, 0, pointer);
				pointer += dummyVersion.m_iCols;
			}

			Matrix dummyProductSpecies = StatisticalUtility.combineMatrices(dummyProduct, dummySpecies);
			oXVector.setSubMatrix(dummyProductSpecies, 0, pointer);
			pointer += dummyProductSpecies.m_iCols;

			oXVector.setSubMatrix(dummyProductSpecies.scalarMultiply(dbh), 0, pointer);
			pointer += dummyProductSpecies.m_iCols;

			oXVector.setSubMatrix(dummyProduct.scalarMultiply(dbh2), 0, pointer);
			pointer += dummyProduct.m_iCols;

			double exp_xBeta = Math.exp(oXVector.multiply(beta).getValueAt(0, 0));
			double probability = exp_xBeta / (1.0 + exp_xBeta);
			//			int productIndex = logCategories.indexOf(product);
			oMat.setValueAt(productType.ordinal(), 0, probability);
		}

		if (isResidualVariabilityEnabled) {
			for (int i = 0; i < oMat.m_iRows; i++) {
				double deviate = StatisticalUtility.getRandom().nextDouble();
				if (deviate < oMat.getValueAt(i, 0)) {
					oMat.setValueAt(i, 0, 1d);
				} else {
					oMat.setValueAt(i, 0, 0d);
				}
			}
		}
		
		return oMat;
	}


	/*
	 * For manuscript purposes.
	 */
	void replaceModelParameters() {
		int totalDegreesOfFreedom = 1595; // total number of trees
		int numberParameters = getParameterEstimates().getMean().m_iRows / 5;
//		Matrix currentMean = getParameterEstimates().getMean();
		Matrix newMean = getParameterEstimates().getRandomDeviate();
		Matrix variance = getParameterEstimates().getVariance();
		if (distributionForVCovRandomDeviates == null) {
			distributionForVCovRandomDeviates = new ChiSquaredDistribution(totalDegreesOfFreedom - numberParameters, variance);
		}
		Matrix newVariance = distributionForVCovRandomDeviates.getRandomRealization();
		setParameterEstimates(new SASParameterEstimates(newMean, newVariance));
	}

	

}
