/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.artemis2009;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.io.javadbf.DBFReader;
import repicea.math.Matrix;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class Artemis2009PredictorTest {

	private static class ReferenceTree implements Comparable<ReferenceTree> {
		private final double dbhCm;
		private final double number;
		private final int id;
		@SuppressWarnings("unused")
		private final String speciesName;
		
		ReferenceTree(int id, String speciesName, double dbhCm, double number) {
			this.id = id;
			this.speciesName = speciesName;
			this.dbhCm = dbhCm;
			this.number = number;
		}

		@Override
		public int compareTo(ReferenceTree arg0) {
			if (this.id < arg0.id) {
				return -1;
			} else if (this.id == arg0.id) {
				return 0;
			} else {
				return 1;
			}
		}
	}
	
	
	private static final Map<Integer, String> TREE_STATUS_LIST = new HashMap<Integer, String>();
	static {
		TREE_STATUS_LIST.put(10, "Vivant");
		TREE_STATUS_LIST.put(12, "Vivant");
		TREE_STATUS_LIST.put(14, "Mort");
		TREE_STATUS_LIST.put(16, "Mort");
		TREE_STATUS_LIST.put(23, "Mort");
		TREE_STATUS_LIST.put(24, "Mort");
		TREE_STATUS_LIST.put(26, "Coup�");
		TREE_STATUS_LIST.put(29, "Vivant");
		TREE_STATUS_LIST.put(30, "Vivant");
		TREE_STATUS_LIST.put(32, "Vivant");
		TREE_STATUS_LIST.put(34, "Mort");
		TREE_STATUS_LIST.put(36, "Mort");
		TREE_STATUS_LIST.put(40, "Vivant");
		TREE_STATUS_LIST.put(42, "Vivant");
		TREE_STATUS_LIST.put(44, "Mort");
		TREE_STATUS_LIST.put(46, "Mort");
		TREE_STATUS_LIST.put(50, "Vivant");
		TREE_STATUS_LIST.put(52, "Vivant");
		TREE_STATUS_LIST.put(54, "Mort");
		TREE_STATUS_LIST.put(56, "Mort");
	}

	private static Map<String, List<ReferenceTree>> ReferenceTreeMap;
	private static Map<String, Artemis2009CompatibleStandImpl> StandMap;
	
	
	private void readReferenceTrees() {
		try {
			String referenceFilename = ObjectUtility.getRelativePackagePath(Artemis2009PredictorTest.class) + "pred10yrs.csv";

			ReferenceTreeMap = new HashMap<String, List<ReferenceTree>>();
			
			CSVReader reader = new CSVReader(referenceFilename);
			
			Object[] record;
			
			String standId;
			int date;
			
			int treeId = 0;
			String speciesName;
			double dbhCm;
			double number;
			ReferenceTree tree;
			String status;
			while ((record = reader.nextRecord()) != null) {
				date = (int) Double.parseDouble(record[1].toString());
				status = record[9].toString();
				if (date == 2024 && status.trim().toLowerCase().equals("vivant")) {
					standId = record[3].toString();
					if (!ReferenceTreeMap.containsKey(standId)) {
						ReferenceTreeMap.put(standId, new ArrayList<ReferenceTree>());
					}
					List<ReferenceTree> treeList = ReferenceTreeMap.get(standId);
					speciesName = record[8].toString();
					number = Double.parseDouble(record[10].toString());
					dbhCm = Double.parseDouble(record[11].toString());
					treeId = (int) Double.parseDouble(record[6].toString());
					tree = new ReferenceTree(treeId, speciesName, dbhCm, number);
					treeList.add(tree);
				}
			}
			for (List<ReferenceTree> treeList : ReferenceTreeMap.values()) {
				Collections.sort(treeList);
			}
			reader.close();
		} catch (IOException e) {
			Assert.fail("Unable to read the reference trees");
		}
	}

	private void readTreesToGrow() {
		try {
			StandMap = new HashMap<String, Artemis2009CompatibleStandImpl>();
			String path = ObjectUtility.getRelativePackagePath(getClass());
			DBFReader dbfReader = new DBFReader(path + "TEST6152.DBF");
			Object[] record;
			String idString;
			double latitude;
			double longitude;
			double altitude;
			double meanAnnualPrecipitationMm;
			double meanAnnualTemperatureC;
			String speciesName;
			double dbhCm;
			String vegpot;
			double number;
			int status;
			Artemis2009CompatibleStandImpl stand;

			while ((record = dbfReader.nextRecord()) != null) {
				idString = record[1].toString();
				latitude = (Double) record[2];
				longitude = (Double) record[3];
				altitude = (Double) record[5];
				meanAnnualPrecipitationMm = (Double) record[6];
				meanAnnualTemperatureC = (Double) record[8];
				speciesName = record[9].toString();
				dbhCm = Double.parseDouble(record[10].toString());
				vegpot = record[14].toString().substring(0, 3).toUpperCase();
				number = (Double) record[16];
				status = Integer.parseInt(record[18].toString());
				stand = StandMap.get(idString);
				if (stand == null) {
					stand = new Artemis2009CompatibleStandImpl(idString, 
							2014, 
							latitude, 
							longitude, 
							altitude, 
							meanAnnualTemperatureC, 
							meanAnnualPrecipitationMm, 
							vegpot);
					StandMap.put(idString, stand);
				} 
				if (dbhCm > 9d && TREE_STATUS_LIST.get(status).equals("Vivant")) {
					new Artemis2009CompatibleTreeImpl(stand, dbhCm, speciesName, number);
				}
			}
			for (Artemis2009CompatibleStandImpl s : StandMap.values()) {
				s.updatePlotVariables();
			}
			dbfReader.close();
		} catch (IOException e) {
			Assert.fail("Unable to read the trees to grow!");
		}
	}
	
	@Test
	public void testMortalityAndDiameterGrowthPredictions() throws IOException {
		if (ReferenceTreeMap == null) {
			readReferenceTrees();
		}
		if (StandMap == null) {
			readTreesToGrow();
		}
		
		Artemis2009MortalityPredictor mortalityPredictor = new Artemis2009MortalityPredictor(false);
		List<Integer> simulationDates = new ArrayList<Integer>();
		simulationDates.add(2014);
		simulationDates.add(2024);
		Artemis2009DiameterIncrementPredictor diamIncPredictor = new Artemis2009DiameterIncrementPredictor(false);
		List<String> potentialVegetationList = new ArrayList<String>();
		
		int nbTreesCompared = 0;
		for (String standId : StandMap.keySet()) {
			Artemis2009CompatibleStand stand = StandMap.get(standId);
			if (!potentialVegetationList.contains(stand.getPotentialVegetation())) {
				potentialVegetationList.add(stand.getPotentialVegetation());
			}
			List<Artemis2009CompatibleTree> trees = ((Artemis2009CompatibleStandImpl) stand).getTrees();
			for (Artemis2009CompatibleTree tree : trees) {
				String treeId = tree.getSubjectId();
//				System.out.println("Comparing probability for tree " + treeId + " in stand " + standId);
				double mortalityProbability = mortalityPredictor.predictEventProbability(stand, tree);
				double actual = tree.getNumber() * (1 - mortalityProbability);
				List<ReferenceTree> innerList = ReferenceTreeMap.get(standId);
				int treeIndex = Integer.parseInt(treeId);
				ReferenceTree refTree = innerList.get(treeIndex);
				double expected = refTree.number;
				Assert.assertEquals("Comparing probability for tree " + treeId + " in stand " + standId, expected, actual, 1E-8);
				
				double diamIncrement = diamIncPredictor.predictGrowth(stand, tree)[0];
				actual = tree.getDbhCm() + Math.round(diamIncrement * 10d) * .1;
				expected = refTree.dbhCm;
				Assert.assertEquals("Comparing diameter increment for tree " + treeId + " in stand " + standId, expected, actual, 1E-5);
				
				nbTreesCompared++;
			}
		}
		System.out.println("Successfully compared " + nbTreesCompared + " trees");
		System.out.println(potentialVegetationList.toString());
	}

	@Test
	public void testRecruitmentPredictions() throws IOException {
		if (ReferenceTreeMap == null) {
			readReferenceTrees();
		}
		
		if (StandMap == null) {
			readTreesToGrow();
		}


		Map<String, List<ReferenceTree>> recruitMap = new HashMap<String, List<ReferenceTree>>();
		for (String standName : ReferenceTreeMap.keySet()) {
			List<ReferenceTree> recruits = new ArrayList<ReferenceTree>();
			recruitMap.put(standName, recruits);
			int size = StandMap.get(standName).getTrees().size();
			List<ReferenceTree> referenceTrees = ReferenceTreeMap.get(standName);
			for (int i = size; i < referenceTrees.size(); i++) {
				recruits.add(referenceTrees.get(i));
			}	
		}

		List<String> potentialVegetationList = new ArrayList<String>();
		Artemis2009RecruitmentOccurrencePredictor occPred = new Artemis2009RecruitmentOccurrencePredictor(false);
		Artemis2009RecruitmentNumberPredictor numbPred = new Artemis2009RecruitmentNumberPredictor(false);
		Artemis2009RecruitDiameterPredictor diamPred = new Artemis2009RecruitDiameterPredictor(false); 
		
		int nbTreesCompared = 0;
		for (String standId : StandMap.keySet()) {
			Artemis2009CompatibleStand stand = StandMap.get(standId);
			if (!potentialVegetationList.contains(stand.getPotentialVegetation())) {
				potentialVegetationList.add(stand.getPotentialVegetation());
			}

			List<String> speciesGroupsForThisStand = ParameterDispatcher.getInstance().getSpeciesGroups(stand);
			List<Artemis2009CompatibleTree> possibleRecruits = new ArrayList<Artemis2009CompatibleTree>();
			for (String sg : speciesGroupsForThisStand) {
				possibleRecruits.add(new Artemis2009CompatibleTreeImpl(sg));
			}

			int i = 0;
			List<ReferenceTree> recruits = recruitMap.get(standId);
			for (Artemis2009CompatibleTree tree : possibleRecruits) {
				double probability = occPred.predictEventProbability(stand, tree);
				double number = numbPred.predictNumberOfRecruits(stand, tree);
				double meanNumber = probability * number;
				double dbh = diamPred.predictRecruitDiameter(stand, tree)[0];
				dbh = Math.round(dbh * 10) * .1;
				ReferenceTree refTree = recruits.get(i++);
				double expectedNumber = refTree.number;
				double expectedDbh = refTree.dbhCm;
				Assert.assertEquals("Comparing number for tree " + refTree.id + " in stand " + standId, expectedNumber, meanNumber, 1E-8);
				Assert.assertEquals("Comparing dbh for tree " + refTree.id + " in stand " + standId, expectedDbh, dbh, 1E-5);
				nbTreesCompared++;
			}
		}
		System.out.println("Successfully compared " + nbTreesCompared + " trees");
		System.out.println(potentialVegetationList.toString());
	}

	@Test
	public void testRecruitDiameterMeanAndVariability() throws IOException {
		if (StandMap == null) {
			readTreesToGrow();
		}
		Artemis2009CompatibleStandImpl stand = StandMap.get("880080470501");
		Artemis2009CompatibleTree tree = stand.getTrees().get(0);
		Artemis2009RecruitDiameterPredictor pred = new Artemis2009RecruitDiameterPredictor(false, true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		Matrix realization;
		for (int i = 0; i < 50000; i++) {
			stand.setMonteCarloRealization(i);
			double[] dbhPrediction = pred.predictRecruitDiameter(stand, tree);
			realization = new Matrix(1,1);
			realization.setValueAt(0, 0, dbhPrediction[0]);
			estimate.addRealization(realization);
		}
		
		double dispersion = 0.57746369036272;
		double gammaMean = 1.4466793436321166;
		double expectedMean = gammaMean *.1 + 9.1;
		double expectedVariance = gammaMean * gammaMean / dispersion * .01;
		
		double actualMean = estimate.getMean().getValueAt(0, 0);
		double actualVariance = estimate.getVariance().getValueAt(0, 0);

		Assert.assertEquals("Comparing mean", expectedMean, actualMean, 1E-2);
		Assert.assertEquals("Comparing variance", expectedVariance, actualVariance, 1E-2);
	}

	
	@Test
	public void testRecruitNumberMeanAndVariability() throws IOException {
		if (StandMap == null) {
			readTreesToGrow();
		}
		Artemis2009CompatibleStandImpl stand = StandMap.get("880080470501");
		Artemis2009CompatibleTree tree = stand.getTrees().get(0);
		Artemis2009RecruitmentNumberPredictor pred = new Artemis2009RecruitmentNumberPredictor(false, true);
		Artemis2009RecruitmentNumberPredictor.Override80Limit = true;
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		Matrix realization;
		for (int i = 0; i < 100000; i++) {
			stand.setMonteCarloRealization(i);
			double nbPrediction = pred.predictNumberOfRecruits(stand, tree);
			realization = new Matrix(1,1);
			realization.setValueAt(0, 0, nbPrediction);
			estimate.addRealization(realization);
		}
		
		double dispersion = 1.41209128903651;
		double expectedMean = 0.5172784824575968;	
		double expectedVariance = expectedMean + expectedMean * expectedMean * dispersion;
		expectedMean = expectedMean + 1; // because y was modelled as nb - 1
		double actualMean = estimate.getMean().getValueAt(0, 0);
		double actualVariance = estimate.getVariance().getValueAt(0, 0);

		Assert.assertEquals("Comparing mean", expectedMean, actualMean, 1E-2);
		Assert.assertEquals("Comparing variance", expectedVariance, actualVariance, 0.03);
	}

}
