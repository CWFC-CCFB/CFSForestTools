/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2009-2017 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.thinners.melothinner;

import quebecmrnfutility.simulation.covariateproviders.plotlevel.QcForestRegionProvider;
import quebecmrnfutility.simulation.covariateproviders.plotlevel.QcSlopeClassProvider;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.plotlevel.CruiseLineProvider;
import repicea.simulation.covariateproviders.plotlevel.EcologicalTypeProvider;
import repicea.simulation.covariateproviders.plotlevel.LandOwnershipProvider;
import repicea.simulation.covariateproviders.plotlevel.LandUseProvider;
import repicea.simulation.covariateproviders.plotlevel.StemDensityHaProvider;

/**
 * The MeloThinnerPlot interface ensures that the plot instance is compatible 
 * with the MeloThinnerPredictor model. <br>
 * <br>
 * A plot can be harvested only if its land use is set to WoodProduction.
 * @see LandUseProvider 
 * @author Mathieu Fortin - May 2017
 */
public interface MeloThinnerPlot extends MonteCarloSimulationCompliantObject, 
											BasalAreaM2HaProvider, 
											StemDensityHaProvider,
											QcSlopeClassProvider,
											EcologicalTypeProvider,
											QcForestRegionProvider,
											CruiseLineProvider,
											LandUseProvider,
											LandOwnershipProvider {

	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}
	
}
