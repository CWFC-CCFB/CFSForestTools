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
package quebecmrnfutility.treelogger.meristreelogger;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings("serial")
public class MerisWoodPiece extends WoodPiece {

	protected MerisWoodPiece(MerisTreeLogCategory logCategory, LoggableTree tree, double volumeOfThisWoodPieceM3) {
		super(logCategory, tree, logCategory.isBark, volumeOfThisWoodPieceM3);
		setProperty(Property.barkVolume_m3, logCategory.isBark ? volumeOfThisWoodPieceM3 : 0d);
		setProperty(Property.woodVolume_m3, logCategory.isBark ? 0d : volumeOfThisWoodPieceM3);
	}

}
