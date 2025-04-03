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
package ontariomnrf.predictor.trillium2026;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ontariomnrf.predictor.trillium2026.Trillium2026Tree.Trillium2026TreeSpecies;
import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("serial")
final class Trillium2026DiameterIncrementInternalPredictor extends REpiceaPredictor {

	enum Effect {
		Intercept,
		DBH,
		BAL,
		dt,
		MeanTminJanuary,
		TotalPrecMarchToMay, 
		MeanTempJuneToAugust, 
		MeanTempAnomaly, 
		DBH_x_BAL, 
		TotalRadiation, 
		MeanSummerVPD, 
		FrostFreeDay, 
		MeanTmaxJuly, 
		SMImean, 
		MaxTempAnomaly, 
		MeanSummerVPDDaylight, 
		TotalPrecJuneToAugust, 
		PrecAnomaly, 
		CMI, 
		HighestTmax, 
		TotalPrcp, 
		MeanTair, 
		DD, 
		LowestTmin;
	}
	
	static final Map<String, Effect> EffectMap = new HashMap<String, Effect>();
	static {
		EffectMap.put("(Intercept)", Effect.Intercept);
		EffectMap.put("DBH.x", Effect.DBH);
		EffectMap.put("BAL", Effect.BAL);
		EffectMap.put("dt", Effect.dt);
		EffectMap.put("MeanTminJanuary", Effect.MeanTminJanuary);
		EffectMap.put("TotalPrecMarchToMay", Effect.TotalPrecMarchToMay);
		EffectMap.put("MeanTempJuneToAugust", Effect.MeanTempJuneToAugust);
		EffectMap.put("T_anom", Effect.MeanTempAnomaly);
		EffectMap.put("int", Effect.DBH_x_BAL);
		EffectMap.put("TotalRadiation", Effect.TotalRadiation);
		EffectMap.put("MeanSummerVPD", Effect.MeanSummerVPD);
		EffectMap.put("FrostFreeDay", Effect.FrostFreeDay);
		EffectMap.put("MeanTmaxJuly", Effect.MeanTmaxJuly);
		EffectMap.put("SMImean", Effect.SMImean);
		EffectMap.put("Mx_anom", Effect.MaxTempAnomaly);
		EffectMap.put("MeanSummerVPDDaylight", Effect.MeanSummerVPDDaylight);
		EffectMap.put("TotalPrecJuneToAugust", Effect.TotalPrecJuneToAugust);
		EffectMap.put("P_anom", Effect.PrecAnomaly);
		EffectMap.put("CMI", Effect.CMI);
		EffectMap.put("HitghestTmax", Effect.HighestTmax);
		EffectMap.put("TotalPrcp", Effect.TotalPrcp);
		EffectMap.put("MeanTair", Effect.MeanTair);
		EffectMap.put("DD", Effect.DD);
		EffectMap.put("LowestTmin", Effect.LowestTmin);
	}
	
	private final Trillium2026DiameterIncrementPredictor owner;
	@SuppressWarnings("unused")
	private final Trillium2026TreeSpecies species;
	private final List<Effect> effects;
	private double sigma;
	private double sigma2;
	

	Trillium2026DiameterIncrementInternalPredictor(Trillium2026DiameterIncrementPredictor owner,
			Trillium2026TreeSpecies species,
			boolean isParametersVariabilityEnabled, 
			boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
		this.owner = owner;
		this.species = species;
		effects = new ArrayList<Effect>();
	}

	@Override
	protected void init() {}

	/*
	 * For extended visibility only.
	 */
	@Override
	protected void setParameterEstimates(ModelParameterEstimates gaussianEstimate) {
		super.setParameterEstimates(gaussianEstimate);
		oXVector = new Matrix(1, getParameterEstimates().getMean().m_iRows);
	}
	
	protected void setEffects(List<Effect> effects) {
		this.effects.clear();
		this.effects.addAll(effects);
	}
	
	protected void setResidualStandardDeviation(double sigma) {
		this.sigma = sigma;
		this.sigma2 = sigma * sigma;
	}

	private void setXVector(Trillium2026Plot plot, Trillium2026Tree tree) {
		oXVector.resetMatrix();
		int index = 0;
		for (Effect effect : effects) {
			switch(effect) {
			case Intercept:
				oXVector.setValueAt(0, index++, 1d);
				break;
			case DBH:
				oXVector.setValueAt(0, index++, tree.getDbhCm());
				break;
			case BAL:
				oXVector.setValueAt(0, index++, tree.getBasalAreaLargerThanSubjectM2Ha());
				break;
			case dt:
				oXVector.setValueAt(0, index++, plot.getGrowthStepLengthYr());
				break;
			case MeanTminJanuary:
				oXVector.setValueAt(0, index++, plot.getMeanTminJanuaryCelsius());
				break;
			case TotalPrecMarchToMay:
				oXVector.setValueAt(0, index++, plot.getTotalPrecMarchToMayMm());
				break;
			case MeanTempJuneToAugust: 
				oXVector.setValueAt(0, index++, plot.getMeanTempJuneToAugustCelsius());
				break;
			case MeanTempAnomaly:
				oXVector.setValueAt(0, index++, plot.getMeanTempAnomalyCelsius());
				break;
			case DBH_x_BAL:
				oXVector.setValueAt(0, index++, tree.getBasalAreaLargerThanSubjectM2Ha() * tree.getDbhCm());
				break;
			case TotalRadiation:
				oXVector.setValueAt(0, index++, plot.getTotalRadiation());
				break;
			case MeanSummerVPD:
				oXVector.setValueAt(0, index++, plot.getMeanSummerVPD());
				break;
			case FrostFreeDay:
				oXVector.setValueAt(0, index++, plot.getFrostFreeDays());
				break;
			case MeanTmaxJuly:
				oXVector.setValueAt(0, index++, plot.getMeanTmaxJulyCelsius());
				break;
			case SMImean:
				oXVector.setValueAt(0, index++, plot.getSMImean());
				break;
			case MaxTempAnomaly:
				oXVector.setValueAt(0, index++, plot.getMaxTempAnomalyCelsius());
				break;
			case MeanSummerVPDDaylight:
				oXVector.setValueAt(0, index++, plot.getMeanSummerVPDDaylight());
				break;
			case TotalPrecJuneToAugust:
				oXVector.setValueAt(0, index++, plot.getTotalPrecJuneToAugustMm());
				break; 
			case PrecAnomaly:
				oXVector.setValueAt(0, index++, plot.getTotalPrecipitationAnomalyMm());
				break;
			case CMI:
				oXVector.setValueAt(0, index++, plot.getCMI());
				break;
			case HighestTmax:
				oXVector.setValueAt(0, index++, plot.getHighestTmaxCelsius());
				break;
			case TotalPrcp:
				oXVector.setValueAt(0, index++, plot.getTotalAnnualPrecipitationMm());
				break;
			case MeanTair:
				oXVector.setValueAt(0, index++, plot.getMeanAnnualTemperatureCelsius());
				break;
			case DD:
				oXVector.setValueAt(0, index++, plot.getDegreeDaysCelsius());
				break;
			case LowestTmin:
				oXVector.setValueAt(0, index++, plot.getLowestTmin());
				break;
			}
		}
	}
	
	synchronized double predictGrowth(Trillium2026Plot plot, Trillium2026Tree tree) {
		Matrix beta = getParametersForThisRealization(plot);
		setXVector(plot, tree);
		double pred = oXVector.multiply(beta).getValueAt(0, 0);
		if (isResidualVariabilityEnabled) {
			pred += StatisticalUtility.getRandom().nextGaussian() * sigma;
		} 
		
		if (owner.doBackTransformation) {
			if (isResidualVariabilityEnabled) {
				pred = Math.sinh(pred);
			} else {
				pred = Math.exp(sigma2 * .5) * Math.sinh(pred);  // sinh is the back transformation and e^s2/2 is the correction factor
			}
		}
		
		double stepLengthYr = plot.getGrowthStepLengthYr();
		if (pred > 1.8 * stepLengthYr) { //  a cap, 1.8 is the 0.9995 percentile of observed periodical diameter increment 
			pred = 1.8 * stepLengthYr;
		}
		
		return pred; 
	}
	
}
