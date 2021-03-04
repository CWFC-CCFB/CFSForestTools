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
package quebecmrnfutility.predictor.hdrelationships.generalhdrelation2009;

import java.util.HashSet;
import java.util.Set;

import quebecmrnfutility.predictor.QuebecGeneralSettings;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmPlus1Provider;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredLnDbhCmPlus1Provider;
import repicea.simulation.hdrelationships.HDRelationshipTree;

/**
 * The HeightableTree interface ensures the compatibility with the HD relationship.
 * @author Mathieu Fortin - November 2012
 */
public interface Heightable2009Tree extends HDRelationshipTree, 
										DbhCmProvider,
										LnDbhCmPlus1Provider,
										SquaredLnDbhCmPlus1Provider {

	public enum Hd2009Species implements SpeciesTypeProvider {
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
		TIL;

		private static Set<String> eligibleSpeciesNames;

		
		private Matrix dummy;
		private SpeciesType speciesType;
		
		Hd2009Species() {
			dummy = new Matrix(1,20);
			dummy.setValueAt(0, ordinal(), 1d);
			if (QuebecGeneralSettings.CONIFEROUS_SPECIES.contains(this.name().toUpperCase().trim())) {
				speciesType = SpeciesType.ConiferousSpecies;
			} else {
				speciesType = SpeciesType.BroadleavedSpecies;
			}
		}
		
		@Override
		public SpeciesType getSpeciesType() {return this.speciesType;}
		public Matrix getDummy() {return this.dummy;}
		
		
		public static Hd2009Species findEligibleSpecies(String speciesName) {
			if (eligibleSpeciesNames == null) {
				eligibleSpeciesNames = new HashSet<String>();
				for (Hd2009Species species : Hd2009Species.values()) {
					eligibleSpeciesNames.add(species.name());
				}
			}
			if (speciesName == null) {
				return null;
			} else {
				String formattedSpeciesName = speciesName.trim().toUpperCase();
				if (eligibleSpeciesNames.contains(formattedSpeciesName)) {
					return Hd2009Species.valueOf(formattedSpeciesName);
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
	public Hd2009Species getHeightableTreeSpecies();
	
	
	/**
	 * This method returns the social status index calculated as the 
	 * squared difference between tree dbh and mean quadratic diameter.
	 * @return a double
	 */
	public double getSocialStatusIndex();
	
	
}
