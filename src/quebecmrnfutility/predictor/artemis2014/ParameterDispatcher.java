/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in Right of Canada
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
package quebecmrnfutility.predictor.artemis2014;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import quebecmrnfutility.simulation.covariateproviders.treelevel.QcTreeQualityProvider.QcTreeQuality;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider.DrainageGroup;
import repicea.util.Index;
import repicea.util.ObjectUtility;

public class ParameterDispatcher {
	
	private static final Set<String> SPECIES_FOR_TBE = new HashSet<String>();
	static {
		SPECIES_FOR_TBE.add("SAB");
		SPECIES_FOR_TBE.add("EPB");
		SPECIES_FOR_TBE.add("EPN");
		SPECIES_FOR_TBE.add("EPR");
		SPECIES_FOR_TBE.add("EPO");
		SPECIES_FOR_TBE.add("EPX");
	}

	private static ParameterDispatcher instance;
	
	private final ParameterMap beta;
	private final ParameterMap omega;
	private final ParameterMap covparms;
	private final ParameterMap effectID;
	private final Index<Integer, String> vegpotIndex;
	private final Index<Integer, String> speciesIndex;
	private final Index<Integer, String> speciesGroupIndex;
	private final Index<Integer, String> moduleIndex;
	private final Map<String, Map<String, String>> speciesMatches;
	private final Map<String, Map<String,Matrix>> dummySpeciesGroup;
	private final Map<String, List<String>> speciesGroupByVegPot;
	
	private ParameterDispatcher() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MatchModuleParameters.csv";
			String omegaFilename = path + "0_MatchModuleOmega.csv";
			String covparmsFilename = path + "0_MatchModuleCovparms.csv";
			String effectIDFilename = path + "0_MatchModuleEffects.csv";

			beta = ParameterLoader.loadVectorFromFile(2, betaFilename);
			omega = ParameterLoader.loadVectorFromFile(2, omegaFilename);
			covparms = ParameterLoader.loadVectorFromFile(2, covparmsFilename);
			effectID = ParameterLoader.loadVectorFromFile(2, effectIDFilename);

			String vegpotIndexFilename = path + "0_Vegpot.csv";
			String speciesIndexFilename = path + "0_Species.csv";
			String speciesGroupIndexFilename = path + "0_SpeciesGroups.csv";
			String moduleIndexFilename = path + "0_Modules.txt";

			vegpotIndex = getIndex(vegpotIndexFilename);
			speciesIndex = getIndex(speciesIndexFilename);
			speciesGroupIndex = getIndex(speciesGroupIndexFilename);
			moduleIndex = getIndex(moduleIndexFilename);

			String matchSpeciesGroupFilename = path + "0_MatchSpeciesGroups.csv";
			ParameterMap pm = ParameterLoader.loadVectorFromFile(2, matchSpeciesGroupFilename);

			speciesMatches = new HashMap<String, Map<String, String>>();
			dummySpeciesGroup = new HashMap<String, Map<String, Matrix>>();
			speciesGroupByVegPot = new HashMap<String, List<String>>();
			
			List<Integer> speciesGroupUnique = new ArrayList<Integer>();
			for (String vegpotName : vegpotIndex.values()) {
				speciesGroupUnique.clear();
				int vegpotID = vegpotIndex.getKeyForThisValue(vegpotName);
				Map<String, String> innerMap = new HashMap<String, String>();
				speciesMatches.put(vegpotName, innerMap);
				for (Integer speciesID : speciesIndex.keySet()) {
					String speciesName = speciesIndex.get(speciesID);
					if (pm.get(vegpotID, speciesID) != null) {
						int speciesGroupID = (int) pm.get(vegpotID, speciesID).getValueAt(0, 0);
						String speciesGroupName = speciesGroupIndex.get(speciesGroupID);
						innerMap.put(speciesName, speciesGroupName);
						if (!speciesGroupUnique.contains(speciesGroupID)) {
							speciesGroupUnique.add(speciesGroupID);
						}
					}
				}

				Map<String, Matrix> innerDummyMap = new HashMap<String, Matrix>();
				dummySpeciesGroup.put(vegpotName, innerDummyMap);
				Collections.sort(speciesGroupUnique);
				
				List<String> speciesGroups = new ArrayList<String>();
				speciesGroupByVegPot.put(vegpotName, speciesGroups);

				for (Integer speciesGroupID : speciesGroupUnique) {
					Matrix dummy = new Matrix(1, speciesGroupUnique.size());
					innerDummyMap.put(speciesGroupIndex.get(speciesGroupID), dummy);
					dummy.setValueAt(0, speciesGroupUnique.indexOf(speciesGroupID), 1d);
					speciesGroups.add(speciesGroupIndex.get(speciesGroupID));
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidParameterException("Unable to load the parameters in the ParameterDispatcher singleton");
		}
	}
	
	public static ParameterDispatcher getInstance() {
		if (instance == null) {
			instance = new ParameterDispatcher();
		}
		return instance;
	}
	
	private Index<Integer, String> getIndex(String filename) throws IOException {
		Index<Integer, String> index = new Index<Integer, String>();
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		int i;
		String value;
		while ((record = reader.nextRecord()) != null) {
			i = Integer.parseInt(record[0].toString());
			value = record[1].toString();
			index.put(i, value);
		}
		reader.close();
		return index;
	}
	
	
	public Index<Integer, String> getVegpotIndex() {return vegpotIndex;}
	
	public Index<Integer, String> getModuleIndex() {return moduleIndex;} 
	
	public ParameterMap getParameters() {return beta;}
	
	public ParameterMap getCovarianceParameters() {return covparms;}
	
	public ParameterMap getCovarianceOfParameterEstimates() {return omega;}
	
	public ParameterMap getEffectID() {return effectID;}
	
	public String getSpeciesGroupName(Artemis2014CompatibleStand stand, String speciesName) {
		String initGroupingSpecies = getInitPrioriSpeciesGrouping(speciesName);
		return speciesMatches.get(stand.getPotentialVegetation()).get(initGroupingSpecies);
	}

	
	/**
	 * Get the initial priori grouping for oak, pine, spruce and poplar species
	 * @param iniSpeciesName the original species code (e.g. CHR, PET)
	 * @return the initial priori grouping for oak, pine, spruce and poplar species
	 */
	protected String getInitPrioriSpeciesGrouping(String iniSpeciesName){
		String result = iniSpeciesName;
		if(iniSpeciesName != null){		
			if (iniSpeciesName.startsWith("CH")) {
				result ="CHX";
			} else if (iniSpeciesName.startsWith("PE")) {
				result="PEU";
			} else if (iniSpeciesName.startsWith("EP")) {
				result="EPX";
			} else if (iniSpeciesName.equals("PIB") || iniSpeciesName.equals("PIR")) {
				result="PIN";
			}
		}
		return result;
	}

	/**
	 * This method returns true if this species is recognised by Artemis modules.
	 * @param speciesName a String (3-character code)
	 * @return a boolean
	 */
	public boolean isRecognizedSpecies(String speciesName) {
		return speciesIndex.containsValue(speciesName.trim().toUpperCase());
	}

	/**
	 * This method returns the species groups for a particular stand.
	 * @param stand an Artemis2009CompatibleStand instance
	 * @return a Set of Strings
	 */
	public List<String> getSpeciesGroups(Artemis2014CompatibleStand stand) {
		return speciesGroupByVegPot.get(stand.getPotentialVegetation());
	}
	
	protected double getProduct(Matrix oXVector, Matrix beta) {
		double product = 0;
		for (int i = 0; i < beta.m_iRows; i++) {
			product += oXVector.getValueAt(0, i) * beta.getValueAt(i, 0);
		}
		return product;
	}
	
	protected static enum ModuleID {
		MORTALITY, DBH_GROWTH, RECRUITMENT_PRESENCE, RECRUITMENT_NUMBER, RECRUITMENT_DBH;

		public int getId () {
			return this.ordinal () + 1;
		}
	}
	
	static final List<String> LIST_ECOREGION = new ArrayList<String>();
	static {
		LIST_ECOREGION.add("1a");
		LIST_ECOREGION.add("2b");
		LIST_ECOREGION.add("2c");
		LIST_ECOREGION.add("3d");
		LIST_ECOREGION.add("4f");
		LIST_ECOREGION.add("4h");
		LIST_ECOREGION.add("4g");
		LIST_ECOREGION.add("5i");
		LIST_ECOREGION.add("5h");
	}

	
	
	
	private boolean isSpruceBudwormHostSpecies(Artemis2014CompatibleTree t) {
		return SPECIES_FOR_TBE.contains(t.getSpeciesGroupName());
	}
	
	protected void constructXVector (Matrix oXVector, Artemis2014CompatibleStand stand, Artemis2014CompatibleTree t, String moduleName, List<Integer> oEffectsVector) {
//			final Matrix oXVector, final ArtStand stand, final ArtTree t,
//			final ModuleID moduleID, final List<Integer> oEffectsVector) {

		oXVector.resetMatrix();
		
		int TBE = 0;
		if (stand.isGoingToBeDefoliated()) {
			TBE = 1;
		}

		int pointer = 0;
//		int dummyTBE = 0;
//		Matrix dummyEssence = null;
		Matrix dummyEssence = dummySpeciesGroup.get(stand.getPotentialVegetation()).get(t.getSpeciesGroupName());
		int dummySAB = t.getSpeciesGroupName() == "SAB" ? 1 : 0; 
		int dummyHEG = t.getSpeciesGroupName() == "HEG" ? 1 : 0; 
		int moduleID = moduleIndex.getKeyForThisValue(moduleName);
		oXVector.setValueAt(0, 0, 1d);
		pointer = 1;
		
//		switch (moduleID) {
//		case MORTALITY:
//		case RECRUITMENT_PRESENCE:
//		case RECRUITMENT_NUMBER:
//		case RECRUITMENT_DBH:
////			dummyEssence = getSettings ().getSpeciesGroupDummyMap ().get (stand.getPotentialVegetationID ())
////					.get (t.getSpecies ().getValue ());
////			oXVector.setValueAt(0, 0, 1d);
////			pointer = 1;
//			dummyTBE = t.getDummyTBE (TBE);
//			break;
//		case DBH_GROWTH:
////			dummyEssence = getSettings ().getSpeciesGroupDummyMap ().get (stand.getPotentialVegetationID ())
////					.get (t.getSpecies ().getValue ());
////			oXVector.setValueAt(0, 0, 1d);
////			pointer = 1;
//			dummyTBE = TBE;
//			break;
//		}

		int dummyTBE = 0; //default value
		if (moduleID == ModuleID.DBH_GROWTH.getId()) {
			dummyTBE = TBE;
		} else if (TBE == 1) {
			dummyTBE = isSpruceBudwormHostSpecies(t) ? 1 : 0;
		}

		double fTmp = 0.0;
		int iCase;

		for (final Iterator<Integer> it = oEffectsVector.iterator (); it.hasNext ();) {
			iCase = it.next ();
			switch (iCase) {
			case 1: // 0 occurence
				fTmp = stand.getElevationM ();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 2: // 1006 occurences
//				double coupe1 = 0.0;
//				coupe1 = getCoupe(stand);
//				
				if (stand.wasHarvestedInPreviousStep()) {
					oXVector.setValueAt(0, pointer, 1d);
				}
				pointer++;				
				break;
			case 3: // 22 091 occurences
				oXVector.setValueAt(0, pointer, 0.0);

				if (stand.isInterventionResult ()) {
					if (!stand.isInitialStand() || moduleID != ModuleID.DBH_GROWTH.getId()) {
						oXVector.setValueAt(0, pointer, 1.0);
					}
				}

				pointer++;
				break;
			case 4: // 995 occurences
				int currentCut = 0;
				if (stand.isInterventionResult ()) {
					if (!stand.isInitialStand() || moduleID != ModuleID.DBH_GROWTH.getId()) {
						currentCut = 1;
					}
				}
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * currentCut);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 10: // 5340 occurences
				oXVector.setValueAt(0, pointer, t.getDbhCm());
				pointer++;
				break;
			case 11: // 14 213 occurences
				oXVector.setValueAt(0, pointer, t.getSquaredDbhCm());
				pointer++;
				break;
			case 12: // 27 854 occurences
				fTmp = t.getDbhCm();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 13: // 1181 occurences
				oXVector.setValueAt(0, pointer, dummySAB * stand.getTotalAnnualPrecipitationMm());
				pointer++;
				break;
			case 14: // 36 918 occurences
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii));
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 17: // 6016 occurences
				oXVector.setValueAt(0, pointer, t.getLnDbhCm ());
				pointer++;
				break;
			case 18: // 5805 occurences
				fTmp = t.getLnDbhCm ();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 31: // 38 799 occurences
				oXVector.setValueAt(0, pointer, Math.log(stand.getGrowthStepLengthYr()));
				pointer++;
				break;
			case 32: // 5600 occurences
				oXVector.setValueAt(0, pointer, Math.log(stand.getGrowthStepLengthYr()) * dummyTBE);
				pointer++;
				break;
			case 33: // 6420 occurences
				fTmp = (Math.log(stand.getGrowthStepLengthYr()) * dummyTBE);
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 34: // 7436 occurences
				double logdt_cc = 0;
				if (stand.getDateYr () <= 1994) {
					logdt_cc = Math.log (1995 - stand.getDateYr()); 
				}
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * logdt_cc);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 35: // 450 occurences
				oXVector.setValueAt(0, pointer, stand.getMeanQuadraticDiameterCm ());
				pointer++;
				break;
			case 36: // 2815 occurences
				oXVector.setValueAt(0, pointer, stand.getNumberOfStemsHa() * stand.getAreaHa());
				pointer++;
				break;
			case 37: // 70 occurences
				oXVector.setValueAt(0, pointer, dummyEssence.multiply (stand.getNumberOfStemsBySpeciesGroup ()
						.transpose ()).getValueAt(0, 0));
				pointer++;
				break;
			case 38: // 2150 occurences
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii)
							* stand.getNumberOfStemsBySpeciesGroup ().getValueAt(0, ii));
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 39: // 0 occurence
				oXVector.setValueAt(0, pointer, (dummyEssence.multiply (stand.getNumberOfStemsBySpeciesGroup ()
						.transpose ())).getValueAt(0, 0) * stand.getBasalAreaM2Ha ());
				pointer++;
				break;
			case 40:
				oXVector.setValueAt(0, pointer, stand.getSeasonalPrecipitationMm());
				pointer++;
				break;
			case 41: // 10 469 occurences
				oXVector.setValueAt(0, pointer, stand.getTotalAnnualPrecipitationMm());
				pointer++;
				break;
			case 42: // 2366 occurences
				fTmp = (stand.getTotalAnnualPrecipitationMm());
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 43: // 4889 occurences
				oXVector.setValueAt(0, pointer, t.getBasalAreaLargerThanSubjectM2Ha ());
				pointer++;
				break;
			case 44: // 10 557 occurences
				fTmp = t.getBasalAreaLargerThanSubjectM2Ha ();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 45: // 17 833 occurences
				oXVector.setValueAt(0, pointer, stand.getBasalAreaM2Ha ());
				pointer++;
				break;
			case 47: // 5260 occurences
				fTmp = stand.getBasalAreaM2Ha ();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 48: // 340 occurences
				oXVector.setValueAt(0, pointer, (dummyEssence.multiply (stand.getNumberOfStemsBySpeciesGroup ()
						.transpose ())).getValueAt(0, 0) * stand.getBasalAreaM2Ha ());
				pointer++;
				break;
			case 49: // 12 283 occurences
				oXVector.setValueAt(0, pointer, dummyTBE);
				pointer++;
				break;
			case 50: // 1650 occurences
				oXVector.setValueAt(0, pointer, stand.getMeanAnnualTemperatureCelsius());
				pointer++;
				break;
			case 51: // 3700 occurences
				fTmp = (stand.getMeanAnnualTemperatureCelsius());
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 52: // 0 occurence
				oXVector.setValueAt(0, pointer, stand.getLatitudeDeg());
				pointer++;
				break;
			case 53: // 0 occurence
				fTmp = (stand.getLatitudeDeg());
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 54: // 60 occurences
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii)
							* stand.getBasalAreaBySpeciesGroup ().getValueAt(0, ii));
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 57:
				oXVector.setValueAt(0, pointer, stand.getElevationM ());
				pointer++;
				break;

			case 59:
				fTmp = t.getSquaredDbhCm();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 60:
				oXVector.setValueAt(0, pointer, stand.getVaporPressureDeficit());
				pointer++;
				break;
			case 61:
				fTmp = stand.getVaporPressureDeficit();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 62:
//				final DrainageGroup drainageGroup = QuebecGeneralSettings.DRAINAGE_CLASS_LIST.get (stand.getDrainageClass ());
				final DrainageGroup drainageGroup = stand.getDrainageGroup();
				for (int ii = 0; ii < drainageGroup.getDrainageDummy ().m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, drainageGroup.getDrainageDummy ().getValueAt(0, ii) * 1);
				}
				pointer += drainageGroup.getDrainageDummy ().m_iCols;
				break;
			case 63:
				oXVector.setValueAt(0, pointer, Math.log (stand.getMeanQuadraticDiameterCm ()+1));
				pointer++;
				break;
			case 64:
				oXVector.setValueAt(0, pointer, Math.log (stand.getNumberOfStemsHa ()+1));
				pointer++;
				break;
			case 65:
				fTmp = Math.log (stand.getNumberOfStemsHa ()+1);
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 66:
				oXVector.setValueAt(0, pointer, Math.log (stand.getBasalAreaM2Ha ()+1));
				pointer++;
				break;
			case 67:
				fTmp = Math.log (stand.getBasalAreaM2Ha ()+1);
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 68:
				fTmp = stand.getNumberOfStemsHa() * stand.getAreaHa() ;
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 69:
				oXVector.setValueAt(0, pointer, stand.getNumberOfStemsHa ());
				pointer++;
				break;
			case 70:
				fTmp = stand.getNumberOfStemsHa ();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 71:
				oXVector.setValueAt(0, pointer, dummyEssence.multiply (stand.getNumberOfStemsBySpeciesGroup().scalarMultiply(1 / stand.getAreaHa())
						.transpose ()).getValueAt(0, 0));
				pointer++;
				break;
			case 72:
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii)	* stand.getNumberOfStemsBySpeciesGroup().scalarMultiply(1 / stand.getAreaHa()).getValueAt(0, ii));
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 73:
				fTmp = dummyEssence.multiply (stand.getNumberOfStemsBySpeciesGroup().scalarMultiply(1d / stand.getAreaHa()).transpose ()).getValueAt(0, 0);
				fTmp = (fTmp / stand.getNumberOfStemsHa ()) * 100;
				oXVector.setValueAt(0, pointer, fTmp);
				pointer++;
				break;
			case 74:
				fTmp = stand.getSeasonalPrecipitationMm();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 75:
				oXVector.setValueAt(0, pointer, stand.getUtilPrecipitation());
				pointer++;
				break;
			case 76:
				fTmp = stand.getUtilPrecipitation();
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 77:
				final QualityClass qualityClass = getQualityClass(t);
				for (int ii = 0; ii < qualityClass.getQualityDummy().m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, qualityClass.getQualityDummy ().getValueAt(0, ii) * 1);
				}
				pointer += qualityClass.getQualityDummy ().m_iCols;
				break;
			case 78:
				oXVector.setValueAt(0, pointer, dummyEssence.multiply(stand.getBasalAreaBySpeciesGroup().scalarMultiply(1d / stand.getAreaHa()).transpose ()).getValueAt(0, 0));
				pointer++;
				break;
			case 79:
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii)	* stand.getBasalAreaBySpeciesGroup().scalarMultiply(1d / stand.getAreaHa()).getValueAt(0, ii));
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 80:
				fTmp = dummyEssence.multiply (stand.getBasalAreaBySpeciesGroup().scalarMultiply(1d / stand.getAreaHa()).transpose ()).getValueAt(0, 0);
				fTmp = (fTmp / stand.getBasalAreaM2Ha ()) * 100;
				oXVector.setValueAt(0, pointer, fTmp);
				pointer++;
				break;
			case 81:
				oXVector.setValueAt(0, pointer, stand.getBasalAreaM2Ha () * stand.getBasalAreaM2Ha ());
				pointer++;
				break;
			case 82:
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * dummyTBE);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 83:
				if (stand.wasSpruceBudwormDefoliatedInPreviousStep()) {
					double tbe = moduleID == ModuleID.DBH_GROWTH.getId() ?
							1d :
								isSpruceBudwormHostSpecies(t) ? 1d : 0d;
					oXVector.setValueAt(0, pointer, tbe);
				}
				pointer++;
				break;
			case 84:
				if (stand.wasSpruceBudwormDefoliatedTwoStepsAgo()) {
					double tbe = moduleID == ModuleID.DBH_GROWTH.getId() ?
							1d :
								isSpruceBudwormHostSpecies(t) ? 1d : 0d;
					oXVector.setValueAt(0, pointer, tbe);
				}
				pointer++;
				break;
			case 85:
				fTmp = 0.0d;
//				int index = stand.getIndexEssGroup ().indexOf (t.getSpecies ().getValue ());
				double nT = dummyEssence.multiply(stand.getNumberOfStemsBySpeciesGroup().transpose()).getValueAt(0, 0);
				if (nT <= 0) {
					fTmp = 1;
				}
				oXVector.setValueAt(0, pointer, fTmp);
				pointer++;
				break;
			case 86:
				fTmp = 0.0d;
//				index = stand.getIndexEssGroup().indexOf (t.getSpecies ().getValue ());
				nT = dummyEssence.multiply(stand.getNumberOfStemsBySpeciesGroup().transpose()).getValueAt(0, 0);
				if (nT <= 0) {
					fTmp = 1;
				}
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * fTmp);
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 87://dhpcm*dhpcm*dummy_sab				
				oXVector.setValueAt(0, pointer, dummySAB * t.getSquaredDbhCm());
				pointer++;
				break;
			case 88://dhpcm*dummy_sab			
				oXVector.setValueAt(0, pointer, dummySAB * t.getSquaredDbhCm());
				pointer++;
				break;
			case 89://dummy_sab*coupe0			
				currentCut = 0;
				if (stand.isInterventionResult ()) {
					//if (!stand.isInitialScene () || moduleID != ModuleID.DBH_GROWTH) {
						currentCut = 1;
					//}
				}
				oXVector.setValueAt(0, pointer, dummySAB * currentCut);
				pointer++;
				break;
			case 90://mq_dhpcm*ess_groupe
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * stand.getMeanQuadraticDiameterCm());
				}
				pointer += dummyEssence.m_iCols;
				break;
			case 91://st_ha_cumul_gt*dummy_sab	
				oXVector.setValueAt(0, pointer, dummySAB * t.getBasalAreaLargerThanSubjectM2Ha());
				pointer++;
				break;

			
		case 92://coupe0*dummy_heg
			currentCut = 0;
			if (stand.isInterventionResult ()) {
				//if (!stand.isInitialScene () || moduleID != ModuleID.DBH_GROWTH) {
					currentCut = 1;
				//}
			}
			oXVector.setValueAt(0, pointer, dummyHEG * currentCut);
			pointer++;
			break;	
		case 93://coupe*dummy_heg
//			double cut = getCoupe(stand);
//			oXVector.setValueAt(0, pointer, t.getDummyHEG () * cut);
			if (stand.wasHarvestedInPreviousStep()) {
				oXVector.setValueAt(0, pointer, dummyHEG);
			}
			pointer++;
			break;
		case 94://coupe*dummy_sab
//			cut = getCoupe(stand);
//			oXVector.setValueAt(0, pointer, t.getDummySAB () * cut);
			if (stand.wasHarvestedInPreviousStep()) {
				oXVector.setValueAt(0, pointer, dummySAB);
			}
			pointer++;
			break;
		case 95://regionouest
			if (!LIST_ECOREGION.contains(stand.getEcoRegion())) {
				oXVector.setValueAt(0, pointer, 1d);
			} 								
			pointer++;	
			break;
		case 96://anc
			int year = 1998;
			if (year > stand.getDateYr()) {
				oXVector.setValueAt(0, pointer, 1d);
			} else {
				oXVector.setValueAt(0, pointer, 0d);
			}
			pointer++;
			break;
		case 97://anc*ess_groupe
			year = 1998;
			double anc_EssG = 0.0d;
			if (year > stand.getDateYr()) {
				anc_EssG = 1.0d;
			} 
			
			for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
				oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * anc_EssG);
			}
			pointer += dummyEssence.m_iCols;
			break;												
		case 98://coupe*ess_Groupe
//			cut = getCoupe(stand);
//			for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
//				oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii) * cut);
//			}
			if (stand.wasHarvestedInPreviousStep()) {
				for (int ii = 0; ii < dummyEssence.m_iCols; ii++) {
					oXVector.setValueAt(0, ii + pointer, dummyEssence.getValueAt(0, ii));
				}
			}
			pointer += dummyEssence.m_iCols;
			break;	
			
			case 99: // coupe1
				if (stand.wasHarvestedTwoStepsAgo()) {
					oXVector.setValueAt(0, pointer, 1d);
				}
				pointer++;
				break;
			}

		}
	}

	private static enum QualityClass {
		D, 
		N,
		Q;

		private Matrix dummy;

		QualityClass() {
			dummy = new Matrix(1,3);
			dummy.setValueAt(0, ordinal(), 1d);
		}

		private Matrix getQualityDummy() {return dummy;}
	}

	
	private QualityClass getQualityClass(Artemis2014CompatibleTree t) {
		QualityClass qc = QualityClass.N;
		if (t.getTreeQuality() != null) {
			if (t.getTreeQuality() == QcTreeQuality.D) {
				qc = QualityClass.D;
			} else if ((t.getTreeQuality() == QcTreeQuality.C)
					|| (t.getTreeQuality() == QcTreeQuality.B)
					|| (t.getTreeQuality() == QcTreeQuality.A)) {
				qc = QualityClass.Q;
			}
		}
		return qc;
	}


	/**
	 * This method returns either the basal area or the number of stems contained in the
	 * collection trees. IMPORTANT: Values are not reported per hectare.
	 * @param stand a Artemis2009CompatibleStand instance
	 * @param trees a Collection of Artemis2009CompatibleTree instances
	 * @param G a boolean (true to calculate the basal area or false for the number of stems)
	 * @return a Matrix instance
	 */
	public static Matrix getGroupEssGorN(Artemis2014CompatibleStand stand, Collection<? extends Artemis2014CompatibleTree> trees, boolean G) {
		List<String> speciesGroups = ParameterDispatcher.getInstance().getSpeciesGroups(stand);
		Matrix oVector = new Matrix(1, speciesGroups.size());
 
		if ((trees == null)||(trees.isEmpty ())) {
			return oVector;
		} else {
			for (Artemis2014CompatibleTree t : trees) {
				if (t.getNumber() > 0) {
					int p = speciesGroups.indexOf(t.getSpeciesGroupName());
					double newValue = oVector.getValueAt(0, p);
					if (G) {
						newValue += t.getStemBasalAreaM2() * t.getNumber();
					}
					else {
						newValue += t.getNumber();
					}
					oVector.setValueAt(0, p, newValue);
				}
			}
			return oVector;
		}
	}

	
//	/**
//	 * For testing
//	 * @param args
//	 * @throws IOException
//	 */
//	public static void main(String[] args) throws IOException {
//		ParameterDispatcher.getInstance();
//	}
	
	
	
}
