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

public interface QcForestRegionProvider {

	public static enum QcForestRegion {
		BasStLaurentGaspesie,
		SaguenayLacSaintJean,
		Quebec,
		TroisRivieres,
		Estrie,
		Montreal,
		Outaouais,
		AbitibiTemiscamingue,
		CoteNord;
		
		private static Map<Integer, QcForestRegion> RegionMap;
		
		/**
		 * This method returns the QuebecForestRegion enum associated to the region code parameter
		 * @param regionCode an integer
		 * @return a QuebecForestRegion enum 
		 */
		public static QcForestRegion getRegion(int regionCode) {
			if (RegionMap == null) {
				RegionMap = new HashMap<Integer, QcForestRegion>();
				for (QcForestRegion region : QcForestRegion.values()) {
					RegionMap.put(region.getRegionCode(), region);
				}
			}
			return RegionMap.get(regionCode);
		}
		
		/**
		 * This method returns the region code. 
		 * @return an integer
		 */
		public int getRegionCode() {return this.ordinal() + 1;};
	}
	
	/**
	 * This method ensures the instance can return a QuebecForestRegion enum representing the administrative region where
	 * it is located.
	 * @return a QuebecForestRegion enum
	 */
	public QcForestRegion getQuebecForestRegion();
	

}
