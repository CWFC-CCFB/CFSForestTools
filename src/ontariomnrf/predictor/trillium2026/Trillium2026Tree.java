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
package ontariomnrf.predictor.trillium2026;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.BasalAreaLargerThanSubjectM2Provider;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredDbhCmProvider;
import repicea.simulation.species.REpiceaSpecies.Species;

public interface Trillium2026Tree extends MonteCarloSimulationCompliantObject,
											DbhCmProvider,
											LnDbhCmProvider,
											SquaredDbhCmProvider,
											BasalAreaLargerThanSubjectM2Provider 	{
	
	/**
	 * Provide a Species instance that must be
	 * compatible with the model. <p>
	 * 
	 * @return an REpiceaSpecies.Species enum
	 */
	public Species getTrillium2026TreeSpecies();
	
}
