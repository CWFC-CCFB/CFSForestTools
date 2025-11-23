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
import java.util.Map;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.SASParameterEstimates;

@SuppressWarnings("serial")
class Artemis2014MortalityInternalPredictor extends REpiceaBinaryEventPredictor<Artemis2014CompatibleStand, Artemis2014CompatibleTree> {

	private List<Integer> effectList;
	
	protected Artemis2014MortalityInternalPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
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
	
	@Override
	public synchronized double predictEventProbability(Artemis2014CompatibleStand stand, Artemis2014CompatibleTree tree, Map<String, Object> parms) {
		Matrix beta = getParametersForThisRealization(stand);
		ParameterDispatcher.getInstance().constructXVector(oXVector, stand, tree, Artemis2014MortalityPredictor.ModuleName, effectList);
//		double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
		double xBeta = ParameterDispatcher.getInstance().getProduct(oXVector, beta);
		double deathProbability = 1.0 - Math.exp(- Math.exp(xBeta));
		return deathProbability;
	}

}
