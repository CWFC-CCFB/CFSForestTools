/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge-Epicea
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

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import quebecmrnfutility.predictor.thinners.betaharvestmodule.BetaHarvestModel;
import quebecmrnfutility.predictor.thinners.betaharvestmodule.BetaHarvestModel.Treatment;
import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import repicea.simulation.thinners.REpiceaTreatmentEnum;

/**
 * This class handles the treatment and its modifier.
 * @author Mathieu Fortin - April 2011
 */
@SuppressWarnings("deprecation")
public class QuebecMRNFGenericTreatment implements Serializable {

	private static final long serialVersionUID = 20110407;
	
	private static Map<String, String> CLASS_MAP = new TreeMap<String, String>();
	static {
		CLASS_MAP.put("FormerOfficialHarvestModel","quebecmrnfutility.predictor.thinners.formerofficialharvestmodule.FormerOfficialHarvestModel$Treatment");
		CLASS_MAP.put("BetaHarvestModel","quebecmrnfutility.predictor.thinners.betaharvestmodule.BetaHarvestModel$Treatment");
		CLASS_MAP.put("OfficialHarvestModel","quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel$TreatmentType");	// OfficialHarvestModel must be after FormerOfficialHarvestModel, otherwise there might be confusion
	}
	
	
	
	private REpiceaTreatmentEnum treatmentType;
	private int modifier;
	
	/**
	 * General constructor for this class. If the modifier exceeds the range [-80%,+infinity] it is brought
	 * back to the closest boundary (for example, a modifier of -109 would be brought back to -80).
	 * @param treatmentType an Enum that represent the treatment (see class OfficialHarvestModel)
	 * @param modifier a modifier in % that ranges from [-80%,+infinity]
	 */
	public QuebecMRNFGenericTreatment(REpiceaTreatmentEnum treatmentType, int modifier) {
		this.treatmentType = treatmentType;
		if (modifier < -80) {
			modifier = -80;
		}
		this.modifier = modifier;
	}
	
	
	/**
	 * This method creates a QuebecMRNFGenericTreatment from a String.
	 * @param treatmentCompleteName the treatment name
	 * @return the QuebecMRNFGenericTreatment or null if the treatment is undefined
	 */
	public static QuebecMRNFGenericTreatment getGenericTreatmentFromCompleteName(String treatmentCompleteName) {
		int modifier = 0;
		REpiceaTreatmentEnum treatment;
		
		treatmentCompleteName = treatmentCompleteName.replace(";", "/");
		
		String[] treatmentElements = treatmentCompleteName.split("/");
		
		String treatmentCode = treatmentElements[0].trim();
		
		if (treatmentElements.length > 1) {
			modifier = Integer.parseInt(treatmentElements[1].trim().replace("+",""));
		}
		
		try {
			int treatmentType = ((Double) Double.parseDouble(treatmentCode)).intValue();
			if (treatmentType < 0 || treatmentType > Treatment.values().length - 1) {
				treatment = null;
			} else {
				treatment = BetaHarvestModel.Treatment.values()[treatmentType];
			}
		} catch (NumberFormatException e) {
			String treatmentName = ((String) treatmentCode).trim();
			if (!treatmentName.isEmpty()) {
				try {
					int indexLastDot = treatmentName.lastIndexOf(".");
					String className = treatmentName.substring(0, indexLastDot);
					className = checkClassNameChange(className);
					String variableName = treatmentName.substring(indexLastDot + 1).trim();
//					Object[] c = ClassLoader.getSystemClassLoader().loadClass(className).getEnumConstants();
					Object[] c = Class.forName(className).getEnumConstants();
					if (c[0] instanceof TreatmentType && variableName.equals("CJ")) {
						variableName = "CJMSCR";
					}
					treatment = null;
					for (Object obj : c) {
						if (((REpiceaTreatmentEnum) obj).name().equals(variableName)) {
							treatment = (REpiceaTreatmentEnum) obj;
							break;
						}
					}
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
					treatment = null;
				}
			} else {
				treatment = null;
			}
		}
		
		if (treatment == null) {
			return null;
		} else {
			return new QuebecMRNFGenericTreatment(treatment, modifier);
		}
		
		
	}
	
	private static String checkClassNameChange(String className) {
		for (String key : CLASS_MAP.keySet()) {
			if (className.contains(key)) {
				return CLASS_MAP.get(key);
			}
		}
		return className;
	}


	/**
	 * This method returns the enum that matches the treatment.
	 * @return an Enum instance
	 */
	public REpiceaTreatmentEnum getTreatmentType() {
		return treatmentType;
	}
	
	/**
	 * This method returns the modifier (an integer between -50 and +50)
	 * @return the integer
	 */
	public int getModifier() {
		return modifier;
	}

	@Override
	public String toString() {
		String output = getTreatmentType().getCompleteName().trim();
		if (modifier != 0) {
			output += "/" + modifier;
		}
		return output;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof QuebecMRNFGenericTreatment)) {
			return false;
		} else {
			QuebecMRNFGenericTreatment newGenericTreatment = (QuebecMRNFGenericTreatment) obj;
			if (this.treatmentType == newGenericTreatment.treatmentType && this.modifier == newGenericTreatment.modifier) {
				return true;
			} else {
				return false;
			}
		}
	}
	
}
