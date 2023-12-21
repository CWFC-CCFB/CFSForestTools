/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2009-2014 Mathieu Fortin for Rouge-Epicea
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
package quebecmrnfutility.predictor.matapedia;

import java.util.ArrayList;
import java.util.List;

public class MatapediaStandImpl implements MatapediaStand {

	private boolean upcomingSBW;
	private boolean sbwPrevious;
	private String subjectID;
	private int monteCarloRealization;
	private List<MatapediaTree> trees;
	
	public MatapediaStandImpl(boolean upcomingSBW, boolean sbwPrevious, int subjectID, int monteCarloRealization) {
		this.upcomingSBW = upcomingSBW;
		this.sbwPrevious = sbwPrevious;
		this.subjectID = ((Integer) subjectID).toString();
		setMonteCarloRealizationId(monteCarloRealization);
		trees = new ArrayList<MatapediaTree>();
	}
	
	@Override
	public String getSubjectId() {
		return subjectID;
	}

	protected void setMonteCarloRealizationId(int i) {
		monteCarloRealization = i;
	}

	@Override
	public int getMonteCarloRealizationId() {
		return monteCarloRealization;
	}

	@Override
	public List<MatapediaTree> getMatapediaTrees() {
		return trees;
	}

	public void addTree(MatapediaTree tree) {
		trees.add(tree);
	}


	@Override
	public boolean isSBWDefoliated() {
		return sbwPrevious;
	}

	@Override
	public boolean isGoingToBeDefoliated() {
		return upcomingSBW;
	}

	@Override
	public boolean isSprayed() {
		return false;
	}

	@Override
	public int getDateYr() {
		return 2000;
	}

}
