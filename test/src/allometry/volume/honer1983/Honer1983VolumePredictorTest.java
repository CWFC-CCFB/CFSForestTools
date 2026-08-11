/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2013 Mathieu Fortin for Rouge-Epicea
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
package allometry.volume.honer1983;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.util.ObjectUtility;

public class Honer1983VolumePredictorTest {
	
	@SuppressWarnings("rawtypes")
	@Test
	public void TotalVolumeTest() throws IOException, ClassNotFoundException {
		
		String path = ObjectUtility.getPackagePath(getClass());
		
		String referenceFilename = path + "referenceResults.ser";
		
		List<Species> speciesNames = new ArrayList<Species>();
		speciesNames.add(Species.Betula_alleghaniensis);
		speciesNames.add(Species.Picea_rubens);
		speciesNames.add(Species.Acer_saccharum);
		speciesNames.add(Species.Abies_balsamea);
		double height;
		List<Honer1983VolumableTreeImpl> trees = new ArrayList<Honer1983VolumableTreeImpl>();
		for (Species speciesName : speciesNames) {
			for (double dbh = 2; dbh <= 40; dbh += 2) {
				height = 1.4 * dbh - 0.02 * dbh * dbh;
				trees.add(new Honer1983VolumableTreeImpl(speciesName, dbh, height));
			}
		}

		Honer1983VolumePredictor pred = new Honer1983VolumePredictor();
		List<Double> predictions = new ArrayList<Double>();
		double volume;
		for (Honer1983VolumableTreeImpl tree : trees) {
			volume = pred.predictTreeTotalUnderbarkVolumeM3(tree);
			predictions.add(volume);
//			System.out.println("Species " + tree.getHonerSpecies().toString() + "; dbh2 " + tree.getSquaredDbhCm() + "; height " + tree.getHeightM() + "; volume " + volume);
		}
		
//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//	  	try {
//	  		FileOutputStream fos = new FileOutputStream(referenceFilename);
//	  		ObjectOutputStream out = new ObjectOutputStream(fos);
//	  		out.writeObject(predictions);
//	  		out.close();
//	  	} catch(IOException ex) {
//	  		ex.printStackTrace();
//	  		throw ex;
//	  	}

	  	System.out.println("Loading reference map...");
	  	List refList;
	  	try {
	  		FileInputStream fis = new FileInputStream(referenceFilename);
	  		ObjectInputStream in = new ObjectInputStream(fis);
	  		refList = (List) in.readObject();
	  		in.close();
	  	} catch(IOException ex) {
	  		ex.printStackTrace();
	  		throw ex;
	  	}

	  	System.out.println("Comparing results...");
  		assertEquals("Number of values", refList.size(), predictions.size());
	  	
	  	for (int i = 0; i < refList.size(); i++) {
	  		double valueRef = (Double) refList.get(i);
	  		double currentValue = predictions.get(i);
	  		assertEquals("Testing value " + i, valueRef, currentValue, 2E-4);
	  	}
	
	}
	

}
