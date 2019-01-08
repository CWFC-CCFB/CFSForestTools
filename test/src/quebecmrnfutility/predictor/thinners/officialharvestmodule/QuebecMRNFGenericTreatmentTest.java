package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import quebecmrnfutility.predictor.thinners.officialharvestmodule.QuebecMRNFGenericTreatment;

public class QuebecMRNFGenericTreatmentTest {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String allo = "quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModel$TreatmentType.CJMSCR;50";
		QuebecMRNFGenericTreatment test = QuebecMRNFGenericTreatment.getGenericTreatmentFromCompleteName(allo);
		System.exit(0);
	}
	
	
}
