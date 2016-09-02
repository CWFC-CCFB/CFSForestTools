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
import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.covariateproviders.treelevel.ABCDQualityProvider.ABCDQuality;
import repicea.simulation.covariateproviders.treelevel.MSCRPriorityProvider.MSCRPriority;
import repicea.simulation.covariateproviders.treelevel.VigorClassProvider.VigorClass;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("serial")
abstract class PetroGradePredictorSubModule extends REpiceaPredictor {

	final PetroLoggerVersion version;

	PetroGradePredictorSubModule(boolean isParametersVariabilityEnabled,	boolean isResidualVariabilityEnabled, PetroLoggerVersion version) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
		this.version = version;
	}

	protected void setParameterEstimates(SASParameterEstimates gaussianEstimate) {
		super.setParameterEstimates(gaussianEstimate);
		oXVector = new Matrix(1, getParameterEstimates().getNumberOfFixedEffectParameters());
	}
	
	@Override
	protected void init() {}

	
	/**
	 * This private method computes the first part of the XVector depending on
	 * the selected version of the model
	 * @param dummyProduct
	 * @param tree
	 * @return the resulting matrix (a row vector)
	 */
	@SuppressWarnings("incomplete-switch")
	protected Matrix getDummyVsSelectedVersion(Matrix dummyProduct, PetroGradeTree tree) {
		Matrix oMat = null;
		switch (version) {
		case WITH_VIGOUR_1234:
			VigorClass vigour1234 = tree.getVigorClass();
			Matrix dummyVig = vigour1234.geDummyVig();
			Matrix dummyProd = vigour1234.geDummyProd();
			oMat = new Matrix(1,dummyProduct.m_iCols*(dummyVig.m_iCols + dummyProd.m_iCols));
			oMat.setSubMatrix(StatisticalUtility.combineMatrices(dummyProduct, dummyVig), 0, 0);
			oMat.setSubMatrix(StatisticalUtility.combineMatrices(dummyProduct, dummyProd), 0, dummyProduct.m_iCols * dummyVig.m_iCols);
			break;
		case WITH_HARV_PRIOR_MSCR: 
			MSCRPriority priorityMSCR = tree.getMSCRPriority();
			oMat = StatisticalUtility.combineMatrices(dummyProduct, priorityMSCR.getDummy());
			break;
		case WITH_QUALITY_ABCD:
			ABCDQuality qualityABCD = tree.getABCDQuality();
			oMat = StatisticalUtility.combineMatrices(dummyProduct, qualityABCD.getDummy());
			break;
		}
		return oMat;
	}

	
}
