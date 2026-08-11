/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge-Epicea
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
package quebecmrnfutility.predictor.thinners.formerofficialharvestmodule;

import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;

/**
 * Trees that can be harvested by the general harvester thinner.
 * @author M. Fortin - May 2010
 */
@Deprecated
public interface FormerOfficialHarvestableTree extends  DbhCmProvider {
	
	@Deprecated
	public enum FormerOfficialHarvestableSpecies {
		BOJ,
		BOP,
		EPX,
		ERR,
		ERS,
		F_0,
		F_1,
		HEG,
		PEU,
		PIN,
		RES,
		SAB,
		THO;

		private Matrix dummy;
		
		private FormerOfficialHarvestableSpecies() {
			dummy = new Matrix(1,13);
			dummy.setValueAt(0, ordinal(), 1d);
		}
		
		public Matrix getDummy() {return this.dummy;}
	}
	
	
	/**
	 * This method returns ln(dbh + 1) with the dbh in cm.
	 * @return a double
	 */
	public default double getLnDbhCmPlus1() {
		return Math.log(getDbhCm() + 1);
	}

	/**
	 * This method returns the square of dbh. 
	 * @return the square of dbh in cm2 (double)
	 */
	public default double getSquaredDbhCm() {
		double dbhCm = getDbhCm();
		return dbhCm * dbhCm;
	}

	public FormerOfficialHarvestableSpecies getFormerOfficialHarvestableTreeSpecies();
}
