/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
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
package quebecmrnfutility.predictor.deadwood;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.predictor.deadwood.ResefDeadWoodCompatiblePlot.ResefForestType;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.stats.estimates.MonteCarloEstimate;

public class ResefDeadWoodPredictorTest {

	static class ResefDeadWoodCompatiblePlotImpl implements ResefDeadWoodCompatiblePlot {

		final ResefForestType forestType;
		int monteCarloId;
		
		ResefDeadWoodCompatiblePlotImpl(ResefForestType forestType) {
			this.forestType = forestType;
		}
		
		@Override
		public double getAreaHa() {
			return 0.04;
		}

		@Override
		public String getSubjectId() {
			return "This plot";
		}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {
			return HierarchicalLevel.PLOT;
		}

		@Override
		public int getMonteCarloRealizationId() {
			return monteCarloId;
		}

		@Override
		public ResefForestType getResefForestType() {
			return forestType;
		}
		
	}
	
	@Test
	public void testDeadWoodPredictionsSugarMaple() {
		ResefDeadWoodPredictor detPredictor = new ResefDeadWoodPredictor(false);
		ResefDeadWoodPredictor stoPredictor = new ResefDeadWoodPredictor(true);
		ResefDeadWoodCompatiblePlotImpl plot = new ResefDeadWoodCompatiblePlotImpl(ResefForestType.SugarMapleDominatedStand);
		MonteCarloEstimate mcEst = new MonteCarloEstimate();
		Matrix real;	
		for (int i = 0; i < 100000; i++) {
			plot.monteCarloId = i;
			real = new Matrix(1,1);
			real.setValueAt(0, 0, stoPredictor.predictDeadWoodBiomassMg(plot, true));
			mcEst.addRealization(real);
		}
		
		double stochasticMean = mcEst.getMean().getValueAt(0, 0);
		double stochasticVariance = mcEst.getVariance().getValueAt(0, 0);
		Assert.assertEquals("Testing means", 25.30773903, stochasticMean, 1E-1);
		double expectedVariance = 7.750338908 * 7.750338908;
		Assert.assertEquals("Testing variances", expectedVariance, stochasticVariance, 1);
		Assert.assertEquals("Testing deterministic means", 25.30773903, detPredictor.predictDeadWoodBiomassMg(plot, true), 1E-8);
	}
	
	
	@Test
	public void testDeadWoodPredictionsSpruce() {
		ResefDeadWoodPredictor detPredictor = new ResefDeadWoodPredictor(false);
		ResefDeadWoodPredictor stoPredictor = new ResefDeadWoodPredictor(true);
		ResefDeadWoodCompatiblePlotImpl plot = new ResefDeadWoodCompatiblePlotImpl(ResefForestType.SpruceDominatedStand);
		MonteCarloEstimate mcEst = new MonteCarloEstimate();
		Matrix real;	
		for (int i = 0; i < 100000; i++) {
			plot.monteCarloId = i;
			real = new Matrix(1,1);
			real.setValueAt(0, 0, stoPredictor.predictDeadWoodBiomassMg(plot, true));
			mcEst.addRealization(real);
		}
		
		double stochasticMean = mcEst.getMean().getValueAt(0, 0);
		double stochasticVariance = mcEst.getVariance().getValueAt(0, 0);
		Assert.assertEquals("Testing means", 11.95025186, stochasticMean, 1E-1);
		double expectedVariance = 2.380472303 * 2.380472303;
		Assert.assertEquals("Testing variances", expectedVariance, stochasticVariance, 1);
		Assert.assertEquals("Testing deterministic means", 11.95025186, detPredictor.predictDeadWoodBiomassMg(plot, true), 1E-8);
	}

	@Test
	public void testDeadWoodPredictionsFir() {
		ResefDeadWoodPredictor detPredictor = new ResefDeadWoodPredictor(false);
		ResefDeadWoodPredictor stoPredictor = new ResefDeadWoodPredictor(true);
		ResefDeadWoodCompatiblePlotImpl plot = new ResefDeadWoodCompatiblePlotImpl(ResefForestType.FirDominatedStand);
		MonteCarloEstimate mcEst = new MonteCarloEstimate();
		Matrix real;	
		for (int i = 0; i < 100000; i++) {
			plot.monteCarloId = i;
			real = new Matrix(1,1);
			real.setValueAt(0, 0, stoPredictor.predictDeadWoodBiomassMg(plot, true));
			mcEst.addRealization(real);
		}
		
		double stochasticMean = mcEst.getMean().getValueAt(0, 0);
		double stochasticVariance = mcEst.getVariance().getValueAt(0, 0);
		Assert.assertEquals("Testing means", 33.0540757, stochasticMean, 2E-1);
		double expectedVariance = 12.00838853 * 12.00838853;
		Assert.assertEquals("Testing variances", expectedVariance, stochasticVariance, 5);
		Assert.assertEquals("Testing deterministic means", 33.0540757, detPredictor.predictDeadWoodBiomassMg(plot, true), 1E-8);
	}

}
