/*
 * This file is part of the mrnf-foresttools library
 *
 * Copyright (C) 2019 Mathieu Fortin - Canadian Wood Fibre Centre
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package canforservutility.biosim;

import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;

/**
 * An implementation of the GeographicalCoordinatesProvider interface.
 * @author Mathieu Fortin - December 2019
 */
public class BioSimPlot implements GeographicalCoordinatesProvider {

	private final double elevationM;
	private final double latitude;
	private final double longitude;

	public BioSimPlot(double latitudeDeg, double longitudeDeg, double elevationM) {
		this.latitude = latitudeDeg;
		this.longitude = longitudeDeg;
		this.elevationM = elevationM;
	}



	@Override
	public double getElevationM() {return elevationM;}

	@Override
	public double getLatitudeDeg() {return latitude;}

	@Override
	public double getLongitudeDeg() {return longitude;}


	@Override
	public String toString() {return latitude + "_" + longitude + "_" + elevationM;}

}
