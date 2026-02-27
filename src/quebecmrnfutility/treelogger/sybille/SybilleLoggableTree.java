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
package quebecmrnfutility.treelogger.sybille;

import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperTree;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.treelogger.LoggableTree;

/**
 * This interface makes sure the instance is compatible with Sybille tree logger.
 * @author Mathieu Fortin - March 2012
 */
public interface SybilleLoggableTree extends LoggableTree, StemTaperTree {

	@Override
	public default SpeciesLocale getSpeciesLocale() {return SpeciesLocale.Quebec;}

}
