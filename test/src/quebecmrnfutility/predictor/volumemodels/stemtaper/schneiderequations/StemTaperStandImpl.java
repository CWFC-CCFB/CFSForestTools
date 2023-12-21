/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec
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
package quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations;

class StemTaperStandImpl implements StemTaperStand {

	double gHa;
	double stemHa;
	
	protected StemTaperStandImpl(double gHa, double stemHa) {
		this.gHa = gHa;
		this.stemHa = stemHa;
	}
	
		
	@Override
	public String getSubjectId() {
		return ((Integer) hashCode()).toString();
	}

	@Override
	public String getEcologicalType() {
		return "MS2";
	}

	@Override
	public String getEcoRegion() {
		return "3a";
	}

	@Override
	public QcDrainageClass getDrainageClass() {
		return QcDrainageClass.C3;
	}

	@Override
	public double getElevationM() {
		return 300;
	}

	@Override
	public double getNumberOfStemsHa() {
		return stemHa;
	}

	@Override
	public double getBasalAreaM2Ha() {
		return gHa;
	}



	@Override
	public int getMonteCarloRealizationId() {
		return 0;
	}

}
