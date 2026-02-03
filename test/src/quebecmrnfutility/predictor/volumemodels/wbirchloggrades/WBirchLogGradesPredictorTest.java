/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin (LERFoB), Robert Schneider (UQAR) 
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
package quebecmrnfutility.predictor.volumemodels.wbirchloggrades;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import quebecmrnfutility.treelogger.wbirchprodvol.WBirchProdVolTreeLoggerParameters.ProductID;
import repicea.io.FormatField;
import repicea.io.javacsv.CSVField;
import repicea.io.javacsv.CSVReader;
import repicea.io.javacsv.CSVWriter;
import repicea.math.Matrix;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class WBirchLogGradesPredictorTest {

	@SuppressWarnings("unused")
	@Test
	public void testFixedEffectPredictions() {
		Map<String, WBirchLogGradesStandImpl> stands = readStands();
		WBirchLogGradesPredictor predictor = new WBirchLogGradesPredictor(false);
		predictor.isTestPurpose = true;
		int nbTrees = 0;
		int nbMatches = 0;
		int nbMatches2 = 0;
		for (WBirchLogGradesStandImpl stand : stands.values()) {
			for (WBirchLogGradesTreeImpl tree : stand.getTrees().values()) {
				if (tree.getSubjectId().equals("113")) {
					int u = 0;
				}
				Matrix pred = predictor.getLogGradeUnderbarkVolumePredictions(stand, tree);
				//		Matrix variances = predictor.getVMatrixForThisTree(tree);
				Matrix predRef = tree.getRealizedValues();
				Assert.assertEquals("Number of elements", predRef.m_iRows, pred.m_iRows);
				for (int i = 0; i < pred.m_iRows; i++) {
					Assert.assertEquals("Comparing tree " + tree.getSubjectId() + " in plot " + stand.getSubjectId(), predRef.getValueAt(i, 0), pred.getValueAt(i, 0), 1E-6);
					nbMatches2++;
				}
				nbMatches += 5;
				nbTrees++;
			}
		}
		System.out.println("Successfully compared " + nbTrees + " trees.");
	}
	
	public static Map<String, WBirchLogGradesStandImpl> readStands() {
//		List<WBirchProdVolStandImpl> standList = new ArrayList<WBirchProdVolStandImpl>();
		String filename = ObjectUtility.getPackagePath(WBirchLogGradesPredictorTest.class) + "pred-simul.csv";
		Map<String, WBirchLogGradesStandImpl> standMap = new HashMap<String, WBirchLogGradesStandImpl>();
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			String plotID;
			int treeID;
			String quality;
			double dbhCm;
			double h20Obs;
			double h20Pred;
			double merVolPred;
			double pulpVolPred;
			double lowGradeSawlogVolPred;
			double sawlogVolPred;
			double lowGradeVeneerVolPred;
			double veneerVolPred;
			double elevation;
			WBirchLogGradesStandImpl stand;
			while ((record = reader.nextRecord()) != null) {
				plotID = record[0].toString().trim();
				treeID = Integer.parseInt(record[1].toString());
				quality = record[2].toString().trim().toUpperCase();
				dbhCm = Double.parseDouble(record[4].toString());
				elevation = Double.parseDouble(record[14].toString()); 
				h20Obs = Double.parseDouble(record[5].toString());
				h20Pred = Double.parseDouble(record[18].toString());
				merVolPred = Double.parseDouble(record[19].toString()) * .001;
				pulpVolPred = Double.parseDouble(record[20].toString()) * .001;
				lowGradeSawlogVolPred = Double.parseDouble(record[21].toString()) * .001;
				sawlogVolPred = Double.parseDouble(record[22].toString()) * .001;
				lowGradeVeneerVolPred = Double.parseDouble(record[23].toString()) * .001;		
				veneerVolPred = Double.parseDouble(record[24].toString()) * .001;		

				if (!standMap.containsKey(plotID)) {
					standMap.put(plotID, new WBirchLogGradesStandImpl(plotID, elevation));
				}
				Matrix predRef = new Matrix(7,1);
				predRef.setValueAt(0, 0, h20Pred);
				predRef.setValueAt(1, 0, merVolPred);
				predRef.setValueAt(2, 0, pulpVolPred);
				predRef.setValueAt(3, 0, sawlogVolPred);
				predRef.setValueAt(4, 0, lowGradeVeneerVolPred);
				predRef.setValueAt(5, 0, veneerVolPred);
				predRef.setValueAt(6, 0, lowGradeSawlogVolPred);

				stand = standMap.get(plotID);
				if (!stand.getTrees().containsKey(treeID)) {
					new WBirchLogGradesTreeImpl(treeID, quality, dbhCm, stand, h20Obs, predRef);
				}
			}
//			standList.addAll(standMap.values());
//			return standList;
			return standMap;
		} catch (IOException e) {
			Assert.fail("Unable to load file " + filename);
			return null;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	
	@Test
	public void testMonteCarloPredictions() {
		Matrix meanRef = new Matrix(7,1);
		meanRef.setValueAt(0, 0, 6.4262054572822125);
		meanRef.setValueAt(1, 0, 358.8726980631341 * .001);
		meanRef.setValueAt(2, 0, 212.9874983502981 * .001);
		meanRef.setValueAt(3, 0, 145.88519971283398 * .001);
		meanRef.setValueAt(4, 0, 0d);
		meanRef.setValueAt(5, 0, 0d);
		meanRef.setValueAt(6, 0, 0d);
		
		Matrix stdRef = new Matrix(7,1);
		stdRef.setValueAt(0, 0, 0.13280569488806207);
		stdRef.setValueAt(1, 0, 0.048334578844355075);
		stdRef.setValueAt(2, 0, 0.07043375186265777);
		stdRef.setValueAt(3, 0, 0.0679108872730103); 
		stdRef.setValueAt(4, 0, 0d);
		stdRef.setValueAt(5, 0, 0d);
		stdRef.setValueAt(6, 0, 0d);
	
		int nbRealizations = 100000;
		Map<String, WBirchLogGradesStandImpl> stands = readStands();
		WBirchLogGradesPredictor predictor = new WBirchLogGradesPredictor(false, true);
		WBirchLogGradesStand stand = stands.get("49");
		WBirchLogGradesTree tree = ((WBirchLogGradesStandImpl) stand).getTrees().get(2);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		Matrix pred;
		for (int i = 0; i < nbRealizations; i++) {
			((WBirchLogGradesStandImpl) stand).setMonteCarloRealizationId(i);
			pred = predictor.getLogGradeUnderbarkVolumePredictions(stand, tree);
			estimate.addRealization(pred);
		}
		
		Matrix mean = estimate.getMean();
		Matrix relDiff = mean.subtract(meanRef).elementWiseDivide(meanRef).getAbsoluteValue();
		Assert.assertTrue("Difference in terms of mean", !relDiff.anyElementLargerThan(1E-2));
		
		Matrix variance = estimate.getVariance();
		Matrix std = variance.diagonalVector().elementWisePower(0.5);
		
		relDiff = std.subtract(stdRef).elementWiseDivide(stdRef).getAbsoluteValue();
		Assert.assertTrue("Difference in terms of std", !relDiff.anyElementLargerThan(1E-2));
	}

	
	// Unchecked since the switch to version Java 8
	public void testMonteCarloPredictions2() throws IOException {
		String filePath = ObjectUtility.getPackagePath(getClass()) + "MCSimul.csv";
		int nbRealizations = 10000;
		Map<String, WBirchLogGradesStandImpl> stands = readStands();
		WBirchLogGradesPredictor predictor = new WBirchLogGradesPredictor(true);
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		WBirchLogGradesStandImpl stand = stands.get(0);
		for (int i = 0; i < nbRealizations; i++) {
			Matrix sumProd = new Matrix(7,1);
			stand.setMonteCarloRealizationId(i);
			for (WBirchLogGradesTree tree : stand.getTrees().values()) {
				sumProd = sumProd.add(predictor.getLogGradeUnderbarkVolumePredictions(stand, tree).scalarMultiply(0.001));
			}
			estimate.addRealization(sumProd);
		}
		CSVWriter writer = new CSVWriter(new File(filePath), false);
		
		
		List<FormatField> fields = new ArrayList<FormatField>();
		fields.add(new CSVField("h20"));
		fields.add(new CSVField("merch"));
		fields.add(new CSVField(ProductID.PulpAndPaper.name()));
		fields.add(new CSVField(ProductID.Sawlog.name()));
		fields.add(new CSVField(ProductID.LowGradeVeneer.name()));
		fields.add(new CSVField(ProductID.Veneer.name()));
		fields.add(new CSVField(ProductID.LowGradeSawlog.name()));
		
		writer.setFields(fields);
		Object[] record;
		for (Matrix mat : estimate.getRealizations()) {
			record = new Object[mat.m_iRows];
			for (int i = 0; i < mat.m_iRows; i++) {
				record[i] = mat.getValueAt(i, 0);
			}
			writer.addRecord(record);
		}
		writer.close();
		
	}

	
}
