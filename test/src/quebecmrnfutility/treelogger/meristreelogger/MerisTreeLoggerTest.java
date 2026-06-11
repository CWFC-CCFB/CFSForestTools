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
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.ExpansionFactorProvider;
import repicea.simulation.covariateproviders.treelevel.REpiceaSpeciesProvider;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;
import repicea.util.ObjectUtility;

public class MerisTreeLoggerTest {

	static class LoggableTreeImpl implements LoggableTree, DbhCmProvider, REpiceaSpeciesProvider, ExpansionFactorProvider {

		final double volumeM3;
		final double dbhCm;
		final double number;
		final Species sp;
		
		
		LoggableTreeImpl(double volumeM3, Species sp, double dbhCm, double number) {
			this.volumeM3 = volumeM3;
			this.dbhCm = dbhCm;
			this.number = number;
			this.sp = sp;
		}
		
		@Override
		public double getCommercialVolumeM3() {return volumeM3;}

		@Override
		public boolean isCommercialVolumeOverbark() {return false;}

		@Override
		public String getSpeciesName() {return sp.getLatinName();}

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

		@Override
		public double getNumber() {return number;}

		@Override
		public Species getREpiceaSpecies() {return sp;}
		
	}
	
	static MerisTreeLogger Singleton;
	
	@BeforeClass
	public static void startup() {
		Singleton = new MerisTreeLogger();
		Singleton.setTreeLoggerParameters(null);
	}
	
	@Test
	public void test01SimpleTreeHappyPath() {
		LoggableTree tree = new LoggableTreeImpl(1d, Species.Picea_glauca, 13.05, 10);
		Singleton.logThisTree(tree);
		Collection<WoodPiece> woodPieces = Singleton.getWoodPieces().get(tree);
		Assert.assertTrue("Testing that wood piece collections is not empty", !woodPieces.isEmpty());
		double totalVolumeWoodM3 = 0d;
		double totalVolumeBarkM3 = 0d;
		double totalVolumeWoodM3WithExpansionFactor = 0d;
		double totalVolumeBarkM3WithExpansionFactor = 0d;
		double totalVolumeWithExpansionFactor = 0d;
		for (WoodPiece wp : woodPieces) {
			totalVolumeWoodM3 += wp.getWoodVolumeM3();
			totalVolumeBarkM3 += wp.getBarkVolumeM3();
			totalVolumeWoodM3WithExpansionFactor += wp.getWeightedWoodVolumeM3();
			totalVolumeBarkM3WithExpansionFactor += wp.getWeightedBarkVolumeM3();
			totalVolumeWithExpansionFactor += wp.getWeightedTotalVolumeM3();
		}
		Assert.assertEquals("Testing bark volume", 0.08, totalVolumeBarkM3, 1E-8);
		Assert.assertEquals("Testing wood volume", 1d, totalVolumeWoodM3, 1E-8);
		Assert.assertEquals("Testing bark volume with expansion factor", 0.8, totalVolumeBarkM3WithExpansionFactor, 1E-8);
		Assert.assertEquals("Testing wood volume with expansion factor", 10d, totalVolumeWoodM3WithExpansionFactor, 1E-8);
		Assert.assertEquals("Testing total volume with expansion factor", 10.8, totalVolumeWithExpansionFactor, 1E-8);
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

	@Test
	public void test03ImportFromFileHappyPath() {
		MerisTreeLoggerParameters parms = Singleton.getTreeLoggerParameters();
		String filename = ObjectUtility.getPackagePath(getClass()) + "CAT_matrice_produits_utf8.csv";
		MerisTypeMatrix newMatrix = parms.readFromFile(filename);
		parms.currentMatrix.replaceBy(newMatrix);
		Map <?,?> mAfter = parms.getLogCategories();
		Assert.assertEquals("Testing number of species", 4, mAfter.size());
		parms.initializeDefaultLogCategories();		
	}

}
