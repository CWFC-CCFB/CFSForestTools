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
import repicea.util.ObjectUtility;

public class StemTaperProject {

	private static final double VERY_SMALL = 1E-6;

	private static class StemTaperExportTool extends ExportTool {
		
		@SuppressWarnings("serial")
		private static class InternalSwingWorker extends InternalSwingWorkerForRecordSet {

			private StemTaperProject caller;
			
			@SuppressWarnings("rawtypes")
			private InternalSwingWorker(StemTaperProject caller, Enum selectedOption) {
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
					r.addField(new GExportFieldDetails("obsvol", record.observedVolume));
					r.addField(new GExportFieldDetails("vol50000", record.mc50000Volume));
					r.addField(new GExportFieldDetails("var50000", record.mc50000Variance));
					r.addField(new GExportFieldDetails("vol10000", record.mc10000Volume));
					r.addField(new GExportFieldDetails("var10000", record.mc10000Variance));
					r.addField(new GExportFieldDetails("vol1000", record.mc1000Volume));
					r.addField(new GExportFieldDetails("var1000", record.mc1000Variance));
					r.addField(new GExportFieldDetails("unvol", record.uncorrectedMean));
					r.addField(new GExportFieldDetails("unvar", record.uncorrectedVariance));
					r.addField(new GExportFieldDetails("corrvol", record.correctedMean));
					r.addField(new GExportFieldDetails("corrvar", record.correctedVariance));
					r.addField(new GExportFieldDetails("t50000", record.time50000));
					r.addField(new GExportFieldDetails("t10000", record.time10000));
					r.addField(new GExportFieldDetails("t1000", record.time1000));
					r.addField(new GExportFieldDetails("tuncorr", record.timeUncorrected));
					r.addField(new GExportFieldDetails("tcorr", record.timeCorrected));
					recordSet.add(r);
				}
				setRecordSet(recordSet);
			}
			
		}
		
		
		private enum ExportFormat {onlyFormat}

		private StemTaperProject caller;
		
		protected StemTaperExportTool(StemTaperProject stp) throws Exception {
			super();
			caller = stp;
		}

		@SuppressWarnings("rawtypes")
		@Override
		protected Vector<Enum> defineAvailableExportOptions() {
			Vector<Enum> exportOptions = new Vector<Enum>();
			exportOptions.add(ExportFormat.onlyFormat);
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
		private double observedVolume;
		private double mc50000Volume;
		private double mc50000Variance;
		private double mc10000Volume;
		private double mc10000Variance;
		private double mc1000Volume;
		private double mc1000Variance;
		private double uncorrectedMean;
		private double uncorrectedVariance;
		private double correctedMean;
		private double correctedVariance;
		private long timeCorrected;
		private long timeUncorrected;
		private long time50000;
		private long time10000;
		private long time1000;
	}
	
	private Map<Integer, StemTaperTree> trees;
	private ArrayList<ResultRecord> records;
	private StemTaperEquation ste;
	
	private StemTaperProject() {
		trees = new HashMap<Integer,StemTaperTree>();
		records = new ArrayList<ResultRecord>();
		try {
			ste = new StemTaperEquation();
			ste.setRememberRandomDeviates(false);
		} catch (Exception e) {
			System.out.println("StemTaperProject.c(): Unable to initialize the stem taper equation!");
			e.printStackTrace();
		}
		readTrees();
		int iter = 0;
		for (Integer index_j : trees.keySet()) {
			StemTaperTree tree = trees.get(index_j);
			try {
				System.out.println("Simulating tree taper for Tree " + index_j);
				records.add(runSimulation(index_j, tree));
			} catch (Exception e) {
				System.out.println("StemTaperProject.runSimulation(): An error occurred while simulating Tree no " + index_j);
			}
			iter++;
			if (iter > 3) {
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private ResultRecord runSimulation(int index_j, StemTaperTree tree) throws Exception {
		ResultRecord record = new ResultRecord();
		record.index_j = index_j;
		record.tree = tree;
		
		Vector<StemTaperHeightSection> heightSections = tree.getHeightSections();
		Collections.sort(heightSections);
		
		if (Math.abs(heightSections.lastElement().getSectionHeight() - tree.getHeight()) > VERY_SMALL) {
			heightSections.add(new StemTaperHeightSection(tree.getHeight(), 0d));
		}

		record.observedVolume = StemTaperEquation.getVolumeThroughSmalianFormula(tree);
		
		ste.setTree(tree);
		
		double minimumHeight = heightSections.firstElement().getSectionHeight();
		
		Matrix ht;
		Estimate<Matrix,?> result;
		
		long initialTime;
		int numberOfLogs = heightSections.size();
		ht = new Matrix(numberOfLogs + 1,1,0,1).scalarMultiply((tree.getHeight() - minimumHeight)/(numberOfLogs)).scalarAdd(minimumHeight);
		
		initialTime = System.currentTimeMillis();
		ste.setNumberOfMonteCarloRealizations(1);
		ste.setEstimationMethod(EstimationMethod.SecondOrder);
		result = ste.predictVolume(ht);
		record.correctedMean = result.getMean().getSumOfElements();
		record.correctedVariance = result.getVariance().getSumOfElements();
		record.timeCorrected = System.currentTimeMillis() - initialTime;
		
		
		initialTime = System.currentTimeMillis();
		ste.setNumberOfMonteCarloRealizations(1);
		ste.setEstimationMethod(EstimationMethod.FirstOrder);
		result = ste.predictVolume(ht);
		record.uncorrectedMean = result.getMean().getSumOfElements();
		record.uncorrectedVariance = result.getVariance().getSumOfElements();
		record.timeUncorrected = System.currentTimeMillis() - initialTime;

		
		initialTime = System.currentTimeMillis();
		ste.setNumberOfMonteCarloRealizations(50000);
		ste.setEstimationMethod(EstimationMethod.MonteCarlo);
		result = ste.predictVolume(ht);
		record.mc50000Volume = result.getMean().getSumOfElements();
		record.mc50000Variance = result.getVariance().getSumOfElements();
		record.time50000 = System.currentTimeMillis() - initialTime;

		initialTime = System.currentTimeMillis();
		ste.setNumberOfMonteCarloRealizations(10000);
		ste.setEstimationMethod(EstimationMethod.MonteCarlo);
		result = ste.predictVolume(ht);
		record.mc10000Volume = result.getMean().getSumOfElements();
		record.mc10000Variance = result.getVariance().getSumOfElements();
		record.time10000 = System.currentTimeMillis() - initialTime;

		initialTime = System.currentTimeMillis();
		ste.setNumberOfMonteCarloRealizations(1000);
		ste.setEstimationMethod(EstimationMethod.MonteCarlo);
		result = ste.predictVolume(ht);
		record.mc1000Volume = result.getMean().getSumOfElements();
		record.mc1000Variance = result.getVariance().getSumOfElements();
		record.time1000 = System.currentTimeMillis() - initialTime;

		return record;
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
		StemTaperProject prj = new StemTaperProject();
		try {
			StemTaperExportTool stet = new StemTaperExportTool(prj);
			String filename = "C:" + File.separator +
							"Users" + File.separator +
							"mfortin" + File.separator +
							"Publications" + File.separator +
							"1-Acceptes" + File.separator +
							"2011 - MRNFStemTaper" + File.separator +
							"Resultats" + File.separator +
							"simul.dbf";
			stet.setFilename(filename);
			Set<Enum> selectedFormats = new HashSet<Enum>();
			selectedFormats.add(StemTaperExportTool.ExportFormat.onlyFormat);
			stet.setSelectedOptions(selectedFormats);
			stet.createRecordSets();
			stet.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
