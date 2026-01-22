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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import canforservutility.predictor.biomass.lambert2005.Lambert2005Tree.Lambert2005Species;
import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictor;

/**
 * Implement the biomass models in Lambert et al. (2005) for each individual species.
 * @author <ul><li>Jean-Francois Lavoie 2021 <li>Mathieu Fortin January 2026 (refactoring)</ul>
 * @see <a href=https://doi.org/10.1139/x05-112> Lambert, M.-C., C.-H. Ung, and F. Raulier. 2005. Canadian
 * national tree aboveground biomass equations. Canadian Journal of Forest Research 35(8): 1996-2018 
 * </a>
 */
@SuppressWarnings("serial")
public class Lambert2005BiomassPredictor extends REpiceaPredictor {
	
	
	static final Map<String, Lambert2005Species> ENGLISH_TO_LATIN_LOOKUP_MAP = new HashMap<String, Lambert2005Species>();
	static {
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Balsam Fir", Lambert2005Species.AbiesBalsamea);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Balsam Poplar", Lambert2005Species.PopulusBalsamifera);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Black Ash", Lambert2005Species.FraxinusNigra);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Black Cherry", Lambert2005Species.PrunusSerotina);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Black Spruce", Lambert2005Species.PiceaMariana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Eastern Hemlock", Lambert2005Species.TsugaCanadensis);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Eastern White Cedar", Lambert2005Species.ThujaOccidentalis);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Eastern White Pine", Lambert2005Species.PinusStrobus);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Grey Birch", Lambert2005Species.BetulaPopulifolia);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Lodgepole Pine", Lambert2005Species.PinusContorta);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Red Pine", Lambert2005Species.PinusResinosa);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Silver Maple", Lambert2005Species.AcerSaccharinum);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Sugar Maple", Lambert2005Species.AcerSaccharum);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Tamarack Larch", Lambert2005Species.LarixLaricina);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("White Birch", Lambert2005Species.BetulaPapyrifera);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("White Oak", Lambert2005Species.QuercusAlba);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("White Spruce", Lambert2005Species.PiceaGlauca);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("feuillu", Lambert2005Species.Broadleaved);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("resineux", Lambert2005Species.Coniferous);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("all", Lambert2005Species.Any);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Alpine Fir", Lambert2005Species.AbiesLasiocarpa);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Basswood", Lambert2005Species.TiliaAmericana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Beech", Lambert2005Species.FagusGrandifolia);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Eastern Red Cedar", Lambert2005Species.JuniperusVirginiana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Hickory", Lambert2005Species.CaryaSp);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Hop-Hornbeam", Lambert2005Species.OstryaVirginiana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Jack Pine", Lambert2005Species.PinusBanksiana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Largetooth Aspen", Lambert2005Species.PopulusGrandidentata);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Red Ash", Lambert2005Species.FraxinusPennsylvanica);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Red Maple", Lambert2005Species.AcerRubrum);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Red Oak", Lambert2005Species.QuercusRubra);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Red Spruce", Lambert2005Species.PiceaRubens);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Trembling Aspen", Lambert2005Species.PopulusTremuloides);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("White Ash", Lambert2005Species.FraxinusAmericana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("White Elm", Lambert2005Species.UlmusAmericana);
		ENGLISH_TO_LATIN_LOOKUP_MAP.put("Yellow Birch", Lambert2005Species.BetulaAlleghaniensis);
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
	
	
	final Map<ModelVersion, Map<Lambert2005Species, Lambert2005BiomassInternalPredictor>> internalPredictors;

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

		internalPredictors = new HashMap<ModelVersion, Map<Lambert2005Species, Lambert2005BiomassInternalPredictor>>();
		init();
	}

	@Override
	protected void init() {
		try {
			for (ModelVersion v : ModelVersion.values()) {
				for (Lambert2005Species sp : Lambert2005Species.values()) {
					if (!internalPredictors.containsKey(v)) {
						internalPredictors.put(v, new HashMap<Lambert2005Species, Lambert2005BiomassInternalPredictor>());
					}
					Map<Lambert2005Species, Lambert2005BiomassInternalPredictor> innerMap = internalPredictors.get(v);
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
	
	static Lambert2005Species getLambertSpecies(Object[] record, int indexSpeciesField, ModelVersion v) {
		String speciesStr = record[indexSpeciesField].toString();
		if (!ENGLISH_TO_LATIN_LOOKUP_MAP.containsKey(speciesStr)) {
			throw new InvalidParameterException("This species cannot be matched to a Latin name: " + speciesStr);
		}
		Lambert2005Species species = ENGLISH_TO_LATIN_LOOKUP_MAP.get(speciesStr);
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
		Lambert2005BiomassInternalPredictor predictor = internalPredictors.get(v).get(tree.getLambert2005Species());
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
		Lambert2005Species species = Lambert2005Species.valueOf(speciesLatin); 
		Lambert2005BiomassInternalPredictor predictor = internalPredictors.get(v).get(species);
		return predictor.predictTotalBiomassMg(species, dbhCm, heightM == null ? 0d : heightM);
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
}
