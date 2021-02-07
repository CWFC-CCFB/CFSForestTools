/*
 * This file is part of the mrnf-foresttools library
 *
 * Author Mathieu Fortin - Canadian Forest Service
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
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
package quebecmrnfutility.app;

import repicea.app.AbstractAppVersion;
import repicea.util.ObjectUtility;

/**
 * The MrnfForesttoolsJAVSVNAppVersion class reads the file that 
 * contains the revision of the project.
 * @author Mathieu Fortin  - February 2021
 */
public class MrnfForesttoolsJARSVNAppVersion extends AbstractAppVersion {

	private static MrnfForesttoolsJARSVNAppVersion SINGLETON;

	private static final String AppName = "mrnf_foresttools";

	protected static final String ShortFilename = AppName + "_revision.csv";
	protected static final String CompleteFilename = ObjectUtility.getRelativePackagePath(MrnfForesttoolsJARSVNAppVersion.class) + ShortFilename;

	private MrnfForesttoolsJARSVNAppVersion() {
		super(AppName, CompleteFilename);
	}

	/**
	 * This method returns the singleton instance of this class which can be requested
	 * to return the revision number of this version.
	 * @return the singleton instance of the REpiceaJARSVNAppVersion class
	 */
	public static MrnfForesttoolsJARSVNAppVersion getInstance() {
		if (SINGLETON == null) {
			SINGLETON = new MrnfForesttoolsJARSVNAppVersion();
		}
		return SINGLETON;
	}


	public static void main(String[] args) {
		String version = MrnfForesttoolsJARSVNAppVersion.getInstance().getRevision();
		System.out.println(version);
	}

}

