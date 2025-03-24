/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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
package quebecmrnfutility.predictor.artemis2009;

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.stats.StatisticalUtility;

/**
 * Implementation of the model of recruitment dbh.
 * @author Mathieu Fortin - 2009-2014, 2025
 */
@SuppressWarnings("serial")
class Artemis2009RecruitDiameterInternalPredictor extends REpiceaPredictor {

	private List<Integer> effectList;

	protected Artemis2009RecruitDiameterInternalPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect in this model
		init();
	}

	protected void init() {
		effectList = new ArrayList<Integer>();
	}

	
	protected void setBeta(Matrix beta, SymmetricMatrix omega) {
		ModelParameterEstimates estimate = new SASParameterEstimates(beta, omega);
		setParameterEstimates(estimate);
		oXVector = new Matrix(1, estimate.getMean().m_iRows);
	}
	
	protected void setEffectList(Matrix effectList) {
		for (int i = 0; i < effectList.m_iRows; i++) {
			this.effectList.add((int) effectList.getValueAt(i, 0));
		}
	}
	
	protected synchronized double[] predictRecruitDiameter(Artemis2009CompatibleStand stand, Artemis2009CompatibleTree tree) {
		Matrix beta = getParametersForThisRealization(stand);
		
		final double dispersion = beta.getValueAt(beta.m_iRows-1, 0);	// last element (dispersion) is taken out of the vector
		beta = beta.getSubMatrix(0, beta.m_iRows - 2, 0, 0); 	// vector is resized to omit the last element (dispersion)

		ParameterDispatcher.getInstance().constructXVector(oXVector, stand, tree, Artemis2009MortalityPredictor.ModuleName, effectList);
//		double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
		final double xBeta = ParameterDispatcher.getInstance().getProduct(oXVector, beta);
		
		final double fGammaMean = Math.exp(xBeta);

		double dVariance = 0.0;

		final double shape = dispersion;		// MF20250324 This was corrected. It was formerly implemented the other way around.
		final double scale = fGammaMean / dispersion; // MF20250324 This was corrected. It was formerly implemented the other way around.

		double[] result = new double[2];
		double fDiameter;

		if (isResidualVariabilityEnabled) {
			double randomDeviate = scale > 1E-8 ?
					StatisticalUtility.getRandom().nextGamma(shape, scale) :
						0d;
			fDiameter = 9.1 + randomDeviate * 0.1;	
			if (fDiameter > 21) {
				fDiameter = 21;			// limiter for inconsistent predictions
			}
		} else {
			fDiameter = 9.1 + fGammaMean * 0.1;
			dVariance = Math.exp(2.0 * xBeta) / dispersion * 0.01; // factor 100 is required to ensure a proper conversion from mm to cm
		}
		
		result[0] = fDiameter;
		result[1] = dVariance;
		return result;
	}

}
