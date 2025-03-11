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

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.plotlevel.LandUseProvider;
import repicea.simulation.covariateproviders.plotlevel.PotentialVegetationProvider;
import repicea.simulation.covariateproviders.plotlevel.StemDensityHaProvider;
import repicea.simulation.thinners.REpiceaThinningOccurrenceProvider;


public interface OfficialHarvestableStand extends MonteCarloSimulationCompliantObject, 
												StemDensityHaProvider, 
												BasalAreaM2HaProvider,
												PotentialVegetationProvider,
												LandUseProvider {
	
	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

	/**
	 * Provide the information on the last treatment occurrence.
	 * @return an REpiceaThinningOccurrenceProvider instance
	 */
	public REpiceaThinningOccurrenceProvider getThinningOccurrence();
}
