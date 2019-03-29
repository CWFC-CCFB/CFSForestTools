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
import repicea.simulation.covariateproviders.standlevel.NaturalDisturbanceInformationProvider;
import repicea.stats.distributions.utility.GaussianUtility;

/**
 * This class implements a simple occurrence model for spruce budworm outbreaks based on the recurrence as 
 * estimated by Boulanger and Arsenault (2004). According to their paper (p. 1041), the mean recurrence time 
 * was estimated at 39.5 years with a standard deviation of 8.6. This class does not implement stochastic features for the 
 * parameters. However is does for the residual error.
 * @author Mathieu Fortin - March 2019
 * @see <a href=https://doi.org/10.1139/X03-269> Boulanger, Y., and D. Arseneault. 2004. Spruce budworm
 * outbreaks in eastern Quebec over the last 450 years. Canadian Journal of Forest Research 34: 1035-1043 
 * </a>
 */
@SuppressWarnings("serial")
public class SpruceBudwormOutbreakOccurrencePredictor extends REpiceaLogisticPredictor<NaturalDisturbanceInformationProvider, Object> {

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
	public double predictEventProbability(NaturalDisturbanceInformationProvider stand, Object tree, Object... parms) {
		Integer timeSinceLastOutbreak = stand.getTimeSinceLastDisturbanceYrs();
		if (timeSinceLastOutbreak == null) {		// here we have to calculate the marginal probability
			double marginalProb = 0d;
			int max = 79;
			double truncationFactor = 1d / (1 - getCumulativeProbability(stand.getTimeSinceFirstKnownDateYrs()));
			for (int time = stand.getTimeSinceFirstKnownDateYrs() + 1; time <= max; time++) {	// marginalized over all the possible values under the assumption that all the years are equally possible
				marginalProb += getConditionalAnnualProbability(time) * (getCumulativeProbability(time) -  getCumulativeProbability(time - 1)) * truncationFactor;
			}
			return marginalProb;
		}
		
		return getConditionalAnnualProbability(timeSinceLastOutbreak);
	}

	private double getConditionalAnnualProbability(Integer timeSinceLastOutbreak) {
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
