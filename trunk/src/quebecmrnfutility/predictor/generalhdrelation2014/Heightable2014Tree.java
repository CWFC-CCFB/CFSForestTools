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

import java.util.HashSet;
import java.util.Set;

import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmPlus1Provider;
import repicea.simulation.covariateproviders.treelevel.SquaredLnDbhCmPlus1Provider;
import repicea.simulation.hdrelationships.HDRelationshipTree;

/**
 * The HeightableTree interface ensures the compatibility with the HD
 * relationship.
 * 
 * @author Denis Hache - Fev. 2014
 */
public interface Heightable2014Tree extends HDRelationshipTree,
										DbhCmProvider,
										LnDbhCmPlus1Provider,
										SquaredLnDbhCmPlus1Provider
//										BasalAreaLargerThanSubjectM2Provider 
										{

	public enum BetaHdSpecies {	
		BOJ,
		BOP,
		CHR,
		EPB,
		EPN,
		EPR,
		ERR,
		ERS,
		FRN,
		HEG,
		MEL,
		OSV,
		PEG,
		PET,
		PIB,
		PIG,
		PRU,
		SAB,
		THO,
		TIL,
		BOG,
		CET,
		ERA,
		FRA,
		ORA,
		PEB,
		PIR;
		private Matrix	dummy;
		
		BetaHdSpecies() {
			dummy = new Matrix(1, 27);
			dummy.m_afData[0][this.ordinal()] = 1d;								
		}

		public Matrix getDummy() {return this.dummy;}
		
		private static Set<String> eligibleSpeciesNames;
		
		/**
		 * This method returns the BetaHdSpecies Enum associated with the string. 
		 * @param speciesName the species name
		 * @return the appropriate enum or null if the string does not match any species
		 */
		public static BetaHdSpecies findEligibleSpecies(String speciesName) {
			if (eligibleSpeciesNames == null) {
				eligibleSpeciesNames = new HashSet<String>();
				for (BetaHdSpecies species : BetaHdSpecies.values()) {
					eligibleSpeciesNames.add(species.name());
				}
			}
			if (speciesName == null) {
				return null;
			} else {
				String formattedSpeciesName = speciesName.trim().toUpperCase();
				if (eligibleSpeciesNames.contains(formattedSpeciesName)) {
					return BetaHdSpecies.valueOf(formattedSpeciesName);
				} else {
					return null;
				}
			}
		}
	}	
	
	
	/**
	 * This method ensures the species compatibility with the hd relationship.
	 * @return a HdSpecies enum instance
	 */
	public BetaHdSpecies getBetaHeightableTreeSpecies();
	
	
//	/**
//	 * This method returns the social status index calculated as the 
//	 * squared difference between tree dbh and mean quadratic diameter.
//	 * @return a double
//	 */
//	public double getSocialStatusIndex();
	
	
}
