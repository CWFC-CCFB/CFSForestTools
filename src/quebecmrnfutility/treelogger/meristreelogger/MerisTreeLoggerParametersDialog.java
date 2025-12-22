/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2025 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
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
package quebecmrnfutility.treelogger.meristreelogger;

import java.awt.Window;

import repicea.simulation.treelogger.TreeLoggerParametersDialog;

@SuppressWarnings("serial")
public class MerisTreeLoggerParametersDialog extends TreeLoggerParametersDialog<MerisTreeLogCategory> {

	protected MerisTreeLoggerParametersDialog(Window window, MerisTreeLoggerParameters params) {
		super(window, params);
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
	protected void settingsAction() {}

}
