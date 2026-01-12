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

import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import repicea.gui.UIControlManager;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class MerisTreeLoggerParametersDialog extends TreeLoggerParametersDialog<MerisTreeLogCategory> {

	private static enum MessageID implements TextableEnum {
		ImportFromCSVFile("Import", "Importer");
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override 
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
		
	}
	
	final JMenuItem importButton; 
	
	protected MerisTreeLoggerParametersDialog(Window window, MerisTreeLoggerParameters params) {
		super(window, params);
		logGradePriorityChangeEnabled = false; 
		importButton = UIControlManager.createCommonMenuItem(MessageID.ImportFromCSVFile);
		mnFile.add(new JSeparator());
		mnFile.add(importButton); // TODO MF2025122 Add actionlistener to this button
	}

	@Override
	protected void initUI() {
		super.initUI();
		mnFile.setEnabled(true);
		mnEdit.setEnabled(true);
		mnSpecies.setEnabled(false);			//	the species cannot be changed in SybilleTreeLogger
		mnLogGrade.setEnabled(false);			// the log grade cannot be changed either
		mnTools.setEnabled(false);
		logGradeGoDown.setEnabled(false);		// the log grade cannot be changed either
		logGradeGoUp.setEnabled(false);
	}

	@Override
	protected void settingsAction() {}

}
