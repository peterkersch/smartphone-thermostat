package com.blackbird.thermostat.technology;

import java.net.Inet6Address;
import java.util.Collections;
import java.util.List;

import com.blackbird.thermostat.protocol.ThermostatServerIdentifier;

public class ServerIdentifierIP extends ThermostatServerIdentifier {
	
	/** List of SSIDs coupled to this thermostat server */
	private List<String> ssids;
	
	/** Link local IPv6 address of the thermostat (static, determined by MAC address of the interface). */
	private Inet6Address ipv6Address; 
	
	public ServerIdentifierIP(List<String> ssids, Inet6Address ipv6Address) {
		this.ssids = ssids;
		this.ipv6Address = ipv6Address;
	}
	
	public List<String> getSSIDs() {
		return Collections.unmodifiableList(ssids);
	}
	
	public Inet6Address getIpv6Address() {
		return ipv6Address;
	}
}
