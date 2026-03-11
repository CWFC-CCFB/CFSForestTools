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
package canforservutility.simulation;

import java.util.ArrayList;
import java.util.List;

import repicea.math.AbstractMathematicalFunctionWrapper;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.math.integral.AbstractGaussQuadrature.NumberOfPoints;
import repicea.math.integral.GaussHermiteQuadrature;
import repicea.math.integral.GaussHermiteQuadrature.GaussHermiteQuadratureCompatibleFunction;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.LinearStatisticalExpression;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;

/**
 * An abstract class to support the abundance part of recruitment models using 
 * occupancy
 * @param <S> a Plot type that implements the RecruitmentPlotWithOccupancy interface
 * @author Mathieu Fortin - February 2026
 */
@SuppressWarnings("serial")
public abstract class REpiceaRecruitmentNumberInternalPredictorWithOccupancyIndex<S extends RecruitmentPlotWithOccupancy>
								extends REpiceaPredictor {

	/**
	 * A nested class for Gauss-Hermite quadrature in case the random variability around occupancy index is
	 * disabled.
	 * @author Mathieu Fortin - June 2023
	 */
	class GaussHermiteImpl extends AbstractMathematicalFunctionWrapper implements GaussHermiteQuadratureCompatibleFunction<Double> {
		
		private final double c;
		
		GaussHermiteImpl(Matrix xVector, Matrix beta, double sigma2) {
			super(new LinkFunction(Type.Log, new LinearStatisticalExpression()));
			getOriginalFunction().setVariables(xVector);
			getOriginalFunction().setParameters(beta);
			this.c = Math.sqrt(2 * sigma2);
		}

		@Override
		public double convertFromGaussToOriginal(double x, double mu, int covarianceIndexI, int covarianceIndexJ) {
			return c*x + mu;
		}

		@Override
		public Double getValue() {
			double mu = getOriginalFunction().getValue();
			return mu;
		}

		@Override
		public Matrix getGradient() {return null;}

		@Override
		public SymmetricMatrix getHessian() {return null;}
	}

	
	protected final Enum<?> species;
	protected final List<Integer> effectList;
	protected final List<Integer> occupancyIndexVarIndices; // effect Ids that include the occupancy index
	protected final GaussHermiteQuadrature ghq;

	protected REpiceaRecruitmentNumberInternalPredictorWithOccupancyIndex(boolean isParametersVariabilityEnabled,
			boolean isRandomEffectsVariabilityEnabled, 
			boolean isResidualVariabilityEnabled,
			Enum<?> species) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		this.species = species;
		effectList = new ArrayList<Integer>();
		occupancyIndexVarIndices = new ArrayList<Integer>();
		ghq = new GaussHermiteQuadrature(NumberOfPoints.N15);
	}

	
	
	public synchronized double predictNumberOfRecruits(S plot, Enum<?> species) {
		Matrix beta = getParametersForThisRealization(plot);
		constructXVector(plot, species);
		if (isModelUsingOccupancyIndex()) {
			Object occupancy = plot.getOccupancyForThisSpecies(species);
			if (occupancy instanceof GaussianEstimate) {
				GaussianEstimate estimate = (GaussianEstimate) occupancy;
				double meanOccIndex = estimate.getMean().getValueAt(0, 0);
				double varOccIndex = estimate.getVariance().getValueAt(0, 0);
				// TODO MF20260309 the occupancy index should return a deviate
				setOccupancyInXVector(plot, species, meanOccIndex); // we set the variable to its mean before performing the quadrature
				GaussHermiteImpl ghi = new GaussHermiteImpl(oXVector, beta, varOccIndex); // TODO MF20260224 this should be a member of the class
				double ghqApproximation = ghq.getIntegralApproximation(ghi, effectList.indexOf(occupancyIndexVarIndices.get(0)), false);
				return getNumber(ghqApproximation, false);
			} else if (occupancy instanceof Double) {
				double occupancyIndex25kmRandomDeviate = (Double) occupancy;
				setOccupancyInXVector(plot, species, occupancyIndex25kmRandomDeviate);
				return getNumber(oXVector.multiply(beta).getValueAt(0, 0), true);
			} else {
				throw new UnsupportedOperationException("Occupance should be a GaussianEstimate instance or a double, but was :" + occupancy.getClass().getName());
			}
		} else { // not using occupancy index
			return getNumber(oXVector.multiply(beta).getValueAt(0, 0), true);
		}
	}


	protected void setOccupancyInXVector(S plot, Enum<?> species, double occupancyIndexValue) {
		for (int effectId : occupancyIndexVarIndices) {
			setValueInXVector(effectId, plot, species, occupancyIndexValue); 
		}
	}
	
	protected void constructXVector(S plot, Enum<?> species) {
		oXVector.resetMatrix();
		
		List<Integer> effectListWithoutOccIndex = new ArrayList<Integer>();
		effectListWithoutOccIndex.addAll(effectList);
		effectListWithoutOccIndex.removeAll(occupancyIndexVarIndices);
		for (int effectId : effectListWithoutOccIndex) {
			setValueInXVector(effectId, plot, species, 0d); // occupancy index set to 0 for now
		}
	}

	public boolean isModelUsingOccupancyIndex() {return !occupancyIndexVarIndices.isEmpty();}

	protected abstract void setValueInXVector(int effectId, S plot, Enum<?> species, double d);

	protected abstract double getNumber(double mu, boolean onTransformedScale);

}
