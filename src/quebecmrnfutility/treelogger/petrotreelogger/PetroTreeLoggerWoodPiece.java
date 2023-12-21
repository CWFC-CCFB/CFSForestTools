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

import repicea.simulation.treelogger.WoodPiece;

/**
 * The PetroTreeLoggerWoodPiece class represents a log of particular quality.
 * It is always considered over bark.
 * @author Mathieu Fortin - August 2020
 *
 */
public class PetroTreeLoggerWoodPiece extends WoodPiece {

	private static final long serialVersionUID = 1L;

	protected PetroTreeLoggerWoodPiece(PetroTreeLogCategory logCategory, PetroLoggableTree tree, double woodVolumeM3) {
		super(logCategory, tree, false, woodVolumeM3); // false: underbark
	}


}
