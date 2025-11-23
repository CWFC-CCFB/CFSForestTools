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

import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.util.Index;

/**
 * Recruitment occurrence module of Artemis 2014.
 * @author Denis Hache and Hugues Power - 2014, Mathieu Fortin - November 2025
 */
@SuppressWarnings("serial")
public class Artemis2014RecruitmentOccurrencePredictor extends REpiceaBinaryEventPredictor<Artemis2014CompatibleStand, Artemis2014CompatibleTree> {

	protected static final String ModuleName = "recrutement_l"; 

	private Map<String, Artemis2014RecruitmentOccurrenceInternalPredictor> internalPredictors;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 */
	public Artemis2014RecruitmentOccurrencePredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);		// no random effect in this module
		init();
	}

	protected void init() {
		internalPredictors = new HashMap<String, Artemis2014RecruitmentOccurrenceInternalPredictor>();
		ParameterDispatcher pd = ParameterDispatcher.getInstance();
		Index<Integer, String> vegpotIndex = pd.getVegpotIndex();
		int moduleIndex = pd.getModuleIndex().getKeyForThisValue(ModuleName);
		Artemis2014RecruitmentOccurrenceInternalPredictor internalPredictor;
		for (Integer vegpotID : vegpotIndex.keySet()) {
			Matrix beta = pd.getParameters().get(vegpotID, moduleIndex);
			Matrix omegaVectorForm = pd.getCovarianceOfParameterEstimates().get(vegpotID, moduleIndex);
			Matrix effectList = pd.getEffectID().get(vegpotID, moduleIndex);

			if (beta != null && omegaVectorForm != null) {
				String vegpotName = vegpotIndex.get(vegpotID);
				internalPredictor = new Artemis2014RecruitmentOccurrenceInternalPredictor(isParametersVariabilityEnabled, isResidualVariabilityEnabled);
				internalPredictors.put(vegpotName, internalPredictor);
				internalPredictor.setBeta(beta, omegaVectorForm.squareSym());
				internalPredictor.setEffectList(effectList);
			}
		}
	}

	@Override
	public double predictEventProbability(Artemis2014CompatibleStand stand,	Artemis2014CompatibleTree tree, Map<String, Object> parms) {
		String potentialVegetationCode = stand.getPotentialVegetation();
		if (potentialVegetationCode != null && internalPredictors.containsKey(potentialVegetationCode)) {
			return internalPredictors.get(potentialVegetationCode).predictEventProbability(stand, tree);
		} else {
			return -1d;
		}
	}

}
