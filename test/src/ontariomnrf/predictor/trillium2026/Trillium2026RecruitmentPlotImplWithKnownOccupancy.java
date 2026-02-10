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

import java.util.List;

import canforservutility.occupancyindex.OccupancyIndexCalculablePlot;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.climate.REpiceaClimate.ClimateVariableTemporalResolution;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.simulation.species.REpiceaSpecies.Species;

final class Trillium2026RecruitmentPlotImplWithKnownOccupancy implements Trillium2026RecruitmentPlot,
																		Trillium2026RecruitmentPlotWithKnownOccupancy {

	class Trillium2026TestTreeImpl implements Trillium2026Tree {
		
		final Species species;
		
		Trillium2026TestTreeImpl(Species species) {
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
		
	private final double growthStepLength;
	private final double basalAreaM2HaConiferous;
	private final double basalAreaM2HaBroadleaved;
	private final int dateYr;
	private final double dd;
	private final double prcp;
	private final double pred;
	private final String id;
	final Species species;
	private final Matrix gSpGrMat;
	private final double occIndex25km;
	private final double frostDays;
	private final double lowestTmin;
	private final double meanTminJanuary;
	private final double totalPrecMarchToMay;
		
	Trillium2026RecruitmentPlotImplWithKnownOccupancy(String id,
			double growthStepLength,
			double basalAreaM2HaConiferous,
			double basalAreaM2HaBroadleaved,
			int dateYr,
			double dd,
			double prcp,
			double frostDays,
			double lowestTmin,
			double meanTminJanuary,
			double totalPrecMarchToMay,
			Species species,
			double pred, 
			double gSpGr,
			double occIndex25km) {
		this.id = id;
		this.growthStepLength = growthStepLength;
		this.basalAreaM2HaConiferous = basalAreaM2HaConiferous;
		this.basalAreaM2HaBroadleaved = basalAreaM2HaBroadleaved;
		this.dateYr = dateYr;
		this.dd = dd;
		this.prcp = prcp;
		this.frostDays = frostDays;
		this.lowestTmin = lowestTmin;
		this.meanTminJanuary = meanTminJanuary;
		this.totalPrecMarchToMay = totalPrecMarchToMay;
		this.species = species;
		this.pred = pred;
		gSpGrMat = new Matrix(1, Trillium2026RecruitmentOccurrencePredictor.SpeciesList.size());
		gSpGrMat.setValueAt(0, Trillium2026RecruitmentOccurrencePredictor.SpeciesList.indexOf(species), gSpGr);
		this.occIndex25km = occIndex25km;
	}
	
	

	@Override
	public String getSubjectId() {return id;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public double getGrowthStepLengthYr() {return growthStepLength;}

	@Override
	public int getDateYr() {return dateYr;}

	@Override
	public double getGrowingDegreeDaysCelsius(ClimateVariableTemporalResolution resolution) {return dd;}

	@Override
	public double getTotalAnnualPrecipitationMm(ClimateVariableTemporalResolution resolution)  {return prcp;}

	double getPredProb() {return pred;}

	Trillium2026Tree getTreeInstance() {
		return new Trillium2026TestTreeImpl(species); 
	}

	@Override
	public double getBasalAreaM2HaForThisSpecies(Enum<?> species) {
		return gSpGrMat.getValueAt(0, Trillium2026RecruitmentOccurrencePredictor.SpeciesList.indexOf(species));
	}

	@Override
	public double getBasalAreaM2HaForThisSpeciesType(SpeciesType type) {
		return type == SpeciesType.ConiferousSpecies ?
				basalAreaM2HaConiferous :
					basalAreaM2HaBroadleaved;
	}

	@Override
	public double getAnnualNbFrostFreeDays(ClimateVariableTemporalResolution resolution) {return frostDays;}

	@Override
	public double getLowestAnnualTemperatureCelsius(ClimateVariableTemporalResolution resolution) {return lowestTmin;}

	@Override
	public double getOccupancyIndex25km(Species species) {return occIndex25km;}

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

	@Override
	public double getMeanMinimumJanuaryTemperatureCelsius(ClimateVariableTemporalResolution resolution) {
		return meanTminJanuary;
	}

	@Override
	public double getTotalPrecipitationFromMarchToMayMm(ClimateVariableTemporalResolution resolution) {
		return totalPrecMarchToMay;
	}
	
}
