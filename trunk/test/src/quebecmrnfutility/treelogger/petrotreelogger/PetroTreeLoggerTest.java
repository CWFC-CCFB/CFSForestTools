package quebecmrnfutility.treelogger.petrotreelogger;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradeTree.PetroGradeSpecies;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlMarshallException;
import quebecmrnfutility.simulation.covariateproviders.treelevel.ABCDQualityProvider.ABCDQuality;
import quebecmrnfutility.simulation.covariateproviders.treelevel.MSCRPriorityProvider.MSCRPriority;
import quebecmrnfutility.simulation.covariateproviders.treelevel.VigorClassProvider.VigorClass;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;
import repicea.util.ObjectUtility;

public class PetroTreeLoggerTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void testWithBasicVersion() throws FileNotFoundException, XmlMarshallException {
		
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
				volumeByLogGrade.put(logCategoryName, vol + wp.getWeightedVolumeM3());
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
	public void testWithMSCR() throws FileNotFoundException, XmlMarshallException {
		
		String refMapPath = ObjectUtility.getPackagePath(getClass()) + "refMapPetroMSCRVersion.ser";
		
		List<PetroLoggableTree> trees = new ArrayList<PetroLoggableTree>();
		for (MSCRPriority priority : MSCRPriority.values()) {
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
				volumeByLogGrade.put(logCategoryName, vol + wp.getWeightedVolumeM3());
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
	public void testWithABCD() throws FileNotFoundException, XmlMarshallException {
		
		String refMapPath = ObjectUtility.getPackagePath(getClass()) + "refMapPetroQualityVersion.ser";
		
		List<PetroLoggableTree> trees = new ArrayList<PetroLoggableTree>();
		for (ABCDQuality quality : ABCDQuality.values()) {
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
				volumeByLogGrade.put(logCategoryName, vol + wp.getWeightedVolumeM3());
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
	public void testWithVigor() throws FileNotFoundException, XmlMarshallException {
		
		String refMapPath = ObjectUtility.getPackagePath(getClass()) + "refMapPetroVigorVersion.ser";
		
		List<PetroLoggableTree> trees = new ArrayList<PetroLoggableTree>();
		for (VigorClass vigor : VigorClass.values()) {
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
				volumeByLogGrade.put(logCategoryName, vol + wp.getWeightedVolumeM3());
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
