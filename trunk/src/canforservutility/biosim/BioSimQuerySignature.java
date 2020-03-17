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
 * Internal class for storing generated climate.
 * @author Mathieu Fortin - December 2019
 */
class BioSimQuerySignature {
	
	final int initialYear;
	final int finalYear;
//	final List<Variable> variables;
	final double latitudeDeg;
	final double longitudeDeg;
	final double elevationM;
	
	BioSimQuerySignature(int initialYear, int finalYear, GeographicalCoordinatesProvider location) {
		this.initialYear = initialYear;
		this.finalYear = finalYear;
//		this.variables = new ArrayList<Variable>();
//		this.variables.addAll(variables);
		this.latitudeDeg = location.getLatitudeDeg();
		this.longitudeDeg = location.getLongitudeDeg();
		this.elevationM = location.getElevationM();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BioSimQuerySignature) {
			BioSimQuerySignature thatQuery = (BioSimQuerySignature) obj;
			if (thatQuery.initialYear == initialYear) {
				if (thatQuery.finalYear == finalYear) {
//					if (thatQuery.variables.equals(variables)) {
						if (thatQuery.latitudeDeg == latitudeDeg) {
							if (thatQuery.longitudeDeg == longitudeDeg) {
								if (thatQuery.elevationM == elevationM) {
									return true;
								}
							}
						}
//					}
				}
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
//		return initialYear * 10000000 + finalYear + variables.hashCode();
		return initialYear * 10000000 + finalYear;
	}

}
