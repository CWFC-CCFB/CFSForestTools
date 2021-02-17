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

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import canforservutility.predictor.disturbances.SimpleRecurrenceBasedDisturbancePredictor.SimpleRecurrenceBasedDisturbanceParameters;
import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.disturbances.DisturbanceParameter;
import repicea.stats.StatisticalUtility;

/**
 * The SimpleRecurrenceBasedDisturbancePredictor class implements a lifetime model based on a simple exponential distribution. 
 * If the predictor is run in stochastic mode, the outcome (event / non event) is recorded so that the same outcome will be produced
 * in the same conditions for consistency.
 * @author Mathieu Fortin - April 2019
 */
class SimpleRecurrenceBasedDisturbanceInternalPredictor extends REpiceaBinaryEventPredictor<MonteCarloSimulationCompliantObject, Object>{

	protected final Map<Integer, Map<Integer, Map<Number, Boolean>>>  recorderMap; // Monte Carlo id / current date / parameter

	SimpleRecurrenceBasedDisturbanceInternalPredictor(boolean isParameterVariabilityEnabled, boolean isResidualVariabilityEnabled, SimpleRecurrenceBasedDisturbanceParameters recurrenceParameters) {
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled); // no random effects here
		
		if (recurrenceParameters == null) {
			throw new InvalidParameterException("The recurrenceParameters argument cannot be null!");
		}
		
		Matrix mean = new Matrix(1,1);
		mean.m_afData[0][0] = recurrenceParameters.getEstimatedRecurrence();
		Matrix variance = new Matrix(1,1);
		variance.m_afData[0][0] = recurrenceParameters.getVariance();
		
		ModelParameterEstimates meanRecurrence = new ModelParameterEstimates(mean, variance);
		setParameterEstimates(meanRecurrence);
		
		recorderMap = new HashMap<Integer, Map<Integer, Map<Number, Boolean>>>();
	}

	
	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.REpiceaPredictor#init()
	 */
	@Override
	protected void init() {}

	
	@Override
	public double predictEventProbability(MonteCarloSimulationCompliantObject stand, Object tree, Map<String, Object> parms) {
		Matrix beta = getParametersForThisRealization(stand);
		double recurrence = beta.m_afData[0][0];
		return 1 - Math.exp(-1d/recurrence);
	}

	
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
			double residualError = StatisticalUtility.getRandom().nextDouble();
			innerInnerMap.put(parameter, residualError < probability);
		}
		return innerInnerMap.get(parameter);
	}


	@Override
	public Object predictEvent(MonteCarloSimulationCompliantObject plotSample, Object tree, Map<String, Object> parms) {
		double eventProbability = predictEventProbability(plotSample, tree);	// parms are not needed here
		if (eventProbability < 0 || eventProbability > 1) {
			return null;
		} else if (isResidualVariabilityEnabled) {
//			int currentDateYrs = (Integer) REpiceaBinaryEventPredictor.findFirstParameterOfThisClass(Integer.class, parms);
			int currentDateYrs = (Integer) parms.get(DisturbanceParameter.ParmCurrentDateYr);
			double recurrence = getParameterEstimates().getMean().m_afData[0][0];
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
