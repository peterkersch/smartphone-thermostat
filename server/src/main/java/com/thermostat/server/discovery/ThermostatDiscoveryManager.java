package com.thermostat.server.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import com.thermostat.security.util.InetUtils;
import com.thermostat.technology.GenericIPService;

/**
 * Ensures that clients can discover the thermostat server unit on the local network via mDNS.
 */
public class ThermostatDiscoveryManager {

	private static Logger logger = Logger.getLogger(ThermostatDiscoveryManager.class.getSimpleName());

	private static Map<InetAddress, JmDNS> jmdnsMap = new HashMap<InetAddress, JmDNS>();
	
	private static ServiceInfo serviceInfo;
	
	public static void init() throws IOException {
		for (InetAddress a : InetUtils.getIPAddresses()) {
			try {
				JmDNS jmdns = JmDNS.create(a);
				serviceInfo = ServiceInfo.create(GenericIPService.MDNS_SERVICE_TYPE, 
						GenericIPService.MDNS_SERVICE_NAME, 
						GenericIPService.THERMOSTAT_SERVER_TCP_PORT, 
						GenericIPService.MDNS_SERVICE_DESCRIPTION);
				jmdns.registerService(serviceInfo);
				jmdnsMap.put(a, jmdns);
				logger.info("Initialized JmDNS for the IP address " + a);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error when initializing JmDNS for the IP address " + a.getHostAddress(), e);
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			init();
			Thread.sleep(1000*10000);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error when initializing service discovery", e);
		}
	}
}
