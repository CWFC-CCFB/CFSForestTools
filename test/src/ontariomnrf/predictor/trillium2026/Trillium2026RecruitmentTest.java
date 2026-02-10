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
import repicea.util.ObjectUtility;

public class Trillium2026RecruitmentTest {
	
	private static Map<Species, List<Trillium2026RecruitmentPlotImplWithKnownOccupancy>> TestPlotListForOccurrences;
//	private static List<IrisCompatibleTestPlotImpl> TestPlotListForNumbers;
	private static Map<Species, List<Trillium2026RecruitmentPlotImpl>> StandardPlotMap;
	private static List<Trillium2026RecruitmentPlotImpl> StandardPlotList;
	
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
		double frostDays = Double.parseDouble(record[header.getIndexOfThisField("FrostFreeDay")].toString());
		double pred = Double.parseDouble(record[header.getIndexOfThisField("pred")].toString());
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
				species,
				pred, 
				gSpGr,
				occIndex25km);
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
//		double occIndex25km = Double.parseDouble(record[header.getIndexOfThisField("occIndex25km")].toString());
		double dd = Double.parseDouble(record[header.getIndexOfThisField("DD")].toString());
		double lowestTmin = Double.parseDouble(record[header.getIndexOfThisField("LowestTmin")].toString());
		double meanTminJanuary = Double.parseDouble(record[header.getIndexOfThisField("MeanTminJanuary")].toString());
		double prcp = Double.parseDouble(record[header.getIndexOfThisField("TotalPrcp")].toString());
		double prcpMarchMay = Double.parseDouble(record[header.getIndexOfThisField("TotalPrecMarchToMay")].toString());
		double frostDays = Double.parseDouble(record[header.getIndexOfThisField("FrostFreeDay")].toString());
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
				species,
				gSpGr,
				(List) StandardPlotList);
		return plot;
	}

	
	@BeforeClass
	public static void initialize() throws IOException {
		TestPlotListForOccurrences = new HashMap<Species, List<Trillium2026RecruitmentPlotImplWithKnownOccupancy>>();
		StandardPlotMap = new HashMap<Species, List<Trillium2026RecruitmentPlotImpl>>();
		StandardPlotList = new ArrayList<Trillium2026RecruitmentPlotImpl>();
		String filename = ObjectUtility.getPackagePath(Trillium2026RecruitmentTest.class) + "0_RecruitmentOccurrenceValidationDataset.csv";
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		while ((record = reader.nextRecord()) != null) {
			Trillium2026RecruitmentPlotImplWithKnownOccupancy plotWithKnownOccupancy =  createTestPlotFromRecord(record, reader.getHeader());
			if (!TestPlotListForOccurrences.containsKey(plotWithKnownOccupancy.species)) {
				TestPlotListForOccurrences.put(plotWithKnownOccupancy.species, new ArrayList<Trillium2026RecruitmentPlotImplWithKnownOccupancy>());
			}
			TestPlotListForOccurrences.get(plotWithKnownOccupancy.species).add(plotWithKnownOccupancy);
			
			Trillium2026RecruitmentPlotImpl standardPlot = createStandardPlotFromRecord(record, reader.getHeader());
			if (!StandardPlotMap.containsKey(standardPlot.species)) {
				StandardPlotMap.put(standardPlot.species, new ArrayList<Trillium2026RecruitmentPlotImpl>());
			}
			StandardPlotMap.get(standardPlot.species).add(standardPlot);
			StandardPlotList.add(standardPlot);
		}
		reader.close();
		
//		TestPlotListForNumbers = new ArrayList<IrisCompatibleTestPlotImpl>();
//		filename = ObjectUtility.getPackagePath(Trillium2026RecruitmentTest.class) + "0_RecruitmentNumberValidationDataset.csv";
//		reader = new CSVReader(filename);
//		while ((record = reader.nextRecord()) != null) {
//			TestPlotListForNumbers.add(createTestPlotFromRecord(record));
//		}
//		reader.close();
	}

	
	
	/*
	 * Validation test for occurrence using R validation dataset
	 */
	@Test
	public void test01OccurrencePredictionsAgainstRPredictions() throws IOException {
		Trillium2026RecruitmentOccurrencePredictor predictor = new Trillium2026RecruitmentOccurrencePredictor(false, null); // deterministic
		Map<Species, List<Trillium2026RecruitmentPlotImplWithKnownOccupancy>> plots = TestPlotListForOccurrences; 
		for (Species sp : plots.keySet()) {
			int nbTested = 0;
			List<Trillium2026RecruitmentPlotImplWithKnownOccupancy> innerList = plots.get(sp);
			for (Trillium2026RecruitmentPlotImplWithKnownOccupancy plot : innerList) {
				Trillium2026Tree tree = plot.getTreeInstance();
				double actual = predictor.predictEventProbability(plot, tree);
				double expected = plot.getPredProb();
				if (Math.abs(actual-expected) > 1E-8) {	
					@SuppressWarnings("unused")
					int u = 0;
				}
				Assert.assertEquals("Testing probability for plot " + plot.getSubjectId() + ", species " + tree.getTrillium2026TreeSpecies().name(), 
						expected, 
						actual, 
						1E-8);
				nbTested++;
			}
			System.out.println("Species " + sp.getLatinName() + "; Number of successfully tested plots = " + nbTested + " / " + innerList.size());
		}
	}

	
//	/*
//	 * Validation test for number of recruits using R validation dataset with known occupancy index.
//	 */
//	@Test
//	public void test02MeanNumberPredictionsAgainstRPredictions() throws IOException {
//		IrisRecruitmentNumberPredictor predictor = new IrisRecruitmentNumberPredictor(false, 
//				new IrisRecruitmentOccurrencePredictor(false, null)); // deterministic
//		List<IrisRecruitmentPlotImplWithKnownOccupancy> plots = TestPlotListForNumbers; 
//		int nbTested = 0;
//		for (IrisRecruitmentPlotImplWithKnownOccupancy plot : plots) {
//			IrisTree tree = plot.getTreeInstance();
//			double actual = predictor.predictNumberOfRecruits(plot, tree.getSpecies());
//			double expected = plot.getPredProb() + 1d; // adding one because 
//			Assert.assertEquals("Testing mean predicted number for plot " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
//					expected, 
//					actual, 
//					1E-8);
//			nbTested++;
//		}
//		System.out.println("Number of successfully tested plots = " + nbTested + " / " + plots.size());
//	}

//	/*
//	 * Validation test for stochastic implementation with known occupancy index.
//	 */
//	@Test
//	public void test03StochasticMeanNumberPredictions() throws IOException {
//		IrisRecruitmentNumberPredictor detPredictor = new IrisRecruitmentNumberPredictor(false,
//				new IrisRecruitmentOccurrencePredictor(false, null)); // deterministic
//		IrisRecruitmentNumberPredictor stoPredictor = new IrisRecruitmentNumberPredictor(false, false, true,
//				new IrisRecruitmentOccurrencePredictor(false, null)); // stochastic but with variability disabled for parameter estimates
//		int nbRealizations = 1000000;
//		List<IrisRecruitmentPlotImplWithKnownOccupancy> plots = TestPlotListForNumbers; 
//		IrisRecruitmentPlotImplWithKnownOccupancy plot = plots.get(0);
//		IrisTree tree = plot.getTreeInstance();
//		double detPred = detPredictor.predictNumberOfRecruits(plot, tree.getSpecies());
//		Matrix realizations = new Matrix(nbRealizations, 1);
//		for (int j = 0; j < nbRealizations; j++) {
//			realizations.setValueAt(j, 0, stoPredictor.predictNumberOfRecruits(plot, tree.getSpecies()));
//		}
//		double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
//		Matrix diff = realizations.scalarAdd(-meanStoPred);
//		Matrix ssq = diff.transpose().multiply(diff);
//		double variance = ssq.getValueAt(0, 0) / (realizations.m_iRows - 1);
//		double invThetaParmEst = stoPredictor.getInvThetaParameterEstimate(tree.getSpecies());
//		double expectedVariance = (detPred - 1) + invThetaParmEst * (detPred - 1) * (detPred - 1);
//		Assert.assertEquals("Testing stochastic mean against deterministic mean " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
//				detPred, 
//				meanStoPred, 
//				2E-3);
//		System.out.println("Expected mean = " + detPred + " Actual mean = " + meanStoPred);
//		Assert.assertEquals("Testing stochastic variance against expected variance " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
//				expectedVariance, 
//				variance, 
//				1E-2);
//		System.out.println("Expected variance = " + expectedVariance + " Actual variance = " + variance);
//	}
	
	
	
	/*
	 * Validation test for stochastic implementation of occurrence part with unknown occupancy index.
	 */
	@Test
	public void test04StochasticImplementationOccurrencePredictions() throws IOException {
		Map<Species, List<Trillium2026RecruitmentPlotImpl>> plots = StandardPlotMap; 
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Trillium2026RecruitmentOccurrencePredictor detPredictor = new Trillium2026RecruitmentOccurrencePredictor(false, (List) StandardPlotList); // deterministic
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Trillium2026RecruitmentOccurrencePredictor stoPredictor = new Trillium2026RecruitmentOccurrencePredictor(false, true, false, (List) StandardPlotList); // stochastic only in the occupancy index
		int nbRealizations = 50000;

		for (Species sp : StandardPlotMap.keySet()) {
			if (sp == Species.Pinus_banksiana) {
				int u = 0;
			}
			System.out.println("Processing species " + sp.getLatinName() + "..."); 
			Trillium2026RecruitmentPlotImpl plot = null;
			for (int i = 0; i < plots.get(sp).size(); i++) {
				Trillium2026RecruitmentPlotImpl tmpPlot = plots.get(sp).get(i);
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
					4E-3);
		}
	}

	
	
//	/*
//	 * Validation test for stochastic implementation of occurrence part with unknown occupancy index.
//	 */
//	@Test
//	public void test05StochasticImplementationMeanNumberPredictions() throws IOException {
//		List<Trillium2026RecruitmentPlotImpl> plots = StandardPlotList; 
//		@SuppressWarnings({ "unchecked", "rawtypes" })
//		IrisRecruitmentNumberPredictor detPredictor = new IrisRecruitmentNumberPredictor(false, false, false, new IrisRecruitmentOccurrencePredictor(false, (List) plots)); // deterministic
//		@SuppressWarnings({ "unchecked", "rawtypes" })
//		IrisRecruitmentNumberPredictor stoPredictor = new IrisRecruitmentNumberPredictor(false, true, false, new IrisRecruitmentOccurrencePredictor(false, (List) plots)); // stochastic only in the occupancy index
//		int nbRealizations = 10000;
//		
//		Trillium2026RecruitmentPlotImpl plot = plots.get(1200); // black spruce 
//		double detPred = detPredictor.predictNumberOfRecruits(plot, plot.getTreeInstance().getSpecies());
//		
//		Matrix realizations = new Matrix(nbRealizations, 1);
//		for (int j = 0; j < nbRealizations; j++) {
//			plot.setMonteCarloRealizationId(j);
//			realizations.setValueAt(j, 0, stoPredictor.predictNumberOfRecruits(plot, plot.getTreeInstance().getSpecies()));
//		}
//		double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
//		System.out.println("Expected mean = " + detPred + " Actual mean = " + meanStoPred);
//		Assert.assertEquals("Testing stochastic mean against deterministic mean " + plot.getSubjectId() + ", species " + plot.getTreeInstance().getSpecies().name(), 
//				detPred, 
//				meanStoPred, 
//				3E-2);
//	}

	
	
	
	@AfterClass
	public static void cleanup() {
		if (TestPlotListForOccurrences != null) {
			TestPlotListForOccurrences.clear();
		}
//		if (TestPlotListForNumbers != null) {
//			TestPlotListForNumbers.clear();
//		}
		if (StandardPlotList != null) {
			StandardPlotList.clear();
		}
	}


}
