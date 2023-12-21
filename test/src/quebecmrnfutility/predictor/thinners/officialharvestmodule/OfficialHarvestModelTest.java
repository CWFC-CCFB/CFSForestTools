/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge-Epicea
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
package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.junit.Test;

import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestableTree.OfficialHarvestableSpecies;
import repicea.simulation.disturbances.DisturbanceParameter;
import repicea.util.ObjectUtility;


public class OfficialHarvestModelTest {
	
	@Test
    public void PredictedProbabilitiesTest() throws Exception {
		Collection<OfficialHarvestableStand> stands = readData();
		OfficialHarvestModel harvester = new OfficialHarvestModel();
		Map<String, Object> parms = new HashMap<String, Object>();
		for (OfficialHarvestableStand stand : stands) {
			for (OfficialHarvestableTree tree : ((OfficialHarvestableStandImpl) stand).getTrees()) {
				System.out.println("Treatment = " + ((OfficialHarvestableStandImpl) stand).getTreatment().toString() + ", Species = " + tree.getOfficialHarvestableTreeSpecies(((OfficialHarvestableStandImpl) stand).getTreatment()).toString());
				parms.put(DisturbanceParameter.ParmTreatment, ((OfficialHarvestableStandImpl) stand).getTreatment());
				parms.put(DisturbanceParameter.ParmModulation, 0);
				double actual = (Double) harvester.predictEvent(stand, tree, parms);
				double expected = ((OfficialHarvestableTreeImpl) tree).getPredictedProbabilityFromFile();
				assertEquals(expected, actual, 1E-5);

			}
		}
		
		
	}

	
	@Test
    public void PredictedProbabilitiesTestWithModifier() throws Exception {
		Collection<OfficialHarvestableStand> stands = readData();
		OfficialHarvestModel harvester = new OfficialHarvestModel();
		Map<String, Object> parms = new HashMap<String, Object>();
		for (OfficialHarvestableStand stand : stands) {
			for (OfficialHarvestableTree tree : ((OfficialHarvestableStandImpl) stand).getTrees()) {
				System.out.println("Treatment = " + ((OfficialHarvestableStandImpl) stand).getTreatment().toString() +", Species = " + tree.getOfficialHarvestableTreeSpecies(((OfficialHarvestableStandImpl) stand).getTreatment()).toString());
				parms.put(DisturbanceParameter.ParmTreatment, ((OfficialHarvestableStandImpl) stand).getTreatment());
				parms.put(DisturbanceParameter.ParmModulation, 50);
				double actual = harvester.predictEventProbability(stand, tree, parms);
				double expected = ((OfficialHarvestableTreeImpl) tree).getPredictedProbabilityFromFile() * 1.5;
				if (expected > 1d) {
					expected = 1;
				}
				assertEquals(expected, actual, 1E-5);

			}
		}
		
		
	}

	@Test
    public void PredictedProbabilitiesTestUnderStochasticImplementation() throws Exception {
		List<OfficialHarvestableStand> stands = readData();
		OfficialHarvestModel harvester = new OfficialHarvestModel(true);
		OfficialHarvestableStand stand = stands.get(0);
		List<OfficialHarvestableTree> trees = new ArrayList<OfficialHarvestableTree>();
		for (OfficialHarvestableSpecies species : OfficialHarvestableSpecies.values()) {
			trees.add(new OfficialHarvestableTreeImpl(species, 30d, 0d));
		}
		Map<String, Object> parms = new HashMap<String, Object>();
		for (TreatmentType treatment : TreatmentType.values()) {
			for (OfficialHarvestableTree tree : trees) {
				System.out.println("Testing treatment : " + treatment.name() + " and species " + tree.getOfficialHarvestableTreeSpecies(treatment));
				parms.put(DisturbanceParameter.ParmTreatment, treatment);
				parms.put(DisturbanceParameter.ParmModulation, 0);
				
				harvester.predictEventProbability(stand, tree, parms);
			}
		}
	}

	
	
	private static List<OfficialHarvestableStand> readData() throws Exception {
		List<OfficialHarvestableStand> stands = new ArrayList<OfficialHarvestableStand>();
		String path = ObjectUtility.getRelativePackagePath(OfficialHarvestModelTest.class);
//		InputStream fileToRead = ClassLoader.getSystemResourceAsStream(path + "testTrees.txt");
		InputStream fileToRead = OfficialHarvestModelTest.class.getResourceAsStream("/" + path + "testTrees.txt");
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(fileToRead));;
			String str = in.readLine();
			while (str != null) {
				// comment / blank line : goes to next line
				if (!str.startsWith("#") && str.trim().length() != 0) {
					// System.err.println (str);
					double nbHa;
					String species;
					double dbh;
					String treatment;
					double predProb;
					try {
						StringTokenizer tkz = new StringTokenizer(str, ";");
						nbHa = Double.parseDouble(tkz.nextToken());
						species = tkz.nextToken();
						dbh = Double.parseDouble(tkz.nextToken());
						predProb = Double.parseDouble(tkz.nextToken());
						treatment = tkz.nextToken();
						
						OfficialHarvestableTree tree = new OfficialHarvestableTreeImpl(species, dbh, predProb);
						OfficialHarvestableStand stand = new OfficialHarvestableStandImpl(nbHa, treatment);
						((OfficialHarvestableStandImpl) stand).addTree(tree);
						stands.add(stand);
					} catch (NumberFormatException e) {
						throw e;
					} catch (NoSuchElementException e) {
						throw e;
					}
				}
				str = in.readLine();
			}
		} catch (Exception e) {
			System.out.println("FileLoader.loadVectorFormFile() : Unable to read table");
			throw e;
		}
		
		return stands;
	}

}
