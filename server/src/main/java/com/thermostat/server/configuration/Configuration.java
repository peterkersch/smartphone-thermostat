package com.thermostat.server.configuration;

import java.io.File;

public class Configuration {

	/** Default config directory */
	private static final String DEFAULT_CONF_DIR = "conf";
	/** Default log directory */
	private static final String DEFAULT_LOG_DIR = "log";

	/** Java system property name that can be used to override default config directory */
	private static final String CONF_DIR_PROPERTY = "path.conf";
	/** Java system property name that can be used to override default log directory */
	private static final String LOG_DIR_PROPERTY = "path.log";
	
	private static final File confBaseDir;
	private static final File logBaseDir;
	
	static {
		String confBaseDirProperty = System.getProperty(CONF_DIR_PROPERTY);
		String logBaseDirProperty = System.getProperty(LOG_DIR_PROPERTY);
		confBaseDir = confBaseDirProperty == null ?
				new File(DEFAULT_CONF_DIR) :
					new File(confBaseDirProperty);
		logBaseDir = logBaseDirProperty == null ?
				new File(DEFAULT_LOG_DIR) :
					new File(logBaseDirProperty);
	}
	
	public static final File getConfigPath(String name) {
		return new File(confBaseDir, name);
	}
	
	public static final File getLogPath(String name) {
		return new File(logBaseDir, name);
	}

}
