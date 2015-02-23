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
package quebecmrnfutility.predictor.stemtaper;

import java.util.Vector;

import repicea.simulation.MonteCarloSimulationCompliantObject;

/**
 * This interface ensures that the object is compatible with the StemTaperEquation class.
 * @author Mathieu Fortin - July 2011
 */
@Deprecated
public interface StemTaperTree extends MonteCarloSimulationCompliantObject {
	
	/**
	 * This method returns the stand in which the tree grows.
	 * @return a StemTaperStand instance
	 */
	public StemTaperStand getStand();
	
	/**
	 * This method returns tree dbh (mm).
	 * @return a double
	 */
	public double getDbh();

	/**
	 * This method returns the square of tree dbh (mm2). Required for modules with corrections
	 * for variable transformation such as Artemis module.
	 * @return a double
	 */
	public double getDbh2();

	/**
	 * This method returns tree height (m).
	 * @return a double
	 */
	public double getHeight();
	
	/**
	 * This method returns the height sections of this tree if they were measured.
	 * @return a Vector of StemTaperHeightSection instances
	 */
	public Vector<StemTaperHeightSection> getHeightSections();
	
	/**
	 * This method adds a height section to this tree.
	 * @param heightSection a StemTaperHeightSection object
	 */
	public void addHeightSection(StemTaperHeightSection heightSection);
	
}
