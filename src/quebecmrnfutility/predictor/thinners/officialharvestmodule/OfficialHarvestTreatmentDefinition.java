/*
 * This file is part of the mrnf-foresttools library
 * 
 * Author: Mathieu Fortin - Canadian Forest Service
 * Copyright (C) 2021 Her Majestry the Queen in right of Canada
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import repicea.gui.components.REpiceaMatchComplexObject;
import repicea.simulation.thinners.REpiceaTreatmentDefinition;

/**
 * An implementation of the REpiceaMatchComplexObject interface, which allows to specify a
 * delay between two treatments.
 * @author Mathieu Fortin - February 2021
 */
@SuppressWarnings("serial")
public class OfficialHarvestTreatmentDefinition implements Serializable, 
															REpiceaMatchComplexObject<OfficialHarvestTreatmentDefinition>,
															REpiceaTreatmentDefinition {

	protected final TreatmentType treatmentType;
	private int delayBeforeNextTreatmentYrs;
	
	private OfficialHarvestTreatmentDefinition(TreatmentType treatmentType) {
		this.treatmentType = treatmentType;
	}

	@Override
	public String toString() {
		return treatmentType.toString();
	}
	
	protected static OfficialHarvestTreatmentDefinition[] getDefinitionOfAllAvailableTreatment() {
		List<OfficialHarvestTreatmentDefinition> definitions = new ArrayList<OfficialHarvestTreatmentDefinition>();
		for (TreatmentType t : TreatmentType.values()) {
			definitions.add(new OfficialHarvestTreatmentDefinition(t));
		}
		return definitions.toArray(new OfficialHarvestTreatmentDefinition[]{});
	}
	
	
	protected boolean isTotalHarvest() {
		return treatmentType == TreatmentType.CPRS;
	}

	@Override
	public int getNbAdditionalFields() {
		return 1;
	}

	@Override
	public List<Object> getAdditionalFields() {
		List<Object> outputList = new ArrayList<Object>();
		outputList.add(delayBeforeNextTreatmentYrs);
		return outputList;
	}

	@Override
	public void setValueAt(int indexOfThisAdditionalField, Object value) {
		if (indexOfThisAdditionalField == 0) {
			delayBeforeNextTreatmentYrs = (Integer) value;
		}
	}
	
	@Override
	public int getDelayBeforeReentryYrs() {return delayBeforeNextTreatmentYrs;}

	@Override
	public OfficialHarvestTreatmentDefinition copy() {
		OfficialHarvestTreatmentDefinition copy = new OfficialHarvestTreatmentDefinition(this.treatmentType);
		copy.delayBeforeNextTreatmentYrs = delayBeforeNextTreatmentYrs;
		return copy;
	}

	@Override
	public TreatmentType getTreatmentType() {return treatmentType;}

	
}
