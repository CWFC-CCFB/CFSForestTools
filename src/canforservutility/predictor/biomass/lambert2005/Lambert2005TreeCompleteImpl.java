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

import repicea.simulation.covariateproviders.treelevel.HeightMProvider;

/**
 * An implementation of the Lambert2005Tree interface for the complete model.
 * @author Mathieu Fortin - February 2026
 */
public class Lambert2005TreeCompleteImpl extends Lambert2005TreeReducedImpl implements HeightMProvider {

	final double heightM;	
	
	/**
	 * Constructor.
	 * @param species a Lambert2005Species enum
	 * @param dbhCm tree diameter at breast height (cm)
	 * @param heightM tree height (m)
	 */
	public Lambert2005TreeCompleteImpl(Lambert2005Species species, double dbhCm, double heightM) {
		super(species, dbhCm);
		if (heightM <= 0d) {
			throw new InvalidParameterException("The tree height must be positive!");
		}
		this.heightM = heightM;		
	}
	

	@Override
	public double getHeightM() {return heightM;}

}



