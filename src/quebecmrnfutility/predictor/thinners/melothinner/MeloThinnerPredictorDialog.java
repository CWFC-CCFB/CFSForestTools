/*
 * This file is part of the CFSForettools library.

 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
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
package quebecmrnfutility.predictor.thinners.melothinner;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import repicea.gui.REpiceaControlPanel;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public final class MeloThinnerPredictorDialog extends REpiceaDialog implements ItemListener, NumberFieldListener {

	private enum MessageID implements TextableEnum {
		Title("Melo et al.'s thinner options", "Options du module de r\u00E9colte de Melo et al."),
		DefaultAAC("Use annual allowance cut (AAC) volume based on past observations", "Utiliser une possibilit\u00E9 foresti\u00E8re bas\u00E9e sur les observations pass\u00E9es"),
		UserSpecifiedAAC("Use user specified annual allowance cut (AAC) volume:", "Sp\u00E9cifier une possibilit\u00E9 foresti\u00E8re :"),
		Units("m3/ha/yr", "m3/ha/ann\u00E9e"),
		UserSpecifiedAACToolTip("<html>The AAC per hectare must be calculated as the total <br> "
				+ "AAC divided by the area designated for wood production.</html>", 
				"<html>La possibilit\u00E9 foresti\u00E8re par hectare est la possibilit\u00E9 totale <br>"
				+ "divis\u00E9e par la surface d\u00E9di\u00E9e \u00E0 la production ligneuse. </html>"),
		;

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
	
	private boolean isCancelled;
	
	protected MeloThinnerPredictorDialog(MeloThinnerPredictor caller, Window parent) {
		super(parent);
		setCancelOnClose(true);
		this.caller = caller;
		defaultAACButton = new JRadioButton(MessageID.DefaultAAC.toString());
		userSpecifiedAACButton = new JRadioButton(MessageID.UserSpecifiedAAC.toString());
		userSpecifiedAACButton.setToolTipText(MessageID.UserSpecifiedAACToolTip.toString());
		ButtonGroup bg = new ButtonGroup();
		bg.add(defaultAACButton);
		bg.add(userSpecifiedAACButton);
		aacField = NumberFormatFieldFactory.createNumberFormatField(15, NumberFormatFieldFactory.Type.Double, 
				NumberFormatFieldFactory.Range.Positive,
				false);
		if (caller.targetAACPerHa != null) {
			userSpecifiedAACButton.setSelected(true);
			aacField.setText(caller.targetAACPerHa.toString());
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
	}

	@Override
	public void doNotListenToAnymore() {
		defaultAACButton.removeItemListener(this);
		userSpecifiedAACButton.removeItemListener(this);
		aacField.removeNumberFieldListener(this);
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
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(new REpiceaControlPanel(this), BorderLayout.SOUTH);
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
	public void cancelAction() {
		super.cancelAction();
		this.isCancelled = true;
	}

	/**
	 * This method returns true if the window has been cancelled.
	 * @return a boolean
	 */
	public boolean hasBeenCancelled() {return isCancelled;}
	
	@Override
	public void setVisible(boolean bool) {
		if (!isVisible() && bool) {
			isCancelled = false;
		}
		super.setVisible(bool);
	}


	@Override
	public void okAction() {
		if (userSpecifiedAACButton.isSelected()) {
			caller.setTargetAACPerHa((Double) aacField.getValue());
		} else {
			caller.setTargetAACPerHa(null);
		}
		super.okAction();
	}

	public static void main(String[] args) {
		REpiceaTranslator.setCurrentLanguage(Language.French);
		MeloThinnerPredictor pred = new MeloThinnerPredictor(true);
		pred.showUI(null);
		System.out.println("Dialog has been cancelled: " + pred.getUI(null).hasBeenCancelled());
		pred.showUI(null);
		System.out.println("Dialog has been cancelled: " + pred.getUI(null).hasBeenCancelled());
		System.exit(0);
	}

}
