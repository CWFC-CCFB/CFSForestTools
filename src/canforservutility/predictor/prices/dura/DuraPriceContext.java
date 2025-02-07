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

import repicea.simulation.MonteCarloSimulationCompliantObject;

public interface DuraPriceContext extends MonteCarloSimulationCompliantObject {


	/**
	 * Provide the exchange rate ratio.<p>
	 * This is the ratio of Canadian currency to American currency the quarter before date.
	 * @return a double
	 */
	public double getEchangeRateRatioCANToUSA_lag1();

	/**
	 * Provide the exchange rate ratio.<p>
	 * This the ratio of Canadian currency to American currency four quarters before date.
	 * @return a double
	 */
	public double getExchangeRateRatioCANToUSA_lag4();

	/**
	 * Provide the number of housing starts.<p> 
	 * This is calculated as the total units of new privately-owned housing units started in the United States. 
	 * @return the number of housing starts (1000s)
	 */
	public double getHousingStartNumber_ThousandUnits();

	/**
	 * Provide the personal saving rate.<p>
	 * This is the saving rate as a percentage of disposable personal income four quarters before date. 
	 * @return a percentage (which goes from 0 to 100).
	 */
	public double getPersonalSavingRate_lag4();

	
	
	/**
	 * Provide the effective interest rate defined by the US Federal Reserve.<p>
	 * Rate set by the American Federal Open Market Committee to regulate the borrowing system between banks a quarter before date.
	 * 
	 * @return the rate (which goes from 0 to 100)
	 */
	public double getFederalFundsRate_lag1();

	
	/**
	 * Provide the effective interest rate defined by the US Federal Reserve.<p>
	 * Rate set by the American Federal Open Market Committee to regulate the borrowing system between banks three quarter before date.
	 * 
	 * @return the rate (which goes from 0 to 100)
	 */
	public double getFederalFundsRate_lag3();

	
	/**
	 * Provide the cost related to weather and climate disasters.<p>
	 * This value is compiled annually, but it is assumed that the effects
	 * span over quarters 3 and 4 of the current year and quarters 1 and 2 of the
	 * following year. The value is reported in billion-dollars.
	 * @return a double
	 */
	public double getClimateCost_BillionDollars();

	/**
	 * Inform whether the quarter is during a Covid pandemic.
	 * @return a boolean
	 */
	public boolean isCovidPeriod();
	
	
	
	
}
