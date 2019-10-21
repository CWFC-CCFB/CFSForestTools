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

import java.util.HashMap;
import java.util.Map;

import canforservutility.biodiversity.indices.DiversityIndices.BetaIndex;
import canforservutility.biodiversity.indices.DiversityIndices.DiversityIndex;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.SimpleEstimate;

@SuppressWarnings("rawtypes")
public class DiversityIndicesEstimates {

	private final Map<DiversityIndex, SimpleEstimate> innerMap;
	
	public DiversityIndicesEstimates() {
		super();
		innerMap = new HashMap<DiversityIndex, SimpleEstimate>();
	}

	public void setAlphaDiversity(SimpleEstimate alpha) {
		innerMap.put(DiversityIndex.Alpha, alpha);
	}
	
	public SimpleEstimate getAlphaDiversity() {
		if (innerMap.containsKey(DiversityIndex.Alpha)) {
			return innerMap.get(DiversityIndex.Alpha);
		} else {
			return null;
		}
	}
	
	public void setGammaDiversity(SimpleEstimate gamma) {
		innerMap.put(DiversityIndex.Gamma, gamma);
	}

	public SimpleEstimate getGammaDiversity() {
		if (innerMap.containsKey(DiversityIndex.Gamma)) {
			return innerMap.get(DiversityIndex.Gamma);
		} else {
			return null;
		}
	}
	
	public void setBetaDiversity(SimpleEstimate beta) {
		innerMap.put(DiversityIndex.Beta, beta);
	}

	public SimpleEstimate getBetaDiversity() {
		if (innerMap.containsKey(DiversityIndex.Beta)) {
			return innerMap.get(DiversityIndex.Beta);
		} else {
			return null;
		}
	}
	
	public SimpleEstimate getBetaDiversity(BetaIndex ind) {
		Estimate est = getBetaDiversity();
		if (est != null && ind.ordinal() < est.getMean().m_iRows) {
			SimpleEstimate se = new SimpleEstimate(est.getMean().getSubMatrix(ind.ordinal(), ind.ordinal(), 0, 0),
					est.getVariance().getSubMatrix(ind.ordinal(), ind.ordinal(), ind.ordinal(), ind.ordinal()));
			return se;
		} else {
			return null;
		}
	}
	
	SimpleEstimate getEstimate(DiversityIndex ind) {
		switch(ind) {
		case Alpha:
			return getAlphaDiversity();
		case Beta:
			return getBetaDiversity();
		case Gamma:
			return getGammaDiversity();
		default:
			return null;
		}
	}
}
