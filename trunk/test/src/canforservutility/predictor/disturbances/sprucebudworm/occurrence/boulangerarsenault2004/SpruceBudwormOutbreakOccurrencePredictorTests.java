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
package canforservutility.predictor.disturbances.sprucebudworm.occurrence.belangerarsenault2004;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class SpruceBudwormOutbreakOccurrencePredictorTests {

	@Test
	public void simpleTest() {
		List<SpruceBudwormOutbreakOccurrencePlot> plots = new ArrayList<SpruceBudwormOutbreakOccurrencePlot>();
		for (int time = 0; time < 90; time++) {
			plots.add(new SpruceBudwormOutbreakOccurrencePlotImpl(time, 0));
		}
		double probSurv = 1d;
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		for (SpruceBudwormOutbreakOccurrencePlot plot : plots) {
			double prob = predictor.predictEventProbability(plot, null, 0);
			probSurv *= (1 - prob);
			List<Double> parms = predictor.getParameters(plot);
			double expected = predictor.getSurvivorFunctionResult(plot.getTimeSinceLastDisturbanceYrs(0) + .5, 1d / parms.get(0), parms.get(1));
			Assert.assertEquals("Testing time = " + plot.getTimeSinceLastDisturbanceYrs(0), expected, probSurv, 1E-8);
			
			System.out.println("Time since last outbreak " + plot.getTimeSinceLastDisturbanceYrs(0) +
					" prob = " + prob + 
					" prob surv = " + probSurv +
					" theoretical prob = " + (predictor.getSurvivorFunctionResult(plot.getTimeSinceLastDisturbanceYrs(0) + .5, 1d / parms.get(0), parms.get(1))));
		}
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo0() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 0);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		double prob = predictor.predictEventProbability(plot, null, 0);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(0), 0.07074982054535149, prob, 1E-4);
	}

	
	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo10() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 10);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		double prob = predictor.predictEventProbability(plot, null, 0);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(0), 0.07114106543167505, prob, 1E-4);
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo40() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 40);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		double prob = predictor.predictEventProbability(plot, null, 0);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(0), 0.1111014556880801, prob, 1E-4);
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo50() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 50);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		double prob = predictor.predictEventProbability(plot, null, 0);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(0), 0.14974957007332415, prob, 1E-4);
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo60() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 60);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		double prob = predictor.predictEventProbability(plot, null, 0);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(0), 0.2031131201163095, prob, 1E-4);
	}
	
	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo70() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 70);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		double prob = predictor.predictEventProbability(plot, null, 0);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(0), 0.26971204226355155, prob, 1E-4);
	}
	
	
}
