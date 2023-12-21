/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * Author: Jean-Francois Lavoie
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

import repicea.simulation.HierarchicalLevel;

public class Lambert2005TreeImpl implements Lambert2005Tree {

	Lambert2005Species species;
	double dbhcm;
	double hm;	
	
	Lambert2005TreeImpl(Lambert2005Species _species, double _dbhcm, double _hm) {
		species = _species;
		dbhcm = _dbhcm;
		hm = _hm;		
	}
	
	@Override
	public double getDbhCm() {
		// TODO Auto-generated method stub
		return dbhcm;
	}

	@Override
	public double getHeightM() {
		// TODO Auto-generated method stub
		return hm;
	}

	@Override
	public String getSubjectId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMonteCarloRealizationId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Lambert2005Species getLambert2005Species() {
		// TODO Auto-generated method stub
		return species;
	}
}
