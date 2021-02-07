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

import repicea.app.AbstractAppVersionCompiler;
import repicea.util.ObjectUtility;

public class MrnfForesttoolsJARSVNAppVersionCompiler extends AbstractAppVersionCompiler {

	private static final String APP_URL = "http://svn.code.sf.net/p/mrnfforesttools/code/trunk";
	private static String Version_Filename = ObjectUtility.getPackagePath(MrnfForesttoolsJARSVNAppVersionCompiler.class).replace("bin", "src") + MrnfForesttoolsJARSVNAppVersion.ShortFilename;
	
	public MrnfForesttoolsJARSVNAppVersionCompiler() {
		super();
	}
	
	public static void main(String args[]) {
		MrnfForesttoolsJARSVNAppVersionCompiler compiler = new MrnfForesttoolsJARSVNAppVersionCompiler();
		try {
			compiler.createRevisionFile(APP_URL, Version_Filename);
			System.out.println("Revision file successfully updated!");
		} catch (Exception e) {
			System.out.println("Error while updating revision file!");
		}
	}

}
