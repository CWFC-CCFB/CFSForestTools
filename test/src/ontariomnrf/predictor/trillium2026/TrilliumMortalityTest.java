/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2026 His Majesty the King in right of Canada
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
import repicea.simulation.climate.REpiceaClimateManager.ClimateVariableTemporalResolution;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class TrilliumMortalityTest {

	static class Trillium2026TreeImpl implements Trillium2026Tree, Trillium2026MortalityPlot {

		private final double growthStepLengthYr;
		private final double meanTminJanuaryCelsius;
		private final double meanTempJuneToAugustCelsius;
		private final double totalPrecMarchToMayMm;
		private final double totalPrecJuneToAugustMm;
		
		private final double dbhCm;
		private final double BAL;
		private final Species species;
		protected final double pred;
		private int mcReal;
		private final int dateYr;
		private final boolean dummyHarvest;
		private final boolean planted;
		
		Trillium2026TreeImpl(double growthStepLengthYr,
				double meanTminJanuaryCelsius,
				double totalPrecMarchToMayMm,
				double meanTempJuneToAugustCelsius,
				double totalPrecJuneToAugustMm,
				double dbhCm,
				double BAL,
				Species species, 
				double pred,
				int dateYr,
				boolean dummyHarvest,
				boolean planted) {
			this.growthStepLengthYr = growthStepLengthYr;
			this.meanTminJanuaryCelsius = meanTminJanuaryCelsius;
			this.totalPrecMarchToMayMm = totalPrecMarchToMayMm;
			this.meanTempJuneToAugustCelsius = meanTempJuneToAugustCelsius;
			this.totalPrecJuneToAugustMm = totalPrecJuneToAugustMm;
			this.dbhCm = dbhCm;
			this.BAL = BAL;
			this.species = species;
			this.pred = pred;
			this.dateYr = dateYr;
			this.dummyHarvest = dummyHarvest;
			this.planted = planted;
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
		public double getMeanMinimumJanuaryTemperatureCelsius(ClimateVariableTemporalResolution resolution) {return meanTminJanuaryCelsius;}

		@Override
		public double getTotalPrecipitationFromMarchToMayMm(ClimateVariableTemporalResolution resolution) {return totalPrecMarchToMayMm;}

		@Override
		public double getMeanTemperatureFromJuneToAugustCelsius(ClimateVariableTemporalResolution resolution) {return meanTempJuneToAugustCelsius;}

		@Override
		public double getTotalPrecipitationFromJuneToAugustMm(ClimateVariableTemporalResolution resolution) {return totalPrecJuneToAugustMm;}

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
		public int getDateYr() {return dateYr;}

		@Override
		public double getLnDbhCm() {
			return Math.log(getDbhCm());
		}

		@Override
		public double getSquaredDbhCm() {
			return getDbhCm() * getDbhCm();
		}

		@Override
		public boolean isFromPlantation() {return planted;}

	}


	private static Map<Species, List<Trillium2026TreeImpl>> TreeMap;

	@BeforeClass
	public static void readTrees() throws IOException {
		TreeMap = new LinkedHashMap<Species, List<Trillium2026TreeImpl>>();
		String filename = ObjectUtility.getPackagePath(TrilliumMortalityTest.class) + "mortalityTestData.csv";
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				double growthStepLengthYr = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("dt")].toString());
				double meanTminJanuaryCelsius = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanTminJanuary")].toString());
				double totalPrecMarchToMayMm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("TotalPrecMarchToMay")].toString());
				double meanTempJuneToAugustCelsius = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanTempJuneToAugust")].toString());
				double totalPrecJuneToAugustMm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("TotalPrecJuneToAugust")].toString());
				double dbhCm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("DBH.x")].toString());
				double BAL = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("BAL")].toString());
				String speciesName = record[reader.getHeader().getIndexOfThisField("SpecGroup")].toString();
				Species species	= Trillium2026MortalityPredictor.getSpeciesFromString(speciesName);
				double pred = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("margPred")].toString());
				int dateYr = ((Double) Double.parseDouble(record[reader.getHeader().getIndexOfThisField("FieldSeasonYear.x")].toString())).intValue();
				boolean dummyHarvest = Integer.parseInt(record[reader.getHeader().getIndexOfThisField("dummyHarvest")].toString()) == 1;
				boolean planted = record[reader.getHeader().getIndexOfThisField("planted")].toString().equals("TRUE");
				Trillium2026TreeImpl tree = new Trillium2026TreeImpl(growthStepLengthYr,
						meanTminJanuaryCelsius,
						totalPrecMarchToMayMm,
						meanTempJuneToAugustCelsius,
						totalPrecJuneToAugustMm,
						dbhCm,
						BAL,
						species, 
						pred, 
						dateYr,
						dummyHarvest,
						planted);
				if (!TreeMap.containsKey(species)) {
					TreeMap.put(species, new ArrayList<Trillium2026TreeImpl>());
				}
				TreeMap.get(species).add(tree);
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
	public void test01DeterministicPredictions() {
		Trillium2026MortalityPredictor mortPredictor = new Trillium2026MortalityPredictor(false); // deterministic
		for (Species species : TreeMap.keySet()) {
			int nbTested = 0;
			for (Trillium2026TreeImpl t : TreeMap.get(species)) {
				double observed = mortPredictor.predictEventProbability(t, t);
				double expected = t.pred;
				Assert.assertEquals("Comparing deterministic predictions for species " + species.name() + " nbTested = " + nbTested, expected, observed, 1E-8);
				nbTested++;
			}
			System.out.println(species.name() + " - Nb trees successfully tested = " + nbTested);
		}
	}

	@Test
	public void test03StochasticPredictions() {
		Trillium2026MortalityPredictor stoPredictor = new Trillium2026MortalityPredictor(false, true, false); // random effect variability enabled
		Trillium2026MortalityPredictor detPredictor = new Trillium2026MortalityPredictor(false); 
		Trillium2026TreeImpl t = TreeMap.get(Species.Abies_balsamea).get(0);
		Matrix real;
		MonteCarloEstimate mcEstimate = new MonteCarloEstimate();
		for (int i = 0; i < 100000; i++) {
			t.setMonteCarloRealizationId(i);
			real = new Matrix(1,1);
			real.setValueAt(0, 0, stoPredictor.predictEventProbability(t, t));
			mcEstimate.addRealization(real);
		}
		double observed = mcEstimate.getMean().getValueAt(0, 0);
		double expected = detPredictor.predictEventProbability(t, t);
		System.out.println("Stochastic prediction = " +  observed + "; deterministic prediction = " + expected);
		Assert.assertEquals("Comparing stochastic and deterministic predictions", expected, observed, 1E-2);
		
	}
	
}
