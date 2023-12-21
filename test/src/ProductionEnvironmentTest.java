/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * Author: Jean-Francois Lavoie
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
import org.junit.Assert;
import org.junit.Test;

import canforservutility.predictor.biomass.lambert2005.Lambert2005BiomassPredictor;


public class ProductionEnvironmentTest {
	@Test 
	
	/**
	 * This test will fail when used with a local class folder, but will succeed when running on a JAR file.
	 * This test is excluded from the normal local tests and executed only with the integration tests from the JAR file
	 */
	public void makeSurePackageIsRunningFromJAR() {
		String resourceURL = Lambert2005BiomassPredictor.class.getResource("Lambert2005BiomassPredictor.class").toString();
		Assert.assertTrue(resourceURL.startsWith("jar:"));
	}
}
