/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package quebecmrnfutility.predictor.volumemodels.merchantablevolume;

import java.security.InvalidParameterException;

/**
 * A class to facilitate the use of the deterministic version of the model in R.
 * @author Mathieu Fortin - August 2021
 */
public class VolumableTreeImpl implements VolumableTree {

	private final double dbhCm;
	private final double heightM;
	private final VolSpecies species;
	
	/**
	 * Constructor.
	 * @param speciesName a three-slot species name (e.g. "SAB" or "EPB")
	 * @param dbhCm diameter at breast height (cm)
	 * @param heightM tree height (m)
	 */
	public VolumableTreeImpl(String speciesName, double dbhCm, double heightM) {
		species = VolSpecies.findEligibleSpecies(speciesName);
		if (species == null) {
			throw new InvalidParameterException("Species " + speciesName + " is not recognized!");
		}
		this.dbhCm = dbhCm;
		this.heightM = heightM;
	}
	
	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getSquaredDbhCm() {return getDbhCm() * getDbhCm();}

	@Override
	public double getHeightM() {return heightM;}

	@Override
	public VolSpecies getVolumableTreeSpecies() {return species;}

}
