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
import canforservutility.predictor.iris2020.recruitment.Iris2020CompatiblePlot.SoilTexture;
import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("serial")
class Iris2020RecruitmentNumberInternalPredictor extends REpiceaPredictor {

	private static List<Integer> EffectsNotToBeConsidered = new ArrayList<Integer>();
	static {
		EffectsNotToBeConsidered.add(3); // drainage class
		EffectsNotToBeConsidered.add(4);
		
		EffectsNotToBeConsidered.add(19); // originType
		EffectsNotToBeConsidered.add(20);

		EffectsNotToBeConsidered.add(22); // pastDisturbanceType
		EffectsNotToBeConsidered.add(23);

		EffectsNotToBeConsidered.add(26); // texture
	}
	
	private List<Integer> effectList;
	protected final double theta;
	
	protected Iris2020RecruitmentNumberInternalPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled, double thetaParm) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);		// no random effect in this model
		init();
		this.theta = thetaParm;
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
			if (!EffectsNotToBeConsidered.contains(effectId) & !effectList.contains(effectId)) {
				effectList.add(effectId);
			}
		}
	}
	
	
	private void constructXVector(Iris2020CompatiblePlot plot, Iris2020CompatibleTree tree) {
		oXVector.resetMatrix();
		
		double meanDegreeDays = plot.getMeanDegreeDaysOverThePeriod();
		double meanPrecipitation = plot.getMeanPrecipitationOverThePeriod();
		double meanGrowingSeasonLength = plot.getMeanGrowingSeasonLengthOverThePeriod();
		double meanFrostDays = plot.getMeanFrostDaysOverThePeriod();
		double meanLowestTmin = plot.getMeanLowestTminOverThePeriod();
		
		SoilTexture texture = plot.getSoilTexture();
		OriginType origin = plot.getOrigin();
		DisturbanceType pastDist = plot.getPastDisturbance();
		
		double gTot = plot.getBasalAreaM2Ha();		// TODO this could be optimized by calling it before entering this function.
		double nTot = plot.getNumberOfStemsHa();
		double slope = plot.getSlopeInclinationPercent();

		Matrix dummy;
		
		int index = 0;
		for (int effectId : effectList) {
			switch(effectId) {
			case 1:	// intercept
				oXVector.m_afData[0][index] = 1d;
				index++;
				break;
			case 2: // drainage class
				dummy = Iris2020RecruitmentOccurrenceInternalPredictor.DrainageGroupDummyMatrices.get(plot.getDrainageGroup());
				oXVector.setSubMatrix(dummy, 0, index);
				index += dummy.m_iCols;
				break;
			case 5: // DD
				oXVector.m_afData[0][index] = meanDegreeDays;
				index++;
				break;
			case 6: // dt
				oXVector.m_afData[0][index] = plot.getGrowthStepLengthYr();
				index++;
				break;
			case 7: // FrostDay
				oXVector.m_afData[0][index] = meanFrostDays;
				index++;
				break;
			case 8: // G_SpGr
				oXVector.m_afData[0][index] = plot.getBasalAreaM2HaBySpecies().m_afData[0][tree.getSpecies().ordinal()];
				index++;
				break;
			case 9: // G_TOT
				oXVector.m_afData[0][index] = gTot;
				index++;
				break;
			case 10: // Length
				oXVector.m_afData[0][index] = meanGrowingSeasonLength;
				index++;
				break;
			case 11: // lnG_TOT
				oXVector.m_afData[0][index] = Math.log(1d + gTot);
				index++;
				break;
			case 12: // lnN_TOT
				oXVector.m_afData[0][index] = Math.log(1d + nTot);
				index++;
				break;
			case 13: // lnPente
				oXVector.m_afData[0][index] = Math.log(1d + slope);
				index++;
				break;
			case 14: // logDD
				oXVector.m_afData[0][index] = Math.log(meanDegreeDays);
				index++;
				break;
			case 15: // logPrcp
				oXVector.m_afData[0][index] = Math.log(meanPrecipitation);
				index++;
				break;
			case 16: // LowestTmin
				oXVector.m_afData[0][index] = meanLowestTmin;
				index++;
				break;
			case 17: // N_TOT
				oXVector.m_afData[0][index] = nTot;
				index++;
				break;
			case 18: // originType
				dummy = origin.getDummyMatrix(); // origin effect
				oXVector.setSubMatrix(dummy, 0, index);
				index += dummy.m_iCols;
				break;
			case 21: // pastDisturbanceType
				dummy = pastDist.getDummyMatrix(); // origin effect
				oXVector.setSubMatrix(dummy, 0, index);
				index += dummy.m_iCols;
				break;
			case 24: // pentePerc
				oXVector.m_afData[0][index] = slope;
				index++;
				break;
			case 25: // texture
				dummy = texture.getDummyMatrix(); // origin effect
				oXVector.setSubMatrix(dummy, 0, index);
				index += dummy.m_iCols;
				break;
			case 27: // timeSince1970
				oXVector.m_afData[0][index] = plot.getDateYr() + plot.getGrowthStepLengthYr() - 1970;
				index++;
				break;
			case 28:
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
			return StatisticalUtility.getRandom().nextNegativeBinomial(mu, theta) + 1;
		} else {
			return mu + 1;		// offset 1 because y = nbRecruits - 1
		}
	}


}