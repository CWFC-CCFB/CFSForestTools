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
package ontariomnrf.predictor.trillium2026.diameterincrement.mixedeffects;

import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.climate.REpiceaClimateVariableInformation.Resolution;
import repicea.simulation.covariateproviders.plotlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.plotlevel.GrowthStepLengthYrProvider;
import repicea.simulation.covariateproviders.plotlevel.InterventionPlannedProvider;
import repicea.simulation.covariateproviders.plotlevel.StemDensityHaProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.AnnualGrowingDegreeDaysCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanAnnualClimateMoistureIndexCmProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanAnnualSoilMoistureIndexPercentProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanMaximumJulyTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanMinimumJanuaryTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanTemperatureFromJuneToAugustCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanVapourPressureDeficitFromJuneToAugustHPaProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.TotalPrecipitationFromJuneToAugustMmProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.TotalPrecipitationFromMarchToMayMmProvider;

/**
 * An interface ensuring that the plot instance is compatible with the
 * Trillium2026DiameterIncrementPredictor.
 */
public interface Trillium2026DiameterIncrementPlot extends MonteCarloSimulationCompliantObject,
											GrowthStepLengthYrProvider,
											MeanMinimumJanuaryTemperatureCelsiusProvider,
											TotalPrecipitationFromMarchToMayMmProvider,
											TotalPrecipitationFromJuneToAugustMmProvider,
											AnnualGrowingDegreeDaysCelsiusProvider,
											MeanTemperatureFromJuneToAugustCelsiusProvider,
											MeanMaximumJulyTemperatureCelsiusProvider,
											MeanVapourPressureDeficitFromJuneToAugustHPaProvider,
											MeanAnnualClimateMoistureIndexCmProvider,
											MeanAnnualSoilMoistureIndexPercentProvider,
											InterventionPlannedProvider,
											BasalAreaM2HaProvider,
											StemDensityHaProvider {

	static final Resolution ClimateVariableResolution = Resolution.IntervalAveraged;
	
}
