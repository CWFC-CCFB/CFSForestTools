/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * Author: Jean-Francois Lavoie
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package canforservutility.predictor.biomass.lambert2005;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import canforservutility.predictor.biomass.lambert2005.Lambert2005Tree.Lambert2005Species;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.REpiceaPredictor;
import repicea.util.ObjectUtility;

/**
 * Implement the biomass models in Lambert et al. (2005).
 * @author Jean-Francois Lavoie 2021
 * @see <a href=https://doi.org/10.1139/x05-112> Lambert, M.-C., C.-H. Ung, and F. Raulier. 2005. Canadian
 * national tree aboveground biomass equations. Canadian Journal of Forest Research 35(8): 1996-2018 
 * </a>
 */
@SuppressWarnings("serial")
public class Lambert2005BiomassPredictor extends REpiceaPredictor {
	
	
	private static Map<String, Lambert2005Species> InternalLookupMap = new HashMap<String, Lambert2005Species>();
	static {
		InternalLookupMap.put("Balsam Fir", Lambert2005Species.AbiesBalsamea);
		InternalLookupMap.put("Balsam Poplar", Lambert2005Species.PopulusBalsamifera);
		InternalLookupMap.put("Black Ash", Lambert2005Species.FraxinusNigra);
		InternalLookupMap.put("Black Cherry", Lambert2005Species.PrunusSerotina);
		InternalLookupMap.put("Black Spruce", Lambert2005Species.PiceaMariana);
		InternalLookupMap.put("Eastern Hemlock", Lambert2005Species.TsugaCanadensis);
		InternalLookupMap.put("Eastern White Cedar", Lambert2005Species.ThujaOccidentalis);
		InternalLookupMap.put("Eastern White Pine", Lambert2005Species.PinusStrobus);
		InternalLookupMap.put("Grey Birch", Lambert2005Species.BetulaPopulifolia);
		InternalLookupMap.put("Lodgepole Pine", Lambert2005Species.PinusContorta);
		InternalLookupMap.put("Red Pine", Lambert2005Species.PinusResinosa);
		InternalLookupMap.put("Silver Maple", Lambert2005Species.AcerSaccharinum);
		InternalLookupMap.put("Sugar Maple", Lambert2005Species.AcerSaccharum);
		InternalLookupMap.put("Tamarack Larch", Lambert2005Species.LarixLaricina);
		InternalLookupMap.put("White Birch", Lambert2005Species.BetulaPapyrifera);
		InternalLookupMap.put("White Oak", Lambert2005Species.QuercusAlba);
		InternalLookupMap.put("White Spruce", Lambert2005Species.PiceaGlauca);
		InternalLookupMap.put("feuillu", Lambert2005Species.Broadleaved);
		InternalLookupMap.put("resineux", Lambert2005Species.Coniferous);
		InternalLookupMap.put("all", Lambert2005Species.Any);
		InternalLookupMap.put("Alpine Fir", Lambert2005Species.AbiesLasiocarpa);
		InternalLookupMap.put("Basswood", Lambert2005Species.TiliaAmericana);
		InternalLookupMap.put("Beech", Lambert2005Species.FagusGrandifolia);
		InternalLookupMap.put("Eastern Red Cedar", Lambert2005Species.JuniperusVirginiana);
		InternalLookupMap.put("Hickory", Lambert2005Species.CaryaSp);
		InternalLookupMap.put("Hop-Hornbeam", Lambert2005Species.OstryaVirginiana);
		InternalLookupMap.put("Jack Pine", Lambert2005Species.PinusBanksiana);
		InternalLookupMap.put("Largetooth Aspen", Lambert2005Species.PopulusGrandidentata);
		InternalLookupMap.put("Red Ash", Lambert2005Species.FraxinusPennsylvanica);
		InternalLookupMap.put("Red Maple", Lambert2005Species.AcerRubrum);
		InternalLookupMap.put("Red Oak", Lambert2005Species.QuercusRubra);
		InternalLookupMap.put("Red Spruce", Lambert2005Species.PiceaRubens);
		InternalLookupMap.put("Trembling Aspen", Lambert2005Species.PopulusTremuloides);
		InternalLookupMap.put("White Ash", Lambert2005Species.FraxinusAmericana);
		InternalLookupMap.put("White Elm", Lambert2005Species.UlmusAmericana);
		InternalLookupMap.put("Yellow Birch", Lambert2005Species.BetulaAlleghaniensis);
	}
	
	private final static String SPECIES_FIELD = "ess";
	private final static String PARM_NAME_FIELD = "Parameter";
	private final static String ESTIMATE_FIELD = "Estimate";
	private final static String DEPENDENT_FIELD = "Dependent";
	private final static String EQUATION_FIELD = "Equation";
	private final static List<String> S_FIELDS = Arrays.asList(new String[]{"s1", "s2", "s3", "s4", "s5", "s6", "s7"});
	
	public enum BiomassCompartment {
		/**
		 * Wood between 0.30 cm in height and minimum diameter of 9 cm.
		 */
		WOOD(0, "wood"),
		/**
		 * Bark between 0.30 cm in height and minimum diameter of 9 cm.
		 */
		BARK(1, "bark"),
		/**
		 * Wood and bark between 0.30 cm in height and minimum diameter of 9 cm.
		 */
		STEM(-1, "n/a"),
		/**
		 * Wood and bark of tree top and branches with a large-end diameter of 9 cm or less.
		 */
		BRANCHES(2, "branche"),
		/**
		 * Foliage.
		 */
		FOLIAGE(3, "foliage"),
		/**
		 * Sum of the foliage and branches.
		 */
		CROWN(-1, "n/a"),
		/**
		 * Sum of foliage, branches, bark and wood.
		 */
		TOTAL(-1, "n/a");
		
		final int rank;
		final String fieldName;
		
		private static List<BiomassCompartment> basicBiomassCompartment;
		static List<BiomassCompartment> getBasicBiomassCompartments() {
			if (basicBiomassCompartment == null) {
				basicBiomassCompartment = new ArrayList<BiomassCompartment>();
				basicBiomassCompartment.add(WOOD);
				basicBiomassCompartment.add(BARK);
				basicBiomassCompartment.add(BRANCHES);
				basicBiomassCompartment.add(FOLIAGE);
				
			}
			return basicBiomassCompartment;}  
		private BiomassCompartment(int _rank, String fieldName) {
			rank = _rank;
			this.fieldName = fieldName;
		}		
		
		private static List<String> getWeightLabels() {
			List<BiomassCompartment> compartments = Arrays.asList(BiomassCompartment.values());
			List<String> weightLabels = compartments.stream().map(p -> p.name().toLowerCase().concat("2")).collect(Collectors.toList());
			return weightLabels;
		}
		
		private static List<String> getErrorCovarianceEquationLabels() {
			List<BiomassCompartment> compartments = Arrays.asList(BiomassCompartment.values());
			List<String> errCovNames = compartments.stream().map(p -> p.name().toLowerCase()).collect(Collectors.toList());
			return errCovNames;
		}
	}

	private static List<String> PARMS_PREFIX = Arrays.asList(new String[] {"a","b","c"});
	
	final static List<String> ESTIMATED_WEIGHT_LABELS = BiomassCompartment.getWeightLabels();
	final static List<String> ERROR_COVARIANCE_EQUATION_LABELS = BiomassCompartment.getErrorCovarianceEquationLabels();
	
	enum Version {
		Complete(3),
		Reduced(2);
		
		final int nbParms;
		static List<String> parmsNameComplete;
		static List<String> parmsNameReduced;
		
		Version(int nbParms) {
			this.nbParms = nbParms;
		}
		
		static List<String> getParmsName(Version v) {
			if (v == Version.Complete) {
				if (parmsNameComplete == null) {
					parmsNameComplete = produceListParms(v);
				}
				return parmsNameComplete;
			} else {
				if (parmsNameReduced == null) {
					parmsNameReduced = produceListParms(v);
				}
				return parmsNameReduced;
			}
		}
	}
	
	private static List<String> produceListParms(Version v) {
		List<String> l = new ArrayList<String>();
		for (BiomassCompartment bc : BiomassCompartment.getBasicBiomassCompartments()) {
			for (int i = 0; i < v.nbParms; i++) {
				l.add(PARMS_PREFIX.get(i) + bc.fieldName);
			}
		}
		return l;
	}
	
	
	private final Map<Version, Map<Lambert2005Species, Lambert2005BiomassInternalPredictor>> internalPredictors;

	/**
	 * Default constructor for deterministic simulations.
	 */
	public Lambert2005BiomassPredictor() {
		this(false);
	}

	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode.
	 */
	public Lambert2005BiomassPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);
	}
	
	protected Lambert2005BiomassPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);

		internalPredictors = new HashMap<Version, Map<Lambert2005Species, Lambert2005BiomassInternalPredictor>>();
		init();
	}

	@Override
	protected void init() {
		try {

			String path = ObjectUtility.getRelativePackagePath(getClass());
			
//			for (String prefix : new String[] {"0", "1"}) {
			for (String prefix : new String[] {"0"}) {
				Version v = prefix.equals("0") ? Version.Complete : Version.Reduced;
				String parmsFilename = path + prefix + "_parms.csv";
				readParameterFile(parmsFilename, v);	
				String covbFilename = path + prefix + "_covb.csv";
				readParameterCovarianceFile(covbFilename, v);
				String estwFilename = path + prefix + "_estw.csv";
				readEstimatedWeightsFile(estwFilename, v);
				String covFilename = path + prefix + "_cov.csv";
				readErrorCovarianceFile(covFilename, v);					
			}
			for (Version v : this.internalPredictors.keySet()) {
				for (Lambert2005BiomassInternalPredictor predictorValue : internalPredictors.get(v).values()) {
					predictorValue.init();
				}
			}
		} catch (Exception e) {
			System.out.println("Lambert2005BiomassPredictor.init() : Unable to initialize the predictor module!  Details : " + e.getMessage());			
		}		
	}
	
	
	private Lambert2005Species getLambertSpecies(Object[] record, int indexSpeciesField, Version v) {
		String speciesStr = record[indexSpeciesField].toString();
		if (!InternalLookupMap.containsKey(speciesStr)) {
			throw new InvalidParameterException("This species cannot be matched to a Latin name: " + speciesStr);
		}
		Lambert2005Species species = InternalLookupMap.get(speciesStr);
		return species;
	}
	
	private void readParameterFile(String filename, Version v)	{
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(filename);
			Object[] record;
			double value;
			String paramName;			
			
			Lambert2005BiomassInternalPredictor currentInternalPredictor = null; 

			int indexSpeciesField = reader.getHeader().getIndexOfThisField(SPECIES_FIELD);
			int indexParmNameField = reader.getHeader().getIndexOfThisField(PARM_NAME_FIELD);
			int indexEstimateField = reader.getHeader().getIndexOfThisField(ESTIMATE_FIELD);
			
			while ((record = reader.nextRecord()) != null) {
				Lambert2005Species currentSpecies = getLambertSpecies(record, indexSpeciesField, v);
				// here we create one internal predictor per species and version encountered
				paramName = record[indexParmNameField].toString();
				value = Double.parseDouble(record[indexEstimateField].toString());

				if (!internalPredictors.containsKey(v)) {
					internalPredictors.put(v, new HashMap<Lambert2005Species, Lambert2005BiomassInternalPredictor>());
				}
				
				Map<Lambert2005Species, Lambert2005BiomassInternalPredictor> innerMap = internalPredictors.get(v);
				
				if (!innerMap.containsKey(currentSpecies)) {	// create the internalPredictor
					innerMap.put(currentSpecies, new Lambert2005BiomassInternalPredictor(v, currentSpecies, this.isParametersVariabilityEnabled, this.isResidualVariabilityEnabled));
				}

				currentInternalPredictor = innerMap.get(currentSpecies);					

				// populate the internalPredictor
				// Note : some values might be missing from the CSV file, and a value of 0.0 should be used in that case.
				// This is why we read from the CSV file and write every found value to the predictor which has been initialized with 0.0 values.
				int paramIndex = Version.getParmsName(v).indexOf(paramName);						
				if (paramIndex == -1) {
					reader.close();
					throw new InvalidParameterException("The parameter name " + paramName + "cannot be found in this version of the model " + v.name());
				}
				currentInternalPredictor.setParameterEstimate(paramIndex, value);						
			}
			
			reader.close();
		} catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			System.out.println("Lambert2005BiomassPredictor.readParameterFile() : Unable to read " + filename + ".  Details : " + e.getMessage());
		}
	}
	
	private void readParameterCovarianceFile(String filename, Version v)	{
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(filename);
			Object[] record;
			String parmName;								 		
			
			int indexSpeciesField = reader.getHeader().getIndexOfThisField(SPECIES_FIELD);
			int indexParmNameField = reader.getHeader().getIndexOfThisField(PARM_NAME_FIELD);

			while ((record = reader.nextRecord()) != null) {
				Lambert2005Species currentSpecies = getLambertSpecies(record, indexSpeciesField, v);

				parmName = record[indexParmNameField].toString();
				List<String> parmNames = Version.getParmsName(v);
				int indexParm = parmNames.indexOf(parmName);
				double values[] = new double[parmNames.size()];
				double values_now[] = new double[parmNames.size()];
				
				for (String pName : parmNames) {
					int fieldIndex = reader.getHeader().getIndexOfThisField(pName);
					int indexInArray = parmNames.indexOf(pName);
					try {
						values_now[indexInArray] = Double.parseDouble(record[fieldIndex].toString());
					} catch (NumberFormatException e)  { 			
						if (!e.getMessage().equals("empty String")) {
							reader.close();
							throw e; 
						}
					}
				}
								
				Lambert2005BiomassInternalPredictor currentInternalPredictor = internalPredictors.get(v).get(currentSpecies);
				
				// populate the internalPredictor cov values
				// Note : some values might be missing from the CSV file, and a value of 0.0 should be used in that case.
				// This is why we read from the CSV file and write every found value to the predictor which has been initialized with 0.0 values.
				currentInternalPredictor.setParameterCovariance(indexParm, values);						
			}
			
			reader.close();
		} catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			System.out.println("Lambert2005BiomassPredictor.readParameterCovarianceFile() : Unable to read " + filename + ".  Details : " + e.getMessage());
		}
	}
	
	private void readEstimatedWeightsFile(String filename, Version v)	{
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(filename);
			Object[] record;
			double value;
			
			int indexSpeciesField = reader.getHeader().getIndexOfThisField(SPECIES_FIELD);
			int indexDependentField = reader.getHeader().getIndexOfThisField(DEPENDENT_FIELD);
			int indexEstimateField = reader.getHeader().getIndexOfThisField(ESTIMATE_FIELD);
											 				
			while ((record = reader.nextRecord()) != null) {
				Lambert2005Species currentSpecies = getLambertSpecies(record, indexSpeciesField, v);
				String weightName = record[indexDependentField].toString();									
				int indexWeight = ESTIMATED_WEIGHT_LABELS.indexOf(weightName);
				value = Double.parseDouble(record[indexEstimateField].toString());
				// all internal predictors should be already known here
				Lambert2005BiomassInternalPredictor currentInternalPredictor = internalPredictors.get(v).get(currentSpecies);
				currentInternalPredictor.setEstimatedWeight(indexWeight, value);						
			}
			
			reader.close();
		} catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			System.out.println("Lambert2005BiomassPredictor.readEstimatedWeightsFile() : Unable to read " + filename + ".  Details : " + e.getMessage());
		}
	}
	
	private void readErrorCovarianceFile(String filename, Version v)	{
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(filename);
			Object[] record;
			
			int indexSpeciesField = reader.getHeader().getIndexOfThisField(SPECIES_FIELD);
			int indexEquationField = reader.getHeader().getIndexOfThisField(EQUATION_FIELD);

			while ((record = reader.nextRecord()) != null) {
				Lambert2005Species currentSpecies = getLambertSpecies(record, indexSpeciesField, v);
				String equationName = record[indexEquationField].toString();									
				int indexEquation = ERROR_COVARIANCE_EQUATION_LABELS.indexOf(equationName);
				double values[] = new double[S_FIELDS.size()];
				for (String sf : S_FIELDS) {
					int indexInFile = reader.getHeader().getIndexOfThisField(sf);
					int indexInArray = S_FIELDS.indexOf(sf);
					values[indexInArray] = Double.parseDouble(record[indexInFile].toString());					
				}
				Lambert2005BiomassInternalPredictor currentInternalPredictor = internalPredictors.get(v).get(currentSpecies);
				currentInternalPredictor.setErrorCovariance(indexEquation, values);						
			}
			
			reader.close();
		} catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			System.out.println("Lambert2005BiomassPredictor.readErrorCovarianceFile() : Unable to read " + filename + ".  Details : " + e.getMessage());
		}
	}
	
	/**
	 * Provide the biomass in different compartments of a particular tree. <p>
	 * The output is a Matrix instance whose elements represent the biomass (kg) of the different compartment calculated for a single tree (NO EXPANSION FACTOR). 
	 * The values of the different compartments can be accessed using the BiomassCompartment enum (e.g., myBiomass.getValueAt(BiomassCompartment.WOOD.ordinal(), 0);).
	 * @param tree a Lambert2005Tree instance
	 * @return a Matrix instance that is a column vector 
	 */
	public synchronized Matrix predictBiomassKg(Lambert2005Tree tree) {
		Version v = tree.implementHeighMProvider() ? Version.Complete : Version.Reduced;
		Lambert2005BiomassInternalPredictor predictor = internalPredictors.get(v).get(tree.getLambert2005Species());
		return predictor.predictBiomass(tree);
	}
	
	Matrix getWeight(Lambert2005Tree tree) {
		Version v = tree.implementHeighMProvider() ? Version.Complete : Version.Reduced;
		Lambert2005BiomassInternalPredictor predictor = internalPredictors.get(v).get(tree.getLambert2005Species());
		return predictor.getWeight(tree);
	}	
}
