/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
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
