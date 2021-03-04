/*
 * This file is part of the quebecmrnf-foresttools library
 *
 * Author Mathieu Fortin - Canadian Forest Service
 * Copyright (C) 2020 Her Majesty the Queen in right of Canada
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
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
package quebecmrnfutility.simulation.covariateproviders.treelevel;

import repicea.math.Matrix;

/**
 * This interface ensures the tree instance can provide its quality
 * class, i.e. A, B, C, D.
 * @author Mathieu Fortin - November 2012
 */
public interface QcTreeQualityProvider {

	/** 
	 * Tree log classification ABCD
	 * @author Mathieu Fortin - May 2010
	 */
	public static enum QcTreeQuality {
		A,
		B,
		C,
		D;
		
		private Matrix dummy;
		
		QcTreeQuality() {
			dummy = new Matrix(1,4);
			dummy.setValueAt(0, ordinal(), 1d);
		}
		
		public Matrix getDummy() {return dummy;}
	}

	/**
	 * This method returns the quality class of the stem according to the ABCD classification.
	 * @return an QcTreeQuality enum variable
	 */
	public QcTreeQuality getTreeQuality();

	
}
