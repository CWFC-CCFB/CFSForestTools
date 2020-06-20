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
package quebecmrnfutility.biosim;

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

import repicea.stats.StatisticalUtility;

/**
 * This class enables a client for the Biosim server at repicea.dyndns.org. 
 * @author Mathieu Fortin - October 2019
 */
@Deprecated
abstract class BioSimClient2 {

	private static final String NORMAL_API = "BioSimNormals";
	private static final String GENERATOR_API = "BioSimWG";
	private static final String MODEL_API = "BioSimModel";
	
	static class BioSimTeleIO extends HashMap<String, Object> {
		 
		String convertToString() {
			String outputString = "";
	        outputString += "comment=" + get("comment") + "///";
            outputString += "compress=" + get("compress") + "///";
	        outputString += "metadata=" + get("metadata") + "///";
	        outputString += "msg=" + get("msg") + "///";
       		outputString += "text=" + get("text") + "///";
      		outputString += "data=" + get("data");
      		return outputString;
		}
	}
	
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


	protected static enum Period {
		FromNormals1971_2000("period=1971_2000"),
		FromNormals1981_2010("period=1981_2010");
		
		String parsedQuery;
		
		Period(String parsedRequest) {
			this.parsedQuery = parsedRequest;
		}
	}
	
	static final List<Month> AllMonths = Arrays.asList(Month.values());
	
		
	protected static enum Variable {	// TODO complete this
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
	
	protected static enum Month {
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
	
	private static final InetSocketAddress REpiceaAddress = new InetSocketAddress("144.172.156.5", 5000);
	private static final InetSocketAddress LANAddress = new InetSocketAddress("192.168.0.194", 5000);

	/**
	 * Retrieves the normals and compiles the mean or sum over some months. 
	 * @param variables the variables to be retrieved and compiled
	 * @param locations the locations
	 * @param averageOverTheseMonths the months over which the mean or sum is to be calculated. If empty or null the 
	 * method returns the monthly averages.
	 * @return a Map with the locations as keys and maps as values.
	 * @throws IOException
	 */
	public static Map<PlotLocation, Map> getNormals(Period period, List<Variable> variables, List<PlotLocation> locations, List<Month> averageOverTheseMonths) throws IOException {
		Map<PlotLocation, Map> outputMap = new HashMap<PlotLocation, Map>();
		
		String variablesQuery = "";
		for (Variable v : variables) {
			variablesQuery += v.name();
			if (variables.indexOf(v) < variables.size() - 1) {
				variablesQuery += "%20";
			}
		}

		String fieldSeparator = ",";
		
		for (PlotLocation location : locations) {
			String query = "";
			query += "lat=" + location.getLatitudeDeg();
			query += "&long=" + location.getLongitudeDeg();
			if (!Double.isNaN(location.getElevationM())) {
				query += "&elev=" + location.getElevationM();
			}
			query += "&var=" + variablesQuery ;
			query += "&compress=0";	// compression is disabled by default
			query += "&" + period.parsedQuery;
			
			String urlString = "http://" + REpiceaAddress.getHostName() + ":" + REpiceaAddress.getPort() + "/" + NORMAL_API + "?" + query;
			
			URL bioSimURL = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) bioSimURL.openConnection();
			int code = connection.getResponseCode();
			if (code != 202) {	// means it is connected
				String innerURLString = "http://" + LANAddress.getHostName() + ":" + LANAddress.getPort() + "/" + NORMAL_API + "?" + query;
				bioSimURL = new URL(innerURLString);
				connection = (HttpURLConnection) bioSimURL.openConnection();
				code = connection.getResponseCode();
				if (code != 200) {	// means it is connected
					throw new IOException("Unable to access BioSIM from internet or the LAN!");
				}				
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			Map<Variable, Integer> fieldIndices = new HashMap<Variable,Integer>();
			int i = 0;
			MonthMap monthMap = new MonthMap();
			while ((inputLine = in.readLine()) != null) {
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
			in.close();
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
	public static Map<PlotLocation, Map> getMonthlyNormals(Period period, List<Variable> variables, List<PlotLocation> locations) throws IOException {
		return getNormals(period, variables, locations, null);
	}

	/**
	 * Retrieves the yearly normals.
	 * @param variables the variables to be retrieved and compiled
	 * @param locations the locations
	 * @return a Map with the locations as keys and maps as values.
	 * @throws IOException
	 */
	public static Map<PlotLocation, Map> getAnnualNormals(Period period, List<Variable> variables, List<PlotLocation> locations) throws IOException {
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
	public static Map<PlotLocation, String> getGeneratedClimate(int fromYr, int toYr, List<Variable> variables, List<PlotLocation> locations) throws IOException {
		boolean compress = false; // disabling compression by default
		Map<PlotLocation, String> outputMap = new HashMap<PlotLocation, String>();
		
		String variablesQuery = "";
		for (Variable v : variables) {
			variablesQuery += v.name();
			if (variables.indexOf(v) < variables.size() - 1) {
				variablesQuery += "%20";
			}
		}

		for (PlotLocation location : locations) {
			@SuppressWarnings("unused")
			Object value = null;
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
			
			String urlString = "http://" + REpiceaAddress.getHostName() + ":" + REpiceaAddress.getPort() + "/" + GENERATOR_API + "?" + query;
			
			URL bioSimURL = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) bioSimURL.openConnection();
			int code = connection.getResponseCode();
			if (code != 202) {	// means it is connected
				String innerURLString = "http://" + LANAddress.getHostName() + ":" + LANAddress.getPort() + "/" + GENERATOR_API + "?"  + query;
				bioSimURL = new URL(innerURLString);
				connection = (HttpURLConnection) bioSimURL.openConnection();
				code = connection.getResponseCode();
				if (code != 200) {	// means it is connected
					throw new IOException("Unable to access BioSIM from internet or the LAN!");
				}				
			}
			String serverReply = getStringFromConnection(connection);
			outputMap.put(location, serverReply);
		}
		return outputMap;
	}
	
	
	private static String getStringFromConnection(HttpURLConnection connection) throws IOException {
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

	public static Map<PlotLocation, String> applyModel(String modelName, Map<PlotLocation, String> teleIORefs) throws IOException {
		boolean compress = false; // disabling compression
		Map<PlotLocation, String> outputMap = new HashMap<PlotLocation, String>();
		for (PlotLocation location : teleIORefs.keySet()) {
			String query = "";
			query += "model=" + modelName;
			if (compress) {
				query += "&compress=1";
			} else {
				query += "&compress=0";
			}
			String teleIORef = teleIORefs.get(location);
			query += "&wgout=" + teleIORef;		
			
			String urlString = "http://" + REpiceaAddress.getHostName() + ":" + REpiceaAddress.getPort() + "/" + MODEL_API + "?" + query;
			
			URL bioSimURL = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) bioSimURL.openConnection();
			int code = connection.getResponseCode();
			if (code != 202) {	// means it is connected
				String innerURLString = "http://" + LANAddress.getHostName() + ":" + LANAddress.getPort() + "/" + MODEL_API + "?"  + query;
				bioSimURL = new URL(innerURLString);
				connection = (HttpURLConnection) bioSimURL.openConnection();
				code = connection.getResponseCode();
				if (code != 200) {	// means it is connected
					throw new IOException("Unable to access BioSIM from internet or the LAN!");
				}				
			}
			String serverReply = getStringFromConnection(connection);
			outputMap.put(location, serverReply);
		}
		return outputMap;

	}
	

	public static void main(String[] args) throws IOException {
		List<PlotLocation> locations = new ArrayList<PlotLocation>();
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
		Map<PlotLocation, String> teleIORefs = BioSimClient2.getGeneratedClimate(2018, 2019, var, locations);
		nbSecs = (System.currentTimeMillis() - initialTime) * .001;
		System.out.println("Elapsed time = " + nbSecs + " size = " + teleIORefs.size());
		
		initialTime = System.currentTimeMillis();
		Map<PlotLocation, String> replies = BioSimClient2.applyModel("DegreeDay_Annual", teleIORefs);
		nbSecs = (System.currentTimeMillis() - initialTime) * .001;
		System.out.println("Elapsed time = " + nbSecs + " size = " + replies.size());
	}

}
