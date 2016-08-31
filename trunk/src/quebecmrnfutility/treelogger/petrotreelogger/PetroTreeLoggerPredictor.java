package quebecmrnfutility.treelogger.petrotreelogger;

import repicea.simulation.REpiceaPredictor;

class PetroTreeLoggerPredictor extends REpiceaPredictor {

	PetroTreeLoggerPredictor(boolean isParametersVariabilityEnabled,
			boolean isRandomEffectsVariabilityEnabled,
			boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
	}
	
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

}
