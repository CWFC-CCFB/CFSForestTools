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
package quebecmrnfutility.predictor.thinners.betaharvestmodule;

import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmPlus1Provider;

/**
 * Trees that can be harvested by the general harvester thinner.
 * @author Mathieu Fortin - May 2010
 */
@Deprecated
public interface BetaHarvestableTree extends DbhCmProvider,
											LnDbhCmPlus1Provider {
	
	@Deprecated
	public enum BetaHarvestableSpecies {
		BOJ,
		BOP,
		EPX,
		ERR,
		ERS,
		F_0,
		HEG,
		PEU,
		PIN,
		RES,
		SAB,
		THO;
		
		private Matrix dummy;
		
		BetaHarvestableSpecies() {
			dummy = new Matrix(1,12);
			dummy.setValueAt(0, ordinal(), 1d);
		}
		
		public Matrix getDummy() {return this.dummy;}
	}


	@Deprecated
	public BetaHarvestableSpecies getBetaHarvestableTreeSpecies();
}
