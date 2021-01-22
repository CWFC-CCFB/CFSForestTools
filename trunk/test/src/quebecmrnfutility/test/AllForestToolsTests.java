package quebecmrnfutility.test;

import canforservutility.predictor.disturbances.SimpleRecurrenceBasedDisturbancePredictorTest;
import canforservutility.predictor.disturbances.sprucebudworm.defoliation.gray2013.DefoliationTest;
import canforservutility.predictor.disturbances.sprucebudworm.occurrence.boulangerarsenault2004.SpruceBudwormOutbreakOccurrencePredictorTest;
import canforservutility.predictor.iris2020.recruitment.Iris2020RecruitmentTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import quebecmrnfutility.predictor.artemis2009.Artemis2009PredictorTest;
import quebecmrnfutility.predictor.hdrelationships.generalhdrelation2009.GeneralHeight2009PredictorTest;
import quebecmrnfutility.predictor.hdrelationships.generalhdrelation2014.GeneralHeight2014PredictorTest;
import quebecmrnfutility.predictor.thinners.melothinner.MeloThinnerTest;

@RunWith(Suite.class)
@SuiteClasses({quebecmrnfutility.test.LanguageSettings.class,
	quebecmrnfutility.predictor.thinners.officialharvestmodule.OfficialHarvestModelTest.class,					// tests harvest module
	quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperPredictorTest.class,
	quebecmrnfutility.predictor.volumemodels.stemtaper.schneiderequations.StemTaperPredictorTestIntensive.class,
	quebecmrnfutility.predictor.volumemodels.loggradespetro.PetroGradePredictorTest.class,
	quebecmrnfutility.treelogger.petrotreelogger.PetroTreeLoggerTest.class,
	quebecmrnfutility.treelogger.sybille.SybilleTreeLoggerTest.class,
	GeneralHeight2009PredictorTest.class,
	GeneralHeight2014PredictorTest.class,
	quebecmrnfutility.predictor.volumemodels.wbirchloggrades.WBirchLogGradesPredictorTest.class,
	quebecmrnfutility.treelogger.wbirchprodvol.WBirchProdVolTreeLoggerTest.class,
	Artemis2009PredictorTest.class,
	quebecmrnfutility.predictor.matapedia.MatapediaMortalityPredictorTest.class,
	MeloThinnerTest.class,
	quebecmrnfutility.predictor.volumemodels.honertotalvolume.HonerTotalVolumeTest.class,
	SimpleRecurrenceBasedDisturbancePredictorTest.class,
	SpruceBudwormOutbreakOccurrencePredictorTest.class,
	DefoliationTest.class,
	Iris2020RecruitmentTest.class,
})
	
public class AllForestToolsTests {}

