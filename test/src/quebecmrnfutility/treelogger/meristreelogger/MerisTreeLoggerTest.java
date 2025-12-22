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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import repicea.simulation.treelogger.WoodPiece;

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
		public boolean isCommercialVolumeOverbark() {return true;}

		@Override
		public String getSpeciesName() {return speciesName;}

		@Override
		public double getBarkProportionOfWoodVolume() {return 0;}

		@Override
		public double getDbhCm() {return dbhCm;}
		
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
		Assert.assertEquals("Testing bark volume", 0.1157, totalVolumeBarkM3, 1E-5);
		Assert.assertEquals("Testing wood volume", 0.8843, totalVolumeWoodM3, 1E-5);
		Assert.assertEquals("Testing wood volume", 1d, totalVolumeWoodM3 + totalVolumeBarkM3, 1E-5);
	}
}
