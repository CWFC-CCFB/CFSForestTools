/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2020-2021 Her Majesty the Queen in right of Canada
 * author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package canforservutility.predictor.iris2020.recruitment;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.DisturbanceType;
import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.OriginType;
import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.SoilDepth;
import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.SoilTexture;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider.DrainageGroup;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("serial")
class Iris2020RecruitmentNumberInternalPredictor extends REpiceaPredictor {

	private List<Integer> effectList;
	protected final double theta; // as produced by R
	protected final double invTheta; //
	
	protected Iris2020RecruitmentNumberInternalPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled, double thetaParm) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect in this model
		init();
		this.theta = thetaParm;
		this.invTheta = 1d/this.theta;
	}

	protected void init() {
		effectList = new ArrayList<Integer>();
	}
	
	protected void setBeta(Matrix beta, SymmetricMatrix omega) {
		ModelParameterEstimates estimate = new ModelParameterEstimates(beta, omega);
		setParameterEstimates(estimate);
		oXVector = new Matrix(1, estimate.getMean().m_iRows);
	}
	
	protected void setEffectList(Matrix effectMat) {
		for (int i = 0; i < effectMat.m_iRows; i++) {
			int effectId = (int) effectMat.getValueAt(i, 0);
			effectList.add(effectId);
		}
	}
	
	
	private void constructXVector(Iris2020CompatiblePlot plot, Iris2020CompatibleTree tree) {
		oXVector.resetMatrix();
		
		double meanDegreeDays = plot.getMeanDegreeDaysOverThePeriod();
		double meanPrecipitation = plot.getMeanPrecipitationOverThePeriod();
		double meanFrostDays = plot.getMeanNumberFrostDaysOverThePeriod();
		double meanLowestTmin = plot.getMeanLowestTemperatureOverThePeriod();
		
		SoilDepth depth = plot.getSoilDepth();
		SoilTexture texture = plot.getSoilTexture();
		DrainageGroup drainage = plot.getDrainageGroup();
		OriginType upcomingOrigin = plot.getUpcomingStandReplacementDisturbance();
		OriginType pastOrigin = plot.getPastStandReplacementDisturbance();
		DisturbanceType upcomingDist = plot.getUpcomingPartialDisturbance();
		DisturbanceType pastDist = plot.getPastPartialDisturbance();

		double g_broadleaved = plot.getBasalAreaOfBroadleavedSpecies();
		double g_coniferous = plot.getBasalAreaOfConiferousSpecies();
		double g_spgr = plot.getBasalAreaM2HaBySpecies().getValueAt(0, tree.getSpecies().ordinal());
		
		double slope = plot.getSlopeInclinationPercent();
		double aspect = plot.getSlopeAspect();
		
		double distanceToConspecific = plot.getDistanceToConspecificKm(tree.getSpecies());

		int index = 0;
		for (int effectId : effectList) {
			switch(effectId) {
			case 1:	// intercept
				oXVector.setValueAt(0, index, 1d);
				index++;
				break;
			case 2: // DD
				oXVector.setValueAt(0, index, meanDegreeDays);
				index++;
				break;
			case 3: // distanceToConspecific
				oXVector.setValueAt(0, index, distanceToConspecific);
				index++;
				break;
			case 4: // dt
				oXVector.setValueAt(0, index, plot.getGrowthStepLengthYr());
				index++;
				break;
			case 5: // 
				if (depth == SoilDepth.Thick) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 6: // 
				if (depth == SoilDepth.Shallow) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 7: // 
				if (depth == SoilDepth.VeryShallow) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 8: // 
				if (drainage == DrainageGroup.Xeric) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 9: // 
				if (drainage == DrainageGroup.Subhydric) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 10: // 
				if (drainage == DrainageGroup.Hydric) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 11: // 
				if (pastDist == DisturbanceType.Fire) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 12: // 
				if (pastDist == DisturbanceType.OtherNatural) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 13: // 
				if (pastDist == DisturbanceType.Harvest) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 14: // 
				if (pastOrigin == OriginType.Fire) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 15: // 
				if (pastOrigin == OriginType.OtherNatural) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 16: // 
				if (pastOrigin == OriginType.Harvest) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 17: // 
				if (texture == SoilTexture.Crude) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 18: // 
				if (texture == SoilTexture.Fine) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 19: // 
				if (upcomingDist == DisturbanceType.Fire) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 20: // 
				if (upcomingDist == DisturbanceType.OtherNatural) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 21: // 
				if (upcomingDist == DisturbanceType.Harvest) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 22: // 
				if (upcomingOrigin == OriginType.Fire) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 23: // 
				if (upcomingOrigin == OriginType.OtherNatural) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 24: // 
				if (upcomingOrigin == OriginType.Harvest) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 25: // FrostDay
				oXVector.setValueAt(0, index, meanFrostDays);
				index++;
				break;
			case 26: // G_F
				oXVector.setValueAt(0, index, g_broadleaved);
				index++;
				break;
			case 27: // G_R
				oXVector.setValueAt(0, index, g_coniferous);
				index++;
				break;
			case 28: // G_SpGr
				oXVector.setValueAt(0, index, g_spgr);
				index++;
				break;
			case 29: // hasExpo:cosExpo
				if (slope > 3) { 
					if (aspect != 400d && aspect != 500d) {
						oXVector.setValueAt(0, index, Math.cos(2 * Math.PI * aspect / 360d));
					}
				}
				index++;
				break;
			case 30: // lnDt
				oXVector.setValueAt(0, index, Math.log(plot.getGrowthStepLengthYr()));
				index++;
				break;
			case 31: // lnG_F
				oXVector.setValueAt(0, index, Math.log(1d + g_broadleaved));
				index++;
				break;
			case 32: // lnG_SpGr
				oXVector.setValueAt(0, index, Math.log(1d + g_spgr));
				index++;
				break;
			case 33: // lnPente
				oXVector.setValueAt(0, index, Math.log(1d + slope));
				index++;
				break;
			case 34: // logDD
				oXVector.setValueAt(0, index, Math.log(meanDegreeDays));
				index++;
				break;
			case 35: // LOGdistanceToConspecific
				oXVector.setValueAt(0, index, Math.log(1 + distanceToConspecific));
				index++;
				break;
			case 36: // logPrcp
				oXVector.setValueAt(0, index, Math.log(meanPrecipitation));
				index++;
				break;
			case 37: // LowestTmin
				oXVector.setValueAt(0, index, meanLowestTmin);
				index++;
				break;
			case 38: // pentePerc
				oXVector.setValueAt(0, index, slope);
				index++;
				break;
			case 39: // timeSince1970
				oXVector.setValueAt(0, index, plot.getDateYr() + plot.getGrowthStepLengthYr() - 1970);
				index++;
				break;
			case 40: // TotalPrcp
				oXVector.setValueAt(0, index, meanPrecipitation);
				index++;
				break;
			default:
				throw new InvalidParameterException("The effect id " + effectId + " is unknown!");
			}
		}
	}

	protected double predictNumberOfRecruits(Iris2020CompatiblePlot plot, Iris2020CompatibleTree tree) {
		Matrix beta = getParametersForThisRealization(plot);
		constructXVector(plot, tree);
		double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
		double mu = Math.exp(xBeta);
		if (isResidualVariabilityEnabled) {
			return StatisticalUtility.getRandom().nextNegativeBinomial(mu, invTheta) + 1;
		} else {
			return mu + 1;		// offset 1 because y = nbRecruits - 1
		}
	}


}