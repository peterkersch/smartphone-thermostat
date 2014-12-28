package com.thermostat.server.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thermostat.server.technology.ListenServerBluecove;
import com.thermostat.server.technology.ListenServerIP;

/**
 * Instantiates available listen server instances 
 */
public class ThermostatListenServerFactory {

	private static Logger logger = Logger.getLogger(ThermostatListenServerFactory.class.getSimpleName());

	private static List<ThermostatListenServer> servers = new ArrayList<ThermostatListenServer>();
	
	static {
		try {
			servers.add(ListenServerBluecove.getInstance());
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error when initializing Bluetooth listen server", e);
		}
		try {
			servers.add(ListenServerIP.getInstance());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error when initializing IP listen server", e);
		}
	}
	
	public static final List<ThermostatListenServer> getListenServers() {
		return Collections.unmodifiableList(servers);
	}
}
