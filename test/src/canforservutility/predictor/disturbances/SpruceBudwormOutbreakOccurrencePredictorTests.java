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

import repicea.simulation.covariateproviders.standlevel.NaturalDisturbanceInformationProvider;


public class SpruceBudwormOutbreakOccurrencePredictorTests {

	@Test
	public void simpleTest() {
		List<NaturalDisturbanceInformationProvider> plots = new ArrayList<NaturalDisturbanceInformationProvider>();
		for (int time = 1; time < 80; time++) {
			plots.add(new SpruceBudwormOutbreakOccurrencePlotImpl(time, 0));
		}
		double probSurv = 1d;
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor();
		for (NaturalDisturbanceInformationProvider plot : plots) {
			double prob = predictor.predictEventProbability(plot, null);
			probSurv *= (1 - prob);
			double expected = 1 - predictor.getCumulativeProbability(plot.getTimeSinceLastDisturbanceYrs());
			Assert.assertEquals("Testing time = " + plot.getTimeSinceLastDisturbanceYrs(), expected, probSurv, 1E-4);
			
			System.out.println("Time since last outbreak " + plot.getTimeSinceLastDisturbanceYrs() +
					" prob = " + prob + 
					" prob surv = " + probSurv +
					" theoretical prob = " + (1 - predictor.getCumulativeProbability(plot.getTimeSinceLastDisturbanceYrs())));
		}
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo0() {
		NaturalDisturbanceInformationProvider plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 0);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor();
		double prob = predictor.predictEventProbability(plot, null);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(), 0.09748353780787794, prob, 1E-4);
	}

	
	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo10() {
		NaturalDisturbanceInformationProvider plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 10);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor();
		double prob = predictor.predictEventProbability(plot, null);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(), 0.09751270828641204, prob, 1E-4);
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo40() {
		NaturalDisturbanceInformationProvider plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 40);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor();
		double prob = predictor.predictEventProbability(plot, null);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(), 0.1505875910576909, prob, 1E-4);
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo50() {
		NaturalDisturbanceInformationProvider plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 50);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor();
		double prob = predictor.predictEventProbability(plot, null);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(), 0.21800952540500182, prob, 1E-4);
	}

	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo60() {
		NaturalDisturbanceInformationProvider plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 60);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor();
		double prob = predictor.predictEventProbability(plot, null);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(), 0.2969216601745537, prob, 1E-4);
	}
	
	@Test
	public void simpleTestWithUnknownTimeOfLastOccurrenceAndInitialTimeSetTo70() {
		NaturalDisturbanceInformationProvider plot = new SpruceBudwormOutbreakOccurrencePlotImpl(null, 70);
		SpruceBudwormOutbreakOccurrencePredictor predictor = new SpruceBudwormOutbreakOccurrencePredictor();
		double prob = predictor.predictEventProbability(plot, null);
		System.out.println("Prob = " + prob);
		Assert.assertEquals("Testing initial time = " + plot.getTimeSinceFirstKnownDateYrs(), 0.39408657334221525, prob, 1E-4);
	}
	
	
}
