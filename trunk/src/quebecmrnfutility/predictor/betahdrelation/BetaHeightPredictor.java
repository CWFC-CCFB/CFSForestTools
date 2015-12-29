package quebecmrnfutility.predictor.betahdrelation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quebecmrnfutility.predictor.betahdrelation.BetaHeightableTree.BetaHdSpecies;
import repicea.math.Matrix;
import repicea.predictor.QuebecGeneralSettings;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelBasedSimulator;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.stats.StatisticalUtility;
import repicea.stats.distributions.GaussianErrorTerm;
import repicea.stats.distributions.GaussianErrorTermList;
import repicea.stats.distributions.GaussianErrorTermList.IndexableErrorTerm;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

/**
 * This class implements the beta height-diameter relationship published in
 * Fortin et al. (2009)
 * 
 * @author Hache Denis - Fev 2014
 * @see Fortin, M., Bernier, S., Saucier, J.-P., and Labb�, F. 2009, Auger, I. Une
 *      relation hauteur-diam�tre tenant compte de l'influence de la station et
 *      du climat pour 20 esp�ces commerciales du Qu�bec. Gouvernement du
 *      Qu�bec, Minist�re des Ressources naturelles et de la Faune, Direction de
 *      la recherche foresti�re. M�moire de recherche foresti�re no 153. 22 p.
 */
public class BetaHeightPredictor extends ModelBasedSimulator {
	
	private static class RegressionElements implements Serializable {
		private static final long serialVersionUID = 20100804L;
		public Matrix Z_tree;
		public double fixedPred;
		public BetaHdSpecies species;
		public RegressionElements() {}
	}

	@SuppressWarnings("serial")
	private static class GaussianErrorTermForHeight extends GaussianErrorTerm {

		public GaussianErrorTermForHeight(IndexableErrorTerm caller, double normalizedValue, double observedValue) {
			super(caller, normalizedValue);
			this.value = observedValue;
		}
		
	}

	protected static class BetaHeightableStandMonteCarlo implements MonteCarloSimulationCompliantObject {
		private final int monteCarloRealization;
		private int					subjectID;
		private HierarchicalLevel	hieraLevel;

		protected BetaHeightableStandMonteCarlo(MonteCarloSimulationCompliantObject subject, BetaHdSpecies species) {
			String id = (subject.getMonteCarloRealizationId() + "_" + species.ordinal());
			this.monteCarloRealization = id.hashCode();
			subjectID = subject.getSubjectId();
			hieraLevel = subject.getHierarchicalLevel();
		}

		@Override
		public int getSubjectId() {
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
	
	
	private static enum DisturbanceType{
		INT,
		MOY,
		NON;		
	}
	/**
	 * This enum variable contains all the possible effects
	 * @author Denis hache - Fev 2014
	 */
	private static enum Effect {
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

	@Deprecated	
	private List<Integer>							measurementDates;
	private boolean									isBlupEstimationDone;

	private Map<BetaHdSpecies, SASParameterEstimate>	betaMatrixReferenceMap	= new HashMap<BetaHdSpecies, SASParameterEstimate>();
	private Map<BetaHdSpecies, GaussianEstimate>	plotRandomEffectReferenceMap	= new HashMap<BetaHdSpecies, GaussianEstimate>();
	private Map<BetaHdSpecies, GaussianEstimate> treeRandomEffectReferenceMap = new HashMap<BetaHdSpecies, GaussianEstimate>();
	private Map<BetaHdSpecies, Map<String, Matrix>>	subDomainDummyReferenceMap		= new HashMap<BetaHdSpecies, Map<String, Matrix>>();
	private Map<BetaHdSpecies, Map<String, Matrix>>	vegPotDummyReferenceMap			= new HashMap<BetaHdSpecies, Map<String, Matrix>>();
	private Map<BetaHdSpecies, Map<String, Matrix>>	ecoTypeDummyReferenceMap		= new HashMap<BetaHdSpecies, Map<String, Matrix>>();
	private Map<BetaHdSpecies, Map<String, Matrix>>	disturbDummyReferenceMap		= new HashMap<BetaHdSpecies, Map<String, Matrix>>();
	private Map<BetaHdSpecies, List<Effect>>			listEffectReferenceMap			= new HashMap<BetaHdSpecies, List<Effect>>();
	private Map<BetaHdSpecies, Matrix> oXVectorReferenceMap = new  HashMap<BetaHdSpecies, Matrix>();
	private Map<BetaHdSpecies, Map<Integer, GaussianEstimate>> blupsLibraryPlotReferenceMap = new HashMap<BetaHdSpecies, Map<Integer, GaussianEstimate>>();

	public BetaHeightPredictor(boolean isParametersVariabilityEnabled, boolean isRandomEffectsVariabilityEnabled, boolean isResidualVariabilityEnabled,
			List<Integer> measurementDates) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		this.measurementDates = measurementDates;
		init();
	}

	/**
	 * Default constructor with all sources of uncertainty disabled.
	 * 
	 * @param measurementDates a list of integers that define the measurement
	 *            dates
	 */
	public BetaHeightPredictor(List<Integer> measurementDates) {
		this(false, false, false, measurementDates);
	}

	@Override
	protected final void init() {
		try {
			for (BetaHdSpecies species : BetaHdSpecies.values()) {			
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
				//RealMatrix matrix = new RealMatrixImpl(omega.m_afData);
				//CholeskyDecomposition chol = new CholeskyDecompositionImpl(matrix);
				//matrix = chol.getL();

				SASParameterEstimate defaultBeta = new SASParameterEstimate(beta, omega);
				betaMatrixReferenceMap.put(species, defaultBeta);
				oXVectorReferenceMap.put(species, new Matrix(1,defaultBeta.getMean().m_iRows));
				Matrix randomEffects = ParameterLoaderExt.loadVectorFromFile(plotRandomEffectsFilename).get();
				Matrix matrixG = randomEffects.getSubMatrix(0, 0, 0, 0);
				Matrix defaultRandomEffectsMean = new Matrix(matrixG.m_iRows, 1);
				plotRandomEffectReferenceMap.put(species, new GaussianEstimate(defaultRandomEffectsMean, matrixG));
				
				Matrix years = new Matrix(measurementDates);
				double sigma2 = randomEffects.m_afData[2][0];//residual
				double phi = randomEffects.m_afData[1][0];//tree
				Matrix oMat = StatisticalUtility.constructRMatrix(years, sigma2, phi, StatisticalUtility.TypeMatrixR.POWER);
				treeRandomEffectReferenceMap.put(species, new GaussianEstimate(new Matrix(oMat.m_iRows, 1), oMat));
				setDefaultResidualError(species, new GaussianErrorTermEstimate(randomEffects.getSubMatrix(2, 2, 0, 0), phi, StatisticalUtility.TypeMatrixR.POWER));

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
				subDomainDummyReferenceMap.put(species, oMap);

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
				vegPotDummyReferenceMap.put(species, oMap);

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
				ecoTypeDummyReferenceMap.put(species, oMap);
				
				
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
				disturbDummyReferenceMap.put(species, oMap);

				List<String> effects = ParameterLoaderExt.loadColumnVectorFromFile(listEffectFilename, 0, String.class);
				List<Effect> listEffect = new ArrayList<Effect>();
				for (String effect : effects) {
					listEffect.add(Effect.fromString(effect));
				}
				listEffectReferenceMap.put(species, listEffect);

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
	public synchronized double predictHeight(BetaHeightableStand stand, BetaHeightableTree tree) {
		double predictedHeight = 0;
		if (!isBlupEstimationDone) {
				predictHeightRandomEffects(stand);
			isBlupEstimationDone = true;
		}
		setVersion(tree.getBetaHeightableTreeSpecies());
		double observedHeight = tree.getHeightM();
		RegressionElements regElement = fixedEffectsPrediction(stand, tree);
		predictedHeight = regElement.fixedPred;
		predictedHeight += blupImplementation(stand, regElement);
		
		double dNormResidual;

		if (observedHeight > 1.3) {			// means that height was already observed
			double variance = getDefaultRandomEffects(HierarchicalLevel.TREE).getVariance().m_afData[0][0];
			dNormResidual = (observedHeight - predictedHeight) / Math.pow(variance, 0.5);
			GaussianErrorTerm errorTerm = new GaussianErrorTermForHeight(tree, dNormResidual, observedHeight - predictedHeight);
			setSpecificResiduals(tree, errorTerm);	// the residual is set in the simulatedResidualError member
//			setSpecificResiduals(tree, dNormResidual);	// the residual is set in the simulatedResidualError member
			return -1d;
		} else {
			predictedHeight += residualImplementation(tree);
//			predictedHeight += residualImplementation(tree, regElement);
			if (predictedHeight < 1.3) {
				predictedHeight = 1.3;
			}
			return predictedHeight;
		}		
	}

	/* This method initializes all the parameters for the HdSpecies.
	 * @throws Exception
	 */
	private void setVersion(BetaHdSpecies species) {

		setDefaultBeta(betaMatrixReferenceMap.get(species));

//		defaultRandomEffects.clear();
		setDefaultRandomEffects(HierarchicalLevel.PLOT, plotRandomEffectReferenceMap.get(species));
//		defaultRandomEffects.put(HierarchicalLevel.Tree, BetaHeightPredictor.treeRandomEffectReferenceMap.get(species));
		
		oXVector = oXVectorReferenceMap.get(species);

	}

	/**
	 * This method computes the fixed effect prediction and put the prediction,
	 * the Z vector,
	 * and the species name into m_oRegressionOutput member. The method applies
	 * in any cases no matter
	 * it is deterministic or stochastic.
	 * 
	 * @param stand a HeightableStand instance
	 * @param t a HeightableTree instance
	 */
	private synchronized RegressionElements fixedEffectsPrediction(BetaHeightableStand stand, BetaHeightableTree t) {
		Matrix modelParameters = getParametersForThisRealization(new BetaHeightableStandMonteCarlo(stand, t.getBetaHeightableTreeSpecies()));

		double basalArea = stand.getBasalAreaM2Ha();
		double averageTemp = stand.getMeanAnnualTemperatureC();	
		String ecoRegion = stand.getEcoRegion();
		boolean isInterventionResult = stand.isInterventionResult();
		boolean isDefoliated = stand.isSBWDefoliated();
		double averageQDiam  = stand.getMeanQuadraticDiameterCm();
		double elevation = stand.getElevationM();	

		String subDomain = QuebecGeneralSettings.ECO_REGION_MAP.get(ecoRegion);
		Map<String, Matrix> oMap = subDomainDummyReferenceMap.get(t.getBetaHeightableTreeSpecies());
		Matrix dummySubDomain = oMap.get(subDomain.toUpperCase());
		
		String potentialVegetation = stand.getEcologicalType().substring(0,3).toUpperCase();	
		Matrix dummyPotVeg = vegPotDummyReferenceMap.get(t.getBetaHeightableTreeSpecies()).get(potentialVegetation.toUpperCase());

		String environmentType = stand.getEcologicalType().substring(3, 4);
		Matrix dummyEcoType = ecoTypeDummyReferenceMap.get(t.getBetaHeightableTreeSpecies()).get(environmentType.toUpperCase());
		
		Matrix dummyDisturbance;
		if (isInterventionResult) {
			dummyDisturbance = disturbDummyReferenceMap.get(t.getBetaHeightableTreeSpecies()).get(DisturbanceType.INT.toString());
		} else if (isDefoliated) {
			dummyDisturbance = disturbDummyReferenceMap.get(t.getBetaHeightableTreeSpecies()).get(DisturbanceType.MOY.toString());
		} else {
			dummyDisturbance = disturbDummyReferenceMap.get(t.getBetaHeightableTreeSpecies()).get(DisturbanceType.NON.toString());
		}

		oXVector.resetMatrix();
		int pointeur = 0;
		BetaHdSpecies species = t.getBetaHeightableTreeSpecies();
		double lnDbh = t.getLnDbhCmPlus1();		
		double lnDbh2 = t.getSquaredLnDbhCmPlus1();
		

		List<Effect> effects = listEffectReferenceMap.get(t.getBetaHeightableTreeSpecies());
		for (Effect effect : effects) {
			switch (effect) {
			case LogDbh:
				oXVector.m_afData[0][pointeur++] = lnDbh;
				break;
			case LogDbh2:
				oXVector.m_afData[0][pointeur++] = lnDbh2;
				break;
			case LogDbh_basalArea:
				oXVector.m_afData[0][pointeur++] = lnDbh * basalArea;
				break;
			case LogDbh_ratioDbh:
				oXVector.m_afData[0][pointeur++] = lnDbh * (t.getDbhCm()/averageQDiam);
				break;
//			case LogDbh_basalAreaGreaterThan:
//				oXVector.m_afData[0][pointeur++] = lnDbh * t.getBasalAreaLargerThanSubjectM2Ha();
//				break;
//			case LogDbh2_basalAreaGreaterThan:
//				oXVector.m_afData[0][pointeur++] = lnDbh2 * t.getBasalAreaLargerThanSubjectM2Ha();
//				break;
			case LogDbh2_ratioDbh:
				oXVector.m_afData[0][pointeur++] = lnDbh2 * (t.getDbhCm()/averageQDiam);
				break;
			case LogDbh_SubDom:
				oXVector.setSubMatrix(dummySubDomain.scalarMultiply(lnDbh), 0, pointeur);
				pointeur += dummySubDomain.m_iCols;
				break;
			case LogDbh_PotVeg:
				oXVector.setSubMatrix(dummyPotVeg.scalarMultiply(lnDbh), 0, pointeur);
				pointeur += dummyPotVeg.m_iCols;
				break;
			case LogDbh_Elevation:
				oXVector.m_afData[0][pointeur++] = lnDbh * elevation;
				break;
			case LogDbh_EcoType:
				oXVector.setSubMatrix(dummyEcoType.scalarMultiply(lnDbh), 0, pointeur);
				pointeur += dummyEcoType.m_iCols;
				break;
			case LogDbh_Disturb:
				oXVector.setSubMatrix(dummyDisturbance.scalarMultiply(lnDbh), 0, pointeur);
				pointeur += dummyDisturbance.m_iCols;
				break;
			case LogDbh_meanT:
				oXVector.m_afData[0][pointeur++] = lnDbh * averageTemp;
				break;
			case LogDbh_pTot:
				oXVector.m_afData[0][pointeur++] = lnDbh * stand.getMeanAnnualPrecipitationMm();
				break;
//			case LogDbh_Dens:
//				oXVector.m_afData[0][pointeur++] = lnDbh * stand.getNumberOfStemsHa();
//				break;
			case LogDbh_NotOuest:
				int isNotOuest = 0;
				String ouest = subDomain.substring(1, subDomain.length()).toUpperCase();
				if(!ouest.equals("OUEST")){
					isNotOuest = 1;
				}
				oXVector.m_afData[0][pointeur++] = lnDbh * isNotOuest;
				break;
//			case LogDbh_Is:
//				oXVector.m_afData[0][pointeur++] = lnDbh * t.getSocialStatusIndex();
//				break;
			default:
				System.out.println("BetaHeightPredictor Class : Unable to apply effect "+effect);
				break;
			}
		}

		Matrix Z_i = new Matrix(1,1);
		Z_i.m_afData[0][0] =lnDbh2;	// design vector for the plot random effect

		double fResult = 1.3 + oXVector.multiply(modelParameters).m_afData[0][0];

		RegressionElements regElements = new RegressionElements();

		regElements.fixedPred = fResult;
		regElements.Z_tree = Z_i;
		regElements.species = species;

		return regElements;
	}

	
	/**
	 * This method computes the best linear unbiased predictors of the random effects
	 * @param stand a HeightableStand instance
	 */
	private synchronized void predictHeightRandomEffects(BetaHeightableStand stand) {
		boolean originalIsParameterVariabilityEnabled = isParametersVariabilityEnabled;
		isParametersVariabilityEnabled = false; // temporarily disabled for the prediction of the random effects			
		
		Matrix blups;
		Matrix blupsVariance;

		RegressionElements regElement;
		
		// put all the trees for which the height is available in a Vector
		HashMap<BetaHdSpecies, List<BetaHeightableTree>> mapHeightableTreesBySpecies = new HashMap<BetaHdSpecies, List<BetaHeightableTree>>();
		if (!stand.getTrees().isEmpty()) {
			for (Object tree : stand.getTrees()) {
				if (tree instanceof BetaHeightableTree) {
					double height = ((BetaHeightableTree) tree).getHeightM();
					if (height > 1.3) {
						List<BetaHeightableTree> heightableTrees = mapHeightableTreesBySpecies.get(((BetaHeightableTree) tree).getBetaHeightableTreeSpecies());
						if (heightableTrees == null) {
							heightableTrees = new ArrayList<BetaHeightableTree>();
							mapHeightableTreesBySpecies.put(((BetaHeightableTree) tree).getBetaHeightableTreeSpecies(), heightableTrees);
						}
						heightableTrees.add((BetaHeightableTree) tree);
					}
					
				}
			}
		}			

		for (BetaHdSpecies keySpecies : mapHeightableTreesBySpecies.keySet()) {
			Matrix matrixG = plotRandomEffectReferenceMap.get(keySpecies).getVariance();
			List<BetaHeightableTree> heightableTrees = mapHeightableTreesBySpecies.get(keySpecies);
			if (!heightableTrees.isEmpty()) {
				// matrices for the blup calculation
				int nbObs = heightableTrees.size();
				Matrix Z = new Matrix(nbObs, matrixG.m_iRows);		// design matrix for random effects 
				Matrix R = new Matrix(nbObs, nbObs);					// within-tree variance-covariance matrix  
				Matrix Res = new Matrix(nbObs, 1);						// vector of residuals

				for (int i = 0; i < nbObs; i++) {
					BetaHeightableTree t = heightableTrees.get(i);
					double height = t.getHeightM();
					setVersion(t.getBetaHeightableTreeSpecies());
					regElement = fixedEffectsPrediction(stand, t);
					Z.setSubMatrix(regElement.Z_tree, i, 0);
					double variance = treeRandomEffectReferenceMap.get(t.getBetaHeightableTreeSpecies()).getVariance().m_afData[0][0];
					R.m_afData[i][i] = variance;
					double residual = height - regElement.fixedPred;
					Res.m_afData[i][0] = residual;
				}
				Matrix V = Z.multiply(matrixG).multiply(Z.transpose()).add(R);	// variance - covariance matrix
				blups = matrixG.multiply(Z.transpose()).multiply(V.getInverseMatrix()).multiply(Res);							// blup_essHD is redefined according to observed values
				blupsVariance = Z.transpose().multiply(R.getInverseMatrix()).multiply(Z).add(matrixG.getInverseMatrix()).getInverseMatrix();			// blup_essHDvar is redefined according to observed values				
				Map<Integer, GaussianEstimate> randomEffectsMap = blupsLibraryPlotReferenceMap.get(keySpecies);
				if (randomEffectsMap == null) {
					randomEffectsMap = new HashMap<Integer, GaussianEstimate>();
					blupsLibraryPlotReferenceMap.put(keySpecies, randomEffectsMap);
				}
				randomEffectsMap.put(stand.getSubjectId(), new GaussianEstimate(blups, blupsVariance));
			}
		}
		

		isParametersVariabilityEnabled = originalIsParameterVariabilityEnabled; // set the parameter variability to its original value;
	}
	
	/**
	 * This method accounts for the random effects in the predictions if the random effect variability is enabled. Otherwise, it returns 0d.
	 * @param stand = a HeightableStand object
	 * @param regElement = a RegressionElements object
	 * @return a simulated random effect (double)
	 */
	private double blupImplementation(BetaHeightableStand stand, RegressionElements regElement) {
		Map<Integer, GaussianEstimate> blupsReferences = blupsLibraryPlotReferenceMap.get(regElement.species); 
		for (Integer subjectID : blupsReferences.keySet()) {
			GaussianEstimate estimate = blupsReferences.get(subjectID);
			setBlupsForThisSubject(HierarchicalLevel.PLOT, subjectID, estimate);	// TODO FP check if this is properly implemented
		}
		Matrix randomEffects = getRandomEffectsForThisSubject(new BetaHeightableStandMonteCarlo(stand, regElement.species));
		return regElement.Z_tree.multiply(randomEffects).m_afData[0][0];
	}

	
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
	
	/**
	 * This method records a normalized residuals into the simulatedResidualError member which is
	 * located in the ModelBasedSimulator class. The method asks the date from the HeightableTree
	 * instance in order to put the normalized residual at the proper location in the vector of residuals.
	 * @param tree a HeightableTree instance
	 * @param errorTerm a GaussianErrorTerm instance
	 */
	private void setSpecificResiduals(BetaHeightableTree tree, GaussianErrorTerm errorTerm) {
		getGaussianErrorTerms(tree).add(errorTerm);
	}

	/**
	 * This method accounts for a random deviate if the residual variability is enabled. Otherwise, it returns 0d. 
	 * @param tree a HeightableTree instance
	 * @param regElement a RegressionElements instance
	 * @return a simulated residual (double)
	 */
	private double residualImplementation(BetaHeightableTree tree) {
		double residualForThisPrediction = 0d; 
		if (isResidualVariabilityEnabled) {
			Matrix residuals = getResidualErrorForThisSubject(tree, tree.getBetaHeightableTreeSpecies());
			int index = getGaussianErrorTerms(tree).getDistanceIndex().indexOf(tree.getErrorTermIndex());
			residualForThisPrediction = residuals.m_afData[index][0]; 
		} else {			if (doesThisSubjectHaveResidualErrorTerm(tree)) {		// means that height was initially measured
				setSpecificResiduals(tree, new GaussianErrorTerm(tree, 0d));
				GaussianErrorTermList list = getGaussianErrorTerms(tree);
				Matrix meanResiduals = getDefaultResidualError(tree.getBetaHeightableTreeSpecies()).getMean(list);
				residualForThisPrediction = meanResiduals.m_afData[meanResiduals.m_iRows - 1][0];
			} 
		}
		return residualForThisPrediction;
	}

	
	
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
