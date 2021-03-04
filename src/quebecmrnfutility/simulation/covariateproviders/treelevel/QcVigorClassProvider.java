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
 * This interface ensures the tree instance can provide its vigor 
 * class, i.e. I, II, III, IV.
 * @author Mathieu Fortin - November 2012
 */
public interface QcVigorClassProvider {

	/**
	 * Vigour class according to Majcen (1990).
	 * @author Mathieu Fortin - May 2010
	 */
	public static enum QcVigorClass {
		V1,
		V2,
		V3,
		V4;
		
		private Matrix dummy;
		private Matrix dummyVig;
		private Matrix dummyProd;
		
		QcVigorClass() {
			dummy = new Matrix(1,4);
			dummy.setValueAt(0, ordinal(), 1d);
			
			dummyVig = new Matrix(1,2);
			if (this.ordinal() == 2 || this.ordinal() == 3) {		// non vigourous
				dummyVig.setValueAt(0, 0, 1d);
			} else {												// vigourous
				dummyVig.setValueAt(0, 1, 1d);
			}

			dummyProd = new Matrix(1,2);
			if (this.ordinal() == 1 || this.ordinal() == 3) {		// pulp and paper
				dummyProd.setValueAt(0, 0, 1d);
			} else {												// sawlog potential
				dummyProd.setValueAt(0, 1, 1d);
			}

		}
		
		public Matrix geDummy() {return dummy;}
		public Matrix geDummyVig() {return dummyVig;}
		public Matrix geDummyProd() {return dummyProd;}
		
	}

	/**
	 * This method returns the vigor class for braodleaved stems according to Majcen's system. 
	 * @return a VigorClass enum variable
	 */
	public QcVigorClass getVigorClass();

}
