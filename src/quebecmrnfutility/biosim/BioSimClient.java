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
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import repicea.net.server.BasicClient;

/**
 * This class enables a client for the Biosim server at rouge-epicea.dyndns.org. 
 * @author Mathieu Fortin - January 2016
 */
public class BioSimClient extends BasicClient {

	
	public BioSimClient() throws UnknownHostException, IOException, ClassNotFoundException {
		super(new InetSocketAddress("rouge-epicea.dyndns.org", 18000));
	}

	/**
	 * This method sends a list of plot locations (latitude, longitude and elevation) to the server which returns
	 * a list of Double[2] (mean annual temperature, mean annual precipitation)
	 * @param obj a List of PlotLocation instances
	 * @return a List of mean annual temperatures and precipitations
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<ClimateVariables> getClimateVariables(List<PlotLocation> obj) {
		return (List) super.processRequest(obj);
	}
	
}
