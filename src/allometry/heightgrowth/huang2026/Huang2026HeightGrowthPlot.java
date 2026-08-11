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

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanJulyTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.TotalAnnualPrecipitationMmProvider;
import repicea.simulation.species.REpiceaSpecies.Species;

interface Huang2026HeightGrowthPlot extends MonteCarloSimulationCompliantObject,
													TotalAnnualPrecipitationMmProvider, 
													MeanJulyTemperatureCelsiusProvider {

	/**
	 * Provide the height at a reference age of 50 years.
	 * @return stand height (m)
	 */
	public double getSiteIndexM();
	
	/**
	 * Porvide the stand age.
	 * @return a double
	 */
	public double getStandAgeYr();
	
	
	public Species getDominantSpecies();
	
	@Override
	public default HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}
	
}
