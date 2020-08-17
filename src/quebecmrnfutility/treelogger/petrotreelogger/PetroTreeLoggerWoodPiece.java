/*
 * English version follows
 * 
 * Ce fichier fait partie de la bibliotheque mrnf-foresttools.
 * Il est protege par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du ministere des Ressources naturelles et de la Faune du Gouvernement du 
 * Qu�bec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Ministere des Ressources naturelles et de la Faune du Quebec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Quebec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Ministere des Ressources naturelles et de la Faune du Quebec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.treelogger.petrotreelogger;

import repicea.simulation.treelogger.WoodPiece;

/**
 * The PetroTreeLoggerWoodPiece class represents a log of particular quality.
 * It is always considered over bark.
 * @author Mathieu Fortin - August 2020
 *
 */
public class PetroTreeLoggerWoodPiece extends WoodPiece {

	private static final long serialVersionUID = 1L;

	protected PetroTreeLoggerWoodPiece(PetroTreeLogCategory logCategory, PetroLoggableTree tree, double volumeM3) {
		super(logCategory, tree, false, volumeM3); // false: underbark
	}


}
