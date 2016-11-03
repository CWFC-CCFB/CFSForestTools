package quebecmrnfutility.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({quebecmrnfutility.test.LanguageSettings.class,
	quebecmrnfutility.predictor.officialharvestmodule.OfficialHarvestModelTest.class,					// tests harvest module
	quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictorTest.class,
	quebecmrnfutility.predictor.stemtaper.schneiderequations.StemTaperPredictorTestIntensive.class,
	quebecmrnfutility.predictor.loggradespetro.PetroGradePredictorTest.class,
	quebecmrnfutility.treelogger.petrotreelogger.PetroTreeLoggerTest.class,
	quebecmrnfutility.treelogger.sybille.SybilleTreeLoggerTest.class,
	quebecmrnfutility.predictor.betahdrelation.GeneralHeight2014PredictorTests.class})
public class AllForestToolsTests {}

