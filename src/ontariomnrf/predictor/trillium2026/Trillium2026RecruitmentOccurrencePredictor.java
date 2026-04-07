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

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canforservutility.occupancyindex.OccupancyIndexCalculablePlot;
import canforservutility.occupancyindex.SimpleOccupancyIndexCalculablePlot;
import repicea.io.javacsv.CSVHeader;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ClimateSensitivePredictor;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
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
 * The Trillium2026RecruitmentOccurrencePredictor class implements the logistic part 
 * of the recruitment module in the Trillium simulator.
 * @author Mathieu Fortin - March 2026
 */
@SuppressWarnings("serial")
public class Trillium2026RecruitmentOccurrencePredictor extends REpiceaBinaryEventPredictor<Trillium2026RecruitmentPlot, Trillium2026Tree> implements 
																REpiceaSpeciesCompliantObject, 
																ClimateSensitivePredictor {

	
	private static final Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> CLIMATE_INFO = new HashMap<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>>();
	static {
		REpiceaClimateVariableInformation.fillClimateInfoMap(CLIMATE_INFO, 
				Trillium2026RecruitmentPlot.class, 
				Trillium2026RecruitmentPlot.ClimateVariableResolution,
				EvaluationDate.EndOfInterval);
	}

	static Map<String, Species> SpeciesLookupMap = new HashMap<String, Species>();
	static List<Species> SpeciesList;
	static {
		Species[] species = new Species[] {
				Species.Abies_balsamea, 
				Species.Acer_pensylvanicum,
				Species.Acer_rubrum, 
				Species.Acer_saccharinum,
				Species.Acer_saccharum, 
				Species.Betula_alleghaniensis, 
				Species.Betula_papyrifera, 
//				Species.Carya_spp,
				Species.Fagus_grandifolia, 
				Species.Fraxinus_americana,
				Species.Fraxinus_nigra,
				Species.Fraxinus_pensylvanica,
				Species.Larix_laricina,
				Species.Other_broadleaved,
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
				Species.Broadleaved_shrubs,
				Species.Thuja_occidentalis,
				Species.Tilia_americana,
				Species.Tsuga_canadensis,
				Species.Ulmus_spp};
		for (Species sp : species) {
			SpeciesLookupMap.put(sp.getLatinName().trim().toLowerCase(), sp);
			SpeciesLookupMap.put("carya sp.", Species.Carya_spp);
			SpeciesLookupMap.put("meridional species", Species.Other_broadleaved);
			SpeciesLookupMap.put("shrubs", Species.Broadleaved_shrubs);
			SpeciesLookupMap.put("ulmus sp.", Species.Ulmus_spp);
			SpeciesLookupMap.put("fraxinus pennsylvanica", Species.Fraxinus_pensylvanica);
		}
		SpeciesList = Arrays.asList(species);
	}

	static List<Integer> OccupancyIndexEffects = new ArrayList<Integer>();
	static {
		OccupancyIndexEffects.add(24);
		OccupancyIndexEffects.add(27);
	}

	private static ParameterMap BetaMap;
	private static ParameterMap OmegaMap;
	private static ParameterMap SpeciesEffectMatchesMap;
	private static ParameterMap OffsetListMap;
	private static List<OccupancyIndexCalculablePlot> ReferencePlotsForOccupancyIndexCalculation;
	
	private final Map<Species, Trillium2026RecruitmentOccurrenceInternalPredictor> internalPredictors;
	protected final double minProbRecruitmentThreshold;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 * @param minProbRecruitmentThreshold the minimum probability to assume that recruitment is possible (e.g., 0.01)
	 */
	public Trillium2026RecruitmentOccurrencePredictor(boolean isVariabilityEnabled, double minProbRecruitmentThreshold) {
		this(isVariabilityEnabled, isVariabilityEnabled, minProbRecruitmentThreshold);	
	}
	
	/**
	 * Constructor for test purposes.<p>
	 * 
	 * IMPORTANT: The random effect variability is only enabling the variability in 
	 * the occupancy index.
	 *
	 * @param isParameterVariabilityEnabled true to enable the parameter estimates variability
	 * @param isResidualVariabilityEnabled true to enable the residual error variability
	 * @param minProbRecruitmentThreshold the minimum probability to assume that recruitment is possible (e.g., 0.01)
	 */
	protected Trillium2026RecruitmentOccurrencePredictor(boolean isParameterVariabilityEnabled, 
			boolean isResidualVariabilityEnabled, double minProbRecruitmentThreshold) {
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled);		
		this.minProbRecruitmentThreshold = minProbRecruitmentThreshold;
		internalPredictors = new HashMap<Species, Trillium2026RecruitmentOccurrenceInternalPredictor>();
		init();
	}

	
	private static Map<String, Map<Integer, SimpleOccupancyIndexCalculablePlot>> readRefOccupancyIndex(String filename) throws IOException {
		Map<String, Map<Integer, SimpleOccupancyIndexCalculablePlot>> occMap = new HashMap<String, Map<Integer, SimpleOccupancyIndexCalculablePlot>>(); 
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			CSVHeader header = reader.getHeader();
			while((record = reader.nextRecord()) != null) {
				String speciesStr = record[header.getIndexOfThisField("SpecGroup")].toString();
				Species sp = SpeciesLookupMap.get(speciesStr.trim().toLowerCase());
				if (sp != null) {
					String id = record[header.getIndexOfThisField("uniquePlotID")].toString();
					int dateYr = ((Number) Double.parseDouble(record[header.getIndexOfThisField("year.x")].toString())).intValue();
					if (!occMap.containsKey(id)) {
						occMap.put(id, new HashMap<Integer,SimpleOccupancyIndexCalculablePlot>());
					}
					Map<Integer, SimpleOccupancyIndexCalculablePlot> innerMap = occMap.get(id);
					if (!innerMap.containsKey(dateYr)) {
						double latitude = Double.parseDouble(record[header.getIndexOfThisField("latitudeDeg")].toString());
						double longitude = Double.parseDouble(record[header.getIndexOfThisField("longitudeDeg")].toString());
						double basalAreaM2Ha = Double.parseDouble(record[header.getIndexOfThisField("G_SpGr")].toString());
						innerMap.put(dateYr, new SimpleOccupancyIndexCalculablePlot(id, latitude, longitude, dateYr, sp, basalAreaM2Ha));
					} else {
						double basalAreaM2Ha = Double.parseDouble(record[header.getIndexOfThisField("G_SpGr")].toString());
						innerMap.get(dateYr).setBasalArea(sp, basalAreaM2Ha);
					}
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return occMap;
	}

	/**
	 * Provide a set of plots from the G&amp;Y program to assess the occupancy index.
	 * @return a List of OccupancyIndexCalculablePlot instances
	 */
	public static List<OccupancyIndexCalculablePlot> getReferencePlotsForOccupancyIndex() {
		List<OccupancyIndexCalculablePlot> copyList = new ArrayList<OccupancyIndexCalculablePlot>();
		if (ReferencePlotsForOccupancyIndexCalculation == null) {
			String rootPath = ObjectUtility.getRelativePackagePath(Trillium2026RecruitmentOccurrencePredictor.class);
			String refOccupancyIndex = rootPath + "0_recruitmentRefOccupancyIndex.csv";
			try {
				Map<String, Map<Integer,SimpleOccupancyIndexCalculablePlot>> occMap = readRefOccupancyIndex(refOccupancyIndex);
				ReferencePlotsForOccupancyIndexCalculation = new ArrayList<OccupancyIndexCalculablePlot>();
				for (Map<Integer,SimpleOccupancyIndexCalculablePlot> innerMap : occMap.values()) {
					ReferencePlotsForOccupancyIndexCalculation.addAll(innerMap.values());
				}
			} catch (IOException e) {
				throw new RuntimeException("Unable to read reference plots for occupancy index calculation!");
			}
		}
		copyList.addAll(ReferencePlotsForOccupancyIndexCalculation);
		return copyList;
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
				throw new RuntimeException("Unable to read parameters from files!");
			}
		}
		try {
			for (int spIndex = 0; spIndex < SpeciesList.size(); spIndex++) {
				Matrix beta = BetaMap.get(spIndex + 1); // index starts from 1 in file
				SymmetricMatrix omega = OmegaMap.get(spIndex + 1).squareSym(); // index starts from 1 in file
				Matrix speciesEffectMatches = SpeciesEffectMatchesMap.get(spIndex + 1); // index starts from 1 in file
				Matrix offset = OffsetListMap.get(spIndex + 1); // index starts from 1 in file
				boolean isOffsetEnabled = offset.getValueAt(0, 0) == 1d;
				Species sp = SpeciesList.get(spIndex);
				Trillium2026RecruitmentOccurrenceInternalPredictor subPredictor = new Trillium2026RecruitmentOccurrenceInternalPredictor(this,
						sp,
						isParametersVariabilityEnabled, 
						isResidualVariabilityEnabled, 
						isOffsetEnabled, 
						beta, 
						omega, 
						speciesEffectMatches);
				internalPredictors.put(sp, subPredictor);
			}
		} catch (Exception e) {
			throw new InvalidParameterException("Unable to load the parameters of Trillium recruitment occurrence module!");
		}
	}

	
	Trillium2026RecruitmentOccurrenceInternalPredictor getInternalPredictor(Species species) {
		if (!SpeciesLookupMap.containsValue(species)) {
			throw new UnsupportedOperationException("The " + getClass().getSimpleName() + " does not support the species " + species.getLatinName());
		}
		return internalPredictors.get(species);
	}

	static Species getTrillium2026SpeciesFromLatinName(String latinName) {
		String formattedName = latinName.trim().toLowerCase();
		if (!SpeciesLookupMap.containsKey(formattedName)) {
			throw new UnsupportedOperationException("The " + Trillium2026RecruitmentOccurrencePredictor.class.getSimpleName() + " does not support the species " + latinName);
		}
		return SpeciesLookupMap.get(formattedName);
	}

	@Override
	public double predictEventProbability(Trillium2026RecruitmentPlot stand, Trillium2026Tree tree, Map<String, Object> parms) {
		return getInternalPredictor(tree.getTrillium2026TreeSpecies()).predictEventProbability(stand, tree, parms);
	}

	@Override
	public List<Species> getEligibleSpecies() {return SpeciesList;}

	@Override
	public SpeciesLocale getScope() {return SpeciesLocale.Ontario;}

	@Override
	public  Map<Class<? extends REpiceaClimateVariableProvider>, Map<Resolution, REpiceaClimateVariableInformation>> getClimateVariableInformationMap() {
		return CLIMATE_INFO;
	}
	
	
}
