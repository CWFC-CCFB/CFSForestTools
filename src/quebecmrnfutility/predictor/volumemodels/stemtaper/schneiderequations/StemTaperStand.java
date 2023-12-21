/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec
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
package quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations;

import quebecmrnfutility.simulation.covariateproviders.plotlevel.QcDrainageClassProvider;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.plotlevel.EcologicalRegionProvider;
import repicea.simulation.covariateproviders.plotlevel.EcologicalTypeProvider;
import repicea.simulation.covariateproviders.plotlevel.ElevationMProvider;
import repicea.simulation.covariateproviders.plotlevel.StemDensityHaProvider;

public interface StemTaperStand extends MonteCarloSimulationCompliantObject,
										BasalAreaM2HaProvider,
										StemDensityHaProvider,
										EcologicalRegionProvider,
										EcologicalTypeProvider,
										QcDrainageClassProvider,
										ElevationMProvider {
	
	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

}
