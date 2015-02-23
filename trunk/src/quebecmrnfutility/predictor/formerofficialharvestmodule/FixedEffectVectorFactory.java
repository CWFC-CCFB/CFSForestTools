/*
 * English version follows
 * 
 * Ce fichier fait partie de la bibliothèque mrnf-foresttools.
 * Il est protégé par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du ministère des Ressources naturelles et de la Faune du Gouvernement du 
 * Québec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Québec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Ministère des Ressources naturelles et de la Faune du Québec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Québec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Québec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Ministère des Ressources naturelles et de la Faune du Québec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.predictor.formerofficialharvestmodule;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import quebecmrnfutility.predictor.formerofficialharvestmodule.FormerOfficialHarvestModel.Treatment;
import quebecmrnfutility.predictor.formerofficialharvestmodule.FormerOfficialHarvestableTree.FormerOfficialHarvestableSpecies;
import repicea.math.Matrix;

/**
 * This private class handles all the effects and generates X vector required by the AutomatedHarvestedModel.
 * @author M. Fortin - August 2010
 */
@SuppressWarnings("deprecation")
class FixedEffectVectorFactory implements Serializable {
	
	private static final long serialVersionUID = 20101018L;

	private enum Effect{
		Intercept,
		Species,
		SpeciesXDbh,
		SpeciesXDbhXDummyDbh,
		SpeciesXLogDbh,
		SpeciesXLogDbhXDummyDbh,
		DbhXDummyDbh,
		LogNbHa,
		LogStHa,
		DbhXDbh
	}
	
	private Map<Treatment, ArrayList<Effect>> effects;
	private Matrix xVector = new Matrix(1,200);
	
	protected FixedEffectVectorFactory() {
		init();
	}
	
	private void init() {
		effects = new HashMap<Treatment, ArrayList<Effect>>();
		ArrayList<Effect> effectList = null;
		for (Treatment treatment : Treatment.values()) {
			switch (treatment) {
			case CA:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.SpeciesXDbh);
				effectList.add(Effect.SpeciesXDbhXDummyDbh);
				effectList.add(Effect.SpeciesXLogDbh);
				effectList.add(Effect.SpeciesXLogDbhXDummyDbh);
				effectList.add(Effect.LogNbHa);
				break;
			case CE:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXDbh);
				effectList.add(Effect.SpeciesXDbhXDummyDbh);
				effectList.add(Effect.SpeciesXLogDbh);
				effectList.add(Effect.SpeciesXLogDbhXDummyDbh);
				effectList.add(Effect.LogNbHa);
				effectList.add(Effect.LogStHa);
				break;
			case CJ:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXDbh);
				effectList.add(Effect.SpeciesXDbhXDummyDbh);
				effectList.add(Effect.SpeciesXLogDbh);
				effectList.add(Effect.SpeciesXLogDbhXDummyDbh);
				effectList.add(Effect.LogNbHa);
				effectList.add(Effect.LogStHa);
				break;
			case CP:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXDbh);
				effectList.add(Effect.SpeciesXDbhXDummyDbh);
				effectList.add(Effect.SpeciesXLogDbh);
				effectList.add(Effect.SpeciesXLogDbhXDummyDbh);
				effectList.add(Effect.LogNbHa);
				break;
			case EC:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXDbh);
				effectList.add(Effect.SpeciesXDbhXDummyDbh);
				effectList.add(Effect.SpeciesXLogDbh);
				effectList.add(Effect.SpeciesXLogDbhXDummyDbh);
				effectList.add(Effect.LogStHa);
				break;
			case ES:
				effectList = new ArrayList<Effect>();
				effectList.add(Effect.Intercept);
				effectList.add(Effect.Species);
				effectList.add(Effect.SpeciesXDbh);
				effectList.add(Effect.DbhXDbh);
				effectList.add(Effect.DbhXDummyDbh);
				break;
			}
			effects.put(treatment, effectList);
		}
	}
	
	/**
	 * This method returns the xVector corresponding to a particular combination of tree, stand and treatment.
	 */
	protected Matrix getFixedEffectVector(FormerOfficialHarvestableStand stand, 
			FormerOfficialHarvestableTree tree,
			Treatment treatment) {
	
		xVector.resetMatrix();
		int pointer = 0;
		ArrayList<Effect> effectsToBeSet = effects.get(treatment);
		
		for (Effect effect : effectsToBeSet) {
			pointer = setEffect(tree, stand, effect, pointer);
		}
	
		return xVector.getSubMatrix(0, 0, 0, pointer - 1);
	}
	
	
	/**
	 * This private method sets a particular effect into the XVector and returns the new position 
	 * of the vector.
	 */
	private int setEffect(FormerOfficialHarvestableTree tree,
			FormerOfficialHarvestableStand stand, 
			Effect effect, 
			int pointer) {
		
		int dummyDbh;
		int output = 0;
		
		FormerOfficialHarvestableSpecies species = tree.getFormerOfficialHarvestableTreeSpecies();
		double dbh = tree.getDbhCm();
		double dbh2 = tree.getSquaredDbhCm();
		double lnDbhPlus1 = tree.getLnDbhCmPlus1();
		
		double basalAreaHa = stand.getBasalAreaM2Ha();
		double numberOfStemsHa = stand.getNumberOfStemsHa();
		
		switch (effect) {
		
		case Intercept:
			xVector.m_afData[0][pointer] = 1d;
			output = 1;
			break;
	
		case Species:
			xVector.setSubMatrix(species.getDummy(), 0, pointer);
			output = species.getDummy().m_iCols;
			break;

		case SpeciesXDbh:
			xVector.setSubMatrix(species.getDummy().scalarMultiply(dbh), 0, pointer);
			output = species.getDummy().m_iCols;
			break;

		case SpeciesXDbhXDummyDbh:
			dummyDbh = (dbh <= 18d) ? 1 : 0;
			if (dummyDbh !=0) {
				xVector.setSubMatrix(species.getDummy().scalarMultiply(dbh), 0, pointer);
			}
			output = species.getDummy().m_iCols;
			break;

		case SpeciesXLogDbh:
			xVector.setSubMatrix(species.getDummy().scalarMultiply(lnDbhPlus1), 0, pointer);
			output = species.getDummy().m_iCols;
			break;

		case SpeciesXLogDbhXDummyDbh:
			dummyDbh = (dbh <= 18d) ? 1 : 0;
			if (dummyDbh !=0) {
				xVector.setSubMatrix(species.getDummy().scalarMultiply(lnDbhPlus1), 0, pointer);
			}
			output = species.getDummy().m_iCols;
			break;
			
		case DbhXDummyDbh:
			dummyDbh = (dbh <= 18d) ? 1 : 0;
			if (dummyDbh !=0) {
				xVector.m_afData[0][pointer] = dbh;
			}
			output = 1;
			break;

		case LogNbHa:
			double logNbHa = Math.log(numberOfStemsHa + 1);
			xVector.m_afData[0][pointer] = logNbHa;
			output = 1;
			break;

		case LogStHa:
			double logStHa = Math.log(basalAreaHa + 1);
			xVector.m_afData[0][pointer] = logStHa;
			output = 1;
			break;

		case DbhXDbh:
			xVector.m_afData[0][pointer] = dbh2;
			output = 1;
			break;
		
		default:
			throw new InvalidParameterException("Unknown effect : " + effect.name());
		}
		
		return pointer += output;
	}
	
	
	
	
}
