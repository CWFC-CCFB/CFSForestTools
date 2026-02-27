/*
 * This file is part of the CFSForestools library.
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
package quebecmrnfutility.treelogger.petrotreelogger;

import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradeTree;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.treelogger.LoggableTree;

/**
 * This interface applies on the Tree object and is required 
 * to use the PetroLogger class
 * @author Mathieu Fortin - October 2009
 */
public interface PetroLoggableTree extends LoggableTree, PetroGradeTree {

	@Override
	public default SpeciesLocale getSpeciesLocale() {
		return SpeciesLocale.Quebec;
	}
	
 }
