/*
 * This file is part of the mrnf-foresttools library.
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
package quebecmrnfutility.predictor.volumemodels.honertotalvolume;

import java.util.HashMap;
import java.util.Map;

import quebecmrnfutility.predictor.volumemodels.honertotalvolume.HonerTotalVolumeTree.HonerTotalVolumeTreeSpecies;
import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictor;

/**
 * The HonerTotalVolumePredictor class implements the underbark volume model developed 
 * by Honer 1983. It provides the total tree volume underbark based on tree dbh and height. <br>
 * <br>
 * IMPORTANT: The implementation is only deterministic since the covariance matrix and the variance of
 * the residual error terms are not provided.
 * @author Mathieu Fortin - March 2013
 */
@SuppressWarnings("serial")
public class HonerTotalVolumePredictor extends REpiceaPredictor {

	private final Map<HonerTotalVolumeTreeSpecies, Matrix> betaMap;
		
	/**
	 * General constructor for this class. 
	 */
	public HonerTotalVolumePredictor() {
		super(false, false,false);
		betaMap = new HashMap<HonerTotalVolumeTreeSpecies, Matrix>();
		init();
	}
	
	@Override
	protected final void init() {
		Matrix mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.SAB, mat);
		mat.setValueAt(0, 0, 2.139);
		mat.setValueAt(1, 0, 91.938);
		mat.setValueAt(2, 0, 0.004331);
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.PRU, mat);
		mat.setValueAt(0, 0, 1.112);
		mat.setValueAt(1, 0, 106.708);
		mat.setValueAt(2, 0, 0.004330);
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.THO, mat);
		mat.setValueAt(0, 0, 4.167);
		mat.setValueAt(1, 0, 74.647);
		mat.setValueAt(2, 0, 0.004330);
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.PIG, mat);
		mat.setValueAt(0, 0, 0.897);
		mat.setValueAt(1, 0, 106.232);
		mat.setValueAt(2, 0, 0.004331);
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.PIB, mat);
		mat.setValueAt(0, 0, 0.691);
		mat.setValueAt(1, 0, 110.848);
		mat.setValueAt(2, 0, 0.004319);
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.PIR, mat);
		mat.setValueAt(0, 0, 0.710);
		mat.setValueAt(1, 0, 108.394);
		mat.setValueAt(2, 0, 0.004331);
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.EPN, mat);
		mat.setValueAt(0, 0, 1.588);
		mat.setValueAt(1, 0, 101.609);
		mat.setValueAt(2, 0, 0.004327);
	
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.EPB, mat);
		mat.setValueAt(0, 0, 1.440);
		mat.setValueAt(1, 0, 104.295);
		mat.setValueAt(2, 0, 0.004322);

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.EPR, mat);
		mat.setValueAt(0, 0, 1.226);
		mat.setValueAt(1, 0, 96.266);
		mat.setValueAt(2, 0, 0.004325);
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.BOJ, mat);
		mat.setValueAt(0, 0, 1.449);
		mat.setValueAt(1, 0, 105.081);
		mat.setValueAt(2, 0, 0.004320);

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.BOP, mat);
		mat.setValueAt(0, 0, 2.222);
		mat.setValueAt(1, 0, 91.554);
		mat.setValueAt(2, 0, 0.004322);

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.CET, mat);
		mat.setValueAt(0, 0, 0.033);
		mat.setValueAt(1, 0, 119.889);
		mat.setValueAt(2, 0, 0.004334);

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.ERR, mat);
		betaMap.put(HonerTotalVolumeTreeSpecies.ERS, mat);
		betaMap.put(HonerTotalVolumeTreeSpecies.ERN, mat);
		mat.setValueAt(0, 0, 1.046);
		mat.setValueAt(1, 0, 117.035);
		mat.setValueAt(2, 0, 0.004334);

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.PET, mat);
		mat.setValueAt(0, 0, -0.312);
		mat.setValueAt(1, 0, 133.101);
		mat.setValueAt(2, 0, 0.004341);

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.PEB, mat);
		mat.setValueAt(0, 0, 0.420);
		mat.setValueAt(1, 0, 120.287);
		mat.setValueAt(2, 0, 0.004341);
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.HEG, mat);
		mat.setValueAt(0, 0, 0.959);
		mat.setValueAt(1, 0, 102.056);
		mat.setValueAt(2, 0, 0.004334);
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.CHR, mat);
		mat.setValueAt(0, 0, 1.512);
		mat.setValueAt(1, 0, 102.568);
		mat.setValueAt(2, 0, 0.004334);
		
		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.OSV, mat);
		mat.setValueAt(0, 0, 1.877);
		mat.setValueAt(1, 0, 101.372);
		mat.setValueAt(2, 0, 0.004334);

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.TIL, mat);
		mat.setValueAt(0, 0, 0.948);
		mat.setValueAt(1, 0, 122.364);
		mat.setValueAt(2, 0, 0.004334);

		mat = new Matrix(3,1);
		betaMap.put(HonerTotalVolumeTreeSpecies.ORA, mat);
		mat.setValueAt(0, 0, 0.634);
		mat.setValueAt(1, 0, 134.263);
		mat.setValueAt(2, 0, 0.004334);
		
	}
	
	/**
	 * This method returns the total volume of a particular tree.
	 * @param tree a HonerTotalVolumeTree instance
	 * @return the total volume (m3)
	 */
	public double predictTreeTotalUnderbarkVolume(HonerTotalVolumeTree tree) {
		HonerTotalVolumeTreeSpecies species = tree.getHonerSpecies();
		Matrix beta = betaMap.get(species);
		
		double a0 = beta.getValueAt(0, 0);
		double a1 = beta.getValueAt(1, 0);
		double a2 = beta.getValueAt(2, 0);
		double h = tree.getHeightM();
		
		
		return a2 * tree.getSquaredDbhCm() / (a0 + a1 / h);
	}
	

}
