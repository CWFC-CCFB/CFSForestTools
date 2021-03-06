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
import java.util.Map;

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.DisturbanceType;
import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.OriginType;
import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.SoilDepth;
import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.SoilTexture;
import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider.DrainageGroup;

@SuppressWarnings("serial")
class Iris2020RecruitmentOccurrenceInternalPredictor extends REpiceaBinaryEventPredictor<Iris2020CompatiblePlot, Iris2020CompatibleTree> {

	
	private List<Integer> effectList;
	private final boolean offsetEnabled;
	
	protected Iris2020RecruitmentOccurrenceInternalPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled, boolean offsetEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect in this model
		this.offsetEnabled = offsetEnabled;
		init();
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
			int effectId = (int) effectMat.getValueAt(i, 0);
			effectList.add(effectId);
		}
	}
	
	@Override
	public synchronized double predictEventProbability(Iris2020CompatiblePlot plot, Iris2020CompatibleTree tree, Map<String, Object> parms) {
		Matrix beta = getParametersForThisRealization(plot);
		constructXVector(plot, tree);
		double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
		if (offsetEnabled) {
			xBeta += Math.log(plot.getGrowthStepLengthYr());
		}
		double recruitmentProbability = 1d - Math.exp(-Math.exp(xBeta));
		return recruitmentProbability;
	}

	private void constructXVector(Iris2020CompatiblePlot plot, Iris2020CompatibleTree tree) {
		oXVector.resetMatrix();
		
		double meanDegreeDays = plot.getMeanDegreeDaysOverThePeriod();
		double meanPrecipitation = plot.getMeanPrecipitationOverThePeriod();
		double frostDays = plot.getMeanNumberFrostDaysOverThePeriod();
		double lowestTmin = plot.getMeanLowestTemperatureOverThePeriod();
		
		SoilDepth depth = plot.getSoilDepth();
		SoilTexture texture = plot.getSoilTexture();
		DrainageGroup drainage = plot.getDrainageGroup();
		OriginType upcomingOrigin = plot.getUpcomingStandReplacementDisturbance();
		OriginType pastOrigin = plot.getPastStandReplacementDisturbance();
		DisturbanceType pastDist = plot.getPastPartialDisturbance();
		DisturbanceType upcomingDist = plot.getUpcomingPartialDisturbance();

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
			case 3: // DD:TotalPrcp
				oXVector.setValueAt(0, index, meanDegreeDays * meanPrecipitation);
				index++;
				break;
			case 4: // distanceToConspecific
				oXVector.setValueAt(0, index, distanceToConspecific);
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
				if (pastDist == DisturbanceType.OtherNatural) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 12: // 
				if (pastDist == DisturbanceType.Harvest) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 13: // 
				if (pastOrigin == OriginType.Fire) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 14: // 
				if (pastOrigin == OriginType.OtherNatural) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 15: // 
				if (pastOrigin == OriginType.Harvest) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 16: // 
				if (texture == SoilTexture.Crude) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 17: // 
				if (texture == SoilTexture.Fine) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 18: // 
				if (upcomingDist == DisturbanceType.OtherNatural) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 19: // 
				if (upcomingDist == DisturbanceType.Harvest) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 20: // 
				if (upcomingOrigin == OriginType.Fire) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 21: // 
				if (upcomingOrigin == OriginType.OtherNatural) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 22: // 
				if (upcomingOrigin == OriginType.Harvest) {
					oXVector.setValueAt(0, index, 1d);
				}
				index++;
				break;
			case 23: // 
				oXVector.setValueAt(0, index, frostDays);
				index++;
				break;
			case 24: // G_F
				oXVector.setValueAt(0, index, g_broadleaved);
				index++;
				break;
			case 25: // G_R
				oXVector.setValueAt(0, index, g_coniferous);
				index++;
				break;
			case 26: // G_SpGr
				oXVector.setValueAt(0, index, g_spgr);
				index++;
				break;
			case 27: //hasExpo:cosExpo
				if (slope > 3) { 
					if (aspect != 400d && aspect != 500d) {
						oXVector.setValueAt(0, index, Math.cos(2 * Math.PI * aspect / 360d));
					}
				}
				index++;
				break;
			case 28: // lnDt
				oXVector.setValueAt(0, index, Math.log(plot.getGrowthStepLengthYr()));
				index++;
				break;
			case 29: // lnG_F
				oXVector.setValueAt(0, index, Math.log(1d + g_broadleaved));
				index++;
				break;
			case 30: // lnG_R
				oXVector.setValueAt(0, index, Math.log(1d + g_coniferous));
				index++;
				break;
			case 31: // lnG_SpGr
				oXVector.setValueAt(0, index, Math.log(1d + g_spgr));
				index++;
				break;
			case 32: // lnPente
				oXVector.setValueAt(0, index, Math.log(1d + slope));
				index++;
				break;
			case 33: // logDD
				oXVector.setValueAt(0, index, Math.log(meanDegreeDays));
				index++;
				break;
			case 34: // logDD:logPrcp
				oXVector.setValueAt(0, index, Math.log(meanDegreeDays) * Math.log(meanPrecipitation));
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
			case 37: // logPrcp
				oXVector.setValueAt(0, index, lowestTmin);
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


}