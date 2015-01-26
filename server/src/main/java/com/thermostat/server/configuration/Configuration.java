package com.thermostat.server.configuration;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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
		// Set conf and log directories
		String confBaseDirProperty = System.getProperty(CONF_DIR_PROPERTY);
		String logBaseDirProperty = System.getProperty(LOG_DIR_PROPERTY);
		confBaseDir = confBaseDirProperty == null ?
				new File(DEFAULT_CONF_DIR) :
					new File(confBaseDirProperty);
		logBaseDir = logBaseDirProperty == null ?
				new File(DEFAULT_LOG_DIR) :
					new File(logBaseDirProperty);
				
		// Set formatter for the root logger
		Logger logger = Logger.getLogger("");
		Formatter formatter = new ThermostatLogFormatter();
		for (Handler h : logger.getHandlers()) {
			h.setFormatter(formatter);
		}
	}
	
	public static final File getConfigPath(String name) {
		return new File(confBaseDir, name);
	}
	
	public static final File getLogPath(String name) {
		return new File(logBaseDir, name);
	}

	private static class ThermostatLogFormatter extends Formatter {
		
		private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		@Override
		public String format(LogRecord r) {
			StringBuilder b = new StringBuilder();
			b.append(format.format(new Date(r.getMillis())));
			b.append(" ");
			b.append(r.getLevel());
			b.append(": ");			
			b.append(r.getMessage());
			Throwable t = r.getThrown(); 
			if (t != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				b.append(sw);
			}
			b.append("\n");
			return b.toString();
		}
	}
}
