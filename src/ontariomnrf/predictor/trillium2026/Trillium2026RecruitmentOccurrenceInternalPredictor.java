/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2026 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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
package ontariomnrf.predictor.trillium2026;

import java.security.InvalidParameterException;
import java.util.Map;

import canforservutility.simulation.REpiceaRecruitmentOccurrenceInternalPredictorWithOccupancyIndex;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.simulation.species.REpiceaSpecies.Species;

@SuppressWarnings("serial")
class Trillium2026RecruitmentOccurrenceInternalPredictor extends REpiceaRecruitmentOccurrenceInternalPredictorWithOccupancyIndex<Trillium2026RecruitmentPlot, Trillium2026Tree> {

	private final Trillium2026RecruitmentOccurrencePredictor owner;


	
	protected Trillium2026RecruitmentOccurrenceInternalPredictor(Trillium2026RecruitmentOccurrencePredictor owner,
			Species species,
			boolean isParametersVariabilityEnabled, 
			boolean isResidualVariabilityEnabled, 
			boolean offsetEnabled, 
			Matrix beta,
			SymmetricMatrix omega,
			Matrix effectMat) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled, species, offsetEnabled);	
		this.owner = owner;
		ModelParameterEstimates estimate = new ModelParameterEstimates(beta, omega);
		setParameterEstimates(estimate);
		oXVector = new Matrix(1, estimate.getMean().m_iRows);
		
		for (int i = 0; i < effectMat.m_iRows; i++) {
			int effectId = (int) effectMat.getValueAt(i, 0);
			effectList.add(effectId);
			if (Trillium2026RecruitmentOccurrencePredictor.OccupancyIndexEffects.contains(effectId)) {
				occupancyIndexVarIndices.add(effectId);
			}
		}
	}

	@Override
	protected void init() {}
	
	@Override
	public double predictEventProbability(Trillium2026RecruitmentPlot plot, Trillium2026Tree tree, Map<String, Object> parms) {
		return calculateEventProbability(plot);
	}

	@Override
	protected double getProb(Matrix beta, Trillium2026RecruitmentPlot plot) {
		double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
		xBeta += addOffsetIfNeeded(plot);
		double recruitmentProbability = 1d - Math.exp(-Math.exp(xBeta));
		return recruitmentProbability < owner.minProbRecruitmentThreshold ? // if the recruitment probability is smaller than the threshold
				0d : 														// the recruitment probability is assumed to be 0
					recruitmentProbability;
	}

	@Override
	protected void setValueInXVector(int effectId, Trillium2026RecruitmentPlot plot, double occupancyIndex25km) {
		int index = effectList.indexOf(effectId);
		if (index == -1) {
			throw new InvalidParameterException("The effect id " + effectId + " is not part of this model!");
		}
		switch(effectId) {
		case 11: // intercept too
		case 1: // intercept
			oXVector.setValueAt(0, index, 1d);
			break;
		case 2:	// areaM2.x
			oXVector.setValueAt(0, index, plot.getAreaHa() * 10000);
			break;
		case 3:	// DD
			oXVector.setValueAt(0, index, plot.getGrowingDegreeDaysCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 4: // DD2
			double dd = plot.getGrowingDegreeDaysCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution);
			oXVector.setValueAt(0, index, dd * dd);
			break;
		case 5: // G_F
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.BroadleavedSpecies));
			break;
		case 6: // G_F2
			double G_F = plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.BroadleavedSpecies);
			oXVector.setValueAt(0, index, G_F * G_F);
			break;
		case 7: // G_R
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.ConiferousSpecies));
			break;
		case 8: // G_R2
			double G_R = plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.ConiferousSpecies);
			oXVector.setValueAt(0, index, G_R * G_R);
			break;
		case 9: // G_SpGr
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpecies(species));
			break;
		case 10: // G_SpGr2
			double g_spgr = plot.getBasalAreaM2HaForThisSpecies(species);
			oXVector.setValueAt(0, index, g_spgr * g_spgr);
			break;
		case 12: // isHarvested
			oXVector.setValueAt(0, index, plot.isGoingToBeHarvested() ? 1d : 0d);
			break;
		case 13: // lnDt
			oXVector.setValueAt(0, index, Math.log(plot.getGrowthStepLengthYr()));
			break;
		case 14: // LowestTmin
			oXVector.setValueAt(0, index, plot.getLowestAnnualTemperatureCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 15: // MeanTminJanuary
			oXVector.setValueAt(0, index, plot.getMeanMinimumJanuaryTemperatureCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 16: // MeanTminJanuary2
			double minTempJan = plot.getMeanMinimumJanuaryTemperatureCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution);
			oXVector.setValueAt(0, index, minTempJan * minTempJan);
			break;
		case 17: // occIndex25km
			oXVector.setValueAt(0, index, occupancyIndex25km);
			break;
		case 18: // speciesThere
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpecies(species) > 0 ? 1d : 0d);
			break;
		case 19: // occIndex25km2
			oXVector.setValueAt(0, index, occupancyIndex25km * occupancyIndex25km);
			break;
		case 20: // TotalPrcp
			oXVector.setValueAt(0, index, plot.getTotalAnnualPrecipitationMm(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 21: // TotalPrcp2
			double totalPrcp = plot.getTotalAnnualPrecipitationMm(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution);
			oXVector.setValueAt(0, index, totalPrcp * totalPrcp);
			break;
		case 22: // TotalPrecJuneToAugust
			oXVector.setValueAt(0, index, plot.getTotalPrecipitationFromJuneToAugustMm(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 23: // TotalPrecMarchToMay
			oXVector.setValueAt(0, index, plot.getTotalPrecipitationFromMarchToMayMm(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		default:
			throw new InvalidParameterException("The effect id " + effectId + " is unknown!");
		}
	}

	@Override
	protected double addOffsetIfNeeded(Trillium2026RecruitmentPlot plot) {
		return offsetEnabled ? 
				Math.log(plot.getGrowthStepLengthYr()) :
					0;
	}


}