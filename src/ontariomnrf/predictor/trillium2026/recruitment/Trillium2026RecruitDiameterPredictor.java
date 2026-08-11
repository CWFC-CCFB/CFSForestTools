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
package ontariomnrf.predictor.trillium2026.recruitment;


import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import modulemanagement.SimulationModule;
import modulemanagement.SimulationModule.ModuleType;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ClimateSensitivePredictor;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.climatemanagement.REpiceaClimateVariableInformation;
import repicea.simulation.climatemanagement.REpiceaClimateVariableInformation.EvaluationDate;
import repicea.simulation.climatemanagement.REpiceaClimateVariableInformation.Resolution;
import repicea.simulation.climatemanagement.REpiceaClimateVariableProvider;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.species.REpiceaSpeciesCompliantObject;
import repicea.util.ObjectUtility;

/**
 * The Trillium2026RecruitDiameterPredictor class implements 
 * a generalized linear model based on a Gamma distribution to 
 * predict recruit diameters in the Trillium simulator. 
 * @author Mathieu Fortin - March 2026
 */
@SuppressWarnings("serial")
@SimulationModule(type = ModuleType.RecruitDiameter, scope = SpeciesLocale.Ontario)
public class Trillium2026RecruitDiameterPredictor extends REpiceaPredictor implements 
															REpiceaSpeciesCompliantObject,
															ClimateSensitivePredictor {

	
	private static final Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> CLIMATE_INFO = new HashMap<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>>();
	static {
		REpiceaClimateVariableInformation.fillClimateInfoMap(CLIMATE_INFO, 
				Trillium2026RecruitmentPlot.class, 
				Trillium2026RecruitmentPlot.ClimateVariableResolution,
				EvaluationDate.EndOfInterval);
	}


//	static boolean IsForTestPurposes = false;

	static boolean Verbose = false;
	private static ParameterMap BetaMap;
	static ParameterMap OmegaMap;
	private static ParameterMap SpeciesEffectMatchesMap;
	private static ParameterMap DispersionMap;
	
	private final Map<Species, Trillium2026RecruitDiameterInternalPredictor> internalPredictors;
	final ConcurrentHashMap<Species, Species> surrogateMap;
	public static void setVerbose(boolean verbose) {Verbose = verbose;}
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 */
	public Trillium2026RecruitDiameterPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);		
	}

	/**
	 * Protected constructor for test purposes.
	 * @param isParameterVariabilityEnabled true to enable the variability in the parameter estimates
	 * @param isResidualVariabilityEnabled true to enable the residual variability
	 */
	public Trillium2026RecruitDiameterPredictor(boolean isParameterVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect in this module
		internalPredictors = new HashMap<Species, Trillium2026RecruitDiameterInternalPredictor>();
		surrogateMap = new ConcurrentHashMap<Species, Species>();
		setSurrogateMapToDefaultValue();
		init();
	}

	@Override
	protected synchronized void init() {
		if (BetaMap == null) {
			String rootPath = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = rootPath + "0_RecruitmentDiameterBeta.csv";
			String omegaFilename = rootPath + "0_RecruitmentDiameterOmega.csv";
			String dispersionFilename = rootPath + "0_RecruitmentDiameterDispersion.csv";
			String speciesEffectMatchesFilename = rootPath + "0_RecruitmentDiameterSpeciesEffectMatches.csv";
			try {
				BetaMap = ParameterLoader.loadVectorFromFile(1, betaFilename);
				OmegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);
				DispersionMap = ParameterLoader.loadVectorFromFile(1, dispersionFilename);
				SpeciesEffectMatchesMap = ParameterLoader.loadVectorFromFile(1, speciesEffectMatchesFilename);
			} catch (Exception e) {
				throw new RuntimeException("Unable to read parameters from files!");
			}
		}
		try {
			for (int spIndex = 0; spIndex < Trillium2026RecruitmentOccurrencePredictor.SpeciesList.size(); spIndex++) {
				Matrix beta = BetaMap.get(spIndex + 1);
				SymmetricMatrix omega = OmegaMap.get(spIndex + 1).squareSym();
				Matrix dispersion = DispersionMap.get(spIndex + 1);
				Matrix speciesEffectMatches = SpeciesEffectMatchesMap.get(spIndex + 1);
				Species sp = Trillium2026RecruitmentOccurrencePredictor.SpeciesList.get(spIndex);
				Trillium2026RecruitDiameterInternalPredictor subPredictor = new Trillium2026RecruitDiameterInternalPredictor(this,
						sp,
						isParametersVariabilityEnabled, 
						isResidualVariabilityEnabled, 
						dispersion.getValueAt(0, 0),
						beta, 
						omega, 
						speciesEffectMatches);
				internalPredictors.put(sp, subPredictor);
			}
		} catch (Exception e) {
			throw new InvalidParameterException("Unable to load the parameters of Trillium recruitment abundance module!");
		}
	}


	/**
	 * Returns the recruit diameter.
	 * @param plot a Trillium2026RecruitmentPlot instance
	 * @param species an Species enum
	 * @return the recruit DBH (cm)
	 */
	public double predictRecruitDiameterCm(Trillium2026RecruitmentPlot plot, Species species) {
		Species sp = convertToEligibleSpecies(species);
		return internalPredictors.get(sp).predictRecruitDiameterCm(plot);
	}
	

	/* 
	 * For test purpose.
	 */
	double getVariance(Trillium2026RecruitmentPlot plot, Species species) {
		Species sp = convertToEligibleSpecies(species);
		return internalPredictors.get(sp).getVariance(plot);
	}

	@Override
	public Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> getClimateVariableInformationMap() {
		return CLIMATE_INFO;
	}

	@Override
	public List<Species> getEligibleSpecies() {return Trillium2026RecruitmentOccurrencePredictor.SpeciesList;}

	@Override
	public SpeciesLocale getScope() {return SpeciesLocale.Ontario;}

	@Override
	public ConcurrentHashMap<Species, Species> getSurrogateMap() {return surrogateMap;}

	@Override
	public void setSurrogateMapToDefaultValue() {
		Trillium2026RecruitmentOccurrencePredictor.setInternallySurrogateMap(surrogateMap);
	}

//	public static void main(String[] args) {
//		new Trillium2026RecruitDiameterPredictor(false);
//	}
}
