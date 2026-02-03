/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec
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
package quebecmrnfutility.predictor.volumemodels.merchantablevolume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredDbhCmProvider;
import repicea.simulation.species.REpiceaSpecies;


/**
 * This interface ensures that the Tree-derived class provides 
 * the getters for the general volume equation in Fortin et al. (2007)
 * @author Mathieu Fortin - Octobre 2009 
 */
public interface VolumableTree extends DbhCmProvider,
										SquaredDbhCmProvider,
										HeightMProvider {
	
	
	enum VolSpecies implements REpiceaSpecies {
		BOG(REpiceaSpecies.Species.Betula_populifolia),   
		BOJ(REpiceaSpecies.Species.Betula_alleghaniensis),
		BOP(REpiceaSpecies.Species.Betula_papyrifera),
		CET(REpiceaSpecies.Species.Prunus_serotina),
		CHR(REpiceaSpecies.Species.Quercus_rubra),
		EPB(REpiceaSpecies.Species.Picea_glauca),
		EPN(REpiceaSpecies.Species.Picea_mariana),
		EPR(REpiceaSpecies.Species.Picea_rubens),
		ERR(REpiceaSpecies.Species.Acer_rubrum),
		ERS(REpiceaSpecies.Species.Acer_saccharum),
		FRA(REpiceaSpecies.Species.Fraxinus_americana),
		FRN(REpiceaSpecies.Species.Fraxinus_nigra),
		HEG(REpiceaSpecies.Species.Fagus_grandifolia),
		MEL(REpiceaSpecies.Species.Larix_laricina),
		ORA(REpiceaSpecies.Species.Ulmus_americana),
		OSV(REpiceaSpecies.Species.Ostrya_virginiana),
		PEB(REpiceaSpecies.Species.Populus_balsamifera),
		PEG(REpiceaSpecies.Species.Populus_grandidentata),
		PET(REpiceaSpecies.Species.Populus_tremuloides),
		PIB(REpiceaSpecies.Species.Pinus_strobus),
		PIG(REpiceaSpecies.Species.Pinus_banksiana),
		PIR(REpiceaSpecies.Species.Pinus_resinosa),
		PRU(REpiceaSpecies.Species.Tsuga_canadensis),
		SAB(REpiceaSpecies.Species.Abies_balsamea),
		THO(REpiceaSpecies.Species.Thuja_occidentalis),
		TIL(REpiceaSpecies.Species.Tilia_americana);
		
		private static List<String> EligibleSpeciesCodes;
		private static Map<String, VolSpecies> SpeciesLookupMap;
		private static List<String> EligibleLatinNames;
		
		private Matrix dummy;
//		private SpeciesType speciesType;
		
		private final REpiceaSpecies.Species species;
		
		VolSpecies(REpiceaSpecies.Species species) {
			this.species = species;
			dummy = new Matrix(1,26);
			dummy.setValueAt(0, ordinal(), 1d);
//			if (QuebecGeneralSettings.CONIFEROUS_SPECIES.contains(this.name().toUpperCase().trim())) {
//				speciesType = SpeciesType.ConiferousSpecies;
//			} else {
//				speciesType = SpeciesType.BroadleavedSpecies;
//			}
		}
		
		@Override
		public SpeciesType getSpeciesType() {return species.getSpeciesType();}
		public Matrix getDummy() {return this.dummy;}
		
		/**
		 * Find an eligible species using the three-letter species code from Quebec.
		 * @param speciesCode a String
		 * @return a VolSpecies enum
		 */
		static VolSpecies findEligibleSpeciesUsingQuebecSpeciesCode(String speciesCode) {
			if (speciesCode == null) {
				return null;
			} else {
				String formattedSpeciesCode = speciesCode.trim().toUpperCase();
				if (VolSpecies.getCodeList().contains(formattedSpeciesCode)) {
					return VolSpecies.valueOf(formattedSpeciesCode);
				} else {
					return null;
				}
			}
		}
		
		private static synchronized Map<String, VolSpecies> getLookupMap() {
			if (SpeciesLookupMap == null) {
				SpeciesLookupMap = new HashMap<String, VolSpecies>();
				for (VolSpecies sp : VolSpecies.values()) {
					SpeciesLookupMap.put(sp.getLatinName().toLowerCase(), sp);
				}
			}
			return SpeciesLookupMap;
		}
		
		private synchronized static List<String> getCodeList() {
			if (EligibleSpeciesCodes == null) {
				EligibleSpeciesCodes = new ArrayList<String>();
				for (VolSpecies species : VolSpecies.values()) {
					EligibleSpeciesCodes.add(species.name());
				}
			}
			return EligibleSpeciesCodes;
		}
		
		protected synchronized static List<String> getLatinNameList() {
			if (EligibleLatinNames == null) {
				EligibleLatinNames = new ArrayList<String>();
				EligibleLatinNames.addAll(VolSpecies.getLookupMap().keySet());
				Collections.sort(EligibleLatinNames);
			}
			return EligibleLatinNames;
		}
		
		/**
		 * Find an eligible species using the three-letter species code from Quebec.
		 * @param latinName a String
		 * @return a VolSpecies enum
		 */
		static VolSpecies findEligibleSpeciesUsingLatinName(String latinName) {
			if (latinName == null || latinName.isEmpty()) {
				return null;
			} else {
				String formattedLatinName = latinName.trim().toLowerCase();
				if (getLatinNameList().contains(formattedLatinName)) {
					return getLookupMap().get(formattedLatinName);
				} else {
					return null;
				}
			}
		}

		@Override
		public void setText(String englishText, String frenchText) {}

		@Override
		public double getBarkProportionOfWoodVolume(SpeciesLocale locale) {
			return species.getBarkProportionOfWoodVolume(locale);
		}
		
		@Override
		public double getBasicWoodDensity(SpeciesLocale locale) {
			return species.getBasicWoodDensity(locale);
		}

		@Override
		public String getLatinName() {return species.getLatinName();}
	}
	
	
	/**
	 * This method ensures the species compatibility with the volume model.
	 * @return a VolSpecies enum instance
	 */
	public REpiceaSpecies getVolumableTreeSpecies();
	
}
