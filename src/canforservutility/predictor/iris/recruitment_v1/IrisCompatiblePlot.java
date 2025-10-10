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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canforservutility.occupancyindex.OccupancyIndexCalculablePlot;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider;
import repicea.simulation.covariateproviders.plotlevel.GrowthStepLengthYrProvider;
import repicea.simulation.covariateproviders.plotlevel.SlopeInclinationPercentProvider;

/**
 * An interface that ensures the Tree instance is compatible with Iris modules.
 * @author Mathieu Fortin - June 2023
 */
public interface IrisCompatiblePlot extends GrowthStepLengthYrProvider,
											SlopeInclinationPercentProvider,
											DrainageGroupProvider,
											OccupancyIndexCalculablePlot {
	
	
	public static enum SoilDepth {
		Thick,
		Average,
		Shallow,
		VeryShallow;
		
		private static Map<SoilDepth, Matrix> DummyMap;
		
		private static Map<SoilDepth, Matrix> getDummyMap() {
			if (DummyMap == null) {
				DummyMap = new HashMap<SoilDepth, Matrix>();
				for (SoilDepth depth : SoilDepth.values()) {
					Matrix dummy = new Matrix(1, SoilDepth.values().length - 1);
					if (depth.ordinal() > 0) {
						dummy.setValueAt(0, depth.ordinal() - 1, 1d);
					}
					DummyMap.put(depth, dummy);
				}
			}
			return DummyMap;
		}

		public Matrix getDummyMatrix() {
			return (getDummyMap().get(this));
		}
	}
	
	public static enum DisturbanceType {
		Unknown,
		Fire,
		OtherNatural,
		Harvest;
		
		private static Map<DisturbanceType, Matrix> DummyMap;
		
		private static Map<DisturbanceType, Matrix> getDummyMap() {
			if (DummyMap == null) {
				DummyMap = new HashMap<DisturbanceType, Matrix>();
				for (DisturbanceType disturbance : DisturbanceType.values()) {
					Matrix dummy = new Matrix(1, DisturbanceType.values().length - 1);
					if (disturbance.ordinal() > 0) {
						dummy.setValueAt(0, disturbance.ordinal() - 1, 1d);
					}
					DummyMap.put(disturbance, dummy);
				}
			}
			return DummyMap;
		}

		public Matrix getDummyMatrix() {
			return (getDummyMap().get(this));
		}

	}

	public static enum SoilTexture {
		Crude,
		Mixed,
		Fine;
		
		private static Map<SoilTexture, Matrix> DummyMap;
		
		private static Map<SoilTexture, Matrix> getDummyMap() {
			if (DummyMap == null) {
				DummyMap = new HashMap<SoilTexture, Matrix>();
				for (SoilTexture texture : SoilTexture.values()) {
					Matrix dummy = new Matrix(1, SoilTexture.values().length - 1);
					if (texture.ordinal() > 0) {
						dummy.setValueAt(0, texture.ordinal() - 1, 1d);
					}
					DummyMap.put(texture, dummy);
				}
			}
			return DummyMap;
		}

		public Matrix getDummyMatrix() {
			return (getDummyMap().get(this));
		}

	}
	
	/**
	 * Return the mean degree-days over the period.
	 * @return a double
	 */
	public double getMeanDegreeDaysOverThePeriod(); 

	/**
	 * Return the mean precipitation (mm) over the period.
	 * @return a double
	 */
	public double getMeanPrecipitationOverThePeriod(); 

	/**
	 * Return the average annual number of frost days over the period.
	 * @return a double
	 */
	public double getMeanNumberFrostDaysOverThePeriod();
	
	/**
	 * Return the average lowest annual temperature over the period.
	 * @return a double
	 */
	public double getMeanLowestTemperatureOverThePeriod();

	/**
	 * Return the soil depth.
	 * @return a SoilDepth enum variable
	 */
	public SoilDepth getSoilDepth();
	
	/**
	 * Return the disturbance that have occurred in the previous interval
	 * @return a DisturbanceType enum variable
	 */
	public DisturbanceType getPastDisturbance();
	
	/**
	 * Return the disturbance that is going to occur in the upcoming interval.
	 * @return a DisturbanceType enum variable
	 */
	public DisturbanceType getUpcomingDisturbance();
		
	/**
	 * Returns the soil texture
	 * @return a SoilTexture enum variable
	 */
	public SoilTexture getSoilTexture();
		
	/**
	 * Return the basal area (m2/ha) of coniferous species.
	 * @return a double
	 */
	public double getBasalAreaOfConiferousSpecies();
	
	/**
	 * Return the basal area (m2/ha) of broaleaved species.
	 * @return a double
	 */
	public double getBasalAreaOfBroadleavedSpecies();
	
	/**
	 * Return the slope aspect in degree.
	 * @return a double
	 */
	public double getSlopeAspect();

	/**
	 * Return the list of plots to use to calculate the occupancy index.
	 * @return a List of IrisProtoPlot instances
	 */
	public List<OccupancyIndexCalculablePlot> getPlotsForOccupancyIndexCalculation();
}
