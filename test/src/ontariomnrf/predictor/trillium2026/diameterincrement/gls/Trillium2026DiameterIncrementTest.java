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
package ontariomnrf.predictor.trillium2026.diameterincrement.gls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.climate.REpiceaClimateVariableInformation;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class Trillium2026DiameterIncrementTest {

	static List<Species> SpeciesUsingSMI = new ArrayList<Species>();
	static void addSpecies(Species sp) {
		if (!SpeciesUsingSMI.contains(sp)) {
			SpeciesUsingSMI.add(sp);
		}
	}
	
	
	static class Trillium2026TreeImpl implements Trillium2026DiameterIncrementTree {

		class Trillium2026PlotImpl implements Trillium2026DiameterIncrementPlot {
			@Override
			public int getGrowthStepLengthYr() {return growthStepLengthYr;}


			@Override
			public double getMeanMinimumJanuaryTemperatureCelsius(REpiceaClimateVariableInformation resolution) {return meanTminJanuaryCelsius;}

			@Override
			public double getTotalPrecipitationFromMarchToMayMm(REpiceaClimateVariableInformation resolution) {return totalPrecMarchToMayMm;}

			@Override
			public double getMeanTemperatureFromJuneToAugustCelsius(REpiceaClimateVariableInformation resolution) {return meanTempJuneToAugustCelsius;}

			@Override
			public double getMeanVPDFromJuneToAugustHPa(REpiceaClimateVariableInformation info) {return meanSummerVPD;}

			@Override
			public double getMeanMaximumJulyTemperatureCelsius(REpiceaClimateVariableInformation info) {return meanTmaxJulyCelsius;}

			@Override
			public double getMeanAnnualSMIPercent(REpiceaClimateVariableInformation info) {
				addSpecies(species);
				return SMImean;
			}

			@Override
			public double getTotalPrecipitationFromJuneToAugustMm(REpiceaClimateVariableInformation info) {return totalPrecJuneToAugustMm;}

			@Override
			public double getMeanAnnualCMICm(REpiceaClimateVariableInformation info) {return CMI;}

			@Override
			public double getGrowingDegreeDaysCelsius(REpiceaClimateVariableInformation resolution) {return degreeDaysCelsius;}

			@Override
			public String getId() {return null;}

			@Override
			public boolean isGoingToBeHarvested() {return isGoingToBeHarvested;}

			@Override
			public double getBasalAreaM2Ha() {return basalAreaM2Ha;}

			@Override
			public double getNumberOfStemsHa() {return stemDensityHa;}


			@Override
			public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}


			@Override
			public int getMonteCarloRealizationId() {return mcReal;}


			@Override
			public String getSubjectId() {return "myPlot";}

		}
		
		
		
		
		private final int growthStepLengthYr;
		private final double meanTminJanuaryCelsius;
		private final double totalPrecMarchToMayMm;
		private final double meanTempJuneToAugustCelsius;
		private final double meanSummerVPD;
		private final double meanTmaxJulyCelsius;
		private final double SMImean;
		private final double totalPrecJuneToAugustMm;
		private final double CMI;
		private final double degreeDaysCelsius;
		private final double dbhCm;
		private final double BAL;
		private final double BAS;
		private final double basalAreaM2Ha;
		private final double stemDensityHa;
		private final Species species;
		protected final double pred;
		protected final double predTransformed;
		private final boolean isGoingToBeHarvested;
		private int mcReal;
		private final Trillium2026PlotImpl plot;
		protected int dateYr = 2000;
		
		Trillium2026TreeImpl(
				int growthStepLengthYr,
				double meanTminJanuaryCelsius,
				double totalPrecMarchToMayMm,
				double meanTempJuneToAugustCelsius,
				double meanSummerVPD,
				double meanTmaxJulyCelsius,
				double SMImean,
				double totalPrecJuneToAugustMm,
				double CMI,
				double degreeDaysCelsius,
				double dbhCm,
				double BAL,
				double BAS,
				double basalAreaM2Ha,
				double stemDensityHa, 
				Species species, 
				double pred,
				double predTransformed,
				int dateYr,
				boolean dummyHarvest) {
			this.growthStepLengthYr = growthStepLengthYr;
			this.meanTminJanuaryCelsius = meanTminJanuaryCelsius;
			this.totalPrecMarchToMayMm = totalPrecMarchToMayMm;
			this.meanTempJuneToAugustCelsius = meanTempJuneToAugustCelsius;
			this.meanSummerVPD = meanSummerVPD;
			this.meanTmaxJulyCelsius = meanTmaxJulyCelsius;
			this.SMImean = SMImean;
			this.totalPrecJuneToAugustMm = totalPrecJuneToAugustMm;
			this.CMI = CMI;
			this.degreeDaysCelsius = degreeDaysCelsius;
			this.dbhCm = dbhCm;
			this.BAL = BAL;
			this.BAS = BAS;
			this.basalAreaM2Ha = basalAreaM2Ha;
			this.stemDensityHa = stemDensityHa;
			this.species = species;
			this.pred = pred;
			this.predTransformed = predTransformed;
			this.isGoingToBeHarvested = dummyHarvest;
			this.plot = new Trillium2026PlotImpl();
		}
		
		@Override
		public String getSubjectId() {return "myTree";}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

		void setMonteCarloRealizationId(int real) {this.mcReal = real;}

		@Override
		public int getMonteCarloRealizationId() {return mcReal;}


		@Override
		public double getDbhCm() {return dbhCm;}

		@Override
		public double getBasalAreaLargerThanSubjectM2Ha() {return BAL;}

		@Override
		public Species getSpecies() {return species;}

		@Override
		public double getBasalAreaSmallerThanSubjectM2Ha() {return BAS;}

		@Override
		public int getErrorTermIndex() {return dateYr;}

	}

	
	private static Map<Species, List<Trillium2026TreeImpl>> TreeMap;

	@BeforeClass
	public static void readTrees() throws IOException {
		TreeMap = new LinkedHashMap<Species, List<Trillium2026TreeImpl>>();
		String filename = ObjectUtility.getPackagePath(Trillium2026DiameterIncrementTest.class) + "diamincTestData.csv";
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				int growthStepLengthYr = Integer.parseInt(record[reader.getHeader().getIndexOfThisField("dt")].toString());
				if (growthStepLengthYr > 1) {
					double meanTminJanuaryCelsius = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanTminJanuary")].toString());
					double totalPrecMarchToMayMm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("TotalPrecMarchToMay")].toString());
					double meanTempJuneToAugustCelsius = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanTempJuneToAugust")].toString());
					double meanSummerVPD = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanSummerVPD")].toString());
					double meanTmaxJulyCelsius = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("MeanTmaxJuly")].toString());
					double SMImean = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("SMImean")].toString());
					double totalPrecJuneToAugustMm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("TotalPrecJuneToAugust")].toString());
					double CMI = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("CMI")].toString());
					double degreeDaysCelsius = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("DD")].toString());
					double dbhCm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("DBH.x")].toString());
					int dummyHarvest = Integer.parseInt(record[reader.getHeader().getIndexOfThisField("dummyHarvest")].toString());
					double BAL = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("BAL")].toString());
					double BAS = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("BAS")].toString());

					double basalAreaM2Ha = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("G_TOT")].toString());
					double stemDensityHa = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("N_TOT")].toString());

					String speciesName = record[reader.getHeader().getIndexOfThisField("SpecGroup")].toString();
					Species species	= Trillium2026DiameterIncrementPredictor.getSpeciesFromString(speciesName);
					double pred = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("pred")].toString());
					double predTransformed = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("pred_backTrans")].toString());
					int dateYr = ((Double) Double.parseDouble(record[reader.getHeader().getIndexOfThisField("FieldSeasonYear.x")].toString())).intValue();
					Trillium2026TreeImpl tree = new Trillium2026TreeImpl(
							growthStepLengthYr,
							meanTminJanuaryCelsius,
							totalPrecMarchToMayMm,
							meanTempJuneToAugustCelsius,
							meanSummerVPD,
							meanTmaxJulyCelsius,
							SMImean,
							totalPrecJuneToAugustMm,
							CMI,
							degreeDaysCelsius,
							dbhCm,
							BAL,
							BAS,
							basalAreaM2Ha,
							stemDensityHa, 
							species, 
							pred,
							predTransformed,
							dateYr,
							dummyHarvest == 1);
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
		Trillium2026DiameterIncrementPredictor.BoundEnabled = false;
	}
	
	@Test
	public void test01DeterministicPredictionsOnTransformedScale() {
		Trillium2026DiameterIncrementPredictor diamIncPredictor = new Trillium2026DiameterIncrementPredictor(false); // deterministic
		diamIncPredictor.enableBackTransformation(false);
		for (Species species : TreeMap.keySet()) {
			System.out.print("Processing species " + species.name() + " - ");
			int nbTested = 0;
			for (Trillium2026TreeImpl t : TreeMap.get(species)) {
				double observed = diamIncPredictor.predictDiameterIncrementCm(t.plot, t);
				double expected = t.pred;
				Assert.assertEquals("Comparing predictions on transformed scale", expected, observed, 1E-8);
				nbTested++;
			}
			System.out.println("Nb trees successfully tested = " + nbTested);
		}
	}

	@Test
	public void test02DeterministicPredictionsOnOriginalScale() {
		Trillium2026DiameterIncrementPredictor diamIncPredictor = new Trillium2026DiameterIncrementPredictor(false); // deterministic
		for (Species species : TreeMap.keySet()) {
			System.out.print("Processing species " + species.name() + " - ");
			int nbTested = 0;
			for (Trillium2026TreeImpl t : TreeMap.get(species)) {
				double observed = diamIncPredictor.predictDiameterIncrementCm(t.plot, t);
				double expected = t.predTransformed;
				Assert.assertEquals("Comparing predictions on transformed scale", expected, observed, 1E-8);
				nbTested++;
			}
			System.out.println("Nb trees successfully tested = " + nbTested);
		}
	}

	@Test
	public void test03StochasticPredictions() {
		Trillium2026DiameterIncrementPredictor stoPredictor = new Trillium2026DiameterIncrementPredictor(false, true); // stochastic but parameter variability disabled
		Trillium2026DiameterIncrementPredictor detPredictor = new Trillium2026DiameterIncrementPredictor(false); // deterministic
		Trillium2026TreeImpl t = TreeMap.get(Species.Abies_balsamea).get(0);
		Matrix real;
		MonteCarloEstimate mcEstimate = new MonteCarloEstimate();
		for (int i = 0; i < 200000; i++) {
			t.setMonteCarloRealizationId(i);
			real = new Matrix(1,1);
			real.setValueAt(0, 0, stoPredictor.predictDiameterIncrementCm(t.plot, t));
			mcEstimate.addRealization(real);
		}
		double observed = mcEstimate.getMean().getValueAt(0, 0);
		double expected = detPredictor.predictDiameterIncrementCm(t.plot, t);
		System.out.println("Stochastic prediction = " +  observed + "; deterministic prediction = " + expected);
		Assert.assertEquals("Comparing stochastic and deterministic predictions", expected, observed, 2E-2);
		
	}

	@Test
	public void test04CholeskyDecompositionVarianceCovarianceMatrix() {
		new Trillium2026DiameterIncrementPredictor(false); // to make sure the static maps are populated
		Map<Species, SymmetricMatrix> oMap = Trillium2026DiameterIncrementPredictor.VCovMap;
		for (Species sp : oMap.keySet()) {
			try {
				oMap.get(sp).getLowerCholTriangle();
			} catch (Exception e) {
				Assert.fail("Unable to compute the Cholesky decomposition of species: " + sp.getLatinName());
			}
		}
	}
	
	@Test
	public void test05CorrelationStructure() {
		Species sp = Species.Abies_balsamea;
		Trillium2026DiameterIncrementPredictor predictor = new Trillium2026DiameterIncrementPredictor(false, true); // to make sure the static maps are populated
		Trillium2026TreeImpl t = TreeMap.get(sp).get(0);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		int initialDateYr = t.dateYr;
		for (int i = 0; i < 100000; i++) {
			t.setMonteCarloRealizationId(i);
			t.dateYr = initialDateYr;
			predictor.predictDiameterIncrementCm(t.plot, t);
			t.dateYr += 5;
			predictor.predictDiameterIncrementCm(t.plot, t);
			Matrix res = predictor.internalPredictorMap.get(sp).getResidualErrorForThisTree(t);
			estimate.addRealization(res);
		}
//		Matrix mean = estimate.getMean();
		SymmetricMatrix variance = estimate.getVariance();
		double expectedVariance = Trillium2026DiameterIncrementPredictor.ResVarMap.get(sp).getValueAt(0, 0);
		Assert.assertEquals("Testing variance 1", expectedVariance, variance.getValueAt(0, 0), 1E-2);
		Assert.assertEquals("Testing variance 2", expectedVariance, variance.getValueAt(1, 1), 1E-2);
		Matrix std = variance.diagonalVector().elementWisePower(0.5);
		Matrix fullVar = std.multiply(std.transpose());
		Matrix correlation = variance.elementWiseDivide(fullVar);
		double expectedCorrelation = Math.pow(Trillium2026DiameterIncrementPredictor.RhoMap.get(sp), 5);
		Assert.assertEquals("Testing correlation", expectedCorrelation, correlation.getValueAt(0, 1), 1E-2);
	}

	@Test
	public void test06WithoutCorrelationStructure() {
		Species sp = Species.Acer_pensylvanicum;
		Trillium2026DiameterIncrementPredictor predictor = new Trillium2026DiameterIncrementPredictor(false, true); // to make sure the static maps are populated
		Trillium2026TreeImpl t = TreeMap.get(sp).get(0);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		int initialDateYr = t.dateYr;
		for (int i = 0; i < 100000; i++) {
			t.setMonteCarloRealizationId(i);
			t.dateYr = initialDateYr;
			predictor.predictDiameterIncrementCm(t.plot, t);
			t.dateYr += 5;
			predictor.predictDiameterIncrementCm(t.plot, t);
			Matrix res = predictor.internalPredictorMap.get(sp).getResidualErrorForThisTree(t);
			estimate.addRealization(res);
		}
//		Matrix mean = estimate.getMean();
		SymmetricMatrix variance = estimate.getVariance();
		double expectedVariance = Trillium2026DiameterIncrementPredictor.ResVarMap.get(sp).getValueAt(0, 0);
		Assert.assertEquals("Testing variance 1", expectedVariance, variance.getValueAt(0, 0), 1E-2);
	}

	
	
	@AfterClass
	public static void cleanup() {
		System.out.println("Species using SMI: " + SpeciesUsingSMI);
		Trillium2026DiameterIncrementPredictor.BoundEnabled = true;
	}
}
