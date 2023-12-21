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

import repicea.math.Matrix;

public class PetroGradeTreeImpl implements PetroGradeTree {

	final PetroGradeSpecies species;
	final double dbhCm;
	QcVigorClass vigorClass;
	QcHarvestPriority mscrPriority;
	QcTreeQuality abcdQuality;
	int realization;
	private Matrix realizedValues;
	
	
	private PetroGradeTreeImpl(PetroGradeSpecies species, double dbhCm, QcVigorClass vigorClass, QcHarvestPriority mscrPriority, QcTreeQuality abcdQuality) {
		this.species = species;
		this.dbhCm = dbhCm;
		this.vigorClass = vigorClass;
		this.abcdQuality = abcdQuality;
		this.mscrPriority = mscrPriority;
		realization = 0;
	}

	void setRealizedValues(Matrix values) {
		this.realizedValues = values;
	}

	Matrix getRealizedValues() {
		return realizedValues;
	}
	
	PetroGradeTreeImpl(PetroGradeSpecies species, double dbhCm) {
		this(species, dbhCm, null, null, null);
	}

	PetroGradeTreeImpl(PetroGradeSpecies species, double dbhCm, QcVigorClass vigorClass) {
		this(species, dbhCm, vigorClass, null, null);
	}

	PetroGradeTreeImpl(PetroGradeSpecies species, double dbhCm, QcHarvestPriority mscrPriority) {
		this(species, dbhCm, null, mscrPriority, null);
	}

	PetroGradeTreeImpl(PetroGradeSpecies species, double dbhCm, QcTreeQuality abcdQuality) {
		this(species, dbhCm, null, null, abcdQuality);
	}
	

	@Override
	public double getDbhCm() {
		return dbhCm;
	}

	@Override
	public double getSquaredDbhCm() {
		return getDbhCm() * getDbhCm();
	}

	@Override
	public QcTreeQuality getTreeQuality() {
		return abcdQuality;
	}

	@Override
	public QcHarvestPriority getHarvestPriority() {
		return mscrPriority;
	}

	@Override
	public QcVigorClass getVigorClass() {
		return vigorClass;
	}

	@Override
	public boolean isModelStochastic() {
		return false;
	}

	@Override
	public double getDbhCmVariance() {
		return 10;
	}

	@Override
	public PetroGradeSpecies getPetroGradeSpecies() {
		return species;
	}


	@Override
	public String getSubjectId() {
		return "test";
	}


	@Override
	public int getMonteCarloRealizationId() {
		return realization;
	}
	
	protected void setRealization(int i) {
		realization = i;
	}

}
