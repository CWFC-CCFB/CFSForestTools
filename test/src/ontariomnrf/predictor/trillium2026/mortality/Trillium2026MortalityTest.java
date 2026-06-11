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
package ontariomnrf.predictor.trillium2026.mortality;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ontariomnrf.predictor.trillium2026.Trillium2026Tree;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.climate.REpiceaClimateVariableInformation;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpeciesCompliantObject;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class Trillium2026MortalityTest {

	static class Trillium2026TreeImpl implements Trillium2026Tree, Trillium2026MortalityPlot {

		private final int growthStepLengthYr;
		private final double meanTminJanuaryCelsius;
		private final double meanTempJuneToAugustCelsius;
		private final double totalPrecMarchToMayMm;
		private final double totalPrecJuneToAugustMm;
		
		private final double stemDensityHa;
		private final double meanTair;
		private final double totalPrcp;
		
		private final double dbhCm;
		private final double BAL;
		private final Species species;
		protected final double pred;
		private int mcReal;
		private final int dateYr;
		private final boolean dummyHarvest;
		private final boolean planted;
		private final String clusterId;
		
		Trillium2026TreeImpl(String clusterId,
				int growthStepLengthYr,
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
				boolean planted,
				double stemDensityHa,
				double meanTair,
				double totalPrcp) {
			this.clusterId = clusterId;
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
			this.stemDensityHa = stemDensityHa;
			this.meanTair = meanTair;
			this.totalPrcp = totalPrcp;
		}
		
		@Override
		public String getSubjectId() {return null;}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

		void setMonteCarloRealizationId(int real) {this.mcReal = real;}

		@Override
		public int getMonteCarloRealizationId() {return mcReal;}

		@Override
		public int getGrowthStepLengthYr() {return growthStepLengthYr;}

		@Override
		public double getMeanMinimumJanuaryTemperatureCelsius(REpiceaClimateVariableInformation resolution) {return meanTminJanuaryCelsius;}

		@Override
		public double getTotalPrecipitationFromMarchToMayMm(REpiceaClimateVariableInformation resolution) {return totalPrecMarchToMayMm;}

		@Override
		public double getMeanTemperatureFromJuneToAugustCelsius(REpiceaClimateVariableInformation resolution) {return meanTempJuneToAugustCelsius;}

		@Override
		public double getTotalPrecipitationFromJuneToAugustMm(REpiceaClimateVariableInformation resolution) {return totalPrecJuneToAugustMm;}

		@Override
		public double getDbhCm() {return dbhCm;}

		@Override
		public double getBasalAreaLargerThanSubjectM2Ha() {return BAL;}

		@Override
		public Species getSpecies(REpiceaSpeciesCompliantObject caller) {return species;}

		@Override
		public boolean isGoingToBeHarvested() {
			return dummyHarvest;
		}

		@Override
		public int getDateYr() {return dateYr;}

		@Override
		public boolean isFromPlantation() {return planted;}

		@Override
		public String getId() {return null;}

		@Override
		public String getClusterId() {return clusterId;}

		@Override
		public double getNumberOfStemsHa() {
			return stemDensityHa;
		}

		@Override
		public double getMeanAnnualTemperatureCelsius(REpiceaClimateVariableInformation arg0) {return meanTair;}

		@Override
		public double getTotalAnnualPrecipitationMm(REpiceaClimateVariableInformation arg0) {return totalPrcp;}

	}


	private static Map<Species, List<Trillium2026TreeImpl>> TreeMap;

	@BeforeClass
	public static void readTrees() throws IOException {
		TreeMap = new LinkedHashMap<Species, List<Trillium2026TreeImpl>>();
		String filename = ObjectUtility.getPackagePath(Trillium2026MortalityTest.class) + "mortalityTestData.csv";
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				String dataset = record[reader.getHeader().getIndexOfThisField("dataset")].toString();
				String plotKey = record[reader.getHeader().getIndexOfThisField("PlotKey")].toString();
				String clusterId = dataset.trim() + "_" + plotKey.trim();
				int growthStepLengthYr = Integer.parseInt(record[reader.getHeader().getIndexOfThisField("dt")].toString());
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
				double stemDensityHa = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("N_TOT")].toString());
				double meanTair = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanTair")].toString());
				double totalPrcp = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("TotalPrcp")].toString());
				
				Trillium2026TreeImpl tree = new Trillium2026TreeImpl(clusterId,
						growthStepLengthYr,
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
						planted,
						stemDensityHa,
						meanTair,
						totalPrcp);
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
	public void test02StochasticPredictions() {
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

	@Test
	public void test03CholeskyDecompositionVarianceCovarianceMatrix() {
		new Trillium2026MortalityPredictor(false); // to make sure the static maps are populated
		Map<Species, List<Double>> oMap = Trillium2026MortalityPredictor.VCovLists;
		for (Species sp : oMap.keySet()) {
			try {
				SymmetricMatrix omega = new Matrix(oMap.get(sp)).squareSym();
				omega.getLowerCholTriangle();
			} catch (Exception e) {
				Assert.fail("Unable to compute the Cholesky decomposition of species: " + sp.getLatinName());
			}
		}
	}

}
