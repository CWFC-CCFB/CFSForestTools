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

import repicea.stats.distributions.GammaFunction;

/**
 * This class implements an occurrence model based on a lifetime analyse for spruce budworm outbreaks based 
 * on the recurrence as estimated by Boulanger and Arsenault (2004). According to their paper (p. 1041), 
 * the mean recurrence time was estimated at 39.5 years with a standard deviation of 8.6. The actual model relies
 * on a Weibull-distributed time of occurrence. 
 * This class does not implement stochastic features for the parameters. However is does for the residual error.
 * @author Mathieu Fortin - March 2019
 * @see <a href=https://doi.org/10.1139/X03-269> Boulanger, Y., and D. Arseneault. 2004. Spruce budworm
 * outbreaks in eastern Quebec over the last 450 years. Canadian Journal of Forest Research 34: 1035-1043 
 * </a>
 */
@SuppressWarnings("serial")
public final class SpruceBudwormOutbreakOccurrencePredictor extends SimpleRecurrenceBasedDisturbancePredictor<SpruceBudwormOutbreakOccurrencePlot> {

	private final double mean = 39.5;
//	private final double sd = 8.6;
	private final double beta = 5.285;	// found iteratively based on a Weibull distributed time of occurrence
	private final double lambda = 1d / mean * GammaFunction.gamma(1 + 1d/beta);	

//	private final Map<Integer, Map<Integer, Map<Number, Boolean>>>  recorderMapForKnownLastOccurrence; // Monte Carlo id / current date / time since last occurrence
	private final Map<Integer, Map<Integer, Map<Number, Boolean>>>  recorderMapForUnknownLastOccurrence; 
	
	/**
	 * Deterministic constructor. 
	 */
	public SpruceBudwormOutbreakOccurrencePredictor() {
		this(false);
	}

	/**
	 * Stochastic constructor. 
	 */
	public SpruceBudwormOutbreakOccurrencePredictor(boolean isResidualVariabilityEnabled) {
		super(isResidualVariabilityEnabled); // false : no random effect in this model	// TODO implement the stochastic features for the parameter estimates
//		recorderMapForKnownLastOccurrence = new HashMap<Integer, Map<Integer, Map<Number, Boolean>>>();
		recorderMapForUnknownLastOccurrence = new HashMap<Integer, Map<Integer, Map<Number, Boolean>>>();
	}

	@Override
	public double predictEventProbability(SpruceBudwormOutbreakOccurrencePlot plotSample, Object tree, Object... parms) {
		if (parms != null && parms.length > 0 && parms[0] instanceof Integer) {
			int currentDateYrs = (Integer) parms[0];
			Integer timeSinceLastOutbreak = plotSample.getTimeSinceLastDisturbanceYrs(currentDateYrs);
			if (timeSinceLastOutbreak == null) {		// here we have to calculate the marginal probability
				double marginalProb = 0d;
				int max = 79;
				double truncationFactor = 1d / getSurvivorFunctionResult(plotSample.getTimeSinceFirstKnownDateYrs(currentDateYrs));
				for (int time = plotSample.getTimeSinceFirstKnownDateYrs(currentDateYrs) + 1; time <= max; time++) {	// marginalized over all the possible values 
					double marginalProbability = getSurvivorFunctionResult(time - 1) -  getSurvivorFunctionResult(time);
					marginalProb += getConditionalAnnualProbabilityofOccurrence(time) * marginalProbability * truncationFactor;
				}
				return marginalProb;
			}
			return getConditionalAnnualProbabilityofOccurrence(timeSinceLastOutbreak);
		} else {
			throw new InvalidParameterException("The first parameter should be an integer which represents the current date (yrs)");
		}
	}

	
	private double getConditionalAnnualProbabilityofOccurrence(int timeSinceLastOutbreak) {
		double s0 = getSurvivorFunctionResult(timeSinceLastOutbreak - 1);
		double s1 = getSurvivorFunctionResult(timeSinceLastOutbreak);
		double probability = 1 - s1/s0;
		return probability;
	}
	
	protected double getSurvivorFunctionResult(int time) {
		double r = Math.exp(-Math.pow(lambda * time, beta));
		return r;
	}
	
	@Override
	public Object predictEvent(SpruceBudwormOutbreakOccurrencePlot plotSample, Object tree, Object... parms) {
		double eventProbability = predictEventProbability(plotSample, tree, parms);
		if (eventProbability < 0 || eventProbability > 1) {
			return null;
		} else if (isResidualVariabilityEnabled) {
			int currentDateYrs = (Integer) parms[0];
			Integer timeSinceLastOutbreak = plotSample.getTimeSinceLastDisturbanceYrs(currentDateYrs);
			if (timeSinceLastOutbreak == null) {
				int timeSinceFirstKnownDate = plotSample.getTimeSinceFirstKnownDateYrs(currentDateYrs);
				return getResidualError(recorderMapForUnknownLastOccurrence,
						plotSample.getMonteCarloRealizationId(), 
						currentDateYrs, 
						timeSinceFirstKnownDate, 
						eventProbability);
			} else {
				return getResidualError(recorderMap,		// default recorder map member in the super class
						plotSample.getMonteCarloRealizationId(), 
						currentDateYrs, 
						timeSinceLastOutbreak, 
						eventProbability);
			}
		} else {
			return eventProbability;
		}
	}

	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.REpiceaPredictor#init()
	 */
	@Override
	protected void init() {}

	
}
