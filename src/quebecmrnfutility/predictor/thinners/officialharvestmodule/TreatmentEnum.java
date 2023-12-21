/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2012 Gouvernement du Quebec - Rouge-Epicea
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

import repicea.util.FullNameEnum;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * This interface just combines the TextableEnum and FullNameEnum interfaces.
 * @author Mathieu Fortin - July 2012
 */
public interface TreatmentEnum extends TextableEnum, FullNameEnum {

	/**
	 * This method is already included in the Enum class.
	 * @return a String
	 */
	public String name();

}
