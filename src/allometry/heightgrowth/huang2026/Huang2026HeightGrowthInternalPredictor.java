/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2026 His Majesty the King in right of Canada
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
package allometry.heightgrowth.huang2026;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.climate.REpiceaClimateVariableInformation.Resolution;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.stats.StatisticalUtility;

/**
 * A internal class to handle the different implementations of the stand dominant height models.
 * @author Mathieu Fortin - June 2026
 */
@SuppressWarnings("serial")
final class Huang2026HeightGrowthInternalPredictor extends REpiceaPredictor {

	
	private final Species sp;
	private final double sigma;
	private final Huang2026HeightGrowthPredictor caller;
	
	protected Huang2026HeightGrowthInternalPredictor(boolean isParametersVariabilityEnabled, 
			boolean isResidualVariabilityEnabled,
			Huang2026HeightGrowthPredictor caller,
			Species sp,
			Matrix beta,
			SymmetricMatrix omega,
			double resSigma2) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);  // no random effect
		this.caller = caller;
		this.sp = sp;
		setParameterEstimates(new ModelParameterEstimates(beta, omega));
		this.sigma = Math.sqrt(resSigma2);
	}

	
	double predictHeightM(Huang2026HeightGrowthPlot plot) {
		Matrix beta = getParametersForThisRealization(plot);
		double hag, k;
		double MWMT20 = plot.getMeanJulyTemperatureCelsius(caller, Resolution.IntervalAveragedStarting20YrsBeforeFinalMeasurement);
		double MWMT_n = plot.getMeanJulyTemperatureCelsius(caller, Resolution.Normals30Year);
		double MAP20 = plot.getTotalAnnualPrecipitationMm(caller, Resolution.IntervalAveragedStarting20YrsBeforeFinalMeasurement);
		double MAP_n = plot.getTotalAnnualPrecipitationMm(caller, Resolution.Normals30Year);
		double hi = plot.getSiteIndexM();
		double age = plot.getStandAgeYr();
		double b0,b1,b2,b3;
		double c1,c2;
		switch (sp) {
		case Picea_glauca:
			b0 = beta.getValueAt(0, 0);
			b1 = beta.getValueAt(1, 0);
			b2 = beta.getValueAt(2, 0);
			b3 = beta.getValueAt(3, 0);
			c1 = beta.getValueAt(4, 0);
			c2 = beta.getValueAt(5, 0);
			k = Math.pow(MAP20/MAP_n, c1) * Math.pow(MWMT20/MWMT_n, c2);
			double expCubeSW = Math.pow(Math.sqrt(Math.log(hi)), 3);
			hag   = k * hi * (1 + Math.exp(b0 + b1 * (Math.log(50 + b3)) + b2 * expCubeSW))/
							 (1 + Math.exp(b0 + b1* (Math.log(age + b3)) + b2 * expCubeSW));
			break;
		case Populus_tremuloides:
			b0 = beta.getValueAt(0, 0);
			b1 = beta.getValueAt(1, 0);
			b2 = beta.getValueAt(2, 0);
			c1 = beta.getValueAt(3, 0);
			c2 = beta.getValueAt(4, 0);
			k = Math.pow(MAP20/MAP_n, c1) * Math.pow(MWMT20/MWMT_n, c2);
			double b0HIPowB1 = b0 * Math.pow(hi, b1);
			hag = k * b0HIPowB1 / (1 + Math.exp(Math.log(b0HIPowB1 / hi - 1) + b2 * Math.log((age + 1)/(51)))); 
			break;
		default:
			throw new UnsupportedOperationException("The species " + sp.getLatinName() + " is not supported yet!");
		}
		return isResidualVariabilityEnabled ?
				hag + StatisticalUtility.getRandom().nextGaussian() * sigma :
					hag;
	}
	
	/*
	 * Useless
	 */
	@Override
	protected void init() {}
	
}
