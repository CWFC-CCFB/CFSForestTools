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
public class Trillium2026DiameterIncrementInternalPredictor extends REpiceaPredictor {

	enum Effect {
		Intercept,
		DBH,
		BAL,
		dt,
		MeanTminJanuary,
		TotalPrecMarchToMay, 
		MeanTempJuneToAugust, 
		T_anom, 
		interval, // TODO check the meaning of this one it is coded as int MF20250328 
		TotalRadiation, 
		MeanSummerVPD, 
		FrostFreeDay, 
		MeanTmaxJuly, 
		SMImean, 
		Mx_anom, 
		MeanSummerVPDDaylight, 
		TotalPrecJuneToAugust, 
		P_anom, 
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
		EffectMap.put("T_anom", Effect.T_anom);
		EffectMap.put("int", Effect.interval);
		EffectMap.put("TotalRadiation", Effect.TotalRadiation);
		EffectMap.put("MeanSummerVPD", Effect.MeanSummerVPD);
		EffectMap.put("FrostFreeDay", Effect.FrostFreeDay);
		EffectMap.put("MeanTmaxJuly", Effect.MeanTmaxJuly);
		EffectMap.put("SMImean", Effect.SMImean);
		EffectMap.put("Mx_anom", Effect.Mx_anom);
		EffectMap.put("MeanSummerVPDDaylight", Effect.MeanSummerVPDDaylight);
		EffectMap.put("TotalPrecJuneToAugust", Effect.TotalPrecJuneToAugust);
		EffectMap.put("P_anom", Effect.P_anom);
		EffectMap.put("CMI", Effect.CMI);
		EffectMap.put("HitghestTmax", Effect.HighestTmax);
		EffectMap.put("TotalPrcp", Effect.TotalPrcp);
		EffectMap.put("MeanTair", Effect.MeanTair);
		EffectMap.put("DD", Effect.DD);
		EffectMap.put("LowestTmin", Effect.LowestTmin);
	}
	
	@SuppressWarnings("unused")
	private final Trillium2026TreeSpecies species;
	private final List<Effect> effects;
	private double sigma;
	private double sigma2;
	private Matrix oXVector;

	Trillium2026DiameterIncrementInternalPredictor(Trillium2026TreeSpecies species,
			boolean isParametersVariabilityEnabled, 
			boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
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
			case T_anom:
				oXVector.setValueAt(0, index++, plot.getT_anom());
				break;
			case interval:
				oXVector.setValueAt(0, index++, plot.getInterval());
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
			case Mx_anom:
				oXVector.setValueAt(0, index++, plot.getMx_anom());
				break;
			case MeanSummerVPDDaylight:
				oXVector.setValueAt(0, index++, plot.getMeanSummerVPDDaylight());
				break;
			case TotalPrecJuneToAugust:
				oXVector.setValueAt(0, index++, plot.getTotalPrecJuneToAugustMm());
				break; 
			case P_anom:
				oXVector.setValueAt(0, index++, plot.getP_anom());
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
			// back-transform here MF20250328
		} else {
			pred += sigma2 * .5; // TODO check if this is the appropriate back transformation
			// back-transform here MF20250328
		}
		return pred;
	}
	
}
