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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.disturbances.DisturbanceParameter;

/**
 * The SimpleRecurrenceBasedDisturbancePredictor class implements a lifetime model based on a simple exponential distribution. 
 * If the predictor is run in stochastic mode, the outcome (event / non event) is recorded so that the same outcome will be produced
 * in the same conditions for consistency.
 * @author Mathieu Fortin - April 2019
 */
@SuppressWarnings("serial")
public class SimpleRecurrenceBasedDisturbancePredictor extends REpiceaBinaryEventPredictor<MonteCarloSimulationCompliantObject, Object>{

	
	/**
	 * This inner class contains the parameters for the predictor to work, namely an estimated recurrence and its variance (which can be equal to 0).
	 * Instances of this class must be passed to the predictor in order to choose the proper internal predictor. There exists one internal predictor for
	 * each different combination of estimated recurrence and variance.
	 * @author Mathieu Fortin - April 2019
	 */
	public static class SimpleRecurrenceBasedDisturbanceParameters extends ArrayList<Double> {

		/**
		 * Constructor.
		 * @param estimatedRecurrence a strictly positive double
		 * @param variance a positive double (can be equal to 0)
		 */
		public SimpleRecurrenceBasedDisturbanceParameters(double estimatedRecurrence, double variance) {
			if (estimatedRecurrence <= 0 || variance < 0) {
				throw new InvalidParameterException("The estimated recurrence and the variance must be positive!");
			}
			if (estimatedRecurrence - 2 * Math.sqrt(variance) <= 0) {
				throw new InvalidParameterException("The variance is too large so that negative recurrence could be generated in stochastic mode!");
			}
			add(estimatedRecurrence);
			add(variance);
		}

		double getEstimatedRecurrence() {return get(0);}
		double getVariance() {return get(1);}
	}
	
	protected final Map<SimpleRecurrenceBasedDisturbanceParameters, SimpleRecurrenceBasedDisturbanceInternalPredictor> internalPredictorMap;

	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to run the model in stochastic mode or false to run in deterministic mode
	 */
	public SimpleRecurrenceBasedDisturbancePredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);
	}

	
	protected SimpleRecurrenceBasedDisturbancePredictor(boolean isParameterVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled); // no random effects here
		internalPredictorMap = new HashMap<SimpleRecurrenceBasedDisturbanceParameters, SimpleRecurrenceBasedDisturbanceInternalPredictor>();
	}
	
	private synchronized SimpleRecurrenceBasedDisturbanceInternalPredictor getInternalPredictor(SimpleRecurrenceBasedDisturbanceParameters selectedParms) {
		if (!internalPredictorMap.containsKey(selectedParms)) {
			internalPredictorMap.put(selectedParms, new SimpleRecurrenceBasedDisturbanceInternalPredictor(isParametersVariabilityEnabled, isResidualVariabilityEnabled, selectedParms));
		}
		return internalPredictorMap.get(selectedParms);
	}
	
	@Override
	public double predictEventProbability(MonteCarloSimulationCompliantObject stand, Object tree, Map<Integer, Object> parms) {
		SimpleRecurrenceBasedDisturbanceParameters selectedParms = (SimpleRecurrenceBasedDisturbanceParameters) parms.get(DisturbanceParameter.ParmSimpleRecurrenceBasedParameters);
		if (selectedParms == null) {
			throw new InvalidParameterException("The SimpleRecurrenceBasedDisturbancePredictor.predictEventProbability() requires a SimpleRecurrenceBasedDisturbanceParameters instance in the parms argument!");
		} else {
			return getInternalPredictor(selectedParms).predictEventProbability(stand, tree, parms);
		}
	}

	@Override
	protected void init() {}
	
	@Override
	public Object predictEvent(MonteCarloSimulationCompliantObject stand, Object tree, Map<Integer, Object> parms) {
		SimpleRecurrenceBasedDisturbanceParameters selectedParms = (SimpleRecurrenceBasedDisturbanceParameters) parms.get(DisturbanceParameter.ParmSimpleRecurrenceBasedParameters);
		if (selectedParms == null) {
			throw new InvalidParameterException("The SimpleRecurrenceBasedDisturbancePredictor.predictEventProbability() requires a SimpleRecurrenceBasedDisturbanceParameters instance in the parms argument!");
		} else {
			return getInternalPredictor(selectedParms).predictEvent(stand, tree, parms);
		}
	}
	
}
