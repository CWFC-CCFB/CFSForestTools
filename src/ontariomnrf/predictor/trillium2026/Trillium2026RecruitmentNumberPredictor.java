/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2026 His Majesty the King in right of Canada
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
package ontariomnrf.predictor.trillium2026;


import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ClimateSensitivePredictor;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.climate.REpiceaClimateVariableInformation;
import repicea.simulation.climate.REpiceaClimateVariableInformation.Resolution;
import repicea.simulation.climate.REpiceaClimateVariableProvider;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.util.ObjectUtility;

/**
 * The Iris2020RecruitmentNumberPredictor class implements the negative binomial part of the recruitment module in the Iris simulator. 
 * @author Mathieu Fortin - June 2023
 */
@SuppressWarnings("serial")
public class Trillium2026RecruitmentNumberPredictor extends REpiceaPredictor implements ClimateSensitivePredictor {

	
	private static final Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> CLIMATE_INFO = new HashMap<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>>();
	static {
		REpiceaClimateVariableInformation.fillClimateInfoMap(CLIMATE_INFO, 
				Trillium2026RecruitmentPlot.class, 
				Trillium2026RecruitmentPlot.ClimateVariableResolution);
	}

	static List<Integer> OccupancyIndexEffects = new ArrayList<Integer>();
	static {
		OccupancyIndexEffects.add(18);
	}

	static boolean IsForTestPurposes = false;

	private static ParameterMap BetaMap;
	private static ParameterMap OmegaMap;
	private static ParameterMap SpeciesEffectMatchesMap;
	
	private final Map<Species, Trillium2026RecruitmentNumberInternalPredictor> internalPredictors;
	final Trillium2026RecruitmentOccurrencePredictor occurrencePredictor;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 * @param occurrencePredictor an IrisRecruitmentOccurrencePredictor instance
	 */
	public Trillium2026RecruitmentNumberPredictor(boolean isVariabilityEnabled, Trillium2026RecruitmentOccurrencePredictor occurrencePredictor) {
		this(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled, occurrencePredictor);		
	}

	/**
	 * Protected constructor for test purposes.
	 * @param isParameterVariabilityEnabled true to enable the variability in the parameter estimates
	 * @param isRandomEffectsVariabilityEnabled true to enable the random effect variability
	 * @param isResidualVariabilityEnabled true to enable the residual variability
	 * @param occurrencePredictor an IrisRecruitmentOccurrencePredictor instance
	 */
	protected Trillium2026RecruitmentNumberPredictor(boolean isParameterVariabilityEnabled, 
			boolean isRandomEffectsVariabilityEnabled,
			boolean isResidualVariabilityEnabled, 
			Trillium2026RecruitmentOccurrencePredictor occurrencePredictor) {
		super(isParameterVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);		// no random effect in this module
		internalPredictors = new HashMap<Species, Trillium2026RecruitmentNumberInternalPredictor>();
		init();
		this.occurrencePredictor = occurrencePredictor;
	}

	@Override
	protected void init() {
		if (BetaMap == null) {
			String rootPath = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = rootPath + "0_RecruitmentNumberBeta.csv";
			String omegaFilename = rootPath + "0_RecruitmentNumberOmega.csv";
			String speciesEffectMatchesFilename = rootPath + "0_RecruitmentNumberSpeciesEffectMatches.csv";
			try {
				BetaMap = ParameterLoader.loadVectorFromFile(1, betaFilename);
				OmegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);
				SpeciesEffectMatchesMap = ParameterLoader.loadVectorFromFile(1, speciesEffectMatchesFilename);
			} catch (Exception e) {
				throw new RuntimeException("Unable to read parameters from files!");
			}
		}
		try {
			for (int spIndex = 0; spIndex < Trillium2026RecruitmentOccurrencePredictor.SpeciesList.size(); spIndex++) {
				Matrix beta = BetaMap.get(spIndex + 1);
				SymmetricMatrix omega = OmegaMap.get(spIndex + 1).squareSym();
				Matrix thetaMat = beta.getSubMatrix(beta.m_iRows - 1, beta.m_iRows - 1, 0, 0);  // theta was concatenated to beta in R
				beta = beta.getSubMatrix(0, beta.m_iRows - 2, 0, 0);  // drop theta from beta
				Matrix speciesEffectMatches = SpeciesEffectMatchesMap.get(spIndex + 1);
				speciesEffectMatches = speciesEffectMatches.getSubMatrix(0, speciesEffectMatches.m_iRows - 1, 0, 0); // assumes the last effect has been removed
				Species sp = Trillium2026RecruitmentOccurrencePredictor.SpeciesList.get(spIndex);
				Trillium2026RecruitmentNumberInternalPredictor subPredictor = new Trillium2026RecruitmentNumberInternalPredictor(this,
						sp,
						isParametersVariabilityEnabled, 
						isRandomEffectsVariabilityEnabled,
						isResidualVariabilityEnabled, 
						thetaMat.getValueAt(0, 0),
						beta, 
						omega, 
						speciesEffectMatches);
				internalPredictors.put(sp, subPredictor);
			}
		} catch (Exception e) {
			throw new InvalidParameterException("Unable to load the parameters in the module of recruitment occurrence in Iris 2020!");
		}
	}

	Trillium2026RecruitmentNumberInternalPredictor getInternalPredictor(Species species) {
		if (!Trillium2026RecruitmentOccurrencePredictor.SpeciesLookupMap.containsValue(species)) {
			throw new UnsupportedOperationException("The " + getClass().getSimpleName() + " does not support the species " + species.getLatinName());
		}
		return internalPredictors.get(species);
	}

	/**
	 * Returns the number of recruits conditional on the occurrence of recruitment.
	 * @param plot an Iris2020CompatiblePlot instance
	 * @param species an IrisSpecies enum
	 * @return a double that is the number of recruits in the plot
	 */
	public double predictNumberOfRecruits(Trillium2026RecruitmentPlot plot, Species species) {
		return getInternalPredictor(species).predictNumberOfRecruits(plot, species);
	}
	

	/*
	 * For test purposes.
	 */
	double getInvThetaParameterEstimate(Species species) {
		return getInternalPredictor(species).invTheta;
	}
	
	@Override
	public Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> getClimateVariableInformationMap() {
		return CLIMATE_INFO;
	}

}
