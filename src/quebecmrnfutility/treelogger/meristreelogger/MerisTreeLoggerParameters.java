/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025-26 His Majesty the King in right of Canada
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
package quebecmrnfutility.treelogger.meristreelogger;

import java.awt.Container;
import java.awt.Window;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import quebecmrnfutility.GeneralSettings;
import repicea.io.javacsv.CSVHeader;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.SpeciesProvider;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.species.REpiceaSpeciesCompliantObject;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;

/**
 * A class that implements the MERIS matrix, a bucking matrix widely used by Quebec's
 * Ministry of Natural Resources and Forests.
 * @author Mathieu Fortin - November 2025
 */
@SuppressWarnings("serial")
public class MerisTreeLoggerParameters extends TreeLoggerParameters<MerisTreeLogCategory> implements REpiceaSpeciesCompliantObject {

	private static final List<String> ReservedFieldNames = Arrays.asList(new String[] {"ESSENCE","DHP","GROUPE"});
//	private static String DefaultCode = "DEFAUT";

	static final List<String> SpeciesList = Collections.unmodifiableList(Arrays.asList(
						"BOG", "BOJ", "BOP", "CAC", "CAF", 
						"CET", "CHB", "CHE", "CHG", "CHR", 
						"EPB", "EPN", "EPO", "EPR", "ERA", 
						"ERN", "ERR", "ERS", "FRA", "FRN", 
						"FRP", "HEG", "MEH", "MEJ", "MEL", 
						"MEU", "NOC", "ORA", "ORR", "ORT", 
						"OSV", "PEB", "PED", "PEG", "PEH", 
						"PET", "PIB", "PID", "PIG", "PIR", 
						"PIS", "PRU", "SAB", "THO", "TIL"));

	
	private static Map<Species, String> SpeciesToSpeciesCodeMap = new HashMap<Species, String>();
	static List<Species> LatinSpeciesList;
	static {
		List<Species> tmpList = new ArrayList<Species>();
		for (String s : SpeciesList) {
			if (!GeneralSettings.SPECIES_LOOKUP_MAP.containsKey(s)) {
				throw new UnsupportedOperationException("Species " + s + " is not found the GeneralSettings.SPECIES_LOOKUP_MAP");
			} else {
				Species speciesEnum = GeneralSettings.SPECIES_LOOKUP_MAP.get(s);
				tmpList.add(speciesEnum);
				SpeciesToSpeciesCodeMap.put(speciesEnum, s);
			}
		}
		LatinSpeciesList = Collections.unmodifiableList(tmpList);
	}
	
//	private static final Map<Species, String> SpeciesLookupMap = new HashMap<Species, String>(); 
//	static {
//		SpeciesLookupMap.put(Species.Betula_populifolia, "BOG");
//		SpeciesLookupMap.put(Species.Betula_alleghaniensis, "BOJ");
//		SpeciesLookupMap.put(Species.Betula_alleghaniensis, "BOP");
//		SpeciesLookupMap.put(Species.Carya_cordiformis, "CAC");
//		SpeciesLookupMap.put(Species.Carya_ovata, "CAF"); 
//		SpeciesLookupMap.put(Species.Prunus_serotina, "CET");
//		SpeciesLookupMap.put(Species.Quercus_alba, "CHB");
//		SpeciesLookupMap.put(Species.Quercus_bicolor, "CHE");
//		SpeciesLookupMap.put(Species.Quercus_macrocarpa, "CHG");
//		SpeciesLookupMap.put(Species.Quercus_rubra, "CHR");
//		SpeciesLookupMap.put(Species.Picea_glauca, "EPB");
//		SpeciesLookupMap.put(Species.Picea_mariana, "EPN");
//		SpeciesLookupMap.put(Species.Picea_abies, "EPO");
//		SpeciesLookupMap.put(Species.Picea_rubens, "EPR");
//		SpeciesLookupMap.put(Species.Acer_saccharinum, "ERA"); 
//		SpeciesLookupMap.put(Species.Acer_nigrum, "ERN");
//		SpeciesLookupMap.put(Species.Acer_rubrum, "ERR");
//		SpeciesLookupMap.put(Species.Acer_saccharum, "ERS");
//		SpeciesLookupMap.put(Species.Fraxinus_americana, "FRA");
//		SpeciesLookupMap.put(Species.Fraxinus_nigra, "FRN");
//		SpeciesLookupMap.put(Species.Fr
//		"FRP", "HEG", "MEH", "MEJ", "MEL", 
//		"MEU", "NOC", "ORA", "ORR", "ORT", 
//		"OSV", "PEB", "PED", "PEG", "PEH", 
//		"PET", "PIB", "PID", "PIG", "PIR", 
//		"PIS", "PRU", "SAB", "THO", "TIL"
//	}
//	private static final Map<String, String> OtherSpeciesLookupMap = new HashMap<String, String>();
//	static {
//		OtherSpeciesLookupMap.put("CHX", "CHR");
//		OtherSpeciesLookupMap.put("EPX", "EPN");
//		OtherSpeciesLookupMap.put("PEU", "PET");
//		OtherSpeciesLookupMap.put("AUT", "CAC");
//		OtherSpeciesLookupMap.put("F0R", "CAC");
//		OtherSpeciesLookupMap.put("FEU", "CAC");
//		OtherSpeciesLookupMap.put("F_0", "CAC");
//		OtherSpeciesLookupMap.put("F_1", "PET"); // intolerant hardwood
//		OtherSpeciesLookupMap.put("PIN", "PIB");
//		OtherSpeciesLookupMap.put("RES", "PRU");
//	}
	
	private transient MerisTreeLoggerParametersDialog guiInterface;
	
	class MerisTypeMatrix {
		
		private final List<String> logCategoryNames;
		/**
		 * First key: species
		 * Second key: diameter class
		 */
		private final LinkedHashMap<String,TreeMap<Integer, RowEntry>> splittingMatrix;
		private final TreeMap<String, List<MerisTreeLogCategory>> logCategoriesMap;
		
		MerisTypeMatrix() {
			logCategoryNames = new ArrayList<String>();
			splittingMatrix = new LinkedHashMap<String, TreeMap<Integer, RowEntry>>();
			logCategoriesMap = new TreeMap<String, List<MerisTreeLogCategory>>();
		}
		
		void replaceBy(MerisTypeMatrix otherMatrix) {
			logCategoryNames.clear();
			logCategoryNames.addAll(otherMatrix.logCategoryNames);
			splittingMatrix.clear();
			splittingMatrix.putAll(otherMatrix.splittingMatrix);
			logCategoriesMap.clear();
			logCategoriesMap.putAll(otherMatrix.logCategoriesMap);
			
			MerisTreeLoggerParameters.this.getLogCategories().clear();
			for (String speciesGroup : logCategoriesMap.keySet()) {
				MerisTreeLoggerParameters.this.getLogCategories().put(speciesGroup, logCategoriesMap.get(speciesGroup));
			}
		}
		
		List<String> getMissingSpeciesCodes() {
			List<String> missingSpeciesCodes = new ArrayList<String>();
			Set<String> keys = splittingMatrix.keySet();
			for (String c : SpeciesList) {
				if (!keys.contains(c)) {
					missingSpeciesCodes.add(c);
				}
			}
			return missingSpeciesCodes;
		}
		
		
		List<MerisWoodPiece> processTree(LoggableTree tree) {
			List<MerisWoodPiece> pieces  = new ArrayList<MerisWoodPiece>();
			
			Species sp = MerisTreeLoggerParameters.this.convertToEligibleSpecies(((SpeciesProvider) tree).getSpecies());
			String speciesCode = SpeciesToSpeciesCodeMap.get(sp);
			if (speciesCode == null) {
				throw new UnsupportedOperationException("This species cannot be matched to a three-character code " + sp.name());
			}
			if (!splittingMatrix.containsKey(speciesCode)) {
				throw new UnsupportedOperationException("This species code is not supported by MerisTreeLogger: " + speciesCode);
			}
			TreeMap<Integer, RowEntry> rowCollections = splittingMatrix.get(speciesCode);
			double dbhCm = ((DbhCmProvider) tree).getDbhCm();
			if (dbhCm < 9.1) {
				return pieces;
			} else {
				int roundedDbh = roundDbh(dbhCm);
				int largestDiameterClass = rowCollections.lastKey();
				int diamClass = roundedDbh > largestDiameterClass ? largestDiameterClass : roundedDbh;
				RowEntry entry = rowCollections.get(diamClass);
				Matrix mat = entry.splitting;
				Matrix result = mat.scalarMultiply(tree.getCommercialVolumeM3());
				List<MerisTreeLogCategory> logCategoriesForThisGroup = logCategoriesMap.get(entry.group);
				for (MerisTreeLogCategory lc : logCategoriesForThisGroup) {
					int index = logCategoryNames.indexOf(lc.getName());
					pieces.add(new MerisWoodPiece(lc, 
							tree, 
							result.getValueAt(0, index)));
				}
			}
			return pieces;
		}
		
		
	}
	
	private static int roundDbh(double dbhCm) {
		return ((Number) (Math.round(dbhCm *.5 - 0.0001) * 2)).intValue();
	}

	private static class RowEntry {
		final String group;
		final Matrix splitting;
		
		RowEntry(String group, Matrix splitting) {
			this.group = group;
			this.splitting = splitting.getDeepClone();
		}
	}
	
	final MerisTypeMatrix currentMatrix;
	final ConcurrentHashMap<Species, Species> surrogateMap;
	
	protected MerisTreeLoggerParameters() {
		super(MerisTreeLogger.class);
		currentMatrix = new MerisTypeMatrix();
		surrogateMap = new ConcurrentHashMap<Species, Species>();
		setSurrogateMapToDefaultValue();
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null ? guiInterface.isVisible() : false;
	}

	@Override
	protected void initializeDefaultLogCategories() {
		String path = ObjectUtility.getRelativePackagePath(getClass());
//		String filepath = path + "Matrice_DAEF_exemple.csv";
		String filepath = path + "DAEF_extract_MRPP.csv";
		currentMatrix.replaceBy(readFromFile(filepath));
	}

	private static List<String> extractBasicLogCategoryNames(CSVReader reader) {
		List<String> logCategoryNames = new ArrayList<String>();
		CSVHeader header = reader.getHeader();
		for (int i = 0; i < header.getNumberOfFields(); i++) {
			String fieldName = header.getField(i).getName();
			if (!ReservedFieldNames.contains(fieldName)) {
				logCategoryNames.add(fieldName);
			}
		}
		return(logCategoryNames);
	}
	
	private int getIndexOfThisField(CSVReader reader, String fieldName) {
		int index = reader.getHeader().getIndexOfThisField(fieldName);
		if (index == -1) {
			throw new UnsupportedOperationException("The field " + fieldName + " seems to be missing in the file!");
		}
		return index;
	}
	

	MerisTypeMatrix readFromFile(String filename) {
		MerisTypeMatrix importedMatrix = internalImportFromFile(filename);
		return importedMatrix;
	}
	
	private synchronized MerisTypeMatrix internalImportFromFile(String filepath) {
		MerisTypeMatrix merisMatrix = new MerisTypeMatrix();
		CSVReader reader = null;
		try {
			reader = new CSVReader(filepath);
			merisMatrix.logCategoryNames.addAll(extractBasicLogCategoryNames(reader));
			Object[] record;
			int rowId = 1;
			int indexSpeciesCode = getIndexOfThisField(reader, ReservedFieldNames.get(0));
			int indexDiameterClass = getIndexOfThisField(reader, ReservedFieldNames.get(1));
			int indexSpeciesGroup = getIndexOfThisField(reader, ReservedFieldNames.get(2));
			while ((record = reader.nextRecord()) != null) {
				String speciesCode = record[indexSpeciesCode].toString().trim().toUpperCase();
				if (!SpeciesList.contains(speciesCode)) {
					throw new UnsupportedOperationException("The matrix must contain all the following species:" + System.lineSeparator() +
							SpeciesList.toString());
				}
				int diameterClass = Integer.parseInt(record[indexDiameterClass].toString());
				String speciesGroup = record[indexSpeciesGroup].toString().trim().toUpperCase();
				if (!merisMatrix.splittingMatrix.containsKey(speciesCode)) {
					merisMatrix.splittingMatrix.put(speciesCode, new TreeMap<Integer, RowEntry>());
				}
				Species sp = GeneralSettings.SPECIES_LOOKUP_MAP.get(speciesCode);
				Map<Integer, RowEntry> rowCollection = merisMatrix.splittingMatrix.get(speciesCode);
				if (rowCollection.containsKey(diameterClass)) {
					throw new UnsupportedOperationException("The file seems to contain twice the diameter class " + diameterClass + " for species " + speciesCode);
				} 
				
				Matrix values = new Matrix(1, merisMatrix.logCategoryNames.size());
				// check if bark has been identified as a log category
				boolean isBarkOneOfLogCategories = false;
				for (String basicLogCategoryName : merisMatrix.logCategoryNames) {
					if (MerisTreeLogCategory.isBarkInName(basicLogCategoryName)) {
						isBarkOneOfLogCategories = true;
						break;
					}
				}

				for (String basicLogCategoryName : merisMatrix.logCategoryNames) {
					int indexForThisField = reader.getHeader().getIndexOfThisField(basicLogCategoryName);
					if (indexForThisField == -1) {
						throw new UnsupportedOperationException("The field " + basicLogCategoryName + " was listed as a log category, but it cannot be found in the file header.");
					}
					double proportion = Double.parseDouble(record[indexForThisField].toString());
					if (proportion > 0d) {
						MerisTreeLogCategory logCategory = new MerisTreeLogCategory(basicLogCategoryName, speciesGroup, sp.getSpeciesType(), isBarkOneOfLogCategories);
						values.setValueAt(0, 
								merisMatrix.logCategoryNames.indexOf(basicLogCategoryName), 
								Double.parseDouble(record[indexForThisField].toString()));
						if (!merisMatrix.logCategoriesMap.containsKey(speciesGroup)) {
							merisMatrix.logCategoriesMap.put(speciesGroup, new ArrayList<MerisTreeLogCategory>());
						}
						List<MerisTreeLogCategory> logCategories = merisMatrix.logCategoriesMap.get(speciesGroup);
						if (!logCategories.contains(logCategory)) {
							logCategories.add(logCategory);
						}
						logCategories.get(logCategories.indexOf(logCategory)).addSpecies(speciesCode);
					}
				}
				
				double rowSum = values.getSumOfElements();
				if (Math.abs(rowSum - 1) > 1E-4 && Math.abs(rowSum - 100) > 1E-4) {
					throw new UnsupportedOperationException("The sum of the proportion in row " + rowId + " is not equal to 1 or 100!");
				}
				
				values = values.scalarMultiply(1d / rowSum);
				RowEntry entry = new RowEntry(speciesGroup, values);
				rowCollection.put(diameterClass, entry);
				rowId++;
			}
			return merisMatrix;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean isCorrect() {return true;}

	@Override
	public TreeLoggerParametersDialog<?> getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new MerisTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}

	public static void main(String[] args) {
		REpiceaTranslator.setCurrentLanguage(Language.French);
		MerisTreeLoggerParameters o = new MerisTreeLoggerParameters();
		o.initializeDefaultLogCategories();
		o.showUI(null);
		System.exit(0);
	}

	@Override
	public List<Species> getEligibleSpecies() {return LatinSpeciesList;}

	@Override
	public SpeciesLocale getScope() {return SpeciesLocale.Quebec;}

	@Override
	public ConcurrentHashMap<Species, Species> getSurrogateMap() {
		return surrogateMap;
	}

	@Override
	public void setSurrogateMapToDefaultValue() {
		getSurrogateMap().clear();
		getSurrogateMap().put(Species.Other_broadleaved, Species.Betula_papyrifera);
		getSurrogateMap().put(Species.Other_coniferous, Species.Picea_mariana);
	}

}
