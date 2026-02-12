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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.math.integral.GaussHermiteQuadrature;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.climate.REpiceaClimateManager.ClimateVariableTemporalResolution;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.model.glm.LinkFunction.Type;

@SuppressWarnings("serial")
class Trillium2026MortalityInternalPredictor extends REpiceaBinaryEventPredictor<Trillium2026MortalityPlot, Trillium2026Tree> {

	private static final ClimateVariableTemporalResolution IntervalResolution = ClimateVariableTemporalResolution.IntervalAveraged;
	
	private static final int DEDLowerBound = 1960;
	private static final int DEDUpperBound = 1985;
	
	private static enum EffectID {
		Intercept,
		DBH_x,
		BAL,
		dummyHarvest,
		IDBH_xBAL,
		plantedTRUE,
		beyondMinus25,
		MeanTminJanuary,
		MeanTempJuneToAugust,
		IDBH_x2,
		TotalPrecMarchToMay,
		TotalPrecJuneToAugust,
		logDBH_x,
		IBAL2,
		DutchElmDiseaseOutbreak
	}
	
	private final List<Integer> effectList;
	private final boolean hasPlotRandomEffect;
	private final EmbeddedLinkFunction linkFunction;
	private final GaussHermiteQuadrature ghq;
	
	protected Trillium2026MortalityInternalPredictor(boolean isParametersVariabilityEnabled,
			boolean isRandomEffectsVariabilityEnabled, 
			boolean isResidualVariabilityEnabled,
			List<Double> effectList,
			List<Double> coefList,
			List<Double> vcovList,
			List<Double> ranefVar) {
		super(isParametersVariabilityEnabled, 
				ranefVar != null && isRandomEffectsVariabilityEnabled, // random effect variability cannot be enabled in the model has no random effects
				isResidualVariabilityEnabled);
		hasPlotRandomEffect = ranefVar != null;
		this.effectList = new ArrayList<Integer>();
		for (Double effect : effectList) {
			this.effectList.add(effect.intValue());
		}
		Matrix beta = new Matrix(coefList);
		SymmetricMatrix omega = new Matrix(vcovList).squareSym();
		setParameterEstimates(new ModelParameterEstimates(beta, omega));
		this.oXVector = new Matrix(1, beta.m_iRows);
		
		if (ranefVar != null) {
			SymmetricMatrix ranefVariance = new Matrix(ranefVar).squareSym();
			GaussianEstimate randomEffect = new GaussianEstimate(new Matrix(ranefVariance.m_iRows,1), ranefVariance);
			setDefaultRandomEffects(HierarchicalLevel.PLOT, randomEffect);
			linkFunction = new EmbeddedLinkFunction(Type.CLogLog, ranefVariance.getValueAt(0, 0));
		} else {
			linkFunction = new EmbeddedLinkFunction(Type.CLogLog, 0d);
		}
		linkFunction.setParameterValue(0, 0d);		// random parameter
		linkFunction.setVariableValue(0, 1d);		// variable that multiplies the random parameter
		linkFunction.setParameterValue(1, 1d);		// parameter that multiplies the xBeta
		ghq = new GaussHermiteQuadrature();
	}
	
	@Override
	public synchronized double predictEventProbability(Trillium2026MortalityPlot plot, Trillium2026Tree tree, Map<String, Object> parms) {
		Matrix beta = getParametersForThisRealization(tree); 
		double xBeta = getFixedEffectPrediction(beta, plot, tree);
		linkFunction.setVariableValue(1, xBeta);
		double prob;
		if (isRandomEffectsVariabilityEnabled) {
			linkFunction.setParameterValue(0, getRandomEffectsForThisSubject(plot).getValueAt(0, 0));
			prob = linkFunction.getValue();
		} else {
			linkFunction.setParameterValue(0, 0d);
			if (hasPlotRandomEffect) {
				prob = ghq.getIntegralApproximation(linkFunction, 
						0, 
						true);
			} else {
				prob = linkFunction.getValue();
			}
		}
		return prob;
	}

	double getFixedEffectPrediction(Matrix beta, Trillium2026MortalityPlot plot, Trillium2026Tree tree) {
		oXVector.resetMatrix();
		int index = 0;
		double dbhCm = tree.getDbhCm();
		double balM2Ha = tree.getBasalAreaLargerThanSubjectM2Ha();
		double meanTminJanuary = plot.getMeanMinimumJanuaryTemperatureCelsius(IntervalResolution);
		for (Integer effectId : effectList) {
			EffectID eff = EffectID.values()[effectId];
			switch(eff) {
			case Intercept:
				oXVector.setValueAt(0, index++, 1d);
				break;
			case DBH_x:
				oXVector.setValueAt(0, index++, dbhCm);
				break;
			case BAL:
				oXVector.setValueAt(0, index++, balM2Ha);
				break;
			case dummyHarvest:
				oXVector.setValueAt(0, index++, plot.isGoingToBeHarvested() ? 1d : 0d);
				break;
			case IDBH_xBAL:
				oXVector.setValueAt(0, index++, dbhCm * balM2Ha);
				break;
			case plantedTRUE:
				oXVector.setValueAt(0, index++, plot.isFromPlantation() ? 1d : 0d);
				break;
			case beyondMinus25:
				double beyondMinus25 = -25-meanTminJanuary;
				oXVector.setValueAt(0, index++, beyondMinus25 < 0 ? 0 : beyondMinus25);
				break;
			case MeanTminJanuary:
				oXVector.setValueAt(0, index++, meanTminJanuary);
				break;
			case MeanTempJuneToAugust:
				oXVector.setValueAt(0, index++, plot.getMeanTemperatureFromJuneToAugustCelsius(IntervalResolution));
				break;
			case IDBH_x2:
				oXVector.setValueAt(0, index++, tree.getSquaredDbhCm());
				break;
			case TotalPrecMarchToMay:
				oXVector.setValueAt(0, index++, plot.getTotalPrecipitationFromMarchToMayMm(IntervalResolution));
				break;
			case TotalPrecJuneToAugust:
				oXVector.setValueAt(0, index++, plot.getTotalPrecipitationFromJuneToAugustMm(IntervalResolution));
				break;
			case logDBH_x:
				oXVector.setValueAt(0, index++, tree.getLnDbhCm());
				break;
			case IBAL2:
				oXVector.setValueAt(0, index++, balM2Ha * balM2Ha);
				break;
			case DutchElmDiseaseOutbreak:
				double nbYearsWithDutchElmDisease = getNbYearsWithDutchElmDisease(plot.getDateYr(), plot.getGrowthStepLengthYr());
				oXVector.setValueAt(0, index++, nbYearsWithDutchElmDisease);
				break;
			}
		}
		double pred = oXVector.multiply(beta).getValueAt(0, 0);
		return pred + Math.log(plot.getGrowthStepLengthYr());
	}
		
	private double getNbYearsWithDutchElmDisease(int dateYr, double growthStepLengthYr) {
		double nextDateYr = dateYr + growthStepLengthYr;
		if (dateYr > DEDUpperBound) {
			return 0d;
		} else if (dateYr >= DEDLowerBound) {
			if (nextDateYr <= DEDUpperBound) {
				return growthStepLengthYr;
			} else if (dateYr <= DEDUpperBound && nextDateYr > DEDUpperBound) {
				return DEDUpperBound - dateYr;
			}
		} else { // then dateYr < DEDLowerBound
			if (nextDateYr > DEDUpperBound) {
				return DEDUpperBound - DEDLowerBound;
			} else if (nextDateYr >= DEDLowerBound && nextDateYr <= DEDUpperBound) {
				return nextDateYr - DEDLowerBound;
			}
		} 
		return 0d;
	}

	@Override
	protected void init() {}

}
