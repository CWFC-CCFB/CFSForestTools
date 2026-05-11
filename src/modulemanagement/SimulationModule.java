package modulemanagement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SimulationModule {

	public static enum ModuleType {HDRelationship, 
		Biomass, 
		DiameterIncrement, 
		HarvestOccurrencePlotLevel,
		HarvestOccurrenceTreeLevel,
		Mortality,
		RecruitmentOccurrence, 
		RecruitmentAbundance, 
		RecruitDiameter, 
		StemTaper,
		Volume, 
		VolumeByLogGrade,
		Unknown};
	
	public ModuleType type() default ModuleType.Unknown; 
	
	public SpeciesLocale scope() default SpeciesLocale.Canada;
	
}
