/*
 * This file is part of the mrnf-foresttools library.
 *
 * Copyright (C) 2021 Her Majesty the Queen in right of Canada
 * author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service
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
package quebecmrnfutility.predictor.volumemodels.merchantablevolume;

/**
 * A class to facilitate the use of the deterministic version of the model in R.
 * @author Mathieu Fortin - August 2021
 */
public final class VolumableStandImpl implements VolumableStand {

	/**
	 * Constructor to be used with deterministic version of the MerchantableVolumePredictor.
	 */
	public VolumableStandImpl() {};
	
	
	@Override
	public String getSubjectId() {
		return null;
	}

	@Override
	public int getMonteCarloRealizationId() {
		return 0;
	}

	@Override
	public String getCruiseLineID() {
		return null;
	}

}
