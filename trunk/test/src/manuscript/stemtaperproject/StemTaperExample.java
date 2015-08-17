package manuscript.stemtaperproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import quebecmrnfutility.predictor.stemtaper.StemTaperEquation;
import quebecmrnfutility.predictor.stemtaper.StemTaperEquation.EstimationMethod;
import quebecmrnfutility.predictor.stemtaper.StemTaperHeightSection;
import quebecmrnfutility.predictor.stemtaper.StemTaperTree;
import repicea.io.GExportFieldDetails;
import repicea.io.GExportRecord;
import repicea.io.GRecordSet;
import repicea.io.tools.ExportTool;
import repicea.math.Matrix;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class StemTaperExample {
	
	private static final double VERY_SMALL = 1E-6;

	private static class StemTaperExportTool extends ExportTool {
	
		@SuppressWarnings("serial")
		private static class InternalSwingWorker extends InternalSwingWorkerForRecordSet {

			private StemTaperExample caller;
			
			@SuppressWarnings("rawtypes")
			private InternalSwingWorker(StemTaperExample caller, Enum selectedOption) {
				super(selectedOption);
				this.caller = caller;
			}
			
			@Override
			protected void doThisJob() throws Exception {
				ArrayList<ResultRecord> records = caller.records;
				GRecordSet recordSet = new GRecordSet();
				GExportRecord r;
				for (ResultRecord record : records) {
					r = new GExportRecord();
					r.addField(new GExportFieldDetails("index_j", record.index_j));
					r.addField(new GExportFieldDetails("dbh", record.tree.getDbh()));
					r.addField(new GExportFieldDetails("height", record.tree.getHeight()));
					r.addField(new GExportFieldDetails("mcIter", record.mcIter));
					r.addField(new GExportFieldDetails("volume", record.volume));
					recordSet.add(r);
				}
				setRecordSet(recordSet);
			}
			
		}
		
		
		private enum ExportOption {onlyFormat}

		private StemTaperExample caller;
		
		protected StemTaperExportTool(StemTaperExample stp) throws Exception {
			super();
			caller = stp;
		}

		@SuppressWarnings("rawtypes")
		@Override
		protected Vector<Enum> defineAvailableExportOptions() {
			Vector<Enum> exportOptions = new Vector<Enum>();
			exportOptions.add(ExportOption.onlyFormat);
			return exportOptions;
		}


		@SuppressWarnings("rawtypes")
		@Override
		protected InternalSwingWorkerForRecordSet instantiateInternalSwingWorkerForRecordSet(Enum selectedOption) {
			return new InternalSwingWorker(caller, selectedOption);
		}
		
	}
	
	
	
	private static class ResultRecord {
		private int index_j;
		private StemTaperTree tree;
		private int mcIter;
		private double volume;
	}
	
	private Map<Integer, StemTaperTree> trees;
	private ArrayList<ResultRecord> records;
	private StemTaperEquation ste;
	
	private StemTaperExample() {
		trees = new HashMap<Integer,StemTaperTree>();
		records = new ArrayList<ResultRecord>();
		try {
			ste = new StemTaperEquation();
		} catch (Exception e) {
			System.out.println("StemTaperProject.c(): Unable to initialize the stem taper equation!");
			e.printStackTrace();
		}
		readTrees();
		StemTaperTree tree = trees.get(3);
		System.out.println("Simulating tree taper for Tree " + 3);
		try {
			runSimulation(3, tree);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void runSimulation(int index_j, StemTaperTree tree) throws Exception {
		
		Vector<StemTaperHeightSection> heightSections = tree.getHeightSections();
		Collections.sort(heightSections);
		
		if (Math.abs(heightSections.lastElement().getSectionHeight() - tree.getHeight()) > VERY_SMALL) {
			heightSections.add(new StemTaperHeightSection(tree.getHeight(), 0d));
		}

		ste.setTree(tree);
		
		double minimumHeight = heightSections.firstElement().getSectionHeight();
		
		Matrix ht;
		@SuppressWarnings("rawtypes")
		Estimate result;
		
		int numberOfLogs = heightSections.size();
		ht = new Matrix(numberOfLogs + 1,1,0,1).scalarMultiply((tree.getHeight() - minimumHeight)/(numberOfLogs)).scalarAdd(minimumHeight);
		
		ste.setNumberOfMonteCarloRealizations(50000);
		ste.setEstimationMethod(EstimationMethod.SecondOrder);
		result = ste.predictVolume(ht);
		ResultRecord record;

		for (int i = 0; i < ((MonteCarloEstimate) result).getRealizations().size(); i++) {
			record = new ResultRecord();
			record.index_j = index_j;
			record.tree = tree;
			record.mcIter = i;
			record.volume = ((MonteCarloEstimate) result).getRealizations().get(i).getSumOfElements();
			records.add(record);
		}

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
	
	
	
	@SuppressWarnings({"rawtypes"})
	public static void main(String[] args) {
		StemTaperExample prj = new StemTaperExample();
		try {
			StemTaperExportTool stet = new StemTaperExportTool(prj);
			String filename = "C:" + File.separator +
							"Users" + File.separator +
							"mfortin" + File.separator +
							"Publications" + File.separator +
							"4-En preparation" + File.separator +
							"2010 - MRNFStemTaper" + File.separator +
							"Resultats" + File.separator +
							"example.dbf";
			stet.setFilename(filename);
			Set<Enum> selectedFormats = new HashSet<Enum>();
			selectedFormats.add(StemTaperExportTool.ExportOption.onlyFormat);
			stet.setSelectedOptions(selectedFormats);
			stet.createRecordSets();
			stet.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
