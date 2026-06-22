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
package allometry.heightgrowth.huang2026;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import allometry.heightgrowth.huang2026.Huang2026HeightGrowthPlot;
import allometry.heightgrowth.huang2026.Huang2026HeightGrowthPredictor;
import repicea.io.javacsv.CSVHeader;
import repicea.io.javacsv.CSVReader;
import repicea.simulation.climate.REpiceaClimateVariableInformation;
import repicea.simulation.climate.REpiceaClimateVariableInformation.Resolution;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.util.ObjectUtility;

public class Huang2026HeightGrowthPredictorTest {
	
	static class Huang2026SiteIndexPlotImpl implements Huang2026HeightGrowthPlot {

		final String plotId;
		final Species sp;
		final double hi;
		final double age;
		final double MWMT20;
		final double MWMT_n;
		final double MAP20;
		final double MAP_n;
		final double pred;
		
		Huang2026SiteIndexPlotImpl(String plotId,
				Species sp,
				double hi,
				double age,
				double MWMT20,
				double MWMT_n,
				double MAP20,
				double MAP_n,
				double pred) {
			this.plotId = plotId;
			this.sp = sp;
			this.hi = hi;
			this.age = age;
			this.MWMT20 = MWMT20;
			this.MWMT_n = MWMT_n;
			this.MAP20 = MAP20;
			this.MAP_n = MAP_n;
			this.pred = pred;
		}
		
		@Override
		public String getSubjectId() {return plotId;}		


		@Override
		public int getMonteCarloRealizationId() {return 0;}

		@Override
		public String getId() {return getSubjectId();}

		@Override
		public double getTotalAnnualPrecipitationMm(REpiceaClimateVariableInformation info) {
			if (info.resolution == Resolution.IntervalAveragedStarting20YrsBeforeFinalMeasurement) {
				return MAP20;
			} else if (info.resolution == Resolution.Normals30Year) {
				return MAP_n;
			} else {
				throw new UnsupportedOperationException("The resolution " + info.resolution.name() + " is not supported!");
			}
		}

		@Override
		public double getMeanJulyTemperatureCelsius(REpiceaClimateVariableInformation info) {
			if (info.resolution == Resolution.IntervalAveragedStarting20YrsBeforeFinalMeasurement) {
				return MWMT20;
			} else if (info.resolution == Resolution.Normals30Year) {
				return MWMT_n;
			} else {
				throw new UnsupportedOperationException("The resolution " + info.resolution.name() + " is not supported!");
			}
		}

		@Override
		public double getSiteIndexM() {return hi;}

		@Override
		public double getStandAgeYr() {return age;}

		@Override
		public Species getDominantSpecies() {return sp;}
		
	}
	
	static Map<Species, List<Huang2026HeightGrowthPlot>> PlotMap;
	
	private static void readTrees() throws Exception {
		PlotMap = new HashMap<Species, List<Huang2026HeightGrowthPlot>>();
		for (Species sp : Huang2026HeightGrowthPredictor.EligibleSpecies) {
			List<Huang2026HeightGrowthPlot> plots = new ArrayList<Huang2026HeightGrowthPlot>();
			String suffix = Huang2026HeightGrowthPredictor.SuffixMap.get(sp);
			String filename = ObjectUtility.getPackagePath(Huang2026SiteIndexPlotImpl.class) + "pred_" + suffix + ".csv";
			CSVReader reader = null;
			try {
				reader = new CSVReader(filename);
				CSVHeader header = reader.getHeader();
				Object[] record;
				int i = 0;
				while ((record = reader.nextRecord()) != null) {
					if (!record[header.getIndexOfThisField("pred")].toString().isEmpty()) {
						String plotId = i + "";
						double hi = Double.parseDouble(record[header.getIndexOfThisField("HI")].toString());
						double age = Double.parseDouble(record[header.getIndexOfThisField("age00")].toString());
						double mwmt20 = Double.parseDouble(record[header.getIndexOfThisField("MWMT20")].toString());
						double mwmt_n = Double.parseDouble(record[header.getIndexOfThisField("MWMT_n")].toString());
						double map20 = Double.parseDouble(record[header.getIndexOfThisField("MAP20")].toString());
						double map_n = Double.parseDouble(record[header.getIndexOfThisField("MAP_n")].toString());
						double pred = Double.parseDouble(record[header.getIndexOfThisField("pred")].toString());
						plots.add(new Huang2026SiteIndexPlotImpl(plotId, sp, hi, age, mwmt20, mwmt_n, map20, map_n, pred));
						i++;
					}
				}
				PlotMap.put(sp, plots);
			} catch (Exception e) {
				throw e;
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		}
		
	}
	
	@BeforeClass
	public static void initialize() throws Exception {
		readTrees();
	}
	
	@Test
	public void test01DeterministicPredictions() {
		Huang2026HeightGrowthPredictor predictor = new Huang2026HeightGrowthPredictor(false, false);
		for (Species sp : Huang2026HeightGrowthPredictor.EligibleSpecies) {
			int nbTested = 0;
			if (!PlotMap.containsKey(sp) || PlotMap.get(sp) == null || PlotMap.get(sp).isEmpty()) {
				Assert.fail("It seems the PlotMap static object does not contain any plot for this species " + sp.getLatinName());
			}
			for (Huang2026HeightGrowthPlot p : PlotMap.get(sp)) {
				double observedPred = predictor.predictHeightM(p);
				nbTested++;
				Assert.assertEquals("Testing prediction for species " + sp.getLatinName(), 
						((Huang2026SiteIndexPlotImpl) p).pred,
						observedPred,
						1E-8);
			}
			System.out.println("Successfully tested " + nbTested + " observations for species " + sp.getLatinName());
		}
	}

	@AfterClass
	public static void finalizeTest(){
		PlotMap.clear();
		PlotMap = null;
	}

}
