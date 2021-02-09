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
package canforservutility.predictor.disturbances.sprucebudworm.occurrence.boulangerarsenault2004;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import repicea.simulation.disturbances.DisturbanceOccurrences;
import repicea.simulation.disturbances.DisturbanceTypeProvider.DisturbanceType;



public class SpruceBudwormOutbreakOccurrencePredictorTest {

	@Test
	public void simpleTest() {
		List<SpruceBudwormOutbreakOccurrencePlot> plots = new ArrayList<SpruceBudwormOutbreakOccurrencePlot>();
		for (int time = 0; time < 90; time++) {
			plots.add(new SpruceBudwormOutbreakOccurrencePlotImpl(time, 0));
		}
		double probSurv = 1d;
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		Map<String, Object> predParms = new HashMap<String, Object>();
		predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmCurrentDateYr, 0);
		for (SpruceBudwormOutbreakOccurrencePlot plot : plots) {
			double prob = predictor.predictEventProbability(plot, null, predParms);
			probSurv *= (1 - prob);
			List<Double> parms = predictor.getParameters(plot);
			double expected = predictor.getSurvivorFunctionResult(plot.getTimeSinceLastDisturbanceYrs(DisturbanceType.SpruceBudwormOutbreak, 0) + .5, 1d / parms.get(0), parms.get(1));
			Assert.assertEquals("Testing time = " + plot.getTimeSinceLastDisturbanceYrs(DisturbanceType.SpruceBudwormOutbreak, 0), expected, probSurv, 1E-8);
			
			System.out.println("Time since last outbreak " + plot.getTimeSinceLastDisturbanceYrs(DisturbanceType.SpruceBudwormOutbreak, 0) +
					" prob = " + prob + 
					" prob surv = " + probSurv +
					" theoretical prob = " + (predictor.getSurvivorFunctionResult(plot.getTimeSinceLastDisturbanceYrs(DisturbanceType.SpruceBudwormOutbreak, 0) + .5, 1d / parms.get(0), parms.get(1))));
		}
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo0() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 0);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		Map<String, Object> predParms = new HashMap<String, Object>();
		predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmCurrentDateYr, 0);
		double prob = predictor.predictEventProbability(plot, null, predParms);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(0), 0.07326448875128741, prob, 1E-4);
	}

	
	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo10() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 10);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		Map<String, Object> predParms = new HashMap<String, Object>();
		predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmCurrentDateYr, 0);
		double prob = predictor.predictEventProbability(plot, null, predParms);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(0), 0.07362230035437763, prob, 1E-4);
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo40() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 40);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		Map<String, Object> predParms = new HashMap<String, Object>();
		predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmCurrentDateYr, 0);
		double prob = predictor.predictEventProbability(plot, null, predParms);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(0), 0.11594906884437328, prob, 1E-4);
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo50() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 50);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		Map<String, Object> predParms = new HashMap<String, Object>();
		predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmCurrentDateYr, 0);
		double prob = predictor.predictEventProbability(plot, null, predParms);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(0), 0.1585175051027792, prob, 1E-4);
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo60() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 60);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		Map<String, Object> predParms = new HashMap<String, Object>();
		predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmCurrentDateYr, 0);
		double prob = predictor.predictEventProbability(plot, null, predParms);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(0), 0.21806173583507135, prob, 1E-4);
	}
	
	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo70() {
		SpruceBudwormOutbreakOccurrencePlot plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 70);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(false, true);
		Map<String, Object> predParms = new HashMap<String, Object>();
		predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmCurrentDateYr, 0);
		double prob = predictor.predictEventProbability(plot, null, predParms);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(0), 0.2928574702983688, prob, 1E-4);
	}
	
	@Test
	public void simpleStochasticTest() {
		int nbRealizations = 1000000;
		SpruceBudwormOutbreakOccurrencePlotImpl plot = new SpruceBudwormOutbreakOccurrencePlotImpl(10, 0);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(true, true);
		double prob = 0;
		Map<String, Object> predParms = new HashMap<String, Object>();
		predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmCurrentDateYr, 2000);
		for (int i = 0; i < nbRealizations; i++) {
			DisturbanceOccurrences occ = new DisturbanceOccurrences(null);
			predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmDisturbanceOccurrences, occ);
			plot.setMonteCarloId(i);
			prob += predictor.predictEventProbability(plot, null, predParms);
		}
		double actual = prob / nbRealizations;
		Assert.assertEquals("Testing stochastic probability", 0.00256476199729598, actual, 1E-4);
	}

	@Test
	public void simpleStochasticTest2() {
		int nbRealizations = 1000000;
		SpruceBudwormOutbreakOccurrencePlotImpl plot = new SpruceBudwormOutbreakOccurrencePlotImpl(20, 0);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(true, true);
		double prob = 0;
		Map<String, Object> predParms = new HashMap<String, Object>();
		predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmCurrentDateYr, 2000);
		for (int i = 0; i < nbRealizations; i++) {
			DisturbanceOccurrences occ = new DisturbanceOccurrences(null);
			predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmDisturbanceOccurrences, occ);
			plot.setMonteCarloId(i);
			prob += predictor.predictEventProbability(plot, null, predParms);
		}
		double actual = prob / nbRealizations;
		Assert.assertEquals("Testing stochastic probability", 0.011359786416281268, actual, 2E-5);
	}

	
	@Test
	public void simpleStochasticTest3() {
		int nbRealizations = 1000000;
		SpruceBudwormOutbreakOccurrencePlotImpl plot = new SpruceBudwormOutbreakOccurrencePlotImpl(10, 0);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(true, true);
		double prob = 0;
		Map<String, Object> predParms = new HashMap<String, Object>();
		predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmCurrentDateYr, 2000);
		for (int i = 0; i < nbRealizations; i++) {
			DisturbanceOccurrences occ = new DisturbanceOccurrences(null, 1995);
			predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmDisturbanceOccurrences, occ);
			plot.setMonteCarloId(i);
			prob += predictor.predictEventProbability(plot, null, predParms);
		}
		double actual = prob / nbRealizations;
		Assert.assertEquals("Testing stochastic probability", 7.6257390949E-4, actual, 1E-5);
	}

	@Test
	public void simpleStochasticTest4() {
		int nbRealizations = 1000000;
		SpruceBudwormOutbreakOccurrencePlotImpl plot = new SpruceBudwormOutbreakOccurrencePlotImpl(10, 0);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor(true, true);
		double prob = 0;
		Map<String, Object> predParms = new HashMap<String, Object>();
		predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmCurrentDateYr, 2000);
		for (int i = 0; i < nbRealizations; i++) {
			DisturbanceOccurrences occ = new DisturbanceOccurrences(null, 2000);
			predParms.put(SpruceBudwormOutbreakOccurrencePredictor.ParmDisturbanceOccurrences, occ);
			plot.setMonteCarloId(i);
			prob += predictor.predictEventProbability(plot, null, predParms);
		}
		double actual = prob / nbRealizations;
		Assert.assertEquals("Testing stochastic probability", 3.7217079246087332E-5, actual, 4E-6);
	}

}
