/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2019 Her Majesty the Queen in Right of Canada
 * Author: Mathieu Fortin - Canadian Forest Service
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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import canforservutility.predictor.disturbances.SimpleRecurrenceBasedDisturbancePredictor.SimpleRecurrenceBasedDisturbanceParameters;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.disturbances.DisturbanceParameter;

public class SimpleRecurrenceBasedDisturbancePredictorTest {

	static class Stand implements MonteCarloSimulationCompliantObject {

		final String id;
		int monteCarloRealizationId;
		
		Stand(String id) {
			this.id = id;
		}
		
		@Override
		public String getSubjectId() {return id;}

		/*
		 * Useless in this case (non-Javadoc)
		 * @see repicea.simulation.MonteCarloSimulationCompliantObject#getHierarchicalLevel()
		 */
		@Override
		public HierarchicalLevel getHierarchicalLevel() {
			return null;
		}

		@Override
		public int getMonteCarloRealizationId() {return monteCarloRealizationId;}
		
	}
	
	/*
	 * This test checks if the predictor in stochastic mode properly records the event.
	 */
	@Test
	public void simpleRecordingOfEvent() {
		SimpleRecurrenceBasedDisturbancePredictor predictor = new SimpleRecurrenceBasedDisturbancePredictor(true, true);
		Stand s = new Stand("myStand");
		SimpleRecurrenceBasedDisturbanceParameters p = new SimpleRecurrenceBasedDisturbanceParameters(35,0);	// recurrence of 35 years without variance
		Object reference = null;
//		int currentDateYr = 2000;
		Map<String, Object> parms = new HashMap<String, Object>();
		parms.put(DisturbanceParameter.ParmCurrentDateYr, 2000);
		parms.put(DisturbanceParameter.ParmSimpleRecurrenceBasedParameters, p);
		for (int i = 0; i < 1000; i++) {
			Object result = predictor.predictEvent(s, null, parms);
			Assert.assertTrue("Testing if the same instance of internal predictor is used", predictor.internalPredictorMap.size() == 1);
			if (reference == null) {
				reference = result;
			} else {
				Assert.assertTrue("Testing if the result is the same", result.equals(reference));
			}
		}
	}
}
