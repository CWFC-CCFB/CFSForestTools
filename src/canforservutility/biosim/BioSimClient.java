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
import java.util.List;
import java.util.Map;

import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;
import repicea.stats.StatisticalUtility;

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
	
	private static final String NORMAL_API = "BioSimNormals";
	private static final String GENERATOR_API = "BioSimWG";
	private static final String MODEL_API = "BioSimModel";
	private static final String MODEL_LIST_API = "BioSimModelList";

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


	static class PlotLocation implements GeographicalCoordinatesProvider {
		
		private final double elevationM;
		private final double latitude;
		private final double longitude;
		private final String plotID;
		
		PlotLocation(String plotID, double latitudeDeg, double longitudeDeg, double elevationM) {
			this.plotID = plotID;
			this.latitude = latitudeDeg;
			this.longitude = longitudeDeg;
			this.elevationM = elevationM;
		}
		
		
		
		@Override
		public double getElevationM() {return elevationM;}

		@Override
		public double getLatitudeDeg() {return latitude;}

		@Override
		public double getLongitudeDeg() {return longitude;}

		public String getPlotId() {return plotID;}
		
		@Override
		public String toString() {return latitude + "_" + longitude + "_" + elevationM;}
		
	}
	
//	static class BioSimTeleIO extends HashMap<String, Object> {
//		 
//		String convertToString() {
//			String outputString = "";
//	        outputString += "comment=" + get("comment") + "///";
//            outputString += "compress=" + get("compress") + "///";
//	        outputString += "metadata=" + get("metadata") + "///";
//	        outputString += "msg=" + get("msg") + "///";
//       		outputString += "text=" + get("text") + "///";
//      		outputString += "data=" + get("data");
//      		return outputString;
//		}
//	}
	
	/**
	 * An inner class that handles the mean and sum of the different variables.
	 * @author Mathieu Fortin - October 2019
	 */
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
	public static Map<GeographicalCoordinatesProvider, Map> getNormals(Period period, List<Variable> variables, List<GeographicalCoordinatesProvider> locations, List<Month> averageOverTheseMonths) throws IOException {
		Map<GeographicalCoordinatesProvider, Map> outputMap = new HashMap<GeographicalCoordinatesProvider, Map>();
		
		String variablesQuery = "";
		for (Variable v : variables) {
			variablesQuery += v.name();
			if (variables.indexOf(v) < variables.size() - 1) {
				variablesQuery += "%20";
			}
		}

		String fieldSeparator = ",";
		
		for (GeographicalCoordinatesProvider location : locations) {
			String query = "";
			query += "lat=" + location.getLatitudeDeg();
			query += "&long=" + location.getLongitudeDeg();
			if (!Double.isNaN(location.getElevationM())) {
				query += "&elev=" + location.getElevationM();
			}
			query += "&var=" + variablesQuery ;
			query += "&compress=0";	// compression is disabled by default
			query += "&" + period.parsedQuery;

			String outputString = BioSimClient.getStringFromConnection(NORMAL_API, query);
			Map<Variable,Integer> fieldIndices = new HashMap<Variable,Integer>();
			MonthMap monthMap = new MonthMap();
			int i = 0;
			String[] lines = outputString.split("\n");
			for (String inputLine : lines) {
				if (inputLine.startsWith("Error")) {
					throw new IOException(inputLine);
				}
				String[] str = inputLine.split(fieldSeparator);
				if (i==0) {
					List<String> fieldNames = Arrays.asList(str);
					for (Variable v : variables) {
						fieldIndices.put(v, fieldNames.indexOf(v.fieldName));
					}
					outputMap.put(location, new MonthMap());
				} else {
					int monthIndex = Integer.parseInt(str[0]) - 1;
					Month m = Month.values()[monthIndex];
					monthMap.put(m, new HashMap<Variable, Double>());
					for (Variable v : variables) {
						double value = Double.parseDouble(str[fieldIndices.get(v)]);
						monthMap.get(m).put(v, value);
					}
				}
				i++;
			}
			if (averageOverTheseMonths == null || averageOverTheseMonths.isEmpty()) {
				outputMap.put(location, monthMap);
			} else {
				outputMap.put(location, monthMap.getMeanForTheseMonths(averageOverTheseMonths, variables));
			}
		}
		return outputMap;
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
	
	
	
	
	
	/**
	 * Retrieves the normals and compiles the mean or sum over some months. 
	 * @param variables the variables to be retrieved and compiled
	 * @param locations the locations
	 * @param averageOverTheseMonths the months over which the mean or sum is to be calculated. If empty or null the 
	 * method returns the monthly averages.
	 * @return a Map with the locations as keys and maps as values.
	 * @throws IOException
	 */
	public static Map<GeographicalCoordinatesProvider, String> getGeneratedClimate(int fromYr, int toYr, List<Variable> variables, List<GeographicalCoordinatesProvider> locations) throws IOException {
		boolean compress = false; // disabling compression by default
		Map<GeographicalCoordinatesProvider, String> outputMap = new HashMap<GeographicalCoordinatesProvider, String>();
		
		String variablesQuery = "";
		for (Variable v : variables) {
			variablesQuery += v.name();
			if (variables.indexOf(v) < variables.size() - 1) {
				variablesQuery += "%20";
			}
		}

		for (GeographicalCoordinatesProvider location : locations) {
			String query = "";
			query += "lat=" + location.getLatitudeDeg();
			query += "&long=" + location.getLongitudeDeg();
			if (!Double.isNaN(location.getElevationM())) {
				query += "&elev=" + location.getElevationM();
			}
			query += "&var=" + variablesQuery ;
			if (compress) {
				query += "&compress=1";
			} else {
				query += "&compress=0";
			}
			query += "&from=" + fromYr;
			query += "&to=" + toYr;
			
			String serverReply = getStringFromConnection(GENERATOR_API, query);
			outputMap.put(location, serverReply);
		}
		return outputMap;
	}

	public static List<String> getModelList() {
		List<String> copy = new ArrayList<String>();
		copy.addAll(ModelListReference);
		return copy;
	}

	public static Map<GeographicalCoordinatesProvider, String> applyModel(String modelName, Map<GeographicalCoordinatesProvider, String> teleIORefs) throws IOException {
		if (!ModelListReference.contains(modelName)) {
			throw new InvalidParameterException("The model " + modelName + " is not a valid model. Please consult the list of models through the function getModelList()");
		}
		boolean compress = false; // disabling compression
		Map<GeographicalCoordinatesProvider, String> outputMap = new HashMap<GeographicalCoordinatesProvider, String>();
		for (GeographicalCoordinatesProvider location : teleIORefs.keySet()) {
			String query = "";
			query += "model=" + modelName;
			if (compress) {
				query += "&compress=1";
			} else {
				query += "&compress=0";
			}
			String teleIORef = teleIORefs.get(location);
			query += "&wgout=" + teleIORef;		
			
			String serverReply = getStringFromConnection(MODEL_API, query);
			outputMap.put(location, serverReply);
		}
		return outputMap;

	}
	

	public static void main(String[] args) throws IOException {
		List<GeographicalCoordinatesProvider> locations = new ArrayList<GeographicalCoordinatesProvider>();
		for (int i = 0; i < 10; i++) {
			PlotLocation loc = new PlotLocation(((Integer) i).toString(),
					45 + StatisticalUtility.getRandom().nextDouble() * 7,
					-74 + StatisticalUtility.getRandom().nextDouble() * 8,
					300 + StatisticalUtility.getRandom().nextDouble() * 400);
			locations.add(loc);
		}
		List<Variable> var = new ArrayList<Variable>();
		var.add(Variable.TN);
		var.add(Variable.TX);
		var.add(Variable.P);
		long initialTime;
		double nbSecs;

		//	initialTime = System.currentTimeMillis();
		//	Map output = BioSimClient2.getMonthlyNormals(var, locations);
		//	nbSecs = (System.currentTimeMillis() - initialTime) * .001;
		//	System.out.println("Elapsed time = " + nbSecs + " size = " + output.size());
		initialTime = System.currentTimeMillis();
		Map<GeographicalCoordinatesProvider, String> teleIORefs = BioSimClient.getGeneratedClimate(2018, 2019, var, locations);
		nbSecs = (System.currentTimeMillis() - initialTime) * .001;
		System.out.println("Elapsed time = " + nbSecs + " size = " + teleIORefs.size());
		
		initialTime = System.currentTimeMillis();
		Map<GeographicalCoordinatesProvider, String> replies = BioSimClient.applyModel("DegreeDay_Annual", teleIORefs);
		nbSecs = (System.currentTimeMillis() - initialTime) * .001;
		System.out.println("Elapsed time = " + nbSecs + " size = " + replies.size());
		int u = 0;
	}

}
