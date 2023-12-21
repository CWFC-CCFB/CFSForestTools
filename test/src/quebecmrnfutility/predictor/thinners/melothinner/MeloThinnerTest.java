/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.thinners.melothinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.simulation.covariateproviders.plotlevel.QcSlopeClassProvider.QcSlopeClass;
import repicea.io.javacsv.CSVReader;
import repicea.simulation.disturbances.DisturbanceParameter;
import repicea.util.ObjectUtility;

public class MeloThinnerTest {

	private static List<MeloThinnerPlotImpl> Plots;
	
	
	
	
	
	private static void ReadPlots() throws IOException {
		if (Plots == null) {
			Plots = new ArrayList<MeloThinnerPlotImpl>();
			String testFile = ObjectUtility.getRelativePackagePath(MeloThinnerTest.class) + "HarvestPred.csv";
			CSVReader reader = new CSVReader(testFile);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				String plotId = record[1].toString();
				String ownershipCode = record[2].toString().trim();
				int regionCode = Integer.parseInt(record[3].toString());
				String slopeClass = record[4].toString();
				QcSlopeClass sc = QcSlopeClass.valueOf(slopeClass);
				double basalAreaM2Ha = Double.parseDouble(record[5].toString());
				double stemDensityHa = Double.parseDouble(record[6].toString());
				String ecologicalType = record[7].toString();
				int year0 = Integer.parseInt(record[9].toString());
				int year1 = Integer.parseInt(record[10].toString());
				List<Integer> possIndex = new ArrayList<Integer>();
				for (int i = 11; i <=37; i++) {
					if (record[i].toString().equals("1")) {
						possIndex.add( i + 27 );
					}
				}
				double[] aac = new double[possIndex.size()];
				for (int i = 0; i < possIndex.size(); i++) {
					aac[i] = Double.parseDouble(record[possIndex.get(i)].toString());
				}
				double pred = Double.parseDouble(record[75].toString());
				double meanPA = Double.parseDouble(record[76].toString());
				
				MeloThinnerPlotImpl plot = new MeloThinnerPlotImpl(plotId, 
						basalAreaM2Ha, 
						stemDensityHa, 
						ecologicalType, 
						sc,
						year0,
						year1,
						regionCode,
						ownershipCode,
						aac,
						pred,
						meanPA);
				Plots.add(plot);
			}
			reader.close();
		}
	}

	@Test
	public void testModelAgainstConditionalSASPrediction() throws IOException {
		ReadPlots();
		int nbPlots = 0;
		MeloThinnerPredictor predictor = new MeloThinnerPredictor(false);
		predictor.setGaussianQuadrature(false);
		Map<String, Object> parms = new HashMap<String, Object>();
		for (MeloThinnerPlotImpl plot : Plots) {
			parms.put(DisturbanceParameter.ParmAAC, plot.getAAC());
			double actual = 1 - predictor.predictEventProbability(plot, null, parms); // 1 - probability of harvesting to get the survival
			double expected = plot.getPredSurvival();
			Assert.assertEquals("Comparing plot no " + plot.getSubjectId(), expected, actual, 1E-8);
			nbPlots++;
		}
		System.out.println("Successfully tested plots : " + nbPlots);
	}

	@Test
	public void testModelAgainstMarginalSASPrediction() throws IOException {
		ReadPlots();
		int nbPlots = 0;
		MeloThinnerPredictor predictor = new MeloThinnerPredictor(false);
		Map<String, Object> parms = new HashMap<String, Object>();
		for (MeloThinnerPlotImpl plot : Plots) {
			parms.put(DisturbanceParameter.ParmAAC, plot.getAAC());
			double actual = 1 - predictor.predictEventProbability(plot, null, parms); // 1 - probability of harvesting to get the survival
			double expected = plot.getMeanPA();
			Assert.assertEquals("Comparing plot no " + plot.getSubjectId(), expected, actual, 1E-6);
			nbPlots++;
		}
		System.out.println("Successfully tested plots : " + nbPlots);
	}

	@Test
	public void testModelAgainstConditionalSASPredictionWithAACProvider() throws IOException {
		ReadPlots();
		int nbPlots = 0;
		MeloThinnerPredictor predictor = new MeloThinnerPredictor(false);
		predictor.setGaussianQuadrature(false);
		Map<String, Object> parms = new HashMap<String, Object>();
		for (MeloThinnerPlotImpl plot : Plots) {
			parms.put(DisturbanceParameter.ParmYear0, plot.getYear0());
			parms.put(DisturbanceParameter.ParmYear1, plot.getYear1());
			double actual = 1 - predictor.predictEventProbability(plot, null, parms); // 1 - probability of harvesting to get the survival
			double expected = plot.getPredSurvival();
			Assert.assertEquals("Comparing plot no " + plot.getSubjectId(), expected, actual, 1E-8);
			nbPlots++;
		}
		System.out.println("Successfully tested plots : " + nbPlots);
	}

	@Test
	public void testModelAgainstMarginalSASPredictionWithAACProvider() throws IOException {
		ReadPlots();
		int nbPlots = 0;
		MeloThinnerPredictor predictor = new MeloThinnerPredictor(false);
		Map<String, Object> parms = new HashMap<String, Object>();
		for (MeloThinnerPlotImpl plot : Plots) {
			parms.put(DisturbanceParameter.ParmYear0, plot.getYear0());
			parms.put(DisturbanceParameter.ParmYear1, plot.getYear1());
			double actual = 1 - predictor.predictEventProbability(plot, null, parms); // 1 - probability of harvesting to get the survival
			double expected = plot.getMeanPA();
			Assert.assertEquals("Comparing plot no " + plot.getSubjectId(), expected, actual, 1E-6);
			nbPlots++;
		}
		System.out.println("Successfully tested plots : " + nbPlots);
	}
	

	@Test
	public void testModelAgainstMarginalSASPredictionWithAACProviderWithReducedAAC() throws IOException {
		ReadPlots();
		MeloThinnerPredictor predictor = new MeloThinnerPredictor(false);
		MeloThinnerPlotImpl plot = Plots.get(0);
		Map<String, Object> parms = new HashMap<String, Object>();
		parms.put(DisturbanceParameter.ParmYear0, plot.getYear0());
		parms.put(DisturbanceParameter.ParmYear1, plot.getYear1());
		double withoutReduction = predictor.predictEventProbability(plot, null, parms); 
		parms.put(DisturbanceParameter.ParmModulation, -0.3);
		double withReduction = predictor.predictEventProbability(plot, null, parms);  // -0.3 : 30% reduction of AAC
		double diff = withoutReduction - withReduction;
		Assert.assertEquals("Comparing plot no " + plot.getSubjectId() + " with and without a 30% reduction of AAC", 0.12438656953854133, diff, 1E-6);
	}

	@Test
	public void testModelAgainstMarginalSASPredictionWithAACProviderWithIncreasedAAC() throws IOException {
		ReadPlots();
		MeloThinnerPredictor predictor = new MeloThinnerPredictor(false);
		MeloThinnerPlotImpl plot = Plots.get(0);
		Map<String, Object> parms = new HashMap<String, Object>();
		parms.put(DisturbanceParameter.ParmYear0, plot.getYear0());
		parms.put(DisturbanceParameter.ParmYear1, plot.getYear1());
		double withoutIncrease = predictor.predictEventProbability(plot, null, parms); 
		parms.put(DisturbanceParameter.ParmModulation, 0.3);
		double withIncrease = predictor.predictEventProbability(plot, null, parms);  // +0.3 : 30% increase of AAC
		double diff = withoutIncrease - withIncrease;
		Assert.assertEquals("Comparing plot no " + plot.getSubjectId() + " with and without a 30% increase of AAC", -0.13954637384827573, diff, 1E-6);
	}

	@Test
	public void testModelWithFixedAAC() throws IOException {
		ReadPlots();
		MeloThinnerPredictor predictor = new MeloThinnerPredictor(false);
		MeloThinnerPlotImpl plot = Plots.get(0);
		Map<String, Object> parms = new HashMap<String, Object>();
		parms.put(DisturbanceParameter.ParmYear0, plot.getYear0());
		parms.put(DisturbanceParameter.ParmYear1, plot.getYear1());
		double basicAACprediction = predictor.predictEventProbability(plot, null, parms); 
		predictor.setTargetAACPerHa(3d);
		double withAACSetTo3prediction = predictor.predictEventProbability(plot, null, parms); 
		predictor.setTargetAACPerHa(null);
		double basicAACprediction2 = predictor.predictEventProbability(plot, null, parms); 
		double diff = basicAACprediction - withAACSetTo3prediction;
		Assert.assertEquals("Comparing plot no " + plot.getSubjectId() + " current AAC and AAC set to 3 m3/ha/yr", -0.440087368502928843, diff, 1E-8);
		Assert.assertEquals("Comparing plot no " + plot.getSubjectId() + " current AAC before and after using the setFixedAAC method", basicAACprediction, basicAACprediction2, 1E-8);
	}

}
