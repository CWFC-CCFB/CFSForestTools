/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2020-2021 Her Majesty the Queen in right of Canada
 * author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package canforservutility.predictor.iris2020.recruitment;

import java.security.InvalidParameterException;

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatibleTree.Iris2020Species;
import repicea.math.Matrix;

public class Iris2020CompatibleTestPlotImpl implements Iris2020CompatiblePlot {

	class Iris2020CompatibleTestTreeImpl implements Iris2020CompatibleTree {
		
		final Iris2020Species species;
		
		Iris2020CompatibleTestTreeImpl(Iris2020Species species) {
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
		public Iris2020Species getSpecies() {return species;}
		
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
	private final OriginType upcomingOrigin;
	private final OriginType pastOrigin;
	private final DrainageGroup drainageGroup;
	private final SoilTexture soilTexture;
	private final double pred;
	private final String id;
	private final Iris2020Species species;
	private final Matrix gSpGrMat;
	private final double distanceToConspecific;
	private final double frostDays;
	private final double lowestTmin;
		
	Iris2020CompatibleTestPlotImpl(String id,
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
			OriginType upcomingOrigin,
			OriginType pastOrigin,
			DisturbanceType upcomingDist,
			DisturbanceType pastDist,
			DrainageGroup drainageGroup,
			SoilTexture soilTexture,
			Iris2020Species species,
			double pred, 
			double gSpGr,
			double distanceToConspecific) {
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
		this.upcomingOrigin = upcomingOrigin;
		this.pastOrigin = pastOrigin;
		this.drainageGroup = drainageGroup;
		this.soilTexture = soilTexture;
		this.species = species;
		this.pred = pred;
		gSpGrMat = new Matrix(1, Iris2020Species.values().length);
		gSpGrMat.setValueAt(0, species.ordinal(), gSpGr);
		this.distanceToConspecific = distanceToConspecific;
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
	public double getMeanDegreeDaysOverThePeriod() {return dd;}

	@Override
	public double getMeanPrecipitationOverThePeriod() {return prcp;}

	@Override
	public SoilDepth getSoilDepth() {return soilDepth;}

	@Override
	public DrainageGroup getDrainageGroup() {return drainageGroup;}

	@Override
	public DisturbanceType getPastPartialDisturbance() {return pastDist;}

	@Override
	public DisturbanceType getUpcomingPartialDisturbance() {return upcomingDist;}

	@Override
	public OriginType getUpcomingStandReplacementDisturbance() {return upcomingOrigin;}

	@Override
	public SoilTexture getSoilTexture() {return soilTexture;}
	
	double getPredProb() {return pred;}

	Iris2020CompatibleTree getTreeInstance() {
		return new Iris2020CompatibleTestTreeImpl(species); 
	}

	@Override
	public double getBasalAreaM2HaForThisSpecies(Iris2020Species species) {return gSpGrMat.getValueAt(0, species.ordinal());}



	@Override
	public double getBasalAreaOfConiferousSpecies() {return basalAreaM2HaConiferous;}

	@Override
	public double getBasalAreaOfBroadleavedSpecies() {return basalAreaM2HaBroadleaved;}

	@Override
	public double getSlopeAspect() {return slopeAspect;}


	@Override
	public double getMeanNumberFrostDaysOverThePeriod() {return frostDays;}

	@Override
	public double getMeanLowestTemperatureOverThePeriod() {return lowestTmin;}

	@Override
	public OriginType getPastStandReplacementDisturbance() {return pastOrigin;}

	@Override
	public double getDistanceToConspecificKm(Iris2020Species species) {return distanceToConspecific;}

	@Override
	public double getLatitudeDeg() {return 0;}

	@Override
	public double getLongitudeDeg() {return 0;}

	@Override
	public double getElevationM() {return 0;}

	@Override
	public void setWeight(double weight) {}

	@Override
	public double getAreaHa() {return 0.04;}
	
}
