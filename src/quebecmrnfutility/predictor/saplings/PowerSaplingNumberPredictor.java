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

import java.io.IOException;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.stats.StatisticalUtility;
import repicea.util.ObjectUtility;

/**
 * A class that implements Hugues Power's model of sapling density.
 * @author Mathieu Fortin - July 2025 
 */
@SuppressWarnings("serial")
public final class PowerSaplingNumberPredictor extends PowerAbstractSaplingPredictor {

	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to run the predictor in stochastic mode
	 */
	public PowerSaplingNumberPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled); 
		init();
	}

	@Override
	protected void init() {
		try {
			Matrix beta = new Matrix(10,1);
			beta.setValueAt(0, 0, 3.335466866); 	// intercept
			beta.setValueAt(1, 0, -0.416251832); 	// coupe
			beta.setValueAt(2, 0, -0.007690412);	// couvertERS 
			beta.setValueAt(3, 0, 0.551816811); 	// couvertSAB 
			beta.setValueAt(4, 0, -0.037518226);	// st_Marchande 
			beta.setValueAt(5, 0, 0.261917271); 	// coupe_CouvertERS 
			beta.setValueAt(6, 0, -0.455604964);	// coupe_CouvertSAB
			beta.setValueAt(7, 0, 0.016599712);		// coupe_St_Marchande 
			beta.setValueAt(8, 0, -0.022045214);		// couvertERS_st_Marchande 
			beta.setValueAt(9, 0, -0.013107319);	// couvertSAB_st_Marchande 
			oXVector = new Matrix(1, beta.m_iRows);
			
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String vcovFilename = path + "0_VcovNB.csv";
			SymmetricMatrix cov = SymmetricMatrix.convertToSymmetricIfPossible(ParameterLoader.loadMatrixFromFile(vcovFilename));
			setParameterEstimates(new ModelParameterEstimates(beta, cov));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Predict the number of saplings in a 40-m2 plot
	 * @param plot a PowerSaplingBasalAreaAndDensityCompatiblePlot instance
	 * @return the number of saplings 
	 */
	public synchronized double predictSaplingNumber(PowerSaplingBasalAreaAndDensityCompatiblePlot plot) {
		Matrix beta = getParametersForThisRealization(plot);
		setXVector(plot);
		double pred = oXVector.multiply(beta).getValueAt(0, 0);
		double lambda = Math.exp(pred); // this is the mean of the Poisson distribution conditional on the parameter estimates
		if (isResidualVariabilityEnabled) {
			return StatisticalUtility.getRandom().nextPoisson(lambda);
		} else {
			Matrix xVx = oXVector.multiply(getParameterEstimates().getVariance()).multiply(oXVector.transpose());
			pred += xVx.getValueAt(0, 0) * .5; // otherwise it is marginalized over the distribution of the parameter estimates
			return Math.exp(pred);
		}
	}

}
