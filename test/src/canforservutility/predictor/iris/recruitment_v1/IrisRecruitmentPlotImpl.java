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
package canforservutility.predictor.iris.recruitment_v1;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import canforservutility.predictor.iris.recruitment_v1.IrisTree.IrisSpecies;
import repicea.simulation.climatemanagement.REpiceaClimateVariableInformation;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;

final class IrisRecruitmentPlotImpl implements IrisRecruitmentPlot {

	class Iris2020CompatibleTestTreeImpl implements IrisTree {
		
		final IrisSpecies species;
		
		Iris2020CompatibleTestTreeImpl(IrisSpecies species) {
			this.species = species;
		}
		
		@Override
		public double getBasalAreaLargerThanSubjectM2Ha() {return 0;}

		@Override
		public double getDbhCm() {return 0;}

		@Override
		public double getSquaredDbhCm() {return 0;}

		@Override
		public double getStemBasalAreaM2() {return 0;}

		@Override
		public double getLnDbhCm() {return 0;}

		@Override
		public String getSubjectId() {return null;}

		@Override
		public int getMonteCarloRealizationId() {return 0;}

		@Override
		public int getErrorTermIndex() {return 0;}

		@Override
		public IrisSpecies getSpecies() {return species;}
		
	}
		
	private final int growthStepLength;
	private final double basalAreaM2HaConiferous;
	private final double basalAreaM2HaBroadleaved;
	private final double slopeInclination;
	private final double slopeAspect;
	private final int dateYr;
	private final double dd;
	private final double prcp;
	private final SoilDepth soilDepth;
	private final DisturbanceType pastDist;
	private final DisturbanceType upcomingDist;
	private final DrainageGroup drainageGroup;
	private final SoilTexture soilTexture;
	private final String id;
//	private final IrisSpecies species;
	private final Map<IrisSpecies, Double> gSpGrMat;
	private final Map<IrisSpecies, Double> predMap;
	private final Map<IrisSpecies, Double> occupancyMap;
	private final double frostDays;
	private final double lowestTmin;
	private final double latitudeDeg;
	private final double longitudeDeg;
	private int monteCarloRealizationId = 0;
		
	IrisRecruitmentPlotImpl(String id,
			double latitudeDeg,
			double longitudeDeg,
			int growthStepLength,
			double basalAreaM2HaConiferous,
			double basalAreaM2HaBroadleaved,
			double slopeInclination,
			double slopeAspect,
			int dateYr,
			double dd,
			double prcp,
			double frostDays,
			double lowestTmin,
			SoilDepth soilDepth,
			DisturbanceType upcomingDist,
			DisturbanceType pastDist,
			DrainageGroup drainageGroup,
			SoilTexture soilTexture,
			IrisSpecies species,
			double gSpGr,
			double occupancyIndex,
			double pred) {
		if (drainageGroup == null) {
			throw new InvalidParameterException("The drainage group cannot be null!");
		}
		this.id = id;
		this.latitudeDeg = latitudeDeg;
		this.longitudeDeg = longitudeDeg;
		this.growthStepLength = growthStepLength;
		this.basalAreaM2HaConiferous = basalAreaM2HaConiferous;
		this.basalAreaM2HaBroadleaved = basalAreaM2HaBroadleaved;
		this.slopeInclination = slopeInclination;
		this.slopeAspect = slopeAspect;
		this.dateYr = dateYr;
		this.dd = dd;
		this.prcp = prcp;
		this.frostDays = frostDays;
		this.lowestTmin = lowestTmin;
		this.soilDepth = soilDepth;
		this.pastDist = pastDist;
		this.upcomingDist = upcomingDist;
		this.drainageGroup = drainageGroup;
		this.soilTexture = soilTexture;
		gSpGrMat = new HashMap<IrisSpecies, Double>();
		predMap = new HashMap<IrisSpecies, Double>();
		occupancyMap = new HashMap<IrisSpecies, Double>();
		updateMap(species, gSpGr, occupancyIndex, pred);
	}

	
	void updateMap(IrisSpecies species, double gSpGr, double occupancy, double pred) {
		gSpGrMat.put(species, gSpGr);
		occupancyMap.put(species, occupancy);
		predMap.put(species, pred);
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
	public double getSlopeInclinationPercent() {return slopeInclination;}

	@Override
	public int getDateYr() {return dateYr;}

	@Override
	public double getGrowingDegreeDaysCelsius(REpiceaClimateVariableInformation info) {return dd;}

	@Override
	public double getTotalAnnualPrecipitationMm(REpiceaClimateVariableInformation info) {return prcp;}

	@Override
	public SoilDepth getSoilDepth() {return soilDepth;}

	@Override
	public DrainageGroup getDrainageGroup() {return drainageGroup;}

	@Override
	public DisturbanceType getPastDisturbance() {return pastDist;}

	@Override
	public DisturbanceType getUpcomingDisturbance() {return upcomingDist;}

	@Override
	public SoilTexture getSoilTexture() {return soilTexture;}
	
	IrisTree getTreeInstance(IrisSpecies species) {
		return new Iris2020CompatibleTestTreeImpl(species); 
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
	public double getSlopeAspect() {return slopeAspect;}


	@Override
	public double getAnnualNbFrostDays(REpiceaClimateVariableInformation info) {return frostDays;}

	@Override
	public double getLowestAnnualTemperatureCelsius(REpiceaClimateVariableInformation info) {return lowestTmin;}

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
	public String getId() {return id;}


	@Override
	public Object getOccupancyForThisSpecies(Enum<?> sp) {
		return occupancyMap.get(sp);
	}

	double getPredForThisSpecies(Enum<?> sp) {
		return predMap.get(sp);
	}
}
