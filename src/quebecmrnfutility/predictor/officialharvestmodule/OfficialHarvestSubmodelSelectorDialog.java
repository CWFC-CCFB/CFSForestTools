package quebecmrnfutility.predictor.officialharvestmodule;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;

import quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestSubmodelSelector.Mode;
import repicea.gui.OwnedWindow;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.gui.WindowSettings;
import repicea.gui.components.REpiceaCellEditor;
import repicea.gui.components.REpiceaTable;
import repicea.gui.components.REpiceaTableModel;
import repicea.io.IOUserInterface;
import repicea.io.REpiceaIOFileHandlerUI;
import repicea.serial.Memorizable;
import repicea.util.REpiceaSystem;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class OfficialHarvestSubmodelSelectorDialog extends REpiceaDialog implements IOUserInterface, OwnedWindow, ActionListener, ItemListener {
	
	static {
		UIControlManager.setTitle(OfficialHarvestSubmodelSelectorDialog.class, "Treatments to be applied in each potential vegetation", "Traitement \u00E0 appliquer dans chaque v\u00E9g\u00E9tation potentielle");
	}

	private static enum ColumnID implements TextableEnum {
		PotentialVegetation("Potential vegetation", "V\u00E9g\u00E9tation potentielle"),
		SilviculturalTreatment("Treatment", "Traitement");

		ColumnID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}


	private final OfficialHarvestSubmodelSelector caller;
	private final REpiceaTable table;
	private final REpiceaTableModel tableModel;
	private final JMenuItem load;
	private final JMenuItem save;
	private final JMenuItem saveAs;
	private final JRadioButton singleTreatmentButton;
	private final JRadioButton treatmentByPotentialVegetationButton;
	private final WindowSettings windowSettings;
	private final JComboBox<TextableEnum> uniqueTreatmentComboBox;
	private final JButton okButton;
	private final JButton cancelButton;
	private boolean isCancelled;
	
	protected OfficialHarvestSubmodelSelectorDialog(OfficialHarvestSubmodelSelector caller, Window parent) {
		super(parent);
		windowSettings = new WindowSettings(REpiceaSystem.getJavaIOTmpDir() + getClass().getSimpleName()+ ".ser", this);
		this.caller = caller;
		load = UIControlManager.createCommonMenuItem(CommonControlID.Open);
		save = UIControlManager.createCommonMenuItem(CommonControlID.Save);
		saveAs = UIControlManager.createCommonMenuItem(CommonControlID.SaveAs);

		okButton = UIControlManager.createCommonButton(CommonControlID.Ok);
		cancelButton = UIControlManager.createCommonButton(CommonControlID.Cancel);
		
		new REpiceaIOFileHandlerUI(this, caller, save, saveAs, load);
		
		tableModel = new REpiceaTableModel(ColumnID.values());
		tableModel.setEditableVetos(0, true);
		table = new REpiceaTable(tableModel, false); // false : adding or deleting rows is disabled
		TextableEnum[] possibleTreatments =  caller.potentialTreatments.toArray(new TextableEnum[]{});
		table.setDefaultEditor(Enum.class, new REpiceaCellEditor(new JComboBox<TextableEnum>(possibleTreatments), tableModel));
		table.setRowSelectionAllowed(false);

		singleTreatmentButton = new JRadioButton(Mode.SingleTreatment.toString());
		uniqueTreatmentComboBox = new JComboBox<TextableEnum>(possibleTreatments);
		treatmentByPotentialVegetationButton = new JRadioButton(Mode.TreatmentByPotentialVegetation.toString());
		ButtonGroup bg = new ButtonGroup();
		bg.add(singleTreatmentButton);
		bg.add(treatmentByPotentialVegetationButton);
		
		refreshInterface();
		initUI();
		pack();

	}
	
	protected JPanel getControlPanel() {
		JPanel pane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pane.add(okButton);
		pane.add(cancelButton);
		return pane;
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
	public void refreshInterface() {
		singleTreatmentButton.setSelected(caller.getMode() == Mode.SingleTreatment);
		treatmentByPotentialVegetationButton.setSelected(caller.getMode() == Mode.TreatmentByPotentialVegetation);
		uniqueTreatmentComboBox.setSelectedItem(caller.singleTreatment);
		tableModel.removeAll();
		Object[] record;
		for (String potentialVegetation : caller.treatmentMatchMap.keySet()) {
			record = new Object[2];
			record[0] = potentialVegetation;
			record[1] = caller.treatmentMatchMap.get(potentialVegetation);
			tableModel.addRow(record);
		}
		checkFeaturesToEnable();
		super.refreshInterface();
	}
	
	protected void checkFeaturesToEnable() {
		uniqueTreatmentComboBox.setEnabled(singleTreatmentButton.isSelected());
		if (!treatmentByPotentialVegetationButton.isSelected()) {
			TableCellEditor editor = table.getCellEditor();
			if (editor != null) {
				editor.cancelCellEditing();
			}
		}
		table.setEnabled(treatmentByPotentialVegetationButton.isSelected());
	}

	@Override
	public void listenTo() {
		tableModel.addTableModelListener(caller);
		singleTreatmentButton.addActionListener(this);
		treatmentByPotentialVegetationButton.addActionListener(this);
		uniqueTreatmentComboBox.addItemListener(this);
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		tableModel.removeTableModelListener(caller);
		singleTreatmentButton.removeActionListener(this);
		treatmentByPotentialVegetationButton.removeActionListener(this);
		uniqueTreatmentComboBox.removeItemListener(this);
		okButton.removeActionListener(this);
		cancelButton.removeActionListener(this);
	}

	@Override
	protected void initUI() {
		setTitle(UIControlManager.getTitle(getClass()));
		setJMenuBar(new JMenuBar());
		JMenu fileMenu = UIControlManager.createCommonMenu(CommonMenuTitle.File);
		getJMenuBar().add(fileMenu);
		fileMenu.add(load);
		fileMenu.add(save);
		fileMenu.add(saveAs);
		
		getContentPane().setLayout(new BorderLayout());
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		getContentPane().add(pane, BorderLayout.CENTER);		

		pane.add(Box.createVerticalStrut(10));
		pane.add(createSimplePanel(singleTreatmentButton, 5));
		pane.add(Box.createVerticalStrut(10));
		pane.add(this.createSimplePanel(uniqueTreatmentComboBox, 20));
		pane.add(Box.createVerticalStrut(20));
		pane.add(createSimplePanel(treatmentByPotentialVegetationButton, 5));
		pane.add(Box.createVerticalStrut(10));
		JScrollPane scrollPane = new JScrollPane(table);
		pane.add(createSimplePanel(scrollPane, 20));
		pane.add(Box.createVerticalStrut(10));
		
		getContentPane().add(getControlPanel(), BorderLayout.SOUTH);
	}

	
	private JPanel createSimplePanel(Component comp, int margin) {
		JPanel pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pane.add(Box.createHorizontalStrut(margin));
		pane.add(comp);
		return(pane);
	}
	
	@Override
	public void postSavingAction() {
		refreshTitle();
	}

	@Override
	public void postLoadingAction() {
		synchronizeUIWithOwner();
	}

	private String getTitleForThisDialog() {
		String titleOfThisClass = UIControlManager.getTitle(getClass());
		return titleOfThisClass;
	}
 
	/**
	 * The method sets the title of the dialog.
	 */
	protected void refreshTitle() {
		String filename = caller.getFilename();
		if (filename.isEmpty()) {
			setTitle(getTitleForThisDialog());
		} else {
			if (filename.length() > 40) {
				filename = "..." + filename.substring(filename.length()-41, filename.length());
			}
			setTitle(getTitleForThisDialog() + " - " + filename);
		}
	}

	@Override
	public WindowSettings getWindowSettings() {return windowSettings;}

	@Override
	public void synchronizeUIWithOwner() {
		doNotListenToAnymore();
		refreshInterface();
		refreshTitle();
		listenTo();
	}

	@Override
	public Memorizable getWindowOwner() {
		return caller;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(singleTreatmentButton)) {
			if (singleTreatmentButton.isSelected()) {
				caller.mode = Mode.SingleTreatment;
				checkFeaturesToEnable();
			}
		} else if (e.getSource().equals(treatmentByPotentialVegetationButton)) {
			if (treatmentByPotentialVegetationButton.isSelected()) {
				caller.mode = Mode.TreatmentByPotentialVegetation;
				checkFeaturesToEnable();
			}
		} else if (e.getSource().equals(okButton)) {
			okAction();
		} else if (e.getSource().equals(cancelButton)) {
			cancelAction();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource().equals(uniqueTreatmentComboBox)) {
			caller.singleTreatment = (Enum<?>) uniqueTreatmentComboBox.getSelectedItem();
			System.out.println("Treatment selected = " + uniqueTreatmentComboBox.getSelectedItem());
		}
	}

}
