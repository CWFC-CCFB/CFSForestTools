/*
 * This file is part of the mrnf-foresttools library
 *
 * Copyright (C) 2016 Mathieu Fortin - LERFoB AgroParisTech/INRA
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.stats.StatisticalUtility;

/**
 * This class enables a client for the Biosim server at repicea.dyndns.org. 
 * @author Mathieu Fortin - January 2016 (Updated October 2019)
 */
public class BioSimClient2 {

	protected static enum Source {
		FromNormals1981_2010("source=FromNormals&from=1981&to=2010");
		
		String parsedQuery;
		
		Source(String parsedRequest) {
			this.parsedQuery = parsedRequest;
		}
	}

		
	public static enum Variable {
		TN("TMIN_MN", "min air temperature"),
		T("", "air temperature"),
		TX("TMAX_MN", "max air temperature"),
		P("PRCP_TT", "precipitation"),
		TD("TDEX_MN", "temperature dew point"),
		H("", "humidity"),
		WS("", "wind speed"),
		WD("", "wind direction"),
		R("", "solar radiation"),
		Z("", "atmospheric pressure"),
		S("", "snow precipitation"),
		SD("", "snow depth accumulation"),
		SWE("", "snow water equivalent"),
		WS2("", "wind speed at 2 m");
		
		String description;
		String fieldName;
		
		Variable(String fieldName, String description) {
			this.fieldName = fieldName;
			this.description = description;
		}
		
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
	
//	private static boolean isLAN = false;
//
//	public static enum BioSimVersion {
//		VERSION_1971_2000, 
//		VERSION_1981_2010;
//	}
	

//	/**
//	 * Default constructor.
//	 * @param version a BioSimVersion enum
//	 * @throws BasicClientException
//	 */
//	protected BioSimClient2(BioSimVersion version) throws BasicClientException {
//		this(new InetSocketAddress("repicea.dyndns.org", 18000), version); // 20 sec before timeout.
//	}
//
//	/**
//	 * Complex constructor.
//	 * @param address an InetSocketAddress instance
//	 * @param version a BioSimVersion enum
//	 * @throws BasicClientException
//	 */
//	protected BioSimClient2(InetSocketAddress address, BioSimVersion version) throws BasicClientException {
//		super(address, 20); // 20 sec before timeout.
//		if (version == null) {
//			this.version = BioSimVersion.VERSION_1971_2000;
//		} else {
//			this.version = version;
//		}
//	}

//	/**
//	 * Returns an instance of BioSimClient. It first tries to connect to repicea.dyndns.org. If 
//	 * it fails, it tries to connect locally. If it fails again, a BasicClientException is thrown.
//	 * @param version a BioSimVersion enum
//	 * @return a BioSimClient instance
//	 * @throws BasicClientException if the remote and the local connections both fail
//	 */
//	public static BioSimClient2 getBioSimClient(BioSimVersion version) throws BasicClientException {
//		BioSimClient2 client = null;
//		if (isLAN) {
//			client = new BioSimClient2(BioSimClient2.LANAddress, version);
//		} else {
//			try {
//				client = new BioSimClient2(BioSimVersion.VERSION_1971_2000);
//				isLAN = false;
//			} catch (BasicClientException e) {
//				client = new BioSimClient2(BioSimClient2.LANAddress, version);
//				isLAN = true;
//			}
//		} 
//		return client;
//	}
	
	public static Map<PlotLocation, Map<Month, Map<Variable, Double>>> getNormals(List<Variable> variables, List<PlotLocation> locations) throws IOException {
		Map<PlotLocation, Map<Month, Map<Variable, Double>>> outputMap = new HashMap<PlotLocation, Map<Month, Map<Variable, Double>>>();
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
			while ((inputLine = in.readLine()) != null) {
				String[] str = inputLine.split(fieldSeparator);
				if (i==0) {
					List<String> fieldNames = Arrays.asList(str);
					for (Variable v : variables) {
						fieldIndices.put(v, fieldNames.indexOf(v.fieldName));
					}
					outputMap.put(location, new HashMap<Month, Map<Variable, Double>>());
				} else {
					int monthIndex = Integer.parseInt(str[0]) - 1;
					Month m = Month.values()[monthIndex];
					outputMap.get(location).put(m, new HashMap<Variable, Double>());
					for (Variable v : variables) {
						double value = Double.parseDouble(str[fieldIndices.get(v)]);
						outputMap.get(location).get(m).put(v, value);
					}
				}
				i++;
			}
			in.close();
			
		}
		return outputMap;
	}
	
//	/**
//	 * This method sends a list of plot locations (latitude, longitude and elevation) to the server which returns
//	 * a list of Double[2] (mean annual temperature, mean annual precipitation)
//	 * @param obj a List of PlotLocation instances
//	 * @return a List of mean annual temperatures and precipitations
//	 * @throws BasicClientException if an exception occurs while sending and processing the request
//	 */
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	public List<ClimateVariables> getClimateVariables(List<PlotLocation> obj) throws BasicClientException {
//		List<ClimateVariables> climVar = new ArrayList<ClimateVariables>();
//		List<PlotLocation> plotLocationsToBeSent = new ArrayList<PlotLocation>();
//		for (PlotLocation plotLocation : obj) {
//			if (RecordedClimateVariables.get(version).containsKey(plotLocation.toString())) {
//				climVar.add(RecordedClimateVariables.get(version).get(plotLocation.toString()));
//			} else {
//				plotLocationsToBeSent.add(plotLocation);
//			}
//		}
//		
//		List<ClimateVariables> climVarReceived;
//		if (byPassConnectionForTesting || plotLocationsToBeSent.isEmpty()) {
//			climVarReceived = new ArrayList<ClimateVariables>();
//		} else {
//			climVarReceived = (List) super.processRequest(new Request(version, plotLocationsToBeSent));
//		}
//		
//		for (ClimateVariables cv : climVarReceived) {
//			for (int i = 0; i < plotLocationsToBeSent.size(); i++) {
//				PlotLocation pl = plotLocationsToBeSent.get(i);
//				if (cv.getPlotId().equals(pl.getPlotId())) {
//					RecordedClimateVariables.get(version).put(pl.toString(), cv);
//					plotLocationsToBeSent.remove(pl);
//					break;
//				}
//			}
//		}
//		climVar.addAll(climVarReceived);
//		return climVar;
//	}

//	/*
//	 * For extended visibility (non-Javadoc)
//	 * @see repicea.net.server.BasicClient#setBypassTimeout(boolean)
//	 */
//	@Override
//	public void setBypassTimeout(boolean bypass) {
//		super.setBypassTimeout(bypass);
//	}

	
	public static void main(String[] args) throws IOException {
		List<PlotLocation> locations = new ArrayList<PlotLocation>();
		for (int i = 0; i < 10; i++) {
			PlotLocation loc = new PlotLocation(((Integer) i).toString(),
					45 + StatisticalUtility.getRandom().nextGaussian(),
					-70 + StatisticalUtility.getRandom().nextGaussian(),
					300 + StatisticalUtility.getRandom().nextGaussian());
			locations.add(loc);
		}
		List<Variable> var = new ArrayList<Variable>();
		var.add(Variable.TN);
		var.add(Variable.TX);
		Map output = BioSimClient2.getNormals(var, locations);
		int u = 0;
	}
	
	
	
	
	
	
}
