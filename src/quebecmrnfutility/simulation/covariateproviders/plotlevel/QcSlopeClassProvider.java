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
package quebecmrnfutility.simulation.covariateproviders.plotlevel;

import java.util.ArrayList;
import java.util.List;

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * This interface ensures that the stand or the plot instance can return its slope class 
 * according to the Quebec Ministry of Natural resources classification.
 * @author Mathieu Fortin - March 2017
 */
public interface QcSlopeClassProvider {

	public static enum QcSlopeClass implements TextableEnum {
		A("0-3%"),
		B("4-8%"),
		C("9-15%"),
		D("16-30%"),
		E("31-40%"),
		F(">40%"),
		S("Sommet");
		
		private static List<String> AvailableSlopeClasses;
		
		QcSlopeClass(String text) {
			setText(text,text);
		}
		
		public static boolean isThisClassRecognized(String slopeClassString) {
			if (AvailableSlopeClasses == null) {
				AvailableSlopeClasses = new ArrayList<String>();
				for (QcSlopeClass slopeClass : QcSlopeClass.values()) {
					AvailableSlopeClasses.add(slopeClass.name());
				}
			}
			return AvailableSlopeClasses.contains(slopeClassString);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	
	/**
	 * This method returns the slope class according to the Quebec Ministry of Natural resources classification.
	 * @return SlopeMRNFClass enum
	 */
	public QcSlopeClass getSlopeClass();
	
}
