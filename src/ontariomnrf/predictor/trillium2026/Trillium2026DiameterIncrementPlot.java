/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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
package ontariomnrf.predictor.trillium2026;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.GrowthStepLengthYrProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.AnnualFrostFreeDaysProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.AnnualGrowingDegreeDaysCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.LowestAnnualTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanAnnualTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanMaximumJulyTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanMinimumJanuaryTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanTemperatureFromJuneToAugustCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.TotalAnnualPrecipitationMmProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.TotalPrecipitationFromJuneToAugustMmProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.TotalPrecipitationFromMarchToMayMmProvider;

public interface Trillium2026DiameterIncrementPlot extends MonteCarloSimulationCompliantObject,
											GrowthStepLengthYrProvider,
											TotalAnnualPrecipitationMmProvider,
											MeanAnnualTemperatureCelsiusProvider,
											MeanMinimumJanuaryTemperatureCelsiusProvider,
											TotalPrecipitationFromMarchToMayMmProvider,
											TotalPrecipitationFromJuneToAugustMmProvider,
											LowestAnnualTemperatureCelsiusProvider,
											AnnualFrostFreeDaysProvider,
											AnnualGrowingDegreeDaysCelsiusProvider,
											MeanTemperatureFromJuneToAugustCelsiusProvider,
											MeanMaximumJulyTemperatureCelsiusProvider {



	/**
	 * Mean temperature anomaly.<p>
	 * That is the difference between the normals and the interval-averaged temperature
	 * @return a double
	 */
	public double getMeanTempAnomalyCelsius(); 
	
	public double getTotalRadiation(); // add unit
	public double getMeanSummerVPD();	// add unit
	public double getSMImean();
	
	/**
	 * Mean maximum temperature anomaly.<p>
	 * That is the difference between the normals and the interval-averaged temperature.
	 * @return a double
	 */
	public double getMaxTempAnomalyCelsius();
	
	public double getMeanSummerVPDDaylight();

	/**
	 * Total precipitation anomaly.<p>
	 * That is the difference between the normals and the interval-averaged temperature.
	 * @return a double
	 */
	public double getTotalPrecipitationAnomalyMm();
	
	public double getCMI();
	public double getHighestTmaxCelsius();


}
