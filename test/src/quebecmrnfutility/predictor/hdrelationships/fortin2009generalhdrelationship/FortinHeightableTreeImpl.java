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
package quebecmrnfutility.predictor.hdrelationships.fortin2009generalhdrelationship;

import repicea.simulation.species.REpiceaSpecies.Species;

@SuppressWarnings("deprecation")
class FortinHeightableTreeImpl implements Fortin2009HeightableTree {

	final double dbhCm;
	final Fortin2009HeightablePlot stand;
	final int subjectID;
	double heightM;
	Hd2009Species speciesCode;
	
	FortinHeightableTreeImpl(Fortin2009HeightableStandImpl stand,
			double dbhCm,
			int subjectID, 
			String species,
			double heightM) {
		this.stand = stand;
		stand.trees.add(this);
		this.dbhCm = dbhCm;
		this.heightM = heightM;
		this.subjectID = subjectID;
		String speciesName = species.toUpperCase().trim();
		try {
			speciesCode = Hd2009Species.valueOf(speciesName);
		} catch (IllegalArgumentException e) {
			speciesCode = Hd2009Species.valueOf("FRN");		// default species for species that were modelled in 2014 but not in 2009
		}
	}
	
	
	
	@Override
	public String getSubjectId() {
		return ((Integer) subjectID).toString();
	}

	@Override
	public int getMonteCarloRealizationId() {
		return stand.getMonteCarloRealizationId();
	}

	@Override
	public double getHeightM() {return heightM;}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public int getErrorTermIndex() {
		return 0;
	}

	@Override
	public Species getREpiceaSpecies() {
		return speciesCode.species;
	}

	@Override
	public double getSocialStatusIndex() {
		return getDbhCm() - ((Fortin2009HeightableStandImpl) stand).getMeanQuadraticDiameterCm();
	}

	@Override
	public Enum<?> getHDRelationshipTreeErrorGroup() {
		return getREpiceaSpecies().getSpeciesType();
	}
}
