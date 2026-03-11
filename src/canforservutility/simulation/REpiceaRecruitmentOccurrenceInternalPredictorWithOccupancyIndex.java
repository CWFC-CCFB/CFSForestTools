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

import repicea.math.AbstractMathematicalFunction;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.math.integral.AbstractGaussQuadrature.NumberOfPoints;
import repicea.math.integral.GaussHermiteQuadrature;
import repicea.math.integral.GaussHermiteQuadrature.GaussHermiteQuadratureCompatibleFunction;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.model.glm.LinkFunction;

/**
 * An abstract class to support the occurrence part of recruitment models using 
 * occupancy
 * @param <S> a Plot type that implements the RecruitmentPlotWithOccupancy interface
 * @author Mathieu Fortin - February 2026
 */
@SuppressWarnings("serial")
public abstract class REpiceaRecruitmentOccurrenceInternalPredictorWithOccupancyIndex<S extends RecruitmentPlotWithOccupancy,T>
								extends REpiceaBinaryEventPredictor<S, T>{

	/**
	 * A nested class for Gauss-Hermite integration in case random variability around the occupancy index is
	 * disabled.
	 * @author Mathieu Fortin - February 2026
	 */
	protected class InternalMathFunction extends LinkFunction implements GaussHermiteQuadratureCompatibleFunction<Double> {

		private double standardDeviation;
		
		public InternalMathFunction() {
			super(Type.CLogLog, new InternalStatisticalExpression());
		}

		@SuppressWarnings("unchecked")
		public void setMembers(Matrix beta, S plot, double meanOccIndex, double varOccIndex) {
			((InternalStatisticalExpression) getOriginalFunction()).setMembers(beta, plot, meanOccIndex);
			this.standardDeviation = Math.sqrt(varOccIndex);
		}
		
		@Override
		public double convertFromGaussToOriginal(double x, double mu, int covarianceIndexI, int covarianceIndexJ) {
			return mu + Math.sqrt(2d) * x * standardDeviation;
		}

	}
	
	class InternalStatisticalExpression extends AbstractMathematicalFunction {

		private Matrix beta;
		private S plot;
		private double meanOccIndex;
		
		InternalStatisticalExpression() {}
		
		void setMembers(Matrix beta, S plot, double meanOccIndex) {
			this.beta = beta;
			this.plot = plot;
			this.meanOccIndex = meanOccIndex;
		}
		
		@Override
		public Double getValue() {
			double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
			xBeta += addOffsetIfNeeded(plot);
//			if (offsetEnabled) {
//				xBeta += Math.log(plot.getGrowthStepLengthYr());
//			}
			return xBeta;
		}
		
		@Override
		public void setVariableValue(int variableIndex, double variableValue) {
			setOccupancyInXVector(plot, REpiceaRecruitmentOccurrenceInternalPredictorWithOccupancyIndex.this.species, variableValue);
		}		

		@Override
		public double getVariableValue(int variableIndex) {return meanOccIndex;}
		
		@Override
		public Matrix getGradient() {return null;}

		@Override
		public SymmetricMatrix getHessian() {return null;}
	}

	
	protected final Enum<?> species;
	protected final List<Integer> effectList;
	protected final boolean offsetEnabled;
	protected final List<Integer> occupancyIndexVarIndices; // effect Ids that include the occupancy index

	protected final GaussHermiteQuadrature ghq;
	protected final InternalMathFunction imf;

	
	protected REpiceaRecruitmentOccurrenceInternalPredictorWithOccupancyIndex(boolean isParametersVariabilityEnabled,
			boolean isRandomEffectsVariabilityEnabled, 
			boolean isResidualVariabilityEnabled,
			Enum<?> species,
			boolean offsetEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		this.species = species;
		this.offsetEnabled = offsetEnabled;
		effectList = new ArrayList<Integer>();
		occupancyIndexVarIndices = new ArrayList<Integer>();
		imf = new InternalMathFunction();
		ghq = new GaussHermiteQuadrature(NumberOfPoints.N15);
		
	}

	protected void setOccupancyInXVector(S plot, Enum<?> species, double occupancyIndexValue) {
		for (int effectId : occupancyIndexVarIndices) {
			setValueInXVector(effectId, plot, species, occupancyIndexValue); 
		}
	}

	
	protected synchronized double calculateEventProbability(S plot, Enum<?> species) {
		Matrix beta = getParametersForThisRealization(plot);
		constructXVector(plot, species);
		if (isModelUsingOccupancyIndex()) {
			Object occupancy = plot.getOccupancyForThisSpecies(species);
			if (occupancy instanceof GaussianEstimate) {
				GaussianEstimate estimate = (GaussianEstimate) occupancy;
				double meanOccIndex = estimate.getMean().getValueAt(0, 0);
				double varOccIndex = estimate.getVariance().getValueAt(0, 0);
				if (varOccIndex == 0d) { // there is no variability
					setOccupancyInXVector(plot, species, meanOccIndex);
					return getProb(beta, plot);
				} else {
					imf.setMembers(beta, plot, meanOccIndex, varOccIndex);
					double prob = ghq.getIntegralApproximation(imf, 0, false);
					return prob;
				}
			} else if (occupancy instanceof Double) {
				double occupancyIndex10kmRandomDeviate = (Double) occupancy;
				setOccupancyInXVector(plot, species, occupancyIndex10kmRandomDeviate);
				return getProb(beta, plot);
			} else {
				throw new UnsupportedOperationException("Occupance should be a GaussianEstimate instance or a double, but was :" + occupancy.getClass().getName());
			}
		} else { // not using occupancy index
			return getProb(beta, plot);
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

	protected abstract void setValueInXVector(int effectId, S plot, Enum<?> species2, double d);

	protected abstract double getProb(Matrix beta, S plot);
	
	protected abstract double addOffsetIfNeeded(S plot);

}
