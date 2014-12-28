package com.thermostat.server.configuration;

import java.io.File;

public class Configuration {

	private static final String CONF_BASE_DIR = "conf";
	
	private static final File confBaseDir = new File(CONF_BASE_DIR);
	
	public static final File getConfigPath(String name) {
		return new File(confBaseDir, name);
	}
}
