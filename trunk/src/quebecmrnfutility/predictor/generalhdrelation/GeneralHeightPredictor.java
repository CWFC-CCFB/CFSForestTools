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
package quebecmrnfutility.predictor.generalhdrelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quebecmrnfutility.predictor.generalhdrelation.HeightableTree.HdSpecies;
import repicea.math.Matrix;
import repicea.predictor.QuebecGeneralSettings;
import repicea.predictor.QuebecGeneralSettings.DrainageGroup;
import repicea.simulation.ModelBasedSimulator;
import repicea.simulation.ParameterLoader;
import repicea.simulation.covariateproviders.treelevel.SpeciesNameProvider.SpeciesType;
import repicea.stats.StatisticalUtility.TypeMatrixR;
import repicea.stats.distributions.GaussianErrorTerm;
import repicea.stats.distributions.GaussianErrorTermList;
import repicea.stats.distributions.GaussianErrorTermList.IndexableErrorTerm;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;


/**
 * This class implements the general height-diameter relationship published in Fortin et al. (2009)
 * @author Mathieu Fortin - October 2009
 * @see Fortin, M., Bernier, S., Saucier, J.-P., and Labb�, F. 2009. Une relation hauteur-diam�tre tenant compte de l'influence de la station et du climat pour 20 esp�ces commerciales du Qu�bec. Gouvernement du Qu�bec, Minist�re des Ressources naturelles et de la Faune, Direction de la recherche foresti�re. M�moire de recherche foresti�re no 153. 22 p.
 */
public final class GeneralHeightPredictor extends ModelBasedSimulator {

	private static class RegressionElements {
		protected Matrix Z_tree;
		protected double fixedPred;
		protected HdSpecies species;
		protected RegressionElements() {}
	}
	
	@SuppressWarnings("serial")
	private static class GaussianErrorTermForHeight extends GaussianErrorTerm {

		public GaussianErrorTermForHeight(IndexableErrorTerm caller, double normalizedValue, double observedValue) {
			super(caller, normalizedValue);
			this.value = observedValue;
		}
		
	}
	
	private static final long serialVersionUID = 20100804L;

	private static enum DisturbanceType {HUMAN, 
		NATURAL, 
		NONE;
	
		private Matrix dummy;
		
		DisturbanceType() {
			dummy = new Matrix(1,3);
			dummy.m_afData[0][this.ordinal()] = 1d;
		}
		
		public Matrix getDummy() {return this.dummy;}
		
	}
		
	private final static Map<String,Matrix> DUMMY_ECO_REGION = new HashMap<String,Matrix>();
	static {
		Matrix dummy;
		dummy = new Matrix(1,4);
		dummy.m_afData[0][0] = 1.0;
		DUMMY_ECO_REGION.put("2b", dummy);
		DUMMY_ECO_REGION.put("2c", dummy);
		DUMMY_ECO_REGION.put("3c", dummy);
		DUMMY_ECO_REGION.put("3d", dummy);
		DUMMY_ECO_REGION.put("4d", dummy);
		DUMMY_ECO_REGION.put("4e", dummy);
		DUMMY_ECO_REGION.put("4f", dummy);
		DUMMY_ECO_REGION.put("5e", dummy);
		DUMMY_ECO_REGION.put("5f", dummy);	// region CENTRE
		
		dummy = new Matrix(1,4);
		dummy.m_afData[0][1] = 1.0;
		DUMMY_ECO_REGION.put("6h", dummy);
		DUMMY_ECO_REGION.put("6i", dummy);
		DUMMY_ECO_REGION.put("6j", dummy);
		DUMMY_ECO_REGION.put("6k", dummy);
		DUMMY_ECO_REGION.put("6l", dummy);
		DUMMY_ECO_REGION.put("6n", dummy);
		DUMMY_ECO_REGION.put("6o", dummy);
		DUMMY_ECO_REGION.put("6p", dummy);
		DUMMY_ECO_REGION.put("6q", dummy);
		DUMMY_ECO_REGION.put("6r", dummy);
		DUMMY_ECO_REGION.put("7a", dummy);
		DUMMY_ECO_REGION.put("7b", dummy);
		DUMMY_ECO_REGION.put("7c", dummy);	// region N_EST
		
		dummy = new Matrix(1,4);
		dummy.m_afData[0][2] = 1.0;
		DUMMY_ECO_REGION.put("1a", dummy);
		DUMMY_ECO_REGION.put("2a", dummy);
		DUMMY_ECO_REGION.put("3a", dummy);
		DUMMY_ECO_REGION.put("3b", dummy);
		DUMMY_ECO_REGION.put("4a", dummy);
		DUMMY_ECO_REGION.put("4b", dummy);
		DUMMY_ECO_REGION.put("4c", dummy);
		DUMMY_ECO_REGION.put("5a", dummy);
		DUMMY_ECO_REGION.put("5b", dummy);
		DUMMY_ECO_REGION.put("5c", dummy);
		DUMMY_ECO_REGION.put("5d", dummy);
		DUMMY_ECO_REGION.put("6a", dummy);
		DUMMY_ECO_REGION.put("6b", dummy);
		DUMMY_ECO_REGION.put("6c", dummy);
		DUMMY_ECO_REGION.put("6d", dummy);
		DUMMY_ECO_REGION.put("6e", dummy);
		DUMMY_ECO_REGION.put("6f", dummy);
		DUMMY_ECO_REGION.put("6g", dummy);	// region OUEST
		
		dummy = new Matrix(1,4);
		dummy.m_afData[0][3] = 1.0;
		DUMMY_ECO_REGION.put("4g", dummy);
		DUMMY_ECO_REGION.put("4h", dummy);
		DUMMY_ECO_REGION.put("5g", dummy);
		DUMMY_ECO_REGION.put("5h", dummy);
		DUMMY_ECO_REGION.put("5i", dummy);
		DUMMY_ECO_REGION.put("5j", dummy); 
		DUMMY_ECO_REGION.put("6m", dummy);	// region S_EST
	}
	
	private List<Integer> blupEstimationDone;

	/**
	 * General constructor for all combinations of uncertainty sources.
	 * @param isParametersVariabilityEnabled a boolean that enables the variability at the parameter level
	 * @param isRandomEffectsVariabilityEnabled a boolean that enables the variability at the random effect level
	 * @param isResidualVariabilityEnabled a boolean that enables the variability at the tree level
	 */
	public GeneralHeightPredictor(boolean isParametersVariabilityEnabled,
			boolean isRandomEffectsVariabilityEnabled,
			boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		init();
		oXVector = new Matrix(1,defaultBeta.getMean().m_iRows);
		blupEstimationDone = new ArrayList<Integer>();
	}

	/**
	 * Default constructor with all sources of uncertainty disabled.
	 * @param measurementDates a list of integers that define the measurement dates
	 */
	public GeneralHeightPredictor() {
		this(false, false, false);
	}

	private void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_HDRelationBeta.csv";
			String omegaFilename = path + "0_HDRelationOmega.csv";
			String covparmsFilename = path + "0_HDRelationCovParms.csv";

			Matrix defaultBetaMean = ParameterLoader.loadVectorFromFile(betaFilename).get();
			Matrix defaultBetaVariance = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
			defaultBeta = new SASParameterEstimate(defaultBetaMean, defaultBetaVariance);
			Matrix covParms = ParameterLoader.loadVectorFromFile(covparmsFilename).get();
			
			Matrix matrixG = covParms.getSubMatrix(0, 19, 0, 0).matrixDiagonal();
			Matrix defaultRandomEffectsMean = new Matrix(matrixG.m_iRows, 1);
			defaultRandomEffects.put(HierarchicalLevel.Plot, new GaussianEstimate(defaultRandomEffectsMean, matrixG));
			
			Matrix sigma2 = covParms.getSubMatrix(20, 20, 0, 0);
			double phi = covParms.m_afData[21][0];
			defaultResidualError.put(SpeciesType.BroadleavedSpecies, new GaussianErrorTermEstimate(sigma2, phi, TypeMatrixR.LINEAR));
			
			sigma2 = covParms.getSubMatrix(22, 22, 0, 0);
			phi = covParms.m_afData[23][0];
			defaultResidualError.put(SpeciesType.ConiferousSpecies, new GaussianErrorTermEstimate(sigma2, phi, TypeMatrixR.LINEAR));			
			
		} catch (Exception e) {
			System.out.println("GeneralHDRelation Class : Unable to initialize the general height-diameter relationship");
		}
	}
	
	/**
	 * This method computes the fixed effect prediction and put the prediction, the Z vector,
	 * and the species name into m_oRegressionOutput member. The method applies in any cases no matter
	 * it is deterministic or stochastic.
	 * @param stand a HeightableStand instance
	 * @param t a HeightableTree instance
	 */
	private synchronized RegressionElements fixedEffectsPrediction(HeightableStand stand, HeightableTree t) {
		Matrix modelParameters = getParametersForThisRealization(stand);
		
		double basalArea = stand.getBasalAreaM2Ha();
		double averageTemp = stand.getMeanAnnualTemperatureC();
		DrainageGroup drainageGroup = getDrainageGroup(stand);
		String ecoRegion = stand.getEcoRegion();
		boolean isInterventionResult = stand.isInterventionResult();
		boolean isDefoliated = stand.isSBWDefoliated();
		
		Matrix dummyDrainageClass = drainageGroup.getDrainageDummy();
		Matrix dummyEcoRegion = GeneralHeightPredictor.DUMMY_ECO_REGION.get(ecoRegion);
		Matrix dummyDisturbance;
		if (isInterventionResult) {
			dummyDisturbance = DisturbanceType.HUMAN.getDummy();
		} else if (isDefoliated) {
			dummyDisturbance = DisturbanceType.NATURAL.getDummy();
		} else {
			dummyDisturbance = DisturbanceType.NONE.getDummy();
		}
		
		oXVector.resetMatrix();
		int pointeur = 0;
		HdSpecies species = t.getHeightableTreeSpecies();
		double lnDbh = t.getLnDbhCmPlus1();
		double SSI = t.getSocialStatusIndex();
		double lnDbh2 = t.getSquaredLnDbhCmPlus1();
		Matrix dummySpecies = species.getDummy();

		oXVector.setSubMatrix(dummySpecies.scalarMultiply(lnDbh), 0, pointeur);
		pointeur += dummySpecies.m_iCols;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(lnDbh * basalArea), 0, pointeur);
		pointeur += dummySpecies.m_iCols;
		oXVector.m_afData[0][pointeur] = lnDbh * averageTemp;
		pointeur ++;
		oXVector.setSubMatrix(dummyDrainageClass.scalarMultiply(lnDbh), 0, pointeur);
		pointeur += dummyDrainageClass.m_iCols;
		oXVector.setSubMatrix(dummyEcoRegion.scalarMultiply(lnDbh), 0, pointeur);
		pointeur += dummyEcoRegion.m_iCols;
		oXVector.setSubMatrix(dummyDisturbance.scalarMultiply(lnDbh), 0, pointeur);
		pointeur += dummyDisturbance.m_iCols;
		oXVector.m_afData[0][pointeur] = lnDbh * SSI;
		pointeur ++;
		oXVector.m_afData[0][pointeur] = lnDbh2 * SSI;
		pointeur ++;

		Matrix matZ_i = dummySpecies.scalarMultiply(lnDbh2);	// design vector for the plot random effect
		oXVector.setSubMatrix(matZ_i, 0, pointeur);
		pointeur += dummySpecies.m_iCols;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(lnDbh2 * basalArea), 0, pointeur);
		pointeur += dummySpecies.m_iCols;

		double fResult = 1.3 + oXVector.multiply(modelParameters).m_afData[0][0];
		
		RegressionElements regElements = new RegressionElements();
		
		regElements.fixedPred = fResult;
		regElements.Z_tree = matZ_i;
		regElements.species = species;
		
		return regElements;
	}
	
	/**
	 * This method accounts for the random effects in the predictions if the random effect variability is enabled. Otherwise, it returns 0d.
	 * @param stand = a HeightableStand object
	 * @param regElement = a RegressionElements object
	 * @return a simulated random effect (double)
	 */
	private double blupImplementation(HeightableStand stand, RegressionElements regElement) {
		Matrix randomEffects = getRandomEffectsForThisSubject(stand);
		return regElement.Z_tree.multiply(randomEffects).m_afData[0][0];
	}
	
	/**
	 * This method calculates the height for individual trees and also implements the 
	 * Monte Carlo simulation automatically. In case of exception, it also returns -1.
	 * If the predicted height is lower than 1.3, this method returns 1.3.
	 * @param stand a HeightableStand object
	 * @param tree a HeightableTree object
	 * @return the predicted height (m)
	 */
	public double predictHeight(HeightableStand stand, HeightableTree tree) {
		try {
			if (!blupEstimationDone.contains(stand.getSubjectId())) {
				predictHeightRandomEffects(stand);
				blupEstimationDone.add(stand.getSubjectId());
			}
			double observedHeight = tree.getHeightM();
			double predictedHeight; 
			RegressionElements regElement = fixedEffectsPrediction(stand, tree);
			predictedHeight = regElement.fixedPred;
			predictedHeight += blupImplementation(stand, regElement);

			if (observedHeight > 1.3) {			// means that height was already observed
				double variance = defaultResidualError.get(regElement.species.getSpeciesType()).getVariance().m_afData[0][0];
				double dNormResidual = (observedHeight - predictedHeight) / Math.pow(variance, 0.5);
				GaussianErrorTerm errorTerm = new GaussianErrorTermForHeight(tree, dNormResidual, observedHeight - predictedHeight);
				setSpecificResiduals(tree, errorTerm);	// the residual is set in the simulatedResidualError member
				return -1d;
			} else {
				predictedHeight += residualImplementation(tree);
				if (predictedHeight < 1.3) {
					predictedHeight = 1.3;
				}
				return predictedHeight;
			}
		} catch (Exception e) {
			System.out.println("Error while estimating tree height for tree " + tree.toString());
			e.printStackTrace();
			return -1d;
		}
	}

	/**
	 * This method records a normalized residuals into the simulatedResidualError member which is
	 * located in the ModelBasedSimulator class. The method asks the date from the HeightableTree
	 * instance in order to put the normalized residual at the proper location in the vector of residuals.
	 * @param tree a HeightableTree instance
	 * @param errorTerm a GaussianErrorTerm instance
	 */
	private void setSpecificResiduals(HeightableTree tree, GaussianErrorTerm errorTerm) {
		getGaussianErrorTerms(tree).add(errorTerm);
	}

	/**
	 * This method accounts for a random deviate if the residual variability is enabled. Otherwise, it returns 0d. 
	 * @param tree a HeightableTree instance
	 * @param regElement a RegressionElements instance
	 * @return a simulated residual (double)
	 */
	private double residualImplementation(HeightableTree tree) {
		double residualForThisPrediction = 0d; 
		if (isResidualVariabilityEnabled) {
			Matrix residuals = getResidualErrorForThisSubject(tree, tree.getHeightableTreeSpecies().getSpeciesType());
			int index = getGaussianErrorTerms(tree).getDistanceIndex().indexOf(tree.getErrorTermIndex());
			residualForThisPrediction = residuals.m_afData[index][0]; 
		} else {
			if (doesThisSubjectHaveResidualErrorTerm(tree)) {		// means that height was initially measured
				setSpecificResiduals(tree, new GaussianErrorTerm(tree, 0d));
				GaussianErrorTermList list = getGaussianErrorTerms(tree);
				Matrix meanResiduals = defaultResidualError.get(tree.getHeightableTreeSpecies().getSpeciesType()).getMean(list);
				residualForThisPrediction = meanResiduals.m_afData[meanResiduals.m_iRows - 1][0];
			} 
		}
		return residualForThisPrediction;
	}
	
	/**
	 * This method computes the best linear unbiased predictors of the random effects
	 * @param stand a HeightableStand instance
	 */
	private synchronized void predictHeightRandomEffects(HeightableStand stand) {
		boolean originalIsParameterVariabilityEnabled = isParametersVariabilityEnabled;
		isParametersVariabilityEnabled = false; // temporarily disabled for the prediction of the random effects
		
		Matrix matrixG = defaultRandomEffects.get(HierarchicalLevel.Plot).getVariance();
		
		Matrix blups;
		Matrix blupsVariance;

		RegressionElements regElement;
		
		// put all the trees for which the height is available in a Vector
		List<HeightableTree> heightableTrees = new ArrayList<HeightableTree>();
		if (!stand.getTrees().isEmpty()) {
			for (Object tree : stand.getTrees()) {
				if (tree instanceof HeightableTree) {
					double height = ((HeightableTree) tree).getHeightM();
					if (height > 1.3) {
						heightableTrees.add((HeightableTree) tree);
					}
					
				}
			}
		}			

		if (!heightableTrees.isEmpty()) {
			// matrices for the blup calculation
			int nbObs = heightableTrees.size();
			Matrix matZ = new Matrix(nbObs, matrixG.m_iRows);		// design matrix for random effects 
			Matrix matR = new Matrix(nbObs, nbObs);					// within-tree variance-covariance matrix  
			Matrix vectRes = new Matrix(nbObs, 1);						// vector of residuals

			for (int i = 0; i < nbObs; i++) {
				HeightableTree t = heightableTrees.get(i);
				double height = t.getHeightM();
				
				regElement = fixedEffectsPrediction(stand, t);
				matZ.setSubMatrix(regElement.Z_tree, i, 0);
				double variance = defaultResidualError.get(regElement.species.getSpeciesType()).getVariance().m_afData[0][0];
				matR.m_afData[i][i] = variance;
				double residual = height - regElement.fixedPred;
				vectRes.m_afData[i][0] = residual;
			}
			Matrix matV = matZ.multiply(matrixG).multiply(matZ.transpose()).add(matR);	// variance - covariance matrix
			blups = matrixG.multiply(matZ.transpose()).multiply(matV.getInverseMatrix()).multiply(vectRes);							// blup_essHD is redefined according to observed values
			blupsVariance = matZ.transpose().multiply(matR.getInverseMatrix()).multiply(matZ).add(matrixG.getInverseMatrix()).getInverseMatrix();			// blup_essHDvar is redefined according to observed values
			Map<Integer, GaussianEstimate> randomEffectsMap = blupsLibrary.get(HierarchicalLevel.Plot);
			if (randomEffectsMap == null) {
				randomEffectsMap = new HashMap<Integer, GaussianEstimate>();
				blupsLibrary.put(HierarchicalLevel.Plot, randomEffectsMap);
			}
			randomEffectsMap.put(stand.getSubjectId(), new GaussianEstimate(blups, blupsVariance));
		}
		
		isParametersVariabilityEnabled = originalIsParameterVariabilityEnabled; // set the parameter variability to its original value;
	}

	private DrainageGroup getDrainageGroup(HeightableStand stand) {
		DrainageGroup drainageGroup = QuebecGeneralSettings.DRAINAGE_CLASS_LIST.get(stand.getDrainageClass());
		if (drainageGroup == null) {
			if (stand.getEcologicalType() != null && stand.getEcologicalType().length() >= 4) {	// else if the ecological type is available then provide a typical class that corresponds to the grouping XERIC MESIC SUBHYDRIC HYDRIC
				String environmentType = stand.getEcologicalType().substring(3, 4);
				if (!environmentType.isEmpty()) {
					drainageGroup = QuebecGeneralSettings.ENVIRONMENT_TYPE.get(environmentType);	
				} 
			}
		} 
		return drainageGroup; 
	}
	
	/**
	 * For testing only
	 * @param stand
	 */
	public Matrix getBlups(HeightableStand stand) {
		if (blupsLibrary.get(HierarchicalLevel.Plot) != null) {
			return blupsLibrary.get(HierarchicalLevel.Plot).get(stand.getSubjectId()).getMean();
		} else {
			return null;
		}
	}
	
}