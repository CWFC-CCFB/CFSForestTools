/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2020-2023 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package canforservutility.predictor.iris.recruitment_v1;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canforservutility.occupancyindex.OccupancyIndexCalculator;
import canforservutility.occupancyindex.OccupancyIndexCalculablePlot;
import canforservutility.predictor.iris.recruitment_v1.IrisTree.IrisSpecies;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.util.ObjectUtility;

/**
 * The Iris2020RecruitmentOccurrencePredictor class implements the logistic part of the recruitment module in the Iris 2020 simulator.
 * @author Mathieu Fortin - May 2020
 */
@SuppressWarnings("serial")
public class IrisRecruitmentOccurrencePredictor extends REpiceaBinaryEventPredictor<IrisRecruitmentPlot, IrisTree> {

	static List<Integer> OccupancyIndexEffects = new ArrayList<Integer>();
	static {
		OccupancyIndexEffects.add(29);
		OccupancyIndexEffects.add(32);
	}

	private Map<IrisSpecies, IrisRecruitmentOccurrenceInternalPredictor> internalPredictors;

	final OccupancyIndexCalculator occIndexCalculator;
	
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 * @param plots a List of IrisProtoPlot instances that are all the plots to be considered in the calculation of the
	 * occupancy index.
	 */
	public IrisRecruitmentOccurrencePredictor(boolean isVariabilityEnabled, List<OccupancyIndexCalculablePlot> plots) {
		this(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled, plots);		// random effect variability is associated with occupancy index measurement error
	}
	
	/**
	 * Constructor for test purposes. <p>
	 * IMPORTANT: The random effect variability is only enabling the variability in 
	 * the occupancy index.
	 * 
	 * @param isParameterVariabilityEnabled true to enable the parameter estimates variability
	 * @param isRandomEffectsVariabilityEnabled true to enable the variability in the occupancy index
	 * @param isResidualVariabilityEnabled true to enable the residual error variability
	 * @param plots a List of IrisProtoPlot instances that are all the plots to be considered in the calculation of the
	 * occupancy index.
	 */
	protected IrisRecruitmentOccurrencePredictor(boolean isParameterVariabilityEnabled, 
			boolean isRandomEffectsVariabilityEnabled, 
			boolean isResidualVariabilityEnabled,
			List<OccupancyIndexCalculablePlot> plots) {
		super(isParameterVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);		
		init();
		occIndexCalculator = plots != null ? 
				new OccupancyIndexCalculator(plots) : 
					null;
	}

	@Override
	protected void init() {
		internalPredictors = new HashMap<IrisSpecies, IrisRecruitmentOccurrenceInternalPredictor>();
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
			for (IrisSpecies sp : IrisSpecies.values()) {
				Matrix beta = betaMap.get(sp.ordinal() + 1);
				SymmetricMatrix omega = omegaMap.get(sp.ordinal() + 1).squareSym();
				Matrix speciesEffectMatches = speciesEffectMatchesMap.get(sp.ordinal() + 1);
				Matrix offset = offsetListMap.get(sp.ordinal() + 1);
				boolean isOffsetEnabled = offset.getValueAt(0, 0) == 1d;
				IrisRecruitmentOccurrenceInternalPredictor subPredictor = new IrisRecruitmentOccurrenceInternalPredictor(this,
						sp,
						isParametersVariabilityEnabled, 
						isRandomEffectsVariabilityEnabled,
						isResidualVariabilityEnabled, 
						isOffsetEnabled, 
						beta, 
						omega, 
						speciesEffectMatches);
				internalPredictors.put(sp, subPredictor);
			}
		} catch (Exception e) {
			throw new InvalidParameterException("Unable to load the parameters in the module of recruitment occurrence in Iris 2020!");
		}
	}

	IrisRecruitmentOccurrenceInternalPredictor getInternalPredictor(IrisSpecies species) {
		return internalPredictors.get(species);
	}
	
	@Override
	public double predictEventProbability(IrisRecruitmentPlot stand, IrisTree tree, Map<String, Object> parms) {
		return getInternalPredictor(tree.getSpecies()).predictEventProbability(stand, tree, parms);
	}
	
	
}
