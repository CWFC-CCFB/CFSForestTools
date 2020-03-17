/*
 * This file is part of the mrnf-foresttools library
 *
 * Copyright (C) 2019 Mathieu Fortin - Canadian Wood Fibre Centre
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
package canforservutility.biosim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import canforservutility.biosim.BioSimEnums.Month;
import canforservutility.biosim.BioSimEnums.Period;
import canforservutility.biosim.BioSimEnums.Variable;
import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;

/**
 * This class enables a client for the Biosim server at repicea.dyndns.org.
 * 
 * @author Mathieu Fortin - October 2019
 */
public final class BioSimClient {

	private static final int MAXIMUM_NB_OBS_AT_A_TIME = 200;
	
	private static final String FieldSeparator = ",";


	private static final InetSocketAddress REpiceaAddress = new InetSocketAddress("repicea.dynu.net", 80);

	private final static String addQueryIfAny(String urlString, String query) {
		if (query != null && !query.isEmpty()) {
			return urlString.trim() + "?" + query;
		} else {
			return urlString;
		}
	}

	private final static String getStringFromConnection(String api, String query) throws IOException {
		String urlString = "http://" + REpiceaAddress.getHostName() + ":" + REpiceaAddress.getPort() + "/" + api;
		urlString = addQueryIfAny(urlString, query);
		URL bioSimURL = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) bioSimURL.openConnection();
		int code = connection.getResponseCode();
		if (code < 200 || code > 202) { // if true that means it is not connected
			throw new IOException("Unable to connect to BioSIM server! Please check your connection or contact your network administrator.");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String completeString = "";
		String lineStr;
		int line = 0;
		while ((lineStr = br.readLine()) != null) {
			if (line == 0) {
				completeString += lineStr;
				
			} else {
				completeString += "\n" + lineStr;
			}
			line++;
		}
		br.close();
		return completeString;
	}

	private static final String SPACE_IN_REQUEST = "%20";

	private static final String NORMAL_API = "BioSimNormals";
	private static final String GENERATOR_API = "BioSimWG";
	private static final String MODEL_API = "BioSimModel";
	private static final String MODEL_LIST_API = "BioSimModelList";
	private static final String BIOSIMCLEANUP_API = "BioSimMemoryCleanUp";
	private static final String BIOSIMMEMORYLOAD_API = "BioSimMemoryLoad";

	protected static final BioSimGeneratedClimateMap GeneratedClimateMap = new BioSimGeneratedClimateMap();

	private static final List<String> ModelListReference = new ArrayList<String>();
	static {
		try {
			String modelList = BioSimClient.getStringFromConnection(BioSimClient.MODEL_LIST_API, null);
			String[] models = modelList.split("\n");
			for (String model : models) {
				ModelListReference.add(model);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class InternalShutDownHook extends Thread {
		@Override
		public void run() {
			try {
				System.out.println("Shutdown hook from BioSimClient called!");
				BioSimClient.removeWgoutObjectsFromServer(GeneratedClimateMap.values());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static {
		Runtime.getRuntime().addShutdownHook(new InternalShutDownHook());
	}

	static final List<Month> AllMonths = Arrays.asList(Month.values());

	private static LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> internalCalculationForNormals(Period period,
			List<Variable> variables, List<GeographicalCoordinatesProvider> locations,
			List<Month> averageOverTheseMonths) throws IOException {
		LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> outputMap = new LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet>();

		String variablesQuery = "";
		for (Variable v : variables) {
			variablesQuery += v.name();
			if (variables.indexOf(v) < variables.size() - 1) {
				variablesQuery += SPACE_IN_REQUEST;
			}
		}


		String query = constructCoordinatesQuery(locations);

		query += "&var=" + variablesQuery;
		query += "&compress=0"; // compression is disabled by default
		query += "&" + period.parsedQuery;

		String serverReply = BioSimClient.getStringFromConnection(NORMAL_API, query);
		
		readLines(serverReply, "month", locations, outputMap);

		if (averageOverTheseMonths == null || averageOverTheseMonths.isEmpty()) {
			return outputMap;
		} else {
			LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> formattedOutputMap = new LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet>();
			for (GeographicalCoordinatesProvider location : outputMap.keySet()) {
				BioSimDataSet ds = outputMap.get(location);
				BioSimMonthMap bsmm = new BioSimMonthMap(ds);
				formattedOutputMap.put(location, bsmm.getMeanForTheseMonths(averageOverTheseMonths, variables));
			}
			return formattedOutputMap;
		}
	}


	
	
	
	/**
	 * Retrieves the normals and compiles the mean or sum over some months.
	 * 
	 * @param variables              the variables to be retrieved and compiled
	 * @param locations              the locations
	 * @param averageOverTheseMonths the months over which the mean or sum is to be
	 *                               calculated. If empty or null the method returns
	 *                               the monthly averages.
	 * @return a Map with the locations as keys and maps as values.
	 * @throws IOException
	 */
	public static LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> getNormals(
			Period period,
			List<Variable> variables, 
			List<GeographicalCoordinatesProvider> locations,
			List<Month> averageOverTheseMonths) throws IOException {
		if (locations.size() > MAXIMUM_NB_OBS_AT_A_TIME) {
			LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> resultingMap = new LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet>();
			List<GeographicalCoordinatesProvider> copyList = new ArrayList<GeographicalCoordinatesProvider>();
			copyList.addAll(locations);
			List<GeographicalCoordinatesProvider> subList = new ArrayList<GeographicalCoordinatesProvider>();
			while (!copyList.isEmpty()) {
				while (!copyList.isEmpty() && subList.size() < MAXIMUM_NB_OBS_AT_A_TIME) {
					subList.add(copyList.remove(0));
				}
				resultingMap.putAll(internalCalculationForNormals(period, variables, subList, averageOverTheseMonths));
				subList.clear();
			}
			return resultingMap;
		} else {
			return internalCalculationForNormals(period, variables, locations, averageOverTheseMonths);
		}
	}

	protected static void removeWgoutObjectsFromServer(Collection<String> references) throws IOException {
		if (references.size() > MAXIMUM_NB_OBS_AT_A_TIME) {
			List<String> referenceList = new ArrayList<String>();
			referenceList.addAll(references);
			List<String> subList = new ArrayList<String>();
			while (!referenceList.isEmpty()) {
				while (!referenceList.isEmpty() && subList.size() < MAXIMUM_NB_OBS_AT_A_TIME) {
					subList.add(referenceList.remove(0));
				}
				internalRemovalOfWgoutObjectsFromServer(subList);
				subList.clear();
			}
		} else {
			internalRemovalOfWgoutObjectsFromServer(references);
		}
	}

	private static void internalRemovalOfWgoutObjectsFromServer(Collection<String> references) throws IOException {
		if (references != null && !references.isEmpty()) {
			String query = "";
			for (String reference : references) {
				if (query.isEmpty()) {
					query += reference;
				} else {
					query += SPACE_IN_REQUEST + reference;
				}
			}
			getStringFromConnection(BIOSIMCLEANUP_API, "ref=" + query);
			for (String reference : references) {
				GeneratedClimateMap.removeValue(reference);
			}
		}
	}

	protected static int getNbWgoutObjectsOnServer() throws Exception {
		String serverReply = getStringFromConnection(BIOSIMMEMORYLOAD_API, null);
		try {
			return Integer.parseInt(serverReply);
		} catch (NumberFormatException e) {
			throw new BioSimClientException("The server reply could not be parsed: " + e.getMessage());
		}
	}

	/**
	 * Retrieves the monthly normals.
	 * 
	 * @param variables the variables to be retrieved and compiled
	 * @param locations the locations
	 * @return a DataSet instance
	 * @throws IOException
	 */
	public static Map<GeographicalCoordinatesProvider, BioSimDataSet> getMonthlyNormals(
			Period period, 
			List<Variable> variables,
			List<GeographicalCoordinatesProvider> locations) throws IOException {
		return getNormals(period, variables, locations, null);
	}

	/**
	 * Retrieves the yearly normals.
	 * 
	 * @param variables the variables to be retrieved and compiled
	 * @param locations the locations
	 * @return a DataSet instance
	 * @throws IOException
	 */
	public static Map<GeographicalCoordinatesProvider, BioSimDataSet> getAnnualNormals(
			Period period, 
			List<Variable> variables,
			List<GeographicalCoordinatesProvider> locations) throws IOException {
		return getNormals(period, variables, locations, AllMonths);
	}

	private static String constructCoordinatesQuery(List<GeographicalCoordinatesProvider> locations) {
		String latStr = "";
		String longStr = "";
		String elevStr = "";
		for (GeographicalCoordinatesProvider location : locations) {
			if (latStr.isEmpty()) {
				latStr += location.getLatitudeDeg();
			} else {
				latStr += SPACE_IN_REQUEST + location.getLatitudeDeg();
			}
			if (longStr.isEmpty()) {
				longStr += location.getLongitudeDeg();
			} else {
				longStr += SPACE_IN_REQUEST + location.getLongitudeDeg();
			}
			if (elevStr.isEmpty()) {
				elevStr += processElevationM(location);
			} else {
				elevStr += SPACE_IN_REQUEST + processElevationM(location);
			}
		}

		String query = "";
		query += "lat=" + latStr;
		query += "&long=" + longStr;
		if (!elevStr.isEmpty()) {
			query += "&elev=" + elevStr;
		}
		return query;
	}

	private static String processElevationM(GeographicalCoordinatesProvider location) {
		if (Double.isNaN(location.getElevationM())) {
			return "NaN";
		} else {
			return "" + location.getElevationM();
		}
	}

	/**
	 * Generates climate for some locations over a particular time interval.
	 * 
	 * @param fromYr    beginning of the interval (inclusive)
	 * @param toYr      end of the interval (inclusive)
	 * @param locations a List of GeographicalCoordinatesProvider instances
	 * @return a LinkedHashMap with GeographicalCoordinatesProvider instances as key
	 *         and String instances as values. Those strings are actually the code
	 *         for the TeleIO instance on the server.
	 * @throws IOException
	 */
	protected static LinkedHashMap<GeographicalCoordinatesProvider, String> getGeneratedClimate(
			int fromYr, 
			int toYr,
			List<GeographicalCoordinatesProvider> locations) throws IOException {
		boolean compress = false; // disabling compression by default
		LinkedHashMap<GeographicalCoordinatesProvider, String> outputMap = new LinkedHashMap<GeographicalCoordinatesProvider, String>();

		List<Variable> var = new ArrayList<Variable>();		// TODO remove this part when the server query handler has been updated.
		var.add(Variable.TN);
		var.add(Variable.TX);
		var.add(Variable.P);

		String variablesQuery = "";
		for (Variable v : var) {
			variablesQuery += v.name();
			if (var.indexOf(v) < var.size() - 1) {
				variablesQuery += SPACE_IN_REQUEST;
			}
		}

		String query = constructCoordinatesQuery(locations);
		query += "&var=" + variablesQuery;
		if (compress) {
			query += "&compress=1";
		} else {
			query += "&compress=0";
		}
		query += "&from=" + fromYr;
		query += "&to=" + toYr;

		String serverReply = getStringFromConnection(GENERATOR_API, query);

		String[] ids = serverReply.split(" ");
		if (ids.length != locations.size()) {
			throw new BioSimClientException("The number of wgout ids is different from the number of locations!");
		}
		for (int i = 0; i < locations.size(); i++) {
			String id = ids[i];
			GeographicalCoordinatesProvider location = locations.get(i);
			if (id.toLowerCase().startsWith("error")) {
				throw new BioSimClientException("The server was unable to generate the climate for this location: "
						+ location.toString() + ": " + id);
			}
			outputMap.put(location, id);
		}
		return outputMap;
	}

	/**
	 * Returns the names of the available models.
	 * 
	 * @return a List of String instances
	 */
	public static List<String> getModelList() {
		List<String> copy = new ArrayList<String>();
		copy.addAll(ModelListReference);
		return copy;
	}

	/**
	 * Applies a particular model on some generated climate variables.
	 * 
	 * @param modelName  the name of the model
	 * @param teleIORefs a LinkedHashMap with the references to the TeleIO objects
	 *                   on the server
	 * @return a LinkedHashMap with GeographicalCoordinatesProvider instances as
	 *         keys and a Map with years and climate variables values as values.
	 * @throws IOException
	 */
	protected static LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> applyModel(
			String modelName,
			LinkedHashMap<GeographicalCoordinatesProvider, String> teleIORefs) throws IOException {
		if (!ModelListReference.contains(modelName)) {
			throw new InvalidParameterException("The model " + modelName
					+ " is not a valid model. Please consult the list of models through the function getModelList()");
		}
		boolean compress = false; // disabling compression

		String wgoutQuery = "";
		List<GeographicalCoordinatesProvider> refListForLocations = new ArrayList<GeographicalCoordinatesProvider>();
		for (GeographicalCoordinatesProvider location : teleIORefs.keySet()) {
			refListForLocations.add(location);
			if (wgoutQuery.isEmpty()) {
				wgoutQuery += teleIORefs.get(location);
			} else {
				wgoutQuery += SPACE_IN_REQUEST + teleIORefs.get(location);
			}
		}

		LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> outputMap = new LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet>();
		String query = "";
		query += "model=" + modelName;
		if (compress) {
			query += "&compress=1";
		} else {
			query += "&compress=0";
		}
		query += "&wgout=" + wgoutQuery;

		String serverReply = getStringFromConnection(MODEL_API, query);
		
		readLines(serverReply, "year", refListForLocations, outputMap);
		
		return outputMap;
	}

	
	private static void readLines(String serverReply,
			String fieldLineStarter,
			List<GeographicalCoordinatesProvider> refListForLocations,
			LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> outputMap) {
		String[] lines = serverReply.split("\n");
		BioSimDataSet dataSet = null;
		int locationId = 0;
		GeographicalCoordinatesProvider location = null;
		boolean properlyInitialized = false;
		for (String line : lines) {
			if (line.toLowerCase().startsWith("error")) {
				throw new BioSimClientException(line);
			} else if (line.toLowerCase().startsWith(fieldLineStarter)) { // means it is a new location
				if (dataSet != null) {	// must be indexed before instantiating a new DataSet
					dataSet.indexFieldType();
				}
				location = refListForLocations.get(locationId);
				String[] fields = line.split(FieldSeparator);
				List<String> fieldNames = Arrays.asList(fields);
				dataSet = new BioSimDataSet(fieldNames);
				outputMap.put(location, dataSet);
				locationId++;
				properlyInitialized = true;
			} else {
				if (!properlyInitialized) {
					throw new BioSimClientException(serverReply);
				} else {
					Object[] fields = Arrays.asList(line.split(FieldSeparator)).toArray(new Object[]{});
					dataSet.addObservation(fields);
				}
			}
		}
		if (dataSet != null) {
			dataSet.indexFieldType();	// last DataSet has not been instantiated so it needs to be here.
		}
	}
	
	
	/**
	 * Returns the climate variables for a particular period with the generated
	 * climate stored on the server.
	 * 
	 * @param fromYr    starting date (yr) of the period (inclusive)
	 * @param toYr      ending date (yr) of the period (inclusive)
	 * @param locations the locations of the plots (GeographicalCoordinatesProvider
	 *                  instances)
	 * @param modelName a string representing the model name
	 * @return a LinkedHashMap of GeographicalCoordinatesProvider instances (keys)
	 *         and climate variables (values)
	 * @throws IOException
	 */
	public static LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> getClimateVariables(int fromYr, 
			int toYr,
			List<GeographicalCoordinatesProvider> locations, 
			String modelName)
			throws IOException {
		return BioSimClient.getClimateVariables(fromYr, toYr, locations, modelName, false);
	}

	private static LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> internalCalculationForClimateVariables(
			int fromYr, 
			int toYr, 
			List<GeographicalCoordinatesProvider> locations,
			String modelName, 
			boolean isEphemeral) throws IOException {
		Map<GeographicalCoordinatesProvider, String> alreadyGeneratedClimate = new HashMap<GeographicalCoordinatesProvider, String>();
		List<GeographicalCoordinatesProvider> locationsToGenerate = new ArrayList<GeographicalCoordinatesProvider>();

		if (isEphemeral) {
			locationsToGenerate.addAll(locations);
		} else { // here we retrieve what is already available
			for (GeographicalCoordinatesProvider location : locations) {
				BioSimQuerySignature querySignature = new BioSimQuerySignature(fromYr, toYr, location);
				if (GeneratedClimateMap.containsKey(querySignature)) {
					alreadyGeneratedClimate.put(location, GeneratedClimateMap.get(querySignature));
				} else {
					locationsToGenerate.add(location);
				}
			}
		}

		Map<GeographicalCoordinatesProvider, String> generatedClimate = new HashMap<GeographicalCoordinatesProvider, String>();
		if (!locationsToGenerate.isEmpty()) { // here we generate the climate if needed
			generatedClimate.putAll(BioSimClient.getGeneratedClimate(fromYr, toYr, locationsToGenerate));
			if (!isEphemeral) { // then we stored the reference in the static map for future use
				for (GeographicalCoordinatesProvider location : generatedClimate.keySet()) {
					GeneratedClimateMap.put(new BioSimQuerySignature(fromYr, toYr, location),
							generatedClimate.get(location));
				}
			}
		}

		generatedClimate.putAll(alreadyGeneratedClimate);

		LinkedHashMap<GeographicalCoordinatesProvider, String> mapForModels = new LinkedHashMap<GeographicalCoordinatesProvider, String>();
		for (GeographicalCoordinatesProvider location : locations) {
			mapForModels.put(location, generatedClimate.get(location));
		}
		LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> resultingMap = BioSimClient.applyModel(modelName, mapForModels);
		if (isEphemeral) { // then we remove the wgout instances stored on the server
			BioSimClient.removeWgoutObjectsFromServer(generatedClimate.values());
		}
		return resultingMap;
	}

	/**
	 * Returns the climate variables for a particular period. If the isEphemeral
	 * parameter is set to true, then the generated climate is stored on the server.
	 * Subsequent calls to this function based on the same locations, period and
	 * variables will retrieve the stored generated climate on the server. To
	 * disable this feature, the isEphemeral parameter should be set to false.
	 * 
	 * @param fromYr      starting date (yr) of the period (inclusive)
	 * @param toYr        ending date (yr) of the period (inclusive)
	 * @param locations   the locations of the plots
	 *                    (GeographicalCoordinatesProvider instances)
	 * @param modelName   a string representing the model name
	 * @param isEphemeral a boolean to enable the storage of the Wgout instances on
	 *                    the server.
	 * @return a LinkedHashMap of GeographicalCoordinatesProvider instances (keys)
	 *         and climate variables (values)
	 * @throws IOException
	 */
	public static LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> getClimateVariables(int fromYr, 
			int toYr,
			List<GeographicalCoordinatesProvider> locations, 
			String modelName,
			boolean isEphemeral) throws IOException {
		if (locations.size() > MAXIMUM_NB_OBS_AT_A_TIME) {
			LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet> resultingMap = new LinkedHashMap<GeographicalCoordinatesProvider, BioSimDataSet>();
			List<GeographicalCoordinatesProvider> copyList = new ArrayList<GeographicalCoordinatesProvider>();
			copyList.addAll(locations);
			List<GeographicalCoordinatesProvider> subList = new ArrayList<GeographicalCoordinatesProvider>();
			while (!copyList.isEmpty()) {
				while (!copyList.isEmpty() && subList.size() < MAXIMUM_NB_OBS_AT_A_TIME) {
					subList.add(copyList.remove(0));
				}
				resultingMap.putAll(internalCalculationForClimateVariables(fromYr, toYr, subList, modelName,
						isEphemeral));
				subList.clear();
			}
			return resultingMap;
		} else {
			return internalCalculationForClimateVariables(fromYr, toYr, locations, modelName, isEphemeral);
		}
	}

	public static void main(String[] args) throws IOException {
//		List<String> references = new ArrayList<String>();
//		for (int i = 0; i < 402; i++) {
//			references.add("" + i);
//		}			
//		
//		BioSimClient.removeWgoutObjectsFromServer(references);
		List<String> models = BioSimClient.getModelList();
		for (String model : models)
			System.out.println(model);
	}
}
