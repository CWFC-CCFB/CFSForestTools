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

import java.io.IOException;
import java.util.List;

import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperPredictor;
import repicea.math.integral.CompositeSimpsonRule;
import repicea.math.integral.TrapezoidalRule;
import repicea.simulation.stemtaper.AbstractStemTaperEstimate;
import repicea.simulation.stemtaper.StemTaperSegment;
import repicea.simulation.stemtaper.StemTaperSegmentList;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.TreeLoggerCompatibilityCheck;

/**
 * The SybilleTreeLogger class is a TreeLogger extended class that relies on Schneider et al.'s taper
 * equations to buck the trees into logs of user-specified length.
 * @author Mathieu Fortin and Jean-Francois Lavoie - January 2012
 */
public class SybilleTreeLogger extends TreeLogger<SybilleTreeLoggerParameters, SybilleLoggableTree> {

	private StemTaperPredictor stp;
	private StemTaperSegmentList segments;
	private final boolean isVariabilityEnabled;
	
	/**
	 * Official constructor for the extension
	 */
	public SybilleTreeLogger() {
		this(false);
	}
	
	/**
	 * General constructor. 
	 * @param isVariabilityEnabled true to enable the stochastic mode 
	 */
	public SybilleTreeLogger(boolean isVariabilityEnabled) {
		super();
		this.isVariabilityEnabled = isVariabilityEnabled;
		stp = new StemTaperPredictor(isVariabilityEnabled);
		segments = new StemTaperSegmentList();
	}
	
	@Override
	protected void logThisTree(SybilleLoggableTree tree) {
		boolean optimize = getTreeLoggerParameters().isIntegrationOptimizationEnabled();

		SybilleLoggableTree t = (SybilleLoggableTree) tree;

		StemTaperSegment segment;
		double heightM;
		String speciesName;
		AbstractStemTaperEstimate estimate;
		
		segments.clear(); 
		speciesName = t.getStemTaperTreeSpecies().name();
		heightM = getTreeLoggerParameters().getStumpHeightM();		// always starts from the specified stump height
		if (optimize) {
			double bottomHeight = heightM;
			double topHeight = bottomHeight + SybilleTreeLogCategory.FOUR_FEET; 	// four feet long sections
			while (topHeight < t.getHeightM()) {
				segment = new StemTaperSegment(bottomHeight, topHeight, new CompositeSimpsonRule(2));
				segments.add(segment);
				bottomHeight += SybilleTreeLogCategory.FOUR_FEET;
				topHeight = bottomHeight + SybilleTreeLogCategory.FOUR_FEET;
			}
			if (bottomHeight > 0 && bottomHeight < (t.getHeightM() - 2 * StemTaperSegment.VERY_SMALL)) { // there must be at least 2mm between bottom height and tree height leaving a clearance of 1mm
				if ((t.getHeightM() - StemTaperSegment.VERY_SMALL) - bottomHeight > SybilleTreeLogCategory.FOUR_FEET * 0.5) {	// if the section is more than 0.5 m long
					segments.add(new StemTaperSegment(bottomHeight, t.getHeightM() - StemTaperSegment.VERY_SMALL, new CompositeSimpsonRule(2)));
				} else {		// if less, than a simple segment
					segments.add(new StemTaperSegment(bottomHeight, t.getHeightM() - StemTaperSegment.VERY_SMALL, new TrapezoidalRule(1d)));
				}
			}
		} else {
			double preferredLength = 0.0254 * 10; // ten inches
			double boleLength = t.getHeightM() - StemTaperSegment.VERY_SMALL - getTreeLoggerParameters().getStumpHeightM();
			int nbOfCompleteSegments = (int) Math.floor(boleLength / preferredLength);
			while (boleLength - nbOfCompleteSegments * preferredLength < StemTaperSegment.VERY_SMALL) {	// the remainder is too small so we will reduce the preferred length
				preferredLength -= .0001;
				nbOfCompleteSegments = (int) Math.floor(boleLength / preferredLength);
			}
			segments.add(new StemTaperSegment(heightM, t.getHeightM()- StemTaperSegment.VERY_SMALL, new TrapezoidalRule(preferredLength)));
		}

		try {
			estimate = stp.getPredictedTaperForTheseSegments(t, segments, getTreeLoggerParameters().getEstimationMethod());
			List<SybilleWoodPiece> pieces;
			do {
				pieces = null;
				for (SybilleTreeLogCategory logCategory : getTreeLoggerParameters().getLogCategories().get(speciesName)) {
					pieces = logCategory.extractFromTree(t, estimate, heightM, optimize);
					if (pieces != null) {
						for (SybilleWoodPiece wp : pieces) {
							heightM += wp.getLength();		// we add the length of the log to the heightM variable
							addWoodPiece(t, wp);
						}
						break;
					}
				}
//				if (pieces != null) {				// if wp is null it means that no log grade could be extracted, i.e. the log grade requirements are not met
//					addWoodPiece(t, wp);
//				} 
			} while (pieces != null);
//			if (getWoodPieces().get(t) == null) {
//				System.out.println("Sybille could not extract any wood piece from tree : " + t.getSpeciesName() + t.getSubjectId());
//				heightM = getTreeLoggerParameters().getStumpHeightM();
//				do {
//					wp = null;
//					for (SybilleTreeLogCategory logCategory : getTreeLoggerParameters().getLogCategories().get(speciesName)) {
//						wp = logCategory.extractFromTree(t, estimate, heightM, optimize);
//						if (wp != null) {
//							heightM += wp.getLength();		// we add the length of the log to the heightM variable
//							break;
//						}
//					}
//					if (wp != null) {				// if wp is null it means that no log grade could be extracted, i.e. the log grade requirements are not met
//						addWoodPiece(t, wp);
//					} 
//				} while (wp != null);
//			}
		} catch (Exception e) {
			System.out.println("Sybille could not log tree : " + t.getSpeciesName() + t.getSubjectId());
			e.printStackTrace();
//			heightM = getTreeLoggerParameters().getStumpHeightM();
//			estimate = stp.getPredictedTaperForTheseSegments(t, segments, getTreeLoggerParameters().getEstimationMethod());
		}
	}

	@Override
	public void setTreeLoggerParameters() {
		SybilleTreeLoggerParameters stlp = createDefaultTreeLoggerParameters();				
		setTreeLoggerParameters(stlp);
		stlp.showUI(null);
	}

	@Override
	public SybilleTreeLoggerParameters createDefaultTreeLoggerParameters() {
		SybilleTreeLoggerParameters stlp = new SybilleTreeLoggerParameters();
		stlp.initializeDefaultLogCategories();
		return stlp;
	}
	
	@Override
	public SybilleLoggableTree getEligible(LoggableTree t) {
		if (t instanceof SybilleLoggableTree) {
			SybilleLoggableTree tree = (SybilleLoggableTree) t;
			if (tree.getStemTaperTreeSpecies() != null) {
				return tree;
			}
		}
		return null;
	}

	protected boolean isVariabilityEnabled() {return isVariabilityEnabled;}

	@Override
	public boolean isCompatibleWith(TreeLoggerCompatibilityCheck check) {
		return check.getTreeInstance() instanceof SybilleLoggableTree;
	}


	public static void main (String[] args) throws IOException {
		SybilleTreeLogger log = new SybilleTreeLogger();
		log.setTreeLoggerParameters();
	}


}
