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

import java.util.HashMap;
import java.util.Map;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.REpiceaBinaryEventPredictor;

/**
 * The SimpleRecurrenceBasedDisturbancePredictor class implements a lifetime model based on a simple exponential distribution. 
 * If the predictor is run in stochastic mode, the outcome (event / non event) is recorded so that the same outcome will be produced
 * in the same conditions for consistency.
 * @author Mathieu Fortin - April 2019
 */
@SuppressWarnings("serial")
public class SimpleRecurrenceBasedDisturbancePredictor<P extends MonteCarloSimulationCompliantObject> extends REpiceaBinaryEventPredictor<P, Object>{

	protected final Map<Integer, Map<Integer, Map<Number, Boolean>>>  recorderMap; // Monte Carlo id / current date / parameter

	/**
	 * Constructor.
	 * @param isResidualVariabilityEnabled true to run the model in stochastic mode or false to run in deterministic mode
	 */
	public SimpleRecurrenceBasedDisturbancePredictor(boolean isResidualVariabilityEnabled) {
		super(false, false, isResidualVariabilityEnabled);
		recorderMap = new HashMap<Integer, Map<Integer, Map<Number, Boolean>>>();
	}

	@Override
	public double predictEventProbability(P stand, Object tree, Object... parms) {
		double recurrence = (Double) parms[1];
		return 1 - Math.exp(-1d/recurrence);
	}

	@Override
	protected void init() {}
	
	protected synchronized boolean getResidualError(Map<Integer, Map<Integer, Map<Number, Boolean>>> oMap,
			int monteCarloRealization, 
			int currentDate, 
			double parameter, 
			double probability) {
		if (!oMap.containsKey(monteCarloRealization)) {
			oMap.put(monteCarloRealization, new HashMap<Integer, Map<Number, Boolean>>());
		}
		Map<Integer, Map<Number, Boolean>> innerMap = oMap.get(monteCarloRealization);
		if (!innerMap.containsKey(currentDate)) {
			innerMap.put(currentDate, new HashMap<Number, Boolean>());
		}
		Map<Number, Boolean> innerInnerMap = innerMap.get(currentDate);
		if (!innerInnerMap.containsKey(parameter)) {
			double residualError = random.nextDouble();
			innerInnerMap.put(parameter, residualError < probability);
		}
		return innerInnerMap.get(parameter);
	}


	@Override
	public Object predictEvent(P plotSample, Object tree, Object... parms) {
		double eventProbability = predictEventProbability(plotSample, tree, parms);
		if (eventProbability < 0 || eventProbability > 1) {
			return null;
		} else if (isResidualVariabilityEnabled) {
			int currentDateYrs = (Integer) parms[0];
			double recurrence = (Double) parms[1];
			return getResidualError(recorderMap,
					plotSample.getMonteCarloRealizationId(), 
					currentDateYrs, 
					recurrence, 
					eventProbability);
		} else {
			return eventProbability;
		}
	}


}
