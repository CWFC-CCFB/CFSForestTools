package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import org.junit.Test;
import quebecmrnfutility.predictor.thinners.officialharvestmodule.QuebecMRNFGenericTreatment;

public class QuebecMRNFGenericTreatmentTest {

	@Test
	public void generatingQuebecMRNFGenericTreatmentFromText() {
		String allo = "quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModel$TreatmentType.CJMSCR;50";
		QuebecMRNFGenericTreatment test = QuebecMRNFGenericTreatment.getGenericTreatmentFromCompleteName(allo);
	}
	
	
}
