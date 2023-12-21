/*
 * This file is part of the CFSForestools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec
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
package quebecmrnfutility.treelogger.sybille;

import java.awt.Container;
import java.awt.Window;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperPredictor.EstimationMethodInDeterministicMode;
import quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperTree.StemTaperTreeSpecies;
import quebecmrnfutility.treelogger.sybille.SybilleTreeLogCategory.LengthID;
import repicea.serial.SerializerChangeMonitor;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The SybilleTreeLoggerParameters is the TreeLoggerParameters implementation for the SybilleTreeLogger.
 * @author Mathieu Fortin and Jean-Francois Lavoie - January 2012
 */
@SuppressWarnings("serial")
public class SybilleTreeLoggerParameters extends TreeLoggerParameters<SybilleTreeLogCategory> {

	static {
		SerializerChangeMonitor.registerClassNameChange("quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictor$EstimationMethodInDeterministicMode", 
				"quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperPredictor$EstimationMethodInDeterministicMode");
	}
	
	
	protected static enum MessageID implements TextableEnum {
		ShortSawlog("short sawlog", "sciage court"),
		PulpAndPaper("pulp", "p\u00E2te");

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
	
	
	
	/**
	 * The stump height (m), ie. the height at which the bucking begins.
	 */
	private double stumpHeightM = 0.15;
	
	private EstimationMethodInDeterministicMode estimationMethod = EstimationMethodInDeterministicMode.SecondOrderMeanOnly;
	
	private boolean integrationOptimization = true;			// means CompositeSimpsonRule is given priority over TrapezoidalRule

	private transient SybilleTreeLoggerParametersDialog guiInterface;
	

	/**
	 * Protected constructor that specifies the TreeLogger class.
	 */
	public SybilleTreeLoggerParameters() {
		super(SybilleTreeLogger.class);
	}
	
	@Override
	protected void initializeDefaultLogCategories() {
		getLogCategories().clear();
		List<SybilleTreeLogCategory> logCategories;
		
		for (StemTaperTreeSpecies species : StemTaperTreeSpecies.values()) {
			logCategories= new ArrayList<SybilleTreeLogCategory>();
			getLogCategories().put(species.name(), logCategories);
			logCategories.add(new SybilleTreeLogCategory(MessageID.ShortSawlog.toString(), species.name(), LengthID.EightFeetLong, 20));
			logCategories.add(new SybilleTreeLogCategory(MessageID.PulpAndPaper.toString(), species.name(), LengthID.FourFeetLong, 8));
		}
		
		setFilename("");
	}

	@Override
	public boolean isCorrect() {
		return true;				// by default the field text instance make sure that the parameters are going to be correct
	}


	@Override
	public SybilleTreeLoggerParametersDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new SybilleTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}

	
	/**
	 * This method returns the stump height (m), i.e. the heiht at which the bucking begins.
	 * @return a double
	 */
	protected double getStumpHeightM() {return stumpHeightM;}
	
	/**
	 * This method sets the stump height (m).
	 * @param stumpHeightM a double
	 */
	public void setStumpHeightM(double stumpHeightM) {this.stumpHeightM = stumpHeightM;}
	
	protected EstimationMethodInDeterministicMode getEstimationMethod() {return estimationMethod;}
	
	/**
	 * This method sets the estimation method for the stem taper integration. The available methods are: first-order and second-order Taylor 
	 * expansion. By default, it is set to the second-order expansion. 
	 * @param estimationMethod an EstimationMethod enum 
	 */
	public void setEstimationMethod(EstimationMethodInDeterministicMode estimationMethod) {this.estimationMethod = estimationMethod;}

	/**
	 * This method enables or disables the integration optimization. By default, the optimization is enabled.
	 * @param enabled a boolean
	 */
	public void setIntegrationOptimizationEnabled(boolean enabled) {integrationOptimization = enabled;}
	
	/**
	 * This method returns true if the integration optimization is enabled.
	 * @return a boolean
	 */
	public boolean isIntegrationOptimizationEnabled() {return integrationOptimization;}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	protected boolean isVariabilityEnabled() {
		if (treeLogger != null) {
			return ((SybilleTreeLogger) treeLogger).isVariabilityEnabled();
		} else {
			return false;
		}
	}

	/*
	 * For testing.
	 */
	public static void main(String[] args) throws IOException {
		SybilleTreeLoggerParameters stlp = new SybilleTreeLoggerParameters();
//		stlp.setReadWritePermissionGranted(new DefaultREpiceaGUIPermission(false));
		stlp.initializeDefaultLogCategories();
		stlp.showUI(null);
//		stlp.showInterface(null);
		System.exit(0);
	}


	
}
