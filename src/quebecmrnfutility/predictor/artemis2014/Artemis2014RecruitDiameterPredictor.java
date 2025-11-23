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

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictor;
import repicea.util.Index;

/**
 * Recruit diameter module of Artemis 2014.
 * @author Denis Hache and Hugues Power - 2014, Mathieu Fortin - November 2025
 */
@SuppressWarnings("serial")
public class Artemis2014RecruitDiameterPredictor extends REpiceaPredictor {

	protected static final String ModuleName = "recrutement_g"; 

	private Map<String, Artemis2014RecruitDiameterInternalPredictor> internalPredictors;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the variability in the parameter estimate and the residual variance
	 */
	public Artemis2014RecruitDiameterPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);
	}

	
	protected Artemis2014RecruitDiameterPredictor(boolean isParameterVariabilityEnabled, boolean isResidualVariabilityEnabled) {		// no random effect in this module
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect in this module
		init();
	}

	
	protected void init() {
		internalPredictors = new HashMap<String, Artemis2014RecruitDiameterInternalPredictor>();
		ParameterDispatcher pd = ParameterDispatcher.getInstance();
		Index<Integer, String> vegpotIndex = pd.getVegpotIndex();
		int moduleIndex = pd.getModuleIndex().getKeyForThisValue(ModuleName);
		Artemis2014RecruitDiameterInternalPredictor internalPredictor;
		for (Integer vegpotID : vegpotIndex.keySet()) {
			Matrix beta = pd.getParameters().get(vegpotID, moduleIndex);
			Matrix omegaVectorForm = pd.getCovarianceOfParameterEstimates().get(vegpotID, moduleIndex);
			Matrix effectList = pd.getEffectID().get(vegpotID, moduleIndex);

			if (beta != null && omegaVectorForm != null) {
				String vegpotName = vegpotIndex.get(vegpotID);
				internalPredictor = new Artemis2014RecruitDiameterInternalPredictor(isParametersVariabilityEnabled, isResidualVariabilityEnabled);
				internalPredictors.put(vegpotName, internalPredictor);
				internalPredictor.setBeta(beta, omegaVectorForm.squareSym());
				internalPredictor.setEffectList(effectList);
			}
		}
	}

	/**
	 * This method predicts the diameter of a recruit.
	 * @param stand an Artemis2009CompatibleStand instance
	 * @param tree an Artemis2009CompatibleTree instance
	 * @param parms additional parameters (unnecessary for this class)
	 * @return an array of two doubles, the first one being the predicted diameter and the second one being its variance
	 */
	public double[] predictRecruitDiameter(Artemis2014CompatibleStand stand, Artemis2014CompatibleTree tree, Object... parms) {
		String potentialVegetationCode = stand.getPotentialVegetation();
		if (potentialVegetationCode == null || !internalPredictors.containsKey(potentialVegetationCode)) {
			throw new InvalidParameterException("The potential vegetation of this plot is either missing or not considered in the recruit diameter submodel!");
		}
		double[] predictedValues = internalPredictors.get(potentialVegetationCode).predictRecruitDiameter(stand, tree);
		return predictedValues;
	}

}
