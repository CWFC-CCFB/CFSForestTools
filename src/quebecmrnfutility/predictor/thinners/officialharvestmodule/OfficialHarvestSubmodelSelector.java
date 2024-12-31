/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2017 Gouvernement du Quebec - Rouge-Epicea
 * Copyright (C) 2024 His Majesty the King in right of Canada
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
package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import java.awt.Container;
import java.awt.Window;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import quebecmrnfutility.predictor.QuebecGeneralSettings;
import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import repicea.gui.components.REpiceaEnhancedMatchSelector;
import repicea.serial.MemorizerPackage;
import repicea.serial.PostUnmarshalling;
import repicea.serial.SerializerChangeMonitor;
import repicea.serial.xml.XmlDeserializer;
import repicea.simulation.covariateproviders.plotlevel.LandUseProvider.LandUse;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * A class to assign treatments to potential vegetation.
 * @author Mathieu Fortin - 2017, December 2024
 */
public final class OfficialHarvestSubmodelSelector extends REpiceaEnhancedMatchSelector<OfficialHarvestTreatmentDefinition> 
												implements PostUnmarshalling {
		
	static {
		SerializerChangeMonitor.registerClassNameChange("quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestSubmodelSelector",
				"quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestSubmodelSelector");
		SerializerChangeMonitor.registerClassNameChange("quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestSubmodelSelector$Mode",
				"quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestSubmodelSelector$Mode");
		SerializerChangeMonitor.registerClassNameChange("quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModel$TreatmentType",
				"quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel$TreatmentType");
		SerializerChangeMonitor.registerClassNameChange("quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestSubmodelSelector$ColumnID",
				"quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestSubmodelSelector$ColumnID");
	}
	
	protected static enum Mode implements TextableEnum {
		SingleTreatment("Single treatment", "Traitement unique"),
		TreatmentByPotentialVegetation("Treatement by potential vegetation", "Traitement par v\u00E9g\u00E9tation potentielle");

		Mode(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	private static enum ColumnID implements TextableEnum {
		PotentialVegetation("Potential vegetation", "V\u00E9g\u00E9tation potentielle"),
		SilviculturalTreatment("Treatment", "Traitement"),
		DelayBeforeReentry("Time before next treatment (yrs)", "Temps avant le prochain traitement (ann\u00E9es)"),
		;

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

	
	@Deprecated
	private Mode mode;
	@Deprecated
	private OfficialHarvestTreatmentDefinition singleTreatment;
	@Deprecated
	private List<OfficialHarvestTreatmentDefinition> potentialMatches;
	@SuppressWarnings("rawtypes")
	@Deprecated 
	private Map matchMap;
	
	protected final Map<Enum<?>, Mode> modes;
	protected final Map<Enum<?>, OfficialHarvestTreatmentDefinition> singleTreatments;
	
	protected final OfficialHarvestSubmodelAreaLimitation areaLimitations;

	/**
	 * Official constructor.
	 */
	public OfficialHarvestSubmodelSelector() {
		super(Arrays.asList(new LandUse[] {LandUse.WoodProduction, LandUse.SensitiveWoodProduction}),
				QuebecGeneralSettings.POTENTIAL_VEGETATION_LIST.toArray(), 
				OfficialHarvestTreatmentDefinition.getDefinitionOfAllAvailableTreatment(),
				ColumnID.values());

		modes = new HashMap<Enum<?>, Mode>();
		singleTreatments = new HashMap<Enum<?>, OfficialHarvestTreatmentDefinition>();
		for (Enum<?> landUse : matchMaps.keySet()) {
			modes.put(landUse, Mode.SingleTreatment);
			if (landUse == LandUse.WoodProduction) {
				for (OfficialHarvestTreatmentDefinition def : getPotentialMatches(landUse)) {
					if (def.isTotalHarvest()) {
						singleTreatments.put(landUse, def.getDeepClone());
						break;
					}
				}
			} else if (landUse == LandUse.SensitiveWoodProduction) {
				for (OfficialHarvestTreatmentDefinition def : getPotentialMatches(landUse)) {
					if (def.isNoHarvest()) {
						singleTreatments.put(landUse, def.getDeepClone());
						break;
					}
				}
			}
		}
		areaLimitations = new OfficialHarvestSubmodelAreaLimitation(TreatmentType.values());
	}
	
	protected OfficialHarvestSubmodelAreaLimitation getAreaLimitations() {
		return areaLimitations;
	}
	
	protected final Mode getMode(Enum<?> lu) {return modes.get(lu);}
	protected final OfficialHarvestTreatmentDefinition getSingleTreatment(Enum<?> lu) {return singleTreatments.get(lu);}
	
	@Override
	public OfficialHarvestSubmodelSelectorDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new OfficialHarvestSubmodelSelectorDialog(this, (Window) parent, columnNames);
		}
		return (OfficialHarvestSubmodelSelectorDialog) guiInterface;
	}

	@Override
	public void load(String filename) throws IOException {
		XmlDeserializer deserializer = new XmlDeserializer(filename);
		OfficialHarvestSubmodelSelector newloadedInstance;
		newloadedInstance = (OfficialHarvestSubmodelSelector) deserializer.readObject();
		unpackMemorizerPackage(newloadedInstance.getMemorizerPackage());
		setFilename(filename);
	}

	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = super.getMemorizerPackage();
		mp.add((Serializable) modes);
		mp.add((Serializable) singleTreatments);
		mp.add(areaLimitations);
		return mp;
	}

	protected final Map<Object, OfficialHarvestTreatmentDefinition> getMatchesMap(Enum<?> landUse) {
		return super.matchMaps.get(landUse);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		super.unpackMemorizerPackage(wasMemorized);
		modes.clear();
		modes.putAll((Map) wasMemorized.get(4));
		singleTreatments.clear();
		singleTreatments.putAll((Map) wasMemorized.get(5));
		areaLimitations.areaLimitationMap.clear();
		areaLimitations.areaLimitationMap.putAll((Map) ((OfficialHarvestSubmodelAreaLimitation) wasMemorized.get(6)).areaLimitationMap);
	}

	@Override
	public OfficialHarvestTreatmentDefinition getMatch(Enum<?> landUse, Object potentialVegetation) {
		if (modes.get(landUse) == Mode.SingleTreatment) {
			return singleTreatments.get(landUse);
		} else {
			return super.getMatch(landUse, potentialVegetation);
		}
	}
	
	@Override
	protected final List<OfficialHarvestTreatmentDefinition> getPotentialMatches(Enum<?> thisEnum) {
		return super.getPotentialMatches(thisEnum);
	}

	
	
	public static void main(String[] args) {
		REpiceaTranslator.setCurrentLanguage(Language.French);
		OfficialHarvestSubmodelSelector selector = new OfficialHarvestSubmodelSelector();
		selector.showUI(null);
		boolean cancelled = selector.getUI(null).hasBeenCancelled();
		System.out.println("The dialog has been cancelled : " + cancelled);
		System.exit(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void postUnmarshallingAction() {
		if (mode != null) {
			modes.put(LandUse.WoodProduction, mode);
		}
		if (matchMap != null) {
			matchMaps.put(LandUse.WoodProduction, matchMap);
		}
		if (singleTreatment != null) {
			singleTreatments.put(LandUse.WoodProduction, singleTreatment);
		}
		if (potentialMatches != null) {
			potentialMatchesMap.put(LandUse.WoodProduction, potentialMatches);
		}
	}

}
