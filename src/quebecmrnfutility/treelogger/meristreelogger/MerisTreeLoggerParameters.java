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
import java.util.TreeMap;

import repicea.io.javacsv.CSVHeader;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public class MerisTreeLoggerParameters extends TreeLoggerParameters<MerisTreeLogCategory>{

	private static List<String> ReservedFieldNames = Arrays.asList(new String[] {"ESSENCE","DHP","GROUPE"});
	private static String DefaultCode = "DEFAUT";
	
	private transient MerisTreeLoggerParametersDialog guiInterface;
	
	class MerisTypeMatrix {
		
		private final List<String> logCategoryNames;
		private final LinkedHashMap<String, RowEntry> splittingMatrix;
		private final TreeMap<String, List<MerisTreeLogCategory>> logCategoriesMap;
		
		MerisTypeMatrix() {
			logCategoryNames = new ArrayList<String>();
			splittingMatrix = new LinkedHashMap<String, RowEntry>();
			logCategoriesMap = new TreeMap<String, List<MerisTreeLogCategory>>();
		}
		
		void replaceBy(MerisTypeMatrix otherMatrix) {
			logCategoryNames.clear();
			logCategoryNames.addAll(otherMatrix.logCategoryNames);
			splittingMatrix.clear();
			splittingMatrix.putAll(otherMatrix.splittingMatrix);
			for (RowEntry entry : splittingMatrix.values()) {
				Collections.sort(entry.diameterClasses);
			}
			logCategoriesMap.clear();
			logCategoriesMap.putAll(otherMatrix.logCategoriesMap);
			
			MerisTreeLoggerParameters.this.getLogCategories().clear();
			for (String speciesGroup : logCategoriesMap.keySet()) {
				MerisTreeLoggerParameters.this.getLogCategories().put(speciesGroup, logCategoriesMap.get(speciesGroup));
			}
		}
		
		List<MerisWoodPiece> processTree(MerisLoggableTree tree) {
			List<MerisWoodPiece> pieces  = new ArrayList<MerisWoodPiece>();
			String speciesCode = tree.getSpeciesName().trim().toUpperCase();
			speciesCode = splittingMatrix.containsKey(speciesCode) ? 
					speciesCode : 
						DefaultCode;
			RowEntry entry = splittingMatrix.get(speciesCode);
			if (tree.getDbhCm() < 9.1) {
				return pieces;
			} else {
				int roundedDbh = roundDbh(tree.getDbhCm());
				int largestDiameterClass = entry.diameterClasses.get(entry.diameterClasses.size() - 1);
				int diamClass = roundedDbh > largestDiameterClass ? largestDiameterClass : roundedDbh;
				Matrix mat = entry.splittingAccordingToClass.get(diamClass);
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
		final List<Integer> diameterClasses;
		final Map<Integer, Matrix> splittingAccordingToClass;
		
		RowEntry(String group) {
			this.group = group;
			this.diameterClasses = new ArrayList<Integer>();
			this.splittingAccordingToClass = new HashMap<Integer, Matrix>();
		}
	}
	
	final MerisTypeMatrix currentMatrix;
	
	protected MerisTreeLoggerParameters() {
		super(MerisTreeLogger.class);
		currentMatrix = new MerisTypeMatrix();
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null ? guiInterface.isVisible() : false;
	}

	@Override
	protected void initializeDefaultLogCategories() {
		String path = ObjectUtility.getRelativePackagePath(getClass());
		String filepath = path + "Matrice_DAEF_exemple.csv";
		MerisTypeMatrix importedMatrix = importFromFile(filepath);
		currentMatrix.replaceBy(importedMatrix);
	}

	private List<String> extractBasicLogCategoryNames(CSVReader reader) {
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
	
	
	private synchronized MerisTypeMatrix importFromFile(String filepath) {
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
				int diameterClass = Integer.parseInt(record[indexDiameterClass].toString());
				String speciesGroup = record[indexSpeciesGroup].toString().trim().toUpperCase();
				if (!merisMatrix.splittingMatrix.containsKey(speciesCode)) {
					merisMatrix.splittingMatrix.put(speciesCode, new RowEntry(speciesGroup));
				}
				RowEntry row = merisMatrix.splittingMatrix.get(speciesCode);
				if (!speciesGroup.equals(row.group)) {
					throw new UnsupportedOperationException("The file seems to have more than one group for species " + speciesCode);
				}
				if (row.diameterClasses.contains(diameterClass)) {
					throw new UnsupportedOperationException("The file seems to contain twice the diameter class " + diameterClass + " for species " + speciesCode);
				}

				Matrix values = new Matrix(1, merisMatrix.logCategoryNames.size());

				for (String basicLogCategoryName : merisMatrix.logCategoryNames) {
					int indexForThisField = reader.getHeader().getIndexOfThisField(basicLogCategoryName);
					if (indexForThisField == -1) {
						throw new UnsupportedOperationException("The field " + basicLogCategoryName + " was listed as a log category, but it cannot be found in the file header.");
					}
					double proportion = Double.parseDouble(record[indexForThisField].toString());
					if (proportion > 0d) {
						MerisTreeLogCategory logCategory = new MerisTreeLogCategory(basicLogCategoryName, speciesGroup);
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
				if (Math.abs(rowSum - 1) > 1E-4) {
					throw new UnsupportedOperationException("The sum of the proportion in row " + rowId + " is not equal to 1!");
				}
				
				values = values.scalarMultiply(1d / rowSum);
				row.diameterClasses.add(diameterClass);
				row.splittingAccordingToClass.put(diameterClass, values);
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
		MerisTreeLoggerParameters o = new MerisTreeLoggerParameters();
		o.initializeDefaultLogCategories();
		o.showUI(null);
	}
}
