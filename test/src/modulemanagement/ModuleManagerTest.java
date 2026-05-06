/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2026 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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
package modulemanagement;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import modulemanagement.SimulationModule.ModuleType;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;

public class ModuleManagerTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void testManager() {
		CFSModuleManager manager = CFSModuleManager.getInstance();
		List classes = manager.getModules(SpeciesLocale.Quebec);
		Assert.assertEquals("Testing the number of modules compatible with Quebec", 24, classes.size());
		classes = manager.getModules(ModuleType.DiameterIncrement, SpeciesLocale.Ontario);
		Assert.assertEquals("Testing the number of diameter increment modules compatible with Ontario", 2, classes.size());
		classes = manager.getModules(ModuleType.DiameterIncrement);
		Assert.assertEquals("Testing the number of diameter increment modules compatible with Ontario", 5, classes.size());
	}
}
