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

		PowerSaplingBasalAreaAndDensityCompatiblePlotImpl(double basalAreaM2Ha, CoverType coverType) {
			this.basalAreaM2Ha = basalAreaM2Ha;
			this.coverType = coverType;
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
		
	}
	
	@Test
	public void test01BasalAreaDeterministicPrediction() {
		PowerSaplingBasalAreaAndDensityCompatiblePlotImpl myPlot = new PowerSaplingBasalAreaAndDensityCompatiblePlotImpl(20, CoverType.Fir);
		PowerSaplingBasalAreaPredictor pred = new PowerSaplingBasalAreaPredictor(false,false);
		double baPrediction = pred.predictSaplingBasalAreaM2Ha(myPlot);
		Assert.assertEquals("Testing deterministic basal area prediction", 6.074210894764617, baPrediction, 1E-8);
	}

	@Test
	public void test02BasalAreaStochasticPrediction() {
		PowerSaplingBasalAreaAndDensityCompatiblePlotImpl myPlot = new PowerSaplingBasalAreaAndDensityCompatiblePlotImpl(20, CoverType.Fir);
		PowerSaplingBasalAreaPredictor pred = new PowerSaplingBasalAreaPredictor(false, true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		Matrix mat;
		for (int i = 0; i < 100000; i++) {
			myPlot.monteCarloId = i;
			double baPrediction = pred.predictSaplingBasalAreaM2Ha(myPlot);
			mat = new Matrix(1,1,baPrediction,0d);
			estimate.addRealization(mat);
		}
		double actualMean = estimate.getMean().getValueAt(0, 0);
		Assert.assertEquals("Testing stochastic basal area prediction", 6.074210894764617, actualMean, 0.1);
		double actualVariance = estimate.getVariance().getValueAt(0, 0);
		Assert.assertTrue("Testing stochastic variance is not null", actualVariance > 0d);
	}

	@Test
	public void test03DensityDeterministicPrediction() {
		PowerSaplingBasalAreaAndDensityCompatiblePlotImpl myPlot = new PowerSaplingBasalAreaAndDensityCompatiblePlotImpl(20, CoverType.Fir);
		PowerSaplingDensityPredictor pred = new PowerSaplingDensityPredictor(false,false);
		double densityPrediction = pred.predictSaplingDensityTreeHa(myPlot);
		Assert.assertEquals("Testing deterministic density prediction", 6873.953377106523, densityPrediction, 1E-8);
	}

	@Test
	public void test04DensityStochasticPrediction() {
		PowerSaplingBasalAreaAndDensityCompatiblePlotImpl myPlot = new PowerSaplingBasalAreaAndDensityCompatiblePlotImpl(20, CoverType.Fir);
		PowerSaplingDensityPredictor pred = new PowerSaplingDensityPredictor(false, true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		Matrix mat;
		for (int i = 0; i < 1000000; i++) {
			myPlot.monteCarloId = i;
			double densityPrediction = pred.predictSaplingDensityTreeHa(myPlot);
			mat = new Matrix(1,1,densityPrediction,0d);
			estimate.addRealization(mat);
		}
		double actualMean = estimate.getMean().getValueAt(0, 0);
		Assert.assertEquals("Testing stochastic density prediction", 6873.953377106523, actualMean, 50);
		double actualVariance = estimate.getVariance().getValueAt(0, 0);
		Assert.assertTrue("Testing stochastic variance is not null", actualVariance > 0d);
	}

	// TODO implement density predictions
	// TODO implement stochastic predictions
	
}
