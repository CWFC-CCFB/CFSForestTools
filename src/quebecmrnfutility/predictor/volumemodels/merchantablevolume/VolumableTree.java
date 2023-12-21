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
package quebecmrnfutility.predictor.volumemodels.merchantablevolume;

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
