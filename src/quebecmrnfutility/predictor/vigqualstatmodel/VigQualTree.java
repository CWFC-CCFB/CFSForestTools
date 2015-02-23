/*
 * English version follows
 * 
 * Ce fichier fait partie de la bibliothèque mrnf-foresttools.
 * Il est protégé par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du ministère des Ressources naturelles et de la Faune du Gouvernement du 
 * Québec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Québec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Ministère des Ressources naturelles et de la Faune du Québec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Québec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Québec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Ministère des Ressources naturelles et de la Faune du Québec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.predictor.vigqualstatmodel;

import repicea.simulation.MonteCarloSimulationCompliantObject;

/**
 * The VigQualTree interface ensures the compatibility of an instance with 
 * the VigQualPredictor class.
 * @author Mathieu Fortin - July 2012
 */
public interface VigQualTree extends MonteCarloSimulationCompliantObject {
	
	/**
	 * This enum class lists the species that are handled by this model, namely
	 * yellow birch, sugar maple and American beech.
	 * @author Mathieu Fortin - July 2012
	 */
	public static enum VigQualTreeSpecies {
		/**
		 * Yellow birch code
		 */
		BOJ,
		/**
		 * Sugar maple code
		 */
		ERS,
		/**
		 * American beech code
		 */
		HEG
	}
	

	/**
	 * This method returns the species of the tree.
	 * @return a VigQualTreeSpecies enum variable
	 */
	public VigQualTreeSpecies getVigQualTreeSpecies();

}
