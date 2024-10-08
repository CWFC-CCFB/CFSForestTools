/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge-Epicea
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
package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel.TreatmentType;
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
		
		private static List<OfficialHarvestableSpecies>	setEnumOther	= Arrays.asList(BOJ, BOP, EPX, ERR, ERS, FEU, HEG, PEU, PIN, RES, SAB, THO);
		private static List<OfficialHarvestableSpecies>	setEnum35	= Arrays.asList(BOJ, BOP, EPX, ERR, ERS, FEU, HEG, PEU, RES, SAB, THO);
		private static List<OfficialHarvestableSpecies>	setEnum45	= Arrays.asList(BOJ, BOP, EPX, ERR, ERS, FEU, HEG, SAB, THO);		// FIXME : does not contain RES
		private static List<OfficialHarvestableSpecies>	setEnumCPI_CP	= Arrays.asList(BOJ, ERR, ERS, FEU, HEG, RES, SAB);
		private static List<OfficialHarvestableSpecies>	setEnumCPI_RL	= Arrays.asList(AUT, BOJ, ERR, ERS, HEG, SAB, TIL);	// FIXME : does not contain RES or FEU
		private static List<OfficialHarvestableSpecies>	setEnumCRS	= Arrays.asList(AUT, ERS, HEG);	// FIXME : does not contain RES or FEU
		private static List<OfficialHarvestableSpecies>	setEnumCJP	= Arrays.asList(BOJ, BOP, EPX, ERR, ERS, FEU, HEG, PEU, RES, SAB, THO);
		private static List<OfficialHarvestableSpecies>	setEnumCJPG_QM	= Arrays.asList(ERS, FEU, HEG, RES);
		private static List<OfficialHarvestableSpecies>	setEnumCPI_CP_CIMOTF = Arrays.asList(BOJ, BOP, EPX, ERR, ERS, FEU, HEG, PEU, RES, SAB, THO);
		private static List<OfficialHarvestableSpecies>	setEnumCPI_RL_CIMOTF = Arrays.asList(BOJ, BOP, EPX, ERR, ERS, FEU, HEG, PEU, RES, SAB, THO);
		private OfficialHarvestableSpecies() {		
		}
		
		public Matrix getDummy(TreatmentType treatment) {
			List<OfficialHarvestableSpecies> setEnum = getListRelatedToTreatment(treatment);
			int pos = setEnum.indexOf(this);
			if (pos == -1) {
				pos = setEnum.indexOf(AUT);		// correct for setEnumCPI_RL and setEnumCRS
			}
			if (pos == -1) {
				pos = setEnum.indexOf(FEU);		// correct for setEnum45
			}
//			for (OfficialHarvestableSpecies species : setEnum) {
//				if (species == this) {
//					break;
//				}
//				pos++;
//			}
			Matrix dummyTmp = new Matrix(1, setEnum.size());
			dummyTmp.setValueAt(0, pos, 1d);
			return dummyTmp;
		}

		private static HashMap<String, Set<String>>	eligibleSpeciesNames	= new HashMap<String, Set<String>>();		
		
		/**
		 * This method returns the OfficialHarvestableSpecies Enum associated
		 * with the string.
		 * 
		 * @param speciesName the species name
		 * @param pTreatment the treatment type for the harvester
		 * @return the appropriate enum or null if the string does not match any species
		 */
		public static OfficialHarvestableSpecies findEligibleSpecies(String speciesName, TreatmentType pTreatment) {

			if (eligibleSpeciesNames.get(pTreatment.toString()) == null) {
				Set<String> eligibleSpeciesNamesSet = new HashSet<String>();
				eligibleSpeciesNames.put(pTreatment.toString(), eligibleSpeciesNamesSet);
				for (OfficialHarvestableSpecies species : getListRelatedToTreatment(pTreatment)) {
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
		
		private static List<OfficialHarvestableSpecies> getListRelatedToTreatment(TreatmentType treatment) {
			List<OfficialHarvestableSpecies> set = null;
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
			} else if(TreatmentType.CJP == treatment){
				set = setEnumCJP;
			} else if(TreatmentType.CJPG_QM == treatment){
				set = setEnumCJPG_QM;
			} else if(TreatmentType.CPI_CP_CIMOTF == treatment){
				set = setEnumCPI_CP_CIMOTF;
			} else if(TreatmentType.CPI_RL_CIMOTF == treatment){
				set = setEnumCPI_RL_CIMOTF;
			} else {
				set = setEnumOther;
			}
			return set;
		}
	}
	
	
	public OfficialHarvestableSpecies getOfficialHarvestableTreeSpecies(TreatmentType treatment);	// TODO : check if the treatment is strictly required. This should be handled internally
}
