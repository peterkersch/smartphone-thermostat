package com.thermostat.server.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thermostat.security.util.HexUtils;
import com.thermostat.security.util.InetUtils;
import com.thermostat.server.security.ThermostatServerSecurityManager;
import com.thermostat.technology.IpDiscoveryProtocol;

public class IpDiscoveryManager extends Thread {

	private static Logger logger = Logger.getLogger(IpDiscoveryManager.class.getSimpleName());

	private final InetAddress discoveryAddress;

	private final byte[] replyBytes;
	
	private MulticastSocket socket;
	
	public IpDiscoveryManager() throws IOException, GeneralSecurityException {
		// Initialize multicast socket and join multicast group
		SocketAddress address = new InetSocketAddress(/*InetUtils.getIPv4Address(), */ 
				IpDiscoveryProtocol.MULTICAST_DISCOVERY_UDP_PORT);
		socket = new MulticastSocket(address);
		socket.setTimeToLive(1);
		discoveryAddress = InetAddress.getByName(IpDiscoveryProtocol.MULTICAST_DISCOVERY_ADDRESS_IPV4);
		socket.joinGroup(discoveryAddress);
		
		// Create reply packet
		String fingerprintString = ThermostatServerSecurityManager.getInstance().getPublicKeyFingerprint();
		byte [] fingerprintBytes = HexUtils.hexToByte(fingerprintString);
		replyBytes = new byte[fingerprintBytes.length + 1];
		replyBytes[0] = IpDiscoveryProtocol.MULTICAST_DISCOVERY_RESPONSE;
		System.arraycopy(fingerprintBytes, 0, replyBytes, 1, fingerprintBytes.length);
	}
	
	public void close() {
		try {
			socket.leaveGroup(discoveryAddress);
			socket.close();
		} catch (IOException e) {
			// Ignore exceptions when closing socket
		}
	}
	
	@Override
	public void run() {

		while (true) {
			try {
				byte [] buf = new byte[IpDiscoveryProtocol.MAX_PACKET_SIZE];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				if (packet.getLength() > 0 && packet.getData()[0] == IpDiscoveryProtocol.MULTICAST_DISCOVERY_REQUEST) {
					ResponderThread t = new ResponderThread(packet.getAddress());
					t.start();
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Error when receiving multicast discovery packet", e);
			}
		}
	}
	
	private class ResponderThread extends Thread {

		private InetAddress clientAddress;
		
		public ResponderThread(InetAddress clientAddress) {
			this.clientAddress = clientAddress;
			
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(IpDiscoveryProtocol.getRandomWaitingTimeMs());
				DatagramPacket packet = new DatagramPacket(replyBytes, 
						replyBytes.length, 
						clientAddress, 
						IpDiscoveryProtocol.MULTICAST_DISCOVERY_UDP_PORT);
				socket.send(packet);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, "Reply thread interrupted", e);
			} catch (IOException e) {
				logger.log(Level.WARNING, "Exception when sending discovery response message", e);
			}
		}
	}
}
