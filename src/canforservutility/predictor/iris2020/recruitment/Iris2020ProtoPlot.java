/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2020-2022 Her Majesty the Queen in right of Canada
 * author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package canforservutility.predictor.iris2020.recruitment;

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatibleTree.Iris2020Species;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.plotlevel.DateYrProvider;
import repicea.simulation.covariateproviders.plotlevel.GeographicalCoordinatesProvider;
import repicea.simulation.covariateproviders.plotlevel.PlotWeightProvider;

interface Iris2020ProtoPlot extends MonteCarloSimulationCompliantObject,
									GeographicalCoordinatesProvider,
									DateYrProvider,
									PlotWeightProvider,
									AreaHaProvider {

	/**
	 * Return the basal area (m2/ha) for this species or species group. 
	 * 
	 * @param species a Iris2020Species enum
	 * @return a double
	 */
	public double getBasalAreaM2HaForThisSpecies(Iris2020Species species);

	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

}
