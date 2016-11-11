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
package quebecmrnfutility.predictor.stemtaper.schneiderequations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperTree.StemTaperTreeSpecies;
import repicea.math.Matrix;
import repicea.predictor.QuebecGeneralSettings;

/**
 * This package class contains the settings of the stem taper equations such as the dummy variables and the 
 * list of the different effects in each version of the stem taper model.
 * @author Mathieu Fortin - January 2012
 */
class StemTaperEquationSettings implements Serializable {

	private static final long serialVersionUID = 20120114L;

	protected static enum ModelType {
		TREEMODEL,
		HYBRIDMODEL;
	}

	/**
	 * This enum variable contains all the possible effects in the different stem taper equations.
	 * @author Mathieu Fortin - January 2012
	 */
	protected static enum Effect {
		Intercept(true),
		Drainage(true), 
		VegPot(true), 
		SubDomain(true), 
		SectionRelativeHeight(false), 
		LogSectionRelativeHeight(false), 
		ExpSectionRelativeHeight(false),
		OneMinusSectionRelativeHeight(false),
		DbhOB(true),
		Elevation(true),
		CoreExpression(false),
		LogCoreExpression(false),
		Height(true),
		SectionRelativeHeight_x_CoreExpression(false),
		NumberOfStemsPerHa(true),
		BasalAreaPerHa(true);
		
		private boolean isATreeConstantEffect;
		
		Effect(boolean isATreeConstantEffect) {
			this.isATreeConstantEffect = isATreeConstantEffect;
		}
		
		/**
		 * This method returns true if the effect is constant across the tree or false otherwise. Constant effect
		 * will not be updated in the second linear term for better performances.
		 * @return a boolean
		 */
		protected boolean isATreeConstantEffect() {return isATreeConstantEffect;}
	}
	

	static final Map<StemTaperTreeSpecies, Map<String, Matrix>> SUBDOMAIN_DUMMY_MAP = new HashMap<StemTaperTreeSpecies, Map<String, Matrix>>();
	static {
		Map<String, Matrix> oMap = new HashMap<String, Matrix>();
		Matrix oMat = new Matrix(1,1);
		for (String subdomain : QuebecGeneralSettings.CLIMATIC_SUBDOMAIN_LIST) {
			oMap.put(subdomain, oMat);
		}
		oMat = new Matrix(1,1); 
		oMat.m_afData[0][0] = 1d;
		oMap.remove("1ouest");
		oMap.remove("2ouest");
		
		oMap.put("3ouest", oMat);
		oMap.put("4ouest", oMat);
		oMap.put("5ouest", oMat);
		oMap.put("6ouest", oMat);
		SUBDOMAIN_DUMMY_MAP.put(StemTaperTreeSpecies.BOP, oMap);
		
		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1,1);
		for (String subdomain : QuebecGeneralSettings.CLIMATIC_SUBDOMAIN_LIST) {
			oMap.put(subdomain, oMat);
		}
		oMat = new Matrix(1,1);
		oMat.m_afData[0][0] = 1d;
		oMap.put("2ouest", oMat);
		oMap.put("3ouest", oMat);
		oMap.put("4ouest", oMat);
		oMap.put("5ouest", oMat);
		oMap.put("6ouest", oMat);
		
		oMap.remove("1ouest");
		SUBDOMAIN_DUMMY_MAP.put(StemTaperTreeSpecies.EPB, oMap);
		
		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1,1);
		for (String subdomain : QuebecGeneralSettings.CLIMATIC_SUBDOMAIN_LIST) {
			oMap.put(subdomain, oMat);
		}
		oMat = new Matrix(1,1);
		oMat.m_afData[0][0] = 1d;
		oMap.remove("1ouest");
		oMap.remove("2est");
		oMap.remove("2ouest");
		oMap.remove("3ouest");		
		
		oMap.put("4ouest", oMat);
		oMap.put("5ouest", oMat);
		oMap.put("6ouest", oMat);		
		SUBDOMAIN_DUMMY_MAP.put(StemTaperTreeSpecies.EPN, oMap);

		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1,3);
		for (String subdomain : QuebecGeneralSettings.CLIMATIC_SUBDOMAIN_LIST) {
			oMap.put(subdomain, oMat);
		}
		oMat = new Matrix(1,3);
		oMat.m_afData[0][0] = 1d;
		oMap.put("3ouest", oMat);
		oMat = new Matrix(1,3);
		oMat.m_afData[0][1] = 1d;
		oMap.put("4ouest", oMat);
		oMat = new Matrix(1,3);
		oMat.m_afData[0][2] = 1d;
		oMap.put("2est", oMat);
		oMap.put("3est", oMat);
		oMap.put("4est", oMat);
		
		oMap.remove("5est");
		oMap.remove("5ouest");
		oMap.remove("6est");
		oMap.remove("6ouest");		
		oMap.remove("1ouest");
		SUBDOMAIN_DUMMY_MAP.put(StemTaperTreeSpecies.PEG, oMap);

		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1,1);
		for (String subdomain : QuebecGeneralSettings.CLIMATIC_SUBDOMAIN_LIST) {
			oMap.put(subdomain, oMat);
		}
		oMat = new Matrix(1,1);
		oMat.m_afData[0][0] = 1d;
		oMap.put("4est", oMat);
		oMap.put("5est", oMat);		
		
		oMap.remove("6est");		
		oMap.remove("1ouest");
		SUBDOMAIN_DUMMY_MAP.put(StemTaperTreeSpecies.PET, oMap);

		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1,2);
		for (String subdomain : QuebecGeneralSettings.CLIMATIC_SUBDOMAIN_LIST) {
			oMap.put(subdomain, oMat);
		}
		oMat = new Matrix(1,2);
		oMat.m_afData[0][0] = 1d;
		oMap.put("3est", oMat);
		oMap.put("5est", oMat);
		oMap.put("6est", oMat);
		oMat = new Matrix(1,2);
		oMat.m_afData[0][1] = 1d;
		oMap.put("5ouest", oMat);
		
		oMap.remove("1ouest");
		SUBDOMAIN_DUMMY_MAP.put(StemTaperTreeSpecies.SAB, oMap);

		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1,2);
		for (String subdomain : QuebecGeneralSettings.CLIMATIC_SUBDOMAIN_LIST) {
			oMap.put(subdomain, oMat);
		}
		oMat = new Matrix(1,2);
		oMat.m_afData[0][0] = 1d;
		oMap.put("4est", oMat);
		oMap.put("5est", oMat);
		oMat = new Matrix(1,2);
		oMat.m_afData[0][1] = 1d;
		oMap.put("4ouest", oMat);
		
		oMap.remove("1ouest");
		oMap.remove("2est");
		oMap.remove("2ouest");
		oMap.remove("3est");
		oMap.remove("5ouest");
		oMap.remove("6est");
		oMap.remove("6ouest");
		SUBDOMAIN_DUMMY_MAP.put(StemTaperTreeSpecies.THO, oMap);

		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1, 1);
		for (String subdomain : QuebecGeneralSettings.CLIMATIC_SUBDOMAIN_LIST) {
			oMap.put(subdomain, oMat);
		}
		oMat = new Matrix(1, 1);
		oMat.m_afData[0][0] = 1d;
		oMap.put("6ouest", oMat);
		
		oMap.remove("1ouest");
		oMap.remove("2est");
		oMap.remove("2ouest");
		oMap.remove("3est");
		oMap.remove("3ouest");		
		SUBDOMAIN_DUMMY_MAP.put(StemTaperTreeSpecies.PIG, oMap);
	}
	
	static final Map<StemTaperTreeSpecies, Map<String, Matrix>> POTENTIAL_VEGETATION_GROUP_DUMMY_MAP = new HashMap<StemTaperTreeSpecies, Map<String, Matrix>>();
	static {
		Map<String, Matrix> oMap = new HashMap<String, Matrix>();
		Matrix oMat = new Matrix(1,3);
		oMap.put("MJ1", oMat);
		oMap.put("MS6", oMat);
		oMap.put("RS2", oMat);				
		oMat = new Matrix(1,3);
		oMat.m_afData[0][0] = 1d;
		oMap.put("MJ2", oMat);
		oMat = new Matrix(1,3);
		oMat.m_afData[0][1] = 1d;
		oMap.put("MS1", oMat);
		oMap.put("MS2", oMat);
		oMap.put("RS1", oMat);
		oMat = new Matrix(1,3);
		oMat.m_afData[0][2] = 1d;
		oMap.put("MS4", oMat);
		POTENTIAL_VEGETATION_GROUP_DUMMY_MAP.put(StemTaperTreeSpecies.EPB, oMap);

		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1,2);		
		oMap.put("ME1", oMat);
		oMap.put("MJ2", oMat);
		oMap.put("MS1", oMat);
		oMap.put("MS2", oMat);
		oMap.put("MS6", oMat);
		oMap.put("RE2", oMat);
		oMap.put("RE3", oMat);		
		oMat = new Matrix(1,2);
		oMat.m_afData[0][0] = 1d;
		oMap.put("RE1", oMat);
		oMat = new Matrix(1,2);
		oMat.m_afData[0][1] = 1d;
		oMap.put("RS2", oMat);
		POTENTIAL_VEGETATION_GROUP_DUMMY_MAP.put(StemTaperTreeSpecies.EPN, oMap);

		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1,1);		
		oMap.put("MJ2", oMat);
		oMap.put("RS5", oMat);		
		oMat = new Matrix(1,1);
		oMat.m_afData[0][0] = 1d;
		oMap.put("MS1", oMat);
		POTENTIAL_VEGETATION_GROUP_DUMMY_MAP.put(StemTaperTreeSpecies.EPR, oMap);
		
		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1,2);		
		oMap.put("FE2", oMat);
		oMap.put("FE3", oMat);
		oMap.put("ME1", oMat);		
		oMat = new Matrix(1,2);
		oMat.m_afData[0][0] = 1d;
		oMap.put("MS1", oMat);
		oMap.put("MS2", oMat);
		oMap.put("MJ1", oMat);
		oMap.put("MJ2", oMat);
		oMat = new Matrix(1,2);
		oMat.m_afData[0][1] = 1d;
		oMap.put("MS6", oMat);
		oMap.put("RS2", oMat);
		POTENTIAL_VEGETATION_GROUP_DUMMY_MAP.put(StemTaperTreeSpecies.PET, oMap);

		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1,2);
	
		oMap.put("MJ1", oMat);		
		oMap.put("MJ2", oMat);		
		oMat = new Matrix(1,2);
		oMat.m_afData[0][0] = 1d;
		oMap.put("MS2", oMat);
		oMap.put("MS4", oMat);
		oMap.put("MS1", oMat);
		oMap.put("MS6", oMat);
		oMat = new Matrix(1,2);
		oMat.m_afData[0][1] = 1d;
		oMap.put("RE2", oMat);
		oMap.put("RS1", oMat);
		oMap.put("RS2", oMat);
		oMap.put("RS5", oMat);
		oMap.put("RP1", oMat);
		POTENTIAL_VEGETATION_GROUP_DUMMY_MAP.put(StemTaperTreeSpecies.SAB, oMap);

		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1, 1);	
		oMap.put("MS2", oMat);
		oMap.put("RE2", oMat);
		oMap.put("RS2", oMat);
		oMat = new Matrix(1, 1);
		oMat.m_afData[0][0] = 1d;
		oMap.put("RE1", oMat);
		POTENTIAL_VEGETATION_GROUP_DUMMY_MAP.put(StemTaperTreeSpecies.PIG, oMap);
	}
	
	public static final Map<StemTaperTreeSpecies, Map<String, Matrix>> DRAINAGE_GROUP_DUMMY_MAP = new HashMap<StemTaperTreeSpecies, Map<String, Matrix>>();
	static {
		Map<String, Matrix> oMap = new HashMap<String, Matrix>();
		Matrix oMat = new Matrix(1,1);
		for (String drainageClass : QuebecGeneralSettings.DRAINAGE_CLASS_LIST.keySet()) {
			oMap.put(drainageClass, oMat);
		}		
		oMat = new Matrix(1,1);
		oMat.m_afData[0][0] = 1d;
		oMap.put("4", oMat);
		oMap.put("5", oMat);
		
		oMap.remove("6");
		DRAINAGE_GROUP_DUMMY_MAP.put(StemTaperTreeSpecies.EPN, oMap);

		oMap = new HashMap<String, Matrix>();
		oMat = new Matrix(1,1);
		for (String drainageClass : QuebecGeneralSettings.DRAINAGE_CLASS_LIST.keySet()) {
			oMap.put(drainageClass, oMat);
		}
		oMat = new Matrix(1,1);
		oMat.m_afData[0][0] = 1d;
		oMap.put("3", oMat);
		oMap.put("4", oMat);
		
		oMap.remove("5");
		oMap.remove("6");
		DRAINAGE_GROUP_DUMMY_MAP.put(StemTaperTreeSpecies.SAB, oMap);

	}
	
	protected static int getInterceptLocation(StemTaperTreeSpecies species, ModelType pModelType) {
		return EFFECTS_MAP.get(pModelType).get(species).indexOf(Effect.Intercept);
	}
	
	static Map<ModelType, Map<StemTaperTreeSpecies, List<Effect>>>	EFFECTS_MAP	= new HashMap<ModelType, Map<StemTaperTreeSpecies, List<Effect>>>();
	static {
		Map<StemTaperTreeSpecies, List<Effect>> effectMapHybModel = new HashMap<StemTaperTreeSpecies, List<Effect>>();
		Map<StemTaperTreeSpecies, List<Effect>> effectMapTreeModel = new HashMap<StemTaperTreeSpecies, List<Effect>>();
		EFFECTS_MAP.put(ModelType.HYBRIDMODEL, effectMapHybModel);
		EFFECTS_MAP.put(ModelType.TREEMODEL, effectMapTreeModel);
		List<Effect> effects = new ArrayList<Effect>();
		effects.add(Effect.CoreExpression);
		effects.add(Effect.LogCoreExpression);
		effects.add(Effect.ExpSectionRelativeHeight);
		effects.add(Effect.Height);
		effects.add(Effect.SectionRelativeHeight_x_CoreExpression);
		effects.add(Effect.Intercept);
		effects.add(Effect.SubDomain);
		effectMapHybModel.put(StemTaperTreeSpecies.BOP, effects);
		effects = new ArrayList<Effect>();		
		effects.add(Effect.CoreExpression);
		effects.add(Effect.LogCoreExpression);
		effects.add(Effect.ExpSectionRelativeHeight);
		effects.add(Effect.Height);
		effects.add(Effect.SectionRelativeHeight_x_CoreExpression);
		effects.add(Effect.Intercept);
		effectMapTreeModel.put(StemTaperTreeSpecies.BOP, effects);
		
		effects = new ArrayList<Effect>();
		effects.add(Effect.CoreExpression);
		effects.add(Effect.LogCoreExpression);
		effects.add(Effect.ExpSectionRelativeHeight);
		effects.add(Effect.DbhOB);
		effects.add(Effect.SectionRelativeHeight_x_CoreExpression);
		effects.add(Effect.NumberOfStemsPerHa);
		effects.add(Effect.BasalAreaPerHa);
		effects.add(Effect.Intercept);
		effects.add(Effect.VegPot);
		effects.add(Effect.SubDomain);
		effectMapHybModel.put(StemTaperTreeSpecies.EPB, effects);
		effects = new ArrayList<Effect>();		
		effects.add(Effect.CoreExpression);
		effects.add(Effect.LogCoreExpression);
		effects.add(Effect.ExpSectionRelativeHeight);
		effects.add(Effect.DbhOB);
		effects.add(Effect.SectionRelativeHeight_x_CoreExpression);
		effects.add(Effect.Intercept);
		effectMapTreeModel.put(StemTaperTreeSpecies.EPB, effects);

		effects = new ArrayList<Effect>();
		effects.add(Effect.SectionRelativeHeight);
		effects.add(Effect.LogSectionRelativeHeight);
		effects.add(Effect.DbhOB);
		effects.add(Effect.Height);
		effects.add(Effect.NumberOfStemsPerHa);
		effects.add(Effect.Intercept);
		effects.add(Effect.Drainage);
		effects.add(Effect.VegPot);
		effects.add(Effect.SubDomain);
		effectMapHybModel.put(StemTaperTreeSpecies.EPN, effects);
		effects = new ArrayList<Effect>();		
		effects.add(Effect.SectionRelativeHeight);
		effects.add(Effect.LogSectionRelativeHeight);
		effects.add(Effect.DbhOB);
		effects.add(Effect.Height);
		effects.add(Effect.Intercept);
		effectMapTreeModel.put(StemTaperTreeSpecies.EPN, effects);
		
		effects = new ArrayList<Effect>();
		effects.add(Effect.CoreExpression);
		effects.add(Effect.LogCoreExpression);
		effects.add(Effect.ExpSectionRelativeHeight);
		effects.add(Effect.SectionRelativeHeight_x_CoreExpression);
		effects.add(Effect.BasalAreaPerHa);
		effects.add(Effect.Intercept);
		effects.add(Effect.VegPot);
		effectMapHybModel.put(StemTaperTreeSpecies.EPR, effects);
		effects = new ArrayList<Effect>();		
		effects.add(Effect.CoreExpression);
		effects.add(Effect.LogCoreExpression);
		effects.add(Effect.ExpSectionRelativeHeight);
		effects.add(Effect.SectionRelativeHeight_x_CoreExpression);
		effects.add(Effect.Intercept);
		effectMapTreeModel.put(StemTaperTreeSpecies.EPR, effects);
		
		effects = new ArrayList<Effect>();
		effects.add(Effect.SectionRelativeHeight);
		effects.add(Effect.BasalAreaPerHa);
		effects.add(Effect.Intercept);
		effects.add(Effect.SubDomain);
		effectMapHybModel.put(StemTaperTreeSpecies.PEG, effects);
		effects = new ArrayList<Effect>();		
		effects.add(Effect.SectionRelativeHeight);
		effects.add(Effect.Intercept);
		effectMapTreeModel.put(StemTaperTreeSpecies.PEG, effects);
			
		effects = new ArrayList<Effect>();
		effects.add(Effect.CoreExpression);
		effects.add(Effect.LogCoreExpression);
		effects.add(Effect.ExpSectionRelativeHeight);
		effects.add(Effect.SectionRelativeHeight_x_CoreExpression);
		effects.add(Effect.Intercept);
		effects.add(Effect.VegPot);
		effects.add(Effect.SubDomain);
		effectMapHybModel.put(StemTaperTreeSpecies.PET, effects);
		effects = new ArrayList<Effect>();		
		effects.add(Effect.CoreExpression);
		effects.add(Effect.LogCoreExpression);
		effects.add(Effect.ExpSectionRelativeHeight);
		effects.add(Effect.SectionRelativeHeight_x_CoreExpression);
		effects.add(Effect.Intercept);
		effectMapTreeModel.put(StemTaperTreeSpecies.PET, effects);
		
		effects = new ArrayList<Effect>();
		effects.add(Effect.SectionRelativeHeight);
		effects.add(Effect.LogSectionRelativeHeight);
		effects.add(Effect.DbhOB);
		effects.add(Effect.Elevation);
		effects.add(Effect.Intercept);
		effects.add(Effect.Drainage);
		effects.add(Effect.VegPot);
		effects.add(Effect.SubDomain);
		effectMapHybModel.put(StemTaperTreeSpecies.SAB, effects);
		effects = new ArrayList<Effect>();		
		effects.add(Effect.SectionRelativeHeight);
		effects.add(Effect.LogSectionRelativeHeight);
		effects.add(Effect.DbhOB);
		effects.add(Effect.Height);
		effects.add(Effect.Intercept);
		effectMapTreeModel.put(StemTaperTreeSpecies.SAB, effects);
		
		effects = new ArrayList<Effect>();
		effects.add(Effect.CoreExpression);
		effects.add(Effect.LogCoreExpression);
		effects.add(Effect.ExpSectionRelativeHeight);
		effects.add(Effect.SectionRelativeHeight_x_CoreExpression);
		effects.add(Effect.Intercept);
		effects.add(Effect.SubDomain);
		effectMapHybModel.put(StemTaperTreeSpecies.THO, effects);
		effects = new ArrayList<Effect>();		
		effects.add(Effect.CoreExpression);
		effects.add(Effect.LogCoreExpression);
		effects.add(Effect.ExpSectionRelativeHeight);
		effects.add(Effect.SectionRelativeHeight_x_CoreExpression);
		effects.add(Effect.Intercept);
		effectMapTreeModel.put(StemTaperTreeSpecies.THO, effects);

		effects = new ArrayList<Effect>();
		effects.add(Effect.CoreExpression);
		effects.add(Effect.LogCoreExpression);
		effects.add(Effect.ExpSectionRelativeHeight);
		effects.add(Effect.SectionRelativeHeight_x_CoreExpression);
		effects.add(Effect.DbhOB);
		effects.add(Effect.Height);
		effects.add(Effect.Intercept);
		effects.add(Effect.VegPot);
		effects.add(Effect.SubDomain);
		effectMapHybModel.put(StemTaperTreeSpecies.PIG, effects);
		effects = new ArrayList<Effect>();		
		effects.add(Effect.CoreExpression);
		effects.add(Effect.LogCoreExpression);
		effects.add(Effect.ExpSectionRelativeHeight);
		effects.add(Effect.SectionRelativeHeight_x_CoreExpression);
		effects.add(Effect.DbhOB);
		effects.add(Effect.Height);
		effects.add(Effect.Intercept);
		effectMapTreeModel.put(StemTaperTreeSpecies.PIG, effects);
		
		effects = new ArrayList<Effect>();
		effects.add(Effect.Intercept);
		effects.add(Effect.SectionRelativeHeight);
		effects.add(Effect.OneMinusSectionRelativeHeight);
		effects.add(Effect.CoreExpression);

		effectMapHybModel.put(StemTaperTreeSpecies.PIB, new ArrayList<Effect>());
		effectMapTreeModel.put(StemTaperTreeSpecies.PIB, effects);
	}

	/**
	 * Get the model type equation(hybrid model or tree model)
	 * 
	 * @param tree The stemTaperTree instance
	 * @return the model type equation
	 */
	public static ModelType getModelTypeEquation(StemTaperTree tree) {
//	public static ModelType getModelTypeEquation(StemTaperTree tree, Object... additionalParameters) {
		ModelType modelType = null;
//		if (additionalParameters != null) {
//			for (Object obj : additionalParameters) {
//				if (obj instanceof ModelType) {
//					return (ModelType) obj;
//				}
//			}
//		}
		modelType = ModelType.HYBRIDMODEL;
		StemTaperTreeSpecies species = tree.getStemTaperTreeSpecies();
		StemTaperStand stand = tree.getStand();
		if (EFFECTS_MAP.get(modelType).get(species).contains(Effect.VegPot)) {
			String potentialVegetation = stand.getEcologicalType().substring(0, 3).toUpperCase();
			if (POTENTIAL_VEGETATION_GROUP_DUMMY_MAP.get(species).get(potentialVegetation) == null) {
				return ModelType.TREEMODEL;
			}
		}
		if (EFFECTS_MAP.get(modelType).get(species).contains(Effect.Drainage)) {
			if (DRAINAGE_GROUP_DUMMY_MAP.get(species).get(stand.getDrainageClass()) == null) {
				return ModelType.TREEMODEL;
			}
		}
		if (EFFECTS_MAP.get(modelType).get(species).contains(Effect.SubDomain)) {
			String subDomain = QuebecGeneralSettings.ECO_REGION_MAP.get(stand.getEcoRegion());
			if (SUBDOMAIN_DUMMY_MAP.get(species).get(subDomain) == null) {
				return ModelType.TREEMODEL;
			}
		}
		if (StemTaperTreeSpecies.PIB == species) {
			return ModelType.TREEMODEL;
		}

		return modelType;
	}
}
