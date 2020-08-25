/*
 * This file is part of the repicea-foresttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package quebecmrnfutility.treelogger.wbirchprodvol;

import quebecmrnfutility.predictor.volumemodels.wbirchloggrades.WBirchLogGradesStand;
import quebecmrnfutility.predictor.volumemodels.wbirchloggrades.WBirchLogGradesTree;
import repicea.simulation.species.REpiceaSpecies;
import repicea.simulation.treelogger.LoggableTree;

public interface WBirchProdVolLoggableTree extends WBirchLogGradesTree, LoggableTree {

	/**
	 * This method ensures the tree instance can provide the stand it grows in.
	 * @return a WBirchProdVolStand instance
	 */
	public WBirchLogGradesStand getStand();

	@Override
	public default double getBarkProportionOfWoodVolume() {
		return REpiceaSpecies.Species.Betula_spp.getBarkProportionOfWoodVolume();
	}
	
	@Override
	public default boolean isCommercialVolumeOverbark() {
		return false;
	}

	@Override
	public default WBirchProdVolTreeSpecies getWBirchProdVolTreeSpecies() {
		return WBirchProdVolTreeSpecies.WhiteBirch;
	}

}
