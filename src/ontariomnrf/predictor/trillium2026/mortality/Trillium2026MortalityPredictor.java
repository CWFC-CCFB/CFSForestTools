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
package ontariomnrf.predictor.trillium2026.mortality;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import modulemanagement.SimulationModule;
import modulemanagement.SimulationModule.ModuleType;
import ontariomnrf.predictor.trillium2026.Trillium2026Tree;
import repicea.io.javacsv.CSVReader;
import repicea.simulation.ClimateSensitivePredictor;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.climate.REpiceaClimateVariableInformation;
import repicea.simulation.climate.REpiceaClimateVariableInformation.EvaluationDate;
import repicea.simulation.climate.REpiceaClimateVariableInformation.Resolution;
import repicea.simulation.climate.REpiceaClimateVariableProvider;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.species.REpiceaSpeciesCompliantObject;
import repicea.util.ObjectUtility;

/**
 * Implementation of the mortality model based in interval-averaged climate variables shown in 
 * Fortin et al. (2025).<p>
 * The approach has been extended to all the species groups in Ontario.
 * @author Mathieu Fortin - February 2026
 * @see <a href=https://doi.org/10.1139/cjfr-2024-0205> Fortin, M., J. Riofrio, L. C. de Melo, M. W. Ashiq, M. Sharma,
 * C. Howard, and B. N. I. Eskelson. 2025. Climate-sensitive models of tree mortality based on lifetime analysis and irregular
 * permanent-plot remeasurements. Canadian Journal of Forest Research 55: 1-15
 * </a>
 */
@SuppressWarnings("serial")
@SimulationModule(type = ModuleType.Mortality, scope = SpeciesLocale.Ontario)
public class Trillium2026MortalityPredictor extends REpiceaBinaryEventPredictor<Trillium2026MortalityPlot, Trillium2026Tree>
											implements REpiceaSpeciesCompliantObject,
														ClimateSensitivePredictor {

	private static final Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> CLIMATE_INFO = new HashMap<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>>();
	static {
		REpiceaClimateVariableInformation.fillClimateInfoMap(CLIMATE_INFO, 
				Trillium2026MortalityPlot.class, 
				Trillium2026MortalityPlot.ClimateVariableResolution,
				EvaluationDate.EndOfInterval);
	}

	
	private static final Map<String, Species> SpeciesLookupMap = new HashMap<String, Species>();
	private static final List<Species> SpeciesList = Collections.unmodifiableList(Arrays.asList(
				Species.Abies_balsamea, 
				Species.Acer_pensylvanicum, 
				Species.Acer_rubrum,
				Species.Acer_saccharinum, 
				Species.Acer_saccharum, 
				Species.Betula_alleghaniensis,
				Species.Betula_papyrifera, 
				Species.Carya_spp,
				Species.Fagus_grandifolia, 
				Species.Fraxinus_americana,
				Species.Fraxinus_nigra, 
				Species.Fraxinus_pennsylvanica,
				Species.Juglans_spp,
				Species.Larix_laricina, 
				Species.Other_broadleaved, // Meridional species
				Species.Ostrya_virginiana,
				Species.Picea_glauca, 
				Species.Picea_mariana, 
				Species.Pinus_banksiana,
				Species.Pinus_resinosa, 
				Species.Pinus_strobus, 
				Species.Populus_balsamifera,
				Species.Populus_grandidentata, 
				Species.Populus_tremuloides, 
				Species.Prunus_pensylvanica,
				Species.Prunus_serotina, 
				Species.Quercus_rubra, 
				Species.Quercus_spp,
				Species.Broadleaved_shrubs,	// shrubs
				Species.Thuja_occidentalis,
				Species.Tilia_americana, 
				Species.Tsuga_canadensis,
				Species.Ulmus_spp));
	
	static {
		for (Species sp : SpeciesList) {
			SpeciesLookupMap.put(sp.getLatinName().toLowerCase().trim(), sp);
		}
		SpeciesLookupMap.put("carya sp.", Species.Carya_spp);
		SpeciesLookupMap.put("juglans sp.", Species.Juglans_spp);
		SpeciesLookupMap.put("meridional species", Species.Other_broadleaved);
		SpeciesLookupMap.put("quercus sp.", Species.Quercus_spp);
		SpeciesLookupMap.put("shrubs", Species.Broadleaved_shrubs);
		SpeciesLookupMap.put("ulmus sp.", Species.Ulmus_spp);
	}
	
	private static HashMap<Species, List<Double>> CoefLists;
	private static HashMap<Species, List<Double>> EffectLists;
	static HashMap<Species, List<Double>> VCovLists;
	private static HashMap<Species, List<Double>> RanefVarLists;
	
	private final Map<Species, Trillium2026MortalityInternalPredictor> internalPredictorMap;
	final ConcurrentHashMap<Species, Species> surrogateMap;

	/**
	 * Constructor.
	 * @param isParametersVariabilityEnabled a boolean to enable the variability in the parameter estimates
	 * @param isRandomEffectsVariabilityEnabled a boolean to enable the variability in the random effect
	 * @param isResidualVariabilityEnabled a boolean to enable the residual variability 
	 */
	public Trillium2026MortalityPredictor(boolean isParametersVariabilityEnabled, boolean isRandomEffectsVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		internalPredictorMap = new HashMap<Species, Trillium2026MortalityInternalPredictor>();
		surrogateMap = new ConcurrentHashMap<Species, Species>();
		setSurrogateMapToDefaultValue();
		init();
	}
	
	/**
	 * General predictor
	 * @param isVariabilityEnabled true to run in stochastic model or false for deterministic
	 */
	public Trillium2026MortalityPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
	}

	@Override
	public double predictEventProbability(Trillium2026MortalityPlot plot, Trillium2026Tree tree, Map<String, Object> parms) {
		Species species = convertToEligibleSpecies(tree.getSpecies(this));
		if (!SpeciesLookupMap.values().contains(species)) {
			throw new UnsupportedOperationException("The mortality model of Trillium 2026 does not support species: " + species.getLatinName());
		}
		return internalPredictorMap.get(species).predictEventProbability(plot, tree);
	}
	
	@Override
	public List<Species> getEligibleSpecies() {return SpeciesList;}

	@Override
	protected synchronized void init() {
		if (CoefLists == null) {
			String path = ObjectUtility.getRelativePackagePath(getClass());

			EffectLists = new HashMap<Species, List<Double>>();
			String effectFilename = path + "0_mort_effectList.csv";
			readFile(EffectLists, effectFilename);
			
			CoefLists = new HashMap<Species, List<Double>>();
			String paramFilename = path + "0_mort_coefs.csv";
			readFile(CoefLists, paramFilename);
			
			VCovLists = new HashMap<Species, List<Double>>();
			String vcovFilename = path + "0_mort_vcov.csv";
			readFile(VCovLists, vcovFilename);

			RanefVarLists = new HashMap<Species, List<Double>>();
			String ranefVarFilename = path + "0_mort_ranefVar.csv";
			readFile(RanefVarLists, ranefVarFilename);
		}
		
		for (Species sp : EffectLists.keySet()) {
			internalPredictorMap.put(sp, new Trillium2026MortalityInternalPredictor(
					this.isParametersVariabilityEnabled,
					this.isRandomEffectsVariabilityEnabled,
					this.isResidualVariabilityEnabled,
					EffectLists.get(sp),
					CoefLists.get(sp),
					VCovLists.get(sp),
					RanefVarLists.get(sp),
					this));
		}
	}

	static Species getSpeciesFromString(String speciesName) {
		Species species = SpeciesLookupMap.get(speciesName.toLowerCase().trim());
		if (species == null) {
			throw new UnsupportedOperationException("The mortality model of Trillium 2026 does not support species: " + speciesName);
		}
		return species;
	}
	
	private void readFile(Map<Species, List<Double>> oMap, String filename) {
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				String speciesName = record[0].toString();
				double parm = Double.parseDouble(record[1].toString());
				Species species = getSpeciesFromString(speciesName);
				if (!oMap.containsKey(species)) {
					oMap.put(species, new ArrayList<Double>());
				}
				oMap.get(species).add(parm);
			}
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	@Override
	public SpeciesLocale getScope() {return SpeciesLocale.Ontario;}

	@Override
	public Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> getClimateVariableInformationMap() {
		return CLIMATE_INFO;
	}

	@Override
	public ConcurrentHashMap<Species, Species> getSurrogateMap() {return surrogateMap;}

	@Override
	public void setSurrogateMapToDefaultValue() {
		surrogateMap.clear();
		surrogateMap.put(Species.Betula_populifolia, Species.Betula_papyrifera);
		surrogateMap.put(Species.Juniperus_virginiana, Species.Thuja_occidentalis);
		surrogateMap.put(Species.Picea_rubens, Species.Picea_mariana);
		surrogateMap.put(Species.Other_coniferous, Species.Abies_balsamea);
	}
}
