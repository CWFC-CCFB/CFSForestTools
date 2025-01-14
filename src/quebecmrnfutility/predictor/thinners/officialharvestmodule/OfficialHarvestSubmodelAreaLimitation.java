/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2019-2021 Her Majesty the Queen in right of Canada
 * Author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import java.awt.Container;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import repicea.gui.REpiceaUIObjectWithParent;

@SuppressWarnings("serial")
public class OfficialHarvestSubmodelAreaLimitation implements REpiceaUIObjectWithParent, Serializable {
	
	protected final Map<Enum<?>, Double> areaLimitationMap;
	private transient OfficialHarvestSubmodelAreaLimitationPanel guiInterface;
	
	OfficialHarvestSubmodelAreaLimitation(Enum<?>[] treatments) {
		areaLimitationMap = new TreeMap<Enum<?>, Double>();
		for (Enum<?> treatment : treatments) {
			areaLimitationMap.put(treatment, 0d);
		}
	}

	@Override
	public OfficialHarvestSubmodelAreaLimitationPanel getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new OfficialHarvestSubmodelAreaLimitationPanel(this);
		}
		return guiInterface;
	}

	@Override
	public boolean isVisible() {
		if (guiInterface != null)
			return guiInterface.isVisible();
		else return false;
	}
}
