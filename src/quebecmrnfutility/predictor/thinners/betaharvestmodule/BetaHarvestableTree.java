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
package quebecmrnfutility.predictor.thinners.betaharvestmodule;

import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmPlus1Provider;

/**
 * Trees that can be harvested by the general harvester thinner.
 * @author Mathieu Fortin - May 2010
 */
@Deprecated
public interface BetaHarvestableTree extends DbhCmProvider,
											LnDbhCmPlus1Provider {
	
	@Deprecated
	public enum BetaHarvestableSpecies {
		BOJ,
		BOP,
		EPX,
		ERR,
		ERS,
		F_0,
		HEG,
		PEU,
		PIN,
		RES,
		SAB,
		THO;
		
		private Matrix dummy;
		
		BetaHarvestableSpecies() {
			dummy = new Matrix(1,12);
			dummy.setValueAt(0, ordinal(), 1d);
		}
		
		public Matrix getDummy() {return this.dummy;}
	}


	@Deprecated
	public BetaHarvestableSpecies getBetaHarvestableTreeSpecies();
}
