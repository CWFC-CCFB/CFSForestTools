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

import repicea.simulation.treelogger.TreeLogCategoryPanel;
import repicea.util.REpiceaTranslator;

@SuppressWarnings("serial")
public class PetroTreeLogCategoryPanel extends TreeLogCategoryPanel<PetroTreeLogCategory> {

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
