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

import repicea.simulation.REpiceaLogisticPredictor;
import repicea.stats.distributions.utility.GaussianUtility;

/**
 * This class implements a simple occurrence model for spruce budworm outbreaks based on the recurrence as 
 * estimated by Belanger and Arsenault (2004). This class does not implement stochastic features for the 
 * parameters. However is does for the residual error.
 * @author Mathieu Fortin - March 2019
 */
@SuppressWarnings("serial")
public class SpruceBudwormOutbreakOccurrencePredictor extends REpiceaLogisticPredictor<SpruceBudwormOutbreakOccurrencePlot, Object> {

	private final double mean = 39.5;
	private final double sd = 8.6;
	private final double truncationFactor;
	
	
	/**
	 * Constructor. 
	 */
	public SpruceBudwormOutbreakOccurrencePredictor() {
		super(false, false, false);
		truncationFactor = 1d / (1 - GaussianUtility.getCumulativeProbability(-mean/sd));
	}
	
	@Override
	public double predictEventProbability(SpruceBudwormOutbreakOccurrencePlot stand, Object tree, Object... parms) {
		Integer timeSinceLastOutbreak = stand.getTimeSinceLastOutbreakYrs();
		if (timeSinceLastOutbreak == null) {		// here we have to calculate the marginal probability
			double marginalProb = 0d;
			int max = 79;
			if (max < 0) {
				return 1d;		// means its been more than 80 years since the last outbreak
			}
			int nbTrials = 0;
			for (int time = stand.getTimeSinceInitialKnownDateYrs() + 1; time <= max; time++) {	// marginalized over all the possible values under the assumption that all the years are equally possible
				marginalProb += getAnnualProbability(time);
				nbTrials++; 
			}
			return marginalProb / nbTrials;
		}
		
		return getAnnualProbability(timeSinceLastOutbreak);
	}

	private double getAnnualProbability(Integer timeSinceLastOutbreak) {
		double cumProbz0 = getCumulativeProbability(timeSinceLastOutbreak - 1);
		double survivalFactor = 1d / (1 - cumProbz0);
		double cumProbz1 = getCumulativeProbability(timeSinceLastOutbreak);
		double probability = cumProbz1 - cumProbz0;
		probability *= survivalFactor;
		return probability;
	}
	
	protected double getCumulativeProbability(int time) {
		double z = (time - mean) / sd;
		return GaussianUtility.getCumulativeProbability(z) * truncationFactor;
	}
	
	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.REpiceaPredictor#init()
	 */
	@Override
	protected void init() {}

}
