/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.artemis2009;

import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.stats.StatisticalUtility;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;

@SuppressWarnings("serial")
class Artemis2009DiameterIncrementInternalPredictor extends REpiceaPredictor { 

	private final List<Integer> effectList;
	
	protected Artemis2009DiameterIncrementInternalPredictor(boolean isParametersVariabilityEnabled,	boolean isOtherRandomEffectsVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isOtherRandomEffectsVariabilityEnabled, isOtherRandomEffectsVariabilityEnabled);
		effectList = new ArrayList<Integer>();
	}

	protected void setBeta(Matrix beta, Matrix omega) {
		ModelParameterEstimates estimate = new SASParameterEstimates(beta, omega);
		setParameterEstimates(estimate);
		oXVector = new Matrix(1, estimate.getMean().m_iRows);
	}
	
	protected void setEffectList(Matrix effectList) {
		for (int i = 0; i < effectList.m_iRows; i++) {
			this.effectList.add((int) effectList.getValueAt(i, 0));
		}
	}

	protected synchronized double[] predictGrowth(Artemis2009CompatibleStand stand, Artemis2009CompatibleTree tree) {
		Matrix beta = getParametersForThisRealization(stand);
		ParameterDispatcher.getInstance().constructXVector(oXVector, stand, tree, Artemis2009DiameterIncrementPredictor.ModuleName, effectList);
		double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
		double pred;
		double dVarianceUn = 0d;
		if (isRandomEffectsVariabilityEnabled) {
			double plotRandomEffect = getRandomEffectsForThisSubject(stand).getValueAt(0, 0);
			IntervalNestedInPlotDefinition intervalDefinition = getIntervalNestedInPlotDefinition(stand, stand.getDateYr());
			double stepRandomEffect = getRandomEffectsForThisSubject(intervalDefinition).getValueAt(0, 0);
			Matrix errorTerm = getResidualErrorForThisSubject(tree, ErrorTermGroup.Default);
			int index = this.getGaussianErrorTerms(tree).getDistanceIndex().indexOf(tree.getErrorTermIndex());
			double residualErrorTerm = errorTerm.getValueAt(index, 0);		// last element
			pred = xBeta + plotRandomEffect + stepRandomEffect + residualErrorTerm;
		} else {
			double plotVariance = getDefaultRandomEffects(HierarchicalLevel.PLOT).getVariance().getValueAt(0, 0);
			double stepVariance = getDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT).getVariance().getValueAt(0, 0);
			double residualVariance = getDefaultResidualError(ErrorTermGroup.Default).getVariance().getValueAt(0, 0);
			double fVarianceLog = plotVariance + stepVariance+ residualVariance;
			dVarianceUn = (Math.exp(fVarianceLog) - 1) * Math.exp(2d * xBeta + fVarianceLog); // variance on the log scale prior to the bias correction
			pred = xBeta + fVarianceLog * .5;
		}
		double[] output = new double[2];
		output[0] = Math.exp(pred) - 1d;
		output[1] = dVarianceUn;
		return output;
	}

	protected void setRandomEffect(HierarchicalLevel level, Matrix randomEffectVariance) {
		Matrix mean = new Matrix(randomEffectVariance.m_iRows, 1);
		setDefaultRandomEffects(level, new GaussianEstimate(mean, randomEffectVariance));
	}

	
	protected void setResidualErrorCovariance(double s2_tree, double correlationParameter) {
		Matrix variance = new Matrix(1,1);
		variance.setValueAt(0, 0, s2_tree);
		setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(variance, correlationParameter, StatisticalUtility.TypeMatrixR.LINEAR_LOG));
	}

	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#init()
	 */
	@Override
	protected void init() {}

}
