/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge-Epicea
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
package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel.TreatmentType;

public class OfficialHarvestableTreeImpl implements OfficialHarvestableTree {

	private OfficialHarvestableSpecies species;
	private double dbh;
	private double predictedProbabilityFromFile;
	
	protected OfficialHarvestableTreeImpl(String species, double dbh, double predictedProbabilityFromFile) {
		this(OfficialHarvestableSpecies.valueOf(species.toUpperCase().trim()), dbh, predictedProbabilityFromFile);
//		this.species = OfficialHarvestableSpecies.valueOf(species.toUpperCase().trim());
//		this.dbh = dbh;
//		this.predictedProbabilityFromFile = predictedProbabilityFromFile;
	}
	
	protected OfficialHarvestableTreeImpl(OfficialHarvestableSpecies species, double dbh, double predictedProbabilityFromFile) {
		this.species = species;
		this.dbh = dbh;
		this.predictedProbabilityFromFile = predictedProbabilityFromFile;
	}

	protected double getPredictedProbabilityFromFile() {return predictedProbabilityFromFile;}




	@Override
	public double getSquaredDbhCm() {
		return dbh * dbh;
	}




	@Override
	public double getDbhCm() {
		return dbh;
	}

	@Override
	public OfficialHarvestableSpecies getOfficialHarvestableTreeSpecies(TreatmentType treatment) {
		return species;
	}

}
