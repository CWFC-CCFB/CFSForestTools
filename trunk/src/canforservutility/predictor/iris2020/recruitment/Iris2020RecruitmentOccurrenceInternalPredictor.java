/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2009-2020 Mathieu Fortin for Canadian Forest Service
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
import repicea.simulation.REpiceaBinaryEventPredictor;

@SuppressWarnings("serial")
class Iris2020RecruitmentOccurrenceInternalPredictor extends REpiceaBinaryEventPredictor<Iris2020CompatiblePlot, Iris2020CompatibleTree> {


	/*
	 * Those are the effects id that are not needed.
	 */
	private static List<Integer> EffectsNotToBeConsidered = new ArrayList<Integer>();
	static {
		EffectsNotToBeConsidered.add(4);
		EffectsNotToBeConsidered.add(5);
		
		EffectsNotToBeConsidered.add(21);
		EffectsNotToBeConsidered.add(22);
		EffectsNotToBeConsidered.add(23);
		EffectsNotToBeConsidered.add(24);
		EffectsNotToBeConsidered.add(25);
		
		EffectsNotToBeConsidered.add(27);
		EffectsNotToBeConsidered.add(28);
		EffectsNotToBeConsidered.add(29);
		EffectsNotToBeConsidered.add(30);

		EffectsNotToBeConsidered.add(33);

		EffectsNotToBeConsidered.add(37);
		EffectsNotToBeConsidered.add(38);
		EffectsNotToBeConsidered.add(39);
		EffectsNotToBeConsidered.add(40);
	}
	
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
			int effectId = (int) effectMat.m_afData[i][0];
			if (!EffectsNotToBeConsidered.contains(effectId) && !effectList.contains(effectId)) {
				effectList.add(effectId);
			}
		}
	}
	
	@Override
	public synchronized double predictEventProbability(Iris2020CompatiblePlot plot, Iris2020CompatibleTree tree, Object... parms) {
		Matrix beta = getParametersForThisRealization(plot);
		constructXVector(plot, tree);
		double xBeta = oXVector.multiply(beta).m_afData[0][0];
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
		double meanGrowingSeasonLength = plot.getMeanGrowingSeasonLengthOverThePeriod();
		double meanFrostDays = plot.getMeanFrostDaysOverThePeriod();
		double meanLowestTmin = plot.getMeanLowestTminOverThePeriod();
		
		SoilDepth depth = plot.getSoilDepth();
		SoilTexture texture = plot.getSoilTexture();
		OriginType origin = plot.getOrigin();
		DisturbanceType pastDist = plot.getPastDisturbance();
		DisturbanceType upcomingDist = plot.getUpcomingDisturbance();
		
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
			case 2:
				oXVector.m_afData[0][index] = meanDegreeDays;
				index++;
				break;
			case 3:
				dummy = depth.getDummyMatrix();
				oXVector.setSubMatrix(dummy, 0, index);
				index += dummy.m_iCols;
				break;
			case 6:
				oXVector.m_afData[0][index] = meanFrostDays;
				index++;
				break;
			case 7:
				oXVector.m_afData[0][index] = gTot;
				index++;
				break;
			case 8:
				if (plot.isOrganicSoil()) {
					oXVector.m_afData[0][index] = 1d;
				}
				index++;
				break;
			case 9:
				oXVector.m_afData[0][index] = meanGrowingSeasonLength;
				index++;
				break;
			case 10:
				oXVector.m_afData[0][index] = Math.log(plot.getGrowthStepLengthYr());
				index++;
				break;
			case 11:
				oXVector.m_afData[0][index] = Math.log(1d + gTot);
				index++;
				break;
			case 12:
				oXVector.m_afData[0][index] = Math.log(1d + nTot);
				index++;
				break;
			case 13:
				oXVector.m_afData[0][index] = Math.log(1d + slope);
				index++;
				break;
			case 14:
				oXVector.m_afData[0][index] = Math.log(meanDegreeDays);
				index++;
				break;
			case 15:
				oXVector.m_afData[0][index] = Math.log(meanDegreeDays) * Math.log(meanPrecipitation);
				index++;
				break;
			case 16:
				oXVector.m_afData[0][index] = Math.log(meanGrowingSeasonLength);
				index++;
				break;
			case 17:
				oXVector.m_afData[0][index] = Math.log(meanPrecipitation);
				index++;
				break;
			case 18:
				oXVector.m_afData[0][index] = meanLowestTmin;
				index++;
				break;
			case 19:
				oXVector.m_afData[0][index] = nTot;
				index++;
				break;
			case 20:
				dummy = origin.getDummyMatrix(); // origin effect
				oXVector.setSubMatrix(dummy, 0, index);
				index += dummy.m_iCols;
				break;
			case 26:
				dummy = pastDist.getDummyMatrix(); // past disturbance effect
				oXVector.setSubMatrix(dummy, 0, index);
				index += dummy.m_iCols;
				break;
			case 31:
				oXVector.m_afData[0][index] = slope;
				index++;
				break;
			case 32:
				dummy = texture.getDummyMatrix(); // texture effect
				oXVector.setSubMatrix(dummy, 0, index);
				index += dummy.m_iCols;
				break;
			case 34:
				oXVector.m_afData[0][index] = plot.getDateYr() + plot.getGrowthStepLengthYr() - 1970;
				index++;
				break;
			case 35:
				oXVector.m_afData[0][index] = meanPrecipitation;
				index++;
				break;
			case 36:
				dummy = upcomingDist.getDummyMatrix(); // upcoming disturbance effect
				oXVector.setSubMatrix(dummy, 0, index);
				index += dummy.m_iCols;
				break;
			default:
				throw new InvalidParameterException("The effect id " + effectId + " is unknown!");
			}
		}
	}


}