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

import org.junit.Assert;
import org.junit.Test;

import repicea.util.JarUtility;

public class MrnfForesttoolsAppVersionTest {

	@Test
	public void compileAndRetrieveRevision() {
		String build = MrnfForesttoolsAppVersion.getInstance().getBuild();
		System.out.println("Build is: " + build);
		if (JarUtility.isEmbeddedInJar(MrnfForesttoolsAppVersion.class)) {
			try {
				Integer.parseInt(build);
			} catch (NumberFormatException e) {
				Assert.fail("The revision cannot be parsed to an integer!");
			}
		} else {
			Assert.assertEquals("Unknown", build);
		}
	}
}