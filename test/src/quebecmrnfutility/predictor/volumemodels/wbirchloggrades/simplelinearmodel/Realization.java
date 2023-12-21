/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package quebecmrnfutility.predictor.volumemodels.wbirchloggrades.simplelinearmodel;

class Realization {
	final double trueTau;
	final double estTau;
	final double estVarianceUncorr;
	final double estVarianceCorr;
	final double samplingPart;
	final double modelPart;
	
	Realization(double trueTau, double estTau, double estVarianceUncorr, double estVarianceCorr, double samplingPart, double modelPart) {
		this.trueTau = trueTau;
		this.estTau = estTau;
		this.estVarianceUncorr = estVarianceUncorr;
		this.estVarianceCorr = estVarianceCorr;
		this.samplingPart = samplingPart;
		this.modelPart = modelPart;
	}
	
	Object[] getRecord() {
		Object[] record = new Object[6];
		record[0] = trueTau;
		record[1] = estTau;
		record[2] = estVarianceUncorr;
		record[3] = estVarianceCorr;
		record[4] = samplingPart;
		record[5] = modelPart;
		return record;
	}
	
	
}
