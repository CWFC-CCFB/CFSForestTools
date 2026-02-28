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
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import quebecmrnfutility.treelogger.meristreelogger.MerisTreeLoggerParameters.MerisTypeMatrix;
import repicea.app.SettingMemory;
import repicea.gui.CommonGuiUtility;
import repicea.gui.CommonGuiUtility.FileChooserOutput;
import repicea.gui.REpiceaAWTProperty;
import repicea.gui.UIControlManager;
import repicea.io.REpiceaFileFilter;
import repicea.io.REpiceaFileFilterList;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class MerisTreeLoggerParametersDialog extends TreeLoggerParametersDialog<MerisTreeLogCategory> {

	private static enum MessageID implements TextableEnum {
		ImportFromCSVFile("Import", "Importer"),
		MissingSpecies("The matrix is incomplete. These species are missing: ", "La matrice est incompl\u00E8te. Les esp\u00E8ces suivantes sont manquantes : "),
		SureToContinue("Are you sure you want to continue?", "Etes-vous s\u00FBr de vouloir continuer?");

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
		mnFile.add(importButton); 
		importButton.setEnabled(getTreeLoggerParameters().getGUIPermission().isEnablingGranted());
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
	public void listenTo() {
		super.listenTo();
		importButton.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		importButton.removeActionListener(this);
	}

	@Override
	protected void settingsAction() {}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(importButton)) {
			importAction();
		} else {
			super.actionPerformed(arg0);
		}
	}

	private void importAction() {
		try {
			SettingMemory settings = getWindowSettings();
			String filename;
			if (settings != null) {
				filename = settings.getProperty(getClass().getSimpleName() + ".last.file.loaded", "");
			} else {
				filename = "";
			}
			REpiceaFileFilterList fileFilters = new REpiceaFileFilterList(REpiceaFileFilter.CSV);

			FileChooserOutput fileChooserOutput = CommonGuiUtility.browseAction(this,
					JFileChooser.FILES_ONLY, 
					filename,
					fileFilters,
					JFileChooser.OPEN_DIALOG);		// false : not restricted

			if (fileChooserOutput.isValid()) {
				MerisTypeMatrix matrixToBeImported = ((MerisTreeLoggerParameters) getTreeLoggerParameters()).readFromFile(fileChooserOutput.getFilename());
				List<String> missingSpeciesCodes = matrixToBeImported.getMissingSpeciesCodes();
				if (!missingSpeciesCodes.isEmpty()) {
					int response = JOptionPane.showConfirmDialog(this, 
							MessageID.MissingSpecies.toString() + System.lineSeparator() +
							missingSpeciesCodes.toString() + System.lineSeparator() +
							MessageID.SureToContinue.toString(), 
							UIControlManager.InformationMessageTitle.Warning.toString(), 
							JOptionPane.OK_CANCEL_OPTION, 
							JOptionPane.WARNING_MESSAGE);
					if (response != 0) {
						return;
					}
				}
				((MerisTreeLoggerParameters) getTreeLoggerParameters()).currentMatrix.replaceBy(matrixToBeImported);
				postLoadingAction();
				firePropertyChange(REpiceaAWTProperty.JustLoaded, null, this);
				if (settings != null) {
					settings.setProperty(getClass().getSimpleName() + ".last.file.loaded", fileChooserOutput.getFilename());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					REpiceaTranslator.getString(UIControlManager.InformationMessage.ErrorWhileLoadingData),
					REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Error),
					JOptionPane.ERROR_MESSAGE);
		}
	}

}
