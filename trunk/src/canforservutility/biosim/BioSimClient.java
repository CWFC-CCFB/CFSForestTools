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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;

/**
 * This class enables a client for the Biosim server at repicea.dyndns.org. 
 * @author Mathieu Fortin - October 2019
 */
public class BioSimClient {

	private static final InetSocketAddress REpiceaAddress = new InetSocketAddress("144.172.156.5", 5000);
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

	private static final Map<QuerySignature, String> GeneratedClimateMap = new ConcurrentHashMap<QuerySignature, String>();
	
	
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

	/**
	 * A class for exceptions specific to BioSim
	 * @author Mathieu Fortin - November 2019
	 */
	@SuppressWarnings("serial")
	public static class BioSimClientException extends InvalidParameterException {
		
		protected BioSimClientException(String message) {
			super(message);
		}
		
	}

	static class QuerySignature {
		
		final int initialYear;
		final int finalYear;
		final List<Variable> variables;
		final double latitudeDeg;
		final double longitudeDeg;
		final double elevationM;
		
		QuerySignature(int initialYear, int finalYear, List<Variable> variables, GeographicalCoordinatesProvider location) {
			this.initialYear = initialYear;
			this.finalYear = finalYear;
			this.variables = new ArrayList<Variable>();
			this.variables.addAll(variables);
			this.latitudeDeg = location.getLatitudeDeg();
			this.longitudeDeg = location.getLongitudeDeg();
			this.elevationM = location.getElevationM();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof QuerySignature) {
				QuerySignature thatQuery = (QuerySignature) obj;
				if (thatQuery.initialYear == initialYear) {
					if (thatQuery.finalYear == finalYear) {
						if (thatQuery.variables.equals(variables)) {
							if (thatQuery.latitudeDeg == latitudeDeg) {
								if (thatQuery.longitudeDeg == longitudeDeg) {
									if (thatQuery.elevationM == elevationM) {
										return true;
									}
								}
							}
						}
					}
				}
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return initialYear * 10000000 + finalYear + variables.hashCode();
		}
	}

	
	/**
	 * An inner class that handles the mean and sum of the different variables.
	 * @author Mathieu Fortin - October 2019
	 */
	@SuppressWarnings("serial")
	static class MonthMap extends HashMap<Month, Map<Variable, Double>> {
		
		Map<Variable, Double> getMeanForTheseMonths(List<Month> months, List<Variable> variables) {
			Map<Variable, Double> outputMap = new HashMap<Variable, Double>();
			int nbDays = 0;
			for (Month month : months) {
				if (containsKey(month)) {
					for (Variable var : variables) {
						if (get(month).containsKey(var)) {
							double value = get(month).get(var);
							if (!var.isAdditive()) {
								value *= month.nbDays;
							} 
							if (!outputMap.containsKey(var)) {
								outputMap.put(var, 0d);
							}
							outputMap.put(var, outputMap.get(var) + value);
						} else {
							throw new InvalidParameterException("The variable " + var.name() + " is not in the MonthMap instance!");
						}
					}
				} else {
					throw new InvalidParameterException("The month " + month.name() + " is not in the MonthMap instance!");
				}
				nbDays += month.nbDays;
			}
			for (Variable var : variables) {
				if (!var.additive) {
					outputMap.put(var, outputMap.get(var) / nbDays);
				}
			}
			return outputMap;
		}
	}


	public static enum Period {
		FromNormals1951_1980("period=1951_1980"),
		FromNormals1961_1990("period=1961_1990"),
		FromNormals1971_2000("period=1971_2000"),
		FromNormals1981_2010("period=1981_2010");
		
		String parsedQuery;
		
		Period(String parsedRequest) {
			this.parsedQuery = parsedRequest;
		}
	}
	
	static final List<Month> AllMonths = Arrays.asList(Month.values());
	
		
	public static enum Variable {	// TODO complete this
		TN("TMIN_MN", false, "min air temperature"),
		T("", false, "air temperature"),
		TX("TMAX_MN", false, "max air temperature"),
		P("PRCP_TT", true, "precipitation"),
		TD("TDEX_MN", false, "temperature dew point"),
		H("", false, "humidity"),
		WS("", false, "wind speed"),
		WD("", false, "wind direction"),
		R("", true, "solar radiation"),
		Z("", false, "atmospheric pressure"),
		S("", true, "snow precipitation"),
		SD("", false, "snow depth accumulation"),
		SWE("", true, "snow water equivalent"),
		WS2("", false, "wind speed at 2 m");
		
		String description;
		String fieldName;
		boolean additive;
		
		Variable(String fieldName, boolean additive, String description) {
			this.fieldName = fieldName;
			this.additive = additive;
			this.description = description;
		}
		
		public boolean isAdditive() {return additive;};
		public String getDescription() {return description;}
	}
	
	public static enum Month {
		January(31),
		February(28),
		March(31),
		April(30),
		May(31),
		June(30),
		July(31),
		August(31),
		September(30),
		October(31),
		November(30),
		December(31);
		
		int nbDays;
		
		Month(int nbDays) {
			this.nbDays = nbDays;
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
		MonthMap monthMap = null;
		String[] lines = outputString.split("\n");
		for (String inputLine : lines) {
			if (inputLine.toLowerCase().startsWith("error")) {
				throw new IOException(inputLine);
			} else {
				String[] str = inputLine.split(fieldSeparator);
				if (inputLine.toLowerCase().startsWith("month")) {
					GeographicalCoordinatesProvider location = locations.get(locationID);
					monthMap = new MonthMap();
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
				monthMap = (MonthMap) outputMap.get(location);
				formattedOutputMap.put(location, monthMap.getMeanForTheseMonths(averageOverTheseMonths, variables));
			}
			return formattedOutputMap;
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
	

	public static LinkedHashMap<GeographicalCoordinatesProvider, Map<Integer, Double>> getClimateVariables(int fromYr, 
			int toYr, 
			List<Variable> variables, 
			List<GeographicalCoordinatesProvider> locations,
			String modelName) throws IOException {
		Map<GeographicalCoordinatesProvider, String> alreadyGeneratedClimate = new HashMap<GeographicalCoordinatesProvider, String>();
		List<GeographicalCoordinatesProvider> locationsToGenerate = new ArrayList<GeographicalCoordinatesProvider>();
		for (GeographicalCoordinatesProvider location : locations) {
			QuerySignature querySignature = new QuerySignature(fromYr, toYr, variables, location);
			if (GeneratedClimateMap.containsKey(querySignature)) {
				alreadyGeneratedClimate.put(location, GeneratedClimateMap.get(querySignature));
			} else {
				locationsToGenerate.add(location);
			}
		}
		
		Map<GeographicalCoordinatesProvider, String> generatedClimate = new HashMap<GeographicalCoordinatesProvider, String>();
		if (!locationsToGenerate.isEmpty()) {
			generatedClimate.putAll(BioSimClient.getGeneratedClimate(fromYr, toYr, variables, locationsToGenerate));
			for (GeographicalCoordinatesProvider location : generatedClimate.keySet()) {
				GeneratedClimateMap.put(new QuerySignature(fromYr, toYr, variables, location), generatedClimate.get(location));
			}
		}
		
		generatedClimate.putAll(alreadyGeneratedClimate);
		
		LinkedHashMap<GeographicalCoordinatesProvider, String> mapForModels = new LinkedHashMap<GeographicalCoordinatesProvider, String>();
		for (GeographicalCoordinatesProvider location : locations) {
			mapForModels.put(location, generatedClimate.get(location));
		}
		
		return BioSimClient.applyModel(modelName, mapForModels);
	}
	

}