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

import canforservutility.occupancyindex.OccupancyIndexCalculablePlot;
import canforservutility.simulation.RecruitmentPlotWithOccupancy;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.climate.REpiceaClimateVariableInformation.Resolution;
import repicea.simulation.covariateproviders.plotlevel.BasalAreaBySpeciesTypeM2HaProvider;
import repicea.simulation.covariateproviders.plotlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.plotlevel.GrowthStepLengthYrProvider;
import repicea.simulation.covariateproviders.plotlevel.InterventionPlannedProvider;
import repicea.simulation.covariateproviders.plotlevel.InterventionResultProvider;
import repicea.simulation.covariateproviders.plotlevel.StemDensityHaProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.AnnualFrostFreeDaysProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.AnnualGrowingDegreeDaysCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.HighestAnnualTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.LowestAnnualTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanAnnualTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanMaximumJulyTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanMinimumJanuaryTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanTemperatureFromJuneToAugustCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.TotalAnnualPrecipitationMmProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.TotalPrecipitationFromJuneToAugustMmProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.TotalPrecipitationFromMarchToMayMmProvider;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;

public interface Trillium2026RecruitmentPlot extends MonteCarloSimulationCompliantObject,
											GrowthStepLengthYrProvider,
											TotalAnnualPrecipitationMmProvider,
											TotalPrecipitationFromMarchToMayMmProvider,
											TotalPrecipitationFromJuneToAugustMmProvider,
											MeanMinimumJanuaryTemperatureCelsiusProvider,
											MeanAnnualTemperatureCelsiusProvider,
											MeanMaximumJulyTemperatureCelsiusProvider,
											MeanTemperatureFromJuneToAugustCelsiusProvider,
											LowestAnnualTemperatureCelsiusProvider,
											HighestAnnualTemperatureCelsiusProvider,
											AnnualFrostFreeDaysProvider,
											AnnualGrowingDegreeDaysCelsiusProvider,
											BasalAreaBySpeciesTypeM2HaProvider,
											BasalAreaM2HaProvider,
											StemDensityHaProvider,
											OccupancyIndexCalculablePlot,
//											SlopeInclinationPercentProvider,
											InterventionResultProvider,
											InterventionPlannedProvider,
											RecruitmentPlotWithOccupancy {

	static final Resolution ClimateVariableResolution = Resolution.IntervalAveragedStarting20YrsBeforeFinalMeasurement;

	@Override
	public default double getBasalAreaM2Ha() {
		return getBasalAreaM2HaForThisSpeciesType(SpeciesType.BroadleavedSpecies) + 
				getBasalAreaM2HaForThisSpeciesType(SpeciesType.ConiferousSpecies);
	};

	
	
}
