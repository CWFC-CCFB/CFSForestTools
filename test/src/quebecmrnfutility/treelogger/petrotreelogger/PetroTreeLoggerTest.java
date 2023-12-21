/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec
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
package quebecmrnfutility.treelogger.petrotreelogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradeTree.PetroGradeSpecies;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcHarvestPriorityProvider.QcHarvestPriority;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcTreeQualityProvider.QcTreeQuality;
import quebecmrnfutility.simulation.covariateproviders.treelevel.QcVigorClassProvider.QcVigorClass;
import repicea.serial.xml.XmlDeserializer;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;
import repicea.util.ObjectUtility;

public class PetroTreeLoggerTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void testWithBasicVersion() throws FileNotFoundException, IOException {
		
		String refMapPath = ObjectUtility.getPackagePath(getClass()) + "refMapPetroBasicVersion.ser";
		
		List<PetroLoggableTree> trees = new ArrayList<PetroLoggableTree>();
		for (int i = 5; i < 15; i++) {
			trees.add(new PetroLoggableTreeImpl(PetroGradeSpecies.ERS, i * 5));
			trees.add(new PetroLoggableTreeImpl(PetroGradeSpecies.BOJ, i * 5));
		}
		
		PetroTreeLogger treeLogger = new PetroTreeLogger();
		treeLogger.setTreeLoggerParameters(null);
		treeLogger.init(trees);
		treeLogger.run();
		Map<String, Double> volumeByLogGrade = new HashMap<String, Double>();
		for (LoggableTree tree : treeLogger.getWoodPieces().keySet()) {
			Collection<WoodPiece> woodPieces = treeLogger.getWoodPieces().get(tree);
			for (WoodPiece wp : woodPieces) {
				String logCategoryName = wp.getLogCategory().getName();
				if (!volumeByLogGrade.containsKey(logCategoryName)) {
					volumeByLogGrade.put(logCategoryName, 0d);
				}
				double vol = volumeByLogGrade.get(logCategoryName);
				volumeByLogGrade.put(logCategoryName, vol + wp.getWeightedWoodVolumeM3());
			}
		}
	
		
//		XmlSerializer serializer = new XmlSerializer(refMapPath);
//		serializer.writeObject(volumeByLogGrade);
		
		XmlDeserializer deserializer = new XmlDeserializer(refMapPath);
		Map refMap = (Map) deserializer.readObject();
		Assert.assertTrue(volumeByLogGrade.size() == refMap.size());
		for (String logCategoryName : volumeByLogGrade.keySet()) {
			double actualVolume = volumeByLogGrade.get(logCategoryName);
			double expectedVolume = (Double) refMap.get(logCategoryName);
			Assert.assertEquals(expectedVolume, actualVolume, 1E-8);
		}
			
	}
	
	
	
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testWithMSCR() throws FileNotFoundException, IOException {
		
		String refMapPath = ObjectUtility.getPackagePath(getClass()) + "refMapPetroMSCRVersion.ser";
		
		List<PetroLoggableTree> trees = new ArrayList<PetroLoggableTree>();
		for (QcHarvestPriority priority : QcHarvestPriority.values()) {
			for (int i = 5; i < 15; i++) {
				trees.add(new PetroLoggableTreeImpl(PetroGradeSpecies.ERS, i * 5, priority));
				trees.add(new PetroLoggableTreeImpl(PetroGradeSpecies.BOJ, i * 5, priority));
			}
		}
		
		PetroTreeLogger treeLogger = new PetroTreeLogger();
		treeLogger.setTreeLoggerParameters(null);
		treeLogger.init(trees);
		treeLogger.run();
		Map<String, Double> volumeByLogGrade = new HashMap<String, Double>();
		for (LoggableTree tree : treeLogger.getWoodPieces().keySet()) {
			Collection<WoodPiece> woodPieces = treeLogger.getWoodPieces().get(tree);
			for (WoodPiece wp : woodPieces) {
				String logCategoryName = wp.getLogCategory().getName();
				if (!volumeByLogGrade.containsKey(logCategoryName)) {
					volumeByLogGrade.put(logCategoryName, 0d);
				}
				double vol = volumeByLogGrade.get(logCategoryName);
				volumeByLogGrade.put(logCategoryName, vol + wp.getWeightedWoodVolumeM3());
			}
		}
	
		
//		XmlSerializer serializer = new XmlSerializer(refMapPath);
//		serializer.writeObject(volumeByLogGrade);
		
		XmlDeserializer deserializer = new XmlDeserializer(refMapPath);
		Map refMap = (Map) deserializer.readObject();
		Assert.assertTrue(volumeByLogGrade.size() == refMap.size());
		for (String logCategoryName : volumeByLogGrade.keySet()) {
			double actualVolume = volumeByLogGrade.get(logCategoryName);
			double expectedVolume = (Double) refMap.get(logCategoryName);
			Assert.assertEquals(expectedVolume, actualVolume, 1E-8);
		}
			
	}

	
	@SuppressWarnings("rawtypes")
	@Test
	public void testWithABCD() throws FileNotFoundException, IOException {
		
		String refMapPath = ObjectUtility.getPackagePath(getClass()) + "refMapPetroQualityVersion.ser";
		
		List<PetroLoggableTree> trees = new ArrayList<PetroLoggableTree>();
		for (QcTreeQuality quality : QcTreeQuality.values()) {
			for (int i = 5; i < 15; i++) {
				trees.add(new PetroLoggableTreeImpl(PetroGradeSpecies.ERS, i * 5, quality));
				trees.add(new PetroLoggableTreeImpl(PetroGradeSpecies.BOJ, i * 5, quality));
			}
		}
		
		PetroTreeLogger treeLogger = new PetroTreeLogger();
		treeLogger.setTreeLoggerParameters(null);
		treeLogger.init(trees);
		treeLogger.run();
		Map<String, Double> volumeByLogGrade = new HashMap<String, Double>();
		for (LoggableTree tree : treeLogger.getWoodPieces().keySet()) {
			Collection<WoodPiece> woodPieces = treeLogger.getWoodPieces().get(tree);
			for (WoodPiece wp : woodPieces) {
				String logCategoryName = wp.getLogCategory().getName();
				if (!volumeByLogGrade.containsKey(logCategoryName)) {
					volumeByLogGrade.put(logCategoryName, 0d);
				}
				double vol = volumeByLogGrade.get(logCategoryName);
				volumeByLogGrade.put(logCategoryName, vol + wp.getWeightedWoodVolumeM3());
			}
		}
	
		
//		XmlSerializer serializer = new XmlSerializer(refMapPath);
//		serializer.writeObject(volumeByLogGrade);
		
		XmlDeserializer deserializer = new XmlDeserializer(refMapPath);
		Map refMap = (Map) deserializer.readObject();
		Assert.assertTrue(volumeByLogGrade.size() == refMap.size());
		for (String logCategoryName : volumeByLogGrade.keySet()) {
			double actualVolume = volumeByLogGrade.get(logCategoryName);
			double expectedVolume = (Double) refMap.get(logCategoryName);
			Assert.assertEquals(expectedVolume, actualVolume, 1E-8);
		}
			
	}

	
	@SuppressWarnings("rawtypes")
	@Test
	public void testWithVigor() throws FileNotFoundException, IOException {
		
		String refMapPath = ObjectUtility.getPackagePath(getClass()) + "refMapPetroVigorVersion.ser";
		
		List<PetroLoggableTree> trees = new ArrayList<PetroLoggableTree>();
		for (QcVigorClass vigor : QcVigorClass.values()) {
			for (int i = 5; i < 15; i++) {
				trees.add(new PetroLoggableTreeImpl(PetroGradeSpecies.ERS, i * 5, vigor));
				trees.add(new PetroLoggableTreeImpl(PetroGradeSpecies.BOJ, i * 5, vigor));
			}
		}
		
		PetroTreeLogger treeLogger = new PetroTreeLogger();
		treeLogger.setTreeLoggerParameters(null);
		treeLogger.init(trees);
		treeLogger.run();
		Map<String, Double> volumeByLogGrade = new HashMap<String, Double>();
		for (LoggableTree tree : treeLogger.getWoodPieces().keySet()) {
			Collection<WoodPiece> woodPieces = treeLogger.getWoodPieces().get(tree);
			for (WoodPiece wp : woodPieces) {
				String logCategoryName = wp.getLogCategory().getName();
				if (!volumeByLogGrade.containsKey(logCategoryName)) {
					volumeByLogGrade.put(logCategoryName, 0d);
				}
				double vol = volumeByLogGrade.get(logCategoryName);
				volumeByLogGrade.put(logCategoryName, vol + wp.getWeightedWoodVolumeM3());
			}
		}
	
		
//		XmlSerializer serializer = new XmlSerializer(refMapPath);
//		serializer.writeObject(volumeByLogGrade);
		
		XmlDeserializer deserializer = new XmlDeserializer(refMapPath);
		Map refMap = (Map) deserializer.readObject();
		Assert.assertTrue(volumeByLogGrade.size() == refMap.size());
		for (String logCategoryName : volumeByLogGrade.keySet()) {
			double actualVolume = volumeByLogGrade.get(logCategoryName);
			double expectedVolume = (Double) refMap.get(logCategoryName);
			Assert.assertEquals(expectedVolume, actualVolume, 1E-8);
		}
			
	}


}
