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
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;

import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestSubmodelSelector.Mode;
import repicea.gui.UIControlManager;
import repicea.gui.components.REpiceaEnhancedMatchSelectorDialog;
import repicea.simulation.covariateproviders.plotlevel.LandUseProvider.LandUse;

@SuppressWarnings({ "serial", "rawtypes" })
public class OfficialHarvestSubmodelSelectorDialog extends REpiceaEnhancedMatchSelectorDialog<OfficialHarvestTreatmentDefinition>
													implements ItemListener, ActionListener {
	
	static {
		UIControlManager.setTitle(OfficialHarvestSubmodelSelectorDialog.class, "Treatments to be applied in each potential vegetation", "Traitement \u00E0 appliquer dans chaque v\u00E9g\u00E9tation potentielle");
	}


	private Map<Enum<?>, JRadioButton> singleTreatmentButtons;
	private Map<Enum<?>, JRadioButton> treatmentByPotentialVegetationButtons;
	private Map<Enum<?>, JComboBox<OfficialHarvestTreatmentDefinition>> singleTreatmentComboBoxes;
	
	private OfficialHarvestSubmodelAreaLimitationPanel areaLimitationsPanel;
	
	protected OfficialHarvestSubmodelSelectorDialog(OfficialHarvestSubmodelSelector caller, Window parent, Object[] columnNames) {
		super(caller, parent, columnNames);
	}

	@Override
	protected void init() {
		super.init();
		singleTreatmentButtons = new HashMap<Enum<?>, JRadioButton>();
		treatmentByPotentialVegetationButtons = new HashMap<Enum<?>, JRadioButton>();
		for (Enum<?> landUse : getCaller().modes.keySet()) {
			JRadioButton singleTreatmentButton = new JRadioButton(Mode.SingleTreatment.toString());
			singleTreatmentButton.setName("singleTreatmentButton_"+ landUse.name());
			singleTreatmentButtons.put(landUse, singleTreatmentButton);
			
			JRadioButton treatmentByPotentialVegetationButton = new JRadioButton(Mode.TreatmentByPotentialVegetation.toString());
			treatmentByPotentialVegetationButton.setName("treatmentByPotentialVegetationButton_" + landUse.name());
			treatmentByPotentialVegetationButtons.put(landUse, treatmentByPotentialVegetationButton);

			ButtonGroup bg = new ButtonGroup();
			bg.add(singleTreatmentButton);
			bg.add(treatmentByPotentialVegetationButton);
		}

		singleTreatmentComboBoxes = new HashMap<Enum<?>, JComboBox<OfficialHarvestTreatmentDefinition>>();
		for (Enum<?> landUse : getCaller().modes.keySet()) {
			JComboBox<OfficialHarvestTreatmentDefinition> singleTreatmentComboBox = new JComboBox<OfficialHarvestTreatmentDefinition>(getCaller().getPotentialMatches(landUse).toArray(new OfficialHarvestTreatmentDefinition[]{}));
			singleTreatmentComboBox.setName("singleTreatmentComboBox_" + landUse.name());
			singleTreatmentComboBoxes.put(landUse, singleTreatmentComboBox);
		}		
	}
	
	
	
	@Override
	public void refreshInterface() {
		for (Enum<?> landUse : getCaller().modes.keySet()) {
			singleTreatmentButtons.get(landUse).setSelected(getCaller().getMode(landUse) == Mode.SingleTreatment);
			treatmentByPotentialVegetationButtons.get(landUse).setSelected(getCaller().getMode(landUse) == Mode.TreatmentByPotentialVegetation);
			singleTreatmentComboBoxes.get(landUse).setSelectedItem(getCaller().getSingleTreatment(landUse));
		}
		if (areaLimitationsPanel != null) {
			areaLimitationsPanel.refreshInterface();
		}
		super.refreshInterface();
		checkFeaturesToEnable();
	}

	@Override
	protected OfficialHarvestSubmodelSelector getCaller() {
		return (OfficialHarvestSubmodelSelector) super.getCaller();
	}
	
	protected void checkFeaturesToEnable() {
		for (Enum<?> landUse : getCaller().modes.keySet()) {
			singleTreatmentComboBoxes.get(landUse).setEnabled(singleTreatmentButtons.get(landUse).isSelected());
			if (!treatmentByPotentialVegetationButtons.get(landUse).isSelected()) {
				TableCellEditor editor = getTable(landUse).getCellEditor();
				if (editor != null) {
					editor.cancelCellEditing();
				}
			}
			getTable(landUse).setEnabled(treatmentByPotentialVegetationButtons.get(landUse).isSelected());
		}
	}

	@Override
	public void listenTo() {
		super.listenTo();
		for (Enum<?> landUse : getCaller().modes.keySet()) {
			singleTreatmentButtons.get(landUse).addActionListener(this);
			treatmentByPotentialVegetationButtons.get(landUse).addActionListener(this);
			singleTreatmentComboBoxes.get(landUse).addItemListener(this);
		}
	}

	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		for (Enum<?> landUse : getCaller().modes.keySet()) {
			singleTreatmentButtons.get(landUse).removeActionListener(this);
			treatmentByPotentialVegetationButtons.get(landUse).removeActionListener(this);
			singleTreatmentComboBoxes.get(landUse).removeItemListener(this);
		}
	}

	private JPanel getLeftPanel() {
		return super.getMainPanel();
	}

	@Override
	protected JComponent getPanelToBeEmbeddedInTab(Enum<?> landUse) {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		pane.add(Box.createVerticalStrut(10));
		pane.add(createSimplePanel(singleTreatmentButtons.get(landUse), 5));
		pane.add(Box.createVerticalStrut(10));
		pane.add(this.createSimplePanel(singleTreatmentComboBoxes.get(landUse), 20));
		pane.add(Box.createVerticalStrut(20));
		pane.add(createSimplePanel(treatmentByPotentialVegetationButtons.get(landUse), 5));
		pane.add(Box.createVerticalStrut(10));
		JScrollPane scrollPane = new JScrollPane(getTable(landUse));
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

	private LandUse getLandUseFromName(String name) {
		int index = name.indexOf("_");
		if (index == -1) {
			return null;
		} else {
			return LandUse.valueOf(name.substring(index + 1));
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JRadioButton && ((JRadioButton) e.getSource()).isSelected()) {
			String name = ((JRadioButton) e.getSource()).getName();
			LandUse landUse;
			if (name.startsWith("singleTreatmentButton")) {
				landUse = getLandUseFromName(name);
				if (landUse != null) {
					getCaller().modes.put(landUse, Mode.SingleTreatment);
					System.out.println("Single treatment for land use: " + landUse.name());
					checkFeaturesToEnable();
				}
			} else if (name.startsWith("treatmentByPotentialVegetationButton")) {
				landUse = getLandUseFromName(name);
				if (landUse != null) {
					getCaller().modes.put(landUse, Mode.TreatmentByPotentialVegetation);
					System.out.println("Treatment by potential vegetation for land use: " + landUse.name());
					checkFeaturesToEnable();
				}
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() instanceof JComboBox) {
			String name = ((JComboBox) e.getSource()).getName();
			if (name.startsWith("singleTreatmentComboBox")) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					LandUse landUse = getLandUseFromName(name);
					if (landUse != null) {
						OfficialHarvestTreatmentDefinition treatment = (OfficialHarvestTreatmentDefinition) ((JComboBox) e.getSource()).getSelectedItem();
						getCaller().singleTreatments.put(landUse, treatment);
						System.out.println("Treatment selected = " + treatment + " for land use " + landUse.name());
					}
				}
			}
		}
	}

}
