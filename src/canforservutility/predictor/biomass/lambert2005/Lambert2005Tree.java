/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * Author: Jean-Francois Lavoie
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;
import repicea.simulation.species.REpiceaSpecies;

/**
 * 
 * 
 * @author Jean-Francois Lavoie, Aug 2021
 */

public interface Lambert2005Tree extends DbhCmProvider, MonteCarloSimulationCompliantObject {

	enum Lambert2005Species implements REpiceaSpecies {	
		Coniferous(Species.Other_coniferous),
		Broadleaved(Species.Other_broadleaved),
		AbiesBalsamea(Species.Abies_balsamea),
		PopulusBalsamifera(Species.Populus_balsamifera),
		FraxinusNigra(Species.Fraxinus_nigra),
		PrunusSerotina(Species.Prunus_serotina),
		PiceaMariana(Species.Picea_mariana),
		TsugaCanadensis(Species.Tsuga_canadensis),
		ThujaOccidentalis(Species.Thuja_occidentalis),
		PinusStrobus(Species.Pinus_strobus),
		BetulaPopulifolia(Species.Betula_populifolia),
		PinusContorta(Species.Pinus_contorta),
		PinusResinosa(Species.Pinus_resinosa),
		AcerSaccharinum(Species.Acer_saccharinum),
		AcerSaccharum(Species.Acer_saccharum),
		LarixLaricina(Species.Larix_laricina),
		BetulaPapyrifera(Species.Betula_papyrifera),
		QuercusAlba(Species.Quercus_alba),
		PiceaGlauca(Species.Picea_glauca),
		AbiesLasiocarpa(Species.Abies_lasiocarpa),
		TiliaAmericana(Species.Tilia_americana),
		FagusGrandifolia(Species.Fagus_grandifolia),
		JuniperusVirginiana(Species.Juniperus_virginiana),
		CaryaSp(Species.Carya_cordiformis),
		OstryaVirginiana(Species.Ostrya_virginiana),
		PinusBanksiana(Species.Pinus_banksiana),
		PopulusGrandidentata(Species.Populus_grandidentata),
		FraxinusPennsylvanica(Species.Fraxinus_pensylvanica),
		AcerRubrum(Species.Acer_rubrum),
		QuercusRubra(Species.Quercus_rubra),
		PiceaRubens(Species.Picea_rubens),
		PopulusTremuloides(Species.Populus_tremuloides),
		FraxinusAmericana(Species.Fraxinus_americana),
		UlmusAmericana(Species.Ulmus_americana),
		BetulaAlleghaniensis(Species.Betula_alleghaniensis);
		
//		private static List<String> EligibleSpeciesCodes;
		private static Map<String, Lambert2005Species> SpeciesLookupMap;
		private static List<String> EligibleLatinNames;

		final REpiceaSpecies species;
		
		Lambert2005Species(REpiceaSpecies species) {
			this.species = species;
		}

		@Override
		public void setText(String englishText, String frenchText) {}

		@Override
		public SpeciesType getSpeciesType() {return species.getSpeciesType();}

		@Override
		public double getBarkProportionOfWoodVolume(SpeciesLocale locale) {
			return species.getBarkProportionOfWoodVolume(locale);
		}

		@Override
		public double getBasicWoodDensity(SpeciesLocale locale) {
			return species.getBarkProportionOfWoodVolume(locale);
		}

		@Override
		public String getLatinName() {
			return species.getLatinName();
		}
		
//		/**
//		 * Find an eligible species using the three-letter species code from Quebec.
//		 * @param speciesCode a String
//		 * @return a VolSpecies enum
//		 */
//		public static Lambert2005Species findEligibleSpeciesUsingQuebecSpeciesCode(String speciesCode) {
//			if (speciesCode == null) {
//				return null;
//			} else {
//				String formattedSpeciesCode = speciesCode.trim().toUpperCase();
//				if (Lambert2005Species.getCodeList().contains(formattedSpeciesCode)) {
//					return Lambert2005Species.valueOf(formattedSpeciesCode);
//				} else {
//					return null;
//				}
//			}
//		}
		
		private static synchronized Map<String, Lambert2005Species> getLookupMap() {
			if (SpeciesLookupMap == null) {
				SpeciesLookupMap = new HashMap<String, Lambert2005Species>();
				for (Lambert2005Species sp : Lambert2005Species.values()) {
					SpeciesLookupMap.put(sp.getLatinName().toLowerCase(), sp);
				}
			}
			return SpeciesLookupMap;
		}
		
//		private synchronized static List<String> getCodeList() {
//			if (EligibleSpeciesCodes == null) {
//				EligibleSpeciesCodes = new ArrayList<String>();
//				for (Lambert2005Species species : Lambert2005Species.values()) {
//					EligibleSpeciesCodes.add(species.name());
//				}
//			}
//			return EligibleSpeciesCodes;
//		}
		
		protected synchronized static List<String> getLatinNameList() {
			if (EligibleLatinNames == null) {
				EligibleLatinNames = new ArrayList<String>();
				EligibleLatinNames.addAll(Lambert2005Species.getLookupMap().keySet());
				Collections.sort(EligibleLatinNames);
			}
			return EligibleLatinNames;
		}
		
		/**
		 * Find an eligible species using the three-letter species code from Quebec.
		 * @param latinName a String
		 * @return a VolSpecies enum
		 */
		static Lambert2005Species findEligibleSpeciesUsingLatinName(String latinName) {
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

	}
	
	public REpiceaSpecies getLambert2005Species();
	
	/**
	 * Ask the tree instance if it implements the HeightMProvider
	 * @return a boolean (true if it implements the interface or false otherwise)
	 */
	public default boolean implementHeighMProvider() {
		return this instanceof HeightMProvider;
	}
	
	
}
