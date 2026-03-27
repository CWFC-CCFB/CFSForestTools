/*
 * This file is part of the CFSForesttools library.
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
package ontariomnrf.predictor.trillium2026;

import java.util.HashMap;
import java.util.Map;

import canforservutility.occupancyindex.OccupancyIndexCalculator;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.climate.REpiceaClimateVariableInformation;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.simulation.species.REpiceaSpecies.Species;

final class Trillium2026RecruitmentPlotImpl implements Trillium2026RecruitmentPlot {

	class Trillium2026TreeImpl implements Trillium2026Tree {
		
		final Species species;
		
		Trillium2026TreeImpl(Species species) {
			this.species = species;
		}
		
		@Override
		public double getBasalAreaLargerThanSubjectM2Ha() {return 0;}

		@Override
		public double getDbhCm() {return 0;}

		@Override
		public double getSquaredDbhCm() {return 0;}

		@Override
		public double getLnDbhCm() {return 0;}

		@Override
		public String getSubjectId() {return null;}

		@Override
		public int getMonteCarloRealizationId() {return 0;}

		@Override
		public Species getTrillium2026TreeSpecies() {return species;}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}
	}
		
	
	static enum Mode {
		Known,
		Estimated,
		Deviate;
	}
	
	private final int growthStepLength;
	private final double basalAreaM2HaConiferous;
	private final double basalAreaM2HaBroadleaved;
	private final int dateYr;
	private final double dd;
	private final double prcp;
	private final String id;
	final Species species;
	private final Map<Species, Double> gSpGrMat;
	private final double frostDays;
	private final double lowestTmin;
//	private final List<OccupancyIndexCalculablePlot> plots;
	private final double latitudeDeg;
	private final double longitudeDeg;
	private int monteCarloRealizationId = 0;
	private double meanTminJanuary;
	private double precMarchToMay;
	private final double totalPrecJuneToAugust;
	private final double highestTmax;
	private final double slopePct;
	private final boolean interventionResult;
	private final boolean isGoingToBeHarvested;
	protected final Map<Species, Double> occupancyIndexMap;
	private final double meanAnnualTemperature;
	private final double meanMaxJulyTemperature;
	private final double meanTemperatureJulyToAugust;
	private final Map<Species, Double> predMap;
	private final OccupancyIndexCalculator occIndexCalc;
	private Mode mode;
		
	Trillium2026RecruitmentPlotImpl(String id,
			double latitudeDeg,
			double longitudeDeg,
			int growthStepLength,
			double basalAreaM2HaConiferous,
			double basalAreaM2HaBroadleaved,
			int dateYr,
			double dd,
			double prcp,
			double frostDays,
			double lowestTmin,
			double meanTminJanuary,
			double precMarchToMay,
			double totalPrecJuneToAugust,
			double highestTmax,
			Species species,
			double gSpGr,
			double slopePct,
			boolean interventionResult,
			boolean isGoingToBeHarvested,
			double occupancyIndex,
//			List<OccupancyIndexCalculablePlot> plots,
			double meanAnnualTemperature,
			double meanMaxJulyTemperature,
			double meanTempJulyToAugust,
			double pred,
			OccupancyIndexCalculator occIndexCalc) {
//		if (plots == null) {
//			throw new InvalidParameterException("The plots argument should not be null!");
//		}
		this.id = id;
		this.latitudeDeg = latitudeDeg;
		this.longitudeDeg = longitudeDeg;
		this.growthStepLength = growthStepLength;
		this.basalAreaM2HaConiferous = basalAreaM2HaConiferous;
		this.basalAreaM2HaBroadleaved = basalAreaM2HaBroadleaved;
		this.dateYr = dateYr;
		this.dd = dd;
		this.prcp = prcp;
		this.frostDays = frostDays;
		this.lowestTmin = lowestTmin;
		this.meanTminJanuary = meanTminJanuary;
		this.precMarchToMay = precMarchToMay;
		this.totalPrecJuneToAugust = totalPrecJuneToAugust;
		this.highestTmax = highestTmax;
		this.species = species;
		gSpGrMat = new HashMap<Species, Double>();
		gSpGrMat.put(species, gSpGr);
		this.slopePct = slopePct;
		this.interventionResult = interventionResult;
		this.isGoingToBeHarvested = isGoingToBeHarvested;
		this.occupancyIndexMap = new HashMap<Species, Double>();
		occupancyIndexMap.put(species, occupancyIndex);
		this.meanAnnualTemperature = meanAnnualTemperature;
		this.meanMaxJulyTemperature = meanMaxJulyTemperature;
		this.meanTemperatureJulyToAugust = meanTempJulyToAugust;
		predMap = new HashMap<Species, Double>();
		predMap.put(species, pred);
		this.occIndexCalc = occIndexCalc;
		mode = Mode.Known;
	}
	
	void update(Species sp, double gSpGr, double occupancyIndex, double pred) {
		gSpGrMat.put(sp, gSpGr);
		occupancyIndexMap.put(sp, occupancyIndex);
		predMap.put(sp, pred);
	}

	@Override
	public String getSubjectId() {return id;}

	@Override
	public int getMonteCarloRealizationId() {return this.monteCarloRealizationId;}
	
	void setMonteCarloRealizationId(int id) {
		this.monteCarloRealizationId = id;
	}

	@Override
	public int getGrowthStepLengthYr() {return growthStepLength;}

	@Override
	public int getDateYr() {return dateYr;}

	@Override
	public double getGrowingDegreeDaysCelsius(REpiceaClimateVariableInformation resolution) {return dd;}

	@Override
	public double getTotalAnnualPrecipitationMm(REpiceaClimateVariableInformation resolution) {return prcp;}

	
	Trillium2026Tree getTreeInstance(Species sp) {
		return new Trillium2026TreeImpl(sp); 
	}

	@Override
	public double getBasalAreaM2HaForThisSpecies(Enum<?> species) {
		Double value = gSpGrMat.get(species);
		return value != null ? value : -1;
	}

	@Override
	public double getBasalAreaM2HaForThisSpeciesType(SpeciesType type) {
		return type == SpeciesType.ConiferousSpecies ?
				basalAreaM2HaConiferous :
					basalAreaM2HaBroadleaved;
	}

	@Override
	public double getAnnualNbFrostFreeDays(REpiceaClimateVariableInformation resolution) {return frostDays;}

	@Override
	public double getLowestAnnualTemperatureCelsius(REpiceaClimateVariableInformation resolution) {return lowestTmin;}

	@Override
	public double getLatitudeDeg() {return latitudeDeg;}

	@Override
	public double getLongitudeDeg() {return longitudeDeg;}

	@Override
	public double getElevationM() {return 0;}

	@Override
	public double getAreaHa() {return 0.04;}

//	@Override
//	public List<OccupancyIndexCalculablePlot> getPlotsForOccupancyIndexCalculation() {
//		return plots;
//	}

	@Override
	public double getMeanMinimumJanuaryTemperatureCelsius(REpiceaClimateVariableInformation resolution) {
		return meanTminJanuary;
	}

	@Override
	public double getTotalPrecipitationFromMarchToMayMm(REpiceaClimateVariableInformation resolution) {
		return precMarchToMay;
	}

	@Override
	public double getTotalPrecipitationFromJuneToAugustMm(REpiceaClimateVariableInformation resolution) {
		return totalPrecJuneToAugust;
	}

	@Override
	public double getHighestAnnualTemperatureCelsius(REpiceaClimateVariableInformation resolution) {
		return highestTmax;
	}


	@Override
	public double getSlopeInclinationPercent() {
		return slopePct;
	}

	@Override
	public boolean isInterventionResult() {
		return interventionResult;
	}

	@Override
	public boolean isGoingToBeHarvested() {
		return isGoingToBeHarvested;
	}


	@Override
	public String getId() {return id;}

	@Override
	public double getMeanAnnualTemperatureCelsius(REpiceaClimateVariableInformation info) {
		return meanAnnualTemperature;
	}

	@Override
	public double getMeanMaximumJulyTemperatureCelsius(REpiceaClimateVariableInformation info) {
		return meanMaxJulyTemperature;
	}

	@Override
	public Object getOccupancyForThisSpecies(Enum<?> sp) {
		switch(mode) {
		case Known:
			return occupancyIndexMap.get(sp);
		case Estimated:
			return occIndexCalc.getOccupancyIndex(this, sp); 
		case Deviate:
			double deviate = occIndexCalc.getOccupancyIndex(this, sp).getRandomDeviate().getValueAt(0, 0);
			return deviate;
		default:
			throw new UnsupportedOperationException("The mode " + mode.name() + " is not supported!");
		}
	}
	
	Double getPred(Species sp) {
		return predMap.get(sp);
	}
	
	void setMode(Mode mode) {
		this.mode = mode;
	}

	@Override
	public double getMeanTemperatureFromJuneToAugustCelsius(REpiceaClimateVariableInformation arg0) {
		return meanTemperatureJulyToAugust;
	}
	
}
