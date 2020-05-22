package quebecmrnfutility.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({quebecmrnfutility.test.LanguageSettings.class,
	quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModelTest.class,					// tests harvest module
	quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperPredictorTest.class,
	quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperPredictorTestIntensive.class,
	quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradePredictorTest.class,
	quebecmrnfutility.treelogger.petrotreelogger.PetroTreeLoggerTest.class,
	quebecmrnfutility.treelogger.sybille.SybilleTreeLoggerTest.class,
	quebecmrnfutility.predictor.hdrelationships.generalhdrelation2009.GeneralHeight2009PredictorTests.class,
	quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014.GeneralHeight2014PredictorTests.class,
//	quebecmrnfutility.biosim.BioSimClientTest.class,		// deprecated tests
	quebecmrnfutility.predictor.volumemodels.wbirchloggrades.WBirchLogGradesPredictorTest.class,
	quebecmrnfutility.treelogger.wbirchprodvol.WBirchProdVolTreeLoggerTest.class,
	quebecmrnfutility.predictor.artemis2009.Artemis2009PredictorTests.class,
	quebecmrnfutility.predictor.matapedia.MatapediaMortalityPredictorTest.class,
	quebecmrnfutility.predictor.thinners.melothinner.MeloThinnerTests.class,
	quebecmrnfutility.predictor.volumemodels.honertotalvolume.HonerTotalVolumeTest.class,
	canforservutility.predictor.disturbances.SimpleRecurrenceBasedDisturbancePredictorTests.class,
	canforservutility.predictor.disturbances.sprucebudworm.occurrence.boulangerarsenault2004.SpruceBudwormOutbreakOccurrencePredictorTests.class,
	canforservutility.predictor.disturbances.sprucebudworm.defoliation.gray2013.DefoliationTests.class,
})
	
public class AllForestToolsTests {}

