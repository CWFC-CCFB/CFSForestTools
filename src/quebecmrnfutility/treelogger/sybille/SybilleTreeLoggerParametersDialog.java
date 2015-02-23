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
package quebecmrnfutility.treelogger.sybille;

import java.awt.Window;

import repicea.gui.UIControlManager;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;

@SuppressWarnings("serial")
public class SybilleTreeLoggerParametersDialog extends TreeLoggerParametersDialog<SybilleTreeLogCategory> {
		
	static {
		UIControlManager.setTitle(SybilleTreeLoggerParametersDialog.class, "Sybille Tree Logger", "Module de billonnage Sybille");
	}
	
	protected SybilleTreeLoggerParametersDialog(Window owner, TreeLoggerParameters<SybilleTreeLogCategory> params) {
		super(owner, params);
	}

	
	@Override
	protected String getTreeLoggerName() {
		return "Sybille Tree Logger";
	}

	@Override
	protected void initUI() {
		super.initUI();
		mnSpecies.setEnabled(false);			//	the species cannot be changed in SybilleTreeLogger
	}

	@Override
	protected void settingsAction() {
		SybilleTreeLoggerOptionDialog dlg = new SybilleTreeLoggerOptionDialog(this);
		dlg.setVisible(true);
	}

	@Override
	protected SybilleTreeLoggerParameters getTreeLoggerParameters() {
		return (SybilleTreeLoggerParameters) super.getTreeLoggerParameters();
	}

}
