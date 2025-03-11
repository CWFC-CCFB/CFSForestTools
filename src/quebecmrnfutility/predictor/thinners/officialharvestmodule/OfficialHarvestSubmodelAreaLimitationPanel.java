/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2019-2021 Her Majesty the Queen in right of Canada
 * Author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service.
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import repicea.gui.REpiceaPanel;
import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.gui.components.NumberFormatFieldFactory.Type;
import repicea.gui.components.REpiceaCellEditor;
import repicea.gui.components.REpiceaTable;
import repicea.gui.components.REpiceaTableModel;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class OfficialHarvestSubmodelAreaLimitationPanel extends REpiceaPanel implements TableModelListener {

	private static enum MessageID implements TextableEnum {
		Treatment("Treatment", "Traitement"),
		MaxAnnualAreaProp("Annual area (ha)", "Surface annuelle (ha)"),
		Title("Maximum annual areas by treatment", "Surfaces annuelles maximales par traitement"),
		Footnote("Values equal to 0 mean no limit", "Les valeurs \u00E9gales \u00E0 0 signifient qu'il n'y a pas de maximum.");

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
	
	private final OfficialHarvestSubmodelAreaLimitation caller;
	
	private REpiceaTable table;
	private REpiceaTableModel tableModel;
	private boolean updateCaller;
	
	OfficialHarvestSubmodelAreaLimitationPanel(OfficialHarvestSubmodelAreaLimitation caller) {
		this.caller = caller;
		
		tableModel = new REpiceaTableModel(new Object[] {MessageID.Treatment.toString(), MessageID.MaxAnnualAreaProp.toString()});
		tableModel.setEditableVetos(0, true);
		table = new REpiceaTable(tableModel, false); // false : adding or deleting rows is disabled
		table.putClientProperty("terminateEditOnFocusLost", true);
		JFormattedNumericField field = NumberFormatFieldFactory.createNumberFormatField(Type.Double, Range.Positive, true);
		table.setDefaultEditor(Double.class, new REpiceaCellEditor(field, tableModel));
		createUI();
	}

	
	private void createUI() {
		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(UIControlManager.getLabel(MessageID.Title));
		JPanel panel2 = new JPanel();
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
		panel2.add(panel);
		panel2.add(Box.createVerticalStrut(5));

		JPanel panel3 = new JPanel();
		panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
		panel3.add(new JScrollPane(table));
		panel3.add(UIControlManager.getLabel(MessageID.Footnote));
		panel3.add(Box.createVerticalStrut(5));

		add(panel2, BorderLayout.NORTH);
		add(panel3, BorderLayout.CENTER);
	}
	
	@Override
	public void refreshInterface() {
		updateCaller = false;
		doNotListenToAnymore();
		tableModel.removeAll();
		for (Enum<?> treatment : caller.areaLimitationMap.keySet()) {
			tableModel.addRow(new Object[]{treatment, caller.areaLimitationMap.get(treatment)});
		}
		listenTo();
		updateCaller = true;
	}

	@Override
	public void listenTo() {
		tableModel.addTableModelListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		tableModel.removeTableModelListener(this);
	}


	@Override
	public void tableChanged(TableModelEvent e) {
		if (updateCaller) {
			TreatmentType trt = (TreatmentType) tableModel.getValueAt(e.getFirstRow(), 0);
			double value = Double.parseDouble(tableModel.getValueAt(e.getFirstRow(), e.getColumn()).toString());
			caller.areaLimitationMap.put(trt, value);
		}
	}

	
}
