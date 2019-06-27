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

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import repicea.net.server.BasicClient;

/**
 * This class enables a client for the Biosim server at rouge-epicea.dyndns.org. 
 * @author Mathieu Fortin - January 2016
 */
public class BioSimClient extends BasicClient {

	protected static final Map<BioSimVersion, Map<String, ClimateVariables>> RecordedClimateVariables = new ConcurrentHashMap<BioSimVersion, Map<String, ClimateVariables>>();
	static {
		RecordedClimateVariables.put(BioSimVersion.VERSION_1971_2000, new ConcurrentHashMap<String, ClimateVariables>());
		RecordedClimateVariables.put(BioSimVersion.VERSION_1981_2010, new ConcurrentHashMap<String, ClimateVariables>());
	}
	

	boolean byPassConnectionForTesting = false;
	
	private static InetSocketAddress LANAddress = new InetSocketAddress("192.168.0.194", 18000);
	
	private static boolean isLAN = false;

	public static enum BioSimVersion {
		VERSION_1971_2000, 
		VERSION_1981_2010;
	}
	
	@SuppressWarnings("serial")
	public static class Request implements Serializable {
		private final BioSimVersion version;
		private final List<PlotLocation> locations;
		
		private Request(BioSimVersion version, List<PlotLocation> locations) {
			this.version = version;
			this.locations = locations;
		}
		
		public BioSimVersion getBioSimVersion() {return version;}
		public List<PlotLocation> getLocations() {return locations;}
	}

	private final BioSimVersion version;

	/**
	 * Default constructor.
	 * @param version a BioSimVersion enum
	 * @throws BasicClientException
	 */
	protected BioSimClient(BioSimVersion version) throws BasicClientException {
		this(new InetSocketAddress("repicea.dyndns.org", 18000), version); // 20 sec before timeout.
	}

	/**
	 * Complex constructor.
	 * @param address an InetSocketAddress instance
	 * @param version a BioSimVersion enum
	 * @throws BasicClientException
	 */
	protected BioSimClient(InetSocketAddress address, BioSimVersion version) throws BasicClientException {
		super(address, 20); // 20 sec before timeout.
		if (version == null) {
			this.version = BioSimVersion.VERSION_1971_2000;
		} else {
			this.version = version;
		}
	}

	/**
	 * Returns an instance of BioSimClient. It first tries to connect to repicea.dyndns.org. If 
	 * it fails, it tries to connect locally. If it fails again, a BasicClientException is thrown.
	 * @param version a BioSimVersion enum
	 * @return a BioSimClient instance
	 * @throws BasicClientException if the remote and the local connections both fail
	 */
	public static BioSimClient getBioSimClient(BioSimVersion version) throws BasicClientException {
		BioSimClient client = null;
		if (isLAN) {
			client = new BioSimClient(BioSimClient.LANAddress, version);
		} else {
			try {
				client = new BioSimClient(BioSimVersion.VERSION_1971_2000);
				isLAN = false;
			} catch (BasicClientException e) {
				client = new BioSimClient(BioSimClient.LANAddress, version);
				isLAN = true;
			}
		} 
		return client;
	}
	
	
	/**
	 * This method sends a list of plot locations (latitude, longitude and elevation) to the server which returns
	 * a list of Double[2] (mean annual temperature, mean annual precipitation)
	 * @param obj a List of PlotLocation instances
	 * @return a List of mean annual temperatures and precipitations
	 * @throws BasicClientException if an exception occurs while sending and processing the request
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<ClimateVariables> getClimateVariables(List<PlotLocation> obj) throws BasicClientException {
		List<ClimateVariables> climVar = new ArrayList<ClimateVariables>();
		List<PlotLocation> plotLocationsToBeSent = new ArrayList<PlotLocation>();
		for (PlotLocation plotLocation : obj) {
			if (RecordedClimateVariables.get(version).containsKey(plotLocation.toString())) {
				climVar.add(RecordedClimateVariables.get(version).get(plotLocation.toString()));
			} else {
				plotLocationsToBeSent.add(plotLocation);
			}
		}
		
		List<ClimateVariables> climVarReceived;
		if (byPassConnectionForTesting || plotLocationsToBeSent.isEmpty()) {
			climVarReceived = new ArrayList<ClimateVariables>();
		} else {
			climVarReceived = (List) super.processRequest(new Request(version, plotLocationsToBeSent));
		}
		
		for (ClimateVariables cv : climVarReceived) {
			for (int i = 0; i < plotLocationsToBeSent.size(); i++) {
				PlotLocation pl = plotLocationsToBeSent.get(i);
				if (cv.getPlotId().equals(pl.getPlotId())) {
					RecordedClimateVariables.get(version).put(pl.toString(), cv);
					plotLocationsToBeSent.remove(pl);
					break;
				}
			}
		}
		climVar.addAll(climVarReceived);
		return climVar;
	}

//	/*
//	 * For extended visibility (non-Javadoc)
//	 * @see repicea.net.server.BasicClient#setBypassTimeout(boolean)
//	 */
//	@Override
//	public void setBypassTimeout(boolean bypass) {
//		super.setBypassTimeout(bypass);
//	}

	
	
	
	
	
	
	
	
}
