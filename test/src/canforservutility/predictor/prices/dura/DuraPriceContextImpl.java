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

class DuraPriceContextImpl implements DuraPriceContext, CovidPeriodProvider {

	
	final double EXCAUSLag4;
	final double CLIMCOSTLAG;
	final double FEDFUNDSLag1;
	final double PSAVERTLag4;
	final double FEDFUNDSLag3;
	final boolean isCovidPeriod;
	final double HOUST;
	final double EXCAUSLag1;
	final double pred;
	final int quarterId;
	
	DuraPriceContextImpl(int quarterId, double EXCAUSLag4,
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
		this.quarterId = quarterId;
	}
	
	
	@Override
	public double getExchangeRateRatioCANToUSA_lag4() {return EXCAUSLag4;}

	@Override
	public double getClimateCost_BillionDollars() {return CLIMCOSTLAG;}

	@Override
	public double getFederalFundsRate_lag1() {return FEDFUNDSLag1;}

	@Override
	public double getPersonalSavingRate_lag4() {return PSAVERTLag4;}

	@Override
	public double getFederalFundsRate_lag3() {return FEDFUNDSLag3;}

	@Override
	public boolean isCovidPeriod() {return isCovidPeriod;}

	@Override
	public double getHousingStartNumber_ThousandUnits() {return HOUST;}

	@Override
	public double getEchangeRateRatioCANToUSA_lag1() {return EXCAUSLag1;}

	/**
	 * Useless for this class.
	 */
	@Override
	public String getSubjectId() {return null;}

	/**
	 * Useless for this class.
	 */
	@Override
	public HierarchicalLevel getHierarchicalLevel() {return null;}

	@Override
	public int getMonteCarloRealizationId() {
		return 0;
	}

	@Override
	public int getErrorTermIndex() {return quarterId;}

}
