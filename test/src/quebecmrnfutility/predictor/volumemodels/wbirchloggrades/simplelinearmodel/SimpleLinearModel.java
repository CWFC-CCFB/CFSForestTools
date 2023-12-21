/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package quebecmrnfutility.predictor.volumemodels.wbirchloggrades.simplelinearmodel;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.distributions.ChiSquaredDistribution;
import repicea.stats.estimates.GaussianErrorTermEstimate;

@SuppressWarnings("serial")
class SimpleLinearModel extends REpiceaPredictor {

	private ChiSquaredDistribution distributionForVCovRandomDeviates;
	
	protected static boolean R2_95Version = false;
	
	protected SimpleLinearModel(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
		init();
	}

	@Override
	protected void init() {
		Matrix beta = new Matrix(2,1);
		beta.setValueAt(0, 0, 4d);
		beta.setValueAt(1, 0, 3d);
		SymmetricMatrix omega = new SymmetricMatrix(2);
		omega.setValueAt(0, 0, 0.025);
		omega.setValueAt(1, 1, 0.0005);
		omega.setValueAt(0, 1, Math.sqrt(omega.getValueAt(0, 0) * omega.getValueAt(1, 1)) * .1);
		setParameterEstimates(new ModelParameterEstimates(beta, omega));
		SymmetricMatrix residualVariance = new SymmetricMatrix(1);
		if (R2_95Version) {
			residualVariance.setValueAt(0, 0, .284);			// to ensure a R2 of 0.95
		} else {
			residualVariance.setValueAt(0, 0, 2d);
		}
		setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(residualVariance));
		oXVector = new Matrix(1, beta.m_iRows);
	}
	
	protected double predictY(SamplePlot plot) {
		Matrix currentBeta = getParametersForThisRealization(plot);
		oXVector.resetMatrix();
		oXVector.setValueAt(0, 0, 1d);
		oXVector.setValueAt(0, 1, plot.getX());
		double pred = oXVector.multiply(currentBeta).getValueAt(0, 0);
		pred += getResidualError().getValueAt(0, 0) * Math.sqrt(plot.getX());
		return pred;
	}

	
	/*
	 * For manuscript purposes.
	 */
	void replaceModelParameters() {
		int degreesOfFreedom = 98;		// assumption of 100 observations - 2 parameters
		Matrix newMean = getParameterEstimates().getRandomDeviate();
		SymmetricMatrix variance = getParameterEstimates().getVariance();
		if (distributionForVCovRandomDeviates == null) {
			distributionForVCovRandomDeviates = new ChiSquaredDistribution(degreesOfFreedom, variance);
		}
		SymmetricMatrix newVariance = distributionForVCovRandomDeviates.getRandomRealization();
		setParameterEstimates(new ModelParameterEstimates(newMean, newVariance));
		
		SymmetricMatrix residualVariance = this.getDefaultResidualError(ErrorTermGroup.Default).getVariance();
		ChiSquaredDistribution residualVarianceDistribution = new ChiSquaredDistribution(degreesOfFreedom, residualVariance);
		SymmetricMatrix newResidualVariance = residualVarianceDistribution.getRandomRealization();
		setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(newResidualVariance));
	}
	
	
	void replaceModelParameters(PlotList sample) {
		int degreesOfFreedom = sample.size() - 2;
		Matrix matX = new Matrix(sample.size(), 2);
		Matrix matY = new Matrix(sample.size(), 1);
		Matrix matW = new Matrix(sample.size(), sample.size());
		for (int i = 0; i < sample.size(); i++) {
			SamplePlot plot = sample.get(i);
			matX.setValueAt(i, 0, 1d);
			matX.setValueAt(i, 1, plot.getX());
			matY.setSubMatrix(plot.getY(), i, 0);
			matW.setValueAt(i, i, plot.getX());
		}
		Matrix invW = matW.getInverseMatrix();
		Matrix invXWX = matX.transpose().multiply(invW).multiply(matX).getInverseMatrix();
		Matrix newMean = invXWX.multiply(matX.transpose()).multiply(invW).multiply(matY);
		Matrix res = matY.subtract(matX.multiply(newMean));
		Matrix newResidualVariance = res.transpose().multiply(invW).multiply(res).scalarMultiply(1d/(degreesOfFreedom));
		Matrix newVariance = invXWX.scalarMultiply(newResidualVariance.getValueAt(0, 0));
		
		setParameterEstimates(new ModelParameterEstimates(newMean, SymmetricMatrix.convertToSymmetricIfPossible(newVariance)));
		setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(SymmetricMatrix.convertToSymmetricIfPossible(newResidualVariance)));
	}


}
