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

import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictor;
import repicea.simulation.stemtaper.StemTaperEstimate;
import repicea.simulation.stemtaper.StemTaperSegment;
import repicea.simulation.stemtaper.StemTaperSegmentList;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.stats.integral.CompositeSimpsonRule;
import repicea.stats.integral.TrapezoidalRule;

/**
 * The SybilleTreeLogger class is a TreeLogger extended class that relies on Schneider et al.'s taper
 * equations to buck the trees into logs of user-specified length.
 * @author Mathieu Fortin and Jean-Fran�ois Lavoie - January 2012
 */
public class SybilleTreeLogger extends TreeLogger<SybilleTreeLoggerParameters, SybilleLoggableTree> {

	private StemTaperPredictor stp;
	private StemTaperSegmentList segments;
	
	/**
	 * Constructor. 
	 * @throws IOException if the StemTaperPredictor cannot be instantiated properly
	 */
	public SybilleTreeLogger() throws IOException {
		super();
		stp = new StemTaperPredictor();
		segments = new StemTaperSegmentList();
	}
	

	@Override
	protected void logThisTree(SybilleLoggableTree tree) {
		stp.setEstimationMethod(getTreeLoggerParameters().getEstimationMethod());
		boolean optimize = getTreeLoggerParameters().isIntegrationOptimizationEnabled();

		SybilleLoggableTree t = (SybilleLoggableTree) tree;

		StemTaperSegment segment;
		double heightM;
		String speciesName;
		StemTaperEstimate estimate;
		
		segments.clear(); 
		speciesName = t.getStemTaperTreeSpecies().name();
		heightM = getTreeLoggerParameters().getStumpHeightM();		// always starts from the specified stump height
		if (!optimize) {
			segments.add(new StemTaperSegment(heightM, t.getHeightM()- StemTaperSegment.VERY_SMALL, new TrapezoidalRule(0.0254 * 10)));
		} else {
			double bottomHeight = heightM;
			double topHeight = bottomHeight + SybilleTreeLogCategory.FOUR_FEET; 	// four feet long sections
			while (topHeight < t.getHeightM()) {
				segment = new StemTaperSegment(bottomHeight, topHeight, new CompositeSimpsonRule(2));
				segments.add(segment);
				bottomHeight += SybilleTreeLogCategory.FOUR_FEET;
				topHeight = bottomHeight + SybilleTreeLogCategory.FOUR_FEET;
			}
			if (bottomHeight > 0 && bottomHeight < (t.getHeightM() - StemTaperSegment.VERY_SMALL)) {
				segments.add(new StemTaperSegment(bottomHeight, t.getHeightM() - StemTaperSegment.VERY_SMALL, new TrapezoidalRule(1d)));
			}
		}

//		stp.setTree(t);

		estimate = stp.getPredictedTaperForTheseSegments(t, segments);
		SybilleWoodPiece wp;
		do {
			wp = null;
			for (SybilleTreeLogCategory logCategory : getTreeLoggerParameters().getLogCategories().get(speciesName)) {
				wp = logCategory.extractFromTree(t, estimate, heightM, optimize);
				if (wp != null) {
					heightM += wp.getLength();		// we add the length of the log to the heightM variable
					break;
				}
			}
			if (wp != null) {				// if wp is null it means that no log grade could be extracted, i.e. the log grade requirements are not met
				addWoodPiece(t, wp);
			} 
		} while (wp != null);
	}

	@Override
	public void setTreeLoggerParameters() {
		SybilleTreeLoggerParameters stlp = createDefaultTreeLoggerParameters();				
		stlp.showInterface(null);
		setTreeLoggerParameters(stlp);
	}

	@Override
	public SybilleTreeLoggerParameters createDefaultTreeLoggerParameters() {
		SybilleTreeLoggerParameters stlp = new SybilleTreeLoggerParameters();
		stlp.initializeDefaultLogCategories();
		return stlp;
	}

//	@Override
//	public void init(Collection<SybilleLoggableTree> loggableTrees) {
//		Collection<SybilleLoggableTree> sybilleLoggableTrees = getValidSybilleLoggableTreesFromACollection(loggableTrees);
//		super.init(sybilleLoggableTrees);
//	}

//	/**
//	 * This method extracts a collection of TreePetroLoggable objects from a collection of LoggableTree instances.
//	 * @param trees a Collection of LoggableTree-derived instances
//	 * @return a Collection of PetroLoggableTree instances
//	 */
//	private Collection<SybilleLoggableTree> getValidSybilleLoggableTreesFromACollection(Collection<? extends LoggableTree> loggableTrees) {
//		Collection<SybilleLoggableTree> sybilleLoggableTrees = new ArrayList<SybilleLoggableTree>();
//		for (LoggableTree t : loggableTrees) {
//			if (t instanceof SybilleLoggableTree) {
//				SybilleLoggableTree tree = (SybilleLoggableTree) t;
//				if (isEligible(tree)) {
//					sybilleLoggableTrees.add(tree);
//				}
//			}
//		}
//		return sybilleLoggableTrees;
//	}

	
	public static void main (String[] args) throws IOException {
		SybilleTreeLogger log = new SybilleTreeLogger();
		log.setTreeLoggerParameters();
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




}