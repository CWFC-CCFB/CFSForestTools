/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2023 His Majesty the King in Right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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
package quebecmrnfutility.predictor.volumemodels.fortin2007volume;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.predictor.volumemodels.fortin2007volume.Fortin2007VolumableStand;
import quebecmrnfutility.predictor.volumemodels.fortin2007volume.Fortin2007VolumableStandImpl;
import quebecmrnfutility.predictor.volumemodels.fortin2007volume.Fortin2007VolumableTree;
import quebecmrnfutility.predictor.volumemodels.fortin2007volume.Fortin2007VolumePredictor;
import repicea.simulation.species.REpiceaSpecies;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;

public class Fortin2007VolumePredictorTest {

	@Test
	public void test01SimpleHappyPath() {
		Fortin2007VolumableStand p = new Fortin2007VolumableStandImpl();
		Fortin2007VolumableTree t = new Fortin2007VolumableTreeImpl("BOJ", 18, 15);
		Fortin2007VolumePredictor volPred = new Fortin2007VolumePredictor();
		double volumeDM3 = volPred.predictTreeCommercialUnderbarkVolumeDm3(p, t);
		Assert.assertEquals("Testing volume", 149.284990537785, volumeDM3, 1E-8);
	}

	@Test
	public void test02WithNonmerchantableTree() {
		Fortin2007VolumableStand p = new Fortin2007VolumableStandImpl();
		Fortin2007VolumableTree t = new Fortin2007VolumableTreeImpl("BOJ", 8d, 15);
		Fortin2007VolumePredictor volPred = new Fortin2007VolumePredictor();
		double volumeDM3 = volPred.predictTreeCommercialUnderbarkVolumeDm3(p, t);
		Assert.assertEquals("Testing volume", 0d, volumeDM3, 1E-8);
	}

	@Test
	public void test03WithNonobservedHeight() {
		Fortin2007VolumableStand p = new Fortin2007VolumableStandImpl();
		Fortin2007VolumableTree t = new Fortin2007VolumableTreeImpl("BOJ", 10d, -1d);
		Fortin2007VolumePredictor volPred = new Fortin2007VolumePredictor();
		try {
			volPred.predictTreeCommercialUnderbarkVolumeDm3(p, t);
			Assert.fail("Should have thrown an exception!");
		} catch (Exception e) {
			
		}
	}

	@Test
	public void test04WithTooShortTree() {
		Fortin2007VolumableStand p = new Fortin2007VolumableStandImpl();
		Fortin2007VolumableTree t = new Fortin2007VolumableTreeImpl("BOJ", 10d, 1d);
		Fortin2007VolumePredictor volPred = new Fortin2007VolumePredictor();
		try {
			volPred.predictTreeCommercialUnderbarkVolumeDm3(p, t);
			Assert.fail("Should have thrown an exception!");
		} catch (Exception e) {}
	}

	@Test
	public void test05FastTrackSimpleHappyPath() {
//		VolumableStand p = new VolumableStandImpl();
//		VolumableTree t = new VolumableTreeImpl("BOJ", 18, 15);
		Fortin2007VolumePredictor volPred = new Fortin2007VolumePredictor();
		double volumeDM3 = volPred.predictDeterministicTreeCommercialVolumeDm3("Betula alleghaniensis", 18d, 15d, false);
		Assert.assertEquals("Testing volume", 149.284990537785, volumeDM3, 1E-8);
	}

	@Test
	public void test06FastTrackWithNonmerchantableTree() {
//		VolumableStand p = new VolumableStandImpl();
//		VolumableTree t = new VolumableTreeImpl("BOJ", 8d, 15);
		Fortin2007VolumePredictor volPred = new Fortin2007VolumePredictor();
		double volumeDM3 = volPred.predictDeterministicTreeCommercialVolumeDm3("Betula alleghaniensis", 8d, 15d, false);
		Assert.assertEquals("Testing volume", 0d, volumeDM3, 1E-8);
	}

	@Test
	public void test07FastTrackWithNonobservedHeight() {
//		VolumableStand p = new VolumableStandImpl();
//		VolumableTree t = new VolumableTreeImpl("BOJ", 10d, -1d);
		Fortin2007VolumePredictor volPred = new Fortin2007VolumePredictor();
		try {
			volPred.predictDeterministicTreeCommercialVolumeDm3("Betula alleghaniensis", 10d, -1d, false);
			Assert.fail("Should have thrown an exception!");
		} catch (Exception e) {
			
		}
	}

	@Test
	public void test08FastTrackWithTooShortTree() {
//		VolumableStand p = new VolumableStandImpl();
//		VolumableTree t = new VolumableTreeImpl("BOJ", 10d, 1d);
		Fortin2007VolumePredictor volPred = new Fortin2007VolumePredictor();
		try {
			volPred.predictDeterministicTreeCommercialVolumeDm3("Betula alleghaniensis", 10d, 1d, false);
			Assert.fail("Should have thrown an exception!");
		} catch (Exception e) {}
	}

	@Test
	public void test09FastTrackSimpleHappyPathOverbark() {
//		VolumableStand p = new VolumableStandImpl();
//		VolumableTree t = new VolumableTreeImpl("BOJ", 18, 15);
		Fortin2007VolumePredictor volPred = new Fortin2007VolumePredictor();
		double volumeDM3 = volPred.predictDeterministicTreeCommercialVolumeDm3("Betula alleghaniensis", 18d, 15d, true);
		Assert.assertEquals("Testing volume", 
				149.284990537785 * (1 + REpiceaSpecies.Species.Betula_alleghaniensis.getBarkProportionOfWoodVolume(SpeciesLocale.Quebec)),
				volumeDM3, 
				1E-8);
	}
	
	@Test
	public void test10LatinNameList() {
		Fortin2007VolumePredictor pred = new Fortin2007VolumePredictor();
		List<Species> species = pred.getEligibleSpecies();
		Assert.assertEquals("Testing the number of species", 26, species.size());
		System.out.println(species);
		try {
			species.add(Species.Abies_alba);
			Assert.fail("Should have thrown an exception");
		} catch (Exception e) {
			int u = 0;
		}
	}

}
