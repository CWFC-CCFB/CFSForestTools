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
package ontariomnrf.predictor.trillium2026.recruitment;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import ontariomnrf.predictor.trillium2026.diameterincrement.Trillium2026DiameterIncrementPredictor;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("serial")
class Trillium2026RecruitDiameterInternalPredictor extends REpiceaPredictor {
	
	protected static boolean EnableCutPoint = true;
	
	
	private final Trillium2026RecruitDiameterPredictor owner;
	private final Species species;
	private final List<Integer> effectList;
	protected final double alpha; // as produced by R
//	protected final double invTheta; //
	
	protected Trillium2026RecruitDiameterInternalPredictor(Trillium2026RecruitDiameterPredictor owner,
			Species sp,
			boolean isParametersVariabilityEnabled, 
			boolean isResidualVariabilityEnabled, 
			double dispersion,
			Matrix beta,
			SymmetricMatrix omega,
			Matrix effectMat) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
		this.owner = owner;
		this.species = sp;
		ModelParameterEstimates estimate = new ModelParameterEstimates(beta, omega);
		setParameterEstimates(estimate);
		oXVector = new Matrix(1, estimate.getMean().m_iRows);
		effectList = new ArrayList<Integer>();
		for (int i = 0; i < effectMat.m_iRows; i++) {
			int effectId = (int) effectMat.getValueAt(i, 0);
			effectList.add(effectId);
		}
		this.alpha =  1d / dispersion;
//		this.invTheta = 1d/this.theta;
	}

	@Override
	protected void init() {}

	public synchronized double predictRecruitDiameterCm(Trillium2026RecruitmentPlot plot) {
		Matrix beta = getParametersForThisRealization(plot);
		constructXVector(plot);
		double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
		double mu = Math.exp(xBeta);
		if (this.isResidualVariabilityEnabled) {
			double theta = mu / alpha;
//			double variance = alpha * theta * theta;
//			MonteCarloEstimate estimate = new MonteCarloEstimate();
//			for (int i = 0; i < 1000000; i++) {
//				estimate.addRealization(new Matrix(1,
//						1,
//						StatisticalUtility.getRandom().nextGamma(alpha, theta),
//						0));
//			}
//			double mean = estimate.getMean().getValueAt(0,0);
//			double varianceEmpirical = estimate.getVariance().getValueAt(0, 0);
			mu = StatisticalUtility.getRandom().nextGamma(alpha, theta);
		}
		double muCm = mu *.1;
		int growthStepLengthYr = plot.getGrowthStepLengthYr();
		if (muCm > Trillium2026DiameterIncrementPredictor.MAXIMUM_PERIOD_ANNUAL_INCREMENT_CM * growthStepLengthYr && EnableCutPoint) {
			System.out.println(getClass().getSimpleName() + "-" + this.species.name() + " hits maximum diameter for recruits " + (muCm + 9.09) + " cm over " + growthStepLengthYr + " yrs.");
			muCm = Trillium2026DiameterIncrementPredictor.MAXIMUM_PERIOD_ANNUAL_INCREMENT_CM * growthStepLengthYr;
		}
		return muCm + 9.09;
	}

	/* 
	 * For test purpose.
	 */
	double getVariance(Trillium2026RecruitmentPlot plot) {
		Matrix beta = getParametersForThisRealization(plot);
		constructXVector(plot);
		double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
		double mu = Math.exp(xBeta);
		double theta = mu / alpha;
		double variance = alpha * theta * theta;
		return variance * 0.01; // 0.01 to convert from mm to cm
	}
	
	protected void constructXVector(Trillium2026RecruitmentPlot plot) {
		oXVector.resetMatrix();
		for (int effectId : effectList) {
			setValueInXVector(effectId, plot);
		}
	}

	
	protected void setValueInXVector(int effectId, Trillium2026RecruitmentPlot plot) {
		int index = effectList.indexOf(effectId);
		if (index == -1) {
			throw new InvalidParameterException("The effect id " + effectId + " is not part of this model!");
		}
		switch(effectId) {
		case 1:	// intercept
			oXVector.setValueAt(0, index, 1d);
			break;
		case 2: // DD
			oXVector.setValueAt(0, index, plot.getGrowingDegreeDaysCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 3: // dt
			oXVector.setValueAt(0, index, plot.getGrowthStepLengthYr());
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
		case 13: // meanTempJuneToAugust
			oXVector.setValueAt(0, index, plot.getMeanTemperatureFromJuneToAugustCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 14: // MeanTminJanuary
			oXVector.setValueAt(0, index, plot.getMeanMinimumJanuaryTemperatureCelsius(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 15: // TotalPrecFromJuneToAugust
			oXVector.setValueAt(0, index, plot.getTotalPrecipitationFromJuneToAugustMm(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		case 16: // TotalPrecFromMarchToMay
			oXVector.setValueAt(0, index, plot.getTotalPrecipitationFromMarchToMayMm(owner, Trillium2026RecruitmentPlot.ClimateVariableResolution));
			break;
		default:
			throw new InvalidParameterException("The effect id " + effectId + " is unknown!");
		}
	}
	
}