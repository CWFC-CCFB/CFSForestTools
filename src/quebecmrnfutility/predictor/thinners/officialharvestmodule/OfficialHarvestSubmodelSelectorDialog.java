/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2017 Gouvernement du Quebec - Rouge-Epicea
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
package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;

import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestSubmodelSelector.Mode;
import repicea.gui.UIControlManager;
import repicea.gui.components.REpiceaMatchSelectorDialog;

@SuppressWarnings({ "serial", "rawtypes" })
public class OfficialHarvestSubmodelSelectorDialog extends REpiceaMatchSelectorDialog implements ItemListener, ActionListener {
	
	static {
		UIControlManager.setTitle(OfficialHarvestSubmodelSelectorDialog.class, "Treatments to be applied in each potential vegetation", "Traitement \u00E0 appliquer dans chaque v\u00E9g\u00E9tation potentielle");
	}


	private JRadioButton singleTreatmentButton;
	private JRadioButton treatmentByPotentialVegetationButton;
	private JComboBox<OfficialHarvestTreatmentDefinition> uniqueTreatmentComboBox;
	private OfficialHarvestSubmodelAreaLimitationPanel areaLimitationsPanel;
	
	@SuppressWarnings("unchecked")
	protected OfficialHarvestSubmodelSelectorDialog(OfficialHarvestSubmodelSelector caller, Window parent, Object[] columnNames) {
		super(caller, parent, columnNames);
	}

	@Override
	protected void init() {
		super.init();
		singleTreatmentButton = new JRadioButton(Mode.SingleTreatment.toString());
		uniqueTreatmentComboBox = new JComboBox<OfficialHarvestTreatmentDefinition>(getCaller().getPotentialMatches().toArray(new OfficialHarvestTreatmentDefinition[]{}));
		treatmentByPotentialVegetationButton = new JRadioButton(Mode.TreatmentByPotentialVegetation.toString());
		ButtonGroup bg = new ButtonGroup();
		bg.add(singleTreatmentButton);
		bg.add(treatmentByPotentialVegetationButton);
	}
	
	
	
	@Override
	public void refreshInterface() {
		singleTreatmentButton.setSelected(getCaller().getMode() == Mode.SingleTreatment);
		treatmentByPotentialVegetationButton.setSelected(getCaller().getMode() == Mode.TreatmentByPotentialVegetation);
		uniqueTreatmentComboBox.setSelectedItem(getCaller().singleTreatment);
		if (areaLimitationsPanel != null)
			areaLimitationsPanel.refreshInterface();
		super.refreshInterface();
		checkFeaturesToEnable();
	}

	@Override
	protected OfficialHarvestSubmodelSelector getCaller() {
		return (OfficialHarvestSubmodelSelector) super.getCaller();
	}
	
	protected void checkFeaturesToEnable() {
		uniqueTreatmentComboBox.setEnabled(singleTreatmentButton.isSelected());
		if (!treatmentByPotentialVegetationButton.isSelected()) {
			TableCellEditor editor = getTable().getCellEditor();
			if (editor != null) {
				editor.cancelCellEditing();
			}
		}
		getTable().setEnabled(treatmentByPotentialVegetationButton.isSelected());
	}

	@Override
	public void listenTo() {
		super.listenTo();
		singleTreatmentButton.addActionListener(this);
		treatmentByPotentialVegetationButton.addActionListener(this);
		uniqueTreatmentComboBox.addItemListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		singleTreatmentButton.removeActionListener(this);
		treatmentByPotentialVegetationButton.removeActionListener(this);
		uniqueTreatmentComboBox.removeItemListener(this);
	}

	private JPanel getLeftPanel() {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		pane.add(Box.createVerticalStrut(10));
		pane.add(createSimplePanel(singleTreatmentButton, 5));
		pane.add(Box.createVerticalStrut(10));
		pane.add(this.createSimplePanel(uniqueTreatmentComboBox, 20));
		pane.add(Box.createVerticalStrut(20));
		pane.add(createSimplePanel(treatmentByPotentialVegetationButton, 5));
		pane.add(Box.createVerticalStrut(10));
		JScrollPane scrollPane = new JScrollPane(getTable());
		pane.add(createSimplePanel(scrollPane, 20));
		pane.add(Box.createVerticalStrut(10));
		return pane;
	}


	private JPanel getRightPanel() {
		areaLimitationsPanel = getCaller().getAreaLimitations().getUI(this);
		return areaLimitationsPanel;
	}

	@Override
	protected JPanel getMainPanel() {
		JPanel pane = new JPanel();
		pane.setLayout(new FlowLayout(FlowLayout.CENTER));
		pane.add(getLeftPanel());
		pane.add(getRightPanel());
		return pane;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(singleTreatmentButton)) {
			if (singleTreatmentButton.isSelected()) {
				getCaller().mode = Mode.SingleTreatment;
				checkFeaturesToEnable();
			}
		} else if (e.getSource().equals(treatmentByPotentialVegetationButton)) {
			if (treatmentByPotentialVegetationButton.isSelected()) {
				getCaller().mode = Mode.TreatmentByPotentialVegetation;
				checkFeaturesToEnable();
			}
		}
	}

	
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource().equals(uniqueTreatmentComboBox)) {
			getCaller().singleTreatment = (OfficialHarvestTreatmentDefinition) uniqueTreatmentComboBox.getSelectedItem();
			System.out.println("Treatment selected = " + uniqueTreatmentComboBox.getSelectedItem());
		}
	}

}
