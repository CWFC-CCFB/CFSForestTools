/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2020 Mathieu Fortin for Canadian Forest Service
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
	
	protected void setBeta(Matrix beta, Matrix omega) {
		ModelParameterEstimates estimate = new ModelParameterEstimates(beta, omega);
		setParameterEstimates(estimate);
		oXVector = new Matrix(1, estimate.getMean().m_iRows);
	}
	
	protected void setEffectList(Matrix effectMat) {
		for (int i = 0; i < effectMat.m_iRows; i++) {
			int effectId = (int) effectMat.m_afData[i][0];
			effectList.add(effectId);
		}
	}
	
	
	private void constructXVector(Iris2020CompatiblePlot plot, Iris2020CompatibleTree tree) {
		oXVector.resetMatrix();
		
		double meanDegreeDays = plot.getMeanDegreeDaysOverThePeriod();
		double meanPrecipitation = plot.getMeanPrecipitationOverThePeriod();
		
		SoilDepth depth = plot.getSoilDepth();
		SoilTexture texture = plot.getSoilTexture();
		DrainageGroup drainage = plot.getDrainageGroup();
		OriginType origin = plot.getOrigin();
		DisturbanceType pastDist = plot.getPastDisturbance();
		DisturbanceType upcomingDist = plot.getUpcomingDisturbance();

		double g_broadleaved = plot.getBasalAreaOfBroadleavedSpecies();
		double g_coniferous = plot.getBasalAreaOfConiferousSpecies();
		double g_spgr = plot.getBasalAreaM2HaBySpecies().m_afData[0][tree.getSpecies().ordinal()];
		
		double slope = plot.getSlopeInclinationPercent();
		double aspect = plot.getSlopeAspect();
		
		int index = 0;
		for (int effectId : effectList) {
			switch(effectId) {
			case 1:	// intercept
				oXVector.m_afData[0][index] = 1d;
				index++;
				break;
			case 2: // DD
				oXVector.m_afData[0][index] = meanDegreeDays;
				index++;
				break;
			case 3: // dt
				oXVector.m_afData[0][index] = plot.getGrowthStepLengthYr();
				index++;
				break;
			case 4: // 
				if (depth == SoilDepth.Average) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 5: // 
				if (depth == SoilDepth.Shallow) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 6: // 
				if (depth == SoilDepth.VeryShallow) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 7: // 
				if (drainage == DrainageGroup.Mesic) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 8: // 
				if (drainage == DrainageGroup.Subhydric) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 9: // 
				if (drainage == DrainageGroup.Hydric) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 10: // 
				if (origin == OriginType.Fire) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 11: // 
				if (origin == OriginType.OtherNatural) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 12: // 
				if (origin == OriginType.Harvest) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 13: // 
				if (pastDist == DisturbanceType.Fire) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 14: // 
				if (pastDist == DisturbanceType.OtherNatural) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 15: // 
				if (pastDist == DisturbanceType.Harvest) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 16: // 
				if (texture == SoilTexture.Crude) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 17: // 
				if (texture == SoilTexture.Mixed) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 18: // 
				if (texture == SoilTexture.Fine) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 19: // 
				if (upcomingDist == DisturbanceType.Fire) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 20: // 
				if (upcomingDist == DisturbanceType.OtherNatural) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 21: // 
				if (upcomingDist == DisturbanceType.Harvest) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 22:
				oXVector.m_afData[0][index] = g_broadleaved;
				index++;
				break;
			case 23:
				oXVector.m_afData[0][index] = g_coniferous;
				index++;
				break;
			case 24:
				oXVector.m_afData[0][index] = g_spgr;
				index++;
				break;
			case 25:
				if (slope > 3) { 
					if (aspect != 400d && aspect != 500d) {
						oXVector.m_afData[0][index] = Math.cos(2 * Math.PI * aspect / 360d);
					}
				}
				index++;
				break;
			case 26: // lnDt
				oXVector.m_afData[0][index] = Math.log(plot.getGrowthStepLengthYr());
				index++;
				break;
			case 27: // lnG_F
				oXVector.m_afData[0][index] = Math.log(1d + g_broadleaved);
				index++;
				break;
			case 28: // lnG_R
				oXVector.m_afData[0][index] = Math.log(1d + g_coniferous);
				index++;
				break;
			case 29: // lnG_SpGr
				oXVector.m_afData[0][index] = Math.log(1d + g_spgr);
				index++;
				break;
			case 30: // lnPente
				oXVector.m_afData[0][index] = Math.log(1d + slope);
				index++;
				break;
			case 31: // logDD
				oXVector.m_afData[0][index] = Math.log(meanDegreeDays);
				index++;
				break;
			case 32: // logPrcp
				oXVector.m_afData[0][index] = Math.log(meanPrecipitation);
				index++;
				break;
			case 33: // pentePerc
				oXVector.m_afData[0][index] = slope;
				index++;
				break;
			case 34: // speciesThere
				if (g_spgr > 0) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 35: // timeSince1970
				oXVector.m_afData[0][index] = plot.getDateYr() + plot.getGrowthStepLengthYr() - 1970;
				index++;
				break;
			case 36: // TotalPrcp
				oXVector.m_afData[0][index] = meanPrecipitation;
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
		double xBeta = oXVector.multiply(beta).m_afData[0][0];
		double mu = Math.exp(xBeta);
		if (isResidualVariabilityEnabled) {
			return StatisticalUtility.getRandom().nextNegativeBinomial(mu, invTheta) + 1;
		} else {
			return mu + 1;		// offset 1 because y = nbRecruits - 1
		}
	}


}