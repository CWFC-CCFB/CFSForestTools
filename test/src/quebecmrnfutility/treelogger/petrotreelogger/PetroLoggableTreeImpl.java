/*
 * This file is part of the CFSForestools library.
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
package quebecmrnfutility.treelogger.petrotreelogger;

public class PetroLoggableTreeImpl implements PetroLoggableTree {

	final PetroGradeSpecies species;
	final double dbhCm;
	QcVigorClass vigorClass;
	QcHarvestPriority mscrPriority;
	QcTreeQuality abcdQuality;
	
	
	private PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm, QcVigorClass vigorClass, QcHarvestPriority mscrPriority, QcTreeQuality abcdQuality) {
		this.species = species;
		this.dbhCm = dbhCm;
		this.vigorClass = vigorClass;
		this.abcdQuality = abcdQuality;
		this.mscrPriority = mscrPriority;
	}

	
	PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm) {
		this(species, dbhCm, null, null, null);
	}

	PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm, QcVigorClass vigorClass) {
		this(species, dbhCm, vigorClass, null, null);
	}

	PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm, QcHarvestPriority mscrPriority) {
		this(species, dbhCm, null, mscrPriority, null);
	}

	PetroLoggableTreeImpl(PetroGradeSpecies species, double dbhCm, QcTreeQuality abcdQuality) {
		this(species, dbhCm, null, null, abcdQuality);
	}
	
	
	@Override
	public double getCommercialVolumeM3() {
		return 1d;
	}

	@Override
	public String getSpeciesName() {
		return getPetroGradeSpecies().toString();
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
		return "";
	}

	@Override
	public int getMonteCarloRealizationId() {
		return 0;
	}

	
	@Override
	public double getBarkProportionOfWoodVolume() {
		return getPetroGradeSpecies().getBarkProportionOfWoodVolume();
	}
	
	@Override
	public boolean isCommercialVolumeOverbark() {
		return false;
	}

}
