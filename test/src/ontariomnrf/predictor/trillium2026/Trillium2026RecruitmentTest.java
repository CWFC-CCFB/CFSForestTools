/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2020-2023 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import canforservutility.occupancyindex.OccupancyIndexCalculator;
import ontariomnrf.predictor.trillium2026.Trillium2026RecruitmentPlotImpl.Mode;
import repicea.io.javacsv.CSVHeader;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class Trillium2026RecruitmentTest {

	private static Map<String, Trillium2026RecruitmentPlotImpl> PlotMapForOccurrence;
	private static Map<String, Trillium2026RecruitmentPlotImpl> PlotMapForNumber;
	private static Map<String, Trillium2026RecruitmentPlotImpl> PlotMapForDiameter;
	
	private static OccupancyIndexCalculator OccIndCalc;


	private static void createOrUpdatePlotFromRecord(Object[] record, CSVHeader header, Map<String, Trillium2026RecruitmentPlotImpl> oMap) {
		String plotId = record[header.getIndexOfThisField("uniquePlotID")].toString();
		double latitudeDeg = Double.parseDouble(record[header.getIndexOfThisField("latitudeDeg")].toString());
		double longitudeDeg = Double.parseDouble(record[header.getIndexOfThisField("longitudeDeg")].toString());
		String speciesName = record[header.getIndexOfThisField("speciesGr")].toString();
		Species species = Trillium2026RecruitmentOccurrencePredictor.getTrillium2026SpeciesFromLatinName(speciesName);
		int dateYr = ((Number) Double.parseDouble(record[header.getIndexOfThisField("year.x")].toString())).intValue();
		int growthStepYr = Integer.parseInt(record[header.getIndexOfThisField("dt")].toString());
		double basalAreaM2HaConiferous = Double.parseDouble(record[header.getIndexOfThisField("G_R")].toString());
		double basalAreaM2HaBroadleaved = Double.parseDouble(record[header.getIndexOfThisField("G_F")].toString());
		double gSpGr = Double.parseDouble(record[header.getIndexOfThisField("G_SpGr")].toString());
		double occIndex25km = Double.parseDouble(record[header.getIndexOfThisField("occIndex25km")].toString());
		double dd = Double.parseDouble(record[header.getIndexOfThisField("DD")].toString());
		double lowestTmin = Double.parseDouble(record[header.getIndexOfThisField("LowestTmin")].toString());
		double meanTminJanuary = Double.parseDouble(record[header.getIndexOfThisField("MeanTminJanuary")].toString());
		double prcp = Double.parseDouble(record[header.getIndexOfThisField("TotalPrcp")].toString());
		double prcpMarchMay = Double.parseDouble(record[header.getIndexOfThisField("TotalPrecMarchToMay")].toString());
		double prcpJuneAug = Double.parseDouble(record[header.getIndexOfThisField("TotalPrecJuneToAugust")].toString());
		double highestTmax = Double.parseDouble(record[header.getIndexOfThisField("HitghestTmax")].toString());
		double frostDays = Double.parseDouble(record[header.getIndexOfThisField("FrostFreeDay")].toString());
		double slopePct = Double.parseDouble(record[header.getIndexOfThisField("slopePct_PDEM_mean")].toString());
		int wasHarvested = Integer.parseInt(record[header.getIndexOfThisField("wasHarvested")].toString());
		int isHarvested = Integer.parseInt(record[header.getIndexOfThisField("isHarvested")].toString());
		int indexMeanAnnualTemp = header.getIndexOfThisField("MeanTair");
		double meanAnnualTemperature = indexMeanAnnualTemp != -1 ? 
				Double.parseDouble(record[indexMeanAnnualTemp].toString()) :
					0;
		int indexMeanMaxJulyTemp = header.getIndexOfThisField("MeanTmaxJuly");
		double meanMaxJulyTemp = indexMeanMaxJulyTemp != -1 ? 
				Double.parseDouble(record[indexMeanMaxJulyTemp].toString()) :
					0;
		int indexMeanTempJulyToAugust = header.getIndexOfThisField("MeanTempJuneToAugust");
		double meanTempJulyToAugust = indexMeanTempJulyToAugust != -1 ?
				Double.parseDouble(record[indexMeanTempJulyToAugust].toString()) :
					0;
		double pred = Double.parseDouble(record[header.getIndexOfThisField("pred")].toString());
		int indexAreaM2 = header.getIndexOfThisField("areaM2.x");
		double areaM2 = indexAreaM2 != -1 ?
				Double.parseDouble(record[indexAreaM2].toString()) :
					400;
		int indexStemDensity = header.getIndexOfThisField("N_TOT");
		double stemDensityHa = indexStemDensity != -1 ?
				Double.parseDouble(record[indexStemDensity].toString()) :
					0;
		String uniqueId = plotId + "_" + dateYr;
		if (oMap.containsKey(uniqueId)) {
			oMap.get(uniqueId).update(species, gSpGr, occIndex25km, pred);
		} else {
			Trillium2026RecruitmentPlotImpl plot = new Trillium2026RecruitmentPlotImpl(plotId,
					latitudeDeg,
					longitudeDeg,
					growthStepYr,
					basalAreaM2HaConiferous,
					basalAreaM2HaBroadleaved,
					dateYr,
					dd,
					prcp,
					frostDays,
					lowestTmin,
					meanTminJanuary,
					prcpMarchMay,
					prcpJuneAug,
					highestTmax,
					species,
					gSpGr,
					slopePct,
					wasHarvested == 1,
					isHarvested == 1,
					occIndex25km,
					meanAnnualTemperature,
					meanMaxJulyTemp,
					meanTempJulyToAugust,
					areaM2,
					stemDensityHa,
					pred,
					OccIndCalc);
			oMap.put(uniqueId, plot);
		}
	}


	@BeforeClass
	public static void initialize() throws IOException {
		OccIndCalc = new OccupancyIndexCalculator(Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex(), 25, null);
		OccIndCalc.registerPlots(Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex());

		PlotMapForOccurrence = new HashMap<String, Trillium2026RecruitmentPlotImpl>();
		String filename = ObjectUtility.getPackagePath(Trillium2026RecruitmentTest.class) + "0_RecruitmentOccurrenceValidationDataset.csv";
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		while ((record = reader.nextRecord()) != null) {
			createOrUpdatePlotFromRecord(record, reader.getHeader(), PlotMapForOccurrence);
		}
		reader.close();

		PlotMapForNumber = new HashMap<String, Trillium2026RecruitmentPlotImpl>(); 
		filename = ObjectUtility.getPackagePath(Trillium2026RecruitmentTest.class) + "0_RecruitmentNumberValidationDataset.csv";
		reader = new CSVReader(filename);
		while ((record = reader.nextRecord()) != null) {
			createOrUpdatePlotFromRecord(record, reader.getHeader(), PlotMapForNumber);
		}
		reader.close();
		
		PlotMapForDiameter = new HashMap<String, Trillium2026RecruitmentPlotImpl>();
		filename = ObjectUtility.getPackagePath(Trillium2026RecruitmentTest.class) + "0_RecruitmentDiameterValidationDataset.csv";
		reader = new CSVReader(filename);
		while ((record = reader.nextRecord()) != null) {
			createOrUpdatePlotFromRecord(record, reader.getHeader(), PlotMapForDiameter);
		}
//		Map<String, Trillium2026RecruitmentPlotImpl> oMap = PlotMapForDiameter;
		reader.close();
	}



	/*
	 * Validation test for occurrence using R validation dataset
	 */
	@Test
	public void test01OccurrencePredictionsAgainstRPredictions() throws IOException {
		System.out.println("Testing deterministic predictions against ground truth...");
		Trillium2026RecruitmentOccurrencePredictor predictor = new Trillium2026RecruitmentOccurrencePredictor(false, 0d); // deterministic
		Map<String, Trillium2026RecruitmentPlotImpl> plots = PlotMapForOccurrence; 
		for (Species sp : Trillium2026RecruitmentOccurrencePredictor.SpeciesList) {
			int nbTested = 0;
			for (Trillium2026RecruitmentPlotImpl plot : plots.values()) {
				Double expected = plot.getPred(sp);
				if (expected != null) {
					Trillium2026Tree tree = plot.getTreeInstance(sp);
					double actual = predictor.predictEventProbability(plot, tree);
					Assert.assertEquals("Testing probability for plot " + plot.getSubjectId() + ", species " + tree.getTrillium2026TreeSpecies().name(), 
							expected, 
							actual, 
							1E-8);
					nbTested++;
				}
			}
			System.out.println("    Species " + sp.getLatinName() + "; Number of successfully tested plots = " + nbTested + " / " + plots.size());
		}
	}

	/*
	 * Validation test for occurrence using R validation dataset.
	 * This test can only be put in place if the original list of plots is used to
	 * estimate the occupancy index.
	 */
	@Test
	public void test02OccupancyIndexCalculation() throws IOException {
		System.out.println("Testing occupancy indices...");
		Map<String, Trillium2026RecruitmentPlotImpl> plots = PlotMapForOccurrence; 
//		OccupancyIndexCalculator occIndCalc = new OccupancyIndexCalculator(Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex(), true);
//		occIndCalc.registerPlots(Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex());
		for (Species sp : Trillium2026RecruitmentOccurrencePredictor.SpeciesList) {
			int nbTested = 0;
			for (Trillium2026RecruitmentPlotImpl p : plots.values()) {
				Double pred = p.getPred(sp);
				if (pred != null) {
					GaussianEstimate estimatedOccIndex = OccIndCalc.getOccupancyIndex(p, sp);
					double actual = estimatedOccIndex.getMean().getValueAt(0, 0);
					double expected = (Double) p.getOccupancyForThisSpecies(sp);
					Assert.assertEquals("Testing occupancy index for plot " + p.getSubjectId() + ", species " + sp.name(), 
							expected, 
							actual, 
							1E-8);
					nbTested++;
				}
			}
			System.out.println("    Species " + sp.getLatinName() + "; Number of successfully tested plots = " + nbTested + " / " + plots.size());
		}
	}

	/*
	 * Validation test for stochastic implementation of occurrence part with unknown occupancy index.
	 */
	@Test
	public void test03StochasticImplementationOccurrencePredictions() throws IOException {
		System.out.println("Testing stochastic implementation of occurrence...");
		int nbRealizations = 10000;
		Trillium2026RecruitmentOccurrencePredictor predictor = new Trillium2026RecruitmentOccurrencePredictor(false, 0d); 
		Map<String, Trillium2026RecruitmentPlotImpl> plots = PlotMapForOccurrence; 
		for (Species sp : Trillium2026RecruitmentOccurrencePredictor.SpeciesList) {
			System.out.println("  Processing species " + sp.getLatinName() + "..."); 
			Trillium2026RecruitmentPlotImpl plot = null;
			for (Trillium2026RecruitmentPlotImpl p : plots.values()) {
				if (p.getBasalAreaM2HaForThisSpecies(sp) > 0) {
					plot = p;
					break;
				}
			}
			if (plot == null) {
				Assert.fail("Should have found a plot with basal area of species greater than 0!");
			}
			plot.setMode(Mode.Estimated);
			double detPred = predictor.predictEventProbability(plot, plot.getTreeInstance(sp));
			plot.setMode(Mode.Deviate);
			Matrix realizations = new Matrix(nbRealizations, 1);
			for (int j = 0; j < nbRealizations; j++) {
				plot.setMonteCarloRealizationId(j);
				realizations.setValueAt(j, 0, predictor.predictEventProbability(plot, plot.getTreeInstance(sp)));
			}
			plot.setMode(Mode.Known);
			double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
			System.out.println("       Expected mean = " + detPred + " Actual mean = " + meanStoPred);
			Assert.assertEquals("Testing stochastic mean against deterministic mean " + plot.getSubjectId() + ", species " + plot.getTreeInstance(sp).getTrillium2026TreeSpecies().name(), 
					detPred, 
					meanStoPred, 
					0.03);
		}
		
	}

	/*
	 * Validation test for number of recruits using R validation dataset with known occupancy index.
	 */
	@Test
	public void test11MeanNumberPredictionsAgainstRPredictions() throws IOException {
		System.out.println("Testing predicted abundance...");
		Trillium2026RecruitmentNumberPredictor predictor = new Trillium2026RecruitmentNumberPredictor(false); // deterministic
		Map<String, Trillium2026RecruitmentPlotImpl> plotMap = PlotMapForNumber; 
		for (Species sp : Trillium2026RecruitmentOccurrencePredictor.SpeciesList) {
			System.out.println("  Processing species " + sp.getLatinName() + "...");
			int nbTested = 0;
			for (Trillium2026RecruitmentPlotImpl plot : plotMap.values()) {
				if (plot.getPred(sp) != null) {
					Trillium2026Tree tree = plot.getTreeInstance(sp);
					double actual = predictor.predictNumberOfRecruits(plot, tree.getTrillium2026TreeSpecies());
					double expected = plot.getPred(sp) + 1d; // adding one because 
					Assert.assertEquals("Testing mean predicted number for plot " + plot.getSubjectId() + ", species " + tree.getTrillium2026TreeSpecies().name(), 
							expected, 
							actual, 
							1E-8);
					nbTested++;
				}
			}
			System.out.println("      Number of successfully tested plots = " + nbTested);
		}
	}


	/*
	 * Validation test for stochastic implementation with known occupancy index.
	 */
	@Test
	public void test12StochasticMeanNumberPredictions() throws IOException {
		System.out.println("Testing stochastic abundance (residual only)...");
		Trillium2026RecruitmentNumberPredictor detPredictor = new Trillium2026RecruitmentNumberPredictor(false); // deterministic
		Trillium2026RecruitmentNumberPredictor stoPredictor = new Trillium2026RecruitmentNumberPredictor(false, true); // stochastic with residual variability only
		int nbRealizations = 1000000;
		Map<String, Trillium2026RecruitmentPlotImpl> plots = PlotMapForNumber; 
		for (Species sp : Trillium2026RecruitmentOccurrencePredictor.SpeciesList) {
			System.out.println(" Processing species " + sp.getLatinName() + "...");
			Trillium2026RecruitmentPlotImpl selectedPlot = null;
			for (Trillium2026RecruitmentPlotImpl p : plots.values()) {
				if (p.getBasalAreaM2HaForThisSpecies(sp) > 0) {
					selectedPlot = p;
					break;
				}
			}
			if (selectedPlot == null) {
				throw new UnsupportedOperationException("No plots were selected!");
			}
			Trillium2026Tree tree = selectedPlot.getTreeInstance(sp);
			double detPred = detPredictor.predictNumberOfRecruits(selectedPlot, tree.getTrillium2026TreeSpecies());
			Matrix realizations = new Matrix(nbRealizations, 1);
			for (int j = 0; j < nbRealizations; j++) {
				realizations.setValueAt(j, 0, stoPredictor.predictNumberOfRecruits(selectedPlot, tree.getTrillium2026TreeSpecies()));
			}
			double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
			Matrix diff = realizations.scalarAdd(-meanStoPred);
			Matrix ssq = diff.transpose().multiply(diff);
			double variance = ssq.getValueAt(0, 0) / (realizations.m_iRows - 1);
			double invThetaParmEst = stoPredictor.getInvThetaParameterEstimate(tree.getTrillium2026TreeSpecies());
			double expectedVariance = (detPred - 1) + invThetaParmEst * (detPred - 1) * (detPred - 1);
			System.out.println("Expected mean = " + detPred + " Actual mean = " + meanStoPred);
			Assert.assertEquals("Testing stochastic mean against deterministic mean " + selectedPlot.getSubjectId() + ", species " + tree.getTrillium2026TreeSpecies().name(), 
					0, 
					1 - meanStoPred/detPred, 
					0.005);
			System.out.println("Expected variance = " + expectedVariance + " Actual variance = " + variance);
			Assert.assertEquals("Testing stochastic variance against expected variance " + selectedPlot.getSubjectId() + ", species " + tree.getTrillium2026TreeSpecies().name(), 
					0,
					1 - variance/expectedVariance, 
					0.03);
		}
	}

	/*
	 * Validation test for stochastic implementation of occurrence part with unknown occupancy index.
	 */
	@Test
	public void test13StochasticImplementationMeanNumberPredictions() throws IOException {
		System.out.println("Testing stochastic abundance (with estimated occupancy)...");
		Trillium2026RecruitmentNumberPredictor predictor = new Trillium2026RecruitmentNumberPredictor(false); // deterministic
		int nbRealizations = 10000;
		Map<String, Trillium2026RecruitmentPlotImpl> plotMap = PlotMapForNumber; 

		for (Species sp : Trillium2026RecruitmentOccurrencePredictor.SpeciesList) {
			if (predictor.getInternalPredictor(sp).isModelUsingOccupancyIndex()) {
				System.out.println("  Processing species " + sp.getLatinName() + "...");
				Trillium2026RecruitmentPlotImpl selectedPlot = null;
				for (Trillium2026RecruitmentPlotImpl p : plotMap.values()) {
					if (p.getBasalAreaM2HaForThisSpecies(sp) > 0) {
						selectedPlot = p;
						break;
					}
				}
				if (selectedPlot == null) {
					throw new UnsupportedOperationException("No plots were selected!");
				}
				selectedPlot.setMode(Mode.Estimated);
				double detPred = predictor.predictNumberOfRecruits(selectedPlot, sp);

				selectedPlot.setMode(Mode.Deviate);
				Matrix realizations = new Matrix(nbRealizations, 1);
				for (int j = 0; j < nbRealizations; j++) {
					selectedPlot.setMonteCarloRealizationId(j);
					realizations.setValueAt(j, 0, predictor.predictNumberOfRecruits(selectedPlot, sp));
				}
				selectedPlot.setMode(Mode.Known);
				
				double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
				System.out.println("   Expected mean = " + detPred + " Actual mean = " + meanStoPred);
				Assert.assertEquals("Testing stochastic mean against deterministic mean " + selectedPlot.getSubjectId() + ", species " + sp.name(), 
						detPred, 
						meanStoPred, 
						3E-2);
				
			}
		}
	}

	/*
	 * Validation test for recruit diameter using R validation dataset.
	 */
	@Test
	public void test21MeanDiameterPredictionsAgainstRPredictions() throws IOException {
		System.out.println("Testing predicted diameter...");
		Trillium2026RecruitDiameterInternalPredictor.EnableCutPoint = false;
		Trillium2026RecruitDiameterPredictor predictor = new Trillium2026RecruitDiameterPredictor(false);
		Map<String, Trillium2026RecruitmentPlotImpl> plotMap = PlotMapForDiameter; 
		for (Species sp : Trillium2026RecruitmentOccurrencePredictor.SpeciesList) {
			System.out.println("  Processing species " + sp.getLatinName() + "...");
			int nbTested = 0;
			for (Trillium2026RecruitmentPlotImpl plot : plotMap.values()) {
				if (plot.getPred(sp) != null) {
					double expected = plot.getPred(sp); 
					double actual = predictor.predictRecruitDiameterCm(plot, sp);
					Assert.assertEquals("Testing mean predicted number for plot " + plot.getSubjectId() + ", species " + sp.name() + " at nbtested = " + nbTested, 
							expected * .1 + 9.09, 
							actual, 
							1E-8);
					nbTested++;
				}
			}
			System.out.println("      Number of successfully tested plots = " + nbTested);
		}
		Trillium2026RecruitDiameterInternalPredictor.EnableCutPoint = true;
	}
	
	/*
	 * Comparison between stochastic and deterministic predictions.
	 */
	@Test
	public void test22MeanDiameterStochasticPredictions() throws IOException {
		System.out.println("Testing predicted diameter (stochastic)...");
		Trillium2026RecruitDiameterInternalPredictor.EnableCutPoint = false;
		Trillium2026RecruitDiameterPredictor detPredictor = new Trillium2026RecruitDiameterPredictor(false); 
		Trillium2026RecruitDiameterPredictor stoPredictor = new Trillium2026RecruitDiameterPredictor(false, true); // false to disable the parameters in the parameter estimates
		Map<String, Trillium2026RecruitmentPlotImpl> plotMap = PlotMapForDiameter; 
		for (Species sp : Trillium2026RecruitmentOccurrencePredictor.SpeciesList) {
			System.out.println("  Processing species " + sp.getLatinName() + "...");
			for (Trillium2026RecruitmentPlotImpl plot : plotMap.values()) {
				if (plot.getPred(sp) != null) {
					double expected = detPredictor.predictRecruitDiameterCm(plot, sp);
					MonteCarloEstimate estimate = new MonteCarloEstimate();
					for (int i = 0; i < 100000; i++) {
						plot.setMonteCarloRealizationId(0);
						double predDbhCm = stoPredictor.predictRecruitDiameterCm(plot, sp);
						estimate.addRealization(new Matrix(1,1, predDbhCm, 0));
					}
					double actual = estimate.getMean().getValueAt(0,0);
					double expectedVariance = stoPredictor.getVariance(plot, sp);
					double actualVariance = estimate.getVariance().getValueAt(0, 0);
					System.out.println("      Deterministic mean = " + expected + "; stochastic mean = " + actual);
					Assert.assertEquals("Testing stochastic mean against deterministic one for plot " + plot.getSubjectId() + ", species " + sp.name(), 
							expected, 
							actual, 
							0.05);
					double ratio = Math.abs(1 - actualVariance / expectedVariance);
					System.out.println("      Expected variance = " + expectedVariance + "; stochastic variance = " + actualVariance + "; ratio diff = " + ratio);
					Assert.assertTrue("Testing variance " + plot.getSubjectId() + ", species " + sp.name(), ratio < 0.05);
					break;
				}
			}
		}
		Trillium2026RecruitDiameterInternalPredictor.EnableCutPoint = true;
	}



	@AfterClass
	public static void cleanup() {
		if (PlotMapForOccurrence != null) {
			PlotMapForOccurrence.clear();
		}
		if (PlotMapForNumber != null) {
			PlotMapForNumber.clear();
		}
		OccIndCalc = null;
	}


}
