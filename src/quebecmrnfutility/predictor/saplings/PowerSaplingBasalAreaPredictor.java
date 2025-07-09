/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package quebecmrnfutility.predictor.saplings;

import quebecmrnfutility.predictor.saplings.PowerSaplingBasalAreaAndDensityCompatiblePlot.CoverType;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.StatisticalUtility;

/**
 * A class that implements Hugues Power's model of sapling basal area.
 * @author Mathieu Fortin - July 2025 
 */
@SuppressWarnings("serial")
public class PowerSaplingBasalAreaPredictor extends REpiceaPredictor {

	final double intercept = 2.027559601;
	final double couvertERS = -0.45055147;
	final double couvertSAB = 0.104705512;
	final double st_Marchande = -0.022874216;
	final double couvertERS_st_Marchande = -0.005334583;
	final double couvertSAB_st_Marchande = -0.000374787;
	final double sigma2_res = 0.5783417;
	final double sigma_res = Math.sqrt(sigma2_res);
	
			
	/**
	 * General constructor
	 * @param isParametersVariabilityEnabled true to enable the variability in the parameter estimates
	 * @param isResidualVariabilityEnabled true to enable the variability in the residual error
	 */
	public PowerSaplingBasalAreaPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled); // no random effect in this model
		init();
	}

	/**
	 * Constructor with single argument.
	 * @param isVariabilityEnabled true to run the predictor in stochastic mode
	 */
	public PowerSaplingBasalAreaPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled); // no random effect in this model
	}

	
	
	@Override
	protected void init() {}
	
	/**
	 * Predict the sapling basal area (m2/ha)
	 * @param plot a PowerSaplingBasalAreaAndDensityCompatiblePlot instance
	 * @return the sapling basal area (m2/ha)
	 */
	public double predictSaplingBasalAreaM2Ha(PowerSaplingBasalAreaAndDensityCompatiblePlot plot) {
		double pred = intercept;
		if (plot.getCoverType() == CoverType.Maple) {
			pred += couvertERS;
			pred += plot.getBasalAreaM2Ha() * couvertERS_st_Marchande;
		} else if (plot.getCoverType() == CoverType.Fir) {
			pred += couvertSAB;
			pred += plot.getBasalAreaM2Ha() * couvertSAB_st_Marchande;
		}
		pred += plot.getBasalAreaM2Ha() * st_Marchande;
		double predBackTransformed;
		if (isResidualVariabilityEnabled) {
			pred += StatisticalUtility.getRandom().nextGaussian() * sigma_res;
			predBackTransformed = Math.exp(pred) - 1;
		} else {
			predBackTransformed = Math.exp(pred + sigma2_res * .5) - 1;
		}
		return predBackTransformed < 0d ? 0d : predBackTransformed;
	}

}
