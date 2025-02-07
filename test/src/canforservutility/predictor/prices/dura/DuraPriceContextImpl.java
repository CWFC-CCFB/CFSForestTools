/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin - Canadian Forest Service
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package canforservutility.predictor.prices.dura;

import repicea.simulation.HierarchicalLevel;

class DuraPriceContextImpl implements DuraPriceContext {

	
	final double EXCAUSLag4;
	final double CLIMCOSTLAG;
	final double FEDFUNDSLag1;
	final double PSAVERTLag4;
	final double FEDFUNDSLag3;
	final boolean isCovidPeriod;
	final double HOUST;
	final double EXCAUSLag1;
	final double pred;
	
	DuraPriceContextImpl(double EXCAUSLag4,
			double CLIMCOSTLAG,
			double FEDFUNDSLag1,
			double PSAVERTLag4,
			double FEDFUNDSLag3,
			boolean isCovidPeriod,
			double HOUST,
			double EXCAUSLag1,
			double pred) {
		this.EXCAUSLag4 = EXCAUSLag4;
		this.CLIMCOSTLAG = CLIMCOSTLAG;
		this.FEDFUNDSLag1 = FEDFUNDSLag1;
		this.PSAVERTLag4 = PSAVERTLag4;
		this.FEDFUNDSLag3 = FEDFUNDSLag3;
		this.isCovidPeriod = isCovidPeriod;
		this.HOUST = HOUST;
		this.EXCAUSLag1 = EXCAUSLag1;
		this.pred = pred;
	}
	
	
	@Override
	public double getEXCAUSLag4() {return EXCAUSLag4;}

	@Override
	public double getCLIMCOSTLAG() {return CLIMCOSTLAG;}

	@Override
	public double getFEDFUNDSLag1() {return FEDFUNDSLag1;}

	@Override
	public double getPSAVERTLag4() {return PSAVERTLag4;}

	@Override
	public double getFEDFUNDSLag3() {return FEDFUNDSLag3;}

	@Override
	public boolean isCovidPeriod() {return isCovidPeriod;}

	@Override
	public double getHOUST() {return HOUST;}

	@Override
	public double getEXCAUSLag1() {return EXCAUSLag1;}

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

}
