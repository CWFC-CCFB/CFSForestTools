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
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class enables a client for the Biosim server at repicea.dyndns.org. 
 * @author Mathieu Fortin - October 2019
 */
public class BioSimClient2 {

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


	protected static enum Source {
		FromNormals1981_2010("source=FromNormals&from=1981&to=2010");
		
		String parsedQuery;
		
		Source(String parsedRequest) {
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
	public static Map<PlotLocation, Map> getNormals(List<Variable> variables, List<PlotLocation> locations, List<Month> averageOverTheseMonths) throws IOException {
		Map<PlotLocation, Map> outputMap = new HashMap<PlotLocation, Map>();
		Source source = Source.FromNormals1981_2010;
		
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
			query += "lat=" + location.getLatitudeDeg() + "&";
			query += "long=" + location.getLongitudeDeg() + "&";
			query += "var=" + variablesQuery + "&";
			query += source.parsedQuery;
			
			String urlString = "http:/" + LANAddress.toString() + "/BioSim?" + query;
			
			URL bioSimURL = new URL(urlString);
			URLConnection connection = bioSimURL.openConnection();
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
	public static Map<PlotLocation, Map> getMonthlyNormals(List<Variable> variables, List<PlotLocation> locations) throws IOException {
		return getNormals(variables, locations, null);
	}

	/**
	 * Retrieves the yearly normals.
	 * @param variables the variables to be retrieved and compiled
	 * @param locations the locations
	 * @return a Map with the locations as keys and maps as values.
	 * @throws IOException
	 */
	public static Map<PlotLocation, Map> getAnnualNormals(List<Variable> variables, List<PlotLocation> locations) throws IOException {
		return getNormals(variables, locations, AllMonths);
	}
	
	
	public static void main(String[] args) throws IOException {
		List<PlotLocation> locations = new ArrayList<PlotLocation>();
		for (int i = 0; i < 1000; i++) {
			PlotLocation loc = new PlotLocation(((Integer) i).toString(),
					45,
					-70,
					300);
			locations.add(loc);
		}
		List<Variable> var = new ArrayList<Variable>();
		var.add(Variable.TN);
		var.add(Variable.TX);
		var.add(Variable.P);
		long initialTime;
		double nbSecs;
		
//		initialTime = System.currentTimeMillis();
//		Map output = BioSimClient2.getMonthlyNormals(var, locations);
//		nbSecs = (System.currentTimeMillis() - initialTime) * .001;
//		System.out.println("Elapsed time = " + nbSecs + " size = " + output.size());
		initialTime = System.currentTimeMillis();
		Map output2 = BioSimClient2.getAnnualNormals(var, locations);
		nbSecs = (System.currentTimeMillis() - initialTime) * .001;
		System.out.println("Elapsed time = " + nbSecs + " size = " + output2.size());
	}
	
	
	
	
	
	
}
