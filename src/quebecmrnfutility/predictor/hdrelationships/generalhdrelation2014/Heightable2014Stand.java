/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2015 Gouvernement du Quebec 
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
package quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014;

import java.util.Collection;

import repicea.simulation.covariateproviders.plotlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.plotlevel.EcologicalRegionProvider;
import repicea.simulation.covariateproviders.plotlevel.EcologicalTypeProvider;
import repicea.simulation.covariateproviders.plotlevel.ElevationMProvider;
import repicea.simulation.covariateproviders.plotlevel.InterventionResultProvider;
import repicea.simulation.covariateproviders.plotlevel.MeanAnnualPrecipitationMmProvider;
import repicea.simulation.covariateproviders.plotlevel.MeanAnnualTemperatureCProvider;
import repicea.simulation.covariateproviders.plotlevel.SpruceBudwormDefoliatedProvider;
import repicea.simulation.hdrelationships.HDRelationshipStand;


public interface Heightable2014Stand extends HDRelationshipStand,
										BasalAreaM2HaProvider,
										MeanAnnualTemperatureCProvider, 
										EcologicalRegionProvider,
										EcologicalTypeProvider,										
										InterventionResultProvider,
										ElevationMProvider,
										SpruceBudwormDefoliatedProvider,
										MeanAnnualPrecipitationMmProvider {
	
	/**
	 * This method returns the mean quadratic diameter for the stand. 
	 * @return the mean quadratic diameter in cm
	 */
	public double getMeanQuadraticDiameterCm();
	
	/**
	 * This method returns a collection of trees that belong to the stand. Those trees do not have to implement
	 * the HeightableTree interface. The GeneralHeightPredictor already includes a method to filter the trees.
	 * @return a Collection instance
	 */
	@SuppressWarnings("rawtypes")
	public Collection getTrees();
}
