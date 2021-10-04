/*
 * This file is part of the mrnf-foresttools library
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * Author: Jean-Francois Lavoie
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	protected enum FileImportParameterColumns {
		ESTTYPE,
		PARAMETER,
		ESTIMATE,
		STDERR,
		TVALUE,
		PROBT,
		DF,
		ESS,
		ESSLAT
	}
	
	protected enum FileImportCovarianceColumns {
		ESTTYPE,
		PARAMETER,
		AWOOD,
		BWOOD,
		CWOOD,
		ABARK,
		BBARK,
		CBARK,
		ABRANCH,
		BBRANCH,
		CBRANCH,
		AFOLIAGE,
		BFOLIAGE,
		CFOLIAGE,
		ESS,
		ESSLAT
	}
	
	protected enum FileImportEstimatedWeightColumns {
		DEPENDENT,
		ESTIMATE,
		PROBT,
		ESS,
		ESSLAT
	}
	
	protected enum EstimatedWeightDependent {
		wood2,
		bark2,
		stem2,
		branches2,
		foliage2,
		crown2,
		total2
	}
	
	protected enum FileImportErrorCovarianceColumns {
		EQUATION,
		S1,
		S2,
		S3,
		S4,
		S5,
		S6,
		S7,
		ESS,
		ESSLAT
	}
	
	protected enum ErrorCovarianceEquation {
		wood,
		bark,
		stem,
		branches,
		foliage,
		crown,
		total;
	
		static final int errorCovarianceEquationSize = ErrorCovarianceEquation.values().length;
	}
	
	protected enum FileImportSURMSEColumns {
		SUR_MSE,
		ESS,
		ESSLAT
	}
	
	public enum BiomassCompartment {
		WOOD(0),
		BARK(1),
		STEM(-1),
		BRANCHES(2),		
		FOLIAGE(3),
		CROWN(-1),
		TOTAL(-1);
		
		final int rank;
		
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
		private BiomassCompartment(int _rank) {
			rank = _rank;
		}		
	}
	
	protected enum BiomassParameter {
		BETA1,
		BETA2, 
		BETA3
	}
	
	protected enum FileImportParameter {
		awood(BiomassCompartment.WOOD, BiomassParameter.BETA1),
		bwood(BiomassCompartment.WOOD, BiomassParameter.BETA2),
		cwood(BiomassCompartment.WOOD, BiomassParameter.BETA3),
		abark(BiomassCompartment.BARK, BiomassParameter.BETA1),
		bbark(BiomassCompartment.BARK, BiomassParameter.BETA2),
		cbark(BiomassCompartment.BARK, BiomassParameter.BETA3),
		abranche(BiomassCompartment.BRANCHES, BiomassParameter.BETA1),
		bbranche(BiomassCompartment.BRANCHES, BiomassParameter.BETA2),
		cbranche(BiomassCompartment.BRANCHES, BiomassParameter.BETA3),
		afoliage(BiomassCompartment.FOLIAGE, BiomassParameter.BETA1),
		bfoliage(BiomassCompartment.FOLIAGE, BiomassParameter.BETA2),
		cfoliage(BiomassCompartment.FOLIAGE, BiomassParameter.BETA3);
		
		static final int fileImportParameterSize = FileImportParameter.values().length;
		
		public final BiomassCompartment category;
		public final BiomassParameter parameter;
				
		static final int biomassCategorySize = BiomassCompartment.values().length;
		static final int biomassParameterSize = BiomassParameter.values().length;
		
		private FileImportParameter(BiomassCompartment cat, BiomassParameter param) {
			this.category = cat;
			this.parameter = param;
		}
	}

	private final Map<Lambert2005Species, Lambert2005BiomassInternalPredictor> internalPredictors;
	
	public Lambert2005BiomassPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled);
	}
	
	protected Lambert2005BiomassPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);

		internalPredictors = new HashMap<Lambert2005Species, Lambert2005BiomassInternalPredictor>();
		init();
	}

	@Override
	protected void init() {
		try {

			String path = ObjectUtility.getRelativePackagePath(getClass());
			String parmsFilename = path + "0_parms.csv";
						
			// read the parameters and create the internalPredictors
			readParameterFile(parmsFilename);	
			
			String covbFilename = path + "0_covb.csv";
			
			// read the parameter covariances and set them to the internal predictors
			readParameterCovarianceFile(covbFilename);
			
			String estwFilename = path + "0_estw.csv";
			
			readEstimatedWeightsFile(estwFilename);
			
			String covFilename = path + "0_cov.csv";
			
			readErrorCovarianceFile(covFilename);					
			
		} catch (Exception e)
		{
			System.out.println("Lambert2005BiomassPredictor.init() : Unable to initialize the predictor module!  Details : " + e.getMessage());			
		}		
	}
	
	private void readParameterFile(String filename)	{
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(filename);
			Object[] record;
			double value;
			String paramName;			
			
			Lambert2005BiomassInternalPredictor currentInternalPredictor = null; 
			
			FileImportParameter[] importColumns = FileImportParameter.values();
								
			while ((record = reader.nextRecord()) != null) {
				// here we create one internal predictor per species encountered
				String species = record[FileImportParameterColumns.ESSLAT.ordinal()].toString();
				
				Lambert2005Species currentSpecies = Lambert2005Species.valueOf(species);
				
				paramName = record[FileImportParameterColumns.PARAMETER.ordinal()].toString();
												
				value = Double.parseDouble(record[FileImportParameterColumns.ESTIMATE.ordinal()].toString());
				
				if (!internalPredictors.containsKey(currentSpecies))
				{	// create the internalPredictor
					internalPredictors.put(Lambert2005Species.valueOf(species), new Lambert2005BiomassInternalPredictor(this.isParametersVariabilityEnabled, this.isResidualVariabilityEnabled));
					currentInternalPredictor = internalPredictors.get(Lambert2005Species.valueOf(species));					
				}
				
				// populate the internalPredictor
				// Note : some values might be missing from the CSV file, and a value of 0.0 should be used in that case.
				// This is why we read from the CSV file and write every found value to the predictor which has been initialized with 0.0 values.
				FileImportParameter paramIndex = FileImportParameter.valueOf(paramName);						
			
				currentInternalPredictor.setParameterEstimate(paramIndex, value);						
			}
			
			reader.close();
		}
		catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			System.out.println("Lambert2005BiomassPredictor.readParameterFile() : Unable to read " + filename + ".  Details : " + e.getMessage());
		}
	}
	
	private void readParameterCovarianceFile(String filename)	{
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(filename);
			Object[] record;
			double value;
			String paramName;								 		
			
			while ((record = reader.nextRecord()) != null) {
				// here we create one internal predictor per species encountered
				String species = record[FileImportCovarianceColumns.ESSLAT.ordinal()].toString();
								
				paramName = record[FileImportCovarianceColumns.PARAMETER.ordinal()].toString();									
				
				double values[] = new double[FileImportParameter.fileImportParameterSize];
				
				for (int i = FileImportCovarianceColumns.AWOOD.ordinal(); i <= FileImportCovarianceColumns.CFOLIAGE.ordinal(); i++)			
				{
					try {
						values[i - FileImportCovarianceColumns.AWOOD.ordinal()] = Double.parseDouble(record[i].toString());
					} catch (NumberFormatException e)
					{ // 			
						if (e.getMessage() == "empty String")
							values[i - FileImportCovarianceColumns.AWOOD.ordinal()] = 0.0;
						else
							throw e; 
					}
				}
				
				// all internal predictors should be already known here
				Lambert2005BiomassInternalPredictor currentInternalPredictor = internalPredictors.get(Lambert2005Species.valueOf(species));									
				
				// populate the internalPredictor cov values
				// Note : some values might be missing from the CSV file, and a value of 0.0 should be used in that case.
				// This is why we read from the CSV file and write every found value to the predictor which has been initialized with 0.0 values.
				FileImportParameter paramIndex = FileImportParameter.valueOf(paramName);						
			
				currentInternalPredictor.setParameterCovariance(paramIndex, values);						
			}
			
			reader.close();
			
			for (Lambert2005BiomassInternalPredictor predictorValue : internalPredictors.values()) {
				predictorValue.init();
			}
		}
		catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			System.out.println("Lambert2005BiomassPredictor.readParameterCovarianceFile() : Unable to read " + filename + ".  Details : " + e.getMessage());
		}
	}
	
	private void readEstimatedWeightsFile(String filename)	{
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(filename);
			Object[] record;
			double value;
											 				
			while ((record = reader.nextRecord()) != null) {
				// here we create one internal predictor per species encountered
				String species = record[FileImportEstimatedWeightColumns.ESSLAT.ordinal()].toString();
								
				EstimatedWeightDependent dependent = EstimatedWeightDependent.valueOf(record[FileImportEstimatedWeightColumns.DEPENDENT.ordinal()].toString());									
				
				value = Double.parseDouble(record[FileImportEstimatedWeightColumns.ESTIMATE.ordinal()].toString());
												
				// all internal predictors should be already known here
				Lambert2005BiomassInternalPredictor currentInternalPredictor = internalPredictors.get(Lambert2005Species.valueOf(species));									
								
				currentInternalPredictor.setEstimatedWeight(dependent, value);						
			}
			
			reader.close();
			
			for (Lambert2005BiomassInternalPredictor predictorValue : internalPredictors.values()) {
				predictorValue.init();
			}
		}
		catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			System.out.println("Lambert2005BiomassPredictor.readEstimatedWeightsFile() : Unable to read " + filename + ".  Details : " + e.getMessage());
		}
	}
	
	private void readErrorCovarianceFile(String filename)	{
		CSVReader reader = null;
		
		try {
			reader = new CSVReader(filename);
			Object[] record;
			double value;
			String paramName;								 		
			
			while ((record = reader.nextRecord()) != null) {
				// here we create one internal predictor per species encountered
				String species = record[FileImportErrorCovarianceColumns.ESSLAT.ordinal()].toString();
								
				ErrorCovarianceEquation equation = ErrorCovarianceEquation.valueOf(record[FileImportErrorCovarianceColumns.EQUATION.ordinal()].toString());									
				
				double values[] = new double[ErrorCovarianceEquation.errorCovarianceEquationSize];
				
				for (int i = FileImportErrorCovarianceColumns.S1.ordinal(); i <= FileImportErrorCovarianceColumns.S7.ordinal(); i++)			
				{
					values[i - FileImportErrorCovarianceColumns.S1.ordinal()] = Double.parseDouble(record[i].toString());					
				}
				
				// all internal predictors should be already known here
				Lambert2005BiomassInternalPredictor currentInternalPredictor = internalPredictors.get(Lambert2005Species.valueOf(species));									
							
				currentInternalPredictor.setErrorCovariance(equation, values);						
			}
			
			reader.close();
			
			for (Lambert2005BiomassInternalPredictor predictorValue : internalPredictors.values()) {
				predictorValue.init();
			}
		}
		catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			System.out.println("Lambert2005BiomassPredictor.readErrorCovarianceFile() : Unable to read " + filename + ".  Details : " + e.getMessage());
		}
	}
		
	public synchronized Matrix predictBiomassKg(Lambert2005Tree tree) {
		Lambert2005BiomassInternalPredictor predictor = internalPredictors.get(tree.getLambert2005Species());
		return predictor.predictBiomass(tree);
	}
	
	Matrix getWeight(Lambert2005Tree tree) {
		Lambert2005BiomassInternalPredictor predictor = internalPredictors.get(tree.getLambert2005Species());
		return predictor.getWeight(tree);
	}	
}
