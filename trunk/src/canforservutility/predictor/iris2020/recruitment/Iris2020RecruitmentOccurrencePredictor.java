/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2020 Mathieu Fortin for Canadian Forest Service
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
package canforservutility.predictor.iris2020.recruitment;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatibleTree.Iris2020Species;
import repicea.math.Matrix;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.util.ObjectUtility;

/**
 * The Iris2020RecruitmentOccurrencePredictor class implements the logistic part of the recruitment module in the Iris 2020 simulator.
 * @author Mathieu Fortin - May 2020
 */
@SuppressWarnings("serial")
public class Iris2020RecruitmentOccurrencePredictor extends REpiceaBinaryEventPredictor<Iris2020CompatiblePlot, Iris2020CompatibleTree> {

	private Map<Iris2020Species, Iris2020RecruitmentOccurrenceInternalPredictor> internalPredictors;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 */
	public Iris2020RecruitmentOccurrencePredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);		// no random effect in this module
		init();
	}

	protected void init() {
		internalPredictors = new HashMap<Iris2020Species, Iris2020RecruitmentOccurrenceInternalPredictor>();
		String rootPath = ObjectUtility.getRelativePackagePath(getClass());
		String betaFilename = rootPath + "0_RecruitmentOccurrenceBeta.csv";
		String omegaFilename = rootPath + "0_RecruitmentOccurrenceOmega.csv";
		String speciesEffectMatchesFilename = rootPath + "0_RecruitmentOccurrenceSpeciesEffectMatches.csv";
		String offsetList = rootPath + "0_RecruitmentOccurrenceOffsetList.csv";
		
		try {
			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(1, betaFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);
			ParameterMap speciesEffectMatchesMap = ParameterLoader.loadVectorFromFile(1, speciesEffectMatchesFilename);
			ParameterMap offsetListMap = ParameterLoader.loadVectorFromFile(1, offsetList);
			for (Iris2020Species sp : Iris2020Species.values()) {
				Matrix beta = betaMap.get(sp.ordinal() + 1);
				Matrix omega = omegaMap.get(sp.ordinal() + 1).squareSym();
				Matrix speciesEffectMatches = speciesEffectMatchesMap.get(sp.ordinal() + 1);
				Matrix offset = offsetListMap.get(sp.ordinal() + 1);
				boolean isOffsetEnabled = offset.m_afData[0][0] == 1d;
				Iris2020RecruitmentOccurrenceInternalPredictor subPredictor = new Iris2020RecruitmentOccurrenceInternalPredictor(isParametersVariabilityEnabled, isResidualVariabilityEnabled, isOffsetEnabled);
				subPredictor.setBeta(beta, omega);
				subPredictor.setEffectList(speciesEffectMatches);
				internalPredictors.put(sp, subPredictor);
			}
		} catch (Exception e) {
			throw new InvalidParameterException("Unable to load the parameters in the module of recruitment occurrence in Iris 2020!");
		}
	}

	@Override
	public double predictEventProbability(Iris2020CompatiblePlot stand, Iris2020CompatibleTree tree, Map<Integer, Object> parms) {
		return internalPredictors.get(tree.getSpecies()).predictEventProbability(stand, tree, parms);
	}
	
	
}
