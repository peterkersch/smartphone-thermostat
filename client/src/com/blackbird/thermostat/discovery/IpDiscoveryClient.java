package com.blackbird.thermostat.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.SystemClock;
import android.util.Log;

import com.blackbird.thermostat.store.ThermostatProfile;
import com.thermostat.security.util.HexUtils;
import com.thermostat.technology.IpDiscoveryProtocol;

// TODO:  enforce a minimum time between two subsequent discoveries
public class IpDiscoveryClient {

	private static String TAG = IpDiscoveryClient.class.getSimpleName();

	private InetAddress discoveryAddress;

	private MulticastSocket socket;
	
	private byte[] requestBytes;
	
	private WifiManager wifiManager;
	
	private Map<String, IpResolverRecord> resolverMap = new HashMap<String, IpDiscoveryClient.IpResolverRecord>();
	
	public IpDiscoveryClient(Context context) throws IOException {
		wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

		// Initialize multicast socket
		SocketAddress address = new InetSocketAddress(IpDiscoveryProtocol.MULTICAST_DISCOVERY_UDP_PORT);
		socket = new MulticastSocket(address);
		socket.setTimeToLive(1);

		// Initialize protocol variables
		discoveryAddress = InetAddress.getByName(IpDiscoveryProtocol.MULTICAST_DISCOVERY_ADDRESS_IPV4);
		requestBytes = new byte[1];
		requestBytes[0] = IpDiscoveryProtocol.MULTICAST_DISCOVERY_REQUEST;
	}
	
	/**
	 * Start discovery procedure.
	 * 
	 * @param thermostats whose IP address should be resolved as a result of the discovery process.
	 */
	public void startDiscovery(Collection<ThermostatProfile> thermostats, boolean stopWhenAllFound) {
		if (!wifiManager.isWifiEnabled()) {
			Log.w(TAG, "WiFi not enabled, IPv4 address discovery should not have been called");
			return;
		}
		
		ResponseThread t = new ResponseThread(thermostats, stopWhenAllFound);
		t.start();
	}
	
	/**
	 * Get the address of the thermostat server with the given public key fingerprint.
	 * 
	 * @param fingerprint the fingerprint of the thermostat server's public key
	 * @return the IPv4 address of the given thermostat server or null if no corresponding records are found 
	 * or they are older then IpDiscoveryProtocol.MAX_RESOLVER_RECORD_AGE_SEC
	 */
	public InetAddress getAddress(String fingerprint) {
		IpResolverRecord record = resolverMap.get(fingerprint);
		if (record != null) {
			long currentTimeMs = SystemClock.elapsedRealtime();
			if (currentTimeMs - record.lastTimestamp < IpDiscoveryProtocol.MAX_RESOLVER_RECORD_AGE_SEC*1000) {
				return record.address;
			}
		}
		return null;
	}
	
	private class ResponseThread extends Thread {
		
		private Set<String> remainingFingerprints = new HashSet<String>();
		
		private boolean stopWhenAllFound;
		
		public ResponseThread(Collection<ThermostatProfile> thermostats, boolean stopWhenAllFound) {
			this.stopWhenAllFound = stopWhenAllFound;
			for (ThermostatProfile profile : thermostats) {
				remainingFingerprints.add(profile.getFingerprint());
			}
		}
		
		@Override
		public void run() {
			Log.d(TAG, "Started IPv4 address discovery");
			MulticastLock lock = wifiManager.createMulticastLock("Thermostat discovery lock");
			lock.acquire();
			
			synchronized (IpDiscoveryClient.this) {
				try {
					// Send packet
					DatagramPacket requestPacket = new DatagramPacket(requestBytes, 
							requestBytes.length, 
							discoveryAddress, 
							IpDiscoveryProtocol.MULTICAST_DISCOVERY_UDP_PORT);
					socket.send(requestPacket);

					// Wait for responses at most IpDiscoveryProtocol.RESPONSE_TIMEOUT_MS time.
					for (long startTime = SystemClock.elapsedRealtime(), currentTime = startTime; 
							currentTime < startTime + IpDiscoveryProtocol.RESPONSE_TIMEOUT_MS;
							currentTime = SystemClock.elapsedRealtime()) {
						 // Stop discovery as soon as all registered thermostat server addresses had been resolved
						if (stopWhenAllFound && remainingFingerprints.isEmpty()) {
							break;
						}
						receivePacket((int) (startTime + IpDiscoveryProtocol.RESPONSE_TIMEOUT_MS - currentTime));
					}
				} catch (SocketTimeoutException e) {
					// Natural completion of discovery period
				} catch (IOException e) {
					Log.e(TAG, "Error when sending IP discovery request packet", e);
				}
			}
			
			lock.release();
			Log.d(TAG, "Completed IPv4 address discovery");
		}
		
		private void receivePacket(int timeoutMs) throws SocketTimeoutException, IOException {
			// Receive response
			byte [] responseBytes = new byte [IpDiscoveryProtocol.MAX_PACKET_SIZE];
			DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
			socket.setSoTimeout(timeoutMs);
			socket.receive(responsePacket);
			
			// Process response
			byte [] response = responsePacket.getData();
			if (responsePacket.getLength() > 0 && response[0] == IpDiscoveryProtocol.MULTICAST_DISCOVERY_RESPONSE) {
				byte [] fingerprintBytes = new byte[responsePacket.getLength()-1];
				System.arraycopy(response, 1, fingerprintBytes, 0, fingerprintBytes.length);
				String fingerprint = HexUtils.byteToHex(fingerprintBytes);
				InetAddress serverAddress = responsePacket.getAddress();
				remainingFingerprints.remove(fingerprint);
				Log.d(TAG, "Resolved IP of thermostat " + fingerprint + ": " + serverAddress.getHostAddress());
				
				// Store IP address of the sender
				IpResolverRecord record = resolverMap.get(fingerprint);
				if (record == null) {
					record = new IpResolverRecord(serverAddress, SystemClock.elapsedRealtime());
					resolverMap.put(fingerprint, record);
				} else {
					record.address = serverAddress;
					record.lastTimestamp = SystemClock.elapsedRealtime();
				}
			}
		}
	}
	
	private static class IpResolverRecord {
		
		public InetAddress address;
		
		public long lastTimestamp;
		
		public IpResolverRecord(InetAddress address, long lastTimestamp) {
			this.address = address;
			this.lastTimestamp = lastTimestamp;
		}
	}
}
