package com.thermostat.server.profile;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Map.Entry;

import com.thermostat.protocol.data.BroadcastEvent;
import com.thermostat.protocol.data.ResidentState;
import com.thermostat.protocol.data.ResidentStatusInfo;
import com.thermostat.server.configuration.Configuration;

/**
 * Logs resident status info received in state update messages. 
 */
public class ResidentStatusLogger {

	private static ResidentStatusLogger instance = null;
	
	private static final String LOG_FILE_NAME = "status.txt";
	
	private static final String FIELD_SEPARATOR = "\t";
	
	private PrintStream stream;
	
	public static ResidentStatusLogger getInstance() throws IOException {
		if (instance == null) {
			instance = new ResidentStatusLogger();
		}
		return instance;
	}
	
	public ResidentStatusLogger() throws IOException {
		stream = new PrintStream(Configuration.getLogPath(LOG_FILE_NAME));
	}
	
	public void write(String fingerprint, ResidentStatusInfo status) {
		long timestamp = status.getTimestamp();
		// Write sensor data
		for (Entry<String, Float> e : status.getSensorValues().entrySet()) {
			stream.println(timestamp + FIELD_SEPARATOR + fingerprint + FIELD_SEPARATOR + e.getKey() + FIELD_SEPARATOR + e.getValue());
		}
		// Write broadcast event data
		for (BroadcastEvent e : status.getEvents()) {
			stream.println(e.timestamp + FIELD_SEPARATOR + fingerprint + FIELD_SEPARATOR + e.name);
		}
		stream.flush();
	}

	public void write(String fingerprint, ResidentState state) {
		long timestamp = (new Date()).getTime();
		stream.println(timestamp + FIELD_SEPARATOR + fingerprint + FIELD_SEPARATOR + "state" + FIELD_SEPARATOR + state.getName());
		stream.flush();
	}
}
