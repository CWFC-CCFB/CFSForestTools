package quebecmrnfutility.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({quebecmrnfutility.test.LanguageSettings.class,
	quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModelTest.class,					// tests harvest module
	quebecmrnfutility.predictor.stemtaper.StemTaperTest.class,											// tests the beta version of the stem taper model
	quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictorTest.class,
	quebecmrnfutility.treelogger.sybille.SybilleTreeLoggerTest.class})
public class AllForestToolsTests {}

