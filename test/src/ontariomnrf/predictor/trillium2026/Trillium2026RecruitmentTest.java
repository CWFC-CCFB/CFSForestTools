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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import repicea.io.javacsv.CSVHeader;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

public class Trillium2026RecruitmentTest {

	private static Map<Species, List<Trillium2026RecruitmentPlotImplWithKnownOccupancy>> TestPlotListForOccurrences;
	private static Map<Species, List<Trillium2026RecruitmentPlotImplWithKnownOccupancy>> TestPlotListForNumbers;
	private static Map<Species, List<Trillium2026RecruitmentPlotImpl>> StandardPlotMapForOccurrence;
	private static Map<Species, List<Trillium2026RecruitmentPlotImpl>> StandardPlotMapForNumber;
	//	private static List<Trillium2026RecruitmentPlotImpl> StandardPlotList;

	private static Trillium2026RecruitmentPlotImplWithKnownOccupancy createTestPlotFromRecord(Object[] record, CSVHeader header) {
		String plotId = record[header.getIndexOfThisField("uniquePlotID")].toString();
		String speciesName = record[header.getIndexOfThisField("speciesGr")].toString();
		Species species = Trillium2026RecruitmentOccurrencePredictor.getTrillium2026SpeciesFromLatinName(speciesName);
		int dateYr = ((Number) Double.parseDouble(record[header.getIndexOfThisField("year.x")].toString())).intValue();
		double growthStepYr = Double.parseDouble(record[header.getIndexOfThisField("dt")].toString());
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
		double pred = Double.parseDouble(record[header.getIndexOfThisField("pred")].toString());

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

		Trillium2026RecruitmentPlotImplWithKnownOccupancy plot = new Trillium2026RecruitmentPlotImplWithKnownOccupancy(plotId,
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
				pred, 
				gSpGr,
				slopePct,
				wasHarvested == 1,
				isHarvested == 1,
				occIndex25km,
				meanAnnualTemperature,
				meanMaxJulyTemp);
		return plot;
	}


	private static Trillium2026RecruitmentPlotImpl createStandardPlotFromRecord(Object[] record, CSVHeader header) {
		String plotId = record[header.getIndexOfThisField("uniquePlotID")].toString();
		double latitudeDeg = Double.parseDouble(record[header.getIndexOfThisField("latitudeDeg")].toString());
		double longitudeDeg = Double.parseDouble(record[header.getIndexOfThisField("longitudeDeg")].toString());
		String speciesName = record[header.getIndexOfThisField("speciesGr")].toString();
		Species species = Trillium2026RecruitmentOccurrencePredictor.getTrillium2026SpeciesFromLatinName(speciesName);
		int dateYr = ((Number) Double.parseDouble(record[header.getIndexOfThisField("year.x")].toString())).intValue();
		double growthStepYr = Double.parseDouble(record[header.getIndexOfThisField("dt")].toString());
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

		//		double pred = Double.parseDouble(record[header.getIndexOfThisField("pred")].toString());

		@SuppressWarnings({ "unchecked", "rawtypes" })
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
				(List) StandardPlotMapForOccurrence.get(species),
				meanAnnualTemperature,
				meanMaxJulyTemp);
		return plot;
	}


	@BeforeClass
	public static void initialize() throws IOException {
		TestPlotListForOccurrences = new HashMap<Species, List<Trillium2026RecruitmentPlotImplWithKnownOccupancy>>();
		StandardPlotMapForOccurrence = new HashMap<Species, List<Trillium2026RecruitmentPlotImpl>>();
		String filename = ObjectUtility.getPackagePath(Trillium2026RecruitmentTest.class) + "0_RecruitmentOccurrenceValidationDataset.csv";
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		while ((record = reader.nextRecord()) != null) {
			Trillium2026RecruitmentPlotImplWithKnownOccupancy plotWithKnownOccupancy =  createTestPlotFromRecord(record, reader.getHeader());
			Species sp = plotWithKnownOccupancy.species;
			if (!TestPlotListForOccurrences.containsKey(sp)) {
				TestPlotListForOccurrences.put(sp, new ArrayList<Trillium2026RecruitmentPlotImplWithKnownOccupancy>());
			}
			if (!StandardPlotMapForOccurrence.containsKey(sp)) {
				StandardPlotMapForOccurrence.put(sp, new ArrayList<Trillium2026RecruitmentPlotImpl>());
			}
			TestPlotListForOccurrences.get(plotWithKnownOccupancy.species).add(plotWithKnownOccupancy);
			Trillium2026RecruitmentPlotImpl standardPlot = createStandardPlotFromRecord(record, reader.getHeader());
			StandardPlotMapForOccurrence.get(standardPlot.species).add(standardPlot);
		}
		reader.close();

		TestPlotListForNumbers = new HashMap<Species, List<Trillium2026RecruitmentPlotImplWithKnownOccupancy>>();
		StandardPlotMapForNumber = new HashMap<Species, List<Trillium2026RecruitmentPlotImpl>>(); 
		filename = ObjectUtility.getPackagePath(Trillium2026RecruitmentTest.class) + "0_RecruitmentNumberValidationDataset.csv";
		reader = new CSVReader(filename);
		while ((record = reader.nextRecord()) != null) {
			Trillium2026RecruitmentPlotImplWithKnownOccupancy plot = createTestPlotFromRecord(record, reader.getHeader());
			Species sp = plot.species;
			if (!TestPlotListForNumbers.containsKey(sp)) {
				TestPlotListForNumbers.put(sp, new ArrayList<Trillium2026RecruitmentPlotImplWithKnownOccupancy>());
			}
			if (!StandardPlotMapForNumber.containsKey(sp)) {
				StandardPlotMapForNumber.put(sp, new ArrayList<Trillium2026RecruitmentPlotImpl>());
			}
			TestPlotListForNumbers.get(sp).add(plot);
			Trillium2026RecruitmentPlotImpl standardPlot = createStandardPlotFromRecord(record, reader.getHeader());
			StandardPlotMapForNumber.get(standardPlot.species).add(standardPlot);
		}
		reader.close();
	}



	/*
	 * Validation test for occurrence using R validation dataset
	 */
	@Test
	public void test01OccurrencePredictionsAgainstRPredictions() throws IOException {
		System.out.println("Testing deterministic predictions against ground truth...");
		Trillium2026RecruitmentOccurrencePredictor predictor = new Trillium2026RecruitmentOccurrencePredictor(false, null); // deterministic
		Map<Species, List<Trillium2026RecruitmentPlotImplWithKnownOccupancy>> plots = TestPlotListForOccurrences; 
		for (Species sp : plots.keySet()) {
			int nbTested = 0;
			List<Trillium2026RecruitmentPlotImplWithKnownOccupancy> innerList = plots.get(sp);
			for (Trillium2026RecruitmentPlotImplWithKnownOccupancy plot : innerList) {
				Trillium2026Tree tree = plot.getTreeInstance();
				double actual = predictor.predictEventProbability(plot, tree);
				double expected = plot.getPredProb();
				Assert.assertEquals("Testing probability for plot " + plot.getSubjectId() + ", species " + tree.getTrillium2026TreeSpecies().name(), 
						expected, 
						actual, 
						1E-8);
				nbTested++;
			}
			System.out.println("    Species " + sp.getLatinName() + "; Number of successfully tested plots = " + nbTested + " / " + innerList.size());
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
		Map<Species, List<Trillium2026RecruitmentPlotImpl>> plots = StandardPlotMapForOccurrence; 
		for (Species sp : plots.keySet()) {
			List<Trillium2026RecruitmentPlotImpl> innerList = plots.get(sp);
			Trillium2026RecruitmentOccurrencePredictor predictor = new Trillium2026RecruitmentOccurrencePredictor(false, Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex()); // deterministic
			int nbTested = 0;
			for (Trillium2026RecruitmentPlotImpl plot : innerList) {
				Trillium2026Tree tree = plot.getTreeInstance();
				GaussianEstimate occIndex = predictor.getInternalPredictor(sp).getOccupancyIndex(plot, sp);
				double actual = occIndex.getMean().getValueAt(0, 0);
				double expected = plot.occupancyIndex;
				Assert.assertEquals("Testing occupancy index for plot " + plot.getSubjectId() + ", species " + tree.getTrillium2026TreeSpecies().name(), 
						expected, 
						actual, 
						1E-8);
				nbTested++;
			}
			System.out.println("    Species " + sp.getLatinName() + "; Number of successfully tested plots = " + nbTested + " / " + innerList.size());
		}
	}

	/*
	 * Validation test for stochastic implementation of occurrence part with unknown occupancy index.
	 */
	@Test
	public void test03StochasticImplementationOccurrencePredictions() throws IOException {
		System.out.println("Testing stochastic implementation of occurrence...");
		Map<Species, List<Trillium2026RecruitmentPlotImpl>> plots = StandardPlotMapForOccurrence; 
		for (Species sp : plots.keySet()) {
			List<Trillium2026RecruitmentPlotImpl> innerList = plots.get(sp);
			Trillium2026RecruitmentOccurrencePredictor detPredictor = new Trillium2026RecruitmentOccurrencePredictor(false, Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex()); // deterministic
			int nbRealizations = 10000;
			Trillium2026RecruitmentOccurrencePredictor stoPredictor = new Trillium2026RecruitmentOccurrencePredictor(false, true, false, Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex()); // stochastic only in the occupancy index
			System.out.println("Processing species " + sp.getLatinName() + "..."); 
			Trillium2026RecruitmentPlotImpl plot = null;
			for (int i = 0; i < plots.get(sp).size(); i++) {
				Trillium2026RecruitmentPlotImpl tmpPlot = innerList.get(i);
				if (tmpPlot.getBasalAreaM2HaForThisSpecies(sp) > 0) {
					plot = tmpPlot;
					break;
				}
			}

			if (plot == null) {
				Assert.fail("Should have found a plot with basal area of species greater than 0!");
			}
			double detPred = detPredictor.predictEventProbability(plot, plot.getTreeInstance());

			Matrix realizations = new Matrix(nbRealizations, 1);
			for (int j = 0; j < nbRealizations; j++) {
				plot.setMonteCarloRealizationId(j);
				realizations.setValueAt(j, 0, stoPredictor.predictEventProbability(plot, plot.getTreeInstance()));
			}
			double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
			System.out.println("       Expected mean = " + detPred + " Actual mean = " + meanStoPred);
			Assert.assertEquals("Testing stochastic mean against deterministic mean " + plot.getSubjectId() + ", species " + plot.getTreeInstance().getTrillium2026TreeSpecies().name(), 
					detPred, 
					meanStoPred, 
					2E-3);
		}
		
	}

	/*
	 * Validation test for number of recruits using R validation dataset with known occupancy index.
	 */
	@Test
	public void test11MeanNumberPredictionsAgainstRPredictions() throws IOException {
		Trillium2026RecruitmentNumberPredictor predictor = new Trillium2026RecruitmentNumberPredictor(false, 
				new Trillium2026RecruitmentOccurrencePredictor(false, null)); // deterministic
		Map<Species, List<Trillium2026RecruitmentPlotImplWithKnownOccupancy>> plotMap = TestPlotListForNumbers; 
		for (Species sp : plotMap.keySet()) {
			System.out.println("Processing species " + sp.getLatinName() + "...");
			int nbTested = 0;
			List<Trillium2026RecruitmentPlotImplWithKnownOccupancy> plots = plotMap.get(sp); 
			for (Trillium2026RecruitmentPlotImplWithKnownOccupancy plot : plots) {
				Trillium2026Tree tree = plot.getTreeInstance();
				double actual = predictor.predictNumberOfRecruits(plot, tree.getTrillium2026TreeSpecies());
				double expected = plot.getPredProb() + 1d; // adding one because 
				Assert.assertEquals("Testing mean predicted number for plot " + plot.getSubjectId() + ", species " + tree.getTrillium2026TreeSpecies().name(), 
						expected, 
						actual, 
						1E-8);
				nbTested++;
			}
			System.out.println("      Number of successfully tested plots = " + nbTested + " / " + plots.size());
		}
	}


	/*
	 * Validation test for stochastic implementation with known occupancy index.
	 */
	@Test
	public void test12StochasticMeanNumberPredictions() throws IOException {
		Trillium2026RecruitmentNumberPredictor detPredictor = new Trillium2026RecruitmentNumberPredictor(false,
				new Trillium2026RecruitmentOccurrencePredictor(false, null)); // deterministic
		Trillium2026RecruitmentNumberPredictor stoPredictor = new Trillium2026RecruitmentNumberPredictor(false, false, true,
				new Trillium2026RecruitmentOccurrencePredictor(false, null)); // stochastic but with variability disabled for parameter estimates and occupancy index
		int nbRealizations = 1000000;
		Map<Species, List<Trillium2026RecruitmentPlotImplWithKnownOccupancy>> plots = TestPlotListForNumbers; 
		for (Species sp : plots.keySet()) {
			System.out.println("Processing species " + sp.getLatinName() + "...");
			Trillium2026RecruitmentPlotImplWithKnownOccupancy selectedPlot = null;
			for (Trillium2026RecruitmentPlotImplWithKnownOccupancy p : plots.get(sp)) {
				if (p.getBasalAreaM2HaForThisSpecies(sp) > 0) {
					selectedPlot = p;
					break;
				}
			}
			if (selectedPlot == null) {
				throw new UnsupportedOperationException("No plots were selected!");
			}
			Trillium2026Tree tree = selectedPlot.getTreeInstance();
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
					0.01);
		}
	}

	/*
	 * Validation test for stochastic implementation of occurrence part with unknown occupancy index.
	 */
	@Test
	public void test13StochasticImplementationMeanNumberPredictions() throws IOException {
		Map<Species, List<Trillium2026RecruitmentPlotImpl>> plotMap = StandardPlotMapForNumber; 
		Trillium2026RecruitmentNumberPredictor detPredictor = new Trillium2026RecruitmentNumberPredictor(false, false, false, 
				new Trillium2026RecruitmentOccurrencePredictor(false, Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex())); // deterministic
		Trillium2026RecruitmentNumberPredictor stoPredictor = new Trillium2026RecruitmentNumberPredictor(false, true, false, 
				new Trillium2026RecruitmentOccurrencePredictor(false, Trillium2026RecruitmentOccurrencePredictor.getReferencePlotsForOccupancyIndex())); // stochastic only in the occupancy index
		int nbRealizations = 10000;

		for (Species sp : plotMap.keySet()) {
			if (detPredictor.getInternalPredictor(sp).hasOccupancyIndexAmongEffect()) {
				System.out.println("Processing species " + sp.getLatinName() + "...");
				Trillium2026RecruitmentPlotImpl selectedPlot = null;
				for (Trillium2026RecruitmentPlotImpl p : plotMap.get(sp)) {
					if (p.getBasalAreaM2HaForThisSpecies(sp) > 0) {
						selectedPlot = p;
						break;
					}
				}
				if (selectedPlot == null) {
					throw new UnsupportedOperationException("No plots were selected!");
				}
				double detPred = detPredictor.predictNumberOfRecruits(selectedPlot, selectedPlot.getTreeInstance().getTrillium2026TreeSpecies());

				Matrix realizations = new Matrix(nbRealizations, 1);
				for (int j = 0; j < nbRealizations; j++) {
					selectedPlot.setMonteCarloRealizationId(j);
					realizations.setValueAt(j, 0, stoPredictor.predictNumberOfRecruits(selectedPlot, selectedPlot.getTreeInstance().getTrillium2026TreeSpecies()));
				}
				double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
				System.out.println("Expected mean = " + detPred + " Actual mean = " + meanStoPred);
				Assert.assertEquals("Testing stochastic mean against deterministic mean " + selectedPlot.getSubjectId() + ", species " + selectedPlot.getTreeInstance().getTrillium2026TreeSpecies().name(), 
						detPred, 
						meanStoPred, 
						3E-2);
				
			}
		}
	}




	@AfterClass
	public static void cleanup() {
		if (TestPlotListForOccurrences != null) {
			TestPlotListForOccurrences.clear();
		}
		if (StandardPlotMapForOccurrence != null) {
			StandardPlotMapForOccurrence.clear();
		}
	}


}
