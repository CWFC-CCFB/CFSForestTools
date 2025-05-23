/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.volumemodels.loggradespetro;

import java.util.ArrayList;
import java.util.List;

class Plot {

	private final List<PetroGradeTreeImpl> trees;
	private final int id;
	
	Plot(int id) {
		this.id = id;
		trees = new ArrayList<PetroGradeTreeImpl>();
	}
	
	void addTree(PetroGradeTreeImpl tree) {
		trees.add(tree);
	}
	
	List<PetroGradeTreeImpl> getTrees() {
		return trees;
	}
	
	@Override
	public String toString() {
		return "Plot " + id;
	}
}
