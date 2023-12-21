/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec  Rouge-Epicea
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
