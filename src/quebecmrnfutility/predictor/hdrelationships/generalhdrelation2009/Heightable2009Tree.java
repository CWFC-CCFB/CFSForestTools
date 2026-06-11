/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge Epicea
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
package quebecmrnfutility.predictor.hdrelationships.generalhdrelation2009;

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmPlus1Provider;
import repicea.simulation.covariateproviders.treelevel.REpiceaSpeciesProvider;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredLnDbhCmPlus1Provider;
import repicea.simulation.hdrelationships.HDRelationshipTree;
import repicea.simulation.species.REpiceaSpecies;

/**
 * The HeightableTree interface ensures the compatibility with the HD relationship.
 * @author Mathieu Fortin - November 2012
 */
public interface Heightable2009Tree extends HDRelationshipTree, 
										DbhCmProvider,
										REpiceaSpeciesProvider,
										LnDbhCmPlus1Provider,
										SquaredLnDbhCmPlus1Provider {

	@Deprecated
	public enum Hd2009Species implements SpeciesTypeProvider, REpiceaSpecies {
		BOJ(Species.Betula_alleghaniensis),
		BOP(Species.Betula_papyrifera),
		CHR(Species.Quercus_rubra),
		EPB(Species.Picea_glauca),
		EPN(Species.Picea_mariana),
		EPR(Species.Picea_rubens),
		ERR(Species.Acer_rubrum),
		ERS(Species.Acer_saccharum),
		FRN(Species.Fraxinus_nigra),
		HEG(Species.Fagus_grandifolia),
		MEL(Species.Larix_laricina),
		OSV(Species.Ostrya_virginiana),
		PEG(Species.Populus_grandidentata),
		PET(Species.Populus_tremuloides),
		PIB(Species.Pinus_strobus),
		PIG(Species.Pinus_banksiana),
		PRU(Species.Tsuga_canadensis),
		SAB(Species.Abies_balsamea),
		THO(Species.Thuja_occidentalis),
		TIL(Species.Tilia_americana);

//		private static Set<String> eligibleSpeciesNames;
		
		final Species species;
		
		Hd2009Species(Species s) {
			this.species = s;
		}
		
		@Override
		public SpeciesType getSpeciesType() {return species.getSpeciesType();}
		
		
//		public static Hd2009Species findEligibleSpecies(String speciesName) {
//			if (eligibleSpeciesNames == null) {
//				eligibleSpeciesNames = new HashSet<String>();
//				for (Hd2009Species species : Hd2009Species.values()) {
//					eligibleSpeciesNames.add(species.name());
//				}
//			}
//			if (speciesName == null) {
//				return null;
//			} else {
//				String formattedSpeciesName = speciesName.trim().toUpperCase();
//				if (eligibleSpeciesNames.contains(formattedSpeciesName)) {
//					return Hd2009Species.valueOf(formattedSpeciesName);
//				} else {
//					return null;
//				}
//			}
//		}

		@Override
		public double getBarkProportionOfWoodVolume(SpeciesLocale arg0) {return species.getBarkProportionOfWoodVolume(arg0);}

		@Override
		public double getBasicWoodDensity(SpeciesLocale arg0) {return species.getBasicWoodDensity(arg0);}

		@Override
		public String getLatinName() {return species.getLatinName();}

		@Override
		public void setText(String arg0, String arg1) {}
		
	}	
	
	
//	/**
//	 * This method ensures the species compatibility with the hd relationship.
//	 * @return a REpiceaSpecies enum instance
//	 */
//	public REpiceaSpecies getHeightableTreeSpecies();
	
	
	/**
	 * This method returns the social status index calculated as the 
	 * squared difference between tree dbh and mean quadratic diameter.
	 * @return a double
	 */
	public double getSocialStatusIndex();
	
	
}
