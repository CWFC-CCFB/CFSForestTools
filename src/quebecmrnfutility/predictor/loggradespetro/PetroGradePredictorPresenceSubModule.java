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
package quebecmrnfutility.predictor.loggradespetro;

import quebecmrnfutility.predictor.loggradespetro.PetroGradePredictor.PetroLoggerVersion;
import quebecmrnfutility.predictor.loggradespetro.PetroGradeTree.PetroGradeSpecies;
import quebecmrnfutility.predictor.loggradespetro.PetroGradeTree.PetroGradeType;
import repicea.math.Matrix;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("serial")
class PetroGradePredictorPresenceSubModule extends PetroGradePredictorSubModule {

	PetroGradePredictorPresenceSubModule(boolean isParametersVariabilityEnabled,	boolean isResidualVariabilityEnabled, PetroLoggerVersion version) {
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

			if (version != PetroLoggerVersion.WITH_NO_VARIABLE) {
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

			double exp_xBeta = Math.exp(oXVector.multiply(beta).m_afData[0][0]);
			double probability = exp_xBeta / (1.0 + exp_xBeta);
			//			int productIndex = logCategories.indexOf(product);
			oMat.m_afData[productType.ordinal()][0] = probability;
		}

		if (isResidualVariabilityEnabled) {
			for (int i = 0; i < oMat.m_iRows; i++) {
				double deviate = random.nextDouble();
				if (deviate < oMat.m_afData[i][0]) {
					oMat.m_afData[i][0] = 1d;
				} else {
					oMat.m_afData[i][0] = 0d;
				}
			}
		}
		
		return oMat;
	}

	
	

}
