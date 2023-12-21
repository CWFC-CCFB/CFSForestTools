/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.matapedia;

public class MatapediaTreeImpl implements MatapediaTree {

	private MatapediaTreeSpecies species;
	private double dbh;
	private double bal;
	
	public MatapediaTreeImpl(MatapediaTreeSpecies species, double dbh, double bal) {
		this.species = species;
		this.dbh = dbh;
		this.bal = bal;
	}
	
//	@Override
//	public Object getSubjectPlusMonteCarloSpecificId() {
//		return this;
//	}

	@Override
	public String getSubjectId() {
		return ((Integer) hashCode()).toString();
	}

	@Override
	public int getMonteCarloRealizationId() {
		return 0;
	}



	@Override
	public double getDbhCm() {
		return dbh;
	}

	@Override
	public double getSquaredDbhCm() {
		return dbh * dbh;
	}

	@Override
	public MatapediaTreeSpecies getMatapediaTreeSpecies() {
		return species;
	}

	@Override
	public double getBasalAreaLargerThanSubjectM2Ha() {
		return bal;
	}

}
