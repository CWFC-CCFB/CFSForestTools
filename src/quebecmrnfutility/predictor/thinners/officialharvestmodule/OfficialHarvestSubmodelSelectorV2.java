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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import quebecmrnfutility.predictor.QuebecGeneralSettings;
import quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModel.TreatmentType;
import repicea.gui.components.REpiceaMatchWithEnumSelector;
import repicea.serial.MemorizerPackage;
import repicea.serial.UnmarshallingException;
import repicea.serial.xml.XmlDeserializer;
import repicea.simulation.covariateproviders.plotlevel.LandUseProvider.LandUse;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * A class to assign treatments to potential vegetation.
 * @author Mathieu Fortin - August 2026
 */
public final class OfficialHarvestSubmodelSelectorV2 extends REpiceaMatchWithEnumSelector<String, TreatmentType> {
		
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

	protected final Map<Enum<?>, Mode> modes;
	protected final Map<Enum<?>, OfficialHarvestTreatmentDefinitionV2> singleTreatments;
	
	protected final OfficialHarvestSubmodelAreaLimitation areaLimitations;

	
	private static OfficialHarvestTreatmentDefinitionV2[] getTreatmentMatches() {
		List<OfficialHarvestTreatmentDefinitionV2> treatmentMatches = new ArrayList<OfficialHarvestTreatmentDefinitionV2>();
		for (String vegPot : QuebecGeneralSettings.POTENTIAL_VEGETATION_LIST) {
			treatmentMatches.add(new OfficialHarvestTreatmentDefinitionV2(vegPot, TreatmentType.PROTECTION));
		}
		return treatmentMatches.toArray(new OfficialHarvestTreatmentDefinitionV2[] {});
	}
	
	/**
	 * Official constructor.
	 */
	public OfficialHarvestSubmodelSelectorV2() {
		super(Arrays.asList(new LandUse[] {LandUse.WoodProduction, LandUse.SensitiveWoodProduction}),
				getTreatmentMatches(),
				ColumnID.values());

		modes = new HashMap<Enum<?>, Mode>();
		singleTreatments = new HashMap<Enum<?>, OfficialHarvestTreatmentDefinitionV2>();
		for (Enum<?> landUse : matchMaps.keySet()) {
			modes.put(landUse, Mode.SingleTreatment);
			if (landUse == LandUse.WoodProduction) {
				singleTreatments.put(landUse, new OfficialHarvestTreatmentDefinitionV2("", TreatmentType.CPRS));
			} else if (landUse == LandUse.SensitiveWoodProduction) {
				singleTreatments.put(landUse, new OfficialHarvestTreatmentDefinitionV2("", TreatmentType.PROTECTION));
			}
		}
		areaLimitations = new OfficialHarvestSubmodelAreaLimitation(TreatmentType.values());
	}
	
	protected OfficialHarvestSubmodelAreaLimitation getAreaLimitations() {return areaLimitations;}
	
	protected final Mode getMode(Enum<?> lu) {return modes.get(lu);}
	protected final OfficialHarvestTreatmentDefinitionV2 getSingleTreatment(Enum<?> lu) {return singleTreatments.get(lu);}
	
	@Override
	public OfficialHarvestSubmodelSelectorDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new OfficialHarvestSubmodelSelectorDialog(this, (Window) parent, columnNames);
		}
		return (OfficialHarvestSubmodelSelectorDialog) guiInterface;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void load(String filename) throws IOException {
		XmlDeserializer deserializer = new XmlDeserializer(filename);
		try {
			Object newloadedInstance = deserializer.readObject();
			MemorizerPackage mp;
			if (newloadedInstance instanceof OfficialHarvestSubmodelSelector) {
				mp = convertFromDeprecatedVersion((OfficialHarvestSubmodelSelector) newloadedInstance);
			} else if (newloadedInstance instanceof OfficialHarvestSubmodelSelectorV2) {
				mp = ((OfficialHarvestSubmodelSelectorV2) newloadedInstance).getMemorizerPackage();
			} else {
				throw new IOException("The deserialized instance is not compatible: from class " + newloadedInstance.getClass().getSimpleName() + " whereas " + OfficialHarvestSubmodelSelectorV2.class.getSimpleName() + " was expected!");
			}
			unpackMemorizerPackage(mp);
			setFilename(filename);
		} catch (UnmarshallingException e1) {
			throw new IOException("A UnmarshallException occurred while loading the file!");
		} catch (RequiredCodeException e2) {
			throw new IOException(e2.getMessage());
		}
	}

	@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
	private MemorizerPackage convertFromDeprecatedVersion(OfficialHarvestSubmodelSelector newloadedInstance) {
		MemorizerPackage mp = newloadedInstance.getMemorizerPackage();
		Map<Enum<?>, Map<String, OfficialHarvestTreatmentDefinition>> formerMatchMap = (Map) mp.get(0);
		Map<Enum<?>, Map<String, OfficialHarvestTreatmentDefinitionV2>> formattedMatchMap = new LinkedHashMap<Enum<?>, Map<String, OfficialHarvestTreatmentDefinitionV2>>();
		for (Enum<?> category : formerMatchMap.keySet()) {
			formattedMatchMap.put(category, new TreeMap<String, OfficialHarvestTreatmentDefinitionV2>());
			Map<String, OfficialHarvestTreatmentDefinition> innerMap = formerMatchMap.get(category); 
			for (String vegPot : innerMap.keySet()) {
				OfficialHarvestTreatmentDefinition def = innerMap.get(vegPot);
				formattedMatchMap.get(category).put(vegPot, new OfficialHarvestTreatmentDefinitionV2(vegPot, def.getTreatmentType(), def.getDelayBeforeReentryYrs()));
			}
		}
		
		Map<Enum<?>, OfficialHarvestSubmodelSelector.Mode> formerModeMap = (Map) mp.get(4);
		Map<Enum<?>, OfficialHarvestSubmodelSelectorV2.Mode> formattedModeMap = new HashMap<Enum<?>, OfficialHarvestSubmodelSelectorV2.Mode>();
		for (Enum<?> category : formerModeMap.keySet()) {
			OfficialHarvestSubmodelSelectorV2.Mode formattedMode = formerModeMap.get(category) == OfficialHarvestSubmodelSelector.Mode.SingleTreatment ? 
					OfficialHarvestSubmodelSelectorV2.Mode.SingleTreatment :
						OfficialHarvestSubmodelSelectorV2.Mode.TreatmentByPotentialVegetation;
			formattedModeMap.put(category, formattedMode);
		}

		Map<Enum<?>, OfficialHarvestTreatmentDefinition> formerSingleTreatmentMap = (Map) mp.get(5);
		Map<Enum<?>, OfficialHarvestTreatmentDefinitionV2> formattedSingleTreatmentMap = new HashMap<Enum<?>, OfficialHarvestTreatmentDefinitionV2>();
		for (Enum<?> category : formerSingleTreatmentMap.keySet()) {
			OfficialHarvestTreatmentDefinition def = formerSingleTreatmentMap.get(category);
			formattedSingleTreatmentMap.put(category, new OfficialHarvestTreatmentDefinitionV2("", def.getTreatmentType(), def.getDelayBeforeReentryYrs()));
		}

		OfficialHarvestSubmodelAreaLimitation areaLimitations = (OfficialHarvestSubmodelAreaLimitation) mp.get(6);
		mp.clear();
		mp.add((Serializable) formattedMatchMap);
		mp.add((Serializable) formattedModeMap);
		mp.add((Serializable) formattedSingleTreatmentMap);
		mp.add(areaLimitations);
		return mp;
	}

	@Override
	public final MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = new MemorizerPackage();
		mp.add((Serializable) matchMaps);
		mp.add((Serializable) modes);
		mp.add((Serializable) singleTreatments);
		mp.add(areaLimitations);
		return mp;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected final Map<String, OfficialHarvestTreatmentDefinitionV2> getMatchesMap(Enum<?> landUse) {
		return (Map) super.matchMaps.get(landUse);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public final void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		matchMaps.clear();
		matchMaps.putAll((Map) wasMemorized.get(0));
		modes.clear();
		modes.putAll((Map) wasMemorized.get(1));
		singleTreatments.clear();
		singleTreatments.putAll((Map) wasMemorized.get(2));
		areaLimitations.areaLimitationMap.clear();
		areaLimitations.areaLimitationMap.putAll((Map) ((OfficialHarvestSubmodelAreaLimitation) wasMemorized.get(3)).areaLimitationMap);
	}

	@Override
	public OfficialHarvestTreatmentDefinitionV2 getMatch(Enum<?> landUse, String potentialVegetation) {
		if (modes.get(landUse) == Mode.SingleTreatment) {
			return singleTreatments.get(landUse);
		} else {
			return (OfficialHarvestTreatmentDefinitionV2) super.getMatch(landUse, potentialVegetation);
		}
	}
	
	
//	public static void main(String[] args) {
//		REpiceaTranslator.setCurrentLanguage(Language.French);
//		OfficialHarvestSubmodelSelectorV2 selector = new OfficialHarvestSubmodelSelectorV2();
//		selector.showUI(null);
//		boolean cancelled = selector.getUI(null).hasBeenCancelled();
//		System.out.println("The dialog has been cancelled : " + cancelled);
//		selector.showUI(null);
//		System.exit(0);
//	}

	

}
