/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2020-2025 His Majesty the King in right of Canada
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
package canforservutility.occupancyindex;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of the OccupancyIndexCalculablePlot interface.
 * 
 * @author Mathieu Fortin - October 2025
 */
public class SimpleOccupancyIndexCalculablePlot implements OccupancyIndexCalculablePlot {

	private final String id;
	private final double latitudeDeg;
	private final double longitudeDeg;
	private final int dateYr;
	private final Map<Enum<?>, Double> baHaSpeciesMap;
//	private final double baHaSpecies;
//	double distanceKm;

	
	/**
	 * Basic constructor.
	 * @param id plotId
	 * @param latitudeDeg latitude
	 * @param longitudeDeg longitude
	 * @param dateYr date of measurement
	 */
	protected SimpleOccupancyIndexCalculablePlot(String id, 
			double latitudeDeg, 
			double longitudeDeg,
			int dateYr) {
		this.id = id;
		this.latitudeDeg = latitudeDeg;
		this.longitudeDeg = longitudeDeg; 
		this.dateYr = dateYr;
		this.baHaSpeciesMap = new HashMap<Enum<?>, Double>();
	}

	/**
	 * Constructor for a single species.
	 * @param id plotId
	 * @param latitudeDeg latitude
	 * @param longitudeDeg longitude
	 * @param dateYr date of measurement
	 * @param species an Enum variable standing for the species
	 * @param baHaSpecies the species basal area in the plot (m2/ha)
	 */
	public SimpleOccupancyIndexCalculablePlot(String id, 
			double latitudeDeg, 
			double longitudeDeg,
			int dateYr,
			Enum<?> species,
			double baHaSpecies) {
		this(id, latitudeDeg, longitudeDeg, dateYr);
		setBasalArea(species, baHaSpecies);
	}

	/**
	 * Set a basal area entry for a particular species.
	 * @param species an Enum variable standing for the species
	 * @param baHaSpecies the species basal area in the plot (m2/ha)
	 */
	public void setBasalArea(Enum<?> species, double baHaSpecies) {
		baHaSpeciesMap.put(species, baHaSpecies);
	}
	
	/**
	 * Constructor for a single species.
	 * @param id plotId
	 * @param latitudeDeg latitude
	 * @param longitudeDeg longitude
	 * @param dateYr date of measurement
	 * @param speciesBasalAreaMap a Map with Species enum as key and basal area (m2/ha) as value
	 */
	public SimpleOccupancyIndexCalculablePlot(String id, 
			double latitudeDeg, 
			double longitudeDeg,
			int dateYr,
			Map<Enum<?>,Double> speciesBasalAreaMap) {
		this(id, latitudeDeg, longitudeDeg, dateYr);
		if (speciesBasalAreaMap == null || speciesBasalAreaMap.isEmpty()) {
			throw new InvalidParameterException("The speciesBasalAreaMap argument should be a non empty map!");
		}
		baHaSpeciesMap.putAll(speciesBasalAreaMap);
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
	public double getBasalAreaM2HaForThisSpecies(Enum<?> species) {
		return baHaSpeciesMap.get(species);
	}

	@Override 
	public double getAreaHa() {return 0.04;}
}
