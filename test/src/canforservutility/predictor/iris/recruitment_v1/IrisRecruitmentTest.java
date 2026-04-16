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
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import canforservutility.predictor.iris.recruitment_v1.IrisRecruitmentPlot.DisturbanceType;
import canforservutility.predictor.iris.recruitment_v1.IrisRecruitmentPlot.SoilDepth;
import canforservutility.predictor.iris.recruitment_v1.IrisRecruitmentPlot.SoilTexture;
import canforservutility.predictor.iris.recruitment_v1.IrisTree.IrisSpecies;
import repicea.io.javacsv.CSVHeader;
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

	private static Map<String, IrisRecruitmentPlotImpl> PlotMapForOccurrence;
	private static Map<String, IrisRecruitmentPlotImpl> PlotMapForNumber;

	private static void createStandardPlotFromRecord(Object[] record, CSVHeader header, Map<String,IrisRecruitmentPlotImpl> oMap) {
		String plotId = record[header.getIndexOfThisField("newID_PE")].toString();
		double latitudeDeg = Double.parseDouble(record[header.getIndexOfThisField("latitudeDeg")].toString());
		double longitudeDeg = Double.parseDouble(record[header.getIndexOfThisField("longitudeDeg")].toString());
		String speciesName = record[header.getIndexOfThisField("speciesGr")].toString();
		IrisSpecies species = IrisSpecies.valueOf(speciesName);
		int dateYr = Integer.parseInt(record[header.getIndexOfThisField("year.x")].toString());
		int growthStepYr = Integer.parseInt(record[header.getIndexOfThisField("dt")].toString());
		double basalAreaM2HaConiferous = Double.parseDouble(record[header.getIndexOfThisField("G_R")].toString());
		double basalAreaM2HaBroadleaved = Double.parseDouble(record[header.getIndexOfThisField("G_F")].toString());
		double gSpGr = Double.parseDouble(record[header.getIndexOfThisField("G_SpGr")].toString());
		double occIndex = Double.parseDouble(record[header.getIndexOfThisField("occIndex10km")].toString());

		double dd = Double.parseDouble(record[header.getIndexOfThisField("DD")].toString());
		double prcp = Double.parseDouble(record[header.getIndexOfThisField("TotalPrcp")].toString());
		double frostDays = Double.parseDouble(record[header.getIndexOfThisField("FrostDay")].toString());
		double lowestTmin = Double.parseDouble(record[header.getIndexOfThisField("LowestTmin")].toString());
		String upcomingDistTypeStr = record[header.getIndexOfThisField("upcomingDistType")].toString().substring(1);
		DisturbanceType upcomingDist = DisturbanceType.valueOf(upcomingDistTypeStr);
		String pastDistTypeStr = record[header.getIndexOfThisField("pastDistType")].toString().substring(1);
		DisturbanceType pastDist = DisturbanceType.valueOf(pastDistTypeStr);
		double slopeInclination = Double.parseDouble(record[header.getIndexOfThisField("pentePerc")].toString());
		double slopeAspect = Double.parseDouble(record[header.getIndexOfThisField("exposition")].toString());
		String textureStr = record[header.getIndexOfThisField("texture")].toString().substring(1);
		SoilTexture soilTexture = SoilTexture.valueOf(textureStr);
		String depthStr = record[header.getIndexOfThisField("depth2")].toString().substring(1);
		SoilDepth soilDepth = SoilDepth.valueOf(depthStr);
		String drainageClass = record[header.getIndexOfThisField("classDrainage")].toString();
		double pred = Double.parseDouble(record[header.getIndexOfThisField("pred")].toString());
		String uniqueID = plotId + "_" + dateYr;
		if (oMap.containsKey(uniqueID)) {
			oMap.get(uniqueID).updateMap(species, gSpGr, occIndex, pred);
		} else {
			IrisRecruitmentPlotImpl plot = new IrisRecruitmentPlotImpl(plotId,
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
					occIndex,
					pred);
			oMap.put(uniqueID, plot);
		}
	}


	@BeforeClass
	public static void initialize() throws IOException {
		PlotMapForOccurrence = new HashMap<String, IrisRecruitmentPlotImpl>();
		String filename = ObjectUtility.getPackagePath(IrisRecruitmentTest.class) + "0_RecruitmentOccurrenceValidationDataset.csv";
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		while ((record = reader.nextRecord()) != null) {
			createStandardPlotFromRecord(record, reader.getHeader(), PlotMapForOccurrence);
		}
		reader.close();

		PlotMapForNumber = new HashMap<String, IrisRecruitmentPlotImpl>();
		filename = ObjectUtility.getPackagePath(IrisRecruitmentTest.class) + "0_RecruitmentNumberValidationDataset.csv";
		reader = new CSVReader(filename);
		while ((record = reader.nextRecord()) != null) {
			createStandardPlotFromRecord(record, reader.getHeader(), PlotMapForNumber);
		}
		reader.close();
	}



	/*
	 * Validation test for occurrence using R validation dataset
	 */
	@Test
	public void test01OccurrencePredictionsAgainstRPredictions() throws IOException {
		IrisRecruitmentOccurrencePredictor predictor = new IrisRecruitmentOccurrencePredictor(false); // deterministic
		Map<String, IrisRecruitmentPlotImpl> plots = PlotMapForOccurrence; 
		for (IrisSpecies species : IrisSpecies.values()) {
			int nbTested = 0;
			for (IrisRecruitmentPlotImpl plot : plots.values()) {
				if (plot.getBasalAreaM2HaForThisSpecies(species) != -1) { // means it was not measured in this plot
					IrisTree tree = plot.getTreeInstance(species);
					double actual = predictor.predictEventProbability(plot, tree);
					double expected = plot.getPredForThisSpecies(species);
					Assert.assertEquals("Testing probability for plot " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
							expected, 
							actual, 
							1E-8);
					nbTested++;
				}
			}
			System.out.println("Number of successfully tested plots = " + nbTested + " / " + plots.size());
		}
	}


	/*
	 * Validation test for number of recruits using R validation dataset with known occupancy index.
	 */
	@Test
	public void test02MeanNumberPredictionsAgainstRPredictions() throws IOException {
		System.out.println("Testing abundance models...");
		IrisRecruitmentNumberPredictor predictor = new IrisRecruitmentNumberPredictor(false, 
				new IrisRecruitmentOccurrencePredictor(false)); // deterministic
		Map<String, IrisRecruitmentPlotImpl> plotMap = PlotMapForNumber; 
		for (IrisSpecies species : IrisSpecies.values()) {
			System.out.println("  Processing species " + species.name());
			int nbTested = 0;
			for (IrisRecruitmentPlotImpl plot : plotMap.values()) {
				IrisTree tree = plot.getTreeInstance(species);
				if (plot.getBasalAreaM2HaForThisSpecies(species) != -1) { // means it was not measured in this plot
					double actual = predictor.predictNumberOfRecruits(plot, tree.getSpecies());
					double expected = plot.getPredForThisSpecies(species) + 1d; // adding one because 
					Assert.assertEquals("Testing mean predicted number for plot " + plot.getSubjectId() + ", species " + tree.getSpecies().name(), 
							expected, 
							actual, 
							1E-8);
					nbTested++;
				}
			}
			System.out.println("   Number of successfully tested plots = " + nbTested + " / " + plotMap.size());
		}
	}

	/*
	 * Validation test for stochastic implementation with known occupancy index.
	 */
	@Test
	public void test03StochasticMeanNumberPredictions() throws IOException {
		IrisRecruitmentNumberPredictor detPredictor = new IrisRecruitmentNumberPredictor(false, 
				new IrisRecruitmentOccurrencePredictor(false)); // deterministic
		IrisRecruitmentNumberPredictor stoPredictor = new IrisRecruitmentNumberPredictor(false, true,
				new IrisRecruitmentOccurrencePredictor(false)); // stochastic but with variability disabled for parameter estimates
		int nbRealizations = 1000000;
		Map<String, IrisRecruitmentPlotImpl> plots = PlotMapForNumber; 

		for (IrisSpecies sp : IrisSpecies.values()) {
			System.out.println(" Processing species " + sp.name() + "...");
			IrisRecruitmentPlotImpl selectedPlot = null;
			for (IrisRecruitmentPlotImpl p : plots.values()) {
				if (p.getBasalAreaM2HaForThisSpecies(sp) > 0) {
					selectedPlot = p;
					break;
				}
			}
			if (selectedPlot == null) {
				throw new UnsupportedOperationException("No plots were selected!");
			}
			IrisTree tree = selectedPlot.getTreeInstance(sp);
			double detPred = detPredictor.predictNumberOfRecruits(selectedPlot, tree.getSpecies());
			Matrix realizations = new Matrix(nbRealizations, 1);
			for (int j = 0; j < nbRealizations; j++) {
				realizations.setValueAt(j, 0, stoPredictor.predictNumberOfRecruits(selectedPlot, tree.getSpecies()));
			}
			double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
			Matrix diff = realizations.scalarAdd(-meanStoPred);
			Matrix ssq = diff.transpose().multiply(diff);
			double variance = ssq.getValueAt(0, 0) / (realizations.m_iRows - 1);
			double invThetaParmEst = stoPredictor.getInvThetaParameterEstimate(tree.getSpecies());
			double expectedVariance = (detPred - 1) + invThetaParmEst * (detPred - 1) * (detPred - 1);
			System.out.println("Expected mean = " + detPred + " Actual mean = " + meanStoPred);
			Assert.assertEquals("Testing stochastic mean against deterministic mean " + selectedPlot.getSubjectId() + ", species " + tree.getSpecies().name(), 
					0, 
					1 - meanStoPred/detPred, 
					0.005);
			System.out.println("Expected variance = " + expectedVariance + " Actual variance = " + variance);
			Assert.assertEquals("Testing stochastic variance against expected variance " + selectedPlot.getSubjectId() + ", species " + tree.getSpecies().name(), 
					0,
					1 - variance/expectedVariance, 
					0.02);
		}
	}



	//	/*
	//	 * Validation test for stochastic implementation of occurrence part with unknown occupancy index.
	//	 */
	//	@Test
	//	public void test04StochasticImplementationOccurrencePredictions() throws IOException {
	//		List<IrisRecruitmentPlotImpl> plots = StandardPlotList; 
	//		@SuppressWarnings({ "unchecked", "rawtypes" })
	//		IrisRecruitmentOccurrencePredictor detPredictor = new IrisRecruitmentOccurrencePredictor(false, false, false, (List) plots, 1); // deterministic
	//		@SuppressWarnings({ "unchecked", "rawtypes" })
	//		IrisRecruitmentOccurrencePredictor stoPredictor = new IrisRecruitmentOccurrencePredictor(false, true, false, (List) plots, 1); // stochastic only in the occupancy index
	//		int nbRealizations = 50000;
	//		
	//		IrisRecruitmentPlotImpl plot = plots.get(1200); // black spruce 
	//		double detPred = detPredictor.predictEventProbability(plot, plot.getTreeInstance());
	//		
	//		Matrix realizations = new Matrix(nbRealizations, 1);
	//		for (int j = 0; j < nbRealizations; j++) {
	//			plot.setMonteCarloRealizationId(j);
	//			realizations.setValueAt(j, 0, stoPredictor.predictEventProbability(plot, plot.getTreeInstance()));
	//		}
	//		double meanStoPred = realizations.getSumOfElements() / realizations.m_iRows;
	//		System.out.println("Expected mean = " + detPred + " Actual mean = " + meanStoPred);
	//		Assert.assertEquals("Testing stochastic mean against deterministic mean " + plot.getSubjectId() + ", species " + plot.getTreeInstance().getSpecies().name(), 
	//				detPred, 
	//				meanStoPred, 
	//				4E-3);
	//	}
	//
	//	
	//	
	//	/*
	//	 * Validation test for stochastic implementation of occurrence part with unknown occupancy index.
	//	 */
	//	@Test
	//	public void test05StochasticImplementationMeanNumberPredictions() throws IOException {
	//		List<IrisRecruitmentPlotImpl> plots = StandardPlotList; 
	//		int nbRealizations = 1000;
	//		@SuppressWarnings({ "unchecked", "rawtypes" })
	//		IrisRecruitmentNumberPredictor detPredictor = new IrisRecruitmentNumberPredictor(false, false, false, new IrisRecruitmentOccurrencePredictor(false, (List) plots, 1)); // deterministic
	//		@SuppressWarnings({ "unchecked", "rawtypes" })
	//		IrisRecruitmentNumberPredictor stoPredictor = new IrisRecruitmentNumberPredictor(false, true, false, new IrisRecruitmentOccurrencePredictor(false, (List) plots, nbRealizations)); // stochastic only in the occupancy index
	//		
	//		IrisRecruitmentPlotImpl plot = plots.get(1200); // black spruce 
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
	//				0.07);
	//	}
	//
	//	
	//	

	@AfterClass
	public static void cleanup() {
		if (PlotMapForOccurrence != null) {
			PlotMapForOccurrence.clear();
		}
		if (PlotMapForNumber != null) {
			PlotMapForNumber.clear();
		}
	}


}
