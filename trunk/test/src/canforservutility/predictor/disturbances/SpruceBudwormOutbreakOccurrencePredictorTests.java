/*
 * This file is part of the mrnf-foresttools library
 *
 * Copyright (C) 2019 Mathieu Fortin - Canadian Forest Service
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package canforservutility.predictor.disturbances;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class SpruceBudwormOutbreakOccurrencePredictorTests {

	@Test
	public void simpleTest() {
		List<SpruceBudwormOutbreakOccurrencePlot> plots = new ArrayList<SpruceBudwormOutbreakOccurrencePlot>();
		for (int time = 1; time < 80; time++) {
			plots.add(new SpruceBudwormOutbreakOccurrencePlotImpl(time, 0));
		}
		double probSurv = 1d;
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor();
		for (SpruceBudwormOutbreakOccurrencePlot plot : plots) {
			double prob = predictor.predictEventProbability(plot, null);
			probSurv *= (1 - prob);
			double expected = 1 - predictor.getCumulativeProbability(plot.getTimeSinceLastOutbreakYrs());
			Assert.assertEquals("Testing time = " + plot.getTimeSinceLastOutbreakYrs(), expected, probSurv, 1E-4);
			
			System.out.println("Time since last outbreak " + plot.getTimeSinceLastOutbreakYrs() +
					" prob = " + prob + 
					" prob surv = " + probSurv +
					" theoretical prob = " + (1 - predictor.getCumulativeProbability(plot.getTimeSinceLastOutbreakYrs())));
		}
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo0() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 0);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor();
		double prob = predictor.predictEventProbability(plot, null);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceInitialKnownDateYrs(), 0.15224427547055028, prob, 1E-4);
	}

	
	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo10() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 10);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor();
		double prob = predictor.predictEventProbability(plot, null);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceInitialKnownDateYrs(), 0.17430432432266457, prob, 1E-4);
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo40() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 40);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor();
		double prob = predictor.predictEventProbability(plot, null);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceInitialKnownDateYrs(), 0.28992474378256194, prob, 1E-4);
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo50() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 50);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor();
		double prob = predictor.predictEventProbability(plot, null);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceInitialKnownDateYrs(), 0.34326907448868305, prob, 1E-4);
	}

	
	
	
}
