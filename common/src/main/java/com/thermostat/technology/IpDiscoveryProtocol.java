package com.thermostat.technology;

import java.util.Random;

/**
 * Constants used for a very simple discovery protocol to find dynamically assigned IPv4 addresses of servers.
 * The first byte of each message is the message type (MULTICAST_DISCOVERY_REQUEST or MULTICAST_DISCOVERY_RESPONSE).
 * Requests contain only the message type byte.
 * Replies also contain the hash code of the server's public key.
 * There is a random waiting interval before sending out replies to prevent collisions.
 */
public class IpDiscoveryProtocol {

	/* Protocol constants */
	
	/**
	 * Link local IPv4 multicast address used to discover IPv4 address of thermostat servers.
	 * TODO: not registered at IANA, investigate again how mDNS could be used instead
	 */
	public static final String MULTICAST_DISCOVERY_ADDRESS_IPV4 = "239.255.255.250";

	/** UDP port used to listen for incoming multicast discovery requests */
	public static final int MULTICAST_DISCOVERY_UDP_PORT = 7212;
	
	/** Discovery request packet type (no payload) */
	public static final byte MULTICAST_DISCOVERY_REQUEST = 1;
	
	/** Discovery reply packet type (payload: hash of public key) */
	public static final byte MULTICAST_DISCOVERY_RESPONSE = 2;
	
	public static final int MAX_PACKET_SIZE = 100;

	/* Timing constants */
	
	/** Maximum age of an address entry before having to perform address discovery again */
	public static final int MAX_RESOLVER_RECORD_AGE_SEC = 5 * 60;
	
	/** Maximum waiting time for responses after having sent the request (ms) */
	public static final int RESPONSE_TIMEOUT_MS = 500;
	
	/** Max waiting time before sending out discovery response */
	public static final int MAX_DELAY_MS = 100;
	
	/** Random number generator to derive waiting time before sending response */
	public static final Random random = new Random();
	
	public static final int getRandomWaitingTimeMs() {
		return random.nextInt(MAX_DELAY_MS);
	}
}
