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

import java.awt.Window;
import java.awt.event.ActionListener;

import repicea.gui.UIControlManager;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;

@SuppressWarnings("serial")
public class PetroTreeLoggerParametersDialog extends TreeLoggerParametersDialog<PetroTreeLogCategory> implements ActionListener {

	static {
		UIControlManager.setTitle(PetroTreeLoggerParametersDialog.class, "Petro Tree Logger", "Module de billonnage Petro");
	}
	
	protected PetroTreeLoggerParametersDialog(Window parent, PetroTreeLoggerParameters params) {
		super(parent, params);
		logGradePriorityChangeEnabled = false; 
	}

	@Override
	protected void initUI() {
		super.initUI();
		mnFile.setEnabled(false);
		mnEdit.setEnabled(false);
		mnSpecies.setEnabled(false);			//	the species cannot be changed in SybilleTreeLogger
		mnLogGrade.setEnabled(false);			// the log grade cannot be changed either
		mnTools.setEnabled(false);
		logGradeGoDown.setEnabled(false);		// the log grade cannot be changed either
		logGradeGoUp.setEnabled(false);
	}


	@Override
	protected void settingsAction() {}		// not needed



}
