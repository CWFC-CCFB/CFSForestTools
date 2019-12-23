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
 * @author Mathieu Fortin - October 2019
 */
public final class BioSimClient {

	private static final int MAXIMUM_NB_OBS_AT_A_TIME = 200;
	
	private static final InetSocketAddress REpiceaAddress = new InetSocketAddress("144.172.156.5", 80);
	private static final InetSocketAddress LANAddress = new InetSocketAddress("192.168.0.194", 5000);
	
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
		if (code < 200 || code > 202) {	// if true that means it is not connected
			String innerURLString = "http://" + LANAddress.getHostName() + ":" + LANAddress.getPort() + "/" + api;
			innerURLString = addQueryIfAny(innerURLString, query);
			bioSimURL = new URL(innerURLString);
			connection = (HttpURLConnection) bioSimURL.openConnection();
			code = connection.getResponseCode();
			if (code != 200) {	// means it is connected
				throw new IOException("Unable to access BioSIM from internet or the LAN!");
			}				
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
	
	
	
	
	private static LinkedHashMap<GeographicalCoordinatesProvider, Map> internalCalculationForNormals(Period period, List<Variable> variables, List<GeographicalCoordinatesProvider> locations, List<Month> averageOverTheseMonths) throws IOException {
		LinkedHashMap<GeographicalCoordinatesProvider, Map> outputMap = new LinkedHashMap<GeographicalCoordinatesProvider, Map>();
		
		String variablesQuery = "";
		for (Variable v : variables) {
			variablesQuery += v.name();
			if (variables.indexOf(v) < variables.size() - 1) {
				variablesQuery += SPACE_IN_REQUEST;
			}
		}

		String fieldSeparator = ",";

		String query = constructCoordinatesQuery(locations);
		
		query += "&var=" + variablesQuery ;
		query += "&compress=0";	// compression is disabled by default
		query += "&" + period.parsedQuery;

		String outputString = BioSimClient.getStringFromConnection(NORMAL_API, query);

		Map<Variable,Integer> fieldIndices = new HashMap<Variable,Integer>();
		
		int locationID = 0;
		BioSimMonthMap monthMap = null;
		String[] lines = outputString.split("\n");
		for (String inputLine : lines) {
			if (inputLine.toLowerCase().startsWith("error")) {
				throw new IOException(inputLine);
			} else {
				String[] str = inputLine.split(fieldSeparator);
				if (inputLine.toLowerCase().startsWith("month")) {
					GeographicalCoordinatesProvider location = locations.get(locationID);
					monthMap = new BioSimMonthMap();
					outputMap.put(location, monthMap);
					if (locationID == 0) {
						List<String> fieldNames = Arrays.asList(str);
						for (Variable v : variables) {
							fieldIndices.put(v, fieldNames.indexOf(v.fieldName));
						}
					}
					locationID++;
				} else {
					int monthIndex = Integer.parseInt(str[0]) - 1;
					Month m = Month.values()[monthIndex];
					monthMap.put(m, new HashMap<Variable, Double>());
					for (Variable v : variables) {
						double value = Double.parseDouble(str[fieldIndices.get(v)]);
						monthMap.get(m).put(v, value);
					}
				}
			}
		}
		
		
		if (averageOverTheseMonths == null || averageOverTheseMonths.isEmpty()) {
			return outputMap;
		} else {
			LinkedHashMap<GeographicalCoordinatesProvider, Map> formattedOutputMap = new LinkedHashMap<GeographicalCoordinatesProvider, Map>();
			for (GeographicalCoordinatesProvider location : outputMap.keySet()) {
				monthMap = (BioSimMonthMap) outputMap.get(location);
				formattedOutputMap.put(location, monthMap.getMeanForTheseMonths(averageOverTheseMonths, variables));
			}
			return formattedOutputMap;
		}
	}
	
	/**
	 * Retrieves the normals and compiles the mean or sum over some months. 
	 * @param variables the variables to be retrieved and compiled
	 * @param locations the locations
	 * @param averageOverTheseMonths the months over which the mean or sum is to be calculated. If empty or null the 
	 * method returns the monthly averages.
	 * @return a Map with the locations as keys and maps as values.
	 * @throws IOException
	 */
	public static LinkedHashMap<GeographicalCoordinatesProvider, Map> getNormals(Period period, List<Variable> variables, List<GeographicalCoordinatesProvider> locations, List<Month> averageOverTheseMonths) throws IOException {
		if (locations.size() > MAXIMUM_NB_OBS_AT_A_TIME) {
			LinkedHashMap<GeographicalCoordinatesProvider, Map> resultingMap = new LinkedHashMap<GeographicalCoordinatesProvider, Map>();
			List<GeographicalCoordinatesProvider> copyList = new ArrayList<GeographicalCoordinatesProvider>();
			copyList.addAll(locations);
			List<GeographicalCoordinatesProvider> subList = new ArrayList<GeographicalCoordinatesProvider>();
			while(!copyList.isEmpty()) {
				while(!copyList.isEmpty() && subList.size() < MAXIMUM_NB_OBS_AT_A_TIME) {
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
	 * @param variables the variables to be retrieved and compiled
	 * @param locations the locations
	 * @return a Map with the locations as keys and maps as values.
	 * @throws IOException
	 */
	public static Map<GeographicalCoordinatesProvider, Map> getMonthlyNormals(Period period, List<Variable> variables, List<GeographicalCoordinatesProvider> locations) throws IOException {
		return getNormals(period, variables, locations, null);
	}

	/**
	 * Retrieves the yearly normals.
	 * @param variables the variables to be retrieved and compiled
	 * @param locations the locations
	 * @return a Map with the locations as keys and maps as values.
	 * @throws IOException
	 */
	public static Map<GeographicalCoordinatesProvider, Map> getAnnualNormals(Period period, List<Variable> variables, List<GeographicalCoordinatesProvider> locations) throws IOException {
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
	 * @param fromYr beginning of the interval (inclusive)
	 * @param toYr end of the interval (inclusive)
	 * @param variables a List of Variable enums
	 * @param locations a List of GeographicalCoordinatesProvider instances
	 * @return a LinkedHashMap with GeographicalCoordinatesProvider instances as key and String instances as values. Those strings are actually the code for 
	 * the TeleIO instance on the server.
	 * @throws IOException
	 */
	protected static LinkedHashMap<GeographicalCoordinatesProvider, String> getGeneratedClimate(int fromYr, int toYr, List<Variable> variables, List<GeographicalCoordinatesProvider> locations) throws IOException {
		boolean compress = false; // disabling compression by default
		LinkedHashMap<GeographicalCoordinatesProvider, String> outputMap = new LinkedHashMap<GeographicalCoordinatesProvider, String>();
		
		String variablesQuery = "";
		for (Variable v : variables) {
			variablesQuery += v.name();
			if (variables.indexOf(v) < variables.size() - 1) {
				variablesQuery += SPACE_IN_REQUEST;
			}
		}

		String query = constructCoordinatesQuery(locations);
		query += "&var=" + variablesQuery ;
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
				throw new BioSimClientException("The server was unable to generate the climate for this location: " + location.toString() + ": " + id);
			}
			outputMap.put(location, id);
		}
		return outputMap;
	}

	/**
	 * Returns the names of the available models.
	 * @return a List of String instances
	 */
	public static List<String> getModelList() {
		List<String> copy = new ArrayList<String>();
		copy.addAll(ModelListReference);
		return copy;
	}

	/**
	 * Applies a particular model on some generated climate variables.
	 * @param modelName the name of the model
	 * @param teleIORefs a LinkedHashMap with the references to the TeleIO objects on the server
	 * @return a LinkedHashMap with GeographicalCoordinatesProvider instances as keys and a Map with years and climate variables values as values.
	 * @throws IOException
	 */
	protected static LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer, Double>> applyModel(String modelName, LinkedHashMap<GeographicalCoordinatesProvider, String> teleIORefs) throws IOException {
		if (!ModelListReference.contains(modelName)) {
			throw new InvalidParameterException("The model " + modelName + " is not a valid model. Please consult the list of models through the function getModelList()");
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

		
		LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer,Double>> outputMap = new LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer,Double>>();
		String query = "";
		query += "model=" + modelName;
		if (compress) {
			query += "&compress=1";
		} else {
			query += "&compress=0";
		}
		query += "&wgout=" + wgoutQuery;		
		
		String serverReply = getStringFromConnection(MODEL_API, query);
		String[] lines = serverReply.split("\n");
		int locationId = 0;
		GeographicalCoordinatesProvider location = null;
		Map<Integer,Double> innerMap = null;
		for (String line : lines) {
			if (line.toLowerCase().startsWith("year")) {
				location = refListForLocations.get(locationId);
				innerMap = new HashMap<Integer,Double>();
				outputMap.put(location, innerMap);
				locationId++;
			} else {
				String[] fields = line.split(",");
				Integer year = null;
				Double value = null;
				for (int i = 0; i < fields.length; i++) {
					if (i == 0) {
						year = Integer.parseInt(fields[i]);
					} else {
						value = Double.parseDouble(fields[i]);
					}
				}
				innerMap.put(year, value);
			}
		}
		return outputMap;
	}

	/**
	 * Returns the climate variables for a particular period with the generated climate stored on the server.
	 * @param fromYr starting date (yr) of the period (inclusive)
	 * @param toYr  ending date (yr) of the period (inclusive)
	 * @param variables the list of variables
	 * @param locations the locations of the plots (GeographicalCoordinatesProvider instances)
	 * @param modelName a string representing the model name
	 * @return a LinkedHashMap of GeographicalCoordinatesProvider instances (keys) and climate variables (values) 
	 * @throws IOException
	 */
	public static LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer, Double>> getClimateVariables(int fromYr, 
			int toYr, 
			List<Variable> variables, 
			List<GeographicalCoordinatesProvider> locations,
			String modelName) throws IOException {
		return BioSimClient.getClimateVariables(fromYr, toYr, variables, locations, modelName, false);
	}
	
	private static LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer, Double>> internalCalculationForClimateVariables(int fromYr, 
			int toYr, 
			List<Variable> variables, 
			List<GeographicalCoordinatesProvider> locations,
			String modelName,
			boolean isEphemeral) throws IOException {
		Map<GeographicalCoordinatesProvider, String> alreadyGeneratedClimate = new HashMap<GeographicalCoordinatesProvider, String>();
		List<GeographicalCoordinatesProvider> locationsToGenerate = new ArrayList<GeographicalCoordinatesProvider>();
		
		if (isEphemeral) {
			locationsToGenerate.addAll(locations);
		} else {  // here we retrieve what is already available
			for (GeographicalCoordinatesProvider location : locations) {
				BioSimQuerySignature querySignature = new BioSimQuerySignature(fromYr, toYr, variables, location);
				if (GeneratedClimateMap.containsKey(querySignature)) {
					alreadyGeneratedClimate.put(location, GeneratedClimateMap.get(querySignature));
				} else {
					locationsToGenerate.add(location);
				}
			}
		}
		
		Map<GeographicalCoordinatesProvider, String> generatedClimate = new HashMap<GeographicalCoordinatesProvider, String>();
		if (!locationsToGenerate.isEmpty()) {	// here we generate the climate if needed
			generatedClimate.putAll(BioSimClient.getGeneratedClimate(fromYr, toYr, variables, locationsToGenerate));
			if (!isEphemeral) {	// then we stored the reference in the static map for future use
				for (GeographicalCoordinatesProvider location : generatedClimate.keySet()) {
					GeneratedClimateMap.put(new BioSimQuerySignature(fromYr, toYr, variables, location), generatedClimate.get(location));
				}
			}
		}
		
		generatedClimate.putAll(alreadyGeneratedClimate);
		
		LinkedHashMap<GeographicalCoordinatesProvider, String> mapForModels = new LinkedHashMap<GeographicalCoordinatesProvider, String>();
		for (GeographicalCoordinatesProvider location : locations) {
			mapForModels.put(location, generatedClimate.get(location));
		}
		LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer, Double>> resultingMap = BioSimClient.applyModel(modelName, mapForModels);
		if (isEphemeral) { // then we remove the wgout instances stored on the server
			BioSimClient.removeWgoutObjectsFromServer(generatedClimate.values());
		}
		return resultingMap;
	}
	
	
	/**
	 * Returns the climate variables for a particular period. If the isEphemeral parameter is set
	 * to true, then the generated climate is stored on the server. Subsequent calls to this function
	 * based on the same locations, period and variables will retrieve the stored generated climate on 
	 * the server. To disable this feature, the isEphemeral parameter should be set to false.
	 * @param fromYr starting date (yr) of the period (inclusive)
	 * @param toYr  ending date (yr) of the period (inclusive)
	 * @param variables the list of variables
	 * @param locations the locations of the plots (GeographicalCoordinatesProvider instances)
	 * @param modelName a string representing the model name
	 * @param isEphemeral a boolean to enable the storage of the Wgout instances on the server. 
	 * @return a LinkedHashMap of GeographicalCoordinatesProvider instances (keys) and climate variables (values) 
	 * @throws IOException
	 */
	public static LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer, Double>> getClimateVariables(int fromYr, 
			int toYr, 
			List<Variable> variables, 
			List<GeographicalCoordinatesProvider> locations,
			String modelName,
			boolean isEphemeral) throws IOException {
		if (locations.size() > MAXIMUM_NB_OBS_AT_A_TIME) {
			LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer, Double>> resultingMap = new LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer, Double>>();
			List<GeographicalCoordinatesProvider> copyList = new ArrayList<GeographicalCoordinatesProvider>();
			copyList.addAll(locations);
			List<GeographicalCoordinatesProvider> subList = new ArrayList<GeographicalCoordinatesProvider>();
			while (!copyList.isEmpty()) {
				while (!copyList.isEmpty() && subList.size() < MAXIMUM_NB_OBS_AT_A_TIME) {
					subList.add(copyList.remove(0));	
				}
				resultingMap.putAll(internalCalculationForClimateVariables(fromYr, toYr, variables, subList, modelName, isEphemeral));
				subList.clear();
			}
			return resultingMap;
		} else {
			return internalCalculationForClimateVariables(fromYr, toYr, variables, locations, modelName, isEphemeral);
		}
	}
	
	public static void main(String[] args) throws IOException {
		List<String> references = new ArrayList<String>();
		for (int i = 0; i < 402; i++) {
			references.add("" + i);
		}			
		
		BioSimClient.removeWgoutObjectsFromServer(references);
	}
}
