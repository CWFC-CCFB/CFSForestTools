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
