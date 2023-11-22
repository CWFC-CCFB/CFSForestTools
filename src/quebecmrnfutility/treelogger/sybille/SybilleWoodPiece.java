/*
 * English version follows
 * 
 * Ce fichier fait partie de la biblioth�que mrnf-foresttools.
 * Il est prot�g� par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du minist�re des Ressources naturelles et de la Faune du Gouvernement du 
 * Qu�bec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Qu�bec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
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
