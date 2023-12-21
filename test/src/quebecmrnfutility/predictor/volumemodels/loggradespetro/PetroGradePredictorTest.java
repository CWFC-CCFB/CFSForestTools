/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.volumemodels.loggradespetro;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradeTree.PetroGradeSpecies;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcHarvestPriorityProvider.QcHarvestPriority;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcTreeQualityProvider.QcTreeQuality;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcVigorClassProvider.QcVigorClass;
import repicea.math.Matrix;
import repicea.stats.estimates.MonteCarloEstimate;

public class PetroGradePredictorTest {

	@Test
	public void testWithBasicVersion() throws FileNotFoundException, IOException {
		
		PetroGradeTreeImpl tree = new PetroGradeTreeImpl(PetroGradeSpecies.ERS, 50);
		
		PetroGradePredictor stoPredictor = new PetroGradePredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		
		for (int realization = 0; realization < 50000; realization++) {
			tree.setRealization(realization);
			estimate.addRealization(stoPredictor.getPredictedGradeUnderbarkVolumes(tree));
		}
	
		PetroGradePredictor detPredictor = new PetroGradePredictor(false);
		Matrix expected = detPredictor.getPredictedGradeUnderbarkVolumes(tree);
		
		Matrix actual = estimate.getMean();

		double relDiff;
		for (int i = 0; i < actual.m_iRows; i++) {
			relDiff = Math.abs(1 - actual.getValueAt(i, 0) / expected.getValueAt(i, 0)); 
			Assert.assertEquals(0, relDiff, .05);
		}
	}
	
	
	
	
	@Test
	public void testWithMSCR() throws FileNotFoundException, IOException {
			
		PetroGradeTreeImpl tree = new PetroGradeTreeImpl(PetroGradeSpecies.ERS, 50, QcHarvestPriority.C);
		
		PetroGradePredictor stoPredictor = new PetroGradePredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		
		for (int realization = 0; realization < 10000; realization++) {
			tree.setRealization(realization);
			estimate.addRealization(stoPredictor.getPredictedGradeUnderbarkVolumes(tree));
		}
	
		PetroGradePredictor detPredictor = new PetroGradePredictor(false);
		Matrix expected = detPredictor.getPredictedGradeUnderbarkVolumes(tree);
		
		Matrix actual = estimate.getMean();

		double relDiff;
		for (int i = 0; i < actual.m_iRows; i++) {
			relDiff = Math.abs(1 - actual.getValueAt(i, 0) / expected.getValueAt(i, 0)); 
			Assert.assertEquals(0, relDiff, .1);
		}
	}

	
	@Test
	public void testWithABCD() throws FileNotFoundException, IOException {
		
		
		PetroGradeTreeImpl tree = new PetroGradeTreeImpl(PetroGradeSpecies.ERS, 50, QcTreeQuality.B);
		
		PetroGradePredictor stoPredictor = new PetroGradePredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		
		for (int realization = 0; realization < 10000; realization++) {
			tree.setRealization(realization);
			estimate.addRealization(stoPredictor.getPredictedGradeUnderbarkVolumes(tree));
		}
	
		PetroGradePredictor detPredictor = new PetroGradePredictor(false);
		Matrix expected = detPredictor.getPredictedGradeUnderbarkVolumes(tree);
		
		Matrix actual = estimate.getMean();

		double relDiff;
		for (int i = 0; i < actual.m_iRows; i++) {
			relDiff = Math.abs(1 - actual.getValueAt(i, 0) / expected.getValueAt(i, 0)); 
			Assert.assertEquals(0, relDiff, .1);
		}
			
	}

	
	@Test
	public void testWithVigor() throws FileNotFoundException, IOException {
		
		PetroGradeTreeImpl tree = new PetroGradeTreeImpl(PetroGradeSpecies.ERS, 50, QcVigorClass.V2);
		
		PetroGradePredictor stoPredictor = new PetroGradePredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		
		for (int realization = 0; realization < 10000; realization++) {
			tree.setRealization(realization);
			estimate.addRealization(stoPredictor.getPredictedGradeUnderbarkVolumes(tree));
		}
	
		PetroGradePredictor detPredictor = new PetroGradePredictor(false);
		Matrix expected = detPredictor.getPredictedGradeUnderbarkVolumes(tree);
		
		Matrix actual = estimate.getMean();

		double relDiff;
		double diff;
		for (int i = 0; i < actual.m_iRows; i++) {
			diff = Math.abs(actual.getValueAt(i, 0) - expected.getValueAt(i, 0)); 
			relDiff = Math.abs(1 - actual.getValueAt(i, 0) / expected.getValueAt(i, 0)); 
			Assert.assertTrue(relDiff < 0.05 || diff < 0.02);
		}
			
	}


}
