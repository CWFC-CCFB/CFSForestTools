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
import java.util.List;

import canforservutility.occupancyindex.OccupancyIndexCalculablePlot;
import canforservutility.predictor.iris.recruitment_v1.IrisCompatibleTree.IrisSpecies;
import repicea.math.Matrix;
import repicea.simulation.climate.REpiceaClimate.ClimateVariableTemporalResolution;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;

final class IrisCompatibleTestPlotImpl implements IrisCompatiblePlot {

	class Iris2020CompatibleTestTreeImpl implements IrisCompatibleTree {
		
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
	
	
	
	private final double growthStepLength;
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
	private final double pred;
	private final String id;
	private final IrisSpecies species;
	private final Matrix gSpGrMat;
	private final double occIndex10km;
	private final double frostDays;
	private final double lowestTmin;
		
	IrisCompatibleTestPlotImpl(String id,
			double growthStepLength,
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
			double pred, 
			double gSpGr,
			double occIndex10km) {
		if (drainageGroup == null) {
			throw new InvalidParameterException("The drainage group cannot be null!");
		}
		this.id = id;
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
		this.species = species;
		this.pred = pred;
		gSpGrMat = new Matrix(1, IrisSpecies.values().length);
		gSpGrMat.setValueAt(0, species.ordinal(), gSpGr);
		this.occIndex10km = occIndex10km;
	}
	
	

	@Override
	public String getSubjectId() {return id;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public double getGrowthStepLengthYr() {return growthStepLength;}

	@Override
	public double getSlopeInclinationPercent() {return slopeInclination;}

	@Override
	public int getDateYr() {return dateYr;}

	@Override
	public double getGrowingDegreeDaysCelsius(ClimateVariableTemporalResolution resolution) {return dd;}

	@Override
	public double getTotalAnnualPrecipitationMm(ClimateVariableTemporalResolution resolution)  {return prcp;}

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
	
	double getPredProb() {return pred;}

	IrisCompatibleTree getTreeInstance() {
		return new Iris2020CompatibleTestTreeImpl(species); 
	}

	@Override
	public double getBasalAreaM2HaForThisSpecies(Enum<?> species) {return gSpGrMat.getValueAt(0, species.ordinal());}

	@Override
	public double getBasalAreaM2HaForThisSpeciesType(SpeciesType type) {
		return type == SpeciesType.ConiferousSpecies ?
				basalAreaM2HaConiferous :
					basalAreaM2HaBroadleaved;
	}

	@Override
	public double getSlopeAspect() {return slopeAspect;}

	@Override
	public double getAnnualNbFrostFreeDays(ClimateVariableTemporalResolution resolution) {return frostDays;}

	@Override
	public double getLowestAnnualTemperatureCelsius(ClimateVariableTemporalResolution resolution) {return lowestTmin;}

	protected double getOccupancyIndex10km(IrisSpecies species) {return occIndex10km;}

	@Override
	public double getLatitudeDeg() {return 0;}

	@Override
	public double getLongitudeDeg() {return 0;}

	@Override
	public double getElevationM() {return 0;}

	@Override
	public double getAreaHa() {return 0.04;}

	@Override
	public List<OccupancyIndexCalculablePlot> getPlotsForOccupancyIndexCalculation() {
		return null;
	}
	
}
