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
package quebecmrnfutility.predictor.deadwood;

import java.util.HashMap;
import java.util.Map;

import quebecmrnfutility.predictor.deadwood.ResefDeadWoodCompatiblePlot.ResefForestType;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;

/**
 * A predictor for dead wood.<p>
 * The predictor is based on a sample of the RESEF network.
 * @author Mathieu Fortin - July 2025
 */
@SuppressWarnings("serial")
public class ResefDeadWoodPredictor extends REpiceaPredictor {

	static class ResefDeadWoodInternalPredictor extends REpiceaPredictor {

		final ResefForestType forestType;
		
		protected ResefDeadWoodInternalPredictor(ResefForestType forestType, boolean isParametersVariabilityEnabled) {
			super(isParametersVariabilityEnabled, false, false);
			this.forestType = forestType;
		}

		/*
		 * For extended visibility
		 */
		@Override
		protected void setParameterEstimates(ModelParameterEstimates modelParms) {
			super.setParameterEstimates(modelParms);
		}
		
		@Override
		protected void init() {
			super.setParameterEstimates(getParameterEstimates());
		}
		
		double predictDeadWoodBiomassMgHa(ResefDeadWoodCompatiblePlot plot) {
			Matrix beta = isParametersVariabilityEnabled ? 
					getParametersForThisRealization(plot) :
					getParameterEstimates().getMean(); 
			double pred = beta.getValueAt(0, 0);
			return pred < 0 ? 0d : pred;
		}
	}
	
	private Map<ResefForestType, ResefDeadWoodInternalPredictor> internalPredictors;
	
	/**
	 * Constructor.
	 * @param isParameterVariabilityEnabled true to enable the variability in the mean
	 */
	public ResefDeadWoodPredictor(boolean isParameterVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, false);
		init();
	}

	@Override
	protected void init() {
		internalPredictors = new HashMap<ResefForestType, ResefDeadWoodInternalPredictor>();
		setInternalPredictor(ResefForestType.SugarMapleDominatedStand, 25.30773903, 7.750338908);
		setInternalPredictor(ResefForestType.SpruceDominatedStand, 11.95025186, 2.380472303);
		setInternalPredictor(ResefForestType.FirDominatedStand, 33.0540757, 12.00838853);
	}
	
	private void setInternalPredictor(ResefForestType forestType, double mu, double stdErr) {
		Matrix mean = new Matrix(1,1);
		SymmetricMatrix variance = new SymmetricMatrix(1);
		mean.setValueAt(0, 0, mu);
		variance.setValueAt(0, 0, stdErr * stdErr);
		ModelParameterEstimates parmEst = new ModelParameterEstimates(mean, variance);
		ResefDeadWoodInternalPredictor internalPredictor = new ResefDeadWoodInternalPredictor(forestType, isParametersVariabilityEnabled);
		internalPredictor.setParameterEstimates(parmEst);
		internalPredictors.put(forestType, internalPredictor);
	}

	/**
	 * Provide the biomass of dead wood.
	 * @param plot a ResefDeadWoodCompatiblePlot instance
	 * @param perHectare a boolean
	 * @return a double
	 */
	public double predictDeadWoodBiomassMg(ResefDeadWoodCompatiblePlot plot, boolean perHectare) {
		double biomassHa = internalPredictors.get(plot.getResefForestType()).predictDeadWoodBiomassMgHa(plot);
		return perHectare ? 
				biomassHa : 
					biomassHa * plot.getAreaHa();
	}
	
}
