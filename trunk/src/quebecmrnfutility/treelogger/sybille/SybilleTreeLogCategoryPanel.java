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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import quebecmrnfutility.treelogger.sybille.SybilleTreeLogCategory.LengthID;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.gui.components.NumberFormatFieldFactory.Type;
import repicea.simulation.treelogger.LogCategoryPanel;


/**	
 * The SybilleTreeLogCategoryPanel is a panel which allows the user to enter the specification of 
 * a particular log grade, such as the small-end diameter and the length.
 *	@author Mathieu Fortin - January 2012
 */
@SuppressWarnings("serial")
public class SybilleTreeLogCategoryPanel extends LogCategoryPanel<SybilleTreeLogCategory> implements ActionListener, NumberFieldListener {
	
	private JFormattedNumericField smallEndDiameterCmTextField;;
	private JFormattedNumericField logLengthMTextField;
	private JRadioButton fourFeetLongButton;
	private JRadioButton eightFeetLongButton;
	private JRadioButton twelveFeetLongButton;
	private JRadioButton noLimitLongButton;
	
	/**	
	 * Default constructor.
	 */
	protected SybilleTreeLogCategoryPanel(SybilleTreeLogCategory logCategory) {
		super(logCategory);
		smallEndDiameterCmTextField = NumberFormatFieldFactory.createNumberFormatField(Type.Double, Range.Positive, false);	// null not allowed
		logLengthMTextField = NumberFormatFieldFactory.createNumberFormatField(Type.Double, Range.Positive, true);	// null allowed
		fourFeetLongButton = new JRadioButton(LengthID.FourFeetLong.toString());
		eightFeetLongButton = new JRadioButton(LengthID.EightFeetLong.toString());
		twelveFeetLongButton = new JRadioButton(LengthID.TwelveFeetLong.toString());
		noLimitLongButton = new JRadioButton(LengthID.NoLimit.toString());
		refreshInterface();
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
		
		JLabel nameLabel = new JLabel(LogCategoryPanel.MessageID.LogGradeName.toString());
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
		
		JLabel featureLabel1 = new JLabel(LogCategoryPanel.MessageID.SmallEndDiameter.toString());
		featureLabel1.setFont(new Font("Arial", Font.PLAIN, 12));
		labelPanel1.add(featureLabel1);
		
		JPanel textFieldPanel1 = new JPanel();
		FlowLayout fl_textFieldPanel1 = (FlowLayout) textFieldPanel1.getLayout();
		fl_textFieldPanel1.setAlignment(FlowLayout.RIGHT);
		featurePanel1.add(textFieldPanel1, BorderLayout.EAST);
		
		smallEndDiameterCmTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldPanel1.add(smallEndDiameterCmTextField);
		smallEndDiameterCmTextField.setColumns(10);
		
		JPanel featurePanel2 = new JPanel();
		panel.add(featurePanel2);
		featurePanel2.setLayout(new BorderLayout(0, 0));
		
		JPanel labelPanel2 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) labelPanel2.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		featurePanel2.add(labelPanel2, BorderLayout.CENTER);
		
//		JLabel featureLabel2 = new JLabel("Diam\u00E8tre au gros bout (cm)");
//		featureLabel2.setFont(new Font("Arial", Font.PLAIN, 12));
//		labelPanel2.add(featureLabel2);
		
//		JPanel textFieldPanel2 = new JPanel();
//		FlowLayout flowLayout_1 = (FlowLayout) textFieldPanel2.getLayout();
//		flowLayout_1.setAlignment(FlowLayout.RIGHT);
//		featurePanel2.add(textFieldPanel2, BorderLayout.EAST);
		
//		largeEndDiameterCmTextField.setHorizontalAlignment(SwingConstants.RIGHT);
//		largeEndDiameterCmTextField.setColumns(10);
//		textFieldPanel2.add(largeEndDiameterCmTextField);
		
		JPanel featurePanel3 = new JPanel();
		panel.add(featurePanel3);
		featurePanel3.setLayout(new BorderLayout(0, 0));
		
		JPanel labelPanel3 = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) labelPanel3.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEFT);
		featurePanel3.add(labelPanel3, BorderLayout.CENTER);
		
		JLabel featureLabel3 = new JLabel(LogCategoryPanel.MessageID.LongLength.toString());
		featureLabel3.setFont(new Font("Arial", Font.PLAIN, 12));
		labelPanel3.add(featureLabel3);
		
		JPanel textFieldPanel3 = new JPanel();
		textFieldPanel3.setLayout(new BoxLayout(textFieldPanel3, BoxLayout.Y_AXIS));
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(fourFeetLongButton);
		buttonGroup.add(eightFeetLongButton);
		buttonGroup.add(twelveFeetLongButton);
		buttonGroup.add(noLimitLongButton);
		
		textFieldPanel3.add(fourFeetLongButton);
		textFieldPanel3.add(eightFeetLongButton);
		textFieldPanel3.add(twelveFeetLongButton);
		textFieldPanel3.add(noLimitLongButton);
		
		featurePanel3.add(textFieldPanel3, BorderLayout.EAST);
		
	}



	@Override
	public void listenTo() {
		super.listenTo();
		smallEndDiameterCmTextField.addNumberFieldListener(this);
		logLengthMTextField.addNumberFieldListener(this);
		fourFeetLongButton.addActionListener(this);
		eightFeetLongButton.addActionListener(this);
		twelveFeetLongButton.addActionListener(this);
		noLimitLongButton.addActionListener(this);
	}


	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		smallEndDiameterCmTextField.removeNumberFieldListener(this);
		logLengthMTextField.removeNumberFieldListener(this);
		fourFeetLongButton.removeActionListener(this);
		eightFeetLongButton.removeActionListener(this);
		twelveFeetLongButton.removeActionListener(this);
		noLimitLongButton.removeActionListener(this);
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(fourFeetLongButton)) {
			getTreeLogCategory().setLogLengthM(LengthID.FourFeetLong);
		} else if (arg0.getSource().equals(eightFeetLongButton)) {
			getTreeLogCategory().setLogLengthM(LengthID.EightFeetLong);
		} else if (arg0.getSource().equals(twelveFeetLongButton)) {
			getTreeLogCategory().setLogLengthM(LengthID.TwelveFeetLong);
		} else if (arg0.getSource().equals(noLimitLongButton)) {
			getTreeLogCategory().setLogLengthM(LengthID.NoLimit);
		}
		
	}

	@Override
	public void refreshInterface() {
		super.refreshInterface();
		smallEndDiameterCmTextField.setText(((Double) getTreeLogCategory().getSmallEndDiameterCm()).toString());
		LengthID lengthID = LengthID.findClosestLengthID(getTreeLogCategory().getLogLengthM());
		switch(lengthID) {
		case FourFeetLong:
			fourFeetLongButton.setSelected(true);
			break;
		case EightFeetLong:
			eightFeetLongButton.setSelected(true);
			break;
		case TwelveFeetLong:
			twelveFeetLongButton.setSelected(true);
			break;
		case NoLimit:
			noLimitLongButton.setSelected(true);
			break;
		}
	}


	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource().equals(smallEndDiameterCmTextField)) {
			double diameterCm = smallEndDiameterCmTextField.getValue().doubleValue();
			getTreeLogCategory().setSmallEndDiameterCm(diameterCm);
		} 
	}

}

