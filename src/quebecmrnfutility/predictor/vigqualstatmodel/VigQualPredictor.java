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
package quebecmrnfutility.predictor.vigqualstatmodel;

import repicea.simulation.REpiceaPredictor;

/**
 * The VigQualPredictor class implements a bivariate probit regression whose margins
 * respectively predicts tree vigor (MSCR) and tree quality (ABCD). The model has no plot random
 * effects. Consequently the random effect variability is disabled.
 * @author Mathieu Fortin - July 2012
 */
public class VigQualPredictor extends REpiceaPredictor {

	private static final long serialVersionUID = 20120705L;

	/**
	 * General constructor.
	 * @param isVariabilityEnabled true to enable the parameter variability or false to run in deterministic mode
	 */
	public VigQualPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);		// no random effect
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

	
}
