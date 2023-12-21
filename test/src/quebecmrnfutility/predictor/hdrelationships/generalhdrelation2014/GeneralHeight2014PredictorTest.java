/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2015 Gouvernement du Quebec 
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
package quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014.Heightable2014Tree.Hd2014Species;
import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;


public class GeneralHeight2014PredictorTest {

	
	static Map<String, Heightable2014Stand> standMap;

	
	static void ReadStands() {
		String filename = ObjectUtility.getPackagePath(GeneralHeight2014PredictorTest.class) + "fichier_test_unitaire_smaller.csv";
		standMap = new HashMap<String, Heightable2014Stand>();
		CSVReader reader;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			int treeID = 0;
			while ((record = reader.nextRecord()) != null) {
				String placetteID = record[0].toString().trim().concat(record[2].toString().trim());
				double basalAreaM2Ha = Double.parseDouble(record[7].toString());
				double meanQuadraticDiameterCm = Double.parseDouble(record[8].toString());
				String regEco = record[13].toString().trim();
				String typeEco = record[15].toString().trim();
				double elevationM = Double.parseDouble(record[17].toString());
				double meanAnnualPrecipitationMm = Double.parseDouble(record[19].toString());
				double meanAnnualTemperatureC = Double.parseDouble(record[21].toString());
				
				double dbhCm = Double.parseDouble(record[4].toString());
				String species = record[5].toString();
				double predictedHeight = Double.parseDouble(record[29].toString());
				
				if (!standMap.containsKey(placetteID)) {
					standMap.put(placetteID, new Heightable2014StandImpl(placetteID,
							basalAreaM2Ha,
							meanQuadraticDiameterCm,
							regEco,
							typeEco,
							elevationM,
							meanAnnualTemperatureC,
							meanAnnualPrecipitationMm));
				}
				Heightable2014StandImpl stand = (Heightable2014StandImpl) standMap.get(placetteID);
				new Heightable2014TreeImpl(stand, dbhCm, predictedHeight, treeID++, species);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void comparePredictionsWithSAS() {
		if (standMap == null) {
			GeneralHeight2014PredictorTest.ReadStands();
		}
		List<Integer> measurementDates = new ArrayList<Integer>();
		measurementDates.add(2015);
		GeneralHeight2014Predictor predictor = new GeneralHeight2014Predictor();
		
		int goodMatches = 0;
		List<Hd2014Species> goodMatchingSpecies = new ArrayList<Hd2014Species>();
		for (Heightable2014Stand stand : standMap.values()) {
			Collection<Heightable2014Tree> trees = stand.getTrees();
			for (Heightable2014Tree t : trees) {
				double actual = predictor.predictHeightM(stand, t);
				double expected = ((Heightable2014TreeImpl) t).getPredictedHeight();
				if (Math.abs(expected-actual) < 1E-8) {
					goodMatches++;
					if (!goodMatchingSpecies.contains(t.getHeightable2014TreeSpecies())) {
						goodMatchingSpecies.add(t.getHeightable2014TreeSpecies());
					}
				} else {
					goodMatchingSpecies.remove(t.getHeightable2014TreeSpecies());
				}
				Assert.assertEquals("Comparing predicted Heights", expected, actual, 1E-8);
				goodMatches++;
			}
		}
		System.out.println("Generalhdrelation2014.comparePredictionsWithSAS - Successful comparisons " + goodMatches);
	}
	
}
