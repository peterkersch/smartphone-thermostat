package com.thermostat.server;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thermostat.server.discovery.IpDiscoveryManager;
import com.thermostat.server.protocol.ThermostatListenServer;
import com.thermostat.server.protocol.ThermostatListenServerFactory;
import com.thermostat.server.zone.ZoneManager;
import com.thermostat.server.zone.ZoneRegistryFactory;

public class ThermostatServer {

	private static Logger logger = Logger.getLogger(ThermostatServer.class.getSimpleName());
	
	public static void main(String[] args) {
		try {
			// Start discovery manager
			IpDiscoveryManager discoveryManager = new IpDiscoveryManager();
			discoveryManager.start();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception when initializing IP discovery manager", e);
			System.exit(-1);
		}
		
		// Start communication threads listening on different technologies
		List<ThermostatListenServer> listenServers = ThermostatListenServerFactory.getListenServers();
		if (listenServers.isEmpty()) {
			logger.severe("No available communication technologies to listen for incoming connections");
			System.exit(-1);
		}
		for (ThermostatListenServer server : listenServers) {
			server.start();
		}
		
		// Start actuation threads managing temperature in each zone
		for (ZoneManager z : ZoneRegistryFactory.getZoneRegistry().getZoneManagers()) {
			z.start();
		}
	}

}
