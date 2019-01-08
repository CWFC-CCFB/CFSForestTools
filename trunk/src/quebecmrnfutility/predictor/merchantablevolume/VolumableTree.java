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
package quebecmrnfutility.predictor.merchantablevolume;

import java.util.HashSet;
import java.util.Set;

import quebecmrnfutility.predictor.QuebecGeneralSettings;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredDbhCmProvider;


/**
 * This interface ensures that the Tree-derived class provides 
 * the getters for the general volume equation in Fortin et al. (2007)
 * @author Mathieu Fortin - Octobre 2009 
 */
public interface VolumableTree extends DbhCmProvider,
										SquaredDbhCmProvider,
										HeightMProvider {
	
	
	public enum VolSpecies implements SpeciesTypeProvider {
		BOG,
		BOJ,
		BOP,
		CET,
		CHR,
		EPB,
		EPN,
		EPR,
		ERR,
		ERS,
		FRA,
		FRN,
		HEG,
		MEL,
		ORA,
		OSV,
		PEB,
		PEG,
		PET,
		PIB,
		PIG,
		PIR,
		PRU,
		SAB,
		THO,
		TIL;
		
		private static Set<String> eligibleSpeciesNames;
		
		private Matrix dummy;
		private SpeciesType speciesType;
		
		
		VolSpecies() {
			dummy = new Matrix(1,26);
			dummy.m_afData[0][this.ordinal()] = 1d;
			if (QuebecGeneralSettings.CONIFEROUS_SPECIES.contains(this.name().toUpperCase().trim())) {
				speciesType = SpeciesType.ConiferousSpecies;
			} else {
				speciesType = SpeciesType.BroadleavedSpecies;
			}
		}
		
		@Override
		public SpeciesType getSpeciesType() {return this.speciesType;}
		public Matrix getDummy() {return this.dummy;}
		
		public static VolSpecies findEligibleSpecies(String speciesName) {
			if (eligibleSpeciesNames == null) {
				eligibleSpeciesNames = new HashSet<String>();
				for (VolSpecies species : VolSpecies.values()) {
					eligibleSpeciesNames.add(species.name());
				}
			}
			if (speciesName == null) {
				return null;
			} else {
				String formattedSpeciesName = speciesName.trim().toUpperCase();
				if (eligibleSpeciesNames.contains(formattedSpeciesName)) {
					return VolSpecies.valueOf(formattedSpeciesName);
				} else {
					return null;
				}
			}
		}

		
	}
	
	
	/**
	 * This method ensures the species compatibility with the volume model.
	 * @return a VolSpecies enum instance
	 */
	public VolSpecies getVolumableTreeSpecies();
	
}
