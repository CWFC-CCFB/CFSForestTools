/*
 * This file is part of the QuebecMRNFModule for Capsis 
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

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import repicea.net.server.BasicClient;

/**
 * This class enables a client for the Biosim server at rouge-epicea.dyndns.org. 
 * @author Mathieu Fortin - January 2016
 */
public class BioSimClient extends BasicClient {

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
	
	public BioSimClient(BioSimVersion version) throws UnknownHostException, IOException, ClassNotFoundException {
		super(new InetSocketAddress("rouge-epicea.dyndns.org", 18000));
		if (version == null) {
			this.version = BioSimVersion.VERSION_1971_2000;
		} else {
			this.version = version;
		}
	}

	/**
	 * This method sends a list of plot locations (latitude, longitude and elevation) to the server which returns
	 * a list of Double[2] (mean annual temperature, mean annual precipitation)
	 * @param obj a List of PlotLocation instances
	 * @return a List of mean annual temperatures and precipitations
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<ClimateVariables> getClimateVariables(List<PlotLocation> obj) throws IOException {
		return (List) super.processRequest(new Request(version, obj));
	}
	
}
