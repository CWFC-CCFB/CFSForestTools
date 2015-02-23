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
package quebecmrnfutility.treelogger.petrotreelogger;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import quebecmrnfutility.treelogger.petrotreelogger.PetroLoggableTree.PetroLoggerSpecies;
import quebecmrnfutility.treelogger.petrotreelogger.PetroTreeLogCategory.ProductType;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.ABCDQualityProvider.ABCDQuality;
import repicea.simulation.covariateproviders.treelevel.MSCRPriorityProvider.MSCRPriority;
import repicea.simulation.covariateproviders.treelevel.VigorClassProvider.VigorClass;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.stats.StatisticalUtility;
import repicea.util.ObjectUtility;

/**
 * This TreeLogger class makes it possible to estimate the volumes by log grades in
 * sugar maple and yellow birch trees. It is based on the Petro grades (F1, F2, F3, F4, P). This class
 * does not implement the stochastic mode yet.
 * @author Mathieu Fortin - October 2009
 */
public class PetroTreeLogger extends TreeLogger<PetroTreeLoggerParameters, PetroLoggableTree> {

	// TODO implement the stochastic mode
	
	private static final int PRESENCE = 1;
	private static final int VOLUME = 2;
	
	private static final double VERY_SMALL = 1E-6;
	
	public enum PetroLoggerVersion {
		WITH_NO_VARIABLE,
		WITH_VIGOUR_1234,
		WITH_QUALITY_ABCD,
		WITH_HARV_PRIOR_MSCR;
		
		PetroLoggerVersion() {}
		
		public int getId() {return this.ordinal() + 1;}
		
	}

	
	private static Map<Integer,Matrix> betaPres;
	private static Map<Integer,Matrix> betaVol;
	private static Map<Integer,Matrix> omegaPres;
	private static Map<Integer,Matrix> omegaVol;
	private static Map<Integer,Matrix> covParmsVol;

	
	
	private Map<Integer,Matrix> defaultBeta = new TreeMap<Integer,Matrix>();
	private Map<Integer,Matrix> matrixOmega = new TreeMap<Integer,Matrix>();
	@SuppressWarnings("unused")
	private Map<Integer,Matrix> lowerCholOmegaMatrix = new TreeMap<Integer,Matrix>();
	private Map<Integer,Matrix> matrixR = new TreeMap<Integer,Matrix>();
	@SuppressWarnings("unused")
	private Map<Integer,Matrix> lowerCholRMatrix = new TreeMap<Integer,Matrix>();
	private PetroLoggerVersion selectedVersion;
	private int numberProducts;
	private boolean isModelStochastic;
	
	private Matrix oXVectorPres;
	private Matrix oXVectorVol;
	
	private Matrix m_matDBHCorrFact;
	
	private boolean loaded;
	
	
	/**
	 * Official constructor for the extension
	 */
	public PetroTreeLogger() {
		super();
	}

	/**
	 * Constructor for script mode.
	 * @param params a PetroTreeLoggerParameters instance
	 * @param trees a collection of PetroLoggableTree instances
	 */
	public PetroTreeLogger(PetroTreeLoggerParameters params, Collection<PetroLoggableTree> trees) {
		super();
		setTreeLoggerParameters(params);
		init(trees);
	}
	
	@Override
	public void setTreeLoggerParameters(PetroTreeLoggerParameters params) {
		this.params = createDefaultTreeLoggerParameters();
		numberProducts = ProductType.values().length;
	}
	
	
	@Override
	public void setTreeLoggerParameters() {
		setTreeLoggerParameters(null);
		params.showInterface(null);
	}

	
	@Override
	public void init(Collection<?> loggableTrees) {
		super.init(loggableTrees);
		PetroLoggableTree typicalTree = (PetroLoggableTree) getLoggableTrees().iterator().next();
		if (typicalTree != null) {
			//isModelStochastic = typicalTree.isModelStochastic();
			//selectedVersion = getPetroLoggerAppropriateVersion(typicalTree);
			//loadParameters();		// loads all the settings for the GeneralVolumeEquation
		} else {
			throw new InvalidParameterException("There is no PetroTreeEligible for the TreeLogger!");
		}
	}

	/**
	 * This method sets the appropriate version according to the features of the typical tree.
	 */
	private PetroLoggerVersion getPetroLoggerAppropriateVersion(PetroLoggableTree typicalTree) {
		
		Object qualiteABCD = typicalTree.getABCDQuality();
		if (qualiteABCD != null) {
			return PetroLoggerVersion.WITH_QUALITY_ABCD;
		} 
		
		Object harvestPriorityMSCR = typicalTree.getMSCRPriority();
		if (harvestPriorityMSCR != null) {
			return PetroLoggerVersion.WITH_HARV_PRIOR_MSCR;
		}
		
		Object vigor1234 = typicalTree.getVigorClass();
		if (vigor1234 != null) {
			return PetroLoggerVersion.WITH_VIGOUR_1234;
		}
		
		return PetroLoggerVersion.WITH_NO_VARIABLE;
	}

	
	private synchronized void loadDefaultParameters() throws IOException {
		if (!loaded) {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			
			String strParametersPresencePath = path + "0_PetroProductPresBeta.csv";
			String strParametersVolumePath = path + "0_PetroProductVolBeta.csv";
			betaPres = loadFileIntDoubleToMapIntMatrix(strParametersPresencePath, false);
			betaVol = loadFileIntDoubleToMapIntMatrix(strParametersVolumePath, false);

			String strOmegaPresencePath = path + "0_PetroProductPresOmega.csv";
			String strOmegaVolumePath = path + "0_PetroProductVolOmega.csv";
			omegaPres = loadFileIntDoubleToMapIntMatrix(strOmegaPresencePath, true);
			omegaVol = loadFileIntDoubleToMapIntMatrix(strOmegaVolumePath, true);

			String strCovParmsVolumePath = path + "0_PetroProductVolCovParms.csv";
			covParmsVol = loadFileIntDoubleToMapIntMatrix(strCovParmsVolumePath, true);
			loaded = true;
		}				
	}
	
	
	private void loadParameters() {
		//	if (!loaded) {
			try {
				loadDefaultParameters();
			} catch (IOException e) {
				throw new InvalidParameterException("Unable to initialize the PetroTreeLogger instance");
			}

			defaultBeta.put(PetroTreeLogger.PRESENCE, betaPres.get(selectedVersion.getId()));
			defaultBeta.put(PetroTreeLogger.VOLUME, betaVol.get(selectedVersion.getId()));
			matrixOmega.put(PetroTreeLogger.PRESENCE, omegaPres.get(selectedVersion.getId()));
			matrixOmega.put(PetroTreeLogger.VOLUME, omegaVol.get(selectedVersion.getId()));
			matrixR.put(PetroTreeLogger.VOLUME, covParmsVol.get(selectedVersion.getId()));

			lowerCholOmegaMatrix = copyMatrixMapToLowerCholMap(matrixOmega);
			lowerCholRMatrix = copyMatrixMapToLowerCholMap(matrixR);

			oXVectorPres = new Matrix(1,this.defaultBeta.get(PetroTreeLogger.PRESENCE).m_iRows);
			oXVectorVol = new Matrix(1,this.defaultBeta.get(PetroTreeLogger.VOLUME).m_iRows);
			
			// all the parameters related to dbh are square for deterministic correction
			Matrix betaVolume = this.defaultBeta.get(PetroTreeLogger.VOLUME);
			Matrix dbhParam = betaVolume.getSubMatrix(betaVolume.m_iRows-5, betaVolume.m_iRows-1, 0, 0);
			this.m_matDBHCorrFact = dbhParam.elementWiseMultiply(dbhParam);
		//	}
	}
	
	private Map<Integer,Matrix> copyMatrixMapToLowerCholMap(Map<Integer,Matrix> oInputMap) {
		Map<Integer,Matrix> oOutputMap = new TreeMap<Integer,Matrix>();
		for (Iterator<Integer> i = oInputMap.keySet().iterator(); i.hasNext();) {
			int modelPart = i.next();
			oOutputMap.put(modelPart, oInputMap.get(modelPart).getLowerCholTriangle());
		}
		return oOutputMap;
	}
	
	
	@Override
	protected void logThisTree(PetroLoggableTree tree) {
		isModelStochastic = tree.isModelStochastic();
		selectedVersion = getPetroLoggerAppropriateVersion(tree);
		loadParameters();
		Map<Integer, Matrix> oMapBeta = defaultBeta;
		PetroLoggableTree t = (PetroLoggableTree) tree;
		Matrix probabilities = predictGradePresence(t, oMapBeta.get(PetroTreeLogger.PRESENCE));
		Matrix untransformedVolumes = predictLogTransformedGradeVolume(t, oMapBeta.get(PetroTreeLogger.VOLUME));

		Matrix correctionFactor;
		if (this.isModelStochastic) {
			correctionFactor = this.matrixR.get(PetroTreeLogger.VOLUME).diagonalVector().scalarMultiply(0.5);
		} else {
			double dbhVariance = t.getDbhCmVariance();
			correctionFactor = this.m_matDBHCorrFact.scalarMultiply(dbhVariance)
					.add(this.matrixR.get(PetroTreeLogger.VOLUME).diagonalVector())
					.scalarMultiply(0.5);
		}

		Matrix conditionalVolumes = untransformedVolumes.add(correctionFactor).expMatrix();
		Matrix volumes = probabilities.elementWiseMultiply(conditionalVolumes);

		PetroLoggerSpecies species = t.getPetroLoggableTreeSpecies(); 
		for (int i = 0; i < volumes.m_iRows; i++) {
			if (volumes.m_afData[i][0] > VERY_SMALL) {
				PetroTreeLogCategory product = getTreeLoggerParameters().getSpeciesLogCategories(species.name()).get(i);
				PetroTreeLoggerWoodPiece piece = new PetroTreeLoggerWoodPiece(product, volumes.m_afData[i][0], t);
				addWoodPiece(tree, piece);
			}
		}
	}


	/**
	 * This method computes a matrix that contains the probabilities of observing
	 * the Petro grade in a TreePetroProductable type object
	 * @param tree a PetroLoggableTree instance
	 * @param beta a Matrix instance that contains the parameters
	 * @return the resulting matrix
	 */
	private Matrix predictGradePresence(PetroLoggableTree tree, Matrix beta) {
		PetroLoggerSpecies species = tree.getPetroLoggableTreeSpecies();
		double dbh = tree.getDbhCm();
		double dbh2 = tree.getSquaredDbhCm();

		Matrix oMat = new Matrix(this.numberProducts,1);
		Matrix dummySpecies = species.getDummy();
		
		List<PetroTreeLogCategory> logCategories = getTreeLoggerParameters().getSpeciesLogCategories(species.name());
		
		for (PetroTreeLogCategory product : logCategories) {
			this.oXVectorPres.resetMatrix();
			int pointer = 0;
			Matrix dummyProduct = product.getProductType().getDummy();
			
			if (selectedVersion != PetroLoggerVersion.WITH_NO_VARIABLE) {
				Matrix dummyVersion = getDummyVsSelectedVersion(dummyProduct, tree);
				oXVectorPres.setSubMatrix(dummyVersion, 0, pointer);
				pointer += dummyVersion.m_iCols;
			}
			
			Matrix dummyProductSpecies = StatisticalUtility.combineMatrices(dummyProduct, dummySpecies);
			oXVectorPres.setSubMatrix(dummyProductSpecies, 0, pointer);
			pointer += dummyProductSpecies.m_iCols;
			
			oXVectorPres.setSubMatrix(dummyProductSpecies.scalarMultiply(dbh), 0, pointer);
			pointer += dummyProductSpecies.m_iCols;
			
			oXVectorPres.setSubMatrix(dummyProduct.scalarMultiply(dbh2), 0, pointer);
			pointer += dummyProduct.m_iCols;
			
			double exp_xBeta = Math.exp(oXVectorPres.multiply(beta).m_afData[0][0]);
			double probability = (double) exp_xBeta / (1.0 + exp_xBeta);
			int productIndex = logCategories.indexOf(product);
			oMat.m_afData[productIndex][0] = probability;
		}
		return oMat;
	}

	/**
	 * This method computes a matrix that contains the log transformed
	 * volumes for each Petro grade conditional on their presence
	 * @param tree
	 * @param speciesName
	 * @param beta
	 * @return the resulting matrix
	 */
	private Matrix predictLogTransformedGradeVolume(PetroLoggableTree tree, Matrix beta) {
		PetroLoggerSpecies species = tree.getPetroLoggableTreeSpecies();
		double dbh = tree.getDbhCm();
		
		Matrix oMat = new Matrix(this.numberProducts,1);
		Matrix dummySpecies = species.getDummy();
		
		List<PetroTreeLogCategory> logCategories = getTreeLoggerParameters().getSpeciesLogCategories(species.name());
				
		for (PetroTreeLogCategory product : logCategories) {
			this.oXVectorVol.resetMatrix();
			int pointer = 0;
			Matrix dummyProduct = product.getProductType().getDummy();
			
			if (selectedVersion != PetroLoggerVersion.WITH_NO_VARIABLE) {
				Matrix dummyVersion = getDummyVsSelectedVersion(dummyProduct, tree);
				oXVectorVol.setSubMatrix(dummyVersion, 0, pointer);
				pointer += dummyVersion.m_iCols;
			}

			Matrix dummyProductSpecies = StatisticalUtility.combineMatrices(dummyProduct, dummySpecies);
			oXVectorVol.setSubMatrix(dummyProductSpecies, 0, pointer);
			pointer += dummyProductSpecies.m_iCols;
			
			oXVectorVol.setSubMatrix(dummyProduct.scalarMultiply(dbh), 0, pointer);
			pointer += dummyProduct.m_iCols;
			
			double xBeta = oXVectorVol.multiply(beta).m_afData[0][0];
			int productIndex = logCategories.indexOf(product);
			oMat.m_afData[productIndex][0] = xBeta;
		}
		return oMat;
	}
	
	/**
	 * This private method computes the first part of the XVector depending on
	 * the selected version of the model
	 * @param dummyProduct
	 * @param tree
	 * @return the resulting matrix (a row vector)
	 */
	@SuppressWarnings("incomplete-switch")
	private Matrix getDummyVsSelectedVersion(Matrix dummyProduct, PetroLoggableTree tree) {
		Matrix oMat = null;
		switch (selectedVersion) {
		case WITH_VIGOUR_1234:
			VigorClass vigour1234 = tree.getVigorClass();
			Matrix dummyVig = vigour1234.geDummyVig();
			Matrix dummyProd = vigour1234.geDummyProd();
			oMat = new Matrix(1,dummyProduct.m_iCols*(dummyVig.m_iCols + dummyProd.m_iCols));
			oMat.setSubMatrix(StatisticalUtility.combineMatrices(dummyProduct, dummyVig), 0, 0);
			oMat.setSubMatrix(StatisticalUtility.combineMatrices(dummyProduct, dummyProd), 0, dummyProduct.m_iCols * dummyVig.m_iCols);
			break;
		case WITH_HARV_PRIOR_MSCR: 
			MSCRPriority priorityMSCR = tree.getMSCRPriority();
			oMat = StatisticalUtility.combineMatrices(dummyProduct, priorityMSCR.getDummy());
			break;
		case WITH_QUALITY_ABCD:
			ABCDQuality qualityABCD = tree.getABCDQuality();
			oMat = StatisticalUtility.combineMatrices(dummyProduct, qualityABCD.getDummy());
			break;
		}
		return oMat;
	}
	
//	/**
//	 * This method log the trees and predicts the log grades. Identical to the predictPetroGrades method except for the Exception handling.
//	 */
//	public void logTrees() {
//		predictPetroGrades();
//	}

//	/**
//	 * This method extracts a collection of TreePetroLoggable objects from a collection of LoggableTree instances.
//	 * @param trees a Collection of LoggableTree-derived instances
//	 * @return a Collection of PetroLoggableTree instances
//	 */
//	private Collection<PetroLoggableTree> getValidPetroLoggableTreesFromACollection(Collection<? extends LoggableTree> loggableTrees) {
//		Collection<PetroLoggableTree> petroLoggableTrees = new ArrayList<PetroLoggableTree>();
//		for (LoggableTree t : loggableTrees) {
//			if (t instanceof PetroLoggableTree) {
//				PetroLoggableTree tree = (PetroLoggableTree) t;
//				if (isEligible(tree)){
//					petroLoggableTrees.add((PetroLoggableTree) t);
//				}
//			}
//		}
//		return petroLoggableTrees;
//	}
	
	@Override
	public PetroTreeLoggerParameters createDefaultTreeLoggerParameters() {
		PetroTreeLoggerParameters params = new PetroTreeLoggerParameters();
		params.initializeDefaultLogCategories();
		return params;
	}

	
	
	/**
	 * This method reads files that contain an integer as index and a series of double. Useful to read parameter files.
	 * @param strFilename the filename
	 * @param omegaMatrix a boolean to indicate whether the series of doubles is a variance-covariance matrix 
	 * @return a map whose keys are the integer and the values are matrices or null if a IOException was thrown
	 */
	private Map<Integer,Matrix> loadFileIntDoubleToMapIntMatrix(String strFilename, boolean omegaMatrix) {
		Map<Integer,Matrix> oFinalMap = new TreeMap<Integer,Matrix>();
		Map<Integer,Vector<Double>> oMap = new TreeMap<Integer,Vector<Double>>();

		int modelVersion;
		double modelParm;
		
		CSVReader reader = null;
		try {
			reader = new CSVReader(strFilename);
			Object[] lineRead = reader.nextRecord();
			while (lineRead != null) {

				modelVersion = Integer.parseInt(lineRead[0].toString());
				modelParm = Double.parseDouble(lineRead[1].toString());

				Vector<Double> oVectorTmp = oMap.get(modelVersion);
				if (oVectorTmp==null) {
					oVectorTmp = new Vector<Double>();
					oMap.put(modelVersion, oVectorTmp);
				}
				
				oVectorTmp.add(modelParm);
				lineRead = reader.nextRecord();
			}

			reader.close();
			
			for (Iterator<Integer> i = oMap.keySet().iterator(); i.hasNext();) {
				modelVersion = i.next();
				Matrix oMatTmp;
				if (omegaMatrix) {
					oMatTmp = new Matrix(oMap.get(modelVersion)).squareSym();
				} else {
					oMatTmp = new Matrix(oMap.get(modelVersion));
				}
				oFinalMap.put(modelVersion, oMatTmp);
			}
			return oFinalMap;
			
		} catch (IOException e) {
			System.out.println("An error occurred while reading file " + strFilename);
			return null;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	@Override
	public PetroLoggableTree getEligible(LoggableTree t) {
		if (t instanceof PetroLoggableTree) {
			PetroLoggableTree tree = (PetroLoggableTree) t;
			if (tree.getPetroLoggableTreeSpecies() != null && tree.getDbhCm() > 23d) {
				return tree;
			}
			
		}
		return null;
	}


//	public static void main(String[] args) {
//		PetroTreeLogger treeLogger = new PetroTreeLogger();
//		treeLogger.setTreeLoggerParameters();
//		int u = 0;
//	}
	
}
