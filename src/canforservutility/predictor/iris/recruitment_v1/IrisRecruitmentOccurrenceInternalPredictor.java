/*
 * This file is part of the cfsforesttools library.
 *
 * Copyright (C) 2020-2023 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package canforservutility.predictor.iris.recruitment_v1;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canforservutility.predictor.iris.recruitment_v1.IrisCompatiblePlot.DisturbanceType;
import canforservutility.predictor.iris.recruitment_v1.IrisCompatiblePlot.SoilDepth;
import canforservutility.predictor.iris.recruitment_v1.IrisCompatiblePlot.SoilTexture;
import canforservutility.predictor.iris.recruitment_v1.IrisCompatibleTree.IrisSpecies;
import repicea.math.AbstractMathematicalFunction;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider.DrainageGroup;
import repicea.stats.distributions.utility.GaussianUtility;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.integral.AbstractGaussQuadrature.NumberOfPoints;
import repicea.stats.integral.GaussLegendreQuadrature;
import repicea.stats.integral.TrapezoidalRule;
import repicea.stats.model.glm.LinkFunction;

@SuppressWarnings("serial")
class IrisRecruitmentOccurrenceInternalPredictor extends REpiceaBinaryEventPredictor<IrisCompatiblePlot, IrisCompatibleTree> {

	
	/**
	 * A nested class for Trapezoidal integration in case random variability around the occupancy index is
	 * disabled.
	 * @author Mathieu Fortin - June 2023
	 */
	class InternalMathFunction extends LinkFunction {

		final Matrix xVector;
		final int indexVar;
		final double meanOccIndex;
		final double varOccIndex;
		
		InternalMathFunction(Matrix xVector, Matrix beta, IrisCompatiblePlot plot, int indexVar, double meanOccIndex, double varOccIndex) {
			super(Type.CLogLog, new InternalStatisticalExpression(xVector, beta, plot, meanOccIndex));
			this.xVector = xVector;
			this.indexVar = indexVar;
			this.meanOccIndex = meanOccIndex;
			this.varOccIndex = varOccIndex;
		}

		@Override
		public Double getValue() {
			double prob = super.getValue();
			double currentOccIndex = xVector.getValueAt(0, indexVar);
			double density = GaussianUtility.getProbabilityDensity(currentOccIndex, meanOccIndex, varOccIndex);
			return prob * density;
		}
	}
	
	class InternalStatisticalExpression extends AbstractMathematicalFunction {

		final Matrix xVector;
		final Matrix beta;
		final IrisCompatiblePlot plot;
		final double meanOccIndex;
		
		InternalStatisticalExpression(Matrix xVector, Matrix beta, IrisCompatiblePlot plot, double meanOccIndex) {
			this.xVector = xVector;
			this.beta = beta;
			this.plot = plot;
			this.meanOccIndex = meanOccIndex;
		}
		
		@Override
		public Double getValue() {
			double xBeta = xVector.multiply(beta).getValueAt(0, 0);
			if (IrisRecruitmentOccurrenceInternalPredictor.this.offsetEnabled) {
				xBeta += Math.log(plot.getGrowthStepLengthYr());
			}
			return xBeta;
		}
		
		@Override
		public void setVariableValue(int variableIndex, double variableValue) {
			IrisRecruitmentOccurrenceInternalPredictor.this.setOccupancyInXVector(plot, IrisRecruitmentOccurrenceInternalPredictor.this.species, variableValue);
		}		

		@Override
		public double getVariableValue(int variableIndex) {return meanOccIndex;}
		
		@Override
		public Matrix getGradient() {return null;}

		@Override
		public SymmetricMatrix getHessian() {return null;}
		
	}


	private final IrisRecruitmentOccurrencePredictor owner;
	private final List<Integer> effectList;
	private final boolean offsetEnabled;
	private final Map<String, Map<Integer, Map<Integer, GaussianEstimate>>> occupancyIndices; // 1st key plot id, 2nd key realization id, 3rd key dateYr
	private final Map<String, Map<Integer, Map<Integer, Double>>> occupancyIndicesDeviates; // 1st key plot id, 2nd key realization id, 3rd key dateYr
	private final List<Integer> occupancyIndexVarIndices; // effect Ids that include the occupancy index
//	private final TrapezoidalRule tr;
	private final IrisSpecies species;
	
	protected IrisRecruitmentOccurrenceInternalPredictor(IrisRecruitmentOccurrencePredictor owner,
			IrisSpecies species,
			boolean isParametersVariabilityEnabled, 
			boolean isOccupancyIndexVariabilityEnabled, 
			boolean isResidualVariabilityEnabled, 
			boolean offsetEnabled, 
			Matrix beta,
			SymmetricMatrix omega,
			Matrix effectMat) {
		super(isParametersVariabilityEnabled, isOccupancyIndexVariabilityEnabled, isResidualVariabilityEnabled);	// isOccupancyIndexVariabilityEnabled is stored as an interval random effect 
		this.owner = owner;
		this.species = species;
		this.offsetEnabled = offsetEnabled;
		
		ModelParameterEstimates estimate = new ModelParameterEstimates(beta, omega);
		setParameterEstimates(estimate);
		oXVector = new Matrix(1, estimate.getMean().m_iRows);
		
		effectList = new ArrayList<Integer>();
		occupancyIndexVarIndices = new ArrayList<Integer>();
		for (int i = 0; i < effectMat.m_iRows; i++) {
			int effectId = (int) effectMat.getValueAt(i, 0);
			effectList.add(effectId);
			if (IrisRecruitmentOccurrencePredictor.OccupancyIndexEffects.contains(effectId)) {
				occupancyIndexVarIndices.add(effectId);
			}
		}
		occupancyIndices = new HashMap<String, Map<Integer, Map<Integer, GaussianEstimate>>>();
		occupancyIndicesDeviates = new HashMap<String, Map<Integer, Map<Integer, Double>>>();
	}

	@Override
	protected void init() {}
	
	private void setOccupancyInXVector(IrisCompatiblePlot plot, IrisSpecies species, double occupancyIndex10km) {
		for (int effectId : occupancyIndexVarIndices) {
			setValueInXVector(effectId, plot, species, occupancyIndex10km); 
		}
	}

	private double getProb(Matrix beta, IrisCompatiblePlot plot) {
		double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
		if (offsetEnabled) {
			xBeta += Math.log(plot.getGrowthStepLengthYr());
		}
		double recruitmentProbability = 1d - Math.exp(-Math.exp(xBeta));
		return recruitmentProbability;
	}
	
	@Override
	public double predictEventProbability(IrisCompatiblePlot plot, IrisCompatibleTree tree, Map<String, Object> parms) {
		return calculateEventProbability(plot, tree.getSpecies());
	}

	protected synchronized double calculateEventProbability(IrisCompatiblePlot plot, IrisSpecies species) {
		Matrix beta = getParametersForThisRealization(plot);
		constructXVector(plot, species);
		if (isUsingOccupancyIndex()) {
			if (plot instanceof IrisCompatibleTestPlotImpl) { // occupancy is assumed to be known
				double occupancyIndex10kmRandomDeviate = ((IrisCompatibleTestPlotImpl) plot).getOccupancyIndex10km(species);
				setOccupancyInXVector(plot, species, occupancyIndex10kmRandomDeviate);
				return getProb(beta, plot);
			}
			if (isRandomEffectsVariabilityEnabled) {
				double occupancyIndex10kmRandomDeviate = getOccupancyRandomDeviate(plot, species);
				setOccupancyInXVector(plot, species, occupancyIndex10kmRandomDeviate);
				return getProb(beta, plot);
			} else {
				double range = 3;
				GaussianEstimate estimate = getOccupancyIndex(plot, species);
				int indexVar = effectList.lastIndexOf(owner.OccupancyIndexEffects.get(0)); 
				double meanOccIndex = estimate.getMean().getValueAt(0, 0);
				double varOccIndex = estimate.getVariance().getValueAt(0, 0);
				InternalMathFunction imf = new InternalMathFunction(oXVector, beta, plot, indexVar, meanOccIndex, varOccIndex);
				
				double std = Math.sqrt(varOccIndex);
				double lowerBound = meanOccIndex - range * std;
				double upperBound = meanOccIndex + range * std;
				
				GaussLegendreQuadrature glq = new GaussLegendreQuadrature(NumberOfPoints.N10);
				glq.setLowerBound(lowerBound);
				glq.setUpperBound(upperBound);
				
				double prob = glq.getIntegralApproximation(imf, indexVar, false);
				return prob;
			}
		} else { // not using occupancy index
			return getProb(beta, plot);
		}
	}
	
	static List<Double> deviates = new ArrayList<Double>();
	
	double getOccupancyRandomDeviate(IrisCompatiblePlot plot, IrisSpecies species) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Map<Integer, Double> innerMap2 = getInnerMap2(plot, (Map) occupancyIndicesDeviates);
		if (!innerMap2.containsKey(plot.getDateYr())) {
			GaussianEstimate estimate = getOccupancyIndex(plot, species);
			double deviate = estimate.getRandomDeviate().getValueAt(0, 0);
			deviates.add(deviate);
			innerMap2.put(plot.getDateYr(), deviate);	
		}
		return innerMap2.get(plot.getDateYr());

	}
	
	private Map<Integer, ?> getInnerMap2(IrisCompatiblePlot plot, Map<String, Map<Integer, Map<Integer, ?>>> oMap) {
		if (isUsingOccupancyIndex()) {
			if (!oMap.containsKey(plot.getSubjectId())) {
				oMap.put(plot.getSubjectId(), new HashMap<Integer, Map<Integer, ?>>());
			}
			Map<Integer, Map<Integer, ?>> innerMap = oMap.get(plot.getSubjectId());
			if (!innerMap.containsKey(plot.getMonteCarloRealizationId())) {
				innerMap.put(plot.getMonteCarloRealizationId(), new HashMap<Integer, Object>());
			}
			Map<Integer, ?> innerMap2 = innerMap.get(plot.getMonteCarloRealizationId());
			return innerMap2;
		} else {
			return null;
		}
		
	}
	
	GaussianEstimate getOccupancyIndex(IrisCompatiblePlot plot, IrisSpecies species) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Map<Integer, GaussianEstimate> innerMap2 = getInnerMap2(plot, (Map) occupancyIndices);
		if (!innerMap2.containsKey(plot.getDateYr())) {
			GaussianEstimate occIndex10kmEstimate = owner.occIndexCalculator.getOccupancyIndex(plot.getPlotsForOccupancyIndexCalculation(), plot, species);
			innerMap2.put(plot.getDateYr(), occIndex10kmEstimate);	
		}
		return innerMap2.get(plot.getDateYr());
	}
	
	private boolean isUsingOccupancyIndex() {return !occupancyIndexVarIndices.isEmpty();}

	private void setValueInXVector(int effectId, IrisCompatiblePlot plot, IrisSpecies species, double occupancyIndex10km) {
		int index = effectList.indexOf(effectId);
		if (index == -1) {
			throw new InvalidParameterException("The effect id " + effectId + " is not part of this model!");
		}
		switch(effectId) {
		case 26: // intercept
		case 1:	// intercept
			oXVector.setValueAt(0, index, 1d);
			break;
		case 2: // DD
			oXVector.setValueAt(0, index, plot.getMeanDegreeDaysOverThePeriod());
			break;
		case 3: // DD:TotalPrcp
			oXVector.setValueAt(0, index, plot.getMeanDegreeDaysOverThePeriod() * plot.getMeanPrecipitationOverThePeriod());
			break;
		case 4: // DD2
			oXVector.setValueAt(0, index, plot.getMeanDegreeDaysOverThePeriod() * plot.getMeanDegreeDaysOverThePeriod());
			break;
		case 5: // 
			if (plot.getSoilDepth() == SoilDepth.Thick) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 6: // 
			if (plot.getSoilDepth() == SoilDepth.Shallow) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 7: // 
			if (plot.getSoilDepth() == SoilDepth.VeryShallow) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 8: // 
			if (plot.getDrainageGroup() == DrainageGroup.Xeric) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 9: // 
			if (plot.getDrainageGroup() == DrainageGroup.Subhydric) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 10: // 
			if (plot.getDrainageGroup() == DrainageGroup.Hydric) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 11: // 
			if (plot.getPastDisturbance() == DisturbanceType.Fire) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 12: // 
			if (plot.getPastDisturbance() == DisturbanceType.OtherNatural) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 13: // 
			if (plot.getPastDisturbance() == DisturbanceType.Harvest) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 14: // 
			if (plot.getSoilTexture() == SoilTexture.Crude) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 15: // 
			if (plot.getSoilTexture() == SoilTexture.Fine) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 16: // 
			if (plot.getUpcomingDisturbance() == DisturbanceType.Fire) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 17: // 
			if (plot.getUpcomingDisturbance() == DisturbanceType.OtherNatural) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 18: // 
			if (plot.getUpcomingDisturbance() == DisturbanceType.Harvest) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 19: // 
			oXVector.setValueAt(0, index, plot.getMeanNumberFrostDaysOverThePeriod());
			break;
		case 20: // G_F
			oXVector.setValueAt(0, index, plot.getBasalAreaOfBroadleavedSpecies());
			break;
		case 21: // G_F2
			oXVector.setValueAt(0, index, plot.getBasalAreaOfBroadleavedSpecies() * plot.getBasalAreaOfBroadleavedSpecies());
			break;
		case 22: // G_R
			oXVector.setValueAt(0, index, plot.getBasalAreaOfConiferousSpecies());
			break;
		case 23: // G_R2
			oXVector.setValueAt(0, index, plot.getBasalAreaOfConiferousSpecies() * plot.getBasalAreaOfConiferousSpecies());
			break;
		case 24: // G_SpGr
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpecies(species));
			break;
		case 25: // G_SpGr2
			double g_spgr = plot.getBasalAreaM2HaForThisSpecies(species);
			oXVector.setValueAt(0, index, g_spgr * g_spgr);
			break;
		case 27: // lnDt
			oXVector.setValueAt(0, index, Math.log(plot.getGrowthStepLengthYr()));
			break;
		case 28: // lowest t min
			oXVector.setValueAt(0, index, plot.getMeanLowestTemperatureOverThePeriod());
			break;
		case 29: // occIndex10km
			oXVector.setValueAt(0, index, occupancyIndex10km);
			break;
		case 30: // pente
			oXVector.setValueAt(0, index, plot.getSlopeInclinationPercent());
			break;
		case 31: // speciesThere
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpecies(species) > 0 ? 1d : 0d);
			break;
		case 32: // occIndex10km2
			oXVector.setValueAt(0, index, occupancyIndex10km * occupancyIndex10km);
			break;
		case 33: // timeSince1970
			oXVector.setValueAt(0, index, plot.getDateYr() + plot.getGrowthStepLengthYr() - 1970);
			break;
		case 34: // TotalPrcp
			oXVector.setValueAt(0, index, plot.getMeanPrecipitationOverThePeriod());
			break;
		case 35: // TotalPrcp2
			oXVector.setValueAt(0, index, plot.getMeanPrecipitationOverThePeriod() * plot.getMeanPrecipitationOverThePeriod());
			break;
		default:
			throw new InvalidParameterException("The effect id " + effectId + " is unknown!");
		}
	}

	/*
	 * Construct the xVector without the occupancy index.
	 */
	private void constructXVector(IrisCompatiblePlot plot, IrisSpecies species) {
		oXVector.resetMatrix();
		
		List<Integer> effectListWithoutOccIndex = new ArrayList<Integer>();
		effectListWithoutOccIndex.addAll(effectList);
		effectListWithoutOccIndex.removeAll(occupancyIndexVarIndices);
		for (int effectId : effectListWithoutOccIndex) {
			setValueInXVector(effectId, plot, species, 0d); // occupancy index set to 0 for now
		}
	}


}