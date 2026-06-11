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
package ontariomnrf.predictor.trillium2026.mortality;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ontariomnrf.predictor.trillium2026.Trillium2026Tree;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.math.integral.GaussHermiteQuadrature;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.model.glm.LinkFunction.Type;

@SuppressWarnings("serial")
class Trillium2026MortalityInternalPredictor extends REpiceaBinaryEventPredictor<Trillium2026MortalityPlot, Trillium2026Tree> {

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
		N_TOT,
		MeanTempJuneToAugust,
		MeanTair,
		IDBH_x2,
		TotalPrcp,
		IBAL2,
		TotalPrecJuneToAugust,
		TotalPrecMarchToMay,
		DutchElmDiseaseOutbreak
	}
	
	private final List<Integer> effectList;
	private final boolean hasPlotRandomEffect;
	private final EmbeddedLinkFunction linkFunction;
	private final GaussHermiteQuadrature ghq;
	private final Trillium2026MortalityPredictor owner;
	
	protected Trillium2026MortalityInternalPredictor(boolean isParametersVariabilityEnabled,
			boolean isRandomEffectsVariabilityEnabled, 
			boolean isResidualVariabilityEnabled,
			List<Double> effectList,
			List<Double> coefList,
			List<Double> vcovList,
			List<Double> ranefVar,
			Trillium2026MortalityPredictor owner) {
		super(isParametersVariabilityEnabled, 
				ranefVar != null && isRandomEffectsVariabilityEnabled, // random effect variability cannot be enabled in the model has no random effects
				isResidualVariabilityEnabled);
		this.owner = owner;
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
			setDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_CLUSTER, randomEffect);
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
			IntervalNestedInClusterDefinition intervalInClusterDefinition = getIntervalNestedInClusterDefinition(plot, plot.getDateYr());
			double intervalRandomEffect = getRandomEffectsForThisSubject(intervalInClusterDefinition).getValueAt(0, 0);
			linkFunction.setParameterValue(0, intervalRandomEffect);
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
//		double dbhCm = tree.getDbhCm();
//		double balM2Ha = tree.getBasalAreaLargerThanSubjectM2Ha();
//		double meanTminJanuary = plot.getMeanMinimumJanuaryTemperatureCelsius(owner, Trillium2026MortalityPlot.ClimateVariableResolution);
		double meanTminJanuary;
		for (Integer effectId : effectList) {
			EffectID eff = EffectID.values()[effectId];
			switch(eff) {
			case Intercept: // 0
				oXVector.setValueAt(0, index++, 1d);
				break;
			case DBH_x: 	// 1
				oXVector.setValueAt(0, index++, tree.getDbhCm());
				break;
			case BAL:		// 2
				oXVector.setValueAt(0, index++, tree.getBasalAreaLargerThanSubjectM2Ha());
				break;
			case dummyHarvest:	// 3
				oXVector.setValueAt(0, index++, plot.isGoingToBeHarvested() ? 1d : 0d);
				break;
			case IDBH_xBAL:		// 4
				oXVector.setValueAt(0, index++, tree.getDbhCm() * tree.getBasalAreaLargerThanSubjectM2Ha());
				break;
			case plantedTRUE:	// 5
				oXVector.setValueAt(0, index++, plot.isFromPlantation() ? 1d : 0d);
				break;
			case beyondMinus25:	// 6
				meanTminJanuary = plot.getMeanMinimumJanuaryTemperatureCelsius(owner, Trillium2026MortalityPlot.ClimateVariableResolution);
				
				double beyondMinus25 = -25-meanTminJanuary;
				oXVector.setValueAt(0, index++, beyondMinus25 < 0 ? 0 : beyondMinus25);
				break;
			case MeanTminJanuary:	// 7
				meanTminJanuary = plot.getMeanMinimumJanuaryTemperatureCelsius(owner, Trillium2026MortalityPlot.ClimateVariableResolution);
				oXVector.setValueAt(0, index++, meanTminJanuary);
				break;
			case N_TOT:	// 8
				oXVector.setValueAt(0, index++, plot.getNumberOfStemsHa());
				break;
			case MeanTempJuneToAugust: // 9
				oXVector.setValueAt(0, index++, plot.getMeanTemperatureFromJuneToAugustCelsius(owner, Trillium2026MortalityPlot.ClimateVariableResolution));
				break;
			case MeanTair: // 10
				oXVector.setValueAt(0, index++, plot.getMeanAnnualTemperatureCelsius(owner, Trillium2026MortalityPlot.ClimateVariableResolution));
				break;
			case IDBH_x2: // 11
				double dbhCm = tree.getDbhCm();
				oXVector.setValueAt(0, index++, dbhCm * dbhCm);
				break;
			case TotalPrcp: // 12
				oXVector.setValueAt(0, index++, plot.getTotalAnnualPrecipitationMm(owner, Trillium2026MortalityPlot.ClimateVariableResolution));
				break;
			case IBAL2: // 13
				double BAL = tree.getBasalAreaLargerThanSubjectM2Ha();
				oXVector.setValueAt(0, index++, BAL * BAL);
				break;
			case TotalPrecJuneToAugust: // 14
				oXVector.setValueAt(0, index++, plot.getTotalPrecipitationFromJuneToAugustMm(owner, Trillium2026MortalityPlot.ClimateVariableResolution));
				break;
			case TotalPrecMarchToMay: 	// 15
				oXVector.setValueAt(0, index++, plot.getTotalPrecipitationFromMarchToMayMm(owner, Trillium2026MortalityPlot.ClimateVariableResolution));
				break;
			case DutchElmDiseaseOutbreak: // 16
				double nbYearsWithDutchElmDisease = getNbYearsWithDutchElmDisease(plot.getDateYr(), plot.getGrowthStepLengthYr());
				oXVector.setValueAt(0, index++, nbYearsWithDutchElmDisease);
				break;
			default:
				throw new UnsupportedOperationException("This effect is not supported: " + eff.name());
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
