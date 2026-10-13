/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
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
package ontariomnrf.predictor.trillium2026.diameterincrement.gls;

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.stats.StatisticalUtility.TypeMatrixR;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;

@SuppressWarnings("serial")
final class Trillium2026DiameterIncrementInternalPredictor extends REpiceaPredictor {

	private final Trillium2026DiameterIncrementPredictor owner;
	private final Species species;
	private final List<Integer> effects;
	final boolean hasCorrelationStructure;
	final boolean hasPlotRandomEffect;
	

	Trillium2026DiameterIncrementInternalPredictor(Trillium2026DiameterIncrementPredictor owner,
			Species species,
			boolean isParametersVariabilityEnabled, 
			boolean isRandomEffectVariabilityEnabled,
			boolean isResidualVariabilityEnabled,
			final ModelParameterEstimates parmEst,
			final List<Integer> effectList,
			Double rho,
			final SymmetricMatrix randomEffectVariance,
			final SymmetricMatrix residualVariance) {
		super(isParametersVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled); // no random effect
		this.owner = owner;
		this.species = species;
		setParameterEstimates(parmEst);
		oXVector = new Matrix(1, getParameterEstimates().getMean().m_iRows);
		effects = new ArrayList<Integer>();
		effects.addAll(effectList);

		hasCorrelationStructure = rho != null;
		setDefaultResidualError(REpiceaPredictor.ErrorTermGroup.Default, 
				hasCorrelationStructure ?
					new GaussianErrorTermEstimate(residualVariance, rho, TypeMatrixR.POWER) :
						new GaussianErrorTermEstimate(residualVariance));
		hasPlotRandomEffect = randomEffectVariance != null;
		if (hasPlotRandomEffect) {
			setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(new Matrix(1,1), randomEffectVariance));
		}
	}

	@Override
	protected void init() {}

	private void setXVector(Trillium2026DiameterIncrementPlot plot, Trillium2026DiameterIncrementTree tree) {
		oXVector.resetMatrix();
		int index = 0;
		for (Integer effect : effects) {
			switch(effect) {
			case 1:  // Intercept
				oXVector.setValueAt(0, index++, 1d);
				break;
			case 2:  // BAL
				oXVector.setValueAt(0, index++, tree.getBasalAreaLargerThanSubjectM2Ha());
				break;
			case 3: // BAS
				oXVector.setValueAt(0, index++, tree.getBasalAreaSmallerThanSubjectM2Ha());
				break;
			case 4: // CMI
				oXVector.setValueAt(0, index++, plot.getMeanAnnualCMICm(owner, Trillium2026DiameterIncrementPlot.ClimateVariableResolution));
				break;
			case 5:  // DBH.x
				oXVector.setValueAt(0, index++, tree.getDbhCm());
				break;
			case 6:  // DD
				oXVector.setValueAt(0, index++, plot.getGrowingDegreeDaysCelsius(owner, Trillium2026DiameterIncrementPlot.ClimateVariableResolution));
				break;
			case 7:  // dt
				oXVector.setValueAt(0, index++, plot.getGrowthStepLengthYr());
				break;
			case 8: // dummyHarvest
				oXVector.setValueAt(0, index++, plot.isGoingToBeHarvested() ? 1 : 0);
				break;
			case 9: // G_TOT
				oXVector.setValueAt(0, index++, plot.getBasalAreaM2Ha());
				break;
			case 10: // G_TOT2
				double ba = plot.getBasalAreaM2Ha();
				oXVector.setValueAt(0, index++, ba * ba);
				break;
			case 11: // MeanTmaxJuly^2
				double meanTmaxJuly = plot.getMeanMaximumJulyTemperatureCelsius(owner, Trillium2026DiameterIncrementPlot.ClimateVariableResolution);
				oXVector.setValueAt(0, index++, meanTmaxJuly * meanTmaxJuly);
				break;
			case 12:  // TotalPrecJuneToAugust^2
				double totalPrecJuneToAugust = plot.getTotalPrecipitationFromJuneToAugustMm(owner, Trillium2026DiameterIncrementPlot.ClimateVariableResolution);
				oXVector.setValueAt(0, index++, totalPrecJuneToAugust * totalPrecJuneToAugust);
				break; 
			case 13: // log dbh
				oXVector.setValueAt(0, index++, Math.log(tree.getDbhCm()));
				break;
			case 14: // log dbh * G_TOT
				oXVector.setValueAt(0, index++, Math.log(tree.getDbhCm()) * plot.getBasalAreaM2Ha());
				break;
			case 15: // log N_TOT
				oXVector.setValueAt(0, index++, Math.log(plot.getNumberOfStemsHa()));
				break;
			case 16: // mean summer VPD
				oXVector.setValueAt(0, index++, plot.getMeanVPDFromJuneToAugustHPa(owner, Trillium2026DiameterIncrementPlot.ClimateVariableResolution));
				break;
			case 17: //	meanTempJuneAugust
				oXVector.setValueAt(0, index++, plot.getMeanTemperatureFromJuneToAugustCelsius(owner, Trillium2026DiameterIncrementPlot.ClimateVariableResolution));
				break;
			case 18: // MeanTmaxJuly
				oXVector.setValueAt(0, index++, plot.getMeanMaximumJulyTemperatureCelsius(owner, Trillium2026DiameterIncrementPlot.ClimateVariableResolution));
				break;
			case 19: // MeanTminJanuary
				oXVector.setValueAt(0, index++, plot.getMeanMinimumJanuaryTemperatureCelsius(owner, Trillium2026DiameterIncrementPlot.ClimateVariableResolution));
				break;
			case 20: // N_TOT
				oXVector.setValueAt(0, index++, plot.getNumberOfStemsHa());
				break;
			case 21:  // SMImean:
				oXVector.setValueAt(0, index++, plot.getMeanAnnualSMIPercent(owner, Trillium2026DiameterIncrementPlot.ClimateVariableResolution));
				break;
			case 22:  // TotalPrecJuneToAugust
				oXVector.setValueAt(0, index++, plot.getTotalPrecipitationFromJuneToAugustMm(owner, Trillium2026DiameterIncrementPlot.ClimateVariableResolution));
				break; 
			case 23:  // totalPrecMarchToMay
				oXVector.setValueAt(0, index++, plot.getTotalPrecipitationFromMarchToMayMm(owner, Trillium2026DiameterIncrementPlot.ClimateVariableResolution));
				break;
			default:
				throw new UnsupportedOperationException("This effectID has not been implemented yet: " + effect);
			}
		}
	}
	
	synchronized double predictDiameterIncrementCm(Trillium2026DiameterIncrementPlot plot, Trillium2026DiameterIncrementTree tree) {
		Matrix beta = getParametersForThisRealization(plot);
		setXVector(plot, tree);
		double pred = oXVector.multiply(beta).getValueAt(0, 0);
		
		if (hasPlotRandomEffect && isRandomEffectsVariabilityEnabled) {
			pred += getRandomEffectsForThisSubject(plot).getValueAt(0, 0);
		}
		
		if (isResidualVariabilityEnabled) {
			double residualErrorTerm;
			if (hasCorrelationStructure) {
				Matrix errorTerm = getResidualErrorForThisSubject(tree, ErrorTermGroup.Default);
				int index = this.getGaussianErrorTerms(tree).getDistanceIndex().indexOf(tree.getErrorTermIndex());
				residualErrorTerm = errorTerm.getValueAt(index, 0);		// last element
			} else {
				residualErrorTerm = getDefaultResidualError(ErrorTermGroup.Default).getRandomDeviate().getValueAt(0,0);
			}
			pred += residualErrorTerm;
		} 
		
		if (owner.doBackTransformation) {
			double variance = 0d;
			if (hasPlotRandomEffect && !isRandomEffectsVariabilityEnabled) {
				variance += getDefaultRandomEffects(HierarchicalLevel.PLOT).getVariance().getValueAt(0, 0);
			}
			if (!isResidualVariabilityEnabled) {
				variance += getDefaultResidualError(ErrorTermGroup.Default).getVariance().getValueAt(0,0);
			}
			pred = Math.sinh(pred);
			if (variance > 0) {
				pred *= Math.exp(0.5 * variance);
			} 
		}
		
		if (Trillium2026DiameterIncrementPredictor.BoundEnabled) {
			double stepLengthYr = plot.getGrowthStepLengthYr();
			if (pred > Trillium2026DiameterIncrementPredictor.MAXIMUM_PERIOD_ANNUAL_INCREMENT_CM * stepLengthYr) { //  a cap, 1.8 is the 0.9995 percentile of observed periodical diameter increment 
				if (Trillium2026DiameterIncrementPredictor.Verbose) {
					System.out.println(getClass().getSimpleName() + "-" + this.species.name() + " hits maximum diameter increment with " + pred + " over " + stepLengthYr + " yrs.");
				}
				pred = Trillium2026DiameterIncrementPredictor.MAXIMUM_PERIOD_ANNUAL_INCREMENT_CM * stepLengthYr;
			}
			
//			if (pred < Trillium2026DiameterIncrementPredictor.MINIMUM_PERIOD_ANNUAL_INCREMENT_CM * stepLengthYr) { //  a cap, -1.4 is the 0.0005 percentile of observed periodical diameter increment 
//				if (Trillium2026DiameterIncrementPredictor.Verbose) {
//					System.out.println(getClass().getSimpleName() + "-" + this.species.name() + " hits minimum diameter increment with " + pred + " over " + stepLengthYr + " yrs.");
//				}
//				pred = Trillium2026DiameterIncrementPredictor.MINIMUM_PERIOD_ANNUAL_INCREMENT_CM * stepLengthYr;
//			}
			
			if (pred < 0) { //  a cap, -1.4 is the 0.0005 percentile of observed periodical diameter increment 
				if (Trillium2026DiameterIncrementPredictor.Verbose) {
					System.out.println(getClass().getSimpleName() + "-" + this.species.name() + " hits minimum diameter increment with " + pred + " over " + stepLengthYr + " yrs.");
				}
				pred = 0;
			}
		}
		return pred; 
	}

	
	/*
	 * For testing only.
	 */
	Matrix getResidualErrorForThisTree(MonteCarloSimulationCompliantObject tree) {
		return getResidualErrorForThisSubject(tree, ErrorTermGroup.Default);
	}

}
