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

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.plotlevel.StemDensityHaProvider;

/**
 * The VigQualStand interface ensures the compatibility of an instance with 
 * the VigQualPredictor class.
 * @author Mathieu Fortin - July 2012
 */

public interface VigQualStand extends MonteCarloSimulationCompliantObject,
										BasalAreaM2HaProvider, 
										StemDensityHaProvider {

	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

	/**
	 * This method returns the ecological type code (4 characters) as defined by the Quebec Ministry of 
	 * Natural Resources and Wildlife
	 * @return a String
	 */
	public String getEcologicalType();

	
}
