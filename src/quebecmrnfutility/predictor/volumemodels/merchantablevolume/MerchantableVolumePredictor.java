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
package quebecmrnfutility.predictor.volumemodels.merchantablevolume;

import quebecmrnfutility.predictor.volumemodels.merchantablevolume.VolumableTree.VolSpecies;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ParameterLoader;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.stats.StatisticalUtility;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;


/**
 * This class implements the merchantable volume equation published in Fortin et al. (2007).
 * NOTE: merchantability is defined as trees with diameter equal to or greater than 9.1 cm 
 * at breast height. The volume was calculated UNDER bark.
 * @author Mathieu Fortin - October 2009
 * @see Fortin, M., DeBlois, J., Bernier, S., and Blais, G. 2007. Mise au point d'un tarif de cubage general 
 * pour les forets quebecoises: une approche pour mieux evaluer l'incertitude associee aux previsions. 
 * The Forestry Chronicle 83(5) 754-765.
 */
public final class MerchantableVolumePredictor extends REpiceaPredictor {

	private static final long serialVersionUID = 20100804L;

	private Matrix sigma2;

	/**
	 * General constructor for all combinations of uncertainty sources.
	 * @param isVariabilityEnabled = a boolean that enables the variability at the parameter level
	 */
	public MerchantableVolumePredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
		init();
		oXVector = new Matrix(1, getParameterEstimates().getMean().m_iRows);
	}
	
	/**
	 * Default constructor with all sources of uncertainty disabled.
	 */
	public MerchantableVolumePredictor() {
		this(false);
	}

	@Override
	protected final void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MerchVolumeBeta.csv";
			String omegaFilename = path + "0_MerchVolumeOmega.csv";
			String covparmsFilename = path + "0_MerchVolumeCovParms.csv";

			Matrix defaultBetaMean = ParameterLoader.loadVectorFromFile(betaFilename).get();
			Matrix defaultBetaVariance = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
			setParameterEstimates(new SASParameterEstimates(defaultBetaMean, defaultBetaVariance));
			Matrix covParms = ParameterLoader.loadVectorFromFile(covparmsFilename).get();

			Matrix matrixGPlotLevel =  covParms.getSubMatrix(0, 2, 0, 0).squareSym();
			Matrix defaultRandomEffectsPlotLevel = new Matrix(matrixGPlotLevel.m_iRows, 1);

			Matrix matrixGCruiseLineLevel = covParms.getSubMatrix(3, 5, 0, 0).squareSym();
			Matrix defaultRandomEffectsCruiseLineLevel = new Matrix(matrixGCruiseLineLevel.m_iRows, 1);
			
			sigma2 = covParms.getSubMatrix(6, covParms.m_iRows - 1, 0, 0);

			setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(defaultRandomEffectsPlotLevel, matrixGPlotLevel));
			setDefaultRandomEffects(HierarchicalLevel.CRUISE_LINE, new GaussianEstimate(defaultRandomEffectsCruiseLineLevel, matrixGCruiseLineLevel));
		} catch (Exception e) {
			System.out.println("GeneralVolumeCalculator.init() : Unable to initialize the GeneralVolumeEquation");
		}
	}
	
	/**
	 * This method return the underbark volume estimate for an individual trees. 
	 * NOTE: Stochastic implementation is handled through the general constructor.
	 * The method returns 0 if the tree is smaller than 9.1 cm in dbh. It returns -1
	 * if the tree height has not been calculated.
	 * @param stand a VolumableStand object
	 * @param tree a TreeVolumable object
	 * @return the commercial underbark volume (dm3)
	 */
	public double predictTreeCommercialUnderbarkVolumeDm3(VolumableStand stand, VolumableTree tree) {
		try {

			if (tree.getDbhCm() < 9.1) {	// means this is a sapling
				return 0d;
			}
			
			if (tree.getHeightM() == -1) {	// means the height has not been calculated
				return -1d;
			}
			
			double volume = fixedEffectPrediction(stand, tree);
			volume += blupImplementation(stand, tree);
			volume += residualImplementation(tree);
			if (volume < 0) {
				volume = 1d;		// at least 1 dm3 if dbh >= 9.1 Correction for negative volumes MF2021-03-25
			}
			return volume;
			
		} catch (Exception e) {
			System.out.println("Error while estimating tree volume for tree " + tree.toString());
			e.printStackTrace();
			return -1d;
		}
	}

	/**
	 * This method computes the fixed effect prediction.
	 * @param stand = a VolumableStand object
	 * @param t = a TreeVolumable object
	 * @return the fixed effect prediction (double)
	 * @throws Exception
	 */
	private synchronized double fixedEffectPrediction(VolumableStand stand, VolumableTree t) throws Exception {
		Matrix modelParameters = getParametersForThisRealization(stand);
		this.oXVector.resetMatrix();
		int pointeur = 0;
		VolSpecies species = t.getVolumableTreeSpecies();
		double dbh = t.getDbhCm();
		double dbh2 = t.getSquaredDbhCm();
		double height = t.getHeightM();
		double cylindre = Math.PI*dbh2*height*0.025;
		
		oXVector.setValueAt(0, pointeur, height/dbh);
		pointeur++;

		Matrix dummy = species.getDummy();
		oXVector.setSubMatrix(dummy.scalarMultiply(cylindre), 0, pointeur);
		pointeur += dummy.m_iCols;
		if (species.getSpeciesType() == SpeciesType.ConiferousSpecies) {
			double cylindreRes = cylindre*dbh;
			oXVector.setSubMatrix(dummy.scalarMultiply(cylindreRes), 0, pointeur);
		}
		
		return oXVector.multiply(modelParameters).getValueAt(0, 0);
	}

	/**
	 * This method accounts for the random effects in the predictions if the random effect variability is enabled. Otherwise, it returns 0d.
	 * @param stand = a VolumableStand object
	 * @param t = a TreeVolumable object
	 * @return a simulated random effect (double)
	 */
	private double blupImplementation(VolumableStand stand, VolumableTree t) {
		if (isRandomEffectsVariabilityEnabled) {					
			String cruiseLineID = stand.getCruiseLineID();
			if (cruiseLineID == null) {
				cruiseLineID = stand.getSubjectId();
			}
			CruiseLine cruiseLine = getCruiseLineForThisSubject(cruiseLineID, stand);
			Matrix cruiseLineRandomEffects = getRandomEffectsForThisSubject(cruiseLine);
			Matrix plotRandomEffects = getRandomEffectsForThisSubject(stand);
			Matrix totalRandomEffects = cruiseLineRandomEffects.add(plotRandomEffects);
			VolSpecies species = t.getVolumableTreeSpecies();
			double dbh2 = t.getSquaredDbhCm();

			int type = 1;
			if (species.getSpeciesType() == SpeciesType.ConiferousSpecies) {
				type = 0;
			}
			return totalRandomEffects.getValueAt(type, 0) * dbh2;
		} else {
			return 0d;
		}
	}
	
	/**
	 * This method accounts for a random deviate if the residual variability is enabled. Otherwise, it returns 0d. 
	 * @param stand a VolumableStand object
	 * @param t a TreeVolumable object
	 * @return a simulated residual (double)
	 */
	private double residualImplementation(VolumableTree t) {
		if (isResidualVariabilityEnabled) {
			VolSpecies species = t.getVolumableTreeSpecies();
			Matrix dummy = species.getDummy();
			double dbh2 = t.getSquaredDbhCm();

			return Math.sqrt(dummy.multiply(sigma2).getValueAt(0, 0)) * dbh2 * StatisticalUtility.getRandom().nextGaussian();
		} else {
			return 0d;
		}
	}
	
//	/**
//	 * For testing purpose.
//	 * @param args
//	 */
//	public static void main (String[] args) {
//		@SuppressWarnings("unused")
//		MerchantableVolumePredictor test = new MerchantableVolumePredictor(false);
//		try {
//			System.out.println("Done without problems.");
//		} catch (Exception e) {
//			System.out.println("Problems!!!!!");
//			e.printStackTrace();
//		}
//	}

	
	
	
}

