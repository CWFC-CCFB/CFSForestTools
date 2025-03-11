/*
 * This file is part of the CFSForesttools library
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

	/**
	 * A class that provides the information 
	 * on an eventual final treatment to be carried out.
	 * @author Mathieu Fortin - March 2025
	 */
	public static class ScheduledFinalHarvestInfo {
		
		final int[] range;
		final double prob;
		
		ScheduledFinalHarvestInfo(int[] rangeYr, double probOccurrence) {
			this.range = rangeYr;
			this.prob = probOccurrence;
		}
		
		/**
		 * Provide the probability that the final treatment is carried out.
		 * @return a probability 
		 */
		public double getProbability() {return prob;}

		/**
		 * Provide the range of date between which the final treatment could occur. <p>
		 * The lower bound is exclusive while the upper bound is inclusive.
		 * @return an array of two integers
		 */
		public int[] getDateYrRange() {return range;}
	}
	
	protected final TreatmentType treatmentType;
	private int delayBeforeNextTreatmentYrs;
	
	private OfficialHarvestTreatmentDefinition(TreatmentType treatmentType) {
		this.treatmentType = treatmentType;
	}

	OfficialHarvestTreatmentDefinition(TreatmentType treatmentType, int delayBeforeNextTreatmentYrs) {
		this(treatmentType);
		this.delayBeforeNextTreatmentYrs = delayBeforeNextTreatmentYrs;
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
	
	/**
	 * Indicate that a final cut has to be schedule whenever the treatment 
	 * is applied.
	 * @return a boolean
	 */
	public boolean doesFinalCutHaveToBeScheduled() {
		return getTreatmentType().doesFinalCutHaveToBeScheduled();
	}

	
	/**
	 * Provide the probability that the final cut is carried out after 
	 * the last treatment (e.g. shelterwood cutting or commercial thinning). <p>
	 * The method returns null if the treatment is not part of a sequence that
	 * ends with a total harvest.
	 * @param lastHarvestDateYr date of the last harvest
	 * @param intervalBeginDateYr date at the beginning of the interval
	 * @param intervalEndDateYr date at the end of the interval
	 * @return a ScheduledFinalHarvestInfo instance
	 */
	public ScheduledFinalHarvestInfo getProbabilityOfFinalCutBeingCarriedOut(int lastHarvestDateYr, 
			int intervalBeginDateYr, 
			int intervalEndDateYr) {
		int[] finalCutRange = getTreatmentType().finalCutRange;
		if (finalCutRange == null) {
			return null;
		} else {
			double windowDurationYr = finalCutRange[1] - finalCutRange[0];
			if (intervalEndDateYr < finalCutRange[0] + lastHarvestDateYr) {
				return new ScheduledFinalHarvestInfo(null, 0d); // were not in the range yet
			} else {
				int upperBound = intervalEndDateYr <= finalCutRange[1] + lastHarvestDateYr ? 
						intervalEndDateYr : 
							finalCutRange[1] + lastHarvestDateYr;
				int lowerBound = intervalBeginDateYr <= finalCutRange[0] + lastHarvestDateYr ? 
						finalCutRange[0]  + lastHarvestDateYr : 
							intervalBeginDateYr;
				double margProb = (upperBound - lowerBound) / windowDurationYr; 
				double remainingProb = (finalCutRange[1] + lastHarvestDateYr - lowerBound) / windowDurationYr; 
				double outputProb = margProb / remainingProb;
				return new ScheduledFinalHarvestInfo(new int[] {lowerBound, upperBound}, outputProb);
			}
		}
	}

	
	/*
	 * Default treatment for wood production.
	 */
	boolean isTotalHarvest() {
		return treatmentType == TreatmentType.CPRS;
	}

	/*
	 * Default treatment for sensitive wood production.
	 */
	boolean isNoHarvest() {
		return treatmentType == TreatmentType.PROTECTION;
	}
	
	@Override
	public int getNbAdditionalFields() {return 1;}

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
	public OfficialHarvestTreatmentDefinition getDeepClone() {
		OfficialHarvestTreatmentDefinition copy = new OfficialHarvestTreatmentDefinition(this.treatmentType);
		copy.delayBeforeNextTreatmentYrs = delayBeforeNextTreatmentYrs;
		return copy;
	}

	@Override
	public TreatmentType getTreatmentType() {return treatmentType;}

	public boolean equals(Object o) {
		if (o instanceof OfficialHarvestTreatmentDefinition) {
			OfficialHarvestTreatmentDefinition def = (OfficialHarvestTreatmentDefinition) o;
			if (def.treatmentType == this.treatmentType && def.delayBeforeNextTreatmentYrs == this.delayBeforeNextTreatmentYrs) {
				return true;
			} else {
				return false;
			}
		} else {
			return super.equals(o);
		}
	}
}
