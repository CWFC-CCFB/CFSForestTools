/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge Epicea
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
package quebecmrnfutility.predictor.hdrelationships.generalhdrelation2009;

import repicea.simulation.covariateproviders.plotlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider;
import repicea.simulation.covariateproviders.plotlevel.EcologicalRegionProvider;
import repicea.simulation.covariateproviders.plotlevel.EcologicalTypeProvider;
import repicea.simulation.covariateproviders.plotlevel.InterventionResultProvider;
import repicea.simulation.covariateproviders.plotlevel.MeanAnnualTemperatureCelsiusProvider;
import repicea.simulation.covariateproviders.plotlevel.SpruceBudwormDefoliatedProvider;
import repicea.simulation.covariateproviders.plotlevel.TreeStatusCollectionsProvider;
import repicea.simulation.hdrelationships.HDRelationshipStand;


public interface Heightable2009Stand extends HDRelationshipStand,
										TreeStatusCollectionsProvider,
										BasalAreaM2HaProvider,
										MeanAnnualTemperatureCelsiusProvider,
										EcologicalRegionProvider,
										EcologicalTypeProvider,
										DrainageGroupProvider,
										InterventionResultProvider,
										SpruceBudwormDefoliatedProvider {
	
}
