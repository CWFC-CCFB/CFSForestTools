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

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ontariomnrf.predictor.trillium2026.Trillium2026Tree.Trillium2026TreeSpecies;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class TrilliumDiameterIncrementTest {


	private static Map<Trillium2026TreeSpecies, List<Trillium2026TreeImpl>> TreeMap;

	@BeforeClass
	public static void readTrees() throws IOException {
		TreeMap = new LinkedHashMap<Trillium2026TreeSpecies, List<Trillium2026TreeImpl>>();
		String filename = ObjectUtility.getPackagePath(TrilliumDiameterIncrementTest.class) + "diameterIncrementTestData.csv";
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				double growthStepLengthYr = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("dt")].toString());
				if (growthStepLengthYr > 1) {
					double totalAnnualPrecipitationMm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("TotalPrcp")].toString());
					double meanAnnualTemperatureCelsius = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanTair")].toString());
					double meanTminJanuaryCelsius = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanTminJanuary")].toString());
					double totalPrecMarchToMayMm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("TotalPrecMarchToMay")].toString());
					double meanTempJuneToAugustCelsius = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanTempJuneToAugust")].toString());
					double t_anom = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("T_anom")].toString());
					double totalRadiation = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("TotalRadiation")].toString());
					double meanSummerVPD = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanSummerVPD")].toString());
					double frostFreeDays = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("FrostFreeDay")].toString());
					double meanTmaxJulyCelsius = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanTmaxJuly")].toString());
					double SMImean = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("SMImean")].toString());
					double Mx_anom = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("Mx_anom")].toString());
					double meanSummerVPDDaylight = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanSummerVPDDaylight")].toString());
					double totalPrecJuneToAugustMm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("TotalPrecJuneToAugust")].toString());
					double P_anom = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("P_anom")].toString());
					double CMI = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("CMI")].toString());
					double highestTmaxCelsius = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("HitghestTmax")].toString());
					double degreeDaysCelsius = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("DD")].toString());
					double lowestTmin = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("LowestTmin")].toString());
					double dbhCm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("DBH.x")].toString());
//					double dbhCm2 = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("DBH.y")].toString());
//					double dDbhCm = dbhCm2 - dbhCm;
//					double transformedY = Math.log(dDbhCm + Math.sqrt(dDbhCm * dDbhCm + 1));
//					double retransformedY = Math.sinh(transformedY);
					double BAL = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("BAL")].toString());
					String genusName = record[reader.getHeader().getIndexOfThisField("SpecGenus")].toString();
					String speciesName = record[reader.getHeader().getIndexOfThisField("SpecSpec")].toString();
					String completeSpeciesName = genusName + speciesName;
					Trillium2026TreeSpecies species	= Trillium2026TreeSpecies.getTrilliumSpecies(completeSpeciesName);
					if (species == null) {
						throw new InvalidParameterException("Species " + completeSpeciesName + " is not recognized");
					}
					double pred = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("pred")].toString());
					double predTransformed = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("pred_transformed")].toString());
					Trillium2026TreeImpl tree = new Trillium2026TreeImpl(growthStepLengthYr,
							totalAnnualPrecipitationMm,
							meanAnnualTemperatureCelsius,
							meanTminJanuaryCelsius,
							totalPrecMarchToMayMm,
							meanTempJuneToAugustCelsius,
							t_anom,
							totalRadiation,
							meanSummerVPD,
							frostFreeDays,
							meanTmaxJulyCelsius,
							SMImean,
							Mx_anom,
							meanSummerVPDDaylight,
							totalPrecJuneToAugustMm,
							P_anom,
							CMI,
							highestTmaxCelsius,
							degreeDaysCelsius,
							lowestTmin,
							dbhCm,
							BAL,
							species, 
							pred, 
							predTransformed);
					if (!TreeMap.containsKey(species)) {
						TreeMap.put(species, new ArrayList<Trillium2026TreeImpl>());
					}
					TreeMap.get(species).add(tree);
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	@Test
	public void test01DeterministicPredictionsOnTransformedScale() {
		Trillium2026DiameterIncrementPredictor diamIncPredictor = new Trillium2026DiameterIncrementPredictor(false); // deterministic
		diamIncPredictor.enableBackTransformation(false);
		for (Trillium2026TreeSpecies species : TreeMap.keySet()) {
			int nbTested = 0;
			for (Trillium2026TreeImpl t : TreeMap.get(species)) {
				double observed = diamIncPredictor.predictGrowth(t, t);
				double expected = t.pred;
				Assert.assertEquals("Comparing predictions on transformed scale", expected, observed, 1E-8);
				nbTested++;
			}
			System.out.println(species.name() + " - Nb trees successfully tested = " + nbTested);
		}
	}

	@Test
	public void test02DeterministicPredictionsOnOriginalScale() {
		Trillium2026DiameterIncrementPredictor diamIncPredictor = new Trillium2026DiameterIncrementPredictor(false); // deterministic
		for (Trillium2026TreeSpecies species : TreeMap.keySet()) {
			int nbTested = 0;
			for (Trillium2026TreeImpl t : TreeMap.get(species)) {
				double observed = diamIncPredictor.predictGrowth(t, t);
				double expected = t.predTransformed;
				Assert.assertEquals("Comparing predictions on transformed scale", expected, observed, 1E-8);
				nbTested++;
			}
			System.out.println(species.name() + " - Nb trees successfully tested = " + nbTested);
		}
	}

	@Test
	public void test03StochasticPredictions() {
		Trillium2026DiameterIncrementPredictor stoPredictor = new Trillium2026DiameterIncrementPredictor(false, true); // deterministic
		Trillium2026DiameterIncrementPredictor detPredictor = new Trillium2026DiameterIncrementPredictor(false); // deterministic
		Trillium2026TreeImpl t = TreeMap.get(Trillium2026TreeSpecies.AbiesBalsamea).get(0);
		Matrix real;
		MonteCarloEstimate mcEstimate = new MonteCarloEstimate();
		for (int i = 0; i < 100000; i++) {
			t.setMonteCarloRealizationId(i);
			real = new Matrix(1,1);
			real.setValueAt(0, 0, stoPredictor.predictGrowth(t, t));
			mcEstimate.addRealization(real);
		}
		double observed = mcEstimate.getMean().getValueAt(0, 0);
		double expected = detPredictor.predictGrowth(t, t);
		System.out.println("Stochastic prediction = " +  observed + "; deterministic prediction = " + expected);
		Assert.assertEquals("Comparing stochastic and deterministic predictions", expected, observed, 1E-2);
		
	}
	
}
