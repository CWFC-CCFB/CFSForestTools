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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.AbstractMathematicalFunction;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.math.integral.AbstractGaussQuadrature.NumberOfPoints;
import repicea.math.integral.GaussLegendreQuadrature;
import repicea.math.utility.GaussianUtility;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.climate.REpiceaClimateManager.ClimateVariableTemporalResolution;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.model.glm.LinkFunction;

@SuppressWarnings("serial")
class Trillium2026RecruitmentOccurrenceInternalPredictor extends REpiceaBinaryEventPredictor<Trillium2026RecruitmentPlot, Trillium2026Tree> {

	private static ClimateVariableTemporalResolution IntervalStartingBeforeInitialMeas = ClimateVariableTemporalResolution.IntervalAveragedStartingBeforeInitialMeasurement;
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
		
		InternalMathFunction(Matrix xVector, Matrix beta, Trillium2026RecruitmentPlot plot, int indexVar, double meanOccIndex, double varOccIndex) {
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
		final Trillium2026RecruitmentPlot plot;
		final double meanOccIndex;
		
		InternalStatisticalExpression(Matrix xVector, Matrix beta, Trillium2026RecruitmentPlot plot, double meanOccIndex) {
			this.xVector = xVector;
			this.beta = beta;
			this.plot = plot;
			this.meanOccIndex = meanOccIndex;
		}
		
		@Override
		public Double getValue() {
			double xBeta = xVector.multiply(beta).getValueAt(0, 0);
			if (Trillium2026RecruitmentOccurrenceInternalPredictor.this.offsetEnabled) {
				xBeta += Math.log(plot.getGrowthStepLengthYr());
			}
			return xBeta;
		}
		
		@Override
		public void setVariableValue(int variableIndex, double variableValue) {
			Trillium2026RecruitmentOccurrenceInternalPredictor.this.setOccupancyInXVector(plot, Trillium2026RecruitmentOccurrenceInternalPredictor.this.species, variableValue);
		}		

		@Override
		public double getVariableValue(int variableIndex) {return meanOccIndex;}
		
		@Override
		public Matrix getGradient() {return null;}

		@Override
		public SymmetricMatrix getHessian() {return null;}
		
	}

	private final Trillium2026RecruitmentOccurrencePredictor owner;
	private final List<Integer> effectList;
	private final boolean offsetEnabled;
	private final Map<String, Map<Integer, Map<Integer, GaussianEstimate>>> occupancyIndices; // 1st key plot id, 2nd key realization id, 3rd key dateYr
	private final Map<String, Map<Integer, Map<Integer, Double>>> occupancyIndicesDeviates; // 1st key plot id, 2nd key realization id, 3rd key dateYr
	private final List<Integer> occupancyIndexVarIndices; // effect Ids that include the occupancy index
	private final Species species;
	
	protected Trillium2026RecruitmentOccurrenceInternalPredictor(Trillium2026RecruitmentOccurrencePredictor owner,
			Species species,
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
			if (Trillium2026RecruitmentOccurrencePredictor.OccupancyIndexEffects.contains(effectId)) {
				occupancyIndexVarIndices.add(effectId);
			}
		}
		occupancyIndices = new HashMap<String, Map<Integer, Map<Integer, GaussianEstimate>>>();
		occupancyIndicesDeviates = new HashMap<String, Map<Integer, Map<Integer, Double>>>();
	}

	@Override
	protected void init() {}
	
	private void setOccupancyInXVector(Trillium2026RecruitmentPlot plot, Species species, double occupancyIndex25km) {
		for (int effectId : occupancyIndexVarIndices) {
			setValueInXVector(effectId, plot, species, occupancyIndex25km); 
		}
	}

	private double getProb(Matrix beta, Trillium2026RecruitmentPlot plot) {
		double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
		if (offsetEnabled) {
			xBeta += Math.log(plot.getGrowthStepLengthYr());
		}
		double recruitmentProbability = 1d - Math.exp(-Math.exp(xBeta));
		return recruitmentProbability;
	}
	
	@Override
	public double predictEventProbability(Trillium2026RecruitmentPlot plot, Trillium2026Tree tree, Map<String, Object> parms) {
		return calculateEventProbability(plot, tree.getTrillium2026TreeSpecies());
	}

	protected synchronized double calculateEventProbability(Trillium2026RecruitmentPlot plot, Species species) {
		Matrix beta = getParametersForThisRealization(plot);
		constructXVector(plot, species);
		if (isUsingOccupancyIndex()) {
			if (plot instanceof Trillium2026RecruitmentPlotWithKnownOccupancy) { // occupancy is assumed to be known
				double occupancyIndex25kmRandomDeviate = ((Trillium2026RecruitmentPlotWithKnownOccupancy) plot).getOccupancyIndex25km(species);
				setOccupancyInXVector(plot, species, occupancyIndex25kmRandomDeviate);
				return getProb(beta, plot);
			}
			if (isRandomEffectsVariabilityEnabled) {
				double occupancyIndex25kmRandomDeviate = getOccupancyRandomDeviate(plot, species);
				setOccupancyInXVector(plot, species, occupancyIndex25kmRandomDeviate);
				return getProb(beta, plot);
			} else {
				final double range = 3;
				GaussianEstimate estimate = getOccupancyIndex(plot, species);
				int indexVar = effectList.lastIndexOf(owner.OccupancyIndexEffects.get(0)); 
				double meanOccIndex = estimate.getMean().getValueAt(0, 0);
				double varOccIndex = estimate.getVariance().getValueAt(0, 0);
				if (varOccIndex == 0d) { // there is no variability
					return getProb(beta, plot);
				} else {
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
			}
		} else { // not using occupancy index
			return getProb(beta, plot);
		}
	}
	
	static List<Double> deviates = new ArrayList<Double>();
	
	double getOccupancyRandomDeviate(Trillium2026RecruitmentPlot plot, Species species) {
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
	
	private Map<Integer, ?> getInnerMap2(Trillium2026RecruitmentPlot plot, Map<String, Map<Integer, Map<Integer, ?>>> oMap) {
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
	
	GaussianEstimate getOccupancyIndex(Trillium2026RecruitmentPlot plot, Species species) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Map<Integer, GaussianEstimate> innerMap2 = getInnerMap2(plot, (Map) occupancyIndices);
		if (!innerMap2.containsKey(plot.getDateYr())) {
			GaussianEstimate occIndex10kmEstimate = owner.occIndexCalculator.getOccupancyIndex(plot.getPlotsForOccupancyIndexCalculation(), plot, species, 25d); // max distance is 25 km for occupancy index  
			innerMap2.put(plot.getDateYr(), occIndex10kmEstimate);	
		}
		return innerMap2.get(plot.getDateYr());
	}
	
	private boolean isUsingOccupancyIndex() {return !occupancyIndexVarIndices.isEmpty();}

	private void setValueInXVector(int effectId, Trillium2026RecruitmentPlot plot, Species species, double occupancyIndex10km) {
		int index = effectList.indexOf(effectId);
		if (index == -1) {
			throw new InvalidParameterException("The effect id " + effectId + " is not part of this model!");
		}
		switch(effectId) {
		case 1:	// intercept
			oXVector.setValueAt(0, index, 1d);
			break;
		case 2: // DD
			oXVector.setValueAt(0, index, plot.getGrowingDegreeDaysCelsius(IntervalStartingBeforeInitialMeas));
			break;
		case 3: // Frost free days
			oXVector.setValueAt(0, index, plot.getAnnualNbFrostFreeDays(IntervalStartingBeforeInitialMeas));
			break;
		case 4: // G_F
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.BroadleavedSpecies));
			break;
		case 5: // G_F2
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.BroadleavedSpecies) * 
					plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.BroadleavedSpecies));
			break;
		case 6: // G_R
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.ConiferousSpecies));
			break;
		case 7: // G_R2
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.ConiferousSpecies) * 
					plot.getBasalAreaM2HaForThisSpeciesType(SpeciesType.ConiferousSpecies));
			break;
		case 8: // G_SpGr
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpecies(species));
			break;
		case 9: // G_SpGr2
			double g_spgr = plot.getBasalAreaM2HaForThisSpecies(species);
			oXVector.setValueAt(0, index, g_spgr * g_spgr);
			break;
		case 10: // lnDt
			oXVector.setValueAt(0, index, Math.log(plot.getGrowthStepLengthYr()));
			break;
		case 11: // lowest t min
			oXVector.setValueAt(0, index, plot.getLowestAnnualTemperatureCelsius(IntervalStartingBeforeInitialMeas));
			break;
		case 12: // MeanTminJanuary
			oXVector.setValueAt(0, index, plot.getMeanMinimumJanuaryTemperatureCelsius(IntervalStartingBeforeInitialMeas));
			break;
		case 13: // occIndex25km
			oXVector.setValueAt(0, index, occupancyIndex10km);
			break;
		case 14: // occIndex25km2
			oXVector.setValueAt(0, index, occupancyIndex10km * occupancyIndex10km);
			break;
		case 15: // speciesThere
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpecies(species) > 0 ? 1d : 0d);
			break;
		case 16: // TotalPrcp
			oXVector.setValueAt(0, index, plot.getTotalAnnualPrecipitationMm(IntervalStartingBeforeInitialMeas));
			break;
		case 17: // TotalPrecMarchToMay
			oXVector.setValueAt(0, index, plot.getTotalPrecipitationFromMarchToMayMm(IntervalStartingBeforeInitialMeas));
			break;
		default:
			throw new InvalidParameterException("The effect id " + effectId + " is unknown!");
		}
	}

	/*
	 * Construct the xVector without the occupancy index.
	 */
	private void constructXVector(Trillium2026RecruitmentPlot plot, Species species) {
		oXVector.resetMatrix();
		
		List<Integer> effectListWithoutOccIndex = new ArrayList<Integer>();
		effectListWithoutOccIndex.addAll(effectList);
		effectListWithoutOccIndex.removeAll(occupancyIndexVarIndices);
		for (int effectId : effectListWithoutOccIndex) {
			setValueInXVector(effectId, plot, species, 0d); // occupancy index set to 0 for now
		}
	}


}