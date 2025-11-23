/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2014 Quebec Ministry of Natural Resources and Forestry
 * Author: Denis Hache and Hugues Power
 * Copyright (C) 2025 His Majesty the King in Right of Canada
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
package quebecmrnfutility.predictor.artemis2014;

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("serial")
class Artemis2014RecruitmentNumberInternalPredictor extends REpiceaPredictor {

	private List<Integer> effectList;
	
	protected Artemis2014RecruitmentNumberInternalPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
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
	
	protected synchronized double predictNumberOfRecruits(Artemis2014CompatibleStand stand, Artemis2014CompatibleTree tree) {
		Matrix beta = getParametersForThisRealization(stand).getDeepClone();
//		double dispersion = beta.m_afData[beta.m_iRows - 1][0];		// MF20190627 This line could cause a bug. In stochastic mode the dispersion could be negative 
		double dispersion = getParameterEstimates().getMean().getValueAt(beta.m_iRows - 1, 0);		// MF20190627 This line could cause a bug. In stochastic mode the dispersion could be negative 
		beta.setValueAt(beta.m_iRows - 1, 0, 1d);    // last element is replaced by 1 to account for the offset variable	
	
		ParameterDispatcher.getInstance().constructXVector(oXVector, stand, tree, Artemis2014RecruitmentNumberPredictor.ModuleName, effectList);
//		double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
		double xBeta = ParameterDispatcher.getInstance().getProduct(oXVector, beta);
		double predictedValue = Math.exp(xBeta);
		
		if (isResidualVariabilityEnabled) {
			int nbRecruits = StatisticalUtility.getRandom().nextNegativeBinomial(predictedValue, dispersion) + 1; // 1 is required since the modelled value was y - 1 
			if (nbRecruits > 80 && !Artemis2014RecruitmentNumberPredictor.Override80Limit) { // maximum number of recruits is set to 80
				nbRecruits = 80;
			}
			return nbRecruits;
		} else {
			return predictedValue + 1d;		// deterministic implementation: 1 is required since the modelled value was y - 1
		}
	}


}
