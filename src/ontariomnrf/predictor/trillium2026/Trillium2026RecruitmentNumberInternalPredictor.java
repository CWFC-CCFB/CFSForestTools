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

import canforservutility.simulation.REpiceaRecruitmentNumberInternalPredictorWithOccupancyIndex;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("serial")
class Trillium2026RecruitmentNumberInternalPredictor extends REpiceaRecruitmentNumberInternalPredictorWithOccupancyIndex<Trillium2026RecruitmentPlot> {

	
	
	private final Trillium2026RecruitmentNumberPredictor owner;
	protected final double theta; // as produced by R
	protected final double invTheta; //
	
	protected Trillium2026RecruitmentNumberInternalPredictor(Trillium2026RecruitmentNumberPredictor owner,
			Species sp,
			boolean isParametersVariabilityEnabled, 
			boolean isResidualVariabilityEnabled, 
			double thetaParm,
			Matrix beta,
			SymmetricMatrix omega,
			Matrix effectMat) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled, sp);		// random effect stands for occupancy index variability
		this.owner = owner;
		ModelParameterEstimates estimate = new ModelParameterEstimates(beta, omega);
		setParameterEstimates(estimate);
		oXVector = new Matrix(1, estimate.getMean().m_iRows);
		
		for (int i = 0; i < effectMat.m_iRows; i++) {
			int effectId = (int) effectMat.getValueAt(i, 0);
			effectList.add(effectId);
			if (Trillium2026RecruitmentNumberPredictor.OccupancyIndexEffects.contains(effectId)) {
				occupancyIndexVarIndices.add(effectId);
			}
		}
		
		this.theta = thetaParm;
		this.invTheta = 1d/this.theta;
	}

	@Override
	protected void init() {}

	@Override
	protected double getNumber(double mu, boolean onTransformedScale) {
		if (onTransformedScale) {
			mu = Math.exp(mu); // we back transform
		}
		if (isResidualVariabilityEnabled) {
			return StatisticalUtility.getRandom().nextNegativeBinomial(mu, invTheta) + 1;
		} else {
			return mu + 1;		// offset 1 because y = nbRecruits - 1
		}
	}
	
	@Override
	protected void setValueInXVector(int effectId, Trillium2026RecruitmentPlot plot, Enum<?> species, double occupancyIndex25km) {
		int index = effectList.indexOf(effectId);
		if (index == -1) {
			throw new InvalidParameterException("The effect id " + effectId + " is not part of this model!");
		}
		switch(effectId) {
		case 1:	// intercept
			oXVector.setValueAt(0, index, 1d);
			break;
		case 2: // dt
			oXVector.setValueAt(0, index, plot.getGrowthStepLengthYr());
			break;
		case 3: // FrostFreeDay
			oXVector.setValueAt(0, index, plot.getAnnualNbFrostFreeDays(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 4: // G_F
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.BroadleavedSpecies));
			break;
		case 5: // G_F2
			double G_F = plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.BroadleavedSpecies);
			oXVector.setValueAt(0, index, G_F * G_F);
			break;
		case 6: // G_R
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.ConiferousSpecies));
			break;
		case 7: // G_R2
			double G_R = plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.ConiferousSpecies);
			oXVector.setValueAt(0, index, G_R * G_R);
			break;
		case 8: // G_SpGr
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpecies(species));
			break;
		case 9: // G_SpGr2
			double g_spgr = plot.getBasalAreaM2HaForThisSpecies(species);
			oXVector.setValueAt(0, index, g_spgr * g_spgr);
			break;
		case 10: // highest temperature
			oXVector.setValueAt(0, index, plot.getHighestAnnualTemperatureCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 11: // isHarvested
			oXVector.setValueAt(0, index, plot.isGoingToBeHarvested() ? 1d : 0d);
			break;
		case 12: // LowestTmin
			oXVector.setValueAt(0, index, plot.getLowestAnnualTemperatureCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 13: // LowestTmin2
			double lowestTmin = plot.getLowestAnnualTemperatureCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution);
			oXVector.setValueAt(0, index, lowestTmin * lowestTmin);
			break;
		case 14: // MeanTair
			oXVector.setValueAt(0, index, plot.getMeanAnnualTemperatureCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 15: // MeanTair2
			double meanTair = plot.getMeanAnnualTemperatureCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution);
			oXVector.setValueAt(0, index, meanTair * meanTair);
			break;
		case 16: // MeanTmaxJuly
			oXVector.setValueAt(0, index, plot.getMeanMaximumJulyTemperatureCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 17: // MeanTmaxJuly2
			double meanTMaxJuly = plot.getMeanMaximumJulyTemperatureCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution);
			oXVector.setValueAt(0, index, meanTMaxJuly * meanTMaxJuly);
			break;
		case 18: // slopePct_PDEM_mean	
			oXVector.setValueAt(0, index, plot.getSlopeInclinationPercent());
			break;
		case 19: // speciesThere
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpecies(species) > 0 ? 1d : 0d);
			break;
		case 20: // TotalPrcp
			oXVector.setValueAt(0, index, plot.getTotalAnnualPrecipitationMm(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 21: // TotalPrecFromMarchToMay
			oXVector.setValueAt(0, index, plot.getTotalPrecipitationFromMarchToMayMm(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 22: // TotalPrecFromMarchToMay2
			double precFromMayToMarch = plot.getTotalPrecipitationFromMarchToMayMm(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution);
			oXVector.setValueAt(0, index, precFromMayToMarch * precFromMayToMarch);
			break;
		case 23: // wasHarvested
			oXVector.setValueAt(0, index, plot.isInterventionResult() ? 1d : 0d);
			break;
		default:
			throw new InvalidParameterException("The effect id " + effectId + " is unknown!");
		}
	}
	
}