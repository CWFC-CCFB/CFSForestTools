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
package quebecmrnfutility;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import quebecmrnfutility.predictor.hdrelationships.generalhdrelation2009.Heightable2009Tree.Hd2009Species;
import quebecmrnfutility.predictor.thinners.formerofficialharvestmodule.FormerOfficialHarvestableTree.FormerOfficialHarvestableSpecies;
import quebecmrnfutility.predictor.volumemodels.merchantablevolume.VolumableTree.VolSpecies;
import repicea.simulation.covariateproviders.plotlevel.LandUseProvider.LandUse;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


/**
 * This class contains the common codes for species and environment that apply in the 
 * Province of Quebec, Canada.
 * @author Mathieu Fortin - January 2012
 */
@SuppressWarnings("deprecation")
public class GeneralSettings {

	public static enum MessageFieldID implements TextableEnum {
		LatitudeError("latitude must be between 45 and 53 degrees", "la latitude doit \u00EAtre comprise entre 45 et 53 degr\u00E9s"),
		LongitudeError("longitude must be between -80 and -58 degrees", "la longitude doit \u00EAtre comprise entre -80 et -58 degr\u00E9s"),
		AltitudeError("elevation must be between 0 and 1300 m", "l'altitude doit \u00EAtre comprise entre 0 et 1300 m"),
		EcolRegError("the following ecological region is not considered in this module: ", "la r\u00E9gion \u00E9cologique suivante n'est pas reconnue par ce module : "),
		VegPotError("the following potential vegetation is not considered in this module: ", "la v\u00E9g\u00E9tation potentielle suivante n'est pas reconnue par ce module : "),
		DrainageError("the following drainage class is not considered in this module: ", "la classe de drainage suivante n'est pas reconnue par ce module : "),
		SlopeClassError("the following slope class is not considered in this module: ", "la classe de pente suivante n'est pas reconnue par ce module : "),
		WeightError("the weight of a plot must be between 0 and 100", "le poids d'une placette doit \u00EAtre compris entre 0 et 100"),
		TotalPrecError("mean annual precipitation must be between 0 and 1700 mm", "les pr\u00E9cipitations annuelles moyennes doivent \u00EAtre comprises entre 0 et 1700 mm"),
		MeanTempError("mean annual temperature must be between -7 and +10 C degrees", "la temp\u00E9rature annuelle moyenne doit \u00EAtre comprise entre -7 et +10 degr\u00E9s Celsius"),
		SpeciesError("the following species is not considered in this module: ", "l'esp\u00E8ce suivante n'est pas reconnue par ce module : "),
		StatusError("the following status is not considered in this module: ", "l'\u00E9tat suivant n'est pas reconnu par ce module : "),
		DBHError("DBH values are inconsistent", "les valeurs de DHP sont incoh\u00E9rentes"),
		TreeFreqError("tree frequencies are inconsistent", "les fr\u00E9quence des arbres sont incoh\u00E9rentes"),
		TreeQualityError("tree quality must be either A,B,C,D or an empty string", "la classe de qualit\u00E9 doit \u00EAtre A, B, C, D ou une cha\u00EEine de caract\u00E8res vide"),
		TreeHeightError("tree height is inconsistent", "la hauteur d'arbre est incoh\u00E9rente"),
		TreeVolumeError("tree volume must be between 0 and 30 m3", "le volume d'un arbre doit \u00EAtre compris entre 0 et 30 m3"),
		EcolTypeError("the ecological type must be a four-character codes ", "le type \u00E9cologique doit comprendre quatre caract\u00E8res "),
		AgeClassError("the following age class is not recognized: ", "la classe d'\u00E2ge suivante n'est pas reconnue par ce module : "),
		LandUseError("The land use must be one of the following strings: " + Arrays.stream(LandUse.values()).map(s -> s.name()).collect(Collectors.toList()),
				"L'affectation doit correspondre \u00E0 l'une des cha\u00EEnes de caract\u00E8res suivantes : " + Arrays.stream(LandUse.values()).map(s -> s.name()).collect(Collectors.toList())),
		
		DescStratumName("Stratum Identifier (String)", "Identifiant de strate (Texte)"),
		HelpStratumName("This field indicates the strata that are in the input file. This field is optional. If you do not specify any field, the model considers that the input file contains a single stratum. If many strata are found in the input file, the module makes it possible to select one stratum.",
				"Ce champ identifie les strates qui sont comprises dans votre fichier d'entr\u00E9e. Ce champs est facultatif. Si vous ne lui associez aucun champ, le module consid\u00E8re que votre fichier ne contient qu'une seule strate. Dans le cas contraire, le module vous offrira la possibilit\u00E9 de s\u00E9lectionner une des strates de votre fichier d'entr\u00E9e si celui-ci en contient plus d'une."),
		DescPlotName("Plot Identifier (String)", "Identifiant de placette (Texte)"),
		HelpPlotName("This field indicates the plots in the input file. It is mandatory.", "Ce champ identifie les placettes qui sont comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire."),
		DescPlotMeasurementName("Plot Measurement Identifier (String)", "Identifiant de mesure de placette (Texte)"),
		HelpPlotMeasurementName("This field indicates the plot measurement in the input file. This field is mandatory.", "Ce champ identifie les mesures des placettes qui sont comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire."),
		DescCruiseLineName("Cruise line identifier (String)", "Identifiant de la vir\u00E9e (Texte)"),
		HelpCruiseLineName("This field provides the id of the cruise line in the input file. It is optional.", "Ce champ contient l'identifiant de la vir\u00E9e dans votre fichier d'entr\u00E9e. Il est facultatif."),
		DescPlotYCoord("Latitude (Double)", "Latitude (Num\u00E9rique)"),
		HelpPlotYCoord("This field indicates the latitude of the plots in the input file. This field is mandatory. The latitude must be in a degree-decimal format (e.g.: 48.54383).", 
				"Ce champ identifie la latitude des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. La latitude doit \u00EAtre lue dans un format degr\u00E9-d\u00E9cimal (p.ex.: 48.54383)."),
		DescPlotXCoord("Longitude (Double)", "Longitude (Num\u00E9rique)"),
		HelpPlotXCoord("This field indicates the longitude of the plots in the input file. This field is mandatory. The longitude must be in a degree-decimal format (e.g.: -72.38273).", 
				"Ce champ identifie la longitude des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. La longitude doit \u00EAtre lue dans un format degr\u00E9-d\u00E9cimal (p.ex.: -72.38273)."),
		DescPlotAltitude("Elevation (Double)", "Altitude (Num\u00E9rique)"),
		HelpPlotAltitude("This field indicates the elevation above sea level of the plots in the input file. This field is mandatory. The elevation must be expressed in meter (e.g.: 332 m).", 
				"Ce champ identifie l'altitude des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. L'altitude doit \u00EAtre exprim\u00E9e en m\u00E8tres (p.ex.: 332 m)."),
		DescPlotEcoRegion("Ecological Region (String)", "R\u00E9gion \u00E9cologique (Texte)"),
		HelpPlotEcoRegion("This field indicates the ecological region in which are located the plots in the input file. This field is mandatory. The values are the usual two-character codes used by Quebec Ministry of Natural Resources and Wildlife (e.g.: 5g).", 
				"Ce champ identifie la r\u00E9gion \u00E9cologique des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. Il s'agit du code \u00E0 deux caract\u00E8res de la r\u00E9gion \u00E9cologique tel qu'utilis\u00E9 par le minist\u00E8re des Ressources naturelles et de la Faune du Qu\u00E9bec (p.ex.: 5g)."),
		DescPlotTypeEco("Ecological type (String)", "Type \u00E9cologique (Texte)"),
		HelpPlotTypeEco("This field indicates the ecological type of the plots in the input file. This field is required. The values are the usual four-character codes used by Quebec Ministry of Natural Resources and Wildlife (e.g.: FE32).",
				"Ce champ identifie le type \u00E9cologique des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. Il s'agit du code \u00E0 quatre caract\u00E8res du type \u00E9cologique tel qu'utilis\u00E9 par le minist\u00E8re des Ressources naturelles et de la Faune du Qu\u00E9bec (p.ex.: FE32)."),
		DescLandUse("Land Use (String)","Affectation (Texte)"),
		HelpLandUse("This field defines the land use of the plots across the landscape, which can be one of the following strings: (i) WoodProduction, (ii) SensitiveWoodProduction, (iii) Conservation, (iv) Inaccessible, or (v) Unproductive. This field is optional and it matters only if the regional application scale has been selected. If the field is left blank or the stand application scale has been selected, then the plots are assigned to the WoodProduction land use.",
							"Ce champs d\u00E9finit l'affectation des placettes, laquelle doit correspondre \u00E0 l'une des cha\u00EEnes de caract\u00E8res suivantes : (i) WoodProduction, (ii) SensitiveWoodProduction, (iii) Conservation, (iv) Inaccessible ou (v) Unproductive. Ce champs est facultatif et n'a d'implication que lorsque la simulation s'effectue \u00E0 l'\u00E9chelle r\u00E9gionale. Si le champs est vide ou la simulation s'effectue \u00E0 l'\u00E9chelle du peuplement, les placettes sont alors assign\u00E9es \u00E0 l'affectation WoodProduction."),
		DescPlotDrainClass("Draining Class (String)", "Classe de drainage (Texte)"),
		HelpPlotDrainClass("This field sets the drainage class for the plots of the input dataset. This field is mandatory. It is the usual code provided by the Quebec Ministry of Natural Resources and Wildlife (e.g. 3).", 
				"Ce champ identifie la classe de drainage des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. Il s'agit du code \u00E0 un caract\u00E8re de la classe de drainage tel qu'utilis\u00E9 par le minist\u00E8re des Ressources naturelles et de la Faune du Qu\u00E9bec (p.ex.: 3)."),
		DescSlopeClass("Slope class (String)", "Classe de pente (Texte)"),
		HelpSlopeClass("This field sets the slope class for the plots of the input dataset. This field is optional. It is the usual code provided by the Quebec Ministry of Natural Resources and Wildlife (e.g. A, B, ...).", 
				"Ce champ identifie la classe de pente des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est facultatif. Il s'agit du code \u00E0 un caract\u00E8re de la classe de drainage tel qu'utilis\u00E9 par le minist\u00E8re des Ressources naturelles et de la Faune du Qu\u00E9bec (p.ex.: A, B, ...)."),
		DescAgeClass("Stand age class (String)", "Classe d'\u00E2ge du peuplement (Texte)"),
		HelpAgeClass("This field sets the age class of the stand. It is optional. This variable can be retrieved from the provincial forest ecomap.",
				"Ce champ identifie la classe d'\u00E2ge du peuplement. Ce champ est facultatif. Ces valeurs peuvent \u00EAtre obtenues \u00E0 partir de la carte \u00E9coforesti\u00E8re."),				
		DescWeight("Plot weight (Double)", "Poids de la placette (Num\u00E9rique)"),
		helpPlotWeight("This field sets the weight of the individual plots given that the stratum encompasses many plots. This fiels is optional. In case no field would be specified, this module will consider each plot as a complete plot (i.e. unitary weight). The value of this field is usually provided by the Forest Inventory Branch of the Quebec Ministry of Natural Resources and Wildlife.",
				"Ce champ identifie le poids des placettes comprises dans votre fichier d'entr\u00E9e par rapport \u00E0 l'ensemble de la strate. Ce champ est facultatif. Si vous ne lui associez aucun champ, ce module attribuera un poids unitaire par d\u00E9faut. La valeur de ce champ est habituellement fournit par la compilation de l'inventaire provincial de la Direction des inventaires forestiers du MRNF."),
		DescPlotTotalPrec("Annual precipitation in mm (Double)", "Pr\u00E9cipitations annuelles en mm (Num\u00E9rique)"),
		HelpTotalPrec("This field sets the mean annual precipitation of the plots. This field is optional. It must be computed as the mean for the 1971-2000 period and may be estimated using BioSIM, a software developed by the Canadian Forest Service.",
				"Ce champ identifie les pr\u00E9cipitations annuelles moyennes des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est facultatif. Il correspond \u00E0 la moyenne sur la p\u00E9riode 1971-2000. La valeur de ce champ est habituellement estim\u00E9e \u00E0 l'aide du logiciel BioSIM du Service canadien des for\u00EAts."),
		DescPlotMeanTemp("Mean temperature in degrees (Double)", "Temp\u00E9rature moyenne en degr\u00E9s (Num\u00E9rique)"),
		HelpPlotMeanTemp("This field identifies the mean annual temperature of the plots. It is optional. It must be computed as the mean for the 1971-2000 period and may be estimated using BioSIM, a software developed by the Canadian Forest Service.",
				"Ce champ identifie la temp\u00E9rature annuelle moyenne des placettes comprises dans votre fichier d'entr\u00E9e. Ce champ est facultatif. Il correspond \u00E0 la moyenne sur la p\u00E9riode 1971-2000. La valeur de ce champ est habituellement estim\u00E9e \u00E0 l'aide du logiciel BioSIM du Service canadien des for\u00EAts."),
		DescTreeSpecies("Tree Species (String)", "Code d'essence de l'arbre (Texte)"),
		HelpTreeSpecies("This field sets the tree species for the trees of the input file. This field is mandatory. It must be specified using the usual species code of the Quebec Ministry of Natural Resources and Wildlife (e.g.: SAB).",
				"Ce champ identifie l'esp\u00E8ce des arbres compris dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. Il s'agit du code \u00E0 trois caract\u00E8res de l'esp\u00E8ce tel qu'utilis\u00E9 par le minist\u00E8re des Ressources naturelles et de la Faune du Qu\u00E9bec (p.ex.: SAB)."),
		DescTreeStatus("Tree status code (String)", "Code d'\u00E9tat de l'arbre (Texte)"),
		HelpTreeStatus("This field identifies the tree status. It is mandatory. It must be specified using the usual tree status code of the Quebec Ministry of Natural Resources and Wildlife (e.g.: 10).",
				"Ce champ identifie l'\u00E9tat des arbres compris dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. Il s'agit du code \u00E0 deux caract\u00E8res de l'\u00E9tat des arbres tel qu'utilis\u00E9 par le minist\u00E8re des Ressources naturelles et de la Faune du Qu\u00E9bec (p.ex.: 10)."),
		DescTreeDBH("Tree diameter at breast height (Double)", "Diam\u00E8tre a hauteur de poitrine de l'arbre (Num\u00E9rique)"),
		HelpTreeDBH("This field sets the diameter at breast height (DBH, 1,3 m) of the trees. It is mandatory. The DBH measure must be in cm (e.g.: 24.2 cm).",
				"Ce champ identifie le diam\u00E8tre \u00E0 hauteur de poitrine (DHP, 1,3 m) des arbres compris dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. Il s'agit de la mesure du DHP en centim\u00E8tres (p.ex.: 24,2 cm)."),
		DescTreeQuality("Tree quality class (String)", "Classe de qualit\u00E9 de l'arbre (Texte)"),
		HelpTreeQuality("This field sets the quality class of the trees. It is optional. The classes are defined as follows from the best to the worst: A, B, C, and D.",
						"Ce champ identifie la classe de qualit\u00E9 de l'arbre. Ce champ est facultatif. Les classes de qualit\u00E9 sont d\u00E9finies comme suit en ordre d\u00E9croissant : A, B, C et D."),
		DescTreeFreq("Tree frequency (Double)", "Fr\u00E9quence de l'arbre (Num\u00E9rique)"),
		HelpTreeFreq("This field sets the tree frequency. It is mandatory. The value must be a double expressed at the plot level (e.g.: 4.0 trees in the plot).",
				"Ce champ identifie la fr\u00E9quence des arbres de m\u00EAme diam\u00E8tre et de m\u00EAme esp\u00E8ce dans votre fichier d'entr\u00E9e. Ce champ est obligatoire. La valeur doit \u00EAtre un double exprim\u00E9 \u00E0 l'\u00E9chelle de la placette (p.ex.: 4,0 arbres dans la placette)."),
		DescTreeHeight("Tree height (Double)", "Hauteur de l'arbre (Num\u00E9rique)"),
		HelpTreeHeight("This field identifies the tree height. It is optional. The module estimates the tree height when no height is specified using a height-diameter relationship. The value of this field must be a double expressed in m (e.g.: 15.4 m).",
				"Ce champ identifie la hauteur des arbres compris dans votre fichier d'entr\u00E9e. Ce champ est facultatif. Ce module estime la hauteur des arbres dont la hauteur est inconnue \u00E0 l'aide d'une relation hauteur-diam\u00E8tre. La valeur de ce champ doit \u00EAtre un double exprim\u00E9 en m\u00E8tres (p.ex.: 15,4 m)."),
		DescTreeVolume("Tree volume (Double)", "Volume de l'arbre (Num\u00E9rique)"),  
		HelpTreeVolume("This field identifies the tree volume. It is optional. If some tree volumes were measured, this module will compute correction factors in order to correct the predictions of the volume equation for this particular plot. The value must be a double expressed in m3 (e.g.: 0.6072 m3). This field is not taken into account during stochastic simulation.",
				"Ce champ identifie le volume de l'arbre. Il est facultatif. Si vous avez observ\u00E9 ou estim\u00E9 le volume de certains arbres \u00E0 l'aide de tarifs de cubage locaux, ce module corrigera les pr\u00E9visions en volume en fonction de ces valeurs. La valeur doit \u00EAtre un double exprim\u00E9 en m\u00E8tres cubes (p.ex.: 0,6072 m3). Ce champ n'est pas pris en compte lors des simulations stochastiques."),
		DescTreeID("Tree ID (String)", "Identifiant de l'arbre (Texte)"),
		HelpTreeID("This field contains the tree ID. It is optional.", "Ce champ contient l'identifiant de l'arbre. Il est facultatif.")
		;
		

		MessageFieldID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText (String englishText, String frenchText) {
			REpiceaTranslator.setString (this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
		
	}

	public static class SpeciesMatch implements Serializable {
		
		private static final long serialVersionUID = 20101030L;
		
		private Hd2009Species speciesHD;
		private VolSpecies speciesVol;
		private FormerOfficialHarvestableSpecies speciesPrel;
		
		public SpeciesMatch(Hd2009Species speciesHD, VolSpecies speciesVol, FormerOfficialHarvestableSpecies speciesPrel) {
			this.speciesHD = speciesHD;
			this.speciesVol = speciesVol;
			this.speciesPrel = speciesPrel;
		}
		
		public Hd2009Species getHDSpecies() {return speciesHD;}
		public VolSpecies getVolSpecies() {return speciesVol;}
		public FormerOfficialHarvestableSpecies getPrelSpecies() {return speciesPrel;}
	}
	

	public static Map<String, SpeciesMatch> SPECIES_LIST = new HashMap<String, SpeciesMatch>();
	static {
		SPECIES_LIST.put("BOG", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.BOG, FormerOfficialHarvestableSpecies.BOP));
		SPECIES_LIST.put("BOJ", new SpeciesMatch(Hd2009Species.BOJ, VolSpecies.BOJ, FormerOfficialHarvestableSpecies.BOJ));
		SPECIES_LIST.put("BOP", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.BOP, FormerOfficialHarvestableSpecies.BOP));
		SPECIES_LIST.put("CAC", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.CET, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("CAF", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.CET, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("CAR", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.CET, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("CEO", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.CET, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("CET", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.CET, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("CHR", new SpeciesMatch(Hd2009Species.CHR, VolSpecies.CHR, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("CHG", new SpeciesMatch(Hd2009Species.CHR, VolSpecies.CHR, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("CHB", new SpeciesMatch(Hd2009Species.CHR, VolSpecies.CHR, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("CRA", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.BOG, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("EPB", new SpeciesMatch(Hd2009Species.EPB, VolSpecies.EPB, FormerOfficialHarvestableSpecies.EPX));
		SPECIES_LIST.put("EPR", new SpeciesMatch(Hd2009Species.EPR, VolSpecies.EPR, FormerOfficialHarvestableSpecies.EPX));
		SPECIES_LIST.put("EPN", new SpeciesMatch(Hd2009Species.EPN, VolSpecies.EPN, FormerOfficialHarvestableSpecies.EPX));
		SPECIES_LIST.put("EPO", new SpeciesMatch(Hd2009Species.EPB, VolSpecies.EPB, FormerOfficialHarvestableSpecies.EPX));
		SPECIES_LIST.put("ERA", new SpeciesMatch(Hd2009Species.ERS, VolSpecies.ERS, FormerOfficialHarvestableSpecies.ERS));
		SPECIES_LIST.put("ERG", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.BOG, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("ERN", new SpeciesMatch(Hd2009Species.ERS, VolSpecies.ERS, FormerOfficialHarvestableSpecies.ERS));
		SPECIES_LIST.put("ERR", new SpeciesMatch(Hd2009Species.ERR, VolSpecies.ERR, FormerOfficialHarvestableSpecies.ERR));
		SPECIES_LIST.put("ERP", new SpeciesMatch(Hd2009Species.ERR, VolSpecies.ERR, FormerOfficialHarvestableSpecies.ERR));
		SPECIES_LIST.put("ERS", new SpeciesMatch(Hd2009Species.ERS, VolSpecies.ERS, FormerOfficialHarvestableSpecies.ERS));
		SPECIES_LIST.put("FRA", new SpeciesMatch(Hd2009Species.FRN, VolSpecies.FRA, FormerOfficialHarvestableSpecies.F_0));
		SPECIES_LIST.put("FRN", new SpeciesMatch(Hd2009Species.FRN, VolSpecies.FRN, FormerOfficialHarvestableSpecies.F_0));
		SPECIES_LIST.put("FRP", new SpeciesMatch(Hd2009Species.FRN, VolSpecies.FRA, FormerOfficialHarvestableSpecies.F_0));
		SPECIES_LIST.put("HEG", new SpeciesMatch(Hd2009Species.HEG, VolSpecies.HEG, FormerOfficialHarvestableSpecies.HEG));
		SPECIES_LIST.put("JUV", new SpeciesMatch(Hd2009Species.THO, VolSpecies.THO, FormerOfficialHarvestableSpecies.RES));
		SPECIES_LIST.put("MAS", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.BOG, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("MEJ", new SpeciesMatch(Hd2009Species.MEL, VolSpecies.MEL, FormerOfficialHarvestableSpecies.RES));
		SPECIES_LIST.put("MEL", new SpeciesMatch(Hd2009Species.MEL, VolSpecies.MEL, FormerOfficialHarvestableSpecies.RES));
		SPECIES_LIST.put("MEU", new SpeciesMatch(Hd2009Species.MEL, VolSpecies.MEL, FormerOfficialHarvestableSpecies.RES));
		SPECIES_LIST.put("NOC", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.CET, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("ORA", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.ORA, FormerOfficialHarvestableSpecies.F_0));
		SPECIES_LIST.put("ORR", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.ORA, FormerOfficialHarvestableSpecies.F_0));
		SPECIES_LIST.put("ORT", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.ORA, FormerOfficialHarvestableSpecies.F_0));
		SPECIES_LIST.put("OSV", new SpeciesMatch(Hd2009Species.OSV, VolSpecies.OSV, FormerOfficialHarvestableSpecies.F_0));
		SPECIES_LIST.put("PET", new SpeciesMatch(Hd2009Species.PET, VolSpecies.PET, FormerOfficialHarvestableSpecies.PEU));
		SPECIES_LIST.put("PED", new SpeciesMatch(Hd2009Species.PET, VolSpecies.PEG, FormerOfficialHarvestableSpecies.PEU));
		SPECIES_LIST.put("PEG", new SpeciesMatch(Hd2009Species.PEG, VolSpecies.PEG, FormerOfficialHarvestableSpecies.PEU));
		SPECIES_LIST.put("PEB", new SpeciesMatch(Hd2009Species.PET, VolSpecies.PEB, FormerOfficialHarvestableSpecies.PEU));
		SPECIES_LIST.put("PID", new SpeciesMatch(Hd2009Species.PIG, VolSpecies.PIG, FormerOfficialHarvestableSpecies.PIN));
		SPECIES_LIST.put("PIG", new SpeciesMatch(Hd2009Species.PIG, VolSpecies.PIG, FormerOfficialHarvestableSpecies.PIN));
		SPECIES_LIST.put("PIB", new SpeciesMatch(Hd2009Species.PIB, VolSpecies.PIB, FormerOfficialHarvestableSpecies.PIN));
		SPECIES_LIST.put("PIR", new SpeciesMatch(Hd2009Species.PIB, VolSpecies.PIR, FormerOfficialHarvestableSpecies.PIN));
		SPECIES_LIST.put("PIS", new SpeciesMatch(Hd2009Species.PIB, VolSpecies.PIR, FormerOfficialHarvestableSpecies.PIN));
		SPECIES_LIST.put("PRP", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.CET, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("PRU", new SpeciesMatch(Hd2009Species.PRU, VolSpecies.PRU, FormerOfficialHarvestableSpecies.RES));
		SPECIES_LIST.put("SAB", new SpeciesMatch(Hd2009Species.SAB, VolSpecies.SAB, FormerOfficialHarvestableSpecies.SAB));
		SPECIES_LIST.put("SAL", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.BOG, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("SOA", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.BOG, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("SOD", new SpeciesMatch(Hd2009Species.BOP, VolSpecies.BOG, FormerOfficialHarvestableSpecies.F_1));
		SPECIES_LIST.put("THO", new SpeciesMatch(Hd2009Species.THO, VolSpecies.THO, FormerOfficialHarvestableSpecies.THO));
		SPECIES_LIST.put("TIL", new SpeciesMatch(Hd2009Species.TIL, VolSpecies.TIL, FormerOfficialHarvestableSpecies.F_0));
	}



}
