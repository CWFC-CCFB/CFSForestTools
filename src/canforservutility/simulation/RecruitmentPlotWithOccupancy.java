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
package canforservutility.simulation;

import repicea.simulation.MonteCarloSimulationCompliantObject;

/**
 * A common plot interface for recruitment model using occupancy index.
 * @author Mathieu Fortin - February 2026
 */
public interface RecruitmentPlotWithOccupancy extends MonteCarloSimulationCompliantObject {

	/**
	 * Return the occupancy for a particular species. <p>
	 * @param sp an Enum that stands for the species
	 * @return either a double (if the occupancy is known or simulated) or 
	 * a GaussianEstimate instance (when it has been estimated).
	 */
	public Object getOccupancyForThisSpecies(Enum<?> sp);

	
	
}
