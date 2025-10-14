/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2020-2023 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package canforservutility.occupancyindex;

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.plotlevel.DateYrProvider;
import repicea.simulation.covariateproviders.plotlevel.GeographicalCoordinatesProvider;

/**
 * This interface ensures the plot can provide the basic information for the calculation
 * of the occupancy index in the IrisOccupancyIndexCalculator class. <p>
 * 
 * This interface is internal. It is extended in the IrisCompatiblePlot interface.
 * @author Mathieu Fortin - June 2023
 */
public interface OccupancyIndexCalculablePlot extends MonteCarloSimulationCompliantObject,
									GeographicalCoordinatesProvider,
									DateYrProvider,
									AreaHaProvider {

	/**
	 * Provide the basal area (m2/ha) for this species or species group. 
	 * 
	 * @param species an enum standing for the species
	 * @return a double
	 */
	public double getBasalAreaM2HaForThisSpecies(Enum<?> species);

	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

}
