/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.thinners.melothinner;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import repicea.io.javacsv.CSVReader;
import repicea.simulation.covariateproviders.plotlevel.LandOwnershipProvider.LandOwnership;
import quebecmrnfutility.simulation.covariateproviders.plotlevel.QcForestRegionProvider.QcForestRegion;
import repicea.util.ObjectUtility;

/**
 * A class that provides the annual allowable cut by region.
 * @author Mathieu Fortin - 2017
 */
class MeloThinnerAACProvider {

	private static MeloThinnerAACProvider Instance;

	private final Map<QcForestRegion, Map<LandOwnership, Map<Integer, Double>>> aacMap; // region : land ownership : year : aac/ha

	private int minimumYearDate;
	private int maximumYearDate;
	
	private MeloThinnerAACProvider() {
		aacMap = new HashMap<QcForestRegion, Map<LandOwnership, Map<Integer, Double>>>();
		init();
	}
	
	private void init() {
		String filename = ObjectUtility.getRelativePackagePath(getClass()) + "dataAAC.csv";
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				int regionCode = Integer.parseInt(record[0].toString());
				QcForestRegion region = QcForestRegion.getRegion(regionCode);
				if (region != null) {
					if (!aacMap.containsKey(region)) {
						aacMap.put(region, new HashMap<LandOwnership, Map<Integer, Double>>());
					}
					Map<LandOwnership, Map<Integer, Double>> innerMap1 = aacMap.get(region);

					String ownershipCode = record[2].toString();
					LandOwnership landOwnership = LandOwnership.getLandOwnership(ownershipCode);
					if (landOwnership != null) {
						if (!innerMap1.containsKey(landOwnership)) {
							innerMap1.put(landOwnership, new HashMap<Integer, Double>());
						}
						Map<Integer, Double> innerMap2 = innerMap1.get(landOwnership);
						
						int year = Integer.parseInt(record[1].toString());
						if (minimumYearDate == 0 || year < minimumYearDate) {
							minimumYearDate = year;
						}
						if (maximumYearDate == 0 || year > maximumYearDate) {
							maximumYearDate = year;
						}
						double aac = Double.parseDouble(record[5].toString());
						innerMap2.put(year, aac);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Unable to load the aac table in MeloThinnerAACProvider!");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		

	}
	
	/**
	 * This method returns the singleton instance of this class.
	 * @return the only MeloThinnerAACProvider instance 
	 */
	static MeloThinnerAACProvider getInstance() {
		if (Instance == null) {
			Instance = new MeloThinnerAACProvider();
		}
		return Instance;
	}

	/**
	 * Provide an array of AAC for particular years.<p>
	 * 
	 * IMPORTANT: The AAC per hectare is calculated as the total AAC divided by
	 * the area designated for wood production. <p>
	 * 
	 * If the year is smaller than 1988, it is assumed that the
	 * AAC is that of 1988. If the year is larger than 2014, it assumed that the AAC is that of 2014.
	 * 
	 * @param region a QuebecForestRegion enum
	 * @param ownership  a LandOwnership enum
	 * @param startingYear not included in the array
	 * @param endingYear included in the array
	 * @param modulationFactor a double that ranges from -1 to 1
	 * @return an array of double
	 */
	double[] getAACValues(QcForestRegion region, LandOwnership ownership, int startingYear, int endingYear, double modulationFactor) {
		if (endingYear <= startingYear) {
			throw new InvalidParameterException("The ending year must be greater than the starting year!");
		}
		int length = endingYear - startingYear;
		double[] aacArray = new double[length];
		for (int yearIndex = 0; yearIndex < aacArray.length; yearIndex++) {
			int yearDate = startingYear + yearIndex + 1;
			if (yearDate < minimumYearDate) {
				yearDate = minimumYearDate;
			}
			if (yearDate > maximumYearDate) {
				yearDate = maximumYearDate;
			}
			aacArray[yearIndex] = aacMap.get(region).get(ownership).get(yearDate) * (1d + modulationFactor);
		}
		return aacArray;
	}

//	/**
//	 * This method returns the array of aac for particular years on public lands. If the year is smaller than 1988, it is assumed that the
//	 * AAC is that of 1988. If the year is larger than 2014, it assumated that the AAC is that of 2014.
//	 * @param region a QuebecForestRegion enum
//	 * @param startingYear not included in the array
//	 * @param endingYear included in the array
//	 * @return an array of double
//	 */
//	public double[] getAACValues(QuebecForestRegion region, int startingYear, int endingYear) {
//		return getAACValues(region, LandOwnership.Public, startingYear, endingYear);
//	}

//	public static void main(String[] args) {
//		double[] aacValue = getInstance().getAACValues(QuebecForestRegion.Estrie, LandOwnership.Public, 2012, 2018);
//		int u = 0;
//	}
	
	
}
