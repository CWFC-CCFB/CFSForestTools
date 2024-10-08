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

class Heightable2009TreeImpl implements Heightable2009Tree {

	final double dbhCm;
	final Heightable2009Stand stand;
	final int subjectID;
	double heightM;
	Hd2009Species speciesCode;
	
	Heightable2009TreeImpl(Heightable2009StandImpl stand,
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
	public double getLnDbhCmPlus1() {return Math.log(getDbhCm() + 1);}

	@Override
	public double getSquaredLnDbhCmPlus1() {
		double lnDbhCmPlus1 = this.getLnDbhCmPlus1();
		return lnDbhCmPlus1 * lnDbhCmPlus1;
	}


	@Override
	public int getErrorTermIndex() {
		return 0;
	}

	@Override
	public Hd2009Species getHeightableTreeSpecies() {
		return speciesCode;
	}

	@Override
	public double getSocialStatusIndex() {
		return getDbhCm() - ((Heightable2009StandImpl) stand).getMeanQuadraticDiameterCm();
	}


	@Override
	public Enum<?> getHDRelationshipTreeErrorGroup() {
		return getHeightableTreeSpecies().getSpeciesType();
	}
}
