/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2024 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
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
package canforservutility.predictor.ungetal2009;

import org.junit.Assert;
import org.junit.Test;

import canforservutility.predictor.ungetal2009.UngEtAl2009Predictor.UngEtAl2009Species;
import repicea.stats.data.DataSet;

public class UngEtAl2009PredictorTest {


	@Test
	public void simpleTest() {
		DataSet pred = new UngEtAl2009Predictor().predictPlotStandingVolumeUsingNat1ModelWithClimateVariables(UngEtAl2009Species.PICEA_MARIANA, 60, 2469.2833333333333, 952.1);
		double vol = (double) pred.getValueAt(pred.getNumberOfObservations() - 1, "MerchVolumeM3Ha");
		Assert.assertEquals("Testing predicted volume with Nat1", 100.43953407504378, vol, 1E-8);
		
		DataSet pred2 = new UngEtAl2009Predictor().predictPlotStandingVolumeUsingNat2ModelWithClimateVariables(UngEtAl2009Species.PICEA_MARIANA, 60, 2469.2833333333333, 952.1);
		double vol2 = (double) pred2.getValueAt(pred2.getNumberOfObservations() - 1, "MerchVolumeM3Ha");
		Assert.assertEquals("Testing predicted volume with Nat2", 107.93182642518205, vol2, 1E-8);
	}
	

}
