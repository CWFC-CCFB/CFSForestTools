/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge-Epicea
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
package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;

/**
 * This private class contains a specific version of the harvest module. The versions depend on the nature of the treatment.
 * @author Mathieu Fortin - October 2011
 */
class OfficialHarvestSubmodel extends REpiceaPredictor {

	private static final long serialVersionUID = 20100906L;

	
	/**
	 * General constructor that enables the stochastic implementation. The OfficialHarvestModel class contains 
	 * a map of instance of this class. Basically, the OfficialHarvestSubmodel implements a subversion of
	 * the Harvest model. NOTE: there is no random effect variability in this model.
	 * @param isParameterVariabilityEnabled = a boolean that enables the parameter variability
	 * @param isResidualVariabilityEnabled = a boolean that enables the residual variability
	 */
	protected OfficialHarvestSubmodel(boolean isParameterVariabilityEnabled,
			boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled);
	}
	
	/**
	 * Default constructor with all uncertainty sources disabled.
	 */
	protected OfficialHarvestSubmodel() {
		this(false, false);
	}

	protected void setParameterEstimates(Matrix beta, SymmetricMatrix variance) {
		setParameterEstimates(new SASParameterEstimates(beta, variance)); 
	}
	
	protected Matrix getSubParametersForThisStand(MonteCarloSimulationCompliantObject stand) {
		return getParametersForThisRealization(stand);
	}

	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#init()
	 */
	@Override
	protected void init() {}
	
}
