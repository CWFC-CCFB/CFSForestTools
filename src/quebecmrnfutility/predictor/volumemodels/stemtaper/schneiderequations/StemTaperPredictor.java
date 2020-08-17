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
package quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperEquationSettings.ModelType;
import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperTree.StemTaperTreeSpecies;
import repicea.math.Matrix;
import repicea.serial.xml.XmlSerializerChangeMonitor;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.stemtaper.AbstractStemTaperEstimate;
import repicea.simulation.stemtaper.AbstractStemTaperPredictor;
import repicea.simulation.stemtaper.StemTaperCrossSection;
import repicea.simulation.stemtaper.StemTaperSegmentList;
import repicea.stats.estimates.ConfidenceInterval;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

/**
 * The StemTaperEquation class predicts the taper and the volume of different height section in trees.
 * The taper equation returns the underbark diameter and consequently, the volume estimated from the 
 * diameter is the underbark volume.
 * The class must be used as follows
 * </br>
 * </br>
 * {@code StemTaperPredictor ste = new StemTaperPredictor();} </br>
 * {@code ste.setTree(}<i>{@code myTree}</i>{@code );}</br>
 * {@code StemTaperEstimate te = ste.predictTaperForTheseHeights(}<i>{@code myRowMatrixThatContainsTheHeightSections}</i>{@code );} </br>
 * {@code Estimate volumeEstimate = te.getVolumeEstimate()} </br>
 * </br>
 * 
 * NOTE: this method is not synchronized!
 * 
 * @author Mathieu Fortin - September 2011
 */
public final class StemTaperPredictor extends AbstractStemTaperPredictor {
	
	
	static {
		XmlSerializerChangeMonitor.registerClassNameChange("quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictor$EstimationMethod", 
				"quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperPredictor$EstimationMethodInDeterministicMode");
	}
	
	
	@SuppressWarnings("serial")
	public static class SchneiderStemTaperEstimate extends AbstractStemTaperEstimate {
		
		public SchneiderStemTaperEstimate(List<Double> computedHeights) {
			super(computedHeights);
		}

		@Override
		protected Matrix getSquaredDiameters(Matrix predictedDiameters) {
			return predictedDiameters;		// already squared
		}

		@Override
		protected Matrix getVarianceOfSquaredDiameter(Matrix variancePredictedDiameters) {
			return variancePredictedDiameters; // already squared
		}

		@Override
		protected double getScalingFactor() {
			return Math.PI *.25 * 1E-3;
		}

		@Override
		public ConfidenceInterval getConfidenceIntervalBounds(double oneMinusAlpha) {
			return null;
		}
		
	}
	
	
	
	private static final long serialVersionUID = 20120110L;
	
	public static int[] timer = new int[15]; 

	/**
	 * This enum variable applies for estimation methods in deterministic mode. It
	 * is useless when the stem taper model is stochastic.
	 * @author Mathieu Fortin - December 2016
	 */
	public static enum EstimationMethodInDeterministicMode {FirstOrder, 
		SecondOrder, 
		FirstOrderMeanOnly,
		SecondOrderMeanOnly}

	private final Map<ModelType, Map<StemTaperTreeSpecies, StemTaperSubModule>> subModules;
	
	/**
	 * Simple constructor with no variability.
	 * @throws Exception
	 */
	public StemTaperPredictor() {
		this(false);
	}
	
	/**
	 * General constructor.
	 * @throws Exception
	 */
	public StemTaperPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
		subModules = new HashMap<ModelType, Map<StemTaperTreeSpecies, StemTaperSubModule>>();
		
		init();
	}

	@Override
	protected final void init() {
		try {
			for (ModelType modelType : ModelType.values()) {
				subModules.put(modelType, new HashMap<StemTaperTreeSpecies, StemTaperSubModule>());
				for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
					boolean doIt = true;
					if (species == StemTaperTreeSpecies.PIB && modelType == ModelType.HYBRIDMODEL) {
						doIt = false;
					}
					if (doIt) {
						StemTaperSubModule currentSubModule = new StemTaperSubModule(modelType, species, isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
						subModules.get(modelType).put(species, currentSubModule);
						
						String path = ObjectUtility.getRelativePackagePath(getClass());
						String prefix = modelType + "param" + "/" + species.name().toLowerCase() + "/";
						prefix = prefix.toLowerCase();
						String suffix = species.name().toUpperCase().concat(".csv");

						String parameterFilename = path + prefix + "parameters".concat(suffix);
						String omegaFilename = path + prefix + "omega".concat(suffix);
						String plotRandomEffectsFilename = path + prefix + "plotRandomEffects".concat(suffix);
						String treeRandomEffectsFilename = path + prefix + "treeRandomEffects".concat(suffix);
						String correlationStructureFilename = path + prefix + "corrStruct".concat(suffix);
						String varFunctFilename = path + prefix + "varFunct".concat(suffix);
						String residStdDevFilename = path + prefix + "residualStdDev".concat(suffix);

						Matrix beta = ParameterLoader.loadVectorFromFile(parameterFilename).get();
						Matrix omega = ParameterLoader.loadMatrixFromFile(omegaFilename);
						
						currentSubModule.setParameterEstimates(new ModelParameterEstimates(beta, omega));

						Matrix g = ParameterLoader.loadMatrixFromFile(plotRandomEffectsFilename);
						if (g.anyElementDifferentFrom(0d)) {
							currentSubModule.setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(new Matrix(g.m_iRows, 1), g));
						} else { // means there is no random effect at this level
//							System.out.println("Schneider stem taper predictor : No random effect at " + HierarchicalLevel.PLOT.toString() + " level for species " + species.name() + " for version " + modelType.name());
						}

						g = ParameterLoader.loadMatrixFromFile(treeRandomEffectsFilename);
						if (g.anyElementDifferentFrom(0d)) {
							currentSubModule.setDefaultRandomEffects(HierarchicalLevel.TREE, new GaussianEstimate(new Matrix(g.m_iRows, 1), g));
						} else { // means there is no random effect at this level
//							System.out.println("Schneider stem taper predictor : No random effect at " + HierarchicalLevel.TREE.toString() + " level for species " + species.name() + " for version " + modelType.name());
						}

						Matrix oVec = ParameterLoader.loadVectorFromFile(varFunctFilename).get();
						currentSubModule.setVarianceParameters(oVec);

						oVec = ParameterLoader.loadVectorFromFile(correlationStructureFilename).get();
						double currentValue, expCurrentValue;
						for (int i = 0; i < oVec.m_iRows; i++) {		// link function following the implementation in nlme
							currentValue = oVec.m_afData[i][0];
							expCurrentValue = Math.exp(currentValue);
							oVec.m_afData[i][0] = expCurrentValue / (1 + expCurrentValue);
						}
						currentSubModule.setCorrelationParameter(oVec.m_afData[0][0]);

						oVec = ParameterLoader.loadVectorFromFile(residStdDevFilename).get();
						currentSubModule.setResidualStdDev(oVec.m_afData[0][0]); 
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Error while reading parameters in StemTaperPredictor class");
		}
	}
	
	
	
	/**
	 * This method computes the stem taper in mm2.
	 * @param heightMeasures a List of Double that represent the height (m)
	 * @return a StemTaperEstimate instance with the cross section squared diameter
	 */
	public AbstractStemTaperEstimate getPredictedTaperForTheseHeights(BasicStemTaperTree t, List<Double> heightMeasures, Object... additionalParameters) {
		if (!(t instanceof StemTaperTree)) {
			throw new InvalidParameterException("The StemTaperPredictor class is designed to work with StemTaperTree instances only!"); 
		}
		StemTaperTree tree = (StemTaperTree) t;
//		ModelType mType = StemTaperEquationSettings.getModelTypeEquation(tree, additionalParameters);
		ModelType mType = StemTaperEquationSettings.getModelTypeEquation(tree);
		
		Map<StemTaperTreeSpecies, StemTaperSubModule> innerMap = subModules.get(mType);
		StemTaperTreeSpecies species = tree.getStemTaperTreeSpecies();
		if (!innerMap.containsKey(species)) {
			throw new InvalidParameterException("This species is not recognized!");
		}

		EstimationMethodInDeterministicMode estimationMethod = null;

		if (additionalParameters != null && additionalParameters.length >= 1) {
			if (additionalParameters[0] instanceof EstimationMethodInDeterministicMode) {
				estimationMethod = (EstimationMethodInDeterministicMode) additionalParameters[0];
			}
		}
		
		StemTaperSubModule subModule = innerMap.get(species);
		return subModule.getPredictedTaperForTheseHeights(tree, heightMeasures, estimationMethod);
	}
	
	
	/**
	 * This method computes the underbark stem taper in mm2. 
	 * @param stemTaperSegments a List of StemTaperSegment instances
	 * @return a StemTaperEstimate instance with the cross section diameter in mm2
	 */
	public AbstractStemTaperEstimate getPredictedTaperForTheseSegments(StemTaperTree tree, StemTaperSegmentList stemTaperSegments, EstimationMethodInDeterministicMode method) {		
		List<Double> currentHeightsToEvaluate = stemTaperSegments.getHeightsWithoutReplicates();	
		return getPredictedTaperForTheseHeights(tree, currentHeightsToEvaluate, method);		
	}
	
	

	/**
	 * This method returns the volume of a series of height sections. The height sections are first sorted and the 
	 * volume is calculated between the first and the last one by summing the volume of the log between the successive 
	 * height sections. The volume is calculated using Smalian's formula. NOTE: the resulting volume is the underbark
	 * volume.
	 * @param tree a StemTaperTree instance
	 * @return the underbark volume in dm3 or -1 if the volume cannot be calculated
	 */
	@SuppressWarnings("unchecked")
	public static double getUnderbarkVolumeThroughSmalianFormula(StemTaperTree tree) {
		List<StemTaperCrossSection> heightSections = tree.getCrossSections();
		
		if (heightSections != null && !heightSections.isEmpty() && heightSections.size() > 1) {
			Collections.sort(heightSections);
			
			double largeDiameter;
			double smallDiameter;
			double logLength;
			double logVolume;
			double volume = 0d;
			double factor = 1d / (8 * 1000);	// to express the volume in dm3
			for (int i = 1; i < heightSections.size(); i++) {
				largeDiameter = heightSections.get(i - 1).getSectionDiameter();
				smallDiameter = heightSections.get(i).getSectionDiameter();
				logLength = heightSections.get(i).getSectionHeight() - heightSections.get(i - 1).getSectionHeight();
				logVolume = Math.PI * logLength * factor * (largeDiameter * largeDiameter + smallDiameter * smallDiameter);
				volume += logVolume;
			}
			return volume;
			
		} else {
			return -1d;
		}
		
	}

	
//	@Override
//	public void clearDeviates() {
//		for (Map<StemTaperTreeSpecies, StemTaperSubModule> innerMap : subModules.values()) {
//			for (StemTaperSubModule p : innerMap.values()) {
//				p.clearDeviates();
//			}
//		}
//	}

	
//	public static void main(String[] args) {
//		new StemTaperPredictor();
//	}
	
}
