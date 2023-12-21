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
package canforservutility.predictor.iris.recruitment_v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import canforservutility.predictor.iris.recruitment_v1.IrisCompatiblePlot.DisturbanceType;
import canforservutility.predictor.iris.recruitment_v1.IrisCompatiblePlot.SoilDepth;
import canforservutility.predictor.iris.recruitment_v1.IrisCompatiblePlot.SoilTexture;
import canforservutility.predictor.iris.recruitment_v1.IrisCompatibleTree.IrisSpecies;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider.DrainageGroup;
import repicea.util.ObjectUtility;

public class IrisRecruitmentTest {
	
	private static final Map<String, DrainageGroup> DrainageGroupMatch = new HashMap<String, DrainageGroup>();
	static {
		DrainageGroupMatch.put("1xerique", DrainageGroup.Xeric);
		DrainageGroupMatch.put("2mesique", DrainageGroup.Mesic);
		DrainageGroupMatch.put("3subhydrique", DrainageGroup.Subhydric);
		DrainageGroupMatch.put("4hydrique", DrainageGroup.Hydric);
	}

	private static List<IrisCompatibleTestPlotImpl> TestPlotListForOccurrences;
	private static List<IrisCompatibleTestPlotImpl> TestPlotListForNumbers;
	private static List<IrisCompatiblePlotImpl> StandardPlotList;
	
	private static IrisCompatibleTestPlotImpl createTestPlotFromRecord(Object[] record) {
		String plotId = record[0].toString();
		String speciesName = record[4].toString();
		IrisSpecies species = IrisSpecies.valueOf(speciesName);
		int dateYr = Integer.parseInt(record[5].toString());
		double growthStepYr = Double.parseDouble(record[6].toString());
		double basalAreaM2HaConiferous = Double.parseDouble(record[7].toString());
		double basalAreaM2HaBroadleaved = Double.parseDouble(record[8].toString());
		double gSpGr = Double.parseDouble(record[9].toString());
		double occIndex10km = Double.parseDouble(record[10].toString());
		double dd = Double.parseDouble(record[11].toString());
		double prcp = Double.parseDouble(record[12].toString());
		double frostDays = Double.parseDouble(record[13].toString());
		double lowestTmin = Double.parseDouble(record[14].toString());
		String upcomingDistTypeStr = record[15].toString().substring(1);
		DisturbanceType upcomingDist = DisturbanceType.valueOf(upcomingDistTypeStr);
		String pastDistTypeStr = record[16].toString().substring(1);
		DisturbanceType pastDist = DisturbanceType.valueOf(pastDistTypeStr);
		double slopeInclination = Double.parseDouble(record[17].toString());
		double slopeAspect = Double.parseDouble(record[18].toString());
		String textureStr = record[19].toString().substring(1);
		SoilTexture soilTexture = SoilTexture.valueOf(textureStr);
		String depthStr = record[20].toString().substring(1);
		SoilDepth soilDepth = SoilDepth.valueOf(depthStr);
		String drainageClass = record[21].toString();
		double pred = Double.parseDouble(record[22].toString());
		IrisCompatibleTestPlotImpl plot = new IrisCompatibleTestPlotImpl(plotId,
				growthStepYr,
				basalAreaM2HaConiferous,
				basalAreaM2HaBroadleaved,
				slopeInclination,
				slopeAspect,
				dateYr,
				dd,
				prcp,
				frostDays,
				lowestTmin,
				soilDepth,
				upcomingDist,
				pastDist,
				DrainageGroupMatch.get(drainageClass),
				soilTexture,
				species,
				pred,
				gSpGr,
				occIndex10km);
		return plot;
	}
	
	
	private static IrisCompatiblePlotImpl createStandardPlotFromRecord(Object[] record) {
		String plotId = record[0].toString();
		double latitudeDeg = Double.parseDouble(record[2].toString());
		double longitudeDeg = Double.parseDouble(record[3].toString());
		String speciesName = record[4].toString();
		IrisSpecies species = IrisSpecies.valueOf(speciesName);
		int dateYr = Integer.parseInt(record[5].toString());
		double growthStepYr = Double.parseDouble(record[6].toString());
		double basalAreaM2HaConiferous = Double.parseDouble(record[7].toString());
		double basalAreaM2HaBroadleaved = Double.parseDouble(record[8].toString());
		double gSpGr = Double.parseDouble(record[9].toString());
		double dd = Double.parseDouble(record[11].toString());
		double prcp = Double.parseDouble(record[12].toString());
		double frostDays = Double.parseDouble(record[13].toString());
		double lowestTmin = Double.parseDouble(record[14].toString());
		String upcomingDistTypeStr = record[15].toString().substring(1);
		DisturbanceType upcomingDist = DisturbanceType.valueOf(upcomingDistTypeStr);
		String pastDistTypeStr = record[16].toString().substring(1);
		DisturbanceType pastDist = DisturbanceType.valueOf(pastDistTypeStr);
		double slopeInclination = Double.parseDouble(record[17].toString());
		double slopeAspect = Double.parseDouble(record[18].toString());
		String textureStr = record[19].toString().substring(1);
		SoilTexture soilTexture = SoilTexture.valueOf(textureStr);
		String depthStr = record[20].toString().substring(1);
		SoilDepth soilDepth = SoilDepth.valueOf(depthStr);
		String drainageClass = record[21].toString();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		IrisCompatiblePlotImpl plot = new IrisCompatiblePlotImpl(plotId,
				latitudeDeg,
				longitudeDeg,
				growthStepYr,
				basalAreaM2HaConiferous,
				basalAreaM2HaBroadleaved,
				slopeInclination,
				slopeAspect,
				dateYr,
				dd,
				prcp,
				frostDays,
				lowestTmin,
				soilDepth,
				upcomingDist,
				pastDist,
				DrainageGroupMatch.get(drainageClass),
				soilTexture,
				species,
				gSpGr,
				(List) StandardPlotList);
		return plot;
	}

	
	@BeforeClass
	public static void initialize() throws IOException {
		TestPlotListForOccurrences = new ArrayList<IrisCompatibleTestPlotImpl>();
		StandardPlotList = new ArrayList<IrisCompatiblePlotImpl>();
		String filename = ObjectUtility.getPackagePath(IrisRecruitmentTest.class) + "0_RecruitmentOccurrenceValidationDataset.csv";
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		while ((record = reader.nextRecord()) != null) {
			TestPlotListForOccurrences.add(createTestPlotFromRecord(record));
			StandardPlotList.add(createStandardPlotFromRecord(record));
		}
		reader.close();
		
		TestPlotListForNumbers = new ArrayList<IrisCompatibleTestPlotImpl>();
		filename = ObjectUtility.getPackagePath(IrisRecruitmentTest.class) + "0_RecruitmentNumberValidationDataset.csv";
		reader = new CSVReader(filename);
		while ((record = reader.nextRecord()) != null) {
			TestPlotListForNumbers.add(createTestPlotFromRecord(record));
		}
		reader.close();
	}

	
	
	/*
	 * Validation test for occurrence using R validation dataset
	 */
	@Test
	public void testOccurrencePredictionsAgainstRPredictions() throws IOException {
		IrisRecruitmentOccurrencePredictor predictor = new IrisRecruitmentOccurrencePredictor(false, null); // deterministic
		List<IrisCompatibleTestPlotImpl> plots = TestPlotListForOccurrences; 
		int nbTested = 0;
		for (IrisCompatibleTestPlotImpl plot : plots) {
			IrisCompatibleTree tree = plot.getTreeInstance();
			double actual = predictor.predictEventProbability(plot, tree);
			double expected = plot.getPredProb();
			if (Math.abs(actual-expected) > 1E-8) {	
				@SuppressWarnings("unused")
				int u = 0;
			}
			Assert.assertEquals("Testing probability for plot " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
					expected, 
					actual, 
					1E-8);
			nbTested++;
		}
		System.out.println("Number of successfully tested plots = " + nbTested + " / " + plots.size());
	}

	
	/*
	 * Validation test for number of recruits using R validation dataset with known occupancy index.
	 */
	@Test
	public void testMeanNumberPredictionsAgainstRPredictions() throws IOException {
		IrisRecruitmentNumberPredictor predictor = new IrisRecruitmentNumberPredictor(false, 
				new IrisRecruitmentOccurrencePredictor(false, null)); // deterministic
		List<IrisCompatibleTestPlotImpl> plots = TestPlotListForNumbers; 
		int nbTested = 0;
		for (IrisCompatibleTestPlotImpl plot : plots) {
			IrisCompatibleTree tree = plot.getTreeInstance();
			double actual = predictor.predictNumberOfRecruits(plot, tree.getSpecies());
			double expected = plot.getPredProb() + 1d; // adding one because 
			Assert.assertEquals("Testing mean predicted number for plot " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
					expected, 
					actual, 
					1E-8);
			nbTested++;
		}
		System.out.println("Number of successfully tested plots = " + nbTested + " / " + plots.size());
	}

	/*
	 * Validation test for stochastic implementation with known occupancy index.
	 */
	@Test
	public void testStochasticMeanNumberPredictions() throws IOException {
		IrisRecruitmentNumberPredictor detPredictor = new IrisRecruitmentNumberPredictor(false,
				new IrisRecruitmentOccurrencePredictor(false, null)); // deterministic
		IrisRecruitmentNumberPredictor stoPredictor = new IrisRecruitmentNumberPredictor(false, false, true,
				new IrisRecruitmentOccurrencePredictor(false, null)); // stochastic but with variability disabled for parameter estimates
		int nbRealizations = 1000000;
		List<IrisCompatibleTestPlotImpl> plots = TestPlotListForNumbers; 
		IrisCompatibleTestPlotImpl plot = plots.get(0);
		IrisCompatibleTree tree = plot.getTreeInstance();
		double detPred = detPredictor.predictNumberOfRecruits(plot, tree.getSpecies());
		Matrix realizations = new Matrix(nbRealizations, 1);
		for (int j = 0; j < nbRealizations; j++) {
			realizations.setValueAt(j, 0, stoPredictor.predictNumberOfRecruits(plot, tree.getSpecies()));
		}
		double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
		Matrix diff = realizations.scalarAdd(-meanStoPred);
		Matrix ssq = diff.transpose().multiply(diff);
		double variance = ssq.getValueAt(0, 0) / (realizations.m_iRows - 1);
		double invThetaParmEst = stoPredictor.getInvThetaParameterEstimate(tree.getSpecies());
		double expectedVariance = (detPred - 1) + invThetaParmEst * (detPred - 1) * (detPred - 1);
		Assert.assertEquals("Testing stochastic mean against deterministic mean " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
				detPred, 
				meanStoPred, 
				2E-3);
		System.out.println("Expected mean = " + detPred + " Actual mean = " + meanStoPred);
		Assert.assertEquals("Testing stochastic variance against expected variance " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
				expectedVariance, 
				variance, 
				1E-2);
		System.out.println("Expected variance = " + expectedVariance + " Actual variance = " + variance);
	}
	
	
	
	/*
	 * Validation test for stochastic implementation of occurrence part with unknown occupancy index.
	 */
	@Test
	public void testStochasticImplementationOccurrencePredictions() throws IOException {
		List<IrisCompatiblePlotImpl> plots = StandardPlotList; 
		@SuppressWarnings({ "unchecked", "rawtypes" })
		IrisRecruitmentOccurrencePredictor detPredictor = new IrisRecruitmentOccurrencePredictor(false, false, false, (List) plots); // deterministic
		@SuppressWarnings({ "unchecked", "rawtypes" })
		IrisRecruitmentOccurrencePredictor stoPredictor = new IrisRecruitmentOccurrencePredictor(false, true, false, (List) plots); // stochastic only in the occupancy index
		int nbRealizations = 50000;
		
		IrisCompatiblePlotImpl plot = plots.get(1200); // black spruce 
		double detPred = detPredictor.predictEventProbability(plot, plot.getTreeInstance());
		
		Matrix realizations = new Matrix(nbRealizations, 1);
		for (int j = 0; j < nbRealizations; j++) {
			plot.setMonteCarloRealizationId(j);
			realizations.setValueAt(j, 0, stoPredictor.predictEventProbability(plot, plot.getTreeInstance()));
		}
		double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
		System.out.println("Expected mean = " + detPred + " Actual mean = " + meanStoPred);
		Assert.assertEquals("Testing stochastic mean against deterministic mean " + plot.getSubjectId() + ", species " + plot.getTreeInstance().getSpecies().name(), 
				detPred, 
				meanStoPred, 
				4E-3);
	}

	
	
	/*
	 * Validation test for stochastic implementation of occurrence part with unknown occupancy index.
	 */
	@Test
	public void testStochasticImplementationMeanNumberPredictions() throws IOException {
		List<IrisCompatiblePlotImpl> plots = StandardPlotList; 
		@SuppressWarnings({ "unchecked", "rawtypes" })
		IrisRecruitmentNumberPredictor detPredictor = new IrisRecruitmentNumberPredictor(false, false, false, new IrisRecruitmentOccurrencePredictor(false, (List) plots)); // deterministic
		@SuppressWarnings({ "unchecked", "rawtypes" })
		IrisRecruitmentNumberPredictor stoPredictor = new IrisRecruitmentNumberPredictor(false, true, false, new IrisRecruitmentOccurrencePredictor(false, (List) plots)); // stochastic only in the occupancy index
		int nbRealizations = 10000;
		
		IrisCompatiblePlotImpl plot = plots.get(1200); // black spruce 
		double detPred = detPredictor.predictNumberOfRecruits(plot, plot.getTreeInstance().getSpecies());
		
		Matrix realizations = new Matrix(nbRealizations, 1);
		for (int j = 0; j < nbRealizations; j++) {
			plot.setMonteCarloRealizationId(j);
			realizations.setValueAt(j, 0, stoPredictor.predictNumberOfRecruits(plot, plot.getTreeInstance().getSpecies()));
		}
		double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
		System.out.println("Expected mean = " + detPred + " Actual mean = " + meanStoPred);
		Assert.assertEquals("Testing stochastic mean against deterministic mean " + plot.getSubjectId() + ", species " + plot.getTreeInstance().getSpecies().name(), 
				detPred, 
				meanStoPred, 
				3E-2);
	}

	
	
	
	@AfterClass
	public static void cleanup() {
		if (TestPlotListForOccurrences != null) {
			TestPlotListForOccurrences.clear();
		}
		if (TestPlotListForNumbers != null) {
			TestPlotListForNumbers.clear();
		}
		if (StandardPlotList != null) {
			StandardPlotList.clear();
		}
	}


}
