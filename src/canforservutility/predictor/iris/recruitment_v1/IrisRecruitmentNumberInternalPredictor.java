/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2020-2023 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package canforservutility.predictor.iris.recruitment_v1;

import java.security.InvalidParameterException;

import canforservutility.predictor.iris.recruitment_v1.IrisRecruitmentPlot.DisturbanceType;
import canforservutility.predictor.iris.recruitment_v1.IrisRecruitmentPlot.SoilDepth;
import canforservutility.predictor.iris.recruitment_v1.IrisRecruitmentPlot.SoilTexture;
import canforservutility.predictor.iris.recruitment_v1.IrisTree.IrisSpecies;
import canforservutility.simulation.REpiceaRecruitmentNumberInternalPredictorWithOccupancyIndex;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider.DrainageGroup;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("serial")
class IrisRecruitmentNumberInternalPredictor extends REpiceaRecruitmentNumberInternalPredictorWithOccupancyIndex<IrisRecruitmentPlot> {

	
	
	private final IrisRecruitmentNumberPredictor owner;
	protected final double theta; // as produced by R
	protected final double invTheta; //
	
	protected IrisRecruitmentNumberInternalPredictor(IrisRecruitmentNumberPredictor owner,
			boolean isParametersVariabilityEnabled, 
			boolean isResidualVariabilityEnabled, 
			double thetaParm,
			Matrix beta,
			SymmetricMatrix omega,
			Matrix effectMat,
			IrisSpecies species) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled, species);		// random effect stands for occupancy index variability
		this.owner = owner;
		
		ModelParameterEstimates estimate = new ModelParameterEstimates(beta, omega);
		setParameterEstimates(estimate);
		oXVector = new Matrix(1, estimate.getMean().m_iRows);
		
		for (int i = 0; i < effectMat.m_iRows; i++) {
			int effectId = (int) effectMat.getValueAt(i, 0);
			effectList.add(effectId);
			if (IrisRecruitmentNumberPredictor.OccupancyIndexEffects.contains(effectId)) {
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
			mu = Math.exp(mu);
		}
		if (isResidualVariabilityEnabled) {
			return StatisticalUtility.getRandom().nextNegativeBinomial(mu, invTheta) + 1;
		} else {
			return mu + 1;		// offset 1 because y = nbRecruits - 1
		}
	}
	
	@Override
	protected void setValueInXVector(int effectId, IrisRecruitmentPlot plot, Enum<?> species, double occupancyIndex10km) {
		int index = effectList.indexOf(effectId);
		if (index == -1) {
			throw new InvalidParameterException("The effect id " + effectId + " is not part of this model!");
		}
		switch(effectId) {
		case 22: // intercept as well
		case 1:	// intercept
			oXVector.setValueAt(0, index, 1d);
			break;
		case 2: // DD
			oXVector.setValueAt(0, index, plot.getGrowingDegreeDaysCelsius(owner,
					IrisRecruitmentOccurrencePredictor.RecruitmentClimateVariableResolution));
			break;
		case 3: // DD2
			double DD = plot.getGrowingDegreeDaysCelsius(owner,
					IrisRecruitmentOccurrencePredictor.RecruitmentClimateVariableResolution);
			oXVector.setValueAt(0, index, DD * DD);
			break;
		case 4: // dt
			oXVector.setValueAt(0, index, plot.getGrowthStepLengthYr());
			break;
		case 5: // 
			if (plot.getSoilDepth() == SoilDepth.VeryShallow) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 6: // 
			if (plot.getDrainageGroup() == DrainageGroup.Subhydric) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 7: // 
			if (plot.getDrainageGroup() == DrainageGroup.Hydric) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 8: // 
			if (plot.getPastDisturbance() == DisturbanceType.Fire) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 9: // 
			if (plot.getPastDisturbance() == DisturbanceType.OtherNatural) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 10: // 
			if (plot.getPastDisturbance() == DisturbanceType.Harvest) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 11: // 
			if (plot.getSoilTexture() == SoilTexture.Crude) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 12: // 
			if (plot.getSoilTexture() == SoilTexture.Fine) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 13: // 
			if (plot.getUpcomingDisturbance() == DisturbanceType.Fire) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 14: // 
			if (plot.getUpcomingDisturbance() == DisturbanceType.OtherNatural) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 15: // 
			if (plot.getUpcomingDisturbance() == DisturbanceType.Harvest) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 16: // FrostDay
			oXVector.setValueAt(0, index, plot.getAnnualNbFrostDays(owner,
					IrisRecruitmentOccurrencePredictor.RecruitmentClimateVariableResolution));
			break;
		case 17: // G_F
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.BroadleavedSpecies));
			break;
		case 18: // G_R
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.ConiferousSpecies));
			break;
		case 19: // G_R2
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.ConiferousSpecies) * 
					plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.ConiferousSpecies));
			break;
		case 20: // G_SpGr
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpecies(species));
			break;
		case 21: // G_SpGr2
			double g_spgr = plot.getBasalAreaM2HaForThisSpecies(species);
			oXVector.setValueAt(0, index, g_spgr * g_spgr);
			break;
		case 23: // lnDt
			oXVector.setValueAt(0, index, Math.log(plot.getGrowthStepLengthYr()));
			break;
		case 24: // LowestTmin
			oXVector.setValueAt(0, index, plot.getLowestAnnualTemperatureCelsius(owner,
					IrisRecruitmentOccurrencePredictor.RecruitmentClimateVariableResolution));
			break;
		case 25: // occIndex10km
			oXVector.setValueAt(0, index, occupancyIndex10km);
			break;
		case 26: // pentePerc
			oXVector.setValueAt(0, index, plot.getSlopeInclinationPercent());
			break;
		case 27: // speciesThere
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpecies(species) > 0 ? 1d : 0d);
			break;
		case 29: // timeSince1970
			oXVector.setValueAt(0, index, plot.getDateYr() + plot.getGrowthStepLengthYr() - 1970);
			break;
		case 30: // TotalPrcp
			oXVector.setValueAt(0, index, plot.getTotalAnnualPrecipitationMm(owner,
					IrisRecruitmentOccurrencePredictor.RecruitmentClimateVariableResolution));
			break;
		case 31: // TotalPrcp * TotalPrcp
			double totalPrcp = plot.getTotalAnnualPrecipitationMm(owner,
					IrisRecruitmentOccurrencePredictor.RecruitmentClimateVariableResolution);
			oXVector.setValueAt(0, index, totalPrcp * totalPrcp);
			break;
		default:
			throw new InvalidParameterException("The effect id " + effectId + " is unknown!");
		}
	}
}