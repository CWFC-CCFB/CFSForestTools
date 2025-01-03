/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * Author: Jean-Francois Lavoie
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
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
package canforservutility.predictor.biomass.lambert2005;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import canforservutility.predictor.biomass.lambert2005.Lambert2005BiomassPredictor.BiomassCompartment;
import canforservutility.predictor.biomass.lambert2005.Lambert2005Tree.Lambert2005Species;
import quebecmrnfutility.predictor.volumemodels.merchantablevolume.MerchantableVolumePredictor;
import quebecmrnfutility.predictor.volumemodels.merchantablevolume.VolumableStand;
import quebecmrnfutility.predictor.volumemodels.merchantablevolume.VolumableStandImpl;
import quebecmrnfutility.predictor.volumemodels.merchantablevolume.VolumableTreeImpl;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.species.REpiceaSpecies;
import repicea.util.ObjectUtility;

public class Lambert2005BiomassPredictorTest {
	enum FileImportPrediction {
		DBH,
		HEIGHT,
		OM_STEM_WOOD,
		OM_STEM_BARK,
		OM_BRANCHES,
		OM_FOLIAGE_TWIGS,
		OM_TOTAL,
		OM_STEM,
		OM_CROWN,
		RESIDU_WOOD,
		RESIDU_BARK,
		RESIDU_STEM,
		RESIDU_BRANCHES,
		RESIDU_FOLIAGE,
		RESIDU_CROWN,
		RESIDU_TOTAL,
		RES_WOOD,
		RES_BARK,
		RES_STEM,
		RES_BRANCHES,
		RES_FOLIAGE,
		RES_CROWN,
		RES_TOTAL,
		PRED_WOOD,
		PRED_BARK,
		PRED_STEM,
		PRED_BRANCHES,
		PRED_FOLIAGE,
		PRED_CROWN,
		PRED_TOTAL,
		ESS,
		ESSLAT
	}
		
	@Test	
	public void biomassPredictionsTest() throws IOException {
				
		Lambert2005BiomassPredictor predictor = new Lambert2005BiomassPredictor(false, false);
		
		// read csv file
		String path = ObjectUtility.getRelativePackagePath(getClass());
		String parmsFilename = path + "0_pred_res.csv";
		CSVReader reader = new CSVReader(parmsFilename);
		
		Object[] record;
		
		int csvLine = 2;
		
		while ((record = reader.nextRecord()) != null) {
			String species = record[FileImportPrediction.ESSLAT.ordinal()].toString();
			double dbhcm = Double.parseDouble(record[FileImportPrediction.DBH.ordinal()].toString());
			double hm = Double.parseDouble(record[FileImportPrediction.HEIGHT.ordinal()].toString());
			
			Matrix groundtruth = new Matrix(7,1);
			groundtruth.setValueAt(FileImportPrediction.PRED_WOOD.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_WOOD.ordinal()].toString()));
			groundtruth.setValueAt(FileImportPrediction.PRED_BARK.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_BARK.ordinal()].toString()));
			groundtruth.setValueAt(FileImportPrediction.PRED_STEM.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_STEM.ordinal()].toString()));
			groundtruth.setValueAt(FileImportPrediction.PRED_BRANCHES.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_BRANCHES.ordinal()].toString()));
			groundtruth.setValueAt(FileImportPrediction.PRED_FOLIAGE.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_FOLIAGE.ordinal()].toString()));
			groundtruth.setValueAt(FileImportPrediction.PRED_CROWN.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_CROWN.ordinal()].toString()));
			groundtruth.setValueAt(FileImportPrediction.PRED_TOTAL.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_TOTAL.ordinal()].toString()));
						
			Lambert2005TreeImpl tree = new Lambert2005TreeImpl(
					Lambert2005Species.valueOf(species),
					dbhcm, 
					hm);
			
			Matrix prediction = predictor.predictBiomassKg(tree);
											
//			System.out.println("Species : " + species); 
//			System.out.println("Ground Truth : " + groundtruth.toString());
//			System.out.println("Prediction : " + prediction.toString());
			
			// now compare prediction with ground truth
			FileImportPrediction[] columns = FileImportPrediction.values();			
			for (int i = 0; i < 7; i++) {	
				String message ="Comparing prediction " + columns[i + FileImportPrediction.PRED_WOOD.ordinal()].toString() + " at line " + csvLine; 
				Assert.assertEquals(message, groundtruth.getValueAt(i, 0), prediction.getValueAt(i, 0), 1E-6);
			}
			
			csvLine++;
		}
		
		reader.close();			
		
	}
		
	@Test
	public void biomassPredictionsWithResidualErrorTest() throws IOException {
		Lambert2005BiomassPredictor predictor = new Lambert2005BiomassPredictor(false, false);
		
		// read csv file
		String path = ObjectUtility.getRelativePackagePath(getClass());
		String parmsFilename = path + "0_pred_res.csv";
		CSVReader reader = new CSVReader(parmsFilename);
		
		Object[] record;
		
		int csvLine = 2;
		
		List<Integer> differentLines = new ArrayList<Integer>();
		
		while ((record = reader.nextRecord()) != null) {
			String species = record[FileImportPrediction.ESSLAT.ordinal()].toString();
			double dbhcm = Double.parseDouble(record[FileImportPrediction.DBH.ordinal()].toString());
			double hm = Double.parseDouble(record[FileImportPrediction.HEIGHT.ordinal()].toString());
			
			Matrix groundtruth = new Matrix(7,1);
			groundtruth.setValueAt(FileImportPrediction.PRED_WOOD.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_WOOD.ordinal()].toString()));
			groundtruth.setValueAt(FileImportPrediction.PRED_BARK.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_BARK.ordinal()].toString()));
			groundtruth.setValueAt(FileImportPrediction.PRED_STEM.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_STEM.ordinal()].toString()));
			groundtruth.setValueAt(FileImportPrediction.PRED_BRANCHES.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_BRANCHES.ordinal()].toString()));
			groundtruth.setValueAt(FileImportPrediction.PRED_FOLIAGE.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_FOLIAGE.ordinal()].toString()));
			groundtruth.setValueAt(FileImportPrediction.PRED_CROWN.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_CROWN.ordinal()].toString()));
			groundtruth.setValueAt(FileImportPrediction.PRED_TOTAL.ordinal() - FileImportPrediction.PRED_WOOD.ordinal(), 0, Double.parseDouble(record[FileImportPrediction.PRED_TOTAL.ordinal()].toString()));
						
			Matrix om = new Matrix(7,1);
			om.setValueAt(0, 0, Double.parseDouble(record[FileImportPrediction.OM_STEM_WOOD.ordinal()].toString()));
			om.setValueAt(1, 0, Double.parseDouble(record[FileImportPrediction.OM_STEM_BARK.ordinal()].toString()));
			om.setValueAt(2, 0, Double.parseDouble(record[FileImportPrediction.OM_STEM.ordinal()].toString()));
			om.setValueAt(3, 0, Double.parseDouble(record[FileImportPrediction.OM_BRANCHES.ordinal()].toString()));
			om.setValueAt(4, 0, Double.parseDouble(record[FileImportPrediction.OM_FOLIAGE_TWIGS.ordinal()].toString()));
			om.setValueAt(5, 0, Double.parseDouble(record[FileImportPrediction.OM_CROWN.ordinal()].toString()));
			om.setValueAt(6, 0, Double.parseDouble(record[FileImportPrediction.OM_TOTAL.ordinal()].toString()));
			
			Matrix res = new Matrix(7,1);
			res.setValueAt(0, 0, Double.parseDouble(record[FileImportPrediction.RES_WOOD.ordinal()].toString()));
			res.setValueAt(1, 0, Double.parseDouble(record[FileImportPrediction.RES_BARK.ordinal()].toString()));
			res.setValueAt(2, 0, Double.parseDouble(record[FileImportPrediction.RES_STEM.ordinal()].toString()));
			res.setValueAt(3, 0, Double.parseDouble(record[FileImportPrediction.RES_BRANCHES.ordinal()].toString()));
			res.setValueAt(4, 0, Double.parseDouble(record[FileImportPrediction.RES_FOLIAGE.ordinal()].toString()));
			res.setValueAt(5, 0, Double.parseDouble(record[FileImportPrediction.RES_CROWN.ordinal()].toString()));
			res.setValueAt(6, 0, Double.parseDouble(record[FileImportPrediction.RES_TOTAL.ordinal()].toString()));
			
			Lambert2005TreeImpl tree = new Lambert2005TreeImpl(
					Lambert2005Species.valueOf(species),
					dbhcm, 
					hm); 					
			
			Matrix prediction = predictor.predictBiomassKg(tree);
			
			Matrix w = predictor.getWeight(tree);					
			
			Matrix m1 = om.subtract(prediction).elementWiseDivide(w);
			
//			System.out.println("Processing line " + csvLine);
//			System.out.println("Species : " + species); 
//			System.out.println("Ground Truth : " + res.toString());
//			System.out.println("Residual  : " + m1.toString());
			
			// now compare prediction with ground truth
			FileImportPrediction[] columns = FileImportPrediction.values();	
								
			Matrix difference = res.subtract(m1).getAbsoluteValue();
			if (difference.anyElementLargerThan(1E-6)) {
				differentLines.add(csvLine);				
			}			
			
			csvLine++;
		}
		
//		for (Integer i : differentLines) {
//			System.out.println("Found differences at CSV line " + i);
//		}
					
		System.out.println("Total of " + differentLines.size() + " lines different on " + csvLine);
		
		// The csv containing the residual errors to check against has 485 lines of data on 25910 that have been computed with specific 
		// weights that have been manually assigned (ref : e-mail from MC Lambert on Sept 10 2021)
		// We expect those 485 lines to fail, but the other ones should be predicted correctly.
		Assert.assertTrue(differentLines.size() == 485);
		
		reader.close();		
	}
	
	
	static class Tree extends VolumableTreeImpl implements Lambert2005Tree {

		final Lambert2005Species lambertSpecies;
		
		public Tree(String speciesName, Lambert2005Species lambertSpecies, double dbhCm, double heightM) {
			super(speciesName, dbhCm, heightM);
			this.lambertSpecies = lambertSpecies;
		}

		@Override
		public Lambert2005Species getLambert2005Species() {return lambertSpecies;}

		@Override
		public String getSubjectId() {return null;}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {return null;}

		@Override
		public int getMonteCarloRealizationId() {return 0;}
		
	}
	
	
	public static void main(String[] args) {
		MerchantableVolumePredictor volPred = new MerchantableVolumePredictor();
		Lambert2005BiomassPredictor bioPred = new Lambert2005BiomassPredictor();
		Lambert2005Species species = Lambert2005Species.PopulusTremuloides;
		REpiceaSpecies.Species repiceaSpecies = REpiceaSpecies.Species.Populus_spp;
		VolumableStand p = new VolumableStandImpl();
		Tree t = new Tree("PET", species, 20, 15);
		double volumeM3 = volPred.predictTreeCommercialUnderbarkVolumeDm3(p, t) * 0.001;
		double overbarkCommercialVolumeM3 = volumeM3 * (1 + repiceaSpecies.getBarkProportionOfWoodVolume());
		double totalAbovegroundbiomassMg = bioPred.predictBiomassKg(t).getValueAt(BiomassCompartment.TOTAL.ordinal(), 0) * 0.001;
		double basicWoodDensity = repiceaSpecies.getBasicWoodDensity();
		double totalAbovegroundVolumeM3 = totalAbovegroundbiomassMg / basicWoodDensity;
		double possibleExpansionFactor = totalAbovegroundVolumeM3 / overbarkCommercialVolumeM3;
		System.out.println("DBH = " + t.getDbhCm() + "; Height = " + t.getHeightM() + "; BEF = " + possibleExpansionFactor);
		int u = 0;
	}
	
}
