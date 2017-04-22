package quebecmrnfutility.predictor.generalhdrelation2014;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quebecmrnfutility.predictor.generalhdrelation2014.Heightable2014Tree.Hd2014Species;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.stats.StatisticalUtility.TypeMatrixR;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

/**
 * This class implements the beta height-diameter relationship published in
 * Fortin et al. (2009)
 * 
 * @author Hache Denis - Fev 2014
 * @see Fortin, M., Bernier, S., Saucier, J.-P., and Labbé, F. 2009, Auger, I. Une
 *      relation hauteur-diamètre tenant compte de l'influence de la station et
 *      du climat pour 20 espèces commerciales du Québec. Gouvernement du
 *      Québec, Ministère des Ressources naturelles et de la Faune, Direction de
 *      la recherche forestière. Mémoire de recherche forestière no 153. 22 p.
 */
public class GeneralHeight2014Predictor extends REpiceaPredictor {

//	@SuppressWarnings("serial")
//	private static class GaussianErrorTermForHeight extends GaussianErrorTerm {
//
//		public GaussianErrorTermForHeight(IndexableErrorTerm caller, double normalizedValue, double observedValue) {
//			super(caller, normalizedValue);
//			this.value = observedValue;
//		}
//		
//	}

	protected static class BetaHeightableStandMonteCarlo implements MonteCarloSimulationCompliantObject {
		private final int monteCarloRealization;
		private String subjectID;
		private HierarchicalLevel	hieraLevel;

		protected BetaHeightableStandMonteCarlo(MonteCarloSimulationCompliantObject subject, Hd2014Species species) {
			String id = (subject.getMonteCarloRealizationId() + "_" + species.ordinal());
			this.monteCarloRealization = id.hashCode();
			subjectID = subject.getSubjectId();
			hieraLevel = subject.getHierarchicalLevel();
		}

		@Override
		public String getSubjectId() {
			return subjectID;
		}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {
			return hieraLevel;
		}


		@Override
		public int getMonteCarloRealizationId() {
			return monteCarloRealization;
		}

	}

	private static final long	serialVersionUID	= -8023375730600740497L;
	
	
	protected static enum DisturbanceType{
		INT,
		MOY,
		NON;		
	}
	
	/**
	 * This enum variable contains all the possible effects
	 * @author Denis hache - Fev 2014
	 */
	protected static enum Effect {
		LogDbh("ldhp"),
		LogDbh2("ldhp2"),
		LogDbh_basalArea("ldhp*st"), 
		LogDbh_basalAreaGreaterThan("ldhp*stgt"),
		LogDbh2_basalAreaGreaterThan("ldhp2*stgt"),
		LogDbh_ratioDbh("ldhp*rdhp"),
		LogDbh2_ratioDbh("ldhp2*rdhp"), 
		LogDbh_SubDom("ldhp*sdom"), 
		LogDbh_PotVeg("ldhp*vp"), 
		LogDbh_Elevation("ldhp*alt"),
		LogDbh_EcoType("ldhp*milieu"),
		LogDbh_Disturb("ldhp*pert"),
		LogDbh_Dens("ldhp*dens"),
		LogDbh_meanT("ldhp*Tmoy"),
		LogDbh_pTot("ldhp*Ptot"),
		LogDbh_Is("ldhp*is"),
		LogDbh_NotOuest("ldhp*ouest");
		
		private String nameEffect;
		
		Effect(String pNameEffect) {
			this.nameEffect = pNameEffect;
		}

		public static Effect fromString(String text) {
			if (text != null) {
				for (Effect b : Effect.values()) {
					if (text.equalsIgnoreCase(b.nameEffect)) {
						return b;
					}
				}
			}
			throw new IllegalArgumentException("No constant with text " + text + " found");
		}
	}

	private final Map<Hd2014Species, GeneralHeight2014InternalPredictor> internalPredictors;
	
	public GeneralHeight2014Predictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
		internalPredictors = new HashMap<Hd2014Species, GeneralHeight2014InternalPredictor>();
		init();
	}

	/**
	 * Default constructor with all sources of uncertainty disabled.
	 */
	public GeneralHeight2014Predictor() {
		this(false);
	}

	@Override
	protected final void init() {
		try {
			for (Hd2014Species species : Hd2014Species.values()) {		
				GeneralHeight2014InternalPredictor internalPredictor = new GeneralHeight2014InternalPredictor(species, 
						isParametersVariabilityEnabled, 
						isRandomEffectsVariabilityEnabled, 
						isResidualVariabilityEnabled);
				internalPredictors.put(species, internalPredictor);
				String path = ObjectUtility.getRelativePackagePath(getClass()) + species.name().toLowerCase() + "/";
				String suffix = species.name().toUpperCase().concat(".csv");
				
				String parameterFilename = path + "parameters".concat(suffix);
				String omegaFilename = path + "omega".concat(suffix);
				String plotRandomEffectsFilename = path + "randomEffects".concat(suffix);
				String listSdomFilename = path + "listeSdom".concat(suffix);
				String listVpFilename = path + "listeVp2".concat(suffix);
				String listEcoTypeFilename = path + "listeTypeEco4".concat(suffix);
				String listEffectFilename = path + "Effects".concat(suffix);
				String listDisturbFileName = path + "listePerturb".concat(suffix);

				Matrix beta = ParameterLoaderExt.loadVectorFromFile(parameterFilename).get();
				Matrix omega = ParameterLoaderExt.loadMatrixFromFile(omegaFilename, 6);				
				try {
					omega.getLowerCholTriangle();
				} catch (Exception e) {
					System.err.println("Error can't use getLowerCholTriangle on omega for " + species);
				}

				SASParameterEstimates defaultBeta = new SASParameterEstimates(beta, omega);
				internalPredictor.setParameterEstimates(defaultBeta);

				Matrix randomEffects = ParameterLoaderExt.loadVectorFromFile(plotRandomEffectsFilename).get();
				Matrix matrixG = randomEffects.getSubMatrix(0, 0, 0, 0);
				Matrix defaultRandomEffectsMean = new Matrix(matrixG.m_iRows, 1);
				internalPredictor.setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(defaultRandomEffectsMean, matrixG));
				
//				Matrix years = new Matrix(measurementDates);
				Matrix sigma2 = randomEffects.getSubMatrix(2, 2, 0, 0);
				double phi = randomEffects.m_afData[1][0];//tree
				GaussianErrorTermEstimate estimate = new GaussianErrorTermEstimate(sigma2, phi, TypeMatrixR.POWER);
				setDefaultResidualError(ErrorTermGroup.Default, estimate);
				internalPredictor.setDefaultResidualError(ErrorTermGroup.Default, estimate);

				Matrix oMat;
				List<String> sdomM = ParameterLoaderExt.loadColumnVectorFromFile(parameterFilename, 3, String.class);
				List<String> sdomBio = ParameterLoaderExt.loadColumnVectorFromFile(listSdomFilename, 0, String.class);
				List<String> sdom = ParameterLoaderExt.loadColumnVectorFromFile(listSdomFilename, 1, String.class);
				Map<String, Matrix> oMap = new HashMap<String, Matrix>();
				for (int i = 0; i < sdomBio.size(); i++) {
					String sdomBioKey = sdomBio.get(i);
					String sdomValue = sdom.get(i);
					oMat = new Matrix(1, sdomM.size());
					if(sdomM.indexOf(sdomValue) != -1){
						oMat.m_afData[0][sdomM.indexOf(sdomValue)] = 1d;
					}
					oMap.put(sdomBioKey, oMat);
				}
				internalPredictor.setSubDomainDummyMap(oMap);

				List<String> vegPotM = ParameterLoaderExt.loadColumnVectorFromFile(parameterFilename, 4, String.class);
				List<String> vegPotKeys = ParameterLoaderExt.loadColumnVectorFromFile(listVpFilename, 0, String.class);
				List<String> vp = ParameterLoaderExt.loadColumnVectorFromFile(listVpFilename, 1, String.class);
				oMap = new HashMap<String, Matrix>();
				for (int i = 0; i < vegPotKeys.size(); i++) {
					String vegPotKey = vegPotKeys.get(i);
					String vpValue = vp.get(i);
					oMat = new Matrix(1, vegPotM.size());
					if(vegPotM.indexOf(vpValue) != -1){
						oMat.m_afData[0][vegPotM.indexOf(vpValue)] = 1d;
					}
					oMap.put(vegPotKey, oMat);
				}
				internalPredictor.setVegPotDummyMap(oMap);

				List<String> ecoTypeM = ParameterLoaderExt.loadColumnVectorFromFile(parameterFilename, 5, String.class);
				List<String> ecoTypeKeys = ParameterLoaderExt.loadColumnVectorFromFile(listEcoTypeFilename, 0, String.class);
				List<String> milieu = ParameterLoaderExt.loadColumnVectorFromFile(listEcoTypeFilename, 1, String.class);
				oMap = new HashMap<String, Matrix>();
				for (int i = 0; i < ecoTypeKeys.size(); i++) {
					String ecoTypeKey = ecoTypeKeys.get(i);
					String milieuValue = milieu.get(i);
					oMat = new Matrix(1, ecoTypeM.size());
					if(ecoTypeM.indexOf(milieuValue) != -1){
						oMat.m_afData[0][ecoTypeM.indexOf(milieuValue)] = 1d;
					}
					oMap.put(ecoTypeKey, oMat);
				}
				internalPredictor.setEcoTypeDummyMap(oMap);
//				ecoTypeDummyReferenceMap.put(species, oMap);
				
				
				List<String> disturbM = ParameterLoaderExt.loadColumnVectorFromFile(parameterFilename, 2, String.class);
				List<String> distrubKeys = ParameterLoaderExt.loadColumnVectorFromFile(listDisturbFileName, 0, String.class);
				List<String> distrub = ParameterLoaderExt.loadColumnVectorFromFile(listDisturbFileName, 1, String.class);
				oMap = new HashMap<String, Matrix>();
				for (int i = 0; i < distrubKeys.size(); i++) {
					String ecoTypeKey = distrubKeys.get(i);
					String distrubValue = distrub.get(i);
					oMat = new Matrix(1, disturbM.size());
					if(disturbM.indexOf(distrubValue) != -1){
						oMat.m_afData[0][disturbM.indexOf(distrubValue)] = 1d;
					}
					oMap.put(ecoTypeKey, oMat);
				}
				internalPredictor.setDisturbanceDummyMap(oMap);
//				disturbDummyReferenceMap.put(species, oMap);

				List<String> effects = ParameterLoaderExt.loadColumnVectorFromFile(listEffectFilename, 0, String.class);
				List<Effect> listEffect = new ArrayList<Effect>();
				for (String effect : effects) {
					listEffect.add(Effect.fromString(effect));
				}
				internalPredictor.setEffectList(listEffect);
//				listEffectReferenceMap.put(species, listEffect);
			}

		} catch (Exception e) {
			System.out.println("BetaHeightPredictor Class : Unable to initialize the beta height-diameter relationship");
		}
	}

	/**
	 * This method calculates the height for individual trees and also
	 * implements the
	 * Monte Carlo simulation automatically. In case of exception, it also
	 * returns -1.
	 * If the predicted height is lower than 1.3, this method returns 1.3.
	 * 
	 * @param stand a HeightableStand object
	 * @param tree a HeightableTree object
	 * @return the predicted height (m)
	 */
	public synchronized double predictHeight(Heightable2014Stand stand, Heightable2014Tree tree) {
		Hd2014Species species = tree.getHeightable2014TreeSpecies();
		return internalPredictors.get(species).predictHeight(stand, tree);
//		double predictedHeight = 0;
//		if (!isBlupEstimationDone) {
//			predictHeightRandomEffects(stand);
//			isBlupEstimationDone = true;
//		}
//		setVersion(tree.getBetaHeightableTreeSpecies());
//		double observedHeight = tree.getHeightM();
//		RegressionElements regElement = fixedEffectsPrediction(stand, tree);
//		predictedHeight = regElement.fixedPred;
//		predictedHeight += blupImplementation(stand, regElement);
//		
//		double dNormResidual;
//
//		if (observedHeight > 1.3) {			// means that height was already observed
//			double variance = getDefaultRandomEffects(HierarchicalLevel.TREE).getVariance().m_afData[0][0];
//			dNormResidual = (observedHeight - predictedHeight) / Math.pow(variance, 0.5);
//			GaussianErrorTerm errorTerm = new GaussianErrorTermForHeight(tree, dNormResidual, observedHeight - predictedHeight);
//			setSpecificResiduals(tree, errorTerm);	// the residual is set in the simulatedResidualError member
////			setSpecificResiduals(tree, dNormResidual);	// the residual is set in the simulatedResidualError member
//			return -1d;
//		} else {
//			predictedHeight += residualImplementation(tree);
////			predictedHeight += residualImplementation(tree, regElement);
//			if (predictedHeight < 1.3) {
//				predictedHeight = 1.3;
//			}
//			return predictedHeight;
//		}		
	}

//	/* This method initializes all the parameters for the HdSpecies.
//	 * @throws Exception
//	 */
//	private void setVersion(BetaHdSpecies species) {
//
//		setParameterEstimates(betaMatrixReferenceMap.get(species));
//
////		defaultRandomEffects.clear();
//		setDefaultRandomEffects(HierarchicalLevel.PLOT, plotRandomEffectReferenceMap.get(species));
////		defaultRandomEffects.put(HierarchicalLevel.Tree, BetaHeightPredictor.treeRandomEffectReferenceMap.get(species));
//		
//		oXVector = oXVectorReferenceMap.get(species);
//
//	}


	
//	/**
//	 * This method computes the best linear unbiased predictors of the random effects
//	 * @param stand a HeightableStand instance
//	 */
//	private synchronized void predictHeightRandomEffects(BetaHeightableStand stand) {
//		boolean originalIsParameterVariabilityEnabled = isParametersVariabilityEnabled;
//		isParametersVariabilityEnabled = false; // temporarily disabled for the prediction of the random effects			
//		
//		Matrix blups;
//		Matrix blupsVariance;
//
//		RegressionElements regElement;
//		
//		// put all the trees for which the height is available in a Vector
//		HashMap<BetaHdSpecies, List<BetaHeightableTree>> mapHeightableTreesBySpecies = new HashMap<BetaHdSpecies, List<BetaHeightableTree>>();
//		if (!stand.getTrees().isEmpty()) {
//			for (Object tree : stand.getTrees()) {
//				if (tree instanceof BetaHeightableTree) {
//					double height = ((BetaHeightableTree) tree).getHeightM();
//					if (height > 1.3) {
//						List<BetaHeightableTree> heightableTrees = mapHeightableTreesBySpecies.get(((BetaHeightableTree) tree).getBetaHeightableTreeSpecies());
//						if (heightableTrees == null) {
//							heightableTrees = new ArrayList<BetaHeightableTree>();
//							mapHeightableTreesBySpecies.put(((BetaHeightableTree) tree).getBetaHeightableTreeSpecies(), heightableTrees);
//						}
//						heightableTrees.add((BetaHeightableTree) tree);
//					}
//					
//				}
//			}
//		}			
//
//		for (BetaHdSpecies keySpecies : mapHeightableTreesBySpecies.keySet()) {
//			Matrix matrixG = plotRandomEffectReferenceMap.get(keySpecies).getVariance();
//			List<BetaHeightableTree> heightableTrees = mapHeightableTreesBySpecies.get(keySpecies);
//			if (!heightableTrees.isEmpty()) {
//				// matrices for the blup calculation
//				int nbObs = heightableTrees.size();
//				Matrix Z = new Matrix(nbObs, matrixG.m_iRows);		// design matrix for random effects 
//				Matrix R = new Matrix(nbObs, nbObs);					// within-tree variance-covariance matrix  
//				Matrix Res = new Matrix(nbObs, 1);						// vector of residuals
//
//				for (int i = 0; i < nbObs; i++) {
//					BetaHeightableTree t = heightableTrees.get(i);
//					double height = t.getHeightM();
//					setVersion(t.getBetaHeightableTreeSpecies());
//					regElement = fixedEffectsPrediction(stand, t);
//					Z.setSubMatrix(regElement.Z_tree, i, 0);
//					double variance = treeRandomEffectReferenceMap.get(t.getBetaHeightableTreeSpecies()).getVariance().m_afData[0][0];
//					R.m_afData[i][i] = variance;
//					double residual = height - regElement.fixedPred;
//					Res.m_afData[i][0] = residual;
//				}
//				Matrix V = Z.multiply(matrixG).multiply(Z.transpose()).add(R);	// variance - covariance matrix
//				blups = matrixG.multiply(Z.transpose()).multiply(V.getInverseMatrix()).multiply(Res);							// blup_essHD is redefined according to observed values
//				blupsVariance = Z.transpose().multiply(R.getInverseMatrix()).multiply(Z).add(matrixG.getInverseMatrix()).getInverseMatrix();			// blup_essHDvar is redefined according to observed values				
//				Map<String, GaussianEstimate> randomEffectsMap = blupsLibraryPlotReferenceMap.get(keySpecies);
//				if (randomEffectsMap == null) {
//					randomEffectsMap = new HashMap<String, GaussianEstimate>();
//					blupsLibraryPlotReferenceMap.put(keySpecies, randomEffectsMap);
//				}
//				randomEffectsMap.put(stand.getSubjectId(), new GaussianEstimate(blups, blupsVariance));
//			}
//		}
//		
//
//		isParametersVariabilityEnabled = originalIsParameterVariabilityEnabled; // set the parameter variability to its original value;
//	}
	
//	/**
//	 * This method accounts for the random effects in the predictions if the random effect variability is enabled. Otherwise, it returns 0d.
//	 * @param stand = a HeightableStand object
//	 * @param regElement = a RegressionElements object
//	 * @return a simulated random effect (double)
//	 */
//	private double blupImplementation(BetaHeightableStand stand, RegressionElements regElement) {
//		Map<String, GaussianEstimate> blupsReferences = blupsLibraryPlotReferenceMap.get(regElement.species); 
//		for (String subjectID : blupsReferences.keySet()) {
//			GaussianEstimate estimate = blupsReferences.get(subjectID);
////			setBlupsForThisSubject(HierarchicalLevel.PLOT, subjectID, estimate);	// TODO FP check if this is properly implemented
//		}
//		Matrix randomEffects = getRandomEffectsForThisSubject(new BetaHeightableStandMonteCarlo(stand, regElement.species));
//		return regElement.Z_tree.multiply(randomEffects).m_afData[0][0];
//	}

	
//	/**
//	 * This method accounts for a random deviate if the residual variability is enabled. Otherwise, it returns 0d. 
//	 * @param tree a HeightableTree instance
//	 * @param regElement a RegressionElements instance
//	 * @return a simulated residual (double)
//	 */
//	private double residualImplementation(BetaHeightableTree tree, RegressionElements regElement) {
//		Matrix residuals = getSpecificResiduals(tree);
//
//		double residualForThisPrediction = 0d; 
//		if (residuals != null) {		// residuals is null only if running in deterministic mode and the height was not initially measured			
//			Matrix RChol = treeRandomEffectReferenceMap.get(regElement.species).getDistribution().getLowerCholeskyTriangle();
//			int date = tree.getYear();
//			int dateIndex = measurementDates.indexOf(date);
//			for (int i = 0; i <= dateIndex; i++) {
//				residualForThisPrediction += RChol.m_afData[dateIndex][i] * residuals.m_afData[i][0];
//			}
//		} 
//		
//		return residualForThisPrediction;
//	}
	
//	private Matrix getSpecificResiduals(BetaHeightableTree tree) {
//		if (isResidualVariabilityEnabled) {				// running in Monte Carlo mode
//			double dNormResidual = random.nextGaussian();
//			setSpecificResiduals(tree, dNormResidual);
//		} 
//		
//		long id = getSubjectPlusMonteCarloSpecificId(tree);
//		if (simulatedResidualError.containsKey(id)) {
//			return simulatedResidualError.get(id);
//		} else {
//			return null;
//		}
//	}
	
//	/**
//	 * This method records a normalized residuals into the simulatedResidualError member which is
//	 * located in the ModelBasedSimulator class. The method asks the date from the HeightableTree
//	 * instance in order to put the normalized residual at the proper location in the vector of residuals.
//	 * @param tree a HeightableTree instance
//	 * @param normalizedResidual a normalized residual
//	 */
//	private synchronized void setSpecificResiduals(BetaHeightableTree tree, double normalizedResiduals) {
//		long id = getSubjectPlusMonteCarloSpecificId(tree);
//		int date = tree.getYear();
//		int dateIndex = measurementDates.indexOf(date);
//		if (!simulatedResidualError.containsKey(id)) {
//			simulatedResidualError.put(id, new Matrix(measurementDates.size(),1));
//		}
//		Matrix residuals = simulatedResidualError.get(id);
//		residuals.m_afData[dateIndex][0] = normalizedResiduals;
//	}
	
//	/**
//	 * This method records a normalized residuals into the simulatedResidualError member which is
//	 * located in the ModelBasedSimulator class. The method asks the date from the HeightableTree
//	 * instance in order to put the normalized residual at the proper location in the vector of residuals.
//	 * @param tree a HeightableTree instance
//	 * @param errorTerm a GaussianErrorTerm instance
//	 */
//	private void setSpecificResiduals(BetaHeightableTree tree, GaussianErrorTerm errorTerm) {
//		getGaussianErrorTerms(tree).add(errorTerm);
//	}
//
//	/**
//	 * This method accounts for a random deviate if the residual variability is enabled. Otherwise, it returns 0d. 
//	 * @param tree a HeightableTree instance
//	 * @param regElement a RegressionElements instance
//	 * @return a simulated residual (double)
//	 */
//	private double residualImplementation(BetaHeightableTree tree) {
//		double residualForThisPrediction = 0d; 
//		if (isResidualVariabilityEnabled) {
//			Matrix residuals = getResidualErrorForThisSubject(tree, tree.getBetaHeightableTreeSpecies());
//			int index = getGaussianErrorTerms(tree).getDistanceIndex().indexOf(tree.getErrorTermIndex());
//			residualForThisPrediction = residuals.m_afData[index][0]; 
//		} else {			if (doesThisSubjectHaveResidualErrorTerm(tree)) {		// means that height was initially measured
//				setSpecificResiduals(tree, new GaussianErrorTerm(tree, 0d));
//				GaussianErrorTermList list = getGaussianErrorTerms(tree);
//				Matrix meanResiduals = getDefaultResidualError(tree.getBetaHeightableTreeSpecies()).getMean(list);
//				residualForThisPrediction = meanResiduals.m_afData[meanResiduals.m_iRows - 1][0];
//			} 
//		}
//		return residualForThisPrediction;
//	}


	
	
//	/**
//	 * For testing purpose.
//	 * 
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		ArrayList<Integer> outputVec = new ArrayList<Integer>();
//		for (int year = 2013; year <= 2013 + 10 * 20; year += 10) {
//			outputVec.add(year);
//		}
//
//		BetaHeightPredictor intance = new BetaHeightPredictor(false, false, false, outputVec);
//	}
//		

}
