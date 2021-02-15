package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import java.awt.Window;
import java.awt.event.ActionEvent;
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

@SuppressWarnings("serial")
public class OfficialHarvestSubmodelSelectorDialog extends REpiceaMatchSelectorDialog implements ItemListener {
	
	static {
		UIControlManager.setTitle(OfficialHarvestSubmodelSelectorDialog.class, "Treatments to be applied in each potential vegetation", "Traitement \u00E0 appliquer dans chaque v\u00E9g\u00E9tation potentielle");
	}


	private JRadioButton singleTreatmentButton;
	private JRadioButton treatmentByPotentialVegetationButton;
	private JComboBox<OfficialHarvestTreatmentDefinition> uniqueTreatmentComboBox;
	
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

	@Override
	protected JPanel getMainPanel() {
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
		} else {
			super.actionPerformed(e);
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
