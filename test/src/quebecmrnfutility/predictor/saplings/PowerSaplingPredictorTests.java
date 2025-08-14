/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service,
 *         Hugues Power, Direction de la recherche forestiere du Quebec
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
package quebecmrnfutility.predictor.saplings;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.predictor.saplings.PowerSaplingBasalAreaAndDensityCompatiblePlot.CoverType;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.stats.estimates.MonteCarloEstimate;

public class PowerSaplingPredictorTests {

	static class PowerSaplingBasalAreaAndDensityCompatiblePlotImpl implements PowerSaplingBasalAreaAndDensityCompatiblePlot {

		int monteCarloId;
		final double basalAreaM2Ha;
		final CoverType coverType;
		final boolean isInterventionResult;

		PowerSaplingBasalAreaAndDensityCompatiblePlotImpl(double basalAreaM2Ha, CoverType coverType, boolean isInterventionResult) {
			this.basalAreaM2Ha = basalAreaM2Ha;
			this.coverType = coverType;
			this.isInterventionResult = isInterventionResult;
		}
		
		@Override
		public String getSubjectId() {return "MyPlot";}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

		@Override
		public int getMonteCarloRealizationId() {return monteCarloId;}

		@Override
		public double getBasalAreaM2Ha() {return basalAreaM2Ha;}

		@Override
		public CoverType getCoverType() {return coverType;}

		@Override
		public boolean isInterventionResult() {return isInterventionResult;}
		
	}
	
	@Test
	public void test01BasalAreaDeterministicPrediction() {
		PowerSaplingBasalAreaAndDensityCompatiblePlotImpl myPlot = new PowerSaplingBasalAreaAndDensityCompatiblePlotImpl(20, CoverType.Fir, false);
		PowerSaplingBasalAreaPredictor pred = new PowerSaplingBasalAreaPredictor(false);
		double baPrediction = pred.predictSaplingBasalAreaM2Ha(myPlot);
		Assert.assertEquals("Testing deterministic basal area prediction", 6.45451208796443, baPrediction, 1E-8);
	}

	@Test
	public void test02BasalAreaStochasticPrediction() {
		PowerSaplingBasalAreaAndDensityCompatiblePlotImpl myPlot = new PowerSaplingBasalAreaAndDensityCompatiblePlotImpl(20, CoverType.Fir, false);
		PowerSaplingBasalAreaPredictor pred = new PowerSaplingBasalAreaPredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		Matrix mat;
		for (int i = 0; i < 100000; i++) {
			myPlot.monteCarloId = i;
			double baPrediction = pred.predictSaplingBasalAreaM2Ha(myPlot);
			mat = new Matrix(1,1,baPrediction,0d);
			estimate.addRealization(mat);
		}
		double actualMean = estimate.getMean().getValueAt(0, 0);
		Assert.assertEquals("Testing stochastic basal area prediction", 6.45451208796443, actualMean, 0.1);
		double actualVariance = estimate.getVariance().getValueAt(0, 0);
		Assert.assertTrue("Testing stochastic variance is not null", actualVariance > 0d);
	}

	@Test
	public void test03BasalAreaInterventionResultDeterministicPrediction() {
		PowerSaplingBasalAreaAndDensityCompatiblePlotImpl myPlot = new PowerSaplingBasalAreaAndDensityCompatiblePlotImpl(20, CoverType.Maple, true);
		PowerSaplingBasalAreaPredictor pred = new PowerSaplingBasalAreaPredictor(false);
		double baPrediction = pred.predictSaplingBasalAreaM2Ha(myPlot);
		Assert.assertEquals("Testing deterministic basal area prediction", 2.6289032101907064, baPrediction, 1E-8);
	}

	@Test
	public void test04BasalAreaInterventionResultStochasticPrediction() {
		PowerSaplingBasalAreaAndDensityCompatiblePlotImpl myPlot = new PowerSaplingBasalAreaAndDensityCompatiblePlotImpl(20, CoverType.Maple, true);
		PowerSaplingBasalAreaPredictor pred = new PowerSaplingBasalAreaPredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		Matrix mat;
		for (int i = 0; i < 100000; i++) {
			myPlot.monteCarloId = i;
			double baPrediction = pred.predictSaplingBasalAreaM2Ha(myPlot);
			mat = new Matrix(1,1,baPrediction,0d);
			estimate.addRealization(mat);
		}
		double actualMean = estimate.getMean().getValueAt(0, 0);
		Assert.assertEquals("Testing stochastic basal area prediction", 2.6289032101907064, actualMean, 0.1);
		double actualVariance = estimate.getVariance().getValueAt(0, 0);
		Assert.assertTrue("Testing stochastic variance is not null", actualVariance > 0d);
	}

	@Test
	public void test10DensityDeterministicPrediction() {
		PowerSaplingBasalAreaAndDensityCompatiblePlotImpl myPlot = new PowerSaplingBasalAreaAndDensityCompatiblePlotImpl(20, CoverType.Fir, false);
		PowerSaplingNumberPredictor pred = new PowerSaplingNumberPredictor(false);
		double densityPrediction = pred.predictSaplingDensityTreeHa(myPlot);
		Assert.assertEquals("Testing deterministic density prediction", 17.721426596284417, densityPrediction, 1E-8);
	}

	@Test
	public void test11DensityStochasticPrediction() {
		PowerSaplingBasalAreaAndDensityCompatiblePlotImpl myPlot = new PowerSaplingBasalAreaAndDensityCompatiblePlotImpl(20, CoverType.Fir, false);
		PowerSaplingNumberPredictor pred = new PowerSaplingNumberPredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		Matrix mat;
		for (int i = 0; i < 1000000; i++) {
			myPlot.monteCarloId = i;
			double densityPrediction = pred.predictSaplingDensityTreeHa(myPlot);
			mat = new Matrix(1,1,densityPrediction,0d);
			estimate.addRealization(mat);
		}
		double actualMean = estimate.getMean().getValueAt(0, 0);
		Assert.assertEquals("Testing stochastic sapling number prediction",  17.721426596284417, actualMean, .1);
		double actualVariance = estimate.getVariance().getValueAt(0, 0);
		Assert.assertEquals("Testing stochastic sapling number prediction", 17.721426596284417, actualVariance, .1);
	}

	
}
