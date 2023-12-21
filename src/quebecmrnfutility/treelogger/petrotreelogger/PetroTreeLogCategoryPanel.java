/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec
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
package quebecmrnfutility.treelogger.petrotreelogger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import repicea.simulation.treelogger.LogCategoryPanel;
import repicea.util.REpiceaTranslator;

@SuppressWarnings("serial")
public class PetroTreeLogCategoryPanel extends LogCategoryPanel<PetroTreeLogCategory> {

	protected PetroTreeLogCategoryPanel(PetroTreeLogCategory logCategory) {
		super(logCategory);
		nameTextField.setText(logCategory.getName());
		nameTextField.setEditable(false);
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
		
		JLabel nameLabel = new JLabel(REpiceaTranslator.getString(MessageID.LogGradeName));
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
		featurePanel1.add(labelPanel1, BorderLayout.CENTER);
		
		JLabel featureLabel1 = new JLabel(REpiceaTranslator.getString(MessageID.SmallEndDiameter));
		featureLabel1.setFont(new Font("Arial", Font.PLAIN, 12));
		labelPanel1.add(featureLabel1);
		
		JPanel textFieldPanel1 = new JPanel();
		FlowLayout fl_textFieldPanel1 = (FlowLayout) textFieldPanel1.getLayout();
		fl_textFieldPanel1.setAlignment(FlowLayout.RIGHT);
		featurePanel1.add(textFieldPanel1, BorderLayout.EAST);
		
		JPanel featurePanel2 = new JPanel();
		panel.add(featurePanel2);
		featurePanel2.setLayout(new BorderLayout(0, 0));
		
		JPanel labelPanel2 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) labelPanel2.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		featurePanel2.add(labelPanel2, BorderLayout.CENTER);
		
		JPanel featurePanel3 = new JPanel();
		panel.add(featurePanel3);
		featurePanel3.setLayout(new BorderLayout(0, 0));
		
		JPanel labelPanel3 = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) labelPanel3.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEFT);
		featurePanel3.add(labelPanel3, BorderLayout.CENTER);
		
		JLabel featureLabel3 = new JLabel(REpiceaTranslator.getString(MessageID.LongLength));
		featureLabel3.setFont(new Font("Arial", Font.PLAIN, 12));
		labelPanel3.add(featureLabel3);
		
		JPanel textFieldPanel3 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) textFieldPanel3.getLayout();
		flowLayout_3.setAlignment(FlowLayout.RIGHT);
		featurePanel3.add(textFieldPanel3, BorderLayout.EAST);
		
	}

	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.gui.Refreshable#refreshInterface()
	 */
	@Override
	public void refreshInterface() {}


	
}
