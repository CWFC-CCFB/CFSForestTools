/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge-Epicea
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
package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import org.junit.Assert;
import org.junit.Test;


public class QuebecMRNFGenericTreatmentTest {

	@Test
	public void generatingQuebecMRNFGenericTreatmentFromText() {
		String allo = "quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModel$TreatmentType.CJMSCR;50";
		QuebecMRNFGenericTreatment test = QuebecMRNFGenericTreatment.getGenericTreatmentFromCompleteName(allo);
		Assert.assertTrue("Testing if this instance is non null", test != null);
	}
	
	
}
