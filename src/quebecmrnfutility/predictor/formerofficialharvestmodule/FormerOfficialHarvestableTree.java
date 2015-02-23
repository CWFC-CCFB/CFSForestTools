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

import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmPlus1Provider;
import repicea.simulation.covariateproviders.treelevel.SquaredDbhCmProvider;

/**
 * Trees that can be harvested by the general harvester thinner.
 * @author M. Fortin - May 2010
 */
@Deprecated
public interface FormerOfficialHarvestableTree extends  DbhCmProvider,
														LnDbhCmPlus1Provider,
														SquaredDbhCmProvider {
	
	@Deprecated
	public enum FormerOfficialHarvestableSpecies {
		BOJ,
		BOP,
		EPX,
		ERR,
		ERS,
		F_0,
		F_1,
		HEG,
		PEU,
		PIN,
		RES,
		SAB,
		THO;

		private Matrix dummy;
		
		private FormerOfficialHarvestableSpecies() {
			dummy = new Matrix(1,13);
			dummy.m_afData[0][this.ordinal()] = 1d;
		}
		
		public Matrix getDummy() {return this.dummy;}
	}
	
	
	public FormerOfficialHarvestableSpecies getFormerOfficialHarvestableTreeSpecies();
}
