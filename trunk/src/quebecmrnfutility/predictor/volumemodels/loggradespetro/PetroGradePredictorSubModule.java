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

import java.security.InvalidParameterException;

import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradePredictor.PetroGradePredictorVersion;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcTreeQualityProvider.QcTreeQuality;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcMarkingPriorityProvider.QcMarkingPriority;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcVigorClassProvider.QcVigorClass;
import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.stats.StatisticalUtility;
import repicea.stats.distributions.ChiSquaredDistribution;

@SuppressWarnings("serial")
abstract class PetroGradePredictorSubModule extends REpiceaPredictor {

	final PetroGradePredictorVersion version;

	ChiSquaredDistribution distributionForVCovRandomDeviates;
	
	PetroGradePredictorSubModule(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled, PetroGradePredictorVersion version) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
		this.version = version;
	}

	@Override
	protected void setParameterEstimates(ModelParameterEstimates gaussianEstimate) {
		if (!(gaussianEstimate instanceof SASParameterEstimates)) {
			throw new InvalidParameterException("The instance should be of the SASParameterEstimates class");
		}
		super.setParameterEstimates(gaussianEstimate);
		oXVector = new Matrix(1, getParameterEstimates().getMean().m_iRows);
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
			QcVigorClass vigour1234 = tree.getVigorClass();
			Matrix dummyVig = vigour1234.geDummyVig();
			Matrix dummyProd = vigour1234.geDummyProd();
			oMat = new Matrix(1,dummyProduct.m_iCols*(dummyVig.m_iCols + dummyProd.m_iCols));
			oMat.setSubMatrix(StatisticalUtility.combineMatrices(dummyProduct, dummyVig), 0, 0);
			oMat.setSubMatrix(StatisticalUtility.combineMatrices(dummyProduct, dummyProd), 0, dummyProduct.m_iCols * dummyVig.m_iCols);
			break;
		case WITH_HARV_PRIOR_MSCR: 
			QcMarkingPriority priorityMSCR = tree.getMSCRPriority();
			oMat = StatisticalUtility.combineMatrices(dummyProduct, priorityMSCR.getDummy());
			break;
		case WITH_QUALITY_ABCD:
			QcTreeQuality qualityABCD = tree.getABCDQuality();
			oMat = StatisticalUtility.combineMatrices(dummyProduct, qualityABCD.getDummy());
			break;
		}
		return oMat;
	}


}
