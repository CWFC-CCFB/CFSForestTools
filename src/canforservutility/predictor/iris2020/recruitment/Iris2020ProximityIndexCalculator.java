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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import canforservutility.predictor.iris2020.recruitment.Iris2020CompatibleTree.Iris2020Species;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.geographic.GeographicDistanceCalculator;
import repicea.stats.QuantileUtility;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.SimpleEstimate;

public class Iris2020ProximityIndexCalculator {

	private final SymmetricMatrix distances;
	private final double maximumDistance;
	private final List<String> plotsId;
	private final int minYearDiff = 0;
	private final int maxYearDiff = 10;
	
	/**
	 * Constructor.
	 * @param plots a List of Iris2020CompatiblePlot instances
	 */
	public Iris2020ProximityIndexCalculator(List<Iris2020CompatiblePlot> plots) {
		plotsId = new ArrayList<String>();
		Matrix latitudes = new Matrix(plots.size(), 1);
		Matrix longitudes = new Matrix(plots.size(), 1);
		
		for (int i = 0; i < plots.size(); i++) {
			Iris2020CompatiblePlot p = plots.get(i);
			if (!plotsId.contains(p.getSubjectId())) {
				plotsId.add(p.getSubjectId());
			} else {
				throw new InvalidParameterException("Some plots have the same ids!");
			}
			latitudes.setValueAt(i, 0, p.getLatitudeDeg());
			longitudes.setValueAt(i, 0, p.getLongitudeDeg());
		}
		distances = GeographicDistanceCalculator.getDistanceBetweenTheseCoordinates(latitudes, longitudes);
		double maxDist = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < distances.m_iRows; i++) {
			for (int j = i; j < distances.m_iRows; j++) {
				if (distances.getValueAt(i, j) > maxDist) {
					maxDist = distances.getValueAt(i, j);
				}
			}
		}
		maximumDistance = maxDist;
	}

	/**
	 * Return the distance between two plots.
	 * 
	 * @param plot1 an Iris2020CompatiblePlot instance
	 * @param plot2 an Iris2020CompatiblePlot instance 
	 * @return the distance (km)
	 */
	public double getDistanceKmBetweenThesePlots(Iris2020ProtoPlot plot1, Iris2020ProtoPlot plot2) {
		int index1 = plotsId.indexOf(plot1.getSubjectId());
		if (index1 == -1) {
			throw new InvalidParameterException("The plot1 argument is not found in the plot list!");
		}
		int index2 = plotsId.indexOf(plot2.getSubjectId());
		if (index2 == -1) {
			throw new InvalidParameterException("The plot2 argument is not found in the plot list!");
		}
		return distances.getValueAt(index1, index2);
	}
		
	
	public Estimate getProximityIndex(List<Iris2020ProtoPlot> plots, Iris2020ProtoPlot thisPlot, Iris2020Species species) {
		List<Iris2020ProtoPlot> plotsWithConspecific = plots.stream().
				filter(p -> p.getBasalAreaM2HaForThisSpecies(species) > 0d).
				collect(Collectors.toList());
		List<Iris2020ProtoPlot> plotsWithConspecificWithinLast10Yrs = plotsWithConspecific.stream().
				filter(p -> thisPlot.getDateYr() - p.getDateYr() >= minYearDiff && thisPlot.getDateYr() - p.getDateYr() <= maxYearDiff).
				collect(Collectors.toList());
		if (plotsWithConspecificWithinLast10Yrs.isEmpty()) {
			Matrix mean = new Matrix(1,1, maximumDistance + 1, 0);
			SymmetricMatrix variance = new SymmetricMatrix(1);
			return new SimpleEstimate(mean, variance);
		} else {
			List<Double> distances = plotsWithConspecificWithinLast10Yrs.stream().
					map(p -> getDistanceKmBetweenThesePlots(thisPlot, p)).
					collect(Collectors.toList());
			List<Double> weights = plotsWithConspecificWithinLast10Yrs.stream().
					map(p -> p.getWeight()).
					collect(Collectors.toList());
			return QuantileUtility.getQuantileEstimateFromSample(distances, 0.01, weights, 100);
		}
		
	}
	
	
	
}
