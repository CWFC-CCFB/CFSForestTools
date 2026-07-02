/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2013 Mathieu Fortin for Rouge-Epicea
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
package allometry.volume.honer1983;

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;
import repicea.simulation.covariateproviders.treelevel.REpiceaSpeciesProvider;

/**
 * This interface ensures the tree instance is compatible with Honer et al.'s (1983) 
 * total volume model.
 * @author Mathieu Fortin - March 2013, July 2026
 */
public interface Honer1983VolumableTree extends DbhCmProvider, 
												HeightMProvider,
												REpiceaSpeciesProvider {


	/**
	 * This method returns the square of dbh. 
	 * @return the square of dbh in cm2 (double)
	 */
	public default double getSquaredDbhCm() {
		double dbhCm = getDbhCm();
		return dbhCm * dbhCm;
	}

}
