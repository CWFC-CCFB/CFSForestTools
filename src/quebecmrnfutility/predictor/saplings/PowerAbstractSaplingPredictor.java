/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service,
 *         Hugues Power, Direction de la recherche forestiere du Quebec
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
package quebecmrnfutility.predictor.saplings;

import quebecmrnfutility.predictor.saplings.PowerSaplingBasalAreaAndDensityCompatiblePlot.CoverType;
import repicea.simulation.REpiceaPredictor;

@SuppressWarnings("serial")
abstract class PowerAbstractSaplingPredictor extends REpiceaPredictor {

	enum Effect {
		Intercept,
		Coupe,
		CouvertERS,
		CouvertSAB,
		G,
		Coupe_x_CouvertERS,
		Coupe_x_CouvertSAB,
		Coupe_x_G,
		CouvertERS_x_G,
		CouvertSAB_x_G
	}
	
	final boolean isVariabilityEnabled;

	
	PowerAbstractSaplingPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled); // no random effect in either of these models
		this.isVariabilityEnabled = isVariabilityEnabled;
	}


	void setXVector(PowerSaplingBasalAreaAndDensityCompatiblePlot plot) {
		oXVector.resetMatrix();
		double g = plot.getBasalAreaM2Ha();
		for (Effect e : Effect.values()) {
			switch (e) {
			case Intercept:
				oXVector.setValueAt(0, e.ordinal(), 1d);
				break;
			case Coupe:
				if (plot.isInterventionResult()) {
					oXVector.setValueAt(0, e.ordinal(), 1d);
				}
				break;
			case CouvertERS:
				if (plot.getCoverType() == CoverType.Maple) {
					oXVector.setValueAt(0, e.ordinal(), 1d);
				}
				break;
			case CouvertSAB:
				if (plot.getCoverType() == CoverType.Fir) {
					oXVector.setValueAt(0, e.ordinal(), 1d);
				}
				break;
			case G:
				oXVector.setValueAt(0, e.ordinal(), g);
				break;
			case Coupe_x_CouvertERS:
				if (plot.isInterventionResult() && plot.getCoverType() == CoverType.Maple) {
					oXVector.setValueAt(0, e.ordinal(), 1d);
				}
				break;
			case Coupe_x_CouvertSAB:
				if (plot.isInterventionResult() && plot.getCoverType() == CoverType.Fir) {
					oXVector.setValueAt(0, e.ordinal(), 1d);
				}
				break;
			case Coupe_x_G:
				if (plot.isInterventionResult()) {
					oXVector.setValueAt(0, e.ordinal(), g);
				}
				break;
			case CouvertERS_x_G:
				if (plot.getCoverType() == CoverType.Maple) {
					oXVector.setValueAt(0, e.ordinal(), g);
				}
				break;
			case CouvertSAB_x_G:
				if (plot.getCoverType() == CoverType.Fir) {
					oXVector.setValueAt(0, e.ordinal(), g);
				}
				break;
			}
		}
	}

}
