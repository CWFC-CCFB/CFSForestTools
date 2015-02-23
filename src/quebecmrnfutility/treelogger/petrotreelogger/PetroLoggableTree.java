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
package quebecmrnfutility.treelogger.petrotreelogger;

import java.util.HashSet;
import java.util.Set;

import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.ABCDQualityProvider;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.MSCRPriorityProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredDbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.VigorClassProvider;
import repicea.simulation.treelogger.LoggableTree;

/**
 * This interface applies on the Tree object and is required 
 * to use the PetroLogger class
 * @author Mathieu Fortin - October 2009
 */
public interface PetroLoggableTree extends LoggableTree, 
											DbhCmProvider,
											SquaredDbhCmProvider,
											ABCDQualityProvider,
											MSCRPriorityProvider,
											VigorClassProvider {
	
	public enum PetroLoggerSpecies {
		BOJ,
		ERS;
		
		private static Set<String> eligibleSpeciesNames;
		
		private Matrix dummy;
		
		PetroLoggerSpecies() {
			dummy = new Matrix(1,2);
			dummy.m_afData[0][this.ordinal()] = 1d;
		}
		
		public Matrix getDummy() {return this.dummy;}


		public static boolean isEligibleSpecies(String speciesName) {
			if (eligibleSpeciesNames == null) {
				eligibleSpeciesNames = new HashSet<String>();
				for (PetroLoggerSpecies species : PetroLoggerSpecies.values()) {
					eligibleSpeciesNames.add(species.name());
				}
			}
			if (eligibleSpeciesNames.contains(speciesName.trim().toUpperCase())) {
				return true;
			} else {
				return false;
			}
		}
		
	}
	

	/**
	 * This method returns true is the model runs in stochastic mode or false if it
	 * is in deterministic mode.
	 * @return a boolean
	 */
	public boolean isModelStochastic();

	
	/**
	 * This method returns the dbh variance when it is available.
	 * @return a double
	 */
	public double getDbhCmVariance();
	
	/**
	 * This method ensures the species compatibility with the PetroTreeLogger instance.
	 * @return a PetroLoggerSpecies enum variable
	 */
	public PetroLoggerSpecies getPetroLoggableTreeSpecies();
}
