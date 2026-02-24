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
		ghq = new GaussHermiteQuadrature(NumberOfPoints.N5);
		
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
