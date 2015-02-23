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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperTree;
import repicea.math.Matrix;
import repicea.simulation.stemtaper.StemTaperEstimate;
import repicea.simulation.stemtaper.StemTaperSegment;
import repicea.simulation.stemtaper.StemTaperSegmentList;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.WoodPiece;
import repicea.stats.estimates.Estimate;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The SybilleTreeLogCategory is the TreeLogCategory implementation for the SybilleTreeLogger.
 * @author Mathieu Fortin and Jean-Fran�ois Lavoie - January 2012
 */
public class SybilleTreeLogCategory extends TreeLogCategory {
	
	protected static enum LengthID implements TextableEnum {
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


	/**
	 * Preferred constructor for script mode.
	 * @param logName the name of this log category
	 * @param logLengthM the log length (m)
	 * @param smallEndDiameterCm the small-end diameter (cm)
	 */
	protected SybilleTreeLogCategory(String logName, String species, LengthID lengthID, double smallEndDiameterCm) {
		super(logName);
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
	 * double lengthID a LengthID enum 	
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
	public SybilleTreeLogCategoryPanel getGuiInterface() {
		return new SybilleTreeLogCategoryPanel(this);
	}


	/**
	 * This method checks whether or not it is possible to extract this particular log grade from a given tree.
	 * @param tree a LoggableTree instance
	 * @param params a series of parameters (e.g. the height at which the log is to be extracted)
	 * @return a WoodPiece instance if the log can be extracted or null otherwise.
	 */
	protected SybilleWoodPiece extractFromTree(LoggableTree tree, Object... params) {
		if (!(tree instanceof StemTaperTree)) {
			throw new InvalidParameterException("Sybille only uses trees that implement the StemTaperTree interface!");
		} 
		
		StemTaperEstimate estimate = (StemTaperEstimate) params[0];
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
			Estimate<?> volumeEstimateForThisSection;
			double estimatedD2 = estimate.getMean().m_afData[endIndex][0];
			if (estimatedD2 * .01 >= smallEndDiameterCm * smallEndDiameterCm) {	// .01 required to shift from mm2 to cm2
				List<Double> heightsForTheseSegments = new ArrayList<Double>();
				for (int i = startIndex; i <= endIndex; i++) {
					heightsForTheseSegments.add(crossSectionHeights.get(i));
				}
				StemTaperSegmentList segments = StemTaperSegmentList.createStemTaperSegmentList(heightsForTheseSegments, optimize); // true = optimize
				volumeEstimateForThisSection = estimate.getVolumeEstimate(segments);
				SybilleWoodPiece wp = new SybilleWoodPiece(this, tree, volumeEstimateForThisSection, trueLengthM);		// the small diameter is large enough
				return wp;
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
			if (squaredDmm2.m_afData[i][0] * .01 <= squaredSmallEndDiameter) {		// .01 to scale from mm2 to cm2
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
