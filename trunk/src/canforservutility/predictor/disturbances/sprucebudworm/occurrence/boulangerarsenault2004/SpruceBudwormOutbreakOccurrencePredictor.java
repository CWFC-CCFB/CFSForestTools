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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.disturbances.DisturbanceOccurrences;
import repicea.simulation.disturbances.DisturbanceTypeProvider.DisturbanceType;
import repicea.util.ObjectUtility;

/**
 * This class implements an occurrence model based on a lifetime analyse for spruce budworm outbreaks based 
 * on the recurrence as estimated by Boulanger and Arsenault (2004). According to their paper (p. 1041), 
 * the mean recurrence time was estimated at 39.5 years with a standard deviation of 8.36. The actual model relies
 * on a Weibull-distributed time of occurrence. 
 * @author Mathieu Fortin - March 2019
 * @see <a href=https://doi.org/10.1139/X03-269> Boulanger, Y., and D. Arseneault. 2004. Spruce budworm
 * outbreaks in eastern Quebec over the last 450 years. Canadian Journal of Forest Research 34: 1035-1043 
 * </a>
 */
@SuppressWarnings("serial")
public final class SpruceBudwormOutbreakOccurrencePredictor extends REpiceaBinaryEventPredictor<SpruceBudwormOutbreakOccurrencePlot, Object> {

	
	static class InternalParameterEstimates extends ModelParameterEstimates {

		InternalParameterEstimates(Matrix mean, Matrix variance) {
			super(mean, variance);
		}

		@Override
		public Matrix getRandomDeviate() {
			Matrix mat;
			while ((mat = super.getRandomDeviate()).anyElementSmallerOrEqualTo(0d)) {};	// to ensure that none of the parameter estimates is smaller than or equal to 0
			return mat;
		}
	}

	private final Map<Integer, Map<Integer, Map<Number, Boolean>>>  recorderMap; // Monte Carlo id / current date / parameter
	private final Map<Integer, Map<Integer, Map<Number, Boolean>>>  recorderMapForUnknownLastOccurrence; 
	

	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 */
	public SpruceBudwormOutbreakOccurrencePredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled); // false : no random effect in this model
	}

	
	protected SpruceBudwormOutbreakOccurrencePredictor(boolean isParameterVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled); // false : no random effect in this model
		recorderMap = new HashMap<Integer, Map<Integer, Map<Number, Boolean>>>();
		recorderMapForUnknownLastOccurrence = new HashMap<Integer, Map<Integer, Map<Number, Boolean>>>();
		init();
	}

	
	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.REpiceaPredictor#init()
	 */
	@Override
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_beta_wei.csv";
			String omegaFilename = path + "0_omega_wei.csv";
			
			Matrix defaultBetaMean = ParameterLoader.loadVectorFromFile(betaFilename).get();
			Matrix defaultBetaVariance = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();

			setParameterEstimates(new InternalParameterEstimates(defaultBetaMean, defaultBetaVariance));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to load the parameters of the SpruceBudwormOutbreakOccurrencePredictor class!");
		}
	}

	
	protected List<Double> getParameters(SpruceBudwormOutbreakOccurrencePlot plotSample) {
		Matrix recurrence = getParametersForThisRealization(plotSample);
//		double betaParm = calculateBeta(recurrence.m_afData[0][0], 1, 10);
//		double lambdaParm = calculateLambda(betaParm, recurrence.m_afData[0][0]);
		List<Double> parameters = new ArrayList<Double>();
		parameters.add(recurrence.m_afData[0][0]);
		parameters.add(recurrence.m_afData[1][0]);
		return parameters;
	}
	

	@Override
	public double predictEventProbability(SpruceBudwormOutbreakOccurrencePlot plotSample, Object tree, Object... parms) {
		Matrix recurrence = getParametersForThisRealization(plotSample);
		double betaParm = recurrence.m_afData[1][0];
		double lambdaParm = 1d / recurrence.m_afData[0][0];
//		double tmp = GammaFunction.gamma(1 + 1d/betaParm);
//		double estimatedPopVariance = 1d / (lambdaParm * lambdaParm) * (GammaFunction.gamma(1 + 2d/betaParm) - tmp * tmp);
		Integer currentDateYr = (Integer) REpiceaBinaryEventPredictor.findFirstParameterOfThisClass(Integer.class, parms);
		if (currentDateYr == null) {
			throw new InvalidParameterException("The parms argument must provide at least an instance of integer which represents the current date (yrs)");
		}
		DisturbanceOccurrences occurrences = (DisturbanceOccurrences) REpiceaBinaryEventPredictor.findFirstParameterOfThisClass(DisturbanceOccurrences.class, parms);
		Integer timeSinceLastOutbreak = getTimeSinceLastOutbreak(plotSample, currentDateYr, occurrences);
		if (timeSinceLastOutbreak == null) {		// here we have to calculate the marginal probability
			double marginalProb = 0d;
			int max = 90;
			double truncationFactor = 1d / getSurvivorFunctionResult(plotSample.getTimeSinceFirstKnownDateYrs(DisturbanceType.SpruceBudwormOutbreak, currentDateYr) + .5, lambdaParm, betaParm);
			for (int time = plotSample.getTimeSinceFirstKnownDateYrs(DisturbanceType.SpruceBudwormOutbreak, currentDateYr) + 1; time <= max; time++) {	// marginalized over all the possible values 
				double marginalProbability = getSurvivorFunctionResult(time - .5, lambdaParm, betaParm) -  getSurvivorFunctionResult(time +.5, lambdaParm, betaParm);
				marginalProb += getConditionalAnnualProbabilityofOccurrence(time, lambdaParm, betaParm) * marginalProbability * truncationFactor;
			}
			return marginalProb;
		}
		double prob = getConditionalAnnualProbabilityofOccurrence(timeSinceLastOutbreak, lambdaParm, betaParm);
		return prob;
	}

	
//	private double calculateBeta(double recurrence, double minRange, double maxRange) {
//		double objective = variancePop / (recurrence * recurrence); 
//		double value = minRange;
//		double step = (maxRange - minRange) * .1;
//		double criteria = 1000000;
//		double bestCriteria = criteria;
//		while (criteria > 1E-4) {
//			criteria = Math.abs(getResult(value) - objective);
//	//		System.out.println("Value = " + value + "; Criteria = " + criteria);
//			if (criteria < bestCriteria) {
//				bestCriteria = criteria;
//			} else {
//				break;
//			}
//			value += step;
//		}
//		if (criteria < 1E-4) {
//			value -= step;
////			System.out.println("Calculated beta = " + value);
//			return value;
//		} else {
//	 		return calculateBeta(recurrence, value - 2 * step, value);
//		}
//	}

//	private double getResult(double value) {
//		double tmp = GammaFunction.gamma(1d + 1d / value);
//		double result = GammaFunction.gamma(1d + 2d / value) / (tmp * tmp) - 1; 
//		return result;
//	}
	

//	private double calculateLambda(double betaParm, double recurrence) {
//		double lambda = 1d / recurrence * GammaFunction.gamma(1 + 1d / betaParm);
//		return lambda;
//	}


	private double getConditionalAnnualProbabilityofOccurrence(int timeSinceLastOutbreak, double lambda, double beta) {
		double s0 = getSurvivorFunctionResult(timeSinceLastOutbreak - .5, lambda, beta);
		double s1 = getSurvivorFunctionResult(timeSinceLastOutbreak + .5, lambda, beta);
		double probability = 1 - s1/s0;
		return probability;
	}
	
	
	protected double getSurvivorFunctionResult(double time, double lambda, double beta) {
		if (time < 0) {
			time = 0;
		}
		double r = Math.exp(-Math.pow(lambda * time, beta));
		return r;
	}
	
	
	@Override
	public Object predictEvent(SpruceBudwormOutbreakOccurrencePlot plotSample, Object tree, Object... parms) {
		double eventProbability = predictEventProbability(plotSample, tree, parms);
		if (eventProbability < 0 || eventProbability > 1) {
			return null;
		} else if (isResidualVariabilityEnabled) {
			int currentDateYr = (Integer) REpiceaBinaryEventPredictor.findFirstParameterOfThisClass(Integer.class, parms);
			DisturbanceOccurrences occurrences = (DisturbanceOccurrences) REpiceaBinaryEventPredictor.findFirstParameterOfThisClass(DisturbanceOccurrences.class, parms);
			Integer timeSinceLastOutbreak = getTimeSinceLastOutbreak(plotSample, currentDateYr, occurrences);
			boolean occurred;
			if (timeSinceLastOutbreak == null) {
				int timeSinceFirstKnownDate = plotSample.getTimeSinceFirstKnownDateYrs(DisturbanceType.SpruceBudwormOutbreak, currentDateYr);
				occurred = getResidualError(recorderMapForUnknownLastOccurrence,
						plotSample.getMonteCarloRealizationId(), 
						currentDateYr, 
						timeSinceFirstKnownDate, 
						eventProbability);
			} else {
				occurred = getResidualError(recorderMap,		// default recorder map member in the super class
						plotSample.getMonteCarloRealizationId(), 
						currentDateYr, 
						timeSinceLastOutbreak, 
						eventProbability);
			}
//			if (occurred && occurrences != null) {
//				occurrences.add(currentDateYr);
//			}
			return occurred;
		} else {
			return eventProbability;
		}
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
			double residualError = random.nextDouble();
			innerInnerMap.put(parameter, residualError < probability);
		}
		return innerInnerMap.get(parameter);
	}

	
	private Integer getTimeSinceLastOutbreak(SpruceBudwormOutbreakOccurrencePlot plotSample, int currentDateYr, DisturbanceOccurrences occurrences) {
		if (occurrences != null) {
			int latestOccurrence = occurrences.getLastOccurrenceDateYrToDate(currentDateYr);
			
			Integer timeSinceLastOutbreak = plotSample.getTimeSinceLastDisturbanceYrs(DisturbanceType.SpruceBudwormOutbreak, currentDateYr);
			
			if (latestOccurrence == -1 && timeSinceLastOutbreak == null) {
				return null;
			} else if (latestOccurrence == - 1) {
				return timeSinceLastOutbreak;
			} else if (timeSinceLastOutbreak == null) {
				return currentDateYr - latestOccurrence;
			} else {
				return Math.min(currentDateYr - latestOccurrence, timeSinceLastOutbreak);
			}
		} else {
			return plotSample.getTimeSinceLastDisturbanceYrs(DisturbanceType.SpruceBudwormOutbreak, currentDateYr);
		}
	}
	


}
