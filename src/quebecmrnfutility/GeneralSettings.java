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
package quebecmrnfutility;

import repicea.io.tools.LevelProviderEnum;


/**
 * This class contains the common codes for species and environment that apply in the 
 * Province of Quebec, Canada.
 * @author Mathieu Fortin - January 2012
 */
public class GeneralSettings {


	public enum Level {
		stratumLevel,
		plotLevel, 
		treeLevel 
	}

	public enum FieldID implements LevelProviderEnum {
		STRATUM(Level.stratumLevel),
		
		PLOT(Level.plotLevel),
		PLOT_AREA(Level.plotLevel),
		
		LATITUDE(Level.plotLevel),
		LONGITUDE(Level.plotLevel),
		ALTITUDE(Level.plotLevel),
		
		ECOREGION(Level.plotLevel),
		TYPEECO(Level.plotLevel),
		DRAINAGE_CLASS(Level.plotLevel),
		
		ORIGIN(Level.plotLevel),
		DISTURBANCE(Level.plotLevel),
		
		PLOTWEIGHT(Level.plotLevel),
		
		PRECTOT(Level.plotLevel),
		MEANTEMP(Level.plotLevel),
		DEGJR(Level.plotLevel),
		PRECUTIL(Level.plotLevel),
		PRECSAIS(Level.plotLevel),
		JRXGEL(Level.plotLevel),
		JRXGELC(Level.plotLevel),
		JRCROIS(Level.plotLevel),
		DPV(Level.plotLevel),
		ARIDITE(Level.plotLevel),
		NEIGEP(Level.plotLevel),
		NEIGET(Level.plotLevel),

		SPECIES(Level.treeLevel),
		TREESTATUS(Level.treeLevel),
		TREEFREQ(Level.treeLevel),
		TREEDHPCM(Level.treeLevel),
		TREEHEIGHT(Level.treeLevel),
		TREEVOLUME(Level.treeLevel),
		TREEQUALITY(Level.treeLevel),
		
		AGE3M(Level.plotLevel),
		AGE4M(Level.plotLevel),
		AGE7M(Level.plotLevel),
		AGE12M(Level.plotLevel),
		AGEHD(Level.plotLevel),
		DOMINANT_HEIGHT(Level.plotLevel);
		
		private Level level;
		
		FieldID(Level level) {
			this.level = level;
		}
		
		@Override
		public Level getFieldLevel() {
			return level;
		}
	}

}
