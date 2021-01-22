package quebecmrnfutility.predictor.thinners.officialharvestmodule;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Ignore;


public class QuebecMRNFGenericTreatmentTest {

	@Test
	public void generatingQuebecMRNFGenericTreatmentFromText() {
		String allo = "quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModel$TreatmentType.CJMSCR;50";
		QuebecMRNFGenericTreatment test = QuebecMRNFGenericTreatment.getGenericTreatmentFromCompleteName(allo);
		Assert.assertTrue("Testing if this instance is non null", test != null);
	}
	
	
}
