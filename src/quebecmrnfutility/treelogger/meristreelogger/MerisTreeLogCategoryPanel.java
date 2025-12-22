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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import repicea.simulation.treelogger.LogCategoryPanel;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class MerisTreeLogCategoryPanel extends LogCategoryPanel<MerisTreeLogCategory> {

	private enum MessageID implements TextableEnum {
		SpeciesList("List of species in this group", "Liste des esp\u00E8ces dans ce groupe");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
	}
	
	private final JList<String> speciesList;
	
	protected MerisTreeLogCategoryPanel(MerisTreeLogCategory logCategory) {
		super(logCategory);
		nameTextField.setText(logCategory.getName());
		nameTextField.setEditable(false);
		speciesList = new JList<String>(logCategory.speciesListInThisGroup.toArray(new String[] {}));
		createUI();
	}

	private void createUI() {
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setLayout(new BorderLayout(0, 0));
		
		JPanel logCategoryNamePanel = new JPanel();
		add(logCategoryNamePanel, BorderLayout.NORTH);
		FlowLayout flowLayout = (FlowLayout) logCategoryNamePanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		logCategoryNamePanel.add(horizontalStrut);
		
		JLabel nameLabel = new JLabel(REpiceaTranslator.getString(LogCategoryPanel.MessageID.LogGradeName));
		nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
		nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
		logCategoryNamePanel.add(nameLabel);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		logCategoryNamePanel.add(horizontalStrut_1);
		
		nameTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		logCategoryNamePanel.add(nameTextField);
		nameTextField.setColumns(15);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JPanel featurePanel1 = new JPanel();
		panel.add(featurePanel1);
		featurePanel1.setLayout(new BorderLayout(0, 0));
		
		JPanel labelPanel1 = new JPanel();
		FlowLayout fl_labelPanel1 = (FlowLayout) labelPanel1.getLayout();
		fl_labelPanel1.setAlignment(FlowLayout.LEFT);
		featurePanel1.add(labelPanel1, BorderLayout.WEST);
		
		JLabel featureLabel1 = new JLabel(REpiceaTranslator.getString(MessageID.SpeciesList));
		featureLabel1.setFont(new Font("Arial", Font.PLAIN, 12));
		labelPanel1.add(featureLabel1);
		
		JPanel textFieldPanel1 = new JPanel();
		FlowLayout fl_textFieldPanel1 = (FlowLayout) textFieldPanel1.getLayout();
		fl_textFieldPanel1.setAlignment(FlowLayout.RIGHT);
		featurePanel1.add(textFieldPanel1, BorderLayout.CENTER);

		featurePanel1.add(Box.createHorizontalStrut(6), BorderLayout.EAST);

		textFieldPanel1.add(speciesList);
		
	}

}
