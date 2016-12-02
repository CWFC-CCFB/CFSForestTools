/*
 * English version follows
 * 
 * Ce fichier fait partie de la biblioth�que mrnf-foresttools.
 * Il est prot�g� par la loi sur le droit d'auteur (L.R.C.,cC-42) et par les
 * conventions internationales. Toute reproduction de ce fichier sans l'accord 
 * du minist�re des Ressources naturelles et de la Faune du Gouvernement du 
 * Qu�bec est strictement interdite.
 * 
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec - Rouge-Epicea
 * 	Pour information, contactez Jean-Pierre Saucier, 
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 *
 * This file is part of the mrnf-foresttools library. It is 
 * protected by copyright law (L.R.C., cC-42) and by international agreements. 
 * Any reproduction of this file without the agreement of Qu�bec Ministry of 
 * Natural Resources and Wildlife is strictly prohibited.
 *
 * Copyright (C) 2009-2012 Gouvernement du Qu�bec 
 * 	For further information, please contact Jean-Pierre Saucier,
 * 			Minist�re des Ressources naturelles et de la Faune du Qu�bec
 * 			jean-pierre.saucier@mrnf.gouv.qc.ca
 */
package quebecmrnfutility.treelogger.sybille;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictor.EstimationMethodInDeterministicMode;
import repicea.gui.OwnedWindow;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.serial.REpiceaMemorizerHandler;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class SybilleTreeLoggerOptionDialog extends REpiceaDialog 
				implements 	ItemListener, 
							ActionListener, 
							Memorizable, 
							NumberFieldListener,
							OwnedWindow {

	static {
		UIControlManager.setTitle(SybilleTreeLoggerOptionDialog.class, 
				"Sybille Tree Logger Options", 
				"Options du module de billonnage Sybille");
	}

	
	public static enum MessageID implements TextableEnum {
		StumpHeight("Stump height (m)", "Hauteur de souche (m)"),
		EstimationMethod("Estimation method", "M\u00E9thode d'estimation"),
		FirstOrderTaylorSeries("First-order Taylor expansion", "S\u00E9rie de Taylor de premier degr\u00E9"),
		SecondOrderTaylorExpansion("Second-order Taylor expansion (default method)", "S\u00E9rie de Taylor de deuxi\u00E8me degr\u00E9 (m\u00E9thode par d\u00E9faut)"),
		OptimizeIntegration("Optimize integration (recommended)", "Optimiser l'int\u00E9gration (recommend\u00E9)");
		
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

	private SybilleTreeLoggerParameters parameters;
	
	private JRadioButton rdbtnFirstOrder;
	private JRadioButton rdbtnSecondOrder;
	private JFormattedNumericField stumpHeightFld;
	private JButton ok;
	private JButton cancel;
	private JCheckBox optimizeCheckBox;
	
	
	protected SybilleTreeLoggerOptionDialog(SybilleTreeLoggerParametersDialog parent) {
		super(parent);
		String title = UIControlManager.getTitle(this.getClass());
		setTitle(title);
		new REpiceaMemorizerHandler(this);
		this.parameters = parent.getTreeLoggerParameters();
		ok = UIControlManager.createCommonButton(CommonControlID.Ok);
		cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
		rdbtnFirstOrder = new JRadioButton(MessageID.FirstOrderTaylorSeries.toString());
		rdbtnSecondOrder = new JRadioButton(MessageID.SecondOrderTaylorExpansion.toString());
		stumpHeightFld = NumberFormatFieldFactory.createNumberFormatField(NumberFormatFieldFactory.Type.Double, 
				Range.Positive, 
				false);		// null not allowed
		optimizeCheckBox = new JCheckBox();
		initUI();
		Dimension dim = new Dimension(400,300);
		setMinimumSize(dim);
		pack();
	}
	
	@Override
	protected void initUI() {
		ButtonGroup buttonGroup = new ButtonGroup();

		JPanel controlPanel = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) controlPanel.getLayout();
		flowLayout_1.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(controlPanel, BorderLayout.SOUTH);
		controlPanel.add(ok);
		controlPanel.add(cancel);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		Component verticalStrut_2 = Box.createVerticalStrut(20);
		panel.add(verticalStrut_2);
		
		JPanel panel_1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(panel_1);
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2);
		panel_2.setBorder(null);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel = new JLabel(MessageID.EstimationMethod.toString());
		panel_2.add(lblNewLabel);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		Component verticalStrut = Box.createVerticalStrut(10);
		panel_2.add(verticalStrut);
		panel_2.add(rdbtnFirstOrder);
		buttonGroup.add(rdbtnFirstOrder);
		
		panel_2.add(rdbtnSecondOrder);
		buttonGroup.add(rdbtnSecondOrder);
		
		
		panel.add(Box.createVerticalStrut(20));
		
		JPanel panel_6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(panel_6);
		
		JPanel panel_7 = new JPanel();
		panel_6.add(panel_7);
		panel_7.setBorder(null);
		panel_7.setLayout(new FlowLayout(FlowLayout.LEADING));
		JLabel lblOptimize = new JLabel(MessageID.OptimizeIntegration.toString());
		panel_7.add(lblOptimize);
		lblOptimize.setFont(new Font("Tahoma", Font.BOLD, 11));
		panel_7.add(Box.createHorizontalStrut(10));
		panel_7.add(optimizeCheckBox);
		
		
		Component verticalStrut_3 = Box.createVerticalStrut(20);
		panel.add(verticalStrut_3);
		
		JPanel panel_5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(panel_5);
		
		JPanel panel_3 = new JPanel(new FlowLayout(FlowLayout.LEADING));
		panel_5.add(panel_3);
		panel_3.setBorder(null);
		
		JLabel lblNewLabel2 = new JLabel(MessageID.StumpHeight.toString());
		panel_3.add(lblNewLabel2);
		lblNewLabel2.setFont(new Font("Tahoma", Font.BOLD, 11));
		Component verticalStrut_1 = Box.createHorizontalStrut(10);
		panel_3.add(verticalStrut_1);
		
		JPanel panel_4 = new JPanel();
		panel_3.add(panel_4);
		
		stumpHeightFld.setColumns(10);
		panel_4.add(stumpHeightFld);
		panel.add(Box.createVerticalStrut(20));
		synchronizeUIWithOwner();
	}

	
	@Override
	public void doNotListenToAnymore() {
		stumpHeightFld.removeNumberFieldListener(this);
		rdbtnFirstOrder.removeItemListener(this);
		rdbtnSecondOrder.removeItemListener(this);
		ok.removeActionListener(this);
		cancel.removeActionListener(this);
		optimizeCheckBox.removeItemListener(this);
	}

	@Override
	public void listenTo() {
		stumpHeightFld.addNumberFieldListener(this);
		rdbtnFirstOrder.addItemListener(this);
		rdbtnSecondOrder.addItemListener(this);
		ok.addActionListener(this);
		cancel.addActionListener(this);
		optimizeCheckBox.addItemListener(this);
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getSource().equals(rdbtnFirstOrder) || arg0.getSource().equals(rdbtnSecondOrder)) {
			if (rdbtnFirstOrder.isSelected()) {
				parameters.setEstimationMethod(EstimationMethodInDeterministicMode.FirstOrderMeanOnly);
			} 
			if (rdbtnSecondOrder.isSelected()) {
				parameters.setEstimationMethod(EstimationMethodInDeterministicMode.SecondOrderMeanOnly);
			}
		} else  if (arg0.getSource().equals(optimizeCheckBox)) {
			parameters.setIntegrationOptimizationEnabled(optimizeCheckBox.isSelected());
		}
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(ok)) {
			okAction();
		} else if (arg0.getSource().equals(cancel)) {
			cancelAction();
		}
	}


	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage memPackage = new MemorizerPackage();
		memPackage.add(parameters.getEstimationMethod());
		memPackage.add(parameters.getStumpHeightM());
		memPackage.add(parameters.isIntegrationOptimizationEnabled());
		return memPackage;
	}

	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		parameters.setEstimationMethod((EstimationMethodInDeterministicMode) wasMemorized.get(0));
		parameters.setStumpHeightM((Double) wasMemorized.get(1));
		parameters.setIntegrationOptimizationEnabled((Boolean) wasMemorized.get(2));
	}


	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource().equals(stumpHeightFld)) {
			double stumpHeightM = Double.parseDouble(stumpHeightFld.getText());
			parameters.setStumpHeightM(stumpHeightM);
		} 
	}

	@Override
	public void synchronizeUIWithOwner() {
		stumpHeightFld.setText(((Double) parameters.getStumpHeightM()).toString());
		
		if (parameters.getEstimationMethod() == EstimationMethodInDeterministicMode.SecondOrderMeanOnly) {
			rdbtnSecondOrder.setSelected(true);
		} else {
			rdbtnFirstOrder.setSelected(true);
		}

		rdbtnSecondOrder.setEnabled(!parameters.isVariabilityEnabled());
		rdbtnFirstOrder.setEnabled(!parameters.isVariabilityEnabled());
		
		optimizeCheckBox.setSelected(parameters.isIntegrationOptimizationEnabled());
	}

	@Override
	public Memorizable getWindowOwner() {return this;}


}
