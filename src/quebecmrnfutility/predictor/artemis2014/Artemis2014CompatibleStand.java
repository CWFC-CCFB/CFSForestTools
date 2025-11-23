/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in Right of Canada
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
package quebecmrnfutility.predictor.artemis2014;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.plotlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.plotlevel.DateYrProvider;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider;
import repicea.simulation.covariateproviders.plotlevel.EcologicalRegionProvider;
import repicea.simulation.covariateproviders.plotlevel.ElevationMProvider;
import repicea.simulation.covariateproviders.plotlevel.GeographicalCoordinatesProvider;
import repicea.simulation.covariateproviders.plotlevel.GrowthStepLengthYrProvider;
import repicea.simulation.covariateproviders.plotlevel.InterventionPlannedProvider;
import repicea.simulation.covariateproviders.plotlevel.InterventionResultProvider;
import repicea.simulation.covariateproviders.plotlevel.MeanQuadraticDiameterCmProvider;
import repicea.simulation.covariateproviders.plotlevel.PotentialVegetationProvider;
import repicea.simulation.covariateproviders.plotlevel.StemDensityHaProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.MeanAnnualTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.climate.TotalAnnualPrecipitationMmProvider;

/**
 * Ensure the plot instance is compatible with Artemis 2014's modules.<p>
 * @author Mathieu Fortin - November 2025
 */
public interface Artemis2014CompatibleStand extends PotentialVegetationProvider, 
													InterventionResultProvider, 
													BasalAreaM2HaProvider,
													MeanQuadraticDiameterCmProvider,
													DateYrProvider,
													TotalAnnualPrecipitationMmProvider,
													MeanAnnualTemperatureCelsiusProvider,
													ElevationMProvider,
													GeographicalCoordinatesProvider,
													StemDensityHaProvider,
													AreaHaProvider,
													MonteCarloSimulationCompliantObject,
													GrowthStepLengthYrProvider,
													InterventionPlannedProvider,
													DrainageGroupProvider,
													EcologicalRegionProvider {
	
	
	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}
	
	/**
	 * This method returns true if the plot is going to be defoliated by spruce budworm.
	 * @return a boolean
	 */
	public boolean isGoingToBeDefoliated();
	
	/**
	 * This method returns true if the stand has just been read from a file and is not the result of an
	 * evolution.
	 * @return a boolean
	 */
	public boolean isInitialStand();
	
	/**
	 * This method returns the number of stems in the plot (NOT PER HECTARE!) for each species group
	 * @return a Matrix
	 */
	public Matrix getNumberOfStemsBySpeciesGroup();
	
	/**
	 * This method returns the basal area in the plot (NOT PER HECTARE!) for each species group
	 * @return a Matrix
	 */
	public Matrix getBasalAreaBySpeciesGroup();
	
	/**
	 * Return true if this stand (plot) has been harvested in the previous step.<p>
	 * That is if the stand in the previous step is an intervention result.
	 * @return a boolean
	 */
	public boolean wasHarvestedInPreviousStep();

	/**
	 * Return true if this stand (plot) has been harvested two steps ago.<p>
	 * That is if the stand in the step before last is an intervention result.
	 * @return a boolean
	 */
	public boolean wasHarvestedTwoStepsAgo();

	public boolean wasSpruceBudwormDefoliatedInPreviousStep();

	public boolean wasSpruceBudwormDefoliatedTwoStepsAgo();

	
	/**
	 * Provide the seasonal precipitation (mm)
	 * @return a double
	 */
	public double getSeasonalPrecipitationMm();
	
	public double getVaporPressureDeficit();
	
	public double getUtilPrecipitation();
}
