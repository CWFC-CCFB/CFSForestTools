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
package canforservutility.occupancyindex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import canforservutility.predictor.iris.recruitment_v1.IrisCompatibleTree.IrisSpecies;
import repicea.io.javacsv.CSVReader;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

public class OccupancyIndexTest {

	private static List<OccupancyIndexCalculablePlot> plots;
	
	@BeforeClass
	public static void initialize() throws IOException {
		String filename = ObjectUtility.getPackagePath(OccupancyIndexTest.class) + "datasetERSForProximityIndex.csv";
		CSVReader r = new CSVReader(filename);
		plots = new ArrayList<OccupancyIndexCalculablePlot>();
		Object[] record;
		while((record = r.nextRecord()) != null) {
			String id = record[0].toString();
			double latitudeDeg = Double.parseDouble(record[1].toString());
			double longitudeDeg = Double.parseDouble(record[2].toString());
//			double weight = Double.parseDouble(record[3].toString());
			int dateYr = Integer.parseInt(record[4].toString());
			double baHaSpecies = Double.parseDouble(record[5].toString());
			plots.add(new SimpleOccupancyIndexCalculablePlot(id, latitudeDeg, longitudeDeg, dateYr, baHaSpecies));
		}
		r.close();
	}
	
	@Test
	public void test01OccupancyIndexSinglePlot() throws IOException {
		Assert.assertTrue("The plots static member is not empty", plots != null && !plots.isEmpty());
		OccupancyIndexCalculator calculator = new OccupancyIndexCalculator(plots);
		System.out.println(calculator.getMaximumDistanceNearestPlot());
		Assert.assertEquals("Testing the size of the id list", 12267, calculator.plotsId.size());
		Assert.assertEquals("Testing the size of the distance matrix", 12267, calculator.distances.m_iRows);
		ConcurrentHashMap<Integer, List<OccupancyIndexCalculablePlot>> dateFilteredPlots = new ConcurrentHashMap<Integer, List<OccupancyIndexCalculablePlot>>();
		GaussianEstimate proximityIndexEstimate = calculator.getOccupancyIndex(plots, plots.get(0), IrisSpecies.ERS, 15d, dateFilteredPlots);
		double proximityIndexMean = proximityIndexEstimate.getMean().getValueAt(0, 0);
		double proximityIndexVariance = proximityIndexEstimate.getVariance().getValueAt(0, 0);
		Assert.assertEquals("Testing the mean of the estimate", 0.5, proximityIndexMean, 1E-8);
		Assert.assertEquals("Testing the size of the distance matrix", 0.05, proximityIndexVariance, 1E-8);
	}

	
	@Test
	public void test02OccupancyIndexAllPlots() throws IOException {
		Assert.assertTrue("The plots static member is not empty", plots != null && !plots.isEmpty());
		OccupancyIndexCalculator calculator = new OccupancyIndexCalculator(plots);
		try {
			for (int i = 0; i < plots.size(); i++) {
				calculator.getOccupancyIndex(plots, plots.get(i), IrisSpecies.ERS, 10d);
			}
			Assert.fail("Should have thrown an unsupported operation exception!");
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			System.out.println("Relax! This error was expected!");
		}
	}

	
	@AfterClass
	public static void cleanup() {
		if (plots != null) {
			plots.clear();
		}
	}

}
