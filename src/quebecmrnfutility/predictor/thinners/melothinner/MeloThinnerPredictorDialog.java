/*
 * This file is part of the mrnf-foresttools library.

 * Author - Mathieu Fortin for Canadian Forest Service
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
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
package quebecmrnfutility.predictor.thinners.melothinner;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public final class MeloThinnerPredictorDialog extends REpiceaDialog implements ItemListener, NumberFieldListener, WindowListener {

	private enum MessageID implements TextableEnum {
		Title("Melo et al.'s thinner options", "Options du module de r\u00E9colte de Melo et al."),
		DefaultAAC("Use annual allowance cut volume based on past observations", "Utiliser une possibilit\u00E9 foresti\u00E8re bas\u00E9e sur les observations pass\u00E9es"),
		UserSpecifiedAAC("Use user specified annual allowance cut volume:", "Sp\u00E9cifier une possibilit\u00E9 foresti\u00E8re :"),
		Units("m3/ha/yr", "m3/ha/ann\u00E9e");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
		
	}
	
	
	private final MeloThinnerPredictor caller;

	private final JRadioButton defaultAACButton;
	private final JRadioButton userSpecifiedAACButton;
	private final JFormattedNumericField aacField;
	
	protected MeloThinnerPredictorDialog(MeloThinnerPredictor caller, Window parent) {
		super(parent);
		this.setCancelOnClose(false);
		this.caller = caller;
		defaultAACButton = new JRadioButton(MessageID.DefaultAAC.toString());
		userSpecifiedAACButton = new JRadioButton(MessageID.UserSpecifiedAAC.toString());
		ButtonGroup bg = new ButtonGroup();
		bg.add(defaultAACButton);
		bg.add(userSpecifiedAACButton);
		aacField = NumberFormatFieldFactory.createNumberFormatField(15, NumberFormatFieldFactory.Type.Double, 
				NumberFormatFieldFactory.Range.Positive,
				false);
		if (caller.fixedAAC != null) {
			userSpecifiedAACButton.setSelected(true);
			aacField.setText(caller.fixedAAC.toString());
		} else {
			defaultAACButton.setSelected(true);
			aacField.setText("0.0");
		}
		aacField.setEnabled(userSpecifiedAACButton.isSelected());
		initUI();
		pack();
	}
		
	@Override
	public void listenTo() {
		defaultAACButton.addItemListener(this);
		userSpecifiedAACButton.addItemListener(this);
		aacField.addNumberFieldListener(this);
		addWindowListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		defaultAACButton.removeItemListener(this);
		userSpecifiedAACButton.removeItemListener(this);
		aacField.removeNumberFieldListener(this);
		removeWindowListener(this);
	}

	@Override
	protected void initUI() {
		setTitle(MessageID.Title.toString());
		getContentPane().setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(createPanelForRadioButton(defaultAACButton, 5));
		JPanel addUserSpecifiedAACPanel = createPanelForRadioButton(userSpecifiedAACButton, 5);
		addUserSpecifiedAACPanel.add(Box.createHorizontalStrut(5));
		addUserSpecifiedAACPanel.add(aacField);
		addUserSpecifiedAACPanel.add(Box.createHorizontalStrut(5));
		addUserSpecifiedAACPanel.add(UIControlManager.getLabel(MessageID.Units));
		addUserSpecifiedAACPanel.add(Box.createHorizontalStrut(5));
		
		mainPanel.add(addUserSpecifiedAACPanel);
		getContentPane().add(mainPanel);
	}

	
	private JPanel createPanelForRadioButton(JRadioButton button, int margin) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (margin > 0) {
			panel.add(Box.createHorizontalStrut(margin));
		}
		panel.add(button);
		return panel;
	}
	
	@Override
	public void numberChanged(NumberFieldEvent e) {}

	@Override
	public void itemStateChanged(ItemEvent e) {
		aacField.setEnabled(userSpecifiedAACButton.isSelected());
	}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		if (userSpecifiedAACButton.isSelected()) {
			caller.setFixedAAC((Double) aacField.getValue());
		} else {
			caller.setFixedAAC(null);
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}


	public static void main(String[] args) {
		MeloThinnerPredictor pred = new MeloThinnerPredictor(true);
		pred.showUI(null);
		pred.showUI(null);
		System.exit(0);
	}

}
