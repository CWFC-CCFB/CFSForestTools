/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2021-2026 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package canforservutility.predictor.biomass.lambert2005;

import java.security.InvalidParameterException;

import repicea.simulation.HierarchicalLevel;

/**
 * An implementation of the Lambert2005Tree interface for the reduced model.
 * @author Mathieu Fortin - February 2026
 */
public class Lambert2005TreeReducedImpl implements Lambert2005Tree {

	final Lambert2005Species species;
	final double dbhCm;
	
	/**
	 * Constructor.
	 * @param species a Lambert2005Species enum
	 * @param dbhCm tree diameter at breast height (cm)
	 */
	public Lambert2005TreeReducedImpl(Lambert2005Species species, double dbhCm) {
		if (species == null) {
			throw new InvalidParameterException("The species argument cannot be null!");
		}
		if (dbhCm <= 0d) {
			throw new InvalidParameterException("The tree diameter must be positive!");
		}
		this.species = species;
		this.dbhCm = dbhCm;
	}
	
	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public String getSubjectId() {return null;}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return null;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public Lambert2005Species getLambert2005Species() {return species;}

}



