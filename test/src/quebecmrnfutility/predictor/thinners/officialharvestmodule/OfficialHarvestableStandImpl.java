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

import java.util.ArrayList;
import java.util.Collection;

import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import repicea.simulation.thinners.REpiceaThinningOccurrenceProvider;

public class OfficialHarvestableStandImpl implements OfficialHarvestableStand {

	private double numberOfStemsHa;
	private Collection<OfficialHarvestableTree> trees;
	private TreatmentType treatment;
	
	
	/**
	 * Constructor
	 * @param numberOfStemsHa number of stems per hectare
	 */
	protected OfficialHarvestableStandImpl(double numberOfStemsHa, String treatment) {
		this.numberOfStemsHa = numberOfStemsHa;
		this.treatment = TreatmentType.valueOf(treatment.toUpperCase().trim());
		trees = new ArrayList<OfficialHarvestableTree>();
	}
	
	protected void addTree(OfficialHarvestableTree tree) {
		trees.add(tree);
	}

	protected TreatmentType getTreatment() {return treatment;}
	
	protected Collection<OfficialHarvestableTree> getTrees() {return trees;}
	
	@Override
	public String getSubjectId() {return ((Integer) hashCode()).toString();}

	@Override
	public int getMonteCarloRealizationId() {					// no need
		return 0;
	}

	@Override
	public double getNumberOfStemsHa() {
		return this.numberOfStemsHa;
	}

	@Override
	public double getBasalAreaM2Ha() {		
		return 0;
	}

	/*
	 * Useless for the tests.
	 */
	@Override
	public String getPotentialVegetation() {
		return null;
	}

	@Override
	public LandUse getLandUse() {
		return LandUse.WoodProduction;
	}

	@Override
	public REpiceaThinningOccurrenceProvider getThinningOccurrence() {return null;}
	
}
