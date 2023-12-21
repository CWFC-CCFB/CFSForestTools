/*
 * This file is part of the CFSForesttools library
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
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
package quebecmrnfutility.app;

import repicea.app.REpiceaAppVersion;

/**
 * The MrnfForesttoolsJAVSVNAppVersion class reads the file that 
 * contains the revision of the project.
 * @author Mathieu Fortin  - February 2021
 */
public class CFSForesttoolsAppVersion extends REpiceaAppVersion {

	private static CFSForesttoolsAppVersion SINGLETON;

	private CFSForesttoolsAppVersion() {
		super();
	}

	/**
	 * This method returns the singleton instance of this class which can be requested
	 * to return the revision number of this version.
	 * @return the singleton instance of the REpiceaJARSVNAppVersion class
	 */
	public static CFSForesttoolsAppVersion getInstance() {
		if (SINGLETON == null) {
			SINGLETON = new CFSForesttoolsAppVersion();
		}
		return SINGLETON;
	}

}

