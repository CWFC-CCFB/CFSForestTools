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
import java.util.List;

import canforservutility.predictor.iris.recruitment_v1.IrisCompatiblePlot.DisturbanceType;
import canforservutility.predictor.iris.recruitment_v1.IrisCompatiblePlot.SoilDepth;
import canforservutility.predictor.iris.recruitment_v1.IrisCompatiblePlot.SoilTexture;
import canforservutility.predictor.iris.recruitment_v1.IrisCompatibleTree.IrisSpecies;
import repicea.math.AbstractMathematicalFunctionWrapper;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.math.integral.GaussHermiteQuadrature;
import repicea.math.integral.GaussHermiteQuadrature.GaussHermiteQuadratureCompatibleFunction;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider.DrainageGroup;
import repicea.stats.LinearStatisticalExpression;
import repicea.stats.StatisticalUtility;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;

@SuppressWarnings("serial")
class IrisRecruitmentNumberInternalPredictor extends REpiceaPredictor {

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
			return mu + 1;
		}

		@Override
		public Matrix getGradient() {return null;}

		@Override
		public SymmetricMatrix getHessian() {return null;}
	}
	
	
	private final IrisRecruitmentNumberPredictor owner;
	private final List<Integer> effectList;
	protected final double theta; // as produced by R
	protected final double invTheta; //
	private final List<Integer> occupancyIndexVarIndices; // effect Ids that include the occupancy index
	private final GaussHermiteQuadrature ghq;
	
	protected IrisRecruitmentNumberInternalPredictor(IrisRecruitmentNumberPredictor owner,
			boolean isParametersVariabilityEnabled, 
			boolean isRandomEffectsVariabilityEnabled,
			boolean isResidualVariabilityEnabled, 
			double thetaParm,
			Matrix beta,
			SymmetricMatrix omega,
			Matrix effectMat) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);		// random effect stands for occupancy index variability
		this.owner = owner;
		
		ModelParameterEstimates estimate = new ModelParameterEstimates(beta, omega);
		setParameterEstimates(estimate);
		oXVector = new Matrix(1, estimate.getMean().m_iRows);
		
		effectList = new ArrayList<Integer>();
		occupancyIndexVarIndices = new ArrayList<Integer>();
		for (int i = 0; i < effectMat.m_iRows; i++) {
			int effectId = (int) effectMat.getValueAt(i, 0);
			effectList.add(effectId);
			if (IrisRecruitmentNumberPredictor.OccupancyIndexEffects.contains(effectId)) {
				occupancyIndexVarIndices.add(effectId);
			}
		}
		
		this.theta = thetaParm;
		this.invTheta = 1d/this.theta;
		ghq = new GaussHermiteQuadrature(); // a default 5-point Gauss-Hermite quadrature
	}

	@Override
	protected void init() {}

	private void setOccupancyInXVector(IrisCompatiblePlot plot, IrisSpecies species, double occupancyIndex10km) {
		for (int effectId : occupancyIndexVarIndices) {
			setValueInXVector(effectId, plot, species, occupancyIndex10km); 
		}
	}

	private double getNumber(Matrix beta) {
		double xBeta = oXVector.multiply(beta).getValueAt(0, 0);
		double mu = Math.exp(xBeta);
		if (isResidualVariabilityEnabled) {
			return StatisticalUtility.getRandom().nextNegativeBinomial(mu, invTheta) + 1;
		} else {
			return mu + 1;		// offset 1 because y = nbRecruits - 1
		}
	}
	
	public synchronized double predictNumberOfRecruits(IrisCompatiblePlot plot, IrisSpecies species) {
		Matrix beta = getParametersForThisRealization(plot);
		constructXVector(plot, species);
		
		if (isUsingOccupancyIndex()) {
			if (plot instanceof IrisCompatibleTestPlotImpl) { // occupancy is assumed to be known
				double occupancyIndex10kmRandomDeviate = ((IrisCompatibleTestPlotImpl) plot).getOccupancyIndex10km(species);
				setOccupancyInXVector(plot, species, occupancyIndex10kmRandomDeviate);
				return getNumber(beta);
			}
			if (isRandomEffectsVariabilityEnabled) {
				double occupancyIndex10kmRandomDeviate = owner.occurrencePredictor.getInternalPredictor(species).getOccupancyRandomDeviate(plot, species);
				setOccupancyInXVector(plot, species, occupancyIndex10kmRandomDeviate);
				return getNumber(beta);
			} else {
				GaussianEstimate estimate = owner.occurrencePredictor.getInternalPredictor(species).getOccupancyIndex(plot, species);
				setOccupancyInXVector(plot, species, estimate.getMean().getValueAt(0, 0)); // we set the variable to its mean before performing the quadrature
				GaussHermiteImpl ghi = new GaussHermiteImpl(oXVector, beta, estimate.getVariance().getValueAt(0, 0));
				double ghqApproximation = ghq.getIntegralApproximation(ghi, effectList.indexOf(IrisRecruitmentNumberPredictor.OccupancyIndexEffects.get(0)), false);
				return ghqApproximation;
			}
		} else { // not using occupancy index
			return getNumber(beta);
		}
	}

	
	private boolean isUsingOccupancyIndex() {return !occupancyIndexVarIndices.isEmpty();}
	
	private void setValueInXVector(int effectId, IrisCompatiblePlot plot, IrisSpecies species, double occupancyIndex10km) {
		int index = effectList.indexOf(effectId);
		if (index == -1) {
			throw new InvalidParameterException("The effect id " + effectId + " is not part of this model!");
		}
		switch(effectId) {
		case 22: // intercept as well
		case 1:	// intercept
			oXVector.setValueAt(0, index, 1d);
			break;
		case 2: // DD
			oXVector.setValueAt(0, index, plot.getMeanDegreeDaysOverThePeriod());
			break;
		case 3: // DD2
			oXVector.setValueAt(0, index, plot.getMeanDegreeDaysOverThePeriod() * plot.getMeanDegreeDaysOverThePeriod());
			break;
		case 4: // dt
			oXVector.setValueAt(0, index, plot.getGrowthStepLengthYr());
			break;
		case 5: // 
			if (plot.getSoilDepth() == SoilDepth.VeryShallow) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 6: // 
			if (plot.getDrainageGroup() == DrainageGroup.Subhydric) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 7: // 
			if (plot.getDrainageGroup() == DrainageGroup.Hydric) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 8: // 
			if (plot.getPastDisturbance() == DisturbanceType.Fire) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 9: // 
			if (plot.getPastDisturbance() == DisturbanceType.OtherNatural) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 10: // 
			if (plot.getPastDisturbance() == DisturbanceType.Harvest) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 11: // 
			if (plot.getSoilTexture() == SoilTexture.Crude) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 12: // 
			if (plot.getSoilTexture() == SoilTexture.Fine) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 13: // 
			if (plot.getUpcomingDisturbance() == DisturbanceType.Fire) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 14: // 
			if (plot.getUpcomingDisturbance() == DisturbanceType.OtherNatural) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 15: // 
			if (plot.getUpcomingDisturbance() == DisturbanceType.Harvest) {
				oXVector.setValueAt(0, index, 1d);
			}
			break;
		case 16: // FrostDay
			oXVector.setValueAt(0, index, plot.getMeanNumberFrostDaysOverThePeriod());
			break;
		case 17: // G_F
			oXVector.setValueAt(0, index, plot.getBasalAreaOfBroadleavedSpecies());
			break;
		case 18: // G_R
			oXVector.setValueAt(0, index, plot.getBasalAreaOfConiferousSpecies());
			break;
		case 19: // G_R2
			oXVector.setValueAt(0, index, plot.getBasalAreaOfConiferousSpecies() * plot.getBasalAreaOfConiferousSpecies());
			break;
		case 20: // G_SpGr
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpecies(species));
			break;
		case 21: // G_SpGr2
			double g_spgr = plot.getBasalAreaM2HaForThisSpecies(species);
			oXVector.setValueAt(0, index, g_spgr * g_spgr);
			break;
		case 23: // lnDt
			oXVector.setValueAt(0, index, Math.log(plot.getGrowthStepLengthYr()));
			break;
		case 24: // LowestTmin
			oXVector.setValueAt(0, index, plot.getMeanLowestTemperatureOverThePeriod());
			break;
		case 25: // occIndex10km
			oXVector.setValueAt(0, index, occupancyIndex10km);
			break;
		case 26: // pentePerc
			oXVector.setValueAt(0, index, plot.getSlopeInclinationPercent());
			break;
		case 27: // speciesThere
			oXVector.setValueAt(0, index, plot.getBasalAreaM2HaForThisSpecies(species) > 0 ? 1d : 0d);
			break;
		case 29: // timeSince1970
			oXVector.setValueAt(0, index, plot.getDateYr() + plot.getGrowthStepLengthYr() - 1970);
			break;
		case 30: // TotalPrcp
			oXVector.setValueAt(0, index, plot.getMeanPrecipitationOverThePeriod());
			break;
		case 31: // TotalPrcp * TotalPrcp
			oXVector.setValueAt(0, index, plot.getMeanPrecipitationOverThePeriod() * plot.getMeanPrecipitationOverThePeriod());
			break;
		default:
			throw new InvalidParameterException("The effect id " + effectId + " is unknown!");
		}
	}
	
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