/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin - Canadian Forest Service
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
package canforservutility.predictor.prices.dura;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import canforservutility.predictor.prices.dura.DuraPricePredictor.WoodProduct;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.util.ObjectUtility;

public class DuraPricePredictorTest {
	
	List<DuraPriceContextImpl> getObservations(String filename) throws NumberFormatException, IOException {
		List<DuraPriceContextImpl> observations = new ArrayList<DuraPriceContextImpl>();
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		int quarterId = 0;
		while((record = reader.nextRecord()) != null) {
			double EXCAUSLag4 = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("EXCAUSLag4")].toString());
			double CLIMCOSTLAG = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("CLIMCOSTLAG")].toString());
			double FEDFUNDSLag1 = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("FEDFUNDSLag1")].toString());
			double PSAVERTLag4 = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("PSAVERTLag4")].toString());
			double FEDFUNDSLag3 = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("FEDFUNDSLag3")].toString());
			int DummyCovid = Integer.parseInt(record[reader.getHeader().getIndexOfThisField("DummyCovid")].toString());
			double HOUST = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("HOUST")].toString());
			double EXCAUSLag1 = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("EXCAUSLag1")].toString());
			double pred = Double.parseDouble(record[reader.getHeader().getIndexOfThisField("fittedvalues")].toString());
			DuraPriceContextImpl dpci = new DuraPriceContextImpl(quarterId++,
					EXCAUSLag4,
					CLIMCOSTLAG,
					FEDFUNDSLag1,
					PSAVERTLag4,
					FEDFUNDSLag3,
					DummyCovid == 1,
					HOUST,
					EXCAUSLag1,
					pred);
			observations.add(dpci);
		}
		reader.close();
		return observations;
	}
	
	
	Matrix getResidualErrorCovMatrix(String filename) throws NumberFormatException, IOException {
		Matrix mat = new Matrix(15,15);
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		int i = 0;
		while( i < 15) {
			record = reader.nextRecord(); 
			for (int j = 1; j <= 15; j++) {
				double value = Double.parseDouble(record[j].toString());
				mat.setValueAt(i, j-1, value);
				if (i != j) {
					mat.setValueAt(j-1, i, value);
				}
			}
			i++;
		}
		reader.close();
		return mat;
	}

	
	
	@Test
	public void testPricePredictions() throws NumberFormatException, IOException {
		DuraPricePredictor predictor = new DuraPricePredictor(false, false);
		for (WoodProduct wp : WoodProduct.values()) {
			System.out.println("Testing wood product " + wp.name());
			String suffix = wp.name().substring(wp.name().indexOf("_") + 1);
			String filename = ObjectUtility.getPackagePath(getClass()) + "dataT" + suffix + ".csv";
			System.out.println("Reading file " + filename);
			List<DuraPriceContextImpl> observations = getObservations(filename);
			Assert.assertTrue("There are observations", !observations.isEmpty());
			for (DuraPriceContextImpl c : observations) {
				double actual = predictor.predictPriceForThisProduct(wp, c);
				double expected = c.pred;
				Assert.assertEquals("Testing prediction", expected, actual, 1E-8);
			}
			System.out.println("Sucessfully tested " + observations.size() + " observaions!");
		}
	}

	
	@Test
	public void testPriceStochasticPredictions() throws NumberFormatException, IOException {
		DuraPricePredictor predictor = new DuraPricePredictor(false, true);
		for (WoodProduct wp : WoodProduct.values()) {
			System.out.println("Testing wood product " + wp.name());
			String suffix = wp.name().substring(wp.name().indexOf("_") + 1);
			String filename = ObjectUtility.getPackagePath(getClass()) + "dataT" + suffix + ".csv";
			System.out.println("Reading file " + filename);
			List<DuraPriceContextImpl> observations = getObservations(filename);
			Matrix residualErrorCovMat = getResidualErrorCovMatrix(ObjectUtility.getPackagePath(getClass()) + "varcov" + suffix + "res.csv");
			Assert.assertTrue("There are observations", !observations.isEmpty());
			DuraPriceContextImpl obs = null;
			for (int i = 0; i < 15; i++) {
				obs = observations.get(i);
				predictor.predictPriceForThisProduct(wp, obs);
			}

			SymmetricMatrix actualMatrix = predictor.subPredictorMap.get(wp).getResidualErrorCovMatrix(obs);
			Assert.assertTrue("Checking if cov matrix of residual errors is correct" , !actualMatrix.subtract(residualErrorCovMat).getAbsoluteValue().anyElementLargerThan(1E-8));
		}
	}

}
