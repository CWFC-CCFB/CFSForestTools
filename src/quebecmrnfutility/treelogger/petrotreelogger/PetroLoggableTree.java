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
package quebecmrnfutility.treelogger.petrotreelogger;

import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradeTree;
import repicea.simulation.treelogger.LoggableTree;

/**
 * This interface applies on the Tree object and is required 
 * to use the PetroLogger class
 * @author Mathieu Fortin - October 2009
 */
public interface PetroLoggableTree extends LoggableTree, PetroGradeTree {

	@Override
	public default double getBarkProportionOfWoodVolume() {
		return getPetroGradeSpecies().getBarkProportionOfWoodVolume();
	}
	
	@Override
	public default boolean isCommercialVolumeOverbark() {
		return false;
	}
	
 }
