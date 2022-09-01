/*
 * This file is part of the cfsforesttools library.
 *
 * Copyright (C) 2020-2022 Her Majesty the Queen in right of Canada
 * author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package canforservutility.predictor.iris2020.recruitment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatibleTree.Iris2020Species;
import repicea.io.javacsv.CSVReader;
import repicea.stats.estimates.Estimate;
import repicea.util.ObjectUtility;

public class Iris2020ProximityIndexTest {

	private static List<Iris2020ProtoPlot> plots;
	
	@BeforeClass
	public static void initialize() throws IOException {
		String filename = ObjectUtility.getPackagePath(Iris2020ProximityIndexTest.class) + "datasetERSForProximityIndex.csv";
		CSVReader r = new CSVReader(filename);
		plots = new ArrayList<Iris2020ProtoPlot>();
		Object[] record;
		while((record = r.nextRecord()) != null) {
			String id = record[0].toString();
			double latitudeDeg = Double.parseDouble(record[1].toString());
			double longitudeDeg = Double.parseDouble(record[2].toString());
			double weight = Double.parseDouble(record[3].toString());
			int dateYr = Integer.parseInt(record[4].toString());
			double baHaSpecies = Double.parseDouble(record[5].toString());
			plots.add(new Iris2020ProtoPlotImpl(id, latitudeDeg, longitudeDeg, weight, dateYr, baHaSpecies));
		}
		r.close();
	}
	
	@Test
	public void proximityIndexTest1() throws IOException {
		Assert.assertTrue("The plots static member is not empty", plots != null && !plots.isEmpty());
		Iris2020OccupancyIndexCalculator calculator = new Iris2020OccupancyIndexCalculator(plots, 15);
		Assert.assertEquals("Testing the size of the id list", 12267, calculator.plotsId.size());
		Assert.assertEquals("Testing the size of the distance matrix", 12267, calculator.distances.m_iRows);
		ConcurrentHashMap<Integer, List<Iris2020ProtoPlot>> dateFilteredPlots = new ConcurrentHashMap<Integer, List<Iris2020ProtoPlot>>();
		Estimate<?> proximityIndexEstimate = calculator.getProximityIndex(plots, plots.get(0), Iris2020Species.ERS, dateFilteredPlots);
		double proximityIndexMean = proximityIndexEstimate.getMean().getValueAt(0, 0);
		double proximityIndexVariance = proximityIndexEstimate.getVariance().getValueAt(0, 0);
		Assert.assertEquals("Testing the mean of the estimate", 0.5, proximityIndexMean, 1E-8);
		Assert.assertEquals("Testing the size of the distance matrix", 0.05, proximityIndexVariance, 1E-8);
	}
	
	@AfterClass
	public static void cleanup() {
		if (plots != null) {
			plots.clear();
		}
	}

	public static void main(String[] args) throws Exception {
		initialize();
		List<Iris2020ProtoPlot> mySelectedPlots = new ArrayList<Iris2020ProtoPlot>();
		for (int i = 0; i < plots.size(); i++)
			mySelectedPlots.add(plots.get(i));
		
		Iris2020OccupancyIndexCalculator calculator = new Iris2020OccupancyIndexCalculator(mySelectedPlots, 15);

		long startTime = System.currentTimeMillis();
		List<Estimate<?>> estimates = calculator.getProximityIndexForThesePlots(mySelectedPlots, Iris2020Species.ERS, 3);
		System.out.println("Time to get " + estimates.size() + " estimates = " + (System.currentTimeMillis() - startTime) * 0.001 + " sec.");
	}

}
