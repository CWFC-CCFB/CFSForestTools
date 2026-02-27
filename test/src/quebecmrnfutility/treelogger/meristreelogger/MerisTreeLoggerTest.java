/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
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
package quebecmrnfutility.treelogger.meristreelogger;

import java.util.Collection;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import quebecmrnfutility.treelogger.meristreelogger.MerisTreeLoggerParameters.MerisTypeMatrix;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.treelogger.WoodPiece;
import repicea.util.ObjectUtility;

public class MerisTreeLoggerTest {

	static class MerisLoggableTreeImpl implements MerisLoggableTree {

		final double volumeM3;
		final String speciesName;
		final double dbhCm;
		
		
		MerisLoggableTreeImpl(double volumeM3, String speciesName, double dbhCm) {
			this.volumeM3 = volumeM3;
			this.speciesName = speciesName;
			this.dbhCm = dbhCm;
		}
		
		@Override
		public double getCommercialVolumeM3() {return volumeM3;}

		@Override
		public boolean isCommercialVolumeOverbark() {return false;}

		@Override
		public String getSpeciesName() {return speciesName;}

		@Override
		public double getDbhCm() {return dbhCm;}

		@Override
		public double getBarkProportionOfWoodVolume(SpeciesLocale locale) {
			return 0.08;
		}

		@Override
		public SpeciesLocale getSpeciesLocale() {
			return SpeciesLocale.Quebec;
		}
		
	}
	
	static MerisTreeLogger Singleton;
	
	@BeforeClass
	public static void startup() {
		Singleton = new MerisTreeLogger();
		Singleton.setTreeLoggerParameters(null);
	}
	
	@Test
	public void test01SimpleTreeHappyPath() {
		MerisLoggableTree tree = new MerisLoggableTreeImpl(1d, "EPB", 13.05);
		Singleton.logThisTree(tree);
		Collection<WoodPiece> woodPieces = Singleton.getWoodPieces().get(tree);
		Assert.assertTrue("Testing that wood piece collections is not empty", !woodPieces.isEmpty());
		double totalVolumeWoodM3 = 0d;
		double totalVolumeBarkM3 = 0d;
		for (WoodPiece wp : woodPieces) {
			totalVolumeWoodM3 += wp.getWoodVolumeM3();
			totalVolumeBarkM3 += wp.getBarkVolumeM3();
		}
		Assert.assertEquals("Testing bark volume", 0.08, totalVolumeBarkM3, 1E-8);
		Assert.assertEquals("Testing wood volume", 1d, totalVolumeWoodM3, 1E-8);
	}
	
	@Test
	public void test02ImportFromFileHappyPath() {
		MerisTreeLoggerParameters parms = Singleton.getTreeLoggerParameters();
		String filename = ObjectUtility.getPackagePath(getClass()) + "Matrice_DAEF_exemple.csv";
		MerisTypeMatrix newMatrix = parms.readFromFile(filename);
		parms.currentMatrix.replaceBy(newMatrix);
		Map<?,?> m = parms.getLogCategories();
		Assert.assertEquals("Testing number of species", 2, m.size());
		parms.initializeDefaultLogCategories();
		m = parms.getLogCategories();
		Assert.assertEquals("Testing number of species", 16, m.size());
	}

}
