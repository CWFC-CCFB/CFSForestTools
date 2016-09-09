/*
 * English version follows
 * 
 * Ce fichier fait partie de la biblioth�que mrnf-foresttools.
 * Il est prot�g� par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du minist�re des Ressources naturelles et de la Faune du Gouvernement du 
 * Qu�bec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Qu�bec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.predictor.generalhdrelation2014;

import java.util.Collection;

import repicea.simulation.covariateproviders.standlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.standlevel.EcologicalRegionProvider;
import repicea.simulation.covariateproviders.standlevel.EcologicalTypeProvider;
import repicea.simulation.covariateproviders.standlevel.ElevationMProvider;
import repicea.simulation.covariateproviders.standlevel.InterventionResultProvider;
import repicea.simulation.covariateproviders.standlevel.MeanAnnualPrecipitationMmProvider;
import repicea.simulation.covariateproviders.standlevel.MeanAnnualTemperatureCProvider;
import repicea.simulation.covariateproviders.standlevel.SpruceBudwormDefoliatedProvider;
import repicea.simulation.hdrelationships.HDRelationshipStand;


public interface Heightable2014Stand extends HDRelationshipStand,
										BasalAreaM2HaProvider,
										MeanAnnualTemperatureCProvider, 
										EcologicalRegionProvider,
										EcologicalTypeProvider,										
										InterventionResultProvider,
										ElevationMProvider,
										SpruceBudwormDefoliatedProvider,
//										StemDensityHaProvider,
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
