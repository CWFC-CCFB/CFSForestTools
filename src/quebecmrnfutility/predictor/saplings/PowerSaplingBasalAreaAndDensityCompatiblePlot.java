/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service,
 *         Hugues Power, Direction de la recherche forestiere du Quebec
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

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.BasalAreaM2HaProvider;

/**
 * An interface to ensure the plot instance is compatible with the
 * PowerSaplingBasalAreaPredictor and PowerSaplingDensityPredictor class.<p>
 * 
 * @author Mathieu Fortin and Hugues Power - July 2025
 */
public interface PowerSaplingBasalAreaAndDensityCompatiblePlot extends MonteCarloSimulationCompliantObject,
																		BasalAreaM2HaProvider {

	public static enum CoverType {
		Maple,
		Fir,
		BlackSpruce;
	}
	
	/**
	 * Provide the cover type of the plot.
	 * @return a CoverType enum
	 */
	public CoverType getCoverType();

	
}
