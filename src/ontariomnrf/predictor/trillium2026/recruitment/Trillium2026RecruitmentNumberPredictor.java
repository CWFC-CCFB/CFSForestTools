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
import java.util.ArrayList;
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
 * The Trillium2026RecruitmentNumberPredictor class implements the negative binomial part of the recruitment module 
 * in the Trillium simulator. 
 * @author Mathieu Fortin - March 2026
 */
@SuppressWarnings("serial")
@SimulationModule(type = ModuleType.RecruitmentAbundance, scope = SpeciesLocale.Ontario)
public class Trillium2026RecruitmentNumberPredictor extends REpiceaPredictor implements 
													REpiceaSpeciesCompliantObject,
													ClimateSensitivePredictor {

	
	private static final Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> CLIMATE_INFO = new HashMap<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>>();
	static {
		REpiceaClimateVariableInformation.fillClimateInfoMap(CLIMATE_INFO, 
				Trillium2026RecruitmentPlot.class, 
				Trillium2026RecruitmentPlot.ClimateVariableResolution,
				EvaluationDate.EndOfInterval);
	}

	static List<Integer> OccupancyIndexEffects = new ArrayList<Integer>();
	static {
//		OccupancyIndexEffects.add(19);
	}

	static boolean IsForTestPurposes = false;

	private static ParameterMap BetaMap;
	static ParameterMap OmegaMap;
	private static ParameterMap ThetaMap;
	private static ParameterMap SpeciesEffectMatchesMap;
	private static ParameterMap MaxCapMap;
	
	final Map<Species, Trillium2026RecruitmentNumberInternalPredictor> internalPredictors;
	final ConcurrentHashMap<Species, Species> surrogateMap;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 */
	public Trillium2026RecruitmentNumberPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);		
	}

	/**
	 * Protected constructor for test purposes.
	 * @param isParameterVariabilityEnabled true to enable the variability in the parameter estimates
	 * @param isResidualVariabilityEnabled true to enable the residual variability
	 */
	public Trillium2026RecruitmentNumberPredictor(boolean isParameterVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect in this module
		internalPredictors = new HashMap<Species, Trillium2026RecruitmentNumberInternalPredictor>();
		surrogateMap = new ConcurrentHashMap<Species, Species>();
		setSurrogateMapToDefaultValue();
		init();
	}

	@Override
	protected synchronized void init() {
		if (BetaMap == null) {
			String rootPath = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = rootPath + "0_RecruitmentNumberBeta.csv";
			String omegaFilename = rootPath + "0_RecruitmentNumberOmega.csv";
			String thetaFilename = rootPath + "0_RecruitmentNumberTheta.csv";
			String maxCapFilename = rootPath + "0_RecruitmentNumberMaxCap.csv";
			String speciesEffectMatchesFilename = rootPath + "0_RecruitmentNumberSpeciesEffectMatches.csv";
			try {
				BetaMap = ParameterLoader.loadVectorFromFile(1, betaFilename);
				OmegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);
				ThetaMap = ParameterLoader.loadVectorFromFile(1, thetaFilename);
				MaxCapMap = ParameterLoader.loadVectorFromFile(1, maxCapFilename);
				SpeciesEffectMatchesMap = ParameterLoader.loadVectorFromFile(1, speciesEffectMatchesFilename);
			} catch (Exception e) {
				throw new RuntimeException("Unable to read parameters from files!");
			}
		}
		try {
			for (int spIndex = 0; spIndex < Trillium2026RecruitmentOccurrencePredictor.SpeciesList.size(); spIndex++) {
				Matrix beta = BetaMap.get(spIndex + 1);
				SymmetricMatrix omega = OmegaMap.get(spIndex + 1).squareSym();
//				Matrix thetaMat = beta.getSubMatrix(beta.m_iRows - 1, beta.m_iRows - 1, 0, 0);  // theta was concatenated to beta in R
//				beta = beta.getSubMatrix(0, beta.m_iRows - 2, 0, 0);  // drop theta from beta
				Matrix thetaMat = ThetaMap.get(spIndex + 1);
				Matrix maxCap = MaxCapMap.get(spIndex + 1);
				Matrix speciesEffectMatches = SpeciesEffectMatchesMap.get(spIndex + 1);
				speciesEffectMatches = speciesEffectMatches.getSubMatrix(0, speciesEffectMatches.m_iRows - 1, 0, 0); // assumes the last effect has been removed
				Species sp = Trillium2026RecruitmentOccurrencePredictor.SpeciesList.get(spIndex);
				Trillium2026RecruitmentNumberInternalPredictor subPredictor = new Trillium2026RecruitmentNumberInternalPredictor(this,
						sp,
						isParametersVariabilityEnabled, 
						isResidualVariabilityEnabled, 
						thetaMat.getValueAt(0, 0),
						beta, 
						omega, 
						speciesEffectMatches,
						maxCap.getValueAt(0, 0));
				internalPredictors.put(sp, subPredictor);
			}
		} catch (Exception e) {
			throw new InvalidParameterException("Unable to load the parameters of Trillium recruitment abundance module!");
		}
	}


	/**
	 * Returns the number of recruits conditional on the occurrence of recruitment.
	 * @param plot an Iris2020CompatiblePlot instance
	 * @param species an IrisSpecies enum
	 * @return a double that is the number of recruits in the plot
	 */
	public double predictNumberOfRecruits(Trillium2026RecruitmentPlot plot, Species species) {
		Species sp = this.convertToEligibleSpecies(species);
		return internalPredictors.get(sp).predictNumberOfRecruits(plot);
	}
	

	/*
	 * For test purposes.
	 */
	double getInvThetaParameterEstimate(Species species) {
		Species sp = this.convertToEligibleSpecies(species);
		return internalPredictors.get(sp).invTheta;
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
//		new Trillium2026RecruitmentNumberPredictor(false);
//	}
}
