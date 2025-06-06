/*
 * This file is part of the CFSForesttools library
 *
 * Author Mathieu Fortin - Canadian Forest Service
 * Copyright (C) 2020 Her Majesty the Queen in right of Canada
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
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
package quebecmrnfutility.simulation.covariateproviders.plotlevel;

import java.util.HashMap;
import java.util.Map;

import repicea.simulation.covariateproviders.plotlevel.DrainageGroupProvider;

/**
 * An interface to ensure the instance can provide its drainage class.
 * @author Mathieu Fortin - 2020
 */
public interface QcDrainageClassProvider extends DrainageGroupProvider {
	
	public static enum QcDrainageClass {
		C0(DrainageGroup.Xeric),
		C1(DrainageGroup.Xeric),
		C2(DrainageGroup.Mesic),
		C3(DrainageGroup.Mesic),
		C4(DrainageGroup.Subhydric),
		C5(DrainageGroup.Hydric),
		C6(DrainageGroup.Hydric);

		final DrainageGroup category;
		
		static Map<String, QcDrainageClass> DrainageMap;
		
		QcDrainageClass(DrainageGroup category) {
			this.category = category;
		}
		
		private static Map<String, QcDrainageClass> getDrainageMap() {
			if (DrainageMap == null) {
				DrainageMap = new HashMap<String, QcDrainageClass>();
				for (QcDrainageClass dc : QcDrainageClass.values()) {
					DrainageMap.put(dc.name().substring(1, 2), dc);
				}
			}
			return DrainageMap;
		}

		/**
		 * Return true if the drainage string can be converted into a 
		 * QcDrainageClass enum.
		 * 
		 * @param drainageStr the string which should be one of the following: 0, 1, 2, 3, 4, 5, 6 
		 * @return a boolean if the string can be converted into a drainage class
		 */
		public static boolean isEligibleDrainageString(String drainageStr) {
			return getDrainageMap().containsKey(drainageStr);
		}
		
		/**
		 * Parse the string into a QcDrainageClass enum.
		 * @param drainageStr the string which should be one of the following: 0, 1, 2, 3, 4, 5, 6 
		 * @return may return null if the string is not eligible.
		 */
		public static QcDrainageClass getDrainageClassFromString(String drainageStr) {
			return getDrainageMap().get(drainageStr);
		}
		
	}
	
	public QcDrainageClass getDrainageClass();
	
	@Override
	public default DrainageGroup getDrainageGroup() {
		if (getDrainageClass() != null) {
			return getDrainageClass().category;
		} else {
			return null;
		}
	}
	
}
