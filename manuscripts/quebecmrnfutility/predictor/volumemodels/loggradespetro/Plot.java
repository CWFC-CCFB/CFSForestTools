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
