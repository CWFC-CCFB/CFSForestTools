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

import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.DateYrProvider;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider;
import repicea.simulation.covariateproviders.plotlevel.GrowthStepLengthYrProvider;
import repicea.simulation.covariateproviders.plotlevel.SlopeInclinationPercentProvider;

public interface Iris2020CompatiblePlot extends 	MonteCarloSimulationCompliantObject,
													GrowthStepLengthYrProvider,
													SlopeInclinationPercentProvider,
													DrainageGroupProvider,
													DateYrProvider {
	
	
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
	
	public static enum OriginType {
		Unknown,
		Fire,
		OtherNatural,
		Harvest;
		
		private static Map<OriginType, Matrix> DummyMap;
		
		private static Map<OriginType, Matrix> getDummyMap() {
			if (DummyMap == null) {
				DummyMap = new HashMap<OriginType, Matrix>();
				for (OriginType origin : OriginType.values()) {
					Matrix dummy = new Matrix(1, OriginType.values().length - 1);
					if (origin.ordinal() > 0) {
						dummy.setValueAt(0, origin.ordinal() - 1, 1d);
					}
					DummyMap.put(origin, dummy);
				}
			}
			return DummyMap;
		}

		public Matrix getDummyMatrix() {
			return (getDummyMap().get(this));
		}

	}

	public static enum DisturbanceType {
		None,
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
	
	@Override
	default public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}
	
	/**
	 * Returns the mean degree-days over the period.
	 * @return a double
	 */
	public double getMeanDegreeDaysOverThePeriod(); 

	/**
	 * Returns the mean precipitation (mm) over the period.
	 * @return a double
	 */
	public double getMeanPrecipitationOverThePeriod(); 

	/**
	 * Returns the soil depth.
	 * @return a SoilDepth enum variable
	 */
	public SoilDepth getSoilDepth();
	
	/**
	 * Returns the disturbance that have occurred recently (e.g. in the last 15 years or so)
	 * @return a DisturbanceType enum variable
	 */
	public DisturbanceType getPastDisturbance();
	
	/**
	 * Returns the disturbance that will occurred in the upcoming growth interval.
	 * @return a DisturbanceType enum variable
	 */
	public DisturbanceType getUpcomingDisturbance();
	
	/**
	 * Returns the origin of the stand 
	 * @return a OriginType enum variable
	 */
	public OriginType getOrigin();

	
	/**
	 * Returns the soil texture
	 * @return a SoilTexture enum variable
	 */
	public SoilTexture getSoilTexture();
	
	/**
	 * Return a Matrix with the basal area (m2/ha) for each species or species group. 
	 * The index of the group corresponds to the ordinal of the Iris2020Species enum
	 * variable.
	 * @return a 1x33 Matrix
	 */
	public Matrix getBasalAreaM2HaBySpecies();
	
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
	
}
