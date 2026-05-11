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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canforservutility.predictor.iris.recruitment_v1.IrisTree.IrisSpecies;
import modulemanagement.SimulationModule;
import modulemanagement.SimulationModule.ModuleType;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ClimateSensitivePredictor;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.climate.REpiceaClimateVariableInformation;
import repicea.simulation.climate.REpiceaClimateVariableInformation.EvaluationDate;
import repicea.simulation.climate.REpiceaClimateVariableInformation.Resolution;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.climate.REpiceaClimateVariableProvider;
import repicea.util.ObjectUtility;

/**
 * The Iris2020RecruitmentOccurrencePredictor class implements the logistic part of the recruitment module in the Iris 2020 simulator.
 * @author Mathieu Fortin - May 2020
 */
@SuppressWarnings("serial")
@SimulationModule(type = ModuleType.RecruitmentOccurrence, scope = SpeciesLocale.Quebec)
public class IrisRecruitmentOccurrencePredictor extends REpiceaBinaryEventPredictor<IrisRecruitmentPlot, IrisTree> 
												implements ClimateSensitivePredictor {

	static ParameterMap BetaMap;
	static ParameterMap OmegaMap;
	static ParameterMap SpeciesEffectMatchesMap;
	static ParameterMap OffsetListMap;

	
	
	static final Resolution RecruitmentClimateVariableResolution = Resolution.IntervalAveragedStarting20YrsBeforeFinalMeasurement;

	private static final Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> CLIMATE_INFO = new HashMap<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>>();
	static {
		REpiceaClimateVariableInformation.fillClimateInfoMap(CLIMATE_INFO, IrisRecruitmentPlot.class, RecruitmentClimateVariableResolution, EvaluationDate.EndOfInterval);
	}


	static List<Integer> OccupancyIndexEffects = new ArrayList<Integer>();
	static {
		OccupancyIndexEffects.add(29);
		OccupancyIndexEffects.add(32);
	}

	private final Map<IrisSpecies, IrisRecruitmentOccurrenceInternalPredictor> internalPredictors;

	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 */
	public IrisRecruitmentOccurrencePredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);		// random effect variability is associated with occupancy index measurement error
	}
	
	/**
	 * Constructor for test purposes. <p>
	 * 
	 * @param isParameterVariabilityEnabled true to enable the parameter estimates variability
	 * @param isResidualVariabilityEnabled true to enable the residual error variability
	 */
	protected IrisRecruitmentOccurrencePredictor(boolean isParameterVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled);		
		internalPredictors = new HashMap<IrisSpecies, IrisRecruitmentOccurrenceInternalPredictor>();
		init();
	}

	@Override
	protected synchronized void init() {
		if (BetaMap == null) {
			String rootPath = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = rootPath + "0_RecruitmentOccurrenceBeta.csv";
			String omegaFilename = rootPath + "0_RecruitmentOccurrenceOmega.csv";
			String speciesEffectMatchesFilename = rootPath + "0_RecruitmentOccurrenceSpeciesEffectMatches.csv";
			String offsetList = rootPath + "0_RecruitmentOccurrenceOffsetList.csv";
			
			try {
				BetaMap = ParameterLoader.loadVectorFromFile(1, betaFilename);
				OmegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);
				SpeciesEffectMatchesMap = ParameterLoader.loadVectorFromFile(1, speciesEffectMatchesFilename);
				OffsetListMap = ParameterLoader.loadVectorFromFile(1, offsetList);
			} catch (IOException e) {
				throw new UnsupportedOperationException(e);
			}
		}
		for (IrisSpecies sp : IrisSpecies.values()) {
			Matrix beta = BetaMap.get(sp.ordinal() + 1);
			SymmetricMatrix omega = OmegaMap.get(sp.ordinal() + 1).squareSym();
			Matrix speciesEffectMatches = SpeciesEffectMatchesMap.get(sp.ordinal() + 1);
			Matrix offset = OffsetListMap.get(sp.ordinal() + 1);
			boolean isOffsetEnabled = offset.getValueAt(0, 0) == 1d;
			IrisRecruitmentOccurrenceInternalPredictor subPredictor = new IrisRecruitmentOccurrenceInternalPredictor(this,
					sp,
					isParametersVariabilityEnabled, 
					isResidualVariabilityEnabled, 
					isOffsetEnabled, 
					beta, 
					omega, 
					speciesEffectMatches);
			internalPredictors.put(sp, subPredictor);
		}
	}

	IrisRecruitmentOccurrenceInternalPredictor getInternalPredictor(IrisSpecies species) {
		return internalPredictors.get(species);
	}
	
	@Override
	public double predictEventProbability(IrisRecruitmentPlot stand, IrisTree tree, Map<String, Object> parms) {
		return getInternalPredictor(tree.getSpecies()).predictEventProbability(stand, tree, parms);
	}
	
	@Override
	public Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> getClimateVariableInformationMap() {
		return CLIMATE_INFO;
	}

}
