/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2020-2022 Her Majesty the Queen in right of Canada
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

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatibleTree.Iris2020Species;

public class Iris2020ProtoPlotImpl implements Iris2020ProtoPlot {

	private final String id;
	private final double latitudeDeg;
	private final double longitudeDeg;
	private final double weight;
	private final int dateYr;
	private final double baHaSpecies;
	double distanceKm;
	
	public Iris2020ProtoPlotImpl(String id, 
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
	public double getBasalAreaM2HaForThisSpecies(Iris2020Species species) {return baHaSpecies;}

	@Override 
	public double getAreaHa() {return 0.04;}
}
