package com.thermostat.security.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InetUtils {

	/**
	 * List all IP addresses of the device, except loopback addresses.
	 * 
	 * @return a List of IP addresses
	 * @throws IOException
	 */
	public static List<InetAddress> getIPAddresses() throws IOException {
		List<InetAddress> addresses = new ArrayList<InetAddress>();
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface intf : interfaces) {
            List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            for (InetAddress a : addrs) {
            	if (a.isLoopbackAddress()) {
            		continue;
            	}
            	addresses.add(a);
            }
        }
		return addresses;
	}
	
	/**
	 * Returns an IP address of the device to be used for local communication on the subnet.
	 * Order of preference for selecting addresses:
	 * 1) IPv6 link local
	 * 2) IPv6 global address
	 * 3) IPv4 address
	 * 
	 * @return the selected IP address or null if no valid IP addresses had been found.
	 */
	public static InetAddress getLocalInetAddress() {
		try {
			List<InetAddress> addresses = getIPAddresses();
			for (InetAddress a : addresses) {
				if (a instanceof Inet6Address && a.isLinkLocalAddress()) {
					return a;
				}
			}
			for (InetAddress a : addresses) {
				if (a instanceof Inet6Address) {
					return a;
				}
			}
			if (addresses.size() > 0) {
				return addresses.get(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * Get the IPv4 address of this terminal.
	 * 
	 * @return the IPv4 address of the terminal or null if it does not have a valid IPv4 address
	 * @throws IOException 
	 */
	public static InetAddress getIPv4Address() throws IOException {
		for (InetAddress a : getIPAddresses()) {
			if (a instanceof Inet4Address) {
				return a;
			}
		}
		return null;
	}
	
	public static boolean isIPv6Enabled() throws IOException {
		for (InetAddress a : getIPAddresses()) {
			if (a instanceof Inet6Address && a.isLinkLocalAddress()) {
				return true;
			}
		}
		return false;
	}
	
	public static int getLinkLocalScopeId() throws IOException {
		for (InetAddress a : getIPAddresses()) {
			if (a instanceof Inet6Address && a.isLinkLocalAddress()) {
				return ((Inet6Address)a).getScopeId();
			}
		}
		throw new IOException("No IPv6 support on this device");
	}
}
