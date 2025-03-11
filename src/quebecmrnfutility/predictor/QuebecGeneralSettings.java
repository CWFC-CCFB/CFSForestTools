/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import quebecmrnfutility.simulation.covariateproviders.plotlevel.QcForestRegionProvider.QcForestRegion;
import repicea.io.javacsv.CSVReader;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider.DrainageGroup;
import repicea.util.ObjectUtility;

/**
 * This class contains the general settings for Quebec context.
 * @author Mathieu Fortin - July 2014
 */
public class QuebecGeneralSettings {

	private final static Random RANDOM = new Random();
	
	private static final Map<String, Map<QcForestRegion, Double>> FOREST_REGION_MAP = new HashMap<String, Map<QcForestRegion, Double>>();
	static {
		String path = ObjectUtility.getRelativePackagePath(QuebecGeneralSettings.class);
		CSVReader reader = null;
		try {
			reader = new CSVReader(path + "correspondanceTable.csv");
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				String regEco = record[0].toString();
				int regionCode = Integer.parseInt(record[1].toString());
				QcForestRegion forestRegion = QcForestRegion.getRegion(regionCode);
				double prob = Double.parseDouble(record[2].toString());
				if (!FOREST_REGION_MAP.containsKey(regEco)) {
					FOREST_REGION_MAP.put(regEco, new HashMap<QcForestRegion, Double>());
				}
				Map<QcForestRegion, Double> innerMap = FOREST_REGION_MAP.get(regEco);
				innerMap.put(forestRegion, prob);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	/**
	 * Provide the administrative region.<p>
	 * 
	 * The match between the ecological and administrative region is not perfect. If the
	 * stochastic mode is enabled, then a random number is generated in order to determine the
	 * administrative region.
	 * 
	 * @param ecologicalRegion the ecological region.
	 * @param stochastic a boolean to enable a stochastic mode
	 * @return a QcForestRegion enum
	 */
	public static QcForestRegion getForestRegion(String ecologicalRegion, boolean stochastic) {
		Map<QcForestRegion, Double> forestRegionMap = FOREST_REGION_MAP.get(ecologicalRegion);
		double maxValue = 0;
		double sumValue = 0;
		double randomValue = RANDOM.nextDouble();
		QcForestRegion currentSelectedRegion = null;
		for (QcForestRegion region : forestRegionMap.keySet()) {
			double prob = forestRegionMap.get(region);
			sumValue += prob;
			if (prob > maxValue) {
				maxValue = prob;
				currentSelectedRegion = region;
			}
			if (stochastic & randomValue < sumValue) {
				return region;
			}
		}
		return currentSelectedRegion;
	}
	
	
	public static final Set<String> CLIMATIC_SUBDOMAIN_LIST = new HashSet<String>();
	static {
		CLIMATIC_SUBDOMAIN_LIST.add("1ouest");
		CLIMATIC_SUBDOMAIN_LIST.add("2ouest");
		CLIMATIC_SUBDOMAIN_LIST.add("2est");
		CLIMATIC_SUBDOMAIN_LIST.add("3ouest");
		CLIMATIC_SUBDOMAIN_LIST.add("3est");
		CLIMATIC_SUBDOMAIN_LIST.add("4ouest");
		CLIMATIC_SUBDOMAIN_LIST.add("4est");
		CLIMATIC_SUBDOMAIN_LIST.add("5ouest");
		CLIMATIC_SUBDOMAIN_LIST.add("5est");
		CLIMATIC_SUBDOMAIN_LIST.add("6ouest");
		CLIMATIC_SUBDOMAIN_LIST.add("6est");
	}

	public static Set<String> SHRUB_SPECIES_LIST = new HashSet<String>();
	static {
		SHRUB_SPECIES_LIST.add("ERG");
		SHRUB_SPECIES_LIST.add("ERP");
		SHRUB_SPECIES_LIST.add("ERE");
		SHRUB_SPECIES_LIST.add("AUC");
		SHRUB_SPECIES_LIST.add("AUR");
		SHRUB_SPECIES_LIST.add("AME");
		SHRUB_SPECIES_LIST.add("ARM");
		SHRUB_SPECIES_LIST.add("BEG");
		SHRUB_SPECIES_LIST.add("BEP");
		SHRUB_SPECIES_LIST.add("CAR");
		SHRUB_SPECIES_LIST.add("CEO");
		SHRUB_SPECIES_LIST.add("COP");
		SHRUB_SPECIES_LIST.add("COA");
		SHRUB_SPECIES_LIST.add("COR");
		SHRUB_SPECIES_LIST.add("COC");
		SHRUB_SPECIES_LIST.add("CRA");
		SHRUB_SPECIES_LIST.add("DIE");
		SHRUB_SPECIES_LIST.add("DIR");
		SHRUB_SPECIES_LIST.add("ILV");
		SHRUB_SPECIES_LIST.add("JUC");
		SHRUB_SPECIES_LIST.add("JUN");
		SHRUB_SPECIES_LIST.add("JUH");
		SHRUB_SPECIES_LIST.add("JUV");
		SHRUB_SPECIES_LIST.add("LON");
		SHRUB_SPECIES_LIST.add("LOH");
		SHRUB_SPECIES_LIST.add("LOV");
		SHRUB_SPECIES_LIST.add("MAS");
		SHRUB_SPECIES_LIST.add("MYG");
		SHRUB_SPECIES_LIST.add("NEM");
		SHRUB_SPECIES_LIST.add("PAQ");
		SHRUB_SPECIES_LIST.add("PRP");
		SHRUB_SPECIES_LIST.add("PRV");
		SHRUB_SPECIES_LIST.add("PRN");
		SHRUB_SPECIES_LIST.add("RHA");
		SHRUB_SPECIES_LIST.add("RHM");
		SHRUB_SPECIES_LIST.add("RHR");
		SHRUB_SPECIES_LIST.add("RHT");
		SHRUB_SPECIES_LIST.add("RIA");
		SHRUB_SPECIES_LIST.add("RIC");
		SHRUB_SPECIES_LIST.add("RIG");
		SHRUB_SPECIES_LIST.add("RIH");
		SHRUB_SPECIES_LIST.add("RIL");
		SHRUB_SPECIES_LIST.add("RIT");
		SHRUB_SPECIES_LIST.add("ROA");
		SHRUB_SPECIES_LIST.add("RUA");
		SHRUB_SPECIES_LIST.add("RUI");
		SHRUB_SPECIES_LIST.add("RUO");
		SHRUB_SPECIES_LIST.add("RUD");
		SHRUB_SPECIES_LIST.add("SAL");
		SHRUB_SPECIES_LIST.add("SAC");
		SHRUB_SPECIES_LIST.add("SAP");
		SHRUB_SPECIES_LIST.add("SHP");
		SHRUB_SPECIES_LIST.add("SOA");
		SHRUB_SPECIES_LIST.add("SOD");
		SHRUB_SPECIES_LIST.add("SPL");
		SHRUB_SPECIES_LIST.add("SPT");
		SHRUB_SPECIES_LIST.add("TAC");
		SHRUB_SPECIES_LIST.add("VIL");
		SHRUB_SPECIES_LIST.add("VIC");
		SHRUB_SPECIES_LIST.add("VIE");
		SHRUB_SPECIES_LIST.add("VIT");
		SHRUB_SPECIES_LIST.add("VIR");
		SHRUB_SPECIES_LIST.add("ERG");
		SHRUB_SPECIES_LIST.add("ERG");
		SHRUB_SPECIES_LIST.add("ERG");
	}


	public static final Set<String> CONIFEROUS_SPECIES = new HashSet<String>();
	static {
		CONIFEROUS_SPECIES.add("EPB");
		CONIFEROUS_SPECIES.add("EPN");
		CONIFEROUS_SPECIES.add("EPR");
		CONIFEROUS_SPECIES.add("MEL");
		CONIFEROUS_SPECIES.add("PIB");
		CONIFEROUS_SPECIES.add("PIG");
		CONIFEROUS_SPECIES.add("PIR");
		CONIFEROUS_SPECIES.add("PRU");
		CONIFEROUS_SPECIES.add("SAB");
		CONIFEROUS_SPECIES.add("THO");
		CONIFEROUS_SPECIES.add("EPX");
		CONIFEROUS_SPECIES.add("PIN");
		CONIFEROUS_SPECIES.add("RES");
	}

	public static final List<String> POTENTIAL_VEGETATION_LIST = new ArrayList<String>();
	static {
		POTENTIAL_VEGETATION_LIST.add("FC1");
		POTENTIAL_VEGETATION_LIST.add("FE1");
		POTENTIAL_VEGETATION_LIST.add("FE2");
		POTENTIAL_VEGETATION_LIST.add("FE3");
		POTENTIAL_VEGETATION_LIST.add("FE4");
		POTENTIAL_VEGETATION_LIST.add("FE5");
		POTENTIAL_VEGETATION_LIST.add("FE6");
		POTENTIAL_VEGETATION_LIST.add("FO1");
		POTENTIAL_VEGETATION_LIST.add("ME1");
		POTENTIAL_VEGETATION_LIST.add("MF1");
		POTENTIAL_VEGETATION_LIST.add("MJ1");
		POTENTIAL_VEGETATION_LIST.add("MJ2");
		POTENTIAL_VEGETATION_LIST.add("MS1");
		POTENTIAL_VEGETATION_LIST.add("MS2");
		POTENTIAL_VEGETATION_LIST.add("MS4");
		POTENTIAL_VEGETATION_LIST.add("MS6");
		POTENTIAL_VEGETATION_LIST.add("RB1");
		POTENTIAL_VEGETATION_LIST.add("RB2");
		POTENTIAL_VEGETATION_LIST.add("RB5");
		POTENTIAL_VEGETATION_LIST.add("RC3");
		POTENTIAL_VEGETATION_LIST.add("RE1");
		POTENTIAL_VEGETATION_LIST.add("RE2");
		POTENTIAL_VEGETATION_LIST.add("RE3");
		POTENTIAL_VEGETATION_LIST.add("RE4");
		POTENTIAL_VEGETATION_LIST.add("RP1");
		POTENTIAL_VEGETATION_LIST.add("RS1");
		POTENTIAL_VEGETATION_LIST.add("RS2");
		POTENTIAL_VEGETATION_LIST.add("RS3");
		POTENTIAL_VEGETATION_LIST.add("RS4");
		POTENTIAL_VEGETATION_LIST.add("RS5");
		POTENTIAL_VEGETATION_LIST.add("RS7");
		POTENTIAL_VEGETATION_LIST.add("RT1");
	}

	public final static Map<String,String> ECO_REGION_MAP = new HashMap<String,String>();
	static {	
		ECO_REGION_MAP.put("1a","1ouest");
		ECO_REGION_MAP.put("2a","2ouest");
		ECO_REGION_MAP.put("2b","2est");
		ECO_REGION_MAP.put("2c","2est");
		ECO_REGION_MAP.put("3a","3ouest");
		ECO_REGION_MAP.put("3b","3ouest");		
		ECO_REGION_MAP.put("3c","3est");
		ECO_REGION_MAP.put("3d","3est");
		ECO_REGION_MAP.put("4a","4ouest");
		ECO_REGION_MAP.put("4b","4ouest");
		ECO_REGION_MAP.put("4c","4ouest");
		ECO_REGION_MAP.put("4d","4est");
		ECO_REGION_MAP.put("4e","4est");
		ECO_REGION_MAP.put("4f","4est");
		ECO_REGION_MAP.put("4g","4est");
		ECO_REGION_MAP.put("4h","4est");		
		ECO_REGION_MAP.put("5a","5ouest");
		ECO_REGION_MAP.put("5b","5ouest");
		ECO_REGION_MAP.put("5c","5ouest");
		ECO_REGION_MAP.put("5d","5ouest");
		ECO_REGION_MAP.put("5e","5est");
		ECO_REGION_MAP.put("5f","5est");
		ECO_REGION_MAP.put("5g","5est");
		ECO_REGION_MAP.put("5h","5est");
		ECO_REGION_MAP.put("5i","5est");
		ECO_REGION_MAP.put("5j","5est");
		ECO_REGION_MAP.put("6a","6ouest");
		ECO_REGION_MAP.put("6b","6ouest");
		ECO_REGION_MAP.put("6c","6ouest");
		ECO_REGION_MAP.put("6d","6ouest");
		ECO_REGION_MAP.put("6e","6ouest");
		ECO_REGION_MAP.put("6f","6ouest");
		ECO_REGION_MAP.put("6g","6ouest");
		ECO_REGION_MAP.put("6h","6est");
		ECO_REGION_MAP.put("6i","6est");
		ECO_REGION_MAP.put("6j","6est");
		ECO_REGION_MAP.put("6k","6est");
		ECO_REGION_MAP.put("6l","6est");
		ECO_REGION_MAP.put("6m","6est");
		ECO_REGION_MAP.put("6n","6est");
		ECO_REGION_MAP.put("6o","6est");
		ECO_REGION_MAP.put("6p","6est");
		ECO_REGION_MAP.put("6q","6est");
		ECO_REGION_MAP.put("6r","6est");
		ECO_REGION_MAP.put("6s","6est");
	}

	public static final Map<String, DrainageGroup> ENVIRONMENT_TYPE = new HashMap<String, DrainageGroup>();
	static {
		ENVIRONMENT_TYPE.put("0", DrainageGroup.Xeric);
		ENVIRONMENT_TYPE.put("1", DrainageGroup.Mesic);
		ENVIRONMENT_TYPE.put("2", DrainageGroup.Mesic);
		ENVIRONMENT_TYPE.put("3", DrainageGroup.Mesic);
		ENVIRONMENT_TYPE.put("4", DrainageGroup.Subhydric);
		ENVIRONMENT_TYPE.put("5", DrainageGroup.Subhydric);
		ENVIRONMENT_TYPE.put("6", DrainageGroup.Subhydric);
		ENVIRONMENT_TYPE.put("7", DrainageGroup.Hydric);
		ENVIRONMENT_TYPE.put("8", DrainageGroup.Hydric);
		ENVIRONMENT_TYPE.put("9", DrainageGroup.Hydric);
	}
	
	public static final Map<Integer, String> TREE_STATUS_LIST = new HashMap<Integer, String>();
	static {
		TREE_STATUS_LIST.put(10, "Vivant");
		TREE_STATUS_LIST.put(12, "Vivant");
		TREE_STATUS_LIST.put(14, "Mort");
		TREE_STATUS_LIST.put(15, "Mort");
		TREE_STATUS_LIST.put(16, "Mort");
		TREE_STATUS_LIST.put(23, "Mort");
		TREE_STATUS_LIST.put(24, "Mort");
		// STATUS 25 - intruder should not be listed
		TREE_STATUS_LIST.put(26, "Coup\u00E9");
		TREE_STATUS_LIST.put(29, "Vivant");
		TREE_STATUS_LIST.put(30, "Vivant");
		TREE_STATUS_LIST.put(32, "Vivant");
		TREE_STATUS_LIST.put(34, "Mort");
		TREE_STATUS_LIST.put(35, "Mort");
		TREE_STATUS_LIST.put(36, "Mort");
		TREE_STATUS_LIST.put(40, "Vivant");
		TREE_STATUS_LIST.put(42, "Vivant");
		TREE_STATUS_LIST.put(44, "Mort");
		TREE_STATUS_LIST.put(45, "Mort");
		TREE_STATUS_LIST.put(46, "Mort");
		TREE_STATUS_LIST.put(50, "Vivant");
		TREE_STATUS_LIST.put(52, "Vivant");
		TREE_STATUS_LIST.put(54, "Mort");
		TREE_STATUS_LIST.put(55, "Mort");
		TREE_STATUS_LIST.put(56, "Mort");
	}

	public static final Map<String, Integer> AGE_CLASS_MAP = new HashMap<String, Integer>();
	static {
		AGE_CLASS_MAP.put("0", 0);
		
		AGE_CLASS_MAP.put("10", 10);
		AGE_CLASS_MAP.put("1010", 10);
		AGE_CLASS_MAP.put("1030", 10);
		AGE_CLASS_MAP.put("1050", 10);
		AGE_CLASS_MAP.put("1070", 10);
		AGE_CLASS_MAP.put("1090", 10);
		AGE_CLASS_MAP.put("10120", 10);
		
		AGE_CLASS_MAP.put("30", 30);
		AGE_CLASS_MAP.put("3010", 30);
		AGE_CLASS_MAP.put("3030", 30);
		AGE_CLASS_MAP.put("3050", 30);
		AGE_CLASS_MAP.put("3070", 30);
		AGE_CLASS_MAP.put("3090", 30);
		AGE_CLASS_MAP.put("30120", 30);

		AGE_CLASS_MAP.put("30VIN", 30);

		AGE_CLASS_MAP.put("50", 50);
		AGE_CLASS_MAP.put("5010", 50);
		AGE_CLASS_MAP.put("5030", 50);
		AGE_CLASS_MAP.put("5050", 50);
		AGE_CLASS_MAP.put("5070", 50);
		AGE_CLASS_MAP.put("5090", 50);
		AGE_CLASS_MAP.put("50120", 50);
		
		AGE_CLASS_MAP.put("50VIN", 50);

		AGE_CLASS_MAP.put("70", 70);
		AGE_CLASS_MAP.put("7010", 70);
		AGE_CLASS_MAP.put("7030", 70);
		AGE_CLASS_MAP.put("7050", 70);
		AGE_CLASS_MAP.put("7070", 70);
		AGE_CLASS_MAP.put("7090", 70);
		AGE_CLASS_MAP.put("70120", 70);
		
		AGE_CLASS_MAP.put("90", 90);
		AGE_CLASS_MAP.put("9010", 90);
		AGE_CLASS_MAP.put("9030", 90);
		AGE_CLASS_MAP.put("9050", 90);
		AGE_CLASS_MAP.put("9070", 90);
		AGE_CLASS_MAP.put("9090", 90);
		AGE_CLASS_MAP.put("90120", 90);

		AGE_CLASS_MAP.put("90JIN", 90);

		AGE_CLASS_MAP.put("120", 120);
		AGE_CLASS_MAP.put("12010", 120);
		AGE_CLASS_MAP.put("12030", 120);
		AGE_CLASS_MAP.put("12050", 120);
		AGE_CLASS_MAP.put("12070", 120);
		AGE_CLASS_MAP.put("12090", 120);
		AGE_CLASS_MAP.put("12012", 120);
		
		AGE_CLASS_MAP.put("120JI", 120);
		AGE_CLASS_MAP.put("120VI", 120);

		AGE_CLASS_MAP.put("JIN", 70);
		AGE_CLASS_MAP.put("JIN10", 70);
		AGE_CLASS_MAP.put("JIN30", 70);

		AGE_CLASS_MAP.put("VIN", 90);
		AGE_CLASS_MAP.put("VIN10", 90);
		AGE_CLASS_MAP.put("VIN30", 90);
		AGE_CLASS_MAP.put("VIN50", 90);
		AGE_CLASS_MAP.put("VIN70", 90);
		
		AGE_CLASS_MAP.put("VINJI", 90);

		AGE_CLASS_MAP.put("JIR", 70);
		AGE_CLASS_MAP.put("VIR", 90);

	}
	
	
//	public static void main(String[] args) {
//		QuebecForestRegion region = QuebecGeneralSettings.getForestRegion("2b", false);
//	}
}
