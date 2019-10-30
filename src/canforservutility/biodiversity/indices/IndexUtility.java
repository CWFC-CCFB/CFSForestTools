/*
 * This file is part of the mrnf-foresttools library
 *
 * Copyright (C) 2019 Mathieu Fortin - Canadian Forest Service
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
package canforservutility.biodiversity.indices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class IndexUtility {

	@SuppressWarnings("serial")
	static class ValidatedHashMap<K,V> extends HashMap<K,V> {
		
		ValidatedHashMap<K,V> getMapWithoutThisKey(K key) {
			ValidatedHashMap<K,V> newMap = new ValidatedHashMap<K,V>();
			for (K k : keySet()) {
				if (!k.equals(key)) {
					newMap.put(k, get(k));
				}
			}
			return newMap;
		}
		
		ValidatedHashMap<K,V> getMapWithoutTheseKeys(List<K> keys) {
			ValidatedHashMap<K,V> newMap = new ValidatedHashMap<K,V>();
			for (K k : keySet()) {
				if (!keys.contains(k)) {
					newMap.put(k, get(k));
				}
			}
			return newMap;
		}
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static List getUniqueSpeciesList(Map<String, List> oMap) {
		List speciesList = new ArrayList();
		for (List innerList : oMap.values()) {
			for (Object sp : innerList) {
				if (!speciesList.contains(sp)) {
					speciesList.add(sp);
				}
			}
		}
		return speciesList;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static ValidatedHashMap<String, List> validateMap(Map<String, List> oMap) {
		if (oMap instanceof ValidatedHashMap) {
			return (ValidatedHashMap) oMap;
		} else {
			ValidatedHashMap<String, List> newMap = new ValidatedHashMap<String, List>();
			for (String key : oMap.keySet()) {
				List value = new ArrayList();
				newMap.put(key, value);
				for (Object s : oMap.get(key)) {
					if (!value.contains(s)) {
						value.add(s);
					}
				}
			}
			return newMap;
		}
	}

}
