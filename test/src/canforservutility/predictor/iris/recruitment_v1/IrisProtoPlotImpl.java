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

import canforservutility.predictor.iris.recruitment_v1.IrisCompatibleTree.IrisSpecies;

public class IrisProtoPlotImpl implements IrisProtoPlot {

	private final String id;
	private final double latitudeDeg;
	private final double longitudeDeg;
	private final double weight;
	private final int dateYr;
	private final double baHaSpecies;
	double distanceKm;
	
	public IrisProtoPlotImpl(String id, 
			double latitudeDeg, 
			double longitudeDeg,
			double weight,
			int dateYr,
			double baHaSpecies) {
		this.id = id;
		this.latitudeDeg = latitudeDeg;
		this.longitudeDeg = longitudeDeg; 
		this.weight = weight;
		this.dateYr = dateYr;
		this.baHaSpecies = baHaSpecies;
	}
	
	@Override
	public String getSubjectId() {return id;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public double getLatitudeDeg() {
		return latitudeDeg;
	}

	@Override
	public double getLongitudeDeg() {
		return longitudeDeg;
	}

	@Override
	public double getElevationM() {return 0;}

	@Override
	public int getDateYr() {return dateYr;}

	@Override
	public void setWeight(double weight) {}

	@Override
	public double getWeight() {return weight;}
	
	@Override
	public double getBasalAreaM2HaForThisSpecies(IrisSpecies species) {return baHaSpecies;}

	@Override 
	public double getAreaHa() {return 0.04;}
}
