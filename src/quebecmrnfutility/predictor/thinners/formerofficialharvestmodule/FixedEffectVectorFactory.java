/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package quebecmrnfutility.predictor.thinners.formerofficialharvestmodule;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import quebecmrnfutility.predictor.thinners.formerofficialharvestmodule.FormerOfficialHarvestModel.Treatment;
import quebecmrnfutility.predictor.thinners.formerofficialharvestmodule.FormerOfficialHarvestableTree.FormerOfficialHarvestableSpecies;
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
			xVector.setValueAt(0, pointer, 1d);
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
				xVector.setValueAt(0, pointer, dbh);
			}
			output = 1;
			break;

		case LogNbHa:
			double logNbHa = Math.log(numberOfStemsHa + 1);
			xVector.setValueAt(0, pointer, logNbHa);
			output = 1;
			break;

		case LogStHa:
			double logStHa = Math.log(basalAreaHa + 1);
			xVector.setValueAt(0, pointer, logStHa);
			output = 1;
			break;

		case DbhXDbh:
			xVector.setValueAt(0, pointer, dbh2);
			output = 1;
			break;
		
		default:
			throw new InvalidParameterException("Unknown effect : " + effect.name());
		}
		
		return pointer += output;
	}
	
	
	
	
}
