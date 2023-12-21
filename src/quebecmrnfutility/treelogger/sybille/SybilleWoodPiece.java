/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec
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
package quebecmrnfutility.treelogger.sybille;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.treelogger.LogCategory;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;
import repicea.stats.estimates.Estimate;

/**
 * The SybilleWoodPiece is the WoodPiece derived class for the 
 * Sybille tree logger. The volume originally calculated is that 
 * UNDER bark.
 * @author Mathieu Fortin - March 2012
 */
public class SybilleWoodPiece extends WoodPiece {
	
	private static final long serialVersionUID = 20120310L;
	
//	private Estimate<?> volumeEstimate;
	private double trueLengthM;
		
	protected SybilleWoodPiece(LogCategory logCategory, LoggableTree tree, Estimate<Matrix, SymmetricMatrix, ?> volumeEstimate, double trueLengthM) {
		super(logCategory, tree, false, volumeEstimate.getMean().getSumOfElements() * .001);		// with bark is false
//		this.volumeEstimate = volumeEstimate;
		this.trueLengthM = trueLengthM;
	}
	
	@Override
	public String toString() {
		return "Log category = " + getLogCategory().getName() + "; Species = " + getTreeFromWhichComesThisPiece().getSpeciesName();
	}

//	/**
//	 * Do not use this method. Use setVolumeDm3(Estimate) instead.
//	 */
//	@Deprecated
//	@Override
//	protected void setOverBarkVolumeM3(double volumeOfThisWoodPiece_m3) {}
//
//	@Override
//	public double getOverBarkVolumeM3() {
//		return volumeEstimate.getMean().getSumOfElements() * .001;				
//	}
	
	/**
	 * This method returns the true length of the log.
	 * @return the log length (m)
	 */
	public double getLength() {return trueLengthM;}
	
}
