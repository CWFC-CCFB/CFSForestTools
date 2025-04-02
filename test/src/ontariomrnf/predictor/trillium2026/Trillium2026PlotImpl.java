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
package ontariomrnf.predictor.trillium2026;

import ontariomnrf.predictor.trillium2026.Trillium2026Plot;
import repicea.simulation.HierarchicalLevel;

class Trillium2026PlotImpl implements Trillium2026Plot {

	
	private final double growthStepLengthYr;
	private final double totalAnnualPrecipitationMm;
	private final double meanAnnualTemperatureCelsius;
	private final double meanTminJanuaryCelsius;
	private final double totalPrecMarchToMayMm;
	private final double meanTempJuneToAugustCelsius;
	private final double t_anom;
	private final double interval;
	private final double totalRadiation;
	private final double meanSummerVPD;
	private final double frostFreeDays;
	private final double meanTmaxJulyCelsius;
	private final double SMImean;
	private final double Mx_anom;
	private final double meanSummerVPDDaylight;
	private final double totalPrecJuneToAugustMm;
	private final double P_anom;
	private final double CMI;
	
	Trillium2026PlotImpl(double growthStepLengthYr,
			double totalAnnualPrecipitationMm,
			double meanAnnualTemperatureCelsius,
			double meanTminJanuaryCelsius,
			double totalPrecMarchToMayMm,
			double meanTempJuneToAugustCelsius,
			double t_anom,
			double interval,
			double totalRadiation,
			double meanSummerVPD,
			double frostFreeDays,
			double meanTmaxJulyCelsius,
			double SMImean,
			double Mx_anom,
			double meanSummerVPDDaylight,
			double totalPrecJuneToAugustMm,
			double P_anom,
			double CMI) {
		this.growthStepLengthYr = growthStepLengthYr;
		this.totalAnnualPrecipitationMm = totalAnnualPrecipitationMm;
		this.meanAnnualTemperatureCelsius = meanAnnualTemperatureCelsius;
		this.meanTminJanuaryCelsius = meanTminJanuaryCelsius;
		this.totalPrecMarchToMayMm = totalPrecMarchToMayMm;
		this.meanTempJuneToAugustCelsius = meanTempJuneToAugustCelsius;
		this.t_anom = t_anom;
		this.interval = interval;
		this.totalRadiation = totalRadiation;
		this.meanSummerVPD = meanSummerVPD;
		this.frostFreeDays = frostFreeDays;
		this.meanTmaxJulyCelsius = meanTmaxJulyCelsius;
		this.SMImean = SMImean;
		this.Mx_anom = Mx_anom;
		this.meanSummerVPDDaylight = meanSummerVPDDaylight;
		this.totalPrecJuneToAugustMm = totalPrecJuneToAugustMm;
		this.P_anom = P_anom;
		this.CMI = CMI;
	}
	
	@Override
	public String getSubjectId() {return null;}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public double getGrowthStepLengthYr() {return growthStepLengthYr;}

	@Override
	public double getTotalAnnualPrecipitationMm() {return totalAnnualPrecipitationMm;}

	@Override
	public double getMeanAnnualTemperatureCelsius() {return meanAnnualTemperatureCelsius;}

	@Override
	public double getMeanTminJanuaryCelsius() {return meanTminJanuaryCelsius;}

	@Override
	public double getTotalPrecMarchToMayMm() {return totalPrecMarchToMayMm;}

	@Override
	public double getMeanTempJuneToAugustCelsius() {return meanTempJuneToAugustCelsius;}

	@Override
	public double getT_anom() {return t_anom;}

	@Override
	public double getInterval() {return interval;}

	@Override
	public double getTotalRadiation() {return totalRadiation;}

	@Override
	public double getMeanSummerVPD() {return meanSummerVPD;}

	@Override
	public double getFrostFreeDays() {return frostFreeDays;}

	@Override
	public double getMeanTmaxJulyCelsius() {return meanTmaxJulyCelsius;}

	@Override
	public double getSMImean() {return SMImean;}

	@Override
	public double getMx_anom() {return Mx_anom;}

	@Override
	public double getMeanSummerVPDDaylight() {return meanSummerVPDDaylight;}

	@Override
	public double getTotalPrecJuneToAugustMm() {return totalPrecJuneToAugustMm;}

	@Override
	public double getP_anom() {return P_anom;}

	@Override
	public double getCMI() {return CMI;}

	@Override
	public double getHighestTmaxCelsius() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getDegreeDaysCelsius() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getLowestTmin() {
		// TODO Auto-generated method stub
		return 0;
	}

}
