package quebecmrnfutility.predictor.saplings;

import quebecmrnfutility.predictor.saplings.PowerSaplingBasalAreaAndDensityCompatiblePlot.CoverType;
import repicea.simulation.REpiceaPredictor;

@SuppressWarnings("serial")
abstract class PowerAbstractSaplingPredictor extends REpiceaPredictor {

	enum Effect {
		Intercept,
		Coupe,
		CouvertERS,
		CouvertSAB,
		G,
		Coupe_x_CouvertERS,
		Coupe_x_CouvertSAB,
		Coupe_x_G,
		CouvertERS_x_G,
		CouvertSAB_x_G
	}
	
	final boolean isVariabilityEnabled;

	
	PowerAbstractSaplingPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled); // no random effect in either of these models
		this.isVariabilityEnabled = isVariabilityEnabled;
	}


	void setXVector(PowerSaplingBasalAreaAndDensityCompatiblePlot plot) {
		oXVector.resetMatrix();
		double g = plot.getBasalAreaM2Ha();
		for (Effect e : Effect.values()) {
			switch (e) {
			case Intercept:
				oXVector.setValueAt(0, e.ordinal(), 1d);
				break;
			case Coupe:
				if (plot.isInterventionResult()) {
					oXVector.setValueAt(0, e.ordinal(), 1d);
				}
				break;
			case CouvertERS:
				if (plot.getCoverType() == CoverType.Maple) {
					oXVector.setValueAt(0, e.ordinal(), 1d);
				}
				break;
			case CouvertSAB:
				if (plot.getCoverType() == CoverType.Fir) {
					oXVector.setValueAt(0, e.ordinal(), 1d);
				}
				break;
			case G:
				oXVector.setValueAt(0, e.ordinal(), g);
				break;
			case Coupe_x_CouvertERS:
				if (plot.isInterventionResult() && plot.getCoverType() == CoverType.Maple) {
					oXVector.setValueAt(0, e.ordinal(), 1d);
				}
				break;
			case Coupe_x_CouvertSAB:
				if (plot.isInterventionResult() && plot.getCoverType() == CoverType.Fir) {
					oXVector.setValueAt(0, e.ordinal(), 1d);
				}
				break;
			case Coupe_x_G:
				if (plot.isInterventionResult()) {
					oXVector.setValueAt(0, e.ordinal(), g);
				}
				break;
			case CouvertERS_x_G:
				if (plot.getCoverType() == CoverType.Maple) {
					oXVector.setValueAt(0, e.ordinal(), g);
				}
				break;
			case CouvertSAB_x_G:
				if (plot.getCoverType() == CoverType.Fir) {
					oXVector.setValueAt(0, e.ordinal(), g);
				}
				break;
			}
		}
	}

}
