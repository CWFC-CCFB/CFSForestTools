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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class TrilliumDiameterIncrementTest {

	static class Trillium2026TreeImpl implements Trillium2026Tree, Trillium2026Plot {

		private final double growthStepLengthYr;
		private final double totalAnnualPrecipitationMm;
		private final double meanAnnualTemperatureCelsius;
		private final double meanTminJanuaryCelsius;
		private final double totalPrecMarchToMayMm;
		private final double meanTempJuneToAugustCelsius;
		private final double t_anom;
		private final double totalRadiation;
		private final double meanSummerVPD;
		private final double frostFreeDays;
		private final double meanTmaxJulyCelsius;
		private final double SMImean;
		private final double Mx_anom;
		private final double meanSummerVPDDaylight;
		private final double totalPrecJuneToAugustMm;
		private final double P_anom;
		private final double CMI;
		private final double highestTmaxCelsius;
		private final double degreeDaysCelsius;
		private final double lowestTmin;
		private final double dbhCm;
		private final double BAL;
		private final Species species;
		protected final double pred;
		protected final double predTransformed;
		private int mcReal;
		private final int dateYr;
		private final boolean dummyHarvest;
		
		Trillium2026TreeImpl(double growthStepLengthYr,
				double totalAnnualPrecipitationMm,
				double meanAnnualTemperatureCelsius,
				double meanTminJanuaryCelsius,
				double totalPrecMarchToMayMm,
				double meanTempJuneToAugustCelsius,
				double t_anom,
				double totalRadiation,
				double meanSummerVPD,
				double frostFreeDays,
				double meanTmaxJulyCelsius,
				double SMImean,
				double Mx_anom,
				double meanSummerVPDDaylight,
				double totalPrecJuneToAugustMm,
				double P_anom,
				double CMI,
				double highestTmaxCelsius,
				double degreeDaysCelsius,
				double lowestTmin,
				double dbhCm,
				double BAL,
				Species species, 
				double pred,
				double predTransformed,
				int dateYr,
				Boolean dummyHarvest) {
			this.growthStepLengthYr = growthStepLengthYr;
			this.totalAnnualPrecipitationMm = totalAnnualPrecipitationMm;
			this.meanAnnualTemperatureCelsius = meanAnnualTemperatureCelsius;
			this.meanTminJanuaryCelsius = meanTminJanuaryCelsius;
			this.totalPrecMarchToMayMm = totalPrecMarchToMayMm;
			this.meanTempJuneToAugustCelsius = meanTempJuneToAugustCelsius;
			this.t_anom = t_anom;
			this.totalRadiation = totalRadiation;
			this.meanSummerVPD = meanSummerVPD;
			this.frostFreeDays = frostFreeDays;
			this.meanTmaxJulyCelsius = meanTmaxJulyCelsius;
			this.SMImean = SMImean;
			this.Mx_anom = Mx_anom;
			this.meanSummerVPDDaylight = meanSummerVPDDaylight;
			this.totalPrecJuneToAugustMm = totalPrecJuneToAugustMm;
			this.P_anom = P_anom;
			this.CMI = CMI;
			this.highestTmaxCelsius = highestTmaxCelsius;
			this.degreeDaysCelsius = degreeDaysCelsius;
			this.lowestTmin = lowestTmin;
			this.dbhCm = dbhCm;
			this.BAL = BAL;
			this.species = species;
			this.pred = pred;
			this.predTransformed = predTransformed;
			this.dateYr = dateYr;
			this.dummyHarvest = dummyHarvest != null ?
					dummyHarvest :
						false;
		}
		
		@Override
		public String getSubjectId() {return null;}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

		void setMonteCarloRealizationId(int real) {this.mcReal = real;}

		@Override
		public int getMonteCarloRealizationId() {return mcReal;}

		@Override
		public double getGrowthStepLengthYr() {return growthStepLengthYr;}

		@Override
		public double getTotalAnnualPrecipitationMm() {return totalAnnualPrecipitationMm;}

		@Override
		public double getMeanAnnualTemperatureCelsius() {return meanAnnualTemperatureCelsius;}

		@Override
		public double getMeanTminJanuaryCelsius() {return meanTminJanuaryCelsius;}

		@Override
		public double getTotalPrecMarchToMayMm() {return totalPrecMarchToMayMm;}

		@Override
		public double getMeanTempJuneToAugustCelsius() {return meanTempJuneToAugustCelsius;}

		@Override
		public double getMeanTempAnomalyCelsius() {return t_anom;}

		@Override
		public double getTotalRadiation() {return totalRadiation;}

		@Override
		public double getMeanSummerVPD() {return meanSummerVPD;}

		@Override
		public double getFrostFreeDays() {return frostFreeDays;}

		@Override
		public double getMeanTmaxJulyCelsius() {return meanTmaxJulyCelsius;}

		@Override
		public double getSMImean() {return SMImean;}

		@Override
		public double getMaxTempAnomalyCelsius() {return Mx_anom;}

		@Override
		public double getMeanSummerVPDDaylight() {return meanSummerVPDDaylight;}

		@Override
		public double getTotalPrecJuneToAugustMm() {return totalPrecJuneToAugustMm;}

		@Override
		public double getTotalPrecipitationAnomalyMm() {return P_anom;}

		@Override
		public double getCMI() {return CMI;}

		@Override
		public double getHighestTmaxCelsius() {return highestTmaxCelsius;}

		@Override
		public double getDegreeDaysCelsius() {return degreeDaysCelsius;}

		@Override
		public double getLowestTmin() {return lowestTmin;}


		@Override
		public double getDbhCm() {return dbhCm;}

		@Override
		public double getBasalAreaLargerThanSubjectM2Ha() {return BAL;}

		@Override
		public Species getTrillium2026TreeSpecies() {return species;}

		@Override
		public boolean isGoingToBeHarvested() {
			return dummyHarvest;
		}

		@Override
		public int getDateYr() {
			return dateYr;
		}

		@Override
		public double getLnDbhCm() {
			return Math.log(getDbhCm());
		}

		@Override
		public double getSquaredDbhCm() {
			return getDbhCm() * getDbhCm();
		}
	}

	
	private static Map<Species, List<Trillium2026TreeImpl>> TreeMap;

	@BeforeClass
	public static void readTrees() throws IOException {
		TreeMap = new LinkedHashMap<Species, List<Trillium2026TreeImpl>>();
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
					String speciesName = record[reader.getHeader().getIndexOfThisField("SpecGroup")].toString();
					Species species	= Trillium2026DiameterIncrementPredictor.getSpeciesFromString(speciesName);
					double pred = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("pred")].toString());
					double predTransformed = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("pred_transformed")].toString());
					int dateYr = ((Double) Double.parseDouble(record[reader.getHeader().getIndexOfThisField("FieldSeasonYear.x")].toString())).intValue();
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
							predTransformed,
							dateYr,
							null);
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
		for (Species species : TreeMap.keySet()) {
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
		for (Species species : TreeMap.keySet()) {
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
		Trillium2026TreeImpl t = TreeMap.get(Species.Abies_balsamea).get(0);
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
