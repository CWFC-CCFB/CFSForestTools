/*
 * This file is part of the CFSForesttools library.
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

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.REpiceaPredictor;
import repicea.util.Index;

@SuppressWarnings("serial")
public class Artemis2009DiameterIncrementPredictor extends REpiceaPredictor {

	protected static final String ModuleName = "accroissement"; 

	private Map<String, Artemis2009DiameterIncrementInternalPredictor> internalPredictors;

	public Artemis2009DiameterIncrementPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
		init();
	}

	@Override
	protected final void init() {
		internalPredictors = new HashMap<String, Artemis2009DiameterIncrementInternalPredictor>();
		ParameterDispatcher pd = ParameterDispatcher.getInstance();
		Index<Integer, String> vegpotIndex = pd.getVegpotIndex();
		int moduleIndex = pd.getModuleIndex().getKeyForThisValue(ModuleName);
		Artemis2009DiameterIncrementInternalPredictor internalPredictor;
		for (Integer vegpotID : vegpotIndex.keySet()) {
			Matrix beta = pd.getParameters().get(vegpotID, moduleIndex);
			Matrix omegaVectorForm = pd.getCovarianceOfParameterEstimates().get(vegpotID, moduleIndex);
			Matrix covparms = pd.getCovarianceParameters().get(vegpotID, moduleIndex);
			Matrix effectList = pd.getEffectID().get(vegpotID, moduleIndex);

			if (beta != null && omegaVectorForm != null) {
				String vegpotName = vegpotIndex.get(vegpotID);
				internalPredictor = new Artemis2009DiameterIncrementInternalPredictor(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled);
				internalPredictors.put(vegpotName, internalPredictor);
				internalPredictor.setBeta(beta, omegaVectorForm.squareSym());
				internalPredictor.setEffectList(effectList);
				
				internalPredictor.setRandomEffect(HierarchicalLevel.PLOT, 
						SymmetricMatrix.convertToSymmetricIfPossible(covparms.getSubMatrix(0, 0, 0, 0)));
				internalPredictor.setRandomEffect(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT, 
						SymmetricMatrix.convertToSymmetricIfPossible(covparms.getSubMatrix(1, 1, 0, 0)));
				internalPredictor.setResidualErrorCovariance(covparms.getValueAt(3, 0), covparms.getValueAt(2, 0));
			}
		}
	}

	
	public double[] predictGrowth(Artemis2009CompatibleStand stand, Artemis2009CompatibleTree tree) {
		String potentialVegetationCode = stand.getPotentialVegetation();
		if (potentialVegetationCode == null || !internalPredictors.containsKey(potentialVegetationCode)) {
			throw new InvalidParameterException("The potential vegetation of this plot is either missing or not considered in the diameter increment submodel!");
		}
		double[] predictedGrowth = internalPredictors.get(potentialVegetationCode).predictGrowth(stand, tree);
		if (predictedGrowth[0] > 3 * stand.getGrowthStepLengthYr()) {	// if the tree grows at more than 3cm/yr than the increment is truncated to 3cm/yr
			predictedGrowth[0] = 3 * stand.getGrowthStepLengthYr();
		}
		return predictedGrowth;
	}

}
