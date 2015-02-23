package quebecmrnfutility.predictor.stemtaper;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.junit.Test;

import quebecmrnfutility.predictor.stemtaper.StemTaperEquation;
import quebecmrnfutility.predictor.stemtaper.StemTaperHeightSection;
import quebecmrnfutility.predictor.stemtaper.StemTaperTree;
import repicea.math.Matrix;
import repicea.stats.estimates.Estimate;
import repicea.util.ObjectUtility;

public class StemTaperTest {

	private static final double VERY_SMALL = 1E-6;

	private Map<Integer, StemTaperTree> trees;
	private StemTaperEquation ste;
	private Map<Integer, Double> records;

	public StemTaperTest() {}
	
	private void initialize() {
		records = new HashMap<Integer, Double>();
		trees = new HashMap<Integer,StemTaperTree>();
		try {
			ste = new StemTaperEquation();
		} catch (Exception e) {
			System.out.println("StemTaperProject.c(): Unable to initialize the stem taper equation!");
			e.printStackTrace();
		}
		readTrees();
		Set<Integer> keySet = trees.keySet();
		Vector<Integer> indices = new Vector<Integer>();
		for (Integer integer : keySet) {
			indices.add(integer);
		}
		Collections.sort(indices);
		
		try {
			int index;
			for (int i = 0; i < 10; i++) {
				index = indices.get(i);
				System.out.println("Simulating tree taper for Tree " + index);
				runSimulation(i, trees.get(index));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void runSimulation(int index_j, StemTaperTree tree) throws Exception {

		Vector<StemTaperHeightSection> heightSections = tree.getHeightSections();
		Collections.sort(heightSections);

		if (Math.abs(heightSections.lastElement().getSectionHeight() - tree.getHeight()) > VERY_SMALL) {
			heightSections.add(new StemTaperHeightSection(tree.getHeight(), 0d));
		}

		ste.setTree(tree);

		double minimumHeight = heightSections.firstElement().getSectionHeight();

		Matrix ht;
		Estimate result;

		int numberOfLogs = heightSections.size();
		ht = new Matrix(numberOfLogs + 1,1,0,1).scalarMultiply((tree.getHeight() - minimumHeight)/(numberOfLogs)).scalarAdd(minimumHeight);

		result = ste.predictVolume(ht);

		records.put(index_j, result.getMean().getSumOfElements());
	}

	private void readTrees() {
		String path = ObjectUtility.getRelativePackagePath(getClass());
		String filename = path + "exampleTrees.csv";

		try {
			InputStream is = ClassLoader.getSystemResourceAsStream(filename);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String str = in.readLine();
			StringTokenizer tokenizer;

			double dbh;
			int index_j;
			double htot;
			double ht_reelle;
			double dia_se;
			StemTaperTree tree;
			int line = 0;
			while (str != null) {
				// comment / blank line : goes to next line
				if (!str.startsWith("#") && str.trim().length() != 0 && line > 0) {
					tokenizer = new StringTokenizer(str, ",");
					dbh = Double.parseDouble(tokenizer.nextToken());
					index_j = Integer.parseInt(tokenizer.nextToken());
					htot = Double.parseDouble(tokenizer.nextToken());
					ht_reelle = Double.parseDouble(tokenizer.nextToken());
					dia_se = Double.parseDouble(tokenizer.nextToken());
					if (!trees.containsKey(index_j)) {
						trees.put(index_j, new StemTaperTreeImpl(dbh, htot));
					}
					tree = trees.get(index_j);
					tree.addHeightSection(new StemTaperHeightSection(ht_reelle, dia_se));
				}
				str = in.readLine();
				line++;
			}
			// By now, we have iterated through all of the rows
			is.close();
		} catch (Exception e) {
			System.out.println("Error reading file : " + filename);
			e.printStackTrace();
		}
	}


	@Test
	@SuppressWarnings("rawtypes")
	public void TestVolumeForTheFirst10Trees() throws Exception {
		StemTaperTest test = new StemTaperTest();
		test.initialize();
		String path = ObjectUtility.getPackagePath(getClass());
		String referenceFilename = path + "resultsFirst10Trees.ser";
//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//      	try {
//      		FileOutputStream fos = new FileOutputStream(referenceFilename);
//      		ObjectOutputStream out = new ObjectOutputStream(fos);
//      		out.writeObject(test.records);
//      		out.close();
//      	} catch(IOException ex) {
//      		ex.printStackTrace();
//      		throw ex;
//      	}
    	
       	System.out.println("Loading reference map...");
		HashMap refMap;
    	try {
    		FileInputStream fis = new FileInputStream(referenceFilename);
    		ObjectInputStream in = new ObjectInputStream(fis);
    		refMap = (HashMap) in.readObject();
    		in.close();
    	} catch(IOException ex) {
    		ex.printStackTrace();
    		throw ex;
    	}

       	System.out.println("Comparing results...");
       	for (Object key : refMap.keySet()) {
       		double volumeRef = (Double) refMap.get(key);
       		double estimatedVolume = test.records.get(key);
       		assertEquals("Testing tree " + key.toString(), volumeRef, estimatedVolume, 0.0001);
       	}


	}

}
