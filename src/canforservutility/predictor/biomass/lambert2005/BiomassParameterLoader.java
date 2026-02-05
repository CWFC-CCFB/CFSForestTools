/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2021-2026 His Majesty the King in right of Canada
 * Authors: Jean-Francois Lavoie and Mathieu Fortin, Canadian Forest Service
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canforservutility.predictor.biomass.lambert2005.Lambert2005BiomassPredictor.BiomassCompartment;
import canforservutility.predictor.biomass.lambert2005.Lambert2005BiomassPredictor.ModelVersion;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.species.REpiceaSpecies.Species;
import repicea.util.ObjectUtility;

class BiomassParameterLoader {
    private static BiomassParameterLoader Singleton;

	final static String SPECIES_FIELD = "ess";
	private final static String PARM_NAME_FIELD = "Parameter";
	private final static String ESTIMATE_FIELD = "Estimate";
	private final static String DEPENDENT_FIELD = "Dependent";
	private final static String EQUATION_FIELD = "Equation";
	static String ANY = "all";
	private final static List<String> S_FIELDS = Arrays.asList(new String[]{"s1", "s2", "s3", "s4", "s5", "s6", "s7"});
	final static List<String> ESTIMATED_WEIGHT_LABELS = BiomassCompartment.getWeightLabels();
	final static List<String> ERROR_COVARIANCE_EQUATION_LABELS = BiomassCompartment.getErrorCovarianceEquationLabels();


    final Map<ModelVersion, Map<Species, Matrix>> parmsMatrices;
    final Map<ModelVersion, Map<Species, Matrix>> varcovMatrices;
    final Map<ModelVersion, Map<Species, Matrix>> weightsMatrices;
    final Map<ModelVersion, Map<Species, Matrix>> covErrMatrices;


    private BiomassParameterLoader() {
        parmsMatrices = new HashMap<ModelVersion, Map<Species, Matrix>>();
        varcovMatrices = new HashMap<ModelVersion, Map<Species, Matrix>>();
        weightsMatrices = new HashMap<ModelVersion, Map<Species, Matrix>>();
        covErrMatrices = new HashMap<ModelVersion, Map<Species, Matrix>>();
        loadParameterFiles();
    }

    
    static synchronized BiomassParameterLoader getInstance() {
        if (Singleton == null) {
            Singleton = new BiomassParameterLoader();
        }
        return Singleton;
    }

    private void loadParameterFiles() {
		String path = ObjectUtility.getRelativePackagePath(getClass());

        for (String prefix : new String[] { "0", "1" })
        {
            ModelVersion v = prefix == "0" ? ModelVersion.Complete : ModelVersion.Reduced;

			String parmsFilename = path + prefix + "_parms.csv";
			String covbFilename = path + prefix + "_covb.csv";
			String estwFilename = path + prefix + "_estw.csv";
			String covFilename = path + prefix + "_cov.csv";
			
            loadParameterFile(parmsFilename, v, parmsMatrices);
            loadParameterCovarianceFile(covbFilename, v, varcovMatrices);
            loadEstimatedWeightsFile(estwFilename, v, weightsMatrices);
            loadErrorCovarianceFile(covFilename, v, covErrMatrices);
        }
    }

    private enum ParmComponent {
        Parms,
        VarCov,
        ErrVCov,
        Weights;
    }

    private static Matrix getMatrix(Map<ModelVersion, Map<Species, Matrix>> parmDict,
        ModelVersion v, 
        Species species,
        ParmComponent parmComponent,
        boolean shouldBeSquare) {
        if (!parmDict.containsKey(v)) {
            parmDict.put(v, new HashMap<Species, Matrix>());
        }
        Map<Species, Matrix> innerMap = parmDict.get(v);
        
        if (!innerMap.containsKey(species)) {
        	int nbParms = parmComponent == ParmComponent.Weights || parmComponent == ParmComponent.ErrVCov ?
        			7 :
        				v.nbParms * BiomassCompartment.getBasicBiomassCompartments().size();
            innerMap.put(species, shouldBeSquare ? new Matrix(nbParms, nbParms) : new Matrix(nbParms, 1));
        }
        
        return innerMap.get(species);
    }


    @SuppressWarnings("resource")
	private void loadParameterFile(String filename, ModelVersion v, Map<ModelVersion, Map<Species, Matrix>> parmDict) {
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			double value;
			String paramName;			
			
			int indexSpeciesField = reader.getHeader().getIndexOfThisField(SPECIES_FIELD);
			int indexParmNameField = reader.getHeader().getIndexOfThisField(PARM_NAME_FIELD);
			int indexEstimateField = reader.getHeader().getIndexOfThisField(ESTIMATE_FIELD);
			
			while ((record = reader.nextRecord()) != null) {
				if (!record[indexSpeciesField].toString().trim().toLowerCase().equals(ANY)) {
					Species currentSpecies = Lambert2005BiomassPredictor.getLambertSpecies(record, indexSpeciesField, v);
					// here we create one internal predictor per species and version encountered
					paramName = record[indexParmNameField].toString();
					value = Double.parseDouble(record[indexEstimateField].toString());

	                Matrix parms = getMatrix(parmDict, v, currentSpecies, ParmComponent.Parms, false); // not square
	                int paramIndex = ModelVersion.getParmNames(v).indexOf(paramName);
	                if (paramIndex == -1)
	                    throw new InvalidParameterException("Parameter" + paramName + " not found in model " + v.name());

	                parms.setValueAt(paramIndex, 0, value);
				}
            }
			reader.close();
		} catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			System.out.println("Lambert2005BiomassPredictor.readParameterFile() : Unable to read " + filename + ".  Details : " + e.getMessage());
		}
    }

    private void loadParameterCovarianceFile(String filename, ModelVersion v, Map<ModelVersion, Map<Species, Matrix>> parmDict) {
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(filename);
			Object[] record;
			String parmName;								 		
			
			int indexSpeciesField = reader.getHeader().getIndexOfThisField(SPECIES_FIELD);
			int indexParmNameField = reader.getHeader().getIndexOfThisField(PARM_NAME_FIELD);

			while ((record = reader.nextRecord()) != null) {
				if (!record[indexSpeciesField].toString().trim().toLowerCase().equals(ANY)) {
					Species currentSpecies = Lambert2005BiomassPredictor.getLambertSpecies(record, indexSpeciesField, v);
					parmName = record[indexParmNameField].toString();
					List<String> parmNames = ModelVersion.getParmNames(v);
					int indexParm = parmNames.indexOf(parmName);
					double values[] = new double[parmNames.size()];

					for (String pName : parmNames) {
						int fieldIndex = reader.getHeader().getIndexOfThisField(pName);
						int indexInArray = parmNames.indexOf(pName);
						try {
							values[indexInArray] = Double.parseDouble(record[fieldIndex].toString());
						} catch (NumberFormatException e)  { 			
							if (!e.getMessage().equals("empty String")) {
								reader.close();
								throw e; 
							}
						}
					}

					Matrix parm = getMatrix(parmDict, v, currentSpecies, ParmComponent.VarCov, true); // square matrix expected
					for (int i = 0; i < values.length; i++) {
						parm.setValueAt(indexParm, i, values[i]);
					}
				}
			}
			
			reader.close();
		} catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			System.out.println("Lambert2005BiomassPredictor.readParameterCovarianceFile() : Unable to read " + filename + ".  Details : " + e.getMessage());
		}

    }
 
    private void loadEstimatedWeightsFile(String filename, ModelVersion v, Map<ModelVersion, Map<Species, Matrix>> parmDict) {
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(filename);
			Object[] record;
			double value;
			
			int indexSpeciesField = reader.getHeader().getIndexOfThisField(SPECIES_FIELD);
			int indexDependentField = reader.getHeader().getIndexOfThisField(DEPENDENT_FIELD);
			int indexEstimateField = reader.getHeader().getIndexOfThisField(ESTIMATE_FIELD);
											 				
			while ((record = reader.nextRecord()) != null) {
				if (!record[indexSpeciesField].toString().trim().toLowerCase().equals(ANY)) {
					Species currentSpecies = Lambert2005BiomassPredictor.getLambertSpecies(record, indexSpeciesField, v);
					String weightName = record[indexDependentField].toString();									
					int indexWeight = ESTIMATED_WEIGHT_LABELS.indexOf(weightName);
					value = Double.parseDouble(record[indexEstimateField].toString());

					Matrix parm = getMatrix(parmDict, v, currentSpecies, ParmComponent.Weights, false); // not square
					parm.setValueAt(indexWeight, 0, value);
				}
			}
			reader.close();
		} catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			System.out.println("Lambert2005BiomassPredictor.readEstimatedWeightsFile() : Unable to read " + filename + ".  Details : " + e.getMessage());
		}
    }
    	
    private void loadErrorCovarianceFile(String filename, ModelVersion v, Map<ModelVersion, Map<Species, Matrix>> parmDict) {
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(filename);
			Object[] record;
			
			int indexSpeciesField = reader.getHeader().getIndexOfThisField(SPECIES_FIELD);
			int indexEquationField = reader.getHeader().getIndexOfThisField(EQUATION_FIELD);

			while ((record = reader.nextRecord()) != null) {
				if (!record[indexSpeciesField].toString().trim().toLowerCase().equals(ANY)) {
					Species currentSpecies = Lambert2005BiomassPredictor.getLambertSpecies(record, indexSpeciesField, v);
					String equationName = record[indexEquationField].toString();									
					int indexEquation = ERROR_COVARIANCE_EQUATION_LABELS.indexOf(equationName);
					double values[] = new double[S_FIELDS.size()];
					for (String sf : S_FIELDS) {
						int indexInFile = reader.getHeader().getIndexOfThisField(sf);
						int indexInArray = S_FIELDS.indexOf(sf);
						values[indexInArray] = Double.parseDouble(record[indexInFile].toString());					
					}
					Matrix parm = getMatrix(parmDict, v, currentSpecies, ParmComponent.ErrVCov, true); // square
					for (int i = 0; i < values.length; i++)	{
						parm.setValueAt(indexEquation, i, values[i]);
					}
				}	
			}

			reader.close();
		} catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			System.out.println("Lambert2005BiomassPredictor.readErrorCovarianceFile() : Unable to read " + filename + ".  Details : " + e.getMessage());
		}
    }
}
