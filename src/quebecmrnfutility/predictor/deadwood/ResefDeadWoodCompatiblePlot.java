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

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.AreaHaProvider;

/**
 * An interface that ensures the compatibility with the ResefDeadWoodPredictor class.
 * @author Mathieu Fortin - July 2025
 */
public interface ResefDeadWoodCompatiblePlot extends AreaHaProvider, MonteCarloSimulationCompliantObject {
	
	public static enum ResefForestType {
		SugarMapleDominatedStand,
		SpruceDominatedStand,
		FirDominatedStand
	}

	/**
	 * Provide a forest type acceptable for the ResefDeadWoodPredictor class.
	 * @return a ResefForestType enum
	 */
	public ResefForestType getResefForestType();

}
