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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperTree;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.stemtaper.AbstractStemTaperEstimate;
import repicea.simulation.stemtaper.StemTaperSegment;
import repicea.simulation.stemtaper.StemTaperSegmentList;
import repicea.simulation.treelogger.LogCategory;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;
import repicea.stats.estimates.Estimate;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The SybilleTreeLogCategory is the TreeLogCategory implementation for the SybilleTreeLogger.
 * @author Mathieu Fortin and Jean-Fran�ois Lavoie - January 2012
 */
public class SybilleTreeLogCategory extends LogCategory {
	
	public static enum LengthID implements TextableEnum {
		FourFeetLong(1.2446, "124.5 cm (4'1\")", "124,5 cm (4'1\")"),
		EightFeetLong(2.4892, "248.9 cm (8'2\")", "248,9 cm (8'2\")"),
		TwelveFeetLong(3.7338, "373.4 cm (12'3\")", "373,4 cm (12'3\")"),
		NoLimit(0d,"No limit", "Aucune limite");

		private double length;
		
		LengthID(double length, String englishText, String frenchText) {
			this.length = length;
			setText(englishText, frenchText);
		}

		/**
		 * This method returns the length of the log (m) associated to this LengthID.
		 * @return a double
		 */
		protected double getLengthM() {return length;}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
		protected static LengthID findClosestLengthID(double lengthM) {
			LengthID closestLengthID = null;
			double closestDistance = 9999d;
			double currentDistance;
			for (LengthID lengthID : LengthID.values()) {
				currentDistance = Math.abs(lengthID.getLengthM() - lengthM);
				if (currentDistance < closestDistance) {
					closestDistance = currentDistance;
					closestLengthID = lengthID;
				}
			}
			return closestLengthID;
		}
		
		
	}

	private static final long serialVersionUID = 20120114L;
	
	protected final static double FOUR_FEET = 0.0254 * 49;
	
	private double smallEndDiameterCm = 0;
	
	private LengthID lengthID = LengthID.FourFeetLong;
	
	private transient SybilleTreeLogCategoryPanel guiInterface;


	/**
	 * Preferred constructor for script mode.
	 * @param logName the name of this log category
	 * @param species the species name
	 * @param lengthID a LengthID enum 
	 * @param smallEndDiameterCm the small-end diameter (cm)
	 */
	public SybilleTreeLogCategory(String logName, String species, LengthID lengthID, double smallEndDiameterCm) {
		super(logName, false);
		setSpecies(species);
		setLogLengthM(lengthID);
		setSmallEndDiameterCm(smallEndDiameterCm);
	}

	public SybilleTreeLogCategory() {}

	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * This method returns the length for this log category.
	 * @return the length (m)
	 */
	protected double getLogLengthM() {
		return lengthID.getLengthM();
	}

	/**
	 * This method sets the length of the log category.
	 * @param lengthID a LengthID enum 	
	 */	
	protected void setLogLengthM(LengthID lengthID) {this.lengthID = lengthID;}
	

	/**
	 * This method returns the small-end diameter required for this tree log category.
	 * @return the diameter (cm)
	 */
	protected double getSmallEndDiameterCm() {return smallEndDiameterCm;}
	
	/**
	 * This method sets the small-end diameter required for this tree log category.
	 * @param smallEndDiameterCm the diameter (cm)	
	 */	
	protected void setSmallEndDiameterCm(double smallEndDiameterCm) {
		if (smallEndDiameterCm < 0d) {
			throw new InvalidParameterException("The small end diameter cannot be negative!");
		}
		this.smallEndDiameterCm = smallEndDiameterCm;
	}
	
	@Override
	public SybilleTreeLogCategoryPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new SybilleTreeLogCategoryPanel(this);
		}
		return guiInterface;
	}

	
	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}


	/**
	 * This method checks whether or not it is possible to extract this particular log grade from a given tree.
	 * @param tree a LoggableTree instance
	 * @param params a series of parameters (e.g. the height at which the log is to be extracted)
	 * @return a WoodPiece instance if the log can be extracted or null otherwise.
	 */
	@Override
	protected List<SybilleWoodPiece> extractFromTree(LoggableTree tree, Object... params) {
		if (!(tree instanceof StemTaperTree)) {
			throw new InvalidParameterException("Sybille only uses trees that implement the StemTaperTree interface!");
		} 
		
		AbstractStemTaperEstimate estimate = (AbstractStemTaperEstimate) params[0];
		double height = (Double) params[1];
		boolean optimize = (Boolean) params[2];

		List<Double> crossSectionHeights = estimate.getCrossSectionHeights();
		double topHeight = crossSectionHeights.get(crossSectionHeights.size() - 1);
		int startIndex, endIndex;
		
		double heightLimit;
		if (getLogLengthM() == 0) {
			heightLimit = topHeight;
		} else {
			heightLimit = height + getLogLengthM();
		}
		
		if (heightLimit > topHeight) {
			return null;	// the required length is too much for what is left of the tree
		} else {
			startIndex = findCloserCeilingCrossSectionIndex(crossSectionHeights, height);
			if (getLogLengthM() < StemTaperSegment.VERY_SMALL) {	//	no length specification
				endIndex = findCloserFloorCrossSectionIndex(estimate.getMean(), smallEndDiameterCm * smallEndDiameterCm);
				if (endIndex == -1 || startIndex == endIndex) {	// means the minimum diameter is already too large 
					return null;
				}
			} else {		// length is specified
				endIndex = findCloserCeilingCrossSectionIndex(crossSectionHeights, height + getLogLengthM());
			}
			double trueLengthM = crossSectionHeights.get(endIndex) - crossSectionHeights.get(startIndex); 
			Estimate<Matrix, SymmetricMatrix, ?> volumeEstimateForThisSection;
			double estimatedD2 = estimate.getMean().getValueAt(endIndex, 0);
			if (estimatedD2 * .01 >= smallEndDiameterCm * smallEndDiameterCm) {	// .01 required to shift from mm2 to cm2
				List<Double> heightsForTheseSegments = new ArrayList<Double>();
				for (int i = startIndex; i <= endIndex; i++) {
					heightsForTheseSegments.add(crossSectionHeights.get(i));
				}
				StemTaperSegmentList segments = StemTaperSegmentList.createStemTaperSegmentList(heightsForTheseSegments, optimize); // true = optimize
				volumeEstimateForThisSection = estimate.getVolumeEstimate(segments);
				SybilleWoodPiece wp = new SybilleWoodPiece(this, tree, volumeEstimateForThisSection, trueLengthM);		// the small diameter is large enough
				List<SybilleWoodPiece> pieces = new ArrayList<SybilleWoodPiece>();
				pieces.add(wp);
				return pieces;
			} else {
				return null;
			}
		}
	}
	
	
	private int findCloserCeilingCrossSectionIndex(List<Double> heights, double targetHeight) {
		int i;
		for (i = 0; i < heights.size(); i++) {
			if (heights.get(i) >= targetHeight) {
				return i;
			}
		}
		return -1;
	}

	
	private int findCloserFloorCrossSectionIndex(Matrix squaredDmm2, double squaredSmallEndDiameter) {
		int i;
		for (i = 0; i < squaredDmm2.m_iRows; i++) {
			if (squaredDmm2.getValueAt(i, 0) * .01 <= squaredSmallEndDiameter) {		// .01 to scale from mm2 to cm2
				return i - 1;
			}
		}
		return squaredDmm2.m_iRows - 1;				// means the last section which is the top of the tree
	}

	
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		
		SybilleTreeLogCategory refCategory = (SybilleTreeLogCategory) obj;
		
		if (refCategory.getSmallEndDiameterCm() != this.getSmallEndDiameterCm()) {
			return false;
		}
		
		if (refCategory.getLogLengthM() != this.getLogLengthM()) {
			return false;
		}
		
		return true;
	}

}
