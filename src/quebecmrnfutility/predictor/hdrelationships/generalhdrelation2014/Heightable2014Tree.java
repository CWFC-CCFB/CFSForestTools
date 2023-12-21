/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2015 Gouvernement du Quebec 
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
package quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014;

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

	public enum Hd2014Species {	
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
		
		Hd2014Species() {
			dummy = new Matrix(1, 27);
			dummy.setValueAt(0, ordinal(), 1d);								
		}

		public Matrix getDummy() {return this.dummy;}
		
		private static Set<String> eligibleSpeciesNames;
		
		/**
		 * This method returns the BetaHdSpecies Enum associated with the string. 
		 * @param speciesName the species name
		 * @return the appropriate enum or null if the string does not match any species
		 */
		public static Hd2014Species findEligibleSpecies(String speciesName) {
			if (eligibleSpeciesNames == null) {
				eligibleSpeciesNames = new HashSet<String>();
				for (Hd2014Species species : Hd2014Species.values()) {
					eligibleSpeciesNames.add(species.name());
				}
			}
			if (speciesName == null) {
				return null;
			} else {
				String formattedSpeciesName = speciesName.trim().toUpperCase();
				if (eligibleSpeciesNames.contains(formattedSpeciesName)) {
					return Hd2014Species.valueOf(formattedSpeciesName);
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
	public Hd2014Species getHeightable2014TreeSpecies();
	
	
	
}
