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
package quebecmrnfutility.predictor.hdrelationships.generalhdrelation2009;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import quebecmrnfutility.predictor.QuebecGeneralSettings;
import quebecmrnfutility.predictor.hdrelationships.generalhdrelation2009.Heightable2009Tree.Hd2009Species;
import repicea.math.DiagonalMatrix;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ParameterLoader;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider.DrainageGroup;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.simulation.hdrelationships.HDRelationshipPredictor;
import repicea.stats.StatisticalUtility.TypeMatrixR;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;


/**
 * This class implements the general height-diameter relationship published in Fortin et al. (2009)
 * @author Mathieu Fortin - October 2009
 * @see Fortin, M., Bernier, S., Saucier, J.-P., and Labbe, F. 2009. Une relation hauteur-diametre tenant 
 * compte de l'influence de la station et du climat pour 20 especes commerciales du Quebec. 
 * Gouvernement du Quebec, Ministere des Ressources naturelles et de la Faune, Direction de 
 * la recherche forestiere. Memoire de recherche forestiere no 153. 22 p.
 */
public final class GeneralHeight2009Predictor extends HDRelationshipPredictor<Heightable2009Stand, Heightable2009Tree> {
	
	private static final long serialVersionUID = 20100804L;

	private static enum DisturbanceType {HUMAN, 
		NATURAL, 
		NONE;
	
		private Matrix dummy;
		
		DisturbanceType() {
			dummy = new Matrix(1,3);
			dummy.setValueAt(0, ordinal(), 1d);
		}
		
		public Matrix getDummy() {return this.dummy;}
		
	}
		
	private final static Map<String,Matrix> DUMMY_ECO_REGION = new HashMap<String,Matrix>();
	static {
		Matrix dummy;
		dummy = new Matrix(1,4);
		dummy.setValueAt(0, 0, 1d);
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
		dummy.setValueAt(0, 1, 1d);
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
		dummy.setValueAt(0, 2, 1d);
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
		dummy.setValueAt(0, 3, 1d);
		DUMMY_ECO_REGION.put("4g", dummy);
		DUMMY_ECO_REGION.put("4h", dummy);
		DUMMY_ECO_REGION.put("5g", dummy);
		DUMMY_ECO_REGION.put("5h", dummy);
		DUMMY_ECO_REGION.put("5i", dummy);
		DUMMY_ECO_REGION.put("5j", dummy); 
		DUMMY_ECO_REGION.put("6m", dummy);	// region S_EST
	}
	
//	private final static Map<DrainageGroup, Matrix> DUMMY_DRAINAGE_GROUP = new HashMap<DrainageGroup, Matrix>();
//	static {
//		for (DrainageGroup dg : DrainageGroup.values()) {
//			Matrix mat = new Matrix(1,4);
//			mat.m_afData[0][dg.ordinal()] = 1d;
//			DUMMY_DRAINAGE_GROUP.put(dg, mat);
//		}
//	}
	
	
	/**
	 * General constructor for all combinations of uncertainty sources.
	 * @param isVariabilityEnabled a boolean that enables the stochastic mode
	 */
	public GeneralHeight2009Predictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled);
		init();
		oXVector = new Matrix(1,getParameterEstimates().getMean().m_iRows);
	}

	/**
	 * Default constructor with all sources of uncertainty disabled.
	 */
	public GeneralHeight2009Predictor() {
		this(false);
	}

	@Override
	protected final void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_HDRelationBeta.csv";
			String omegaFilename = path + "0_HDRelationOmega.csv";
			String covparmsFilename = path + "0_HDRelationCovParms.csv";

			Matrix defaultBetaMean = ParameterLoader.loadVectorFromFile(betaFilename).get();
			SymmetricMatrix defaultBetaVariance = SymmetricMatrix.convertToSymmetricIfPossible(
					ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym());
			setParameterEstimates(new SASParameterEstimates(defaultBetaMean, defaultBetaVariance));
			Matrix covParms = ParameterLoader.loadVectorFromFile(covparmsFilename).get();
			
			DiagonalMatrix matrixG = covParms.getSubMatrix(0, 19, 0, 0).matrixDiagonal();
			Matrix defaultRandomEffectsMean = new Matrix(matrixG.m_iRows, 1);
			setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(defaultRandomEffectsMean, matrixG));
			SymmetricMatrix sigma2 = SymmetricMatrix.convertToSymmetricIfPossible(covParms.getSubMatrix(20, 20, 0, 0));
			double phi = covParms.getValueAt(21, 0);
			setDefaultResidualError(SpeciesType.BroadleavedSpecies, new GaussianErrorTermEstimate(sigma2, phi, TypeMatrixR.LINEAR));
			
			sigma2 = SymmetricMatrix.convertToSymmetricIfPossible(covParms.getSubMatrix(22, 22, 0, 0));
			phi = covParms.getValueAt(23, 0);
			setDefaultResidualError(SpeciesType.ConiferousSpecies, new GaussianErrorTermEstimate(sigma2, phi, TypeMatrixR.LINEAR));			
			
		} catch (Exception e) {
			System.out.println("GeneralHDRelation Class : Unable to initialize the general height-diameter relationship");
		}
	}
	
	@Override
	protected synchronized RegressionElements fixedEffectsPrediction(Heightable2009Stand stand, Heightable2009Tree t, Matrix beta) {
//		Matrix modelParameters = getParametersForThisRealization(stand);
		Matrix modelParameters = beta;
		double basalArea = stand.getBasalAreaM2Ha();
		if (basalArea <= 0d) {
			System.out.println("Error in HD relationship: The basal area of the plot has not been calculated yet!");
			throw new InvalidParameterException("The basal area of the plot has not been calculated yet!");
		}
		double averageTemp = stand.getMeanAnnualTemperatureC();
		DrainageGroup drainageGroup = getDrainageGroup(stand);
		String ecoRegion = stand.getEcoRegion();
		boolean isInterventionResult = stand.isInterventionResult();
		boolean isDefoliated = stand.isSBWDefoliated();
		
		Matrix dummyDrainageClass = drainageGroup.getDrainageDummy();
		Matrix dummyEcoRegion = GeneralHeight2009Predictor.DUMMY_ECO_REGION.get(ecoRegion);
		Matrix dummyDisturbance;
		if (isInterventionResult) {
			dummyDisturbance = DisturbanceType.HUMAN.getDummy();
		} else if (isDefoliated) {
			dummyDisturbance = DisturbanceType.NATURAL.getDummy();
		} else {
			dummyDisturbance = DisturbanceType.NONE.getDummy();
		}
		
		oXVector.resetMatrix();
		int pointer = 0;
		Hd2009Species species = t.getHeightableTreeSpecies();
		double lnDbh = t.getLnDbhCmPlus1();
		double SSI = t.getSocialStatusIndex();
		double lnDbh2 = t.getSquaredLnDbhCmPlus1();
		Matrix dummySpecies = species.getDummy();

		oXVector.setSubMatrix(dummySpecies.scalarMultiply(lnDbh), 0, pointer);
		pointer += dummySpecies.m_iCols;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(lnDbh * basalArea), 0, pointer);
		pointer += dummySpecies.m_iCols;
		oXVector.setValueAt(0, pointer, lnDbh * averageTemp);
		pointer ++;
		oXVector.setSubMatrix(dummyDrainageClass.scalarMultiply(lnDbh), 0, pointer);
		pointer += dummyDrainageClass.m_iCols;
		oXVector.setSubMatrix(dummyEcoRegion.scalarMultiply(lnDbh), 0, pointer);
		pointer += dummyEcoRegion.m_iCols;
		oXVector.setSubMatrix(dummyDisturbance.scalarMultiply(lnDbh), 0, pointer);
		pointer += dummyDisturbance.m_iCols;
		oXVector.setValueAt(0, pointer, lnDbh * SSI);
		pointer ++;
		oXVector.setValueAt(0, pointer, lnDbh2 * SSI);
		pointer ++;

		Matrix matZ_i = dummySpecies.scalarMultiply(lnDbh2);	// design vector for the plot random effect
		oXVector.setSubMatrix(matZ_i, 0, pointer);
		pointer += dummySpecies.m_iCols;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(lnDbh2 * basalArea), 0, pointer);
		pointer += dummySpecies.m_iCols;

		double fResult = 1.3 + oXVector.multiply(modelParameters).getValueAt(0, 0);
		
		RegressionElements regElements = new RegressionElements();
		
		regElements.fixedPred = fResult;
		regElements.vectorZ = matZ_i;
		regElements.species = species;
		
		return regElements;
	}
	
	
	private DrainageGroup getDrainageGroup(Heightable2009Stand stand) {
		DrainageGroup drainageGroup = stand.getDrainageGroup();
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
	public Matrix getBlups(Heightable2009Stand stand) {
		if (doBlupsExistForThisSubject(stand)) {
			return getBlupsForThisSubject(stand).getMean();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<Heightable2009Tree> getTreesFromStand(Heightable2009Stand stand) {
		return stand.getTrees(StatusClass.alive);
	}
	

}