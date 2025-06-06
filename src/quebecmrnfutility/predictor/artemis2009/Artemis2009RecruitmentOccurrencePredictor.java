/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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

import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.util.Index;

/**
 * The Artemis2009RecruitmentOccurrencePredictor class implements the logistic part of the recruitment module in the Artemis-2009 simulator.
 * @author Mathieu Fortin - August 2014
 */
@SuppressWarnings("serial")
public class Artemis2009RecruitmentOccurrencePredictor extends REpiceaBinaryEventPredictor<Artemis2009CompatibleStand, Artemis2009CompatibleTree> {

	protected static final String ModuleName = "recrutement_l"; 

	private Map<String, Artemis2009RecruitmentOccurrenceInternalPredictor> internalPredictors;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 */
	public Artemis2009RecruitmentOccurrencePredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);		// no random effect in this module
		init();
	}

	protected void init() {
		internalPredictors = new HashMap<String, Artemis2009RecruitmentOccurrenceInternalPredictor>();
		ParameterDispatcher pd = ParameterDispatcher.getInstance();
		Index<Integer, String> vegpotIndex = pd.getVegpotIndex();
		int moduleIndex = pd.getModuleIndex().getKeyForThisValue(ModuleName);
		Artemis2009RecruitmentOccurrenceInternalPredictor internalPredictor;
		for (Integer vegpotID : vegpotIndex.keySet()) {
			Matrix beta = pd.getParameters().get(vegpotID, moduleIndex);
			Matrix omegaVectorForm = pd.getCovarianceOfParameterEstimates().get(vegpotID, moduleIndex);
			Matrix effectList = pd.getEffectID().get(vegpotID, moduleIndex);

			if (beta != null && omegaVectorForm != null) {
				String vegpotName = vegpotIndex.get(vegpotID);
				internalPredictor = new Artemis2009RecruitmentOccurrenceInternalPredictor(isParametersVariabilityEnabled, isResidualVariabilityEnabled);
				internalPredictors.put(vegpotName, internalPredictor);
				internalPredictor.setBeta(beta, omegaVectorForm.squareSym());
				internalPredictor.setEffectList(effectList);
			}
		}
	}

	@Override
	public double predictEventProbability(Artemis2009CompatibleStand stand,	Artemis2009CompatibleTree tree, Map<String, Object> parms) {
		String potentialVegetationCode = stand.getPotentialVegetation();
		if (potentialVegetationCode != null && internalPredictors.containsKey(potentialVegetationCode)) {
			return internalPredictors.get(potentialVegetationCode).predictEventProbability(stand, tree);
		} else {
			return -1d;
		}
	}
	
//	@Override
//	public void clearDeviates() {
//		for (Artemis2009RecruitmentOccurrenceInternalPredictor p : internalPredictors.values()) {
//			p.clearDeviates();
//		}
//	}

//	public static void main(String[] args) {
//		Artemis2009RecruitmentOccurrencePredictor pred = new Artemis2009RecruitmentOccurrencePredictor(false, false);
//		int u = 0;
//	}
	
	
}
