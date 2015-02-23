/*
 * English version follows
 * 
 * Ce fichier fait partie de la bibliothèque mrnf-foresttools.
 * Il est protégé par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du ministère des Ressources naturelles et de la Faune du Gouvernement du 
 * Québec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Québec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Ministère des Ressources naturelles et de la Faune du Québec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Québec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Québec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Ministère des Ressources naturelles et de la Faune du Québec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.predictor.officialharvestmodule;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredDbhCmProvider;

/**
 * Trees that can be harvested by the official harvester.
 * @author M. Fortin - May 2010
 */
public interface OfficialHarvestableTree extends DbhCmProvider,
												SquaredDbhCmProvider {
	
	public enum OfficialHarvestableSpecies {
		AUT,
		BOJ,
		BOP,
		EPX,
		ERR,
		ERS,
		FEU,
		HEG,
		PEU,
		PIN,
		RES,
		SAB,
		THO,
		TIL;
		
		private static EnumSet<OfficialHarvestableSpecies>	setEnumOther	= EnumSet.of(BOJ, BOP, EPX, ERR, ERS, FEU, HEG, PEU, PIN, RES, SAB, THO);
		private static EnumSet<OfficialHarvestableSpecies>	setEnum35	= EnumSet.of(BOJ, BOP, EPX, ERR, ERS, FEU, HEG, PEU, RES, SAB, THO);
		private static EnumSet<OfficialHarvestableSpecies>	setEnum45	= EnumSet.of(BOJ, BOP, EPX, ERR, ERS, FEU, HEG, SAB, THO);
		private static EnumSet<OfficialHarvestableSpecies>	setEnumCPI_CP	= EnumSet.of(BOJ, ERR, ERS, FEU, HEG, RES, SAB);
		private static EnumSet<OfficialHarvestableSpecies>	setEnumCPI_RL	= EnumSet.of(AUT, BOJ, ERR, ERS, HEG, SAB, TIL);
		private static EnumSet<OfficialHarvestableSpecies>	setEnumCRS	= EnumSet.of(AUT, ERS, HEG);
		private OfficialHarvestableSpecies() {		
		}
		
		public Matrix getDummy(TreatmentType treatment) {
			EnumSet<OfficialHarvestableSpecies>	setEnum = null;
			setEnum = getEnumSet(treatment);
			int pos = 0;
			for (OfficialHarvestableSpecies species : setEnum) {
				if (species == this) {
					break;
				}
				pos++;
			}
			Matrix dummyTmp = new Matrix(1, setEnum.size());
			dummyTmp.m_afData[0][pos] = 1d;
			return dummyTmp;
		}

		private static HashMap<String, Set<String>>	eligibleSpeciesNames	= new HashMap<String, Set<String>>();		
		
		/**
		 * This method returns the OfficialHarvestableSpecies Enum associated
		 * with the string.
		 * 
		 * @param speciesName the species name
		 * @param treatment the treatment type for the harvester
		 * @return the appropriate enum or null if the string does not match any species
		 */
		public static OfficialHarvestableSpecies findEligibleSpecies(String speciesName, TreatmentType pTreatment) {

			if (eligibleSpeciesNames.get(pTreatment.toString()) == null) {
				Set<String> eligibleSpeciesNamesSet = new HashSet<String>();
				eligibleSpeciesNames.put(pTreatment.toString(), eligibleSpeciesNamesSet);
				for (OfficialHarvestableSpecies species : getEnumSet(pTreatment)) {
					eligibleSpeciesNamesSet.add(species.name());
				}
			}

			if (speciesName == null) {
				return null;
			} else {
				String formattedSpeciesName = speciesName.trim().toUpperCase();
				Map<String, String> speciesMap =  OfficialHarvestModel.speciesMap.get(pTreatment);
				if(speciesMap != null){
					formattedSpeciesName = speciesMap.get(formattedSpeciesName);
				}
				
				Set<String> eligibleSpeciesNamesSet = eligibleSpeciesNames.get(pTreatment.toString());
				if (eligibleSpeciesNamesSet.contains(formattedSpeciesName)) {
					return OfficialHarvestableSpecies.valueOf(formattedSpeciesName);
				} else {
					return null;
				}
			}
		}
		
		private static EnumSet<OfficialHarvestableSpecies> getEnumSet(TreatmentType treatment) {
			EnumSet<OfficialHarvestableSpecies> set = null;
			if (TreatmentType.CP_35 == treatment){
				set = setEnum35;
			} else if(TreatmentType.CP_45 == treatment) {
				set = setEnum45;
			} else if(TreatmentType.CPI_CP == treatment) {
				set = setEnumCPI_CP;
			} else if(TreatmentType.CPI_RL == treatment) {
				set = setEnumCPI_RL;
			} else if(TreatmentType.CRS == treatment){
				set = setEnumCRS;
			} else {
				set = setEnumOther;
			}
			return set;
		}
	}
	
	
	public OfficialHarvestableSpecies getOfficialHarvestableTreeSpecies(TreatmentType treatment);
}
