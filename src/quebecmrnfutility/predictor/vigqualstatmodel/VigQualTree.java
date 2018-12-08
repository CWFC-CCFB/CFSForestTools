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
package quebecmrnfutility.predictor.vigqualstatmodel;

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;

/**
 * The VigQualTree interface ensures the compatibility of an instance with 
 * the VigQualPredictor class.
 * @author Mathieu Fortin - July 2012
 */
public interface VigQualTree extends MonteCarloSimulationCompliantObject {
	
	
	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

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
