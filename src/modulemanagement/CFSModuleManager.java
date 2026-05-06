/*
 * This file is part of the CFSForesttools library.
 *
 * Copyright (C) 2026 His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package modulemanagement;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import modulemanagement.SimulationModule.ModuleType;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;
import repicea.util.JarUtility;
import repicea.util.ObjectUtility;

/**
 * A class to list the different modules in this library.
 * @author Mathieu Fortin - May 2026
 */
public class CFSModuleManager {

	@SuppressWarnings("serial")
	public static class ModuleManagerException extends RuntimeException {
		private ModuleManagerException(Exception e) {
			super(e);
		}
	}
		
	private static CFSModuleManager SINGLETON;
	
	
	final boolean isRunningFromJar;
	private final static String CLASS_STR = ".class";
	
	private final Map<ModuleType, Map<SpeciesLocale, List<Class<? extends REpiceaPredictor>>>> moduleMap;
	private final Map<SpeciesLocale, List<SpeciesLocale>> scopeMap;
	
	@SuppressWarnings("unchecked")
	CFSModuleManager() {
		String jarFilename = JarUtility.getJarFileImInIfAny(getClass());
		isRunningFromJar = jarFilename != null;
		List<String> classes = new ArrayList<String>();
		moduleMap = new HashMap<ModuleType, Map<SpeciesLocale, List<Class<? extends REpiceaPredictor>>>>();
		if (isRunningFromJar) {
			JarFile f = null;
			try {
				f = new JarFile(new File(jarFilename));
				Enumeration<JarEntry> iterator = f.entries();
				while (iterator.hasMoreElements()) {
				    JarEntry je = (JarEntry) iterator.nextElement();
				    if(!je.isDirectory() && je.getName().endsWith(CLASS_STR)){
						String className = je.getName().substring(0, je.getName().length() - CLASS_STR.length());
						classes.add(className.replace('/', '.'));
				    }
				}
			} catch (IOException e) {
				throw new ModuleManagerException(e);
			} finally {
				if (f != null) {
					try {
						f.close();
					} catch (IOException e) {}
				}
			}
		} else {
			String path = ObjectUtility.getTrueRootPath(getClass()) + "main";
			System.out.println("Path set to " + path);
			File directory = new File(path);
			retrieveClassFiles(directory, directory, classes);
		}
		
		ClassLoader cl = getClass().getClassLoader();
		for (String className : classes) {
			try {
				Class<?> clazz = cl.loadClass(className);
				SimulationModule annotation = clazz.getAnnotation(SimulationModule.class);
				if (annotation != null) {
					ModuleType type = annotation.type();
					SpeciesLocale scope = annotation.scope();
					if (!moduleMap.containsKey(type)) {
						moduleMap.put(type, new HashMap<SpeciesLocale, List<Class<? extends REpiceaPredictor>>>());
					}
					Map<SpeciesLocale, List<Class<? extends REpiceaPredictor>>> innerMap = moduleMap.get(type);
					if (!innerMap.containsKey(scope)) {
						innerMap.put(scope, new ArrayList<Class<? extends REpiceaPredictor>>());
					}
					innerMap.get(scope).add((Class<? extends REpiceaPredictor>) clazz);
			    }
			} catch(ClassNotFoundException e) {
				System.out.println("Cannot load class " + className);
			}
		}
		scopeMap = new HashMap<SpeciesLocale, List<SpeciesLocale>>();
	}

	static void retrieveClassFiles(File rootDirectory, File directory, List<String> classList) {
		File[] files = directory.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					retrieveClassFiles(rootDirectory, f, classList);
				} else if (f.getName().endsWith(CLASS_STR)) {
					URI relativePath = ObjectUtility.relativizeTheseFile(rootDirectory, f.getAbsoluteFile()); 
					String pathToFile = relativePath.getPath();
					String className = pathToFile.substring(0, pathToFile.length() - CLASS_STR.length());
					classList.add(className.replace('/', '.'));
				}
			}
		}
	}

	/**
	 * Access to the singleton of this class. 
	 * @return a CFSModuleManager instance
	 */
	public static synchronized CFSModuleManager getInstance() {
		if (SINGLETON == null) {
			SINGLETON = new CFSModuleManager();
		}
		return SINGLETON;
	}
	
	/**
	 * Provide the modules for a certain type.
	 * @param type a ModuleType enum
	 * @return a List of Class instances
	 */
	public List<Class<? extends REpiceaPredictor>> getModules(ModuleType type) {
		List<Class<? extends REpiceaPredictor>> classes = new ArrayList<Class<? extends REpiceaPredictor>>();
		if (moduleMap.containsKey(type)) {
			for (List<Class<? extends REpiceaPredictor>> classList : moduleMap.get(type).values()) {
				classes.addAll(classList);
			}
		}
		return classes;
	}

	/**
	 * Provide the modules for a certain scope.
	 * @param scope a SpeciesLocale enum
	 * @return a List of Class instances
	 */
	public List<Class<? extends REpiceaPredictor>> getModules(SpeciesLocale scope) {
		List<SpeciesLocale> scopes = getCompatibleSpeciesLocales(scope);
		List<Class<? extends REpiceaPredictor>> classes = new ArrayList<Class<? extends REpiceaPredictor>>();
		for (Map<SpeciesLocale, List<Class<? extends REpiceaPredictor>>> localMap : moduleMap.values()) {
			for (SpeciesLocale thisScope : localMap.keySet()) {
				if (scopes.contains(thisScope)) {
					classes.addAll(localMap.get(thisScope));
				}
			}
		}
		return classes;
	}

	private synchronized List<SpeciesLocale> getCompatibleSpeciesLocales(SpeciesLocale scope) {
		if (!scopeMap.containsKey(scope)) {
			List<SpeciesLocale> scopes = new ArrayList<SpeciesLocale>();
			SpeciesLocale currentScope = scope;
			while (currentScope != null) {
				scopes.add(currentScope);
				currentScope = currentScope.getNextLevel();
			}
			scopeMap.put(scope, scopes);
		}
		return scopeMap.get(scope);
	}
	
	
	/**
	 * Provide the modules for a certain type and scope.
	 * @param type a ModuleType enum
	 * @param scope a SpeciesLocale enum
	 * @return a List of Class instances
	 */
	public List<Class<? extends REpiceaPredictor>> getModules(ModuleType type, SpeciesLocale scope) {
		List<SpeciesLocale> scopes = getCompatibleSpeciesLocales(scope);
		List<Class<? extends REpiceaPredictor>> classes = new ArrayList<Class<? extends REpiceaPredictor>>();
		if (moduleMap.containsKey(type)) {
			Map<SpeciesLocale, List<Class<? extends REpiceaPredictor>>> localMap = moduleMap.get(type);
			for (SpeciesLocale thisScope : localMap.keySet()) {
				if (scopes.contains(thisScope)) {
					classes.addAll(localMap.get(thisScope));
				}
			}
		}
		return classes;
	}

}
