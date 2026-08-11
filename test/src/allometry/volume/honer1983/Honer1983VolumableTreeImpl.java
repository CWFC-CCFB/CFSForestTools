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

import repicea.simulation.species.REpiceaSpecies.Species;

public class Honer1983VolumableTreeImpl implements Honer1983VolumableTree {//, VolumableTree {

	private final Species species;
	private final double dbh;
	private final double height;
	
	protected Honer1983VolumableTreeImpl(Species species, double dbh, double height) {
		this.species = species;
		this.dbh = dbh;
		this.height = height;
	}
	
	@Override
	public double getHeightM() {return height;}

	@Override
	public double getDbhCm() {return dbh;}

	@Override
	public Species getREpiceaSpecies() {
		return species;
	}


}
