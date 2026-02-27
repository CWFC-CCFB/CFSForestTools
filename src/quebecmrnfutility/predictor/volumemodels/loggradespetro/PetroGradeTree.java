/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.volumemodels.loggradespetro;

import java.util.HashSet;
import java.util.Set;

import quebecmrnfutility.simulation.covariateproviders.treelevel.QcHarvestPriorityProvider;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcTreeQualityProvider;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcVigorClassProvider;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.BarkProportionProvider;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredDbhCmProvider;
import repicea.simulation.species.REpiceaSpecies;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;

/**
 * This interface makes sure the tree instance is compatible with the PetroGradePredictor class.
 * @author Mathieu Fortin - Sept 2016
 */
public interface PetroGradeTree extends DbhCmProvider,
										SquaredDbhCmProvider,
										QcTreeQualityProvider,
										QcHarvestPriorityProvider,
										QcVigorClassProvider,
										MonteCarloSimulationCompliantObject {

	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	public enum PetroGradeSpecies implements BarkProportionProvider {
		BOJ(REpiceaSpecies.Species.Betula_alleghaniensis),
		ERS(REpiceaSpecies.Species.Acer_saccharum);

		private static Set<String> eligibleSpeciesNames;

		private Matrix dummy;
		
		private final REpiceaSpecies.Species species;
		
		PetroGradeSpecies(REpiceaSpecies.Species species) {
			this.species = species;
			dummy = new Matrix(1,2);
			dummy.setValueAt(0, ordinal(), 1d);
		}

		public Matrix getDummy() {return this.dummy;}


		public static boolean isEligibleSpecies(String speciesName) {
			if (eligibleSpeciesNames == null) {
				eligibleSpeciesNames = new HashSet<String>();
				for (PetroGradeSpecies species : PetroGradeSpecies.values()) {
					eligibleSpeciesNames.add(species.name());
				}
			}
			if (eligibleSpeciesNames.contains(speciesName.trim().toUpperCase())) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public double getBarkProportionOfWoodVolume(SpeciesLocale locale) {
			return species.getBarkProportionOfWoodVolume(locale);
		}

	}
	
	public static enum PetroGradeType {
		PETRO_F1("F1"),
		PETRO_F2("F2"), 
		PETRO_F3("F3"),
		PETRO_F4("F4"),
		PETRO_P("P");

		String name;
		Matrix dummy;

		PetroGradeType(String name) {
			this.name = name;
			this.dummy = new Matrix(1,5);
			dummy.setValueAt(0, ordinal(), 1d);
		}	

		public String getName() {return this.name;}
		public Matrix getDummy() {return this.dummy;}
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
	 * This method ensures the species compatibility with the PetroGradePredictor instance.
	 * @return a PetroGradeSpecies enum variable
	 */
	public PetroGradeSpecies getPetroGradeSpecies();
}
