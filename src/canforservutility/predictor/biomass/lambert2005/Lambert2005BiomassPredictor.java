/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2021-2026 His Majesty the King in right of Canada
 * Authors: Jean-Francois Lavoie and Mathieu Fortin, Canadian Forest Service
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package canforservutility.predictor.biomass.lambert2005;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.species.REpiceaSpeciesCompliantObject;

/**
 * Implement the biomass models in Lambert et al. (2005) for each individual species.
 * @author <ul><li>Jean-Francois Lavoie 2021 <li>Mathieu Fortin January 2026 (refactoring)</ul>
 * @see <a href=https://doi.org/10.1139/x05-112> Lambert, M.-C., C.-H. Ung, and F. Raulier. 2005. Canadian
 * national tree aboveground biomass equations. Canadian Journal of Forest Research 35(8): 1996-2018 
 * </a>
 */
@SuppressWarnings("serial")
public class Lambert2005BiomassPredictor extends REpiceaPredictor implements REpiceaSpeciesCompliantObject {
	
	static final Map<String, Species> ENGLISH_TO_LATIN_LOOKUP_MAP = new HashMap<String, Species>();
	static {
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Balsam Fir", Species.Abies_balsamea);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Balsam Poplar", Species.Populus_balsamifera);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Black Ash", Species.Fraxinus_nigra);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Black Cherry", Species.Prunus_serotina);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Black Spruce", Species.Picea_mariana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Eastern Hemlock", Species.Tsuga_canadensis);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Eastern White Cedar", Species.Thuja_occidentalis);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Eastern White Pine", Species.Pinus_strobus);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Grey Birch", Species.Betula_populifolia);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Lodgepole Pine", Species.Pinus_contorta);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Red Pine", Species.Pinus_resinosa);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Silver Maple", Species.Acer_saccharinum);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Sugar Maple", Species.Acer_saccharum);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Tamarack Larch", Species.Larix_laricina);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("White Birch", Species.Betula_papyrifera);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("White Oak", Species.Quercus_alba);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("White Spruce", Species.Picea_glauca);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("feuillu", Species.Other_broadleaved);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("resineux", Species.Other_coniferous);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("all", Species.Other);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Alpine Fir", Species.Abies_lasiocarpa);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Basswood", Species.Tilia_americana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Beech", Species.Fagus_grandifolia);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Eastern Red Cedar", Species.Juniperus_virginiana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Hickory", Species.Carya_spp);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Hop-Hornbeam", Species.Ostrya_virginiana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Jack Pine", Species.Pinus_banksiana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Largetooth Aspen", Species.Populus_grandidentata);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Red Ash", Species.Fraxinus_pensylvanica);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Red Maple", Species.Acer_rubrum);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Red Oak", Species.Quercus_rubra);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Red Spruce", Species.Picea_rubens);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Trembling Aspen", Species.Populus_tremuloides);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("White Ash", Species.Fraxinus_americana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("White Elm", Species.Ulmus_americana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Yellow Birch", Species.Betula_alleghaniensis);
	}
	
	static final Map<String, Species> Species_LookupMap = new HashMap<String, Species>();
	static {
		for (Species sp : ENGLISH_TO_LATIN_LOOKUP_MAP.values()) {
			Species_LookupMap.put(sp.getLatinName().trim().toLowerCase(), sp);
		}
	}
	
	public static enum BiomassCompartment {
		/**
		 * Wood between 0.30 cm in height and minimum diameter of 9 cm.
		 */
		WOOD(0, "wood"),
		/**
		 * Bark between 0.30 cm in height and minimum diameter of 9 cm.
		 */
		BARK(1, "bark"),
		/**
		 * Wood and bark between 0.30 cm in height and minimum diameter of 9 cm.
		 */
		STEM(-1, "n/a"),
		/**
		 * Wood and bark of tree top and branches with a large-end diameter of 9 cm or less.
		 */
		BRANCHES(2, "branche"),
		/**
		 * Foliage.
		 */
		FOLIAGE(3, "foliage"),
		/**
		 * Sum of the foliage and branches.
		 */
		CROWN(-1, "n/a"),
		/**
		 * Sum of foliage, branches, bark and wood.
		 */
		TOTAL(-1, "n/a");
		
		final int rank;
		final String fieldName;
		
		private static List<BiomassCompartment> basicBiomassCompartment;
		static List<BiomassCompartment> getBasicBiomassCompartments() {
			if (basicBiomassCompartment == null) {
				basicBiomassCompartment = new ArrayList<BiomassCompartment>();
				basicBiomassCompartment.add(WOOD);
				basicBiomassCompartment.add(BARK);
				basicBiomassCompartment.add(BRANCHES);
				basicBiomassCompartment.add(FOLIAGE);
				
			}
			return basicBiomassCompartment;}  
		private BiomassCompartment(int _rank, String fieldName) {
			rank = _rank;
			this.fieldName = fieldName;
		}		
		
		static List<String> getWeightLabels() {
			List<BiomassCompartment> compartments = Arrays.asList(BiomassCompartment.values());
			List<String> weightLabels = compartments.stream().map(p -> p.name().toLowerCase().concat("2")).collect(Collectors.toList());
			return weightLabels;
		}
		
		static List<String> getErrorCovarianceEquationLabels() {
			List<BiomassCompartment> compartments = Arrays.asList(BiomassCompartment.values());
			List<String> errCovNames = compartments.stream().map(p -> p.name().toLowerCase()).collect(Collectors.toList());
			return errCovNames;
		}
	}

	private static List<String> PARMS_PREFIX = Arrays.asList(new String[] {"a","b","c"});
	
	
	/**
	 * Define the version of the model.
	 */
	enum ModelVersion {
		/**
		 * Complete version of the model with dependence on DBH and height.
		 */
		Complete(3),
		/**
		 * Reduced version of the model with dependence on DBH only.
		 */
		Reduced(2);
		
		final int nbParms;
		static List<String> parmsNameComplete;
		static List<String> parmsNameReduced;
		
		ModelVersion(int nbParms) {
			this.nbParms = nbParms;
		}
		
		static List<String> getParmNames(ModelVersion v) {
			if (v == ModelVersion.Complete) {
				if (parmsNameComplete == null) {
					parmsNameComplete = produceListParms(v);
				}
				return parmsNameComplete;
			} else {
				if (parmsNameReduced == null) {
					parmsNameReduced = produceListParms(v);
				}
				return parmsNameReduced;
			}
		}
	}
	
	private static List<String> produceListParms(ModelVersion v) {
		List<String> l = new ArrayList<String>();
		for (BiomassCompartment bc : BiomassCompartment.getBasicBiomassCompartments()) {
			for (int i = 0; i < v.nbParms; i++) {
				l.add(PARMS_PREFIX.get(i) + bc.fieldName);
			}
		}
		return l;
	}
	
	
	final Map<ModelVersion, Map<Species, Lambert2005BiomassInternalPredictor>> internalPredictors;

	/**
	 * Default constructor for deterministic simulations.
	 */
	public Lambert2005BiomassPredictor() {
		this(false);
	}

	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode.
	 */
	public Lambert2005BiomassPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);
	}
	
	protected Lambert2005BiomassPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);

		internalPredictors = new HashMap<ModelVersion, Map<Species, Lambert2005BiomassInternalPredictor>>();
		init();
	}

	@Override
	protected void init() {
		try {
			for (ModelVersion v : ModelVersion.values()) {
				for (Species sp : ENGLISH_TO_LATIN_LOOKUP_MAP.values()) {
					if (!internalPredictors.containsKey(v)) {
						internalPredictors.put(v, new HashMap<Species, Lambert2005BiomassInternalPredictor>());
					}
					Map<Species, Lambert2005BiomassInternalPredictor> innerMap = internalPredictors.get(v);
					innerMap.put(sp, new Lambert2005BiomassInternalPredictor(v, 
							sp, 
							isParametersVariabilityEnabled,
							isResidualVariabilityEnabled,
							BiomassParameterLoader.getInstance()));
				}
			}
		} catch (Exception e) {
			System.out.println("Lambert2005BiomassPredictor.init() : Unable to initialize the predictor module!  Details : " + e.getMessage());			
		}		
	}
	
	static Species getLambertSpecies(Object[] record, int indexSpeciesField, ModelVersion v) {
		String speciesStr = record[indexSpeciesField].toString();
		if (!ENGLISH_TO_LATIN_LOOKUP_MAP.containsKey(speciesStr)) {
			throw new InvalidParameterException("This species cannot be matched to a Species enum: " + speciesStr);
		}
		Species species = ENGLISH_TO_LATIN_LOOKUP_MAP.get(speciesStr);
		return species;
	}

	static Species findEligibleSpeciesUsingLatinName(String latinName) {
		String formattedLatinName = latinName.trim().toLowerCase();
		Species species = Species_LookupMap.get(formattedLatinName);
		if (species == null) {
			throw new UnsupportedOperationException("This species cannot be match with a Species enum: " + latinName);
		}
		return species;
	}
	
	/**
	 * Provide the biomass in different compartments of a particular tree. <p>
	 * The output is a Matrix instance whose elements represent the biomass (kg) of the different compartment calculated for a single tree (NO EXPANSION FACTOR). 
	 * The values of the different compartments can be accessed using the BiomassCompartment enum (e.g., myBiomass.getValueAt(BiomassCompartment.WOOD.ordinal(), 0);).
	 * @param tree a Lambert2005Tree instance
	 * @return a Matrix instance that is a column vector 
	 */
	public Matrix predictBiomassKg(Lambert2005Tree tree) {
		ModelVersion v = tree.implementHeighMProvider() ? ModelVersion.Complete : ModelVersion.Reduced;
		Species species = tree.getLambert2005Species();
		if (!Species_LookupMap.containsValue(species)) {
			throw new UnsupportedOperationException("The species " + species.getLatinName() + " is not supported in Lambert et al.'s biomass model!");
		}
		Lambert2005BiomassInternalPredictor predictor = internalPredictors.get(v).get(species);
		return predictor.predictBiomass(tree);
	}

	/**
	 * Fast track for deterministic predictions with either models.<p>
	 * If heightM is not null, the complete version is used. Otherwise, it 
	 * uses the reduced version.
	 * @param speciesLatin the Latin name (e.g. AbiesBalsamea)
	 * @param dbhCm tree diameter (cm)
	 * @param heightM tree height (m)
	 * @return the total biomass (Mg)
	 */
	public double predictTotalBiomassMg(String speciesLatin, double dbhCm, Double heightM) {
		if (dbhCm <= 0d) {
			throw new InvalidParameterException("The dbhCm argument must be positive!");
		}
		if (heightM != null && heightM <= 0) {
			throw new InvalidParameterException("If not null, the heightM argument must be positive!");
		}
		ModelVersion v = heightM != null ? ModelVersion.Complete : ModelVersion.Reduced;
		Species species = findEligibleSpeciesUsingLatinName(speciesLatin);
		Lambert2005BiomassInternalPredictor predictor = internalPredictors.get(v).get(species);
		return predictor.predictTotalBiomassMg(dbhCm, heightM == null ? 0d : heightM);
	}

	/**
	 * Fast track for deterministic predictions with the reduced model.
	 * @param speciesLatin the Latin name (e.g. AbiesBalsamea)
	 * @param dbhCm tree diameter (cm)
	 * @return the total biomass (Mg)
	 */
	public double predictTotalBiomassMg(String speciesLatin, double dbhCm) {
		return predictTotalBiomassMg(speciesLatin, dbhCm, null);
	}

	Matrix getWeight(Lambert2005Tree tree) {
		ModelVersion v = tree.implementHeighMProvider() ? ModelVersion.Complete : ModelVersion.Reduced;
		Lambert2005BiomassInternalPredictor predictor = internalPredictors.get(v).get(tree.getLambert2005Species());
		return predictor.getWeight(tree);
	}

	@Override
	public List<Species> getEligibleSpecies() {
		List<Species> speciesList = new ArrayList<Species>(Species_LookupMap.values());
		Collections.sort(speciesList);
		return speciesList;
	}

	@Override
	public SpeciesLocale getScope() {return SpeciesLocale.Canada;}	
}
