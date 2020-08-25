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
package quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.BarkProportionProvider;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredDbhCmProvider;
import repicea.simulation.species.REpiceaSpecies;
import repicea.simulation.stemtaper.AbstractStemTaperPredictor.BasicStemTaperTree;
import repicea.simulation.stemtaper.StemTaperCrossSection;

/**
 * This interface ensures that the object is compatible with the StemTaperEquation class.
 * @author Mathieu Fortin - July 2011
 */
public interface StemTaperTree extends BasicStemTaperTree,
										MonteCarloSimulationCompliantObject,
										DbhCmProvider,
										SquaredDbhCmProvider,
										HeightMProvider {
	
	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	public enum StemTaperTreeSpecies implements BarkProportionProvider {
		/**
		 * Betula papyrifera
		 */
		BOP(REpiceaSpecies.Species.Betula_spp), 
		/**
		 * Picea glauca
		 */
		EPB(REpiceaSpecies.Species.Picea_abies), 
		/**
		 * Picea mariana
		 */
		EPN(REpiceaSpecies.Species.Picea_abies), 
		/**
		 * Picea rubens
		 */
		EPR(REpiceaSpecies.Species.Picea_abies), 
		/**
		 * Pupolus grandidentata
		 */
		PEG(REpiceaSpecies.Species.Populus_spp),
		/**
		 * Pinus strobus
		 */
		PIB(REpiceaSpecies.Species.Pinus_strobus),
		/**
		 * Pinus banksiana
		 */
		PIG(REpiceaSpecies.Species.Pinus_sylvestris),
		/**
		 * Populus tremuloides
		 */
		PET(REpiceaSpecies.Species.Populus_spp), 
		/**
		 * Abies balsamea
		 */
		SAB(REpiceaSpecies.Species.Abies_spp), 
		/**
		 * Thuja occidentalis
		 */
		THO(REpiceaSpecies.Species.Thuja_plicata);
		
		private static Set<String> eligibleSpeciesNames;
	
		private final REpiceaSpecies.Species species;
		
		StemTaperTreeSpecies(REpiceaSpecies.Species species) {
			this.species = species;
		}
		
		/**
		 * This method returns the StemTaperTreeSpecies Enum associated with the string. 
		 * @param speciesName the species name
		 * @return the appropriate enum or null if the string does not match any species
		 */
		public static StemTaperTreeSpecies findEligibleSpecies(String speciesName) {
			if (eligibleSpeciesNames == null) {
				eligibleSpeciesNames = new HashSet<String>();
				for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
					eligibleSpeciesNames.add(species.name());
				}
			}
			if (speciesName == null) {
				return null;
			} else {
				String formattedSpeciesName = speciesName.trim().toUpperCase();
				if (eligibleSpeciesNames.contains(formattedSpeciesName)) {
					return StemTaperTreeSpecies.valueOf(formattedSpeciesName);
				} else {
					return null;
				}
			}
		}

		@Override
		public double getBarkProportionOfWoodVolume() {
			return species.getBarkProportionOfWoodVolume();
		}

	}
	
	
	
	/**
	 * This method returns the stand in which the tree grows.
	 * @return a StemTaperStand instance
	 */
	public StemTaperStand getStand();
	
	
	/**
	 * This method returns the height sections of this tree if they were measured.
	 * @return a Vector of StemTaperCrossSection instances
	 */
	public List<StemTaperCrossSection> getCrossSections();
	

	/**
	 * This method returns a StemTaperTreeSpecies enum variable. This variable sets the 
	 * version of the stem taper to be used. 
	 * @return a StemTaperTreeSpecies enum instance
	 */
	public StemTaperTreeSpecies getStemTaperTreeSpecies();
	
//	public void setMonteCarloRealizationId(int id);
}
