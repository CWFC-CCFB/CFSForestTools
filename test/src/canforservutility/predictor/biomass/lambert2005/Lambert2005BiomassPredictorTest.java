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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import canforservutility.predictor.biomass.lambert2005.Lambert2005BiomassPredictor.BiomassCompartment;
import canforservutility.predictor.biomass.lambert2005.Lambert2005BiomassPredictor.ModelVersion;
import canforservutility.predictor.biomass.lambert2005.Lambert2005Tree.Lambert2005Species;
import quebecmrnfutility.predictor.volumemodels.merchantablevolume.MerchantableVolumePredictor;
import quebecmrnfutility.predictor.volumemodels.merchantablevolume.VolumableStand;
import quebecmrnfutility.predictor.volumemodels.merchantablevolume.VolumableStandImpl;
import quebecmrnfutility.predictor.volumemodels.merchantablevolume.VolumableTreeImpl;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.species.REpiceaSpecies;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.util.ObjectUtility;

public class Lambert2005BiomassPredictorTest {

	private static final String DBH_FIELD = "dbh";
	private static final String HEIGHT_FIELD = "height";
	private static final List<String> PRED_FIELDS = new ArrayList<String>();
	static {
		List<BiomassCompartment> biomassCompartments = Arrays.asList(BiomassCompartment.values()); 
		List<String> tmpList = biomassCompartments.stream().map(p -> "pred_".concat(p.name().toLowerCase())).collect(Collectors.toList());
		PRED_FIELDS.addAll(tmpList);
	}

	private static final List<String> RES_FIELDS = new ArrayList<String>();
	static {
		List<BiomassCompartment> biomassCompartments = Arrays.asList(BiomassCompartment.values()); 
		List<String> tmpList = biomassCompartments.stream().map(p -> "res_".concat(p.name().toLowerCase())).collect(Collectors.toList());
		RES_FIELDS.addAll(tmpList);
	}

	private static final List<String> OM_FIELDS = Arrays.asList(new String[] {"om_stem_wood",
			"om_stem_bark",
			"om_stem",
			"om_branches",
			"om_foliage_twigs",
			"om_crown",
			"om_Total"});
	
	@Test	
	public void test01BiomassPredictionsWithCompleteModel() throws IOException {
				
		Lambert2005BiomassPredictor predictor = new Lambert2005BiomassPredictor(false, false);
		
		// read csv file
		String path = ObjectUtility.getRelativePackagePath(getClass());
		String parmsFilename = path + "0_pred_res.csv";
		CSVReader reader = new CSVReader(parmsFilename);
		
		Object[] record;
		
		int csvLine = 2;
		
		while ((record = reader.nextRecord()) != null) {
			String species = record[reader.getHeader().getIndexOfThisField(BiomassParameterLoader.SPECIES_FIELD)].toString();
			if (!species.trim().toLowerCase().equals(BiomassParameterLoader.ANY)) {
				double dbhcm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField(DBH_FIELD)].toString());
				double hm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField(HEIGHT_FIELD)].toString());
				
				Matrix groundtruth = new Matrix(PRED_FIELDS.size(),1);
				for (String f : PRED_FIELDS) {
					int indexInFile = reader.getHeader().getIndexOfThisField(f);
					groundtruth.setValueAt(PRED_FIELDS.indexOf(f), 
							0, 
							Double.parseDouble(record[indexInFile].toString()));
				}
							
				Lambert2005Tree tree = new Lambert2005TreeCompleteImpl(
						Lambert2005BiomassPredictor.ENGLISH_TO_LATIN_LOOKUP_MAP.get(species),
						dbhcm, 
						hm);
				
				Matrix prediction = predictor.predictBiomassKg(tree);
												
//				System.out.println("Species : " + species); 
//				System.out.println("Ground Truth : " + groundtruth.toString());
//				System.out.println("Prediction : " + prediction.toString());
				
				for (int i = 0; i < PRED_FIELDS.size(); i++) {
					String message ="Comparing prediction " + PRED_FIELDS.get(i) + " at line " + csvLine; 
					Assert.assertEquals(message, groundtruth.getValueAt(i, 0), prediction.getValueAt(i, 0), 1E-6);
				}
				
				csvLine++;
			}
		}
		reader.close();			
	}
	
	@Test
	public void test02BiomassPredictionsWithResidualErrorAndCompleteModel() throws IOException {
		Lambert2005BiomassPredictor predictor = new Lambert2005BiomassPredictor(false, false);
		
		// read csv file
		String path = ObjectUtility.getRelativePackagePath(getClass());
		String parmsFilename = path + "0_pred_res.csv";
		CSVReader reader = new CSVReader(parmsFilename);
		
		Object[] record;
		
		int csvLine = 2;
		
		Map<REpiceaSpecies, List<Integer>> differentLines = new HashMap<REpiceaSpecies, List<Integer>>();
		
		while ((record = reader.nextRecord()) != null) {
			String species = record[reader.getHeader().getIndexOfThisField(BiomassParameterLoader.SPECIES_FIELD)].toString();
			if (!species.trim().toLowerCase().equals(BiomassParameterLoader.ANY)) {
				double dbhcm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField(DBH_FIELD)].toString());
				double hm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField(HEIGHT_FIELD)].toString());
				
				Matrix groundtruth = new Matrix(PRED_FIELDS.size(),1);
				for (String f : PRED_FIELDS) {
					int indexInFile = reader.getHeader().getIndexOfThisField(f);
					groundtruth.setValueAt(PRED_FIELDS.indexOf(f), 
							0, 
							Double.parseDouble(record[indexInFile].toString()));
				}

				Matrix om = new Matrix(OM_FIELDS.size(),1);
				for (String f : OM_FIELDS) {
					int indexInFile = reader.getHeader().getIndexOfThisField(f);
					om.setValueAt(OM_FIELDS.indexOf(f), 
							0, 
							Double.parseDouble(record[indexInFile].toString()));
				}

				Matrix res = new Matrix(RES_FIELDS.size(),1);
				for (String f : RES_FIELDS) {
					int indexInFile = reader.getHeader().getIndexOfThisField(f);
					res.setValueAt(RES_FIELDS.indexOf(f), 
							0, 
							Double.parseDouble(record[indexInFile].toString()));
				}
				
				Lambert2005Tree tree = new Lambert2005TreeCompleteImpl(
						Lambert2005BiomassPredictor.ENGLISH_TO_LATIN_LOOKUP_MAP.get(species),
						dbhcm, 
						hm);
				
				Matrix prediction = predictor.predictBiomassKg(tree);
				
				Matrix w = predictor.getWeight(tree);					
				
				Matrix m1 = om.subtract(prediction).elementWiseDivide(w);
				
//				System.out.println("Processing line " + csvLine);
//				System.out.println("Species : " + species); 
//				System.out.println("Ground Truth : " + res.toString());
//				System.out.println("Residual  : " + m1.toString());
									
				Matrix difference = res.subtract(m1).getAbsoluteValue();
				if (difference.anyElementLargerThan(1E-6)) {
					REpiceaSpecies speciesEnum = tree.getLambert2005Species(); 
					if (!differentLines.containsKey(speciesEnum)) {
						differentLines.put(speciesEnum, new ArrayList<Integer>());
					}
					differentLines.get(speciesEnum).add(csvLine);				
				}			
				
				csvLine++;
			}
		}
		
//		for (Integer i : differentLines) {
//			System.out.println("Found differences at CSV line " + i);
//		}
		int nbDifferences = 0;
		for (List<Integer> diffForAParticularSpecies : differentLines.values()) {
			nbDifferences += diffForAParticularSpecies.size();
		}
					
		System.out.println("Total of " + nbDifferences + " lines different on " + csvLine);
		
		// The csv containing the residual errors to check against has 116 lines of data on 25910 that have been computed with specific 
		// weights that have been manually assigned (ref : e-mail from MC Lambert on Sept 10 2021)
		// We expect those 116 lines to fail, but the other ones should be predicted correctly.
		Assert.assertTrue(nbDifferences == 91);
		
		reader.close();		
	}
	
	@Test	
	public void test03BiomassPredictionsWithReducedModel() throws IOException {
				
		Lambert2005BiomassPredictor predictor = new Lambert2005BiomassPredictor(false, false);
		
		// read csv file
		String path = ObjectUtility.getRelativePackagePath(getClass());
		String parmsFilename = path + "1_pred_res.csv";
		CSVReader reader = new CSVReader(parmsFilename);
		
		Object[] record;
		
		int csvLine = 2;
		
		while ((record = reader.nextRecord()) != null) {
			String species = record[reader.getHeader().getIndexOfThisField(BiomassParameterLoader.SPECIES_FIELD)].toString();
			if (!species.trim().toLowerCase().equals(BiomassParameterLoader.ANY)) {
				double dbhcm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField(DBH_FIELD)].toString());
				
				Matrix groundtruth = new Matrix(PRED_FIELDS.size(),1);
				for (String f : PRED_FIELDS) {
					int indexInFile = reader.getHeader().getIndexOfThisField(f);
					groundtruth.setValueAt(PRED_FIELDS.indexOf(f), 
							0, 
							Double.parseDouble(record[indexInFile].toString()));
				}
							
				Lambert2005Tree tree = new Lambert2005TreeReducedImpl(
						Lambert2005BiomassPredictor.ENGLISH_TO_LATIN_LOOKUP_MAP.get(species),
						dbhcm);
				
				Matrix prediction = predictor.predictBiomassKg(tree);
												
//				System.out.println("Species : " + species); 
//				System.out.println("Ground Truth : " + groundtruth.toString());
//				System.out.println("Prediction : " + prediction.toString());
				
				for (int i = 0; i < PRED_FIELDS.size(); i++) {
					String message ="Comparing prediction " + PRED_FIELDS.get(i) + " at line " + csvLine; 
					Assert.assertEquals(message, groundtruth.getValueAt(i, 0), prediction.getValueAt(i, 0), 1E-6);
				}
				
				csvLine++;
			}
		}
		reader.close();			
	}

	// TODO MF20260113 Reenable this test when we have parameter estimates with more digits
	@Ignore
	@Test
	public void test04BiomassPredictionsWithResidualErrorAndReducedModel() throws IOException {
		Lambert2005BiomassPredictor predictor = new Lambert2005BiomassPredictor(false, false);
		
		// read csv file
		String path = ObjectUtility.getRelativePackagePath(getClass());
		String parmsFilename = path + "0_pred_res.csv";
		CSVReader reader = new CSVReader(parmsFilename);
		
		Object[] record;
		
		int csvLine = 2;
		
		Map<REpiceaSpecies, List<Integer>> differentLines = new HashMap<REpiceaSpecies, List<Integer>>();
		
		while ((record = reader.nextRecord()) != null) {
			String species = record[reader.getHeader().getIndexOfThisField(BiomassParameterLoader.SPECIES_FIELD)].toString();
			double dbhcm = Double.parseDouble(record[reader.getHeader().getIndexOfThisField(DBH_FIELD)].toString());
			
			Matrix groundtruth = new Matrix(PRED_FIELDS.size(),1);
			for (String f : PRED_FIELDS) {
				int indexInFile = reader.getHeader().getIndexOfThisField(f);
				groundtruth.setValueAt(PRED_FIELDS.indexOf(f), 
						0, 
						Double.parseDouble(record[indexInFile].toString()));
			}

			Matrix om = new Matrix(OM_FIELDS.size(),1);
			for (String f : OM_FIELDS) {
				int indexInFile = reader.getHeader().getIndexOfThisField(f);
				om.setValueAt(OM_FIELDS.indexOf(f), 
						0, 
						Double.parseDouble(record[indexInFile].toString()));
			}

			Matrix res = new Matrix(RES_FIELDS.size(),1);
			for (String f : RES_FIELDS) {
				int indexInFile = reader.getHeader().getIndexOfThisField(f);
				res.setValueAt(RES_FIELDS.indexOf(f), 
						0, 
						Double.parseDouble(record[indexInFile].toString()));
			}
			
			Lambert2005Tree tree = new Lambert2005TreeReducedImpl(
					Lambert2005BiomassPredictor.ENGLISH_TO_LATIN_LOOKUP_MAP.get(species),
					dbhcm);
			
			Matrix prediction = predictor.predictBiomassKg(tree);
			
			Matrix w = predictor.getWeight(tree);					
			
			Matrix m1 = om.subtract(prediction).elementWiseDivide(w);
			
//			System.out.println("Processing line " + csvLine);
//			System.out.println("Species : " + species); 
//			System.out.println("Ground Truth : " + res.toString());
//			System.out.println("Residual  : " + m1.toString());
								
			Matrix difference = res.subtract(m1).getAbsoluteValue();
			if (difference.anyElementLargerThan(0.5)) {
				REpiceaSpecies speciesEnum = tree.getLambert2005Species(); 
				if (!differentLines.containsKey(speciesEnum)) {
					differentLines.put(speciesEnum, new ArrayList<Integer>());
				}
				differentLines.get(speciesEnum).add(csvLine);				
			}			
			
			csvLine++;
		}
		
//		for (Integer i : differentLines) {
//			System.out.println("Found differences at CSV line " + i);
//		}
		int nbDifferences = 0;
		for (List<Integer> diffForAParticularSpecies : differentLines.values()) {
			nbDifferences += diffForAParticularSpecies.size();
		}
					
		System.out.println("Total of " + nbDifferences + " lines different on " + csvLine);
		
		// The csv containing the residual errors to check against has 116 lines of data on 25910 that have been computed with specific 
		// weights that have been manually assigned (ref : e-mail from MC Lambert on Sept 10 2021)
		// We expect those 116 lines to fail, but the other ones should be predicted correctly.
		Assert.assertTrue(nbDifferences == 116);
		
		reader.close();		
	}

	@Test
	public void test05ParameterVariability() throws InterruptedException {
		Lambert2005Tree tree = new Lambert2005TreeReducedImpl(Lambert2005Species.AbiesBalsamea, 20);
		Lambert2005BiomassPredictor predictor = new Lambert2005BiomassPredictor(true);
		List<String> errorList = new ArrayList<String>();
		for (ModelVersion v : predictor.internalPredictors.keySet()) {
			Map<Lambert2005Species, Lambert2005BiomassInternalPredictor> innerMap = predictor.internalPredictors.get(v);
			for (Lambert2005Species s : innerMap.keySet()) {
				try { 
				Matrix betaThisReal = innerMap.get(s).testParametersForThisRealization(tree);
				Matrix betaMean = innerMap.get(s).testMeanParameters();
				Assert.assertTrue(betaThisReal.subtract(betaMean).getAbsoluteValue().anyElementLargerThan(1E-4));
				} catch (UnsupportedOperationException e) {
					errorList.add("Unable to perform Cholesky decomposition for version " + v.name() + " and species " + s.name());
				}
			}
		}

		if (!errorList.isEmpty()) {
			for (String s : errorList) {
				System.err.println(s);
			}
			Assert.fail("Some versions and species have troubles with the Cholesky decomposition!");
		}
	}

	@Test
	public void test06DeterministicFastTrackCompleteVersion() {
		Lambert2005BiomassPredictor pred = new Lambert2005BiomassPredictor();
		double observed = pred.predictTotalBiomassMg("Abies balsamea", 27.7, 22.1);
		Assert.assertEquals("Comparing fasttrack 3-parm model", 0.2632655726, observed, 1E-8);
	}
	
	@Test
	public void test07DeterministicFastTrackReducedVersion() {
		Lambert2005BiomassPredictor pred = new Lambert2005BiomassPredictor();
		double observed = pred.predictTotalBiomassMg("Abies lasiocarpa", 33.1);
		Assert.assertEquals("Comparing fasttrack 2-parm model", 0.38409593131, observed, 1E-8);
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
		REpiceaSpecies.Species repiceaSpecies = REpiceaSpecies.Species.Populus_tremuloides;
		VolumableStand p = new VolumableStandImpl();
		Tree t = new Tree("PET", species, 20, 15);
		double volumeM3 = volPred.predictTreeCommercialUnderbarkVolumeDm3(p, t) * 0.001;
		double overbarkCommercialVolumeM3 = volumeM3 * (1 + repiceaSpecies.getBarkProportionOfWoodVolume(SpeciesLocale.Quebec));
		double totalAbovegroundbiomassMg = bioPred.predictBiomassKg(t).getValueAt(BiomassCompartment.TOTAL.ordinal(), 0) * 0.001;
		double basicWoodDensity = repiceaSpecies.getBasicWoodDensity(SpeciesLocale.Quebec);
		double totalAbovegroundVolumeM3 = totalAbovegroundbiomassMg / basicWoodDensity;
		double possibleExpansionFactor = totalAbovegroundVolumeM3 / overbarkCommercialVolumeM3;
		System.out.println("DBH = " + t.getDbhCm() + "; Height = " + t.getHeightM() + "; BEF = " + possibleExpansionFactor);
//		int u = 0;
	}
	
}
