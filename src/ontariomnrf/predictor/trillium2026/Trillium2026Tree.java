/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
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
import java.util.HashMap;
import java.util.Map;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.BasalAreaLargerThanSubjectM2Provider;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;

public interface Trillium2026Tree extends MonteCarloSimulationCompliantObject,
											DbhCmProvider,
											BasalAreaLargerThanSubjectM2Provider 	{

	public static enum Trillium2026TreeSpecies { //32 species or species groups
		AbiesBalsamea,
		AcerPensylvanicum,
		AcerRubrum,
		AcerSaccharinum,
		AcerSaccharum,
		BetulaAlleghaniensis,
		BetulaPapyrifera,
		CaryaSpp,
		FagusGrandifolia,
		FraxinusAmericana,
		FraxinusNigra,
		FraxinusPennsylvanica,
		JuglansSpp,				// To be merged with meridional species
		LarixLaricina,
		LiriodendronTulipifera, // To be merged with meridional species
		OstryaVirginiana,
		PiceaMariana,
		PiceaGlauca,
		PinusBanksiana,
		PinusResinosa,
		PinusStrobus,
		PopulusBalsamifera,
		PopulusGrandidentata,
		PopulusTremuloides,
		PrunusPensylvanica,
		PrunusSerotina,
		QuercusRubra,
		QuercusSpp,				// To be merged with meridional species
		ThujaOccidentalis,
		TiliaAmericana,
		TsugaCanadensis,
		UlmusSpp,
		Shrubs,
		MeridionalSpecies; 		// should further include Quercus spp, Juglans spp, Liriodendro tulipifera, Pinus rigida, Sassafras albidum
		
		private static Map<String, Trillium2026TreeSpecies> MatchingTrilliumSpeciesMap;
		private static Map<String, Trillium2026TreeSpecies> SppGroupMap;
		
		private static synchronized Map<String, Trillium2026TreeSpecies> getMatchingTrilliumSpeciesMap() {
			if (MatchingTrilliumSpeciesMap == null) {
				MatchingTrilliumSpeciesMap = new HashMap<String, Trillium2026TreeSpecies>();
				SppGroupMap = new HashMap<String, Trillium2026TreeSpecies>();
				for (Trillium2026TreeSpecies sp : Trillium2026TreeSpecies.values()) {
					String lowercaseName = sp.name().toLowerCase();
					MatchingTrilliumSpeciesMap.put(lowercaseName, sp);
					if (lowercaseName.endsWith("spp")) {
						SppGroupMap.put(lowercaseName.substring(0, lowercaseName.indexOf("spp")), sp);
					}
				}
			}
			return MatchingTrilliumSpeciesMap;
		}
		
		/**
		 * Find the enum corresponding to a species name.
		 * @param speciesLatinName the species name
		 * @return a Trillium2026TreeSpecies enum
		 */
		public static Trillium2026TreeSpecies getTrilliumSpecies(String speciesLatinName) {
			if (speciesLatinName == null) {
				throw new InvalidParameterException("The code argument cannot be null!");
			}
			
			String formattedCode = speciesLatinName.replace(" ", "").replace(".", "p").toLowerCase();
			Trillium2026TreeSpecies species = getMatchingTrilliumSpeciesMap().get(formattedCode);
			if (species != null) {
				return species;
			} else {
				for (String genus : SppGroupMap.keySet()) {
					if (formattedCode.startsWith(genus)) {
						return SppGroupMap.get(genus);
					}
				}
			}
			return null;
		}
	}
	
	
	public Trillium2026TreeSpecies getTrillium2026TreeSpecies();
}
