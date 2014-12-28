package com.thermostat.security;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.thermostat.security.util.SecureRandomGenerator;

/**
 * Implements secure and authenticated data transfer protocol 
 * between communicating parties using public key cryptography. 
 */
public class SecureAuthenticatedDataTransfer<R extends PrivateKey, U extends PublicKey> {

	/** Maximum time to wait for the entire handshake operation to complete  */
	private static final long TIMEOUT_MS = 10000;
	
	/** Separator used between data elements in transmitted data */
	private static final String SEPARATOR = "|";
	
	private ThermostatSecurityManager<R, U> securityManager;
	
	private DataInputStream input;
	
	private DataOutputStream output;
	
	/**
	 * Creates a secure authenticate data transfer object to receive data from a peer.
	 * 
	 * @param securityManager used to perform encryption / decryption operations
	 * @param input the DataInputStream to read data from the peer
	 * @param output the DataInputStream to read data from the peer
	 */
	public SecureAuthenticatedDataTransfer(ThermostatSecurityManager<R, U> securityManager, DataInputStream input, DataOutputStream output) {
		this.securityManager = securityManager;
		this.input = input;
		this.output = output;
	}

	/**
	 * Send data to a peer.
	 * 
	 * @param publicKey the public key of the peer
	 * @param data the data to be sent
	 * @throws Exception
	 */
	public void send(String publicKeyString, String data) throws Exception {
		CommunicationThread t = new CommunicationThread(publicKeyString, data);
		t.run();
		try {
			// wait for the send thread to complete
			t.join(TIMEOUT_MS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (t.getException() != null) {
			throw t.getException();
		}
		if (!t.hasSucceeded()) {
			throw new IOException("Timeout");
		}
	}

	/**
	 * Receive data from a peer.
	 * 
	 * @return the received data
	 * @throws Exception
	 */
	public String receive() throws Exception {
		CommunicationThread t = new CommunicationThread();
		t.run();
		try {
			// wait for the send thread to complete
			t.join(TIMEOUT_MS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (t.getException() != null) {
			throw t.getException();
		}
		if (!t.hasSucceeded()) {
			throw new IOException("Timeout");
		}
		
		return t.getReply();
	}
	
	private final class CommunicationThread extends Thread {

		private String publicKeyString;
		private String message;
		
		private Exception exception = null;
		
		private boolean succeeded = false;
		
		private String reply = null;
		
		public CommunicationThread() {
		}
		
		public CommunicationThread(String publicKeyString, String message) {
			this.publicKeyString = publicKeyString;
			this.message = message;
		}
		
		public Exception getException() {
			return exception;
		}
		
		public boolean hasSucceeded() {
			return succeeded;
		}
		
		public String getReply() {
			return reply;
		}
		
		@Override
		public void run() {
			try {
				if (publicKeyString == null) {
					receive();
				} else {
					send();
				}
			} catch (Exception e) {
				exception = e;
			}
		}
		
		private void send() throws Exception {
			// Send public key fingerprint + challenge
			String challenge = SecureRandomGenerator.getRandomHexString();
			String request = securityManager.getPublicKeyFingerprint() + SEPARATOR + challenge;
			String encryptedMessage = securityManager.encrypt(request, publicKeyString);
			output.writeUTF(encryptedMessage);
			output.flush();
			// Read challenge response and challenge to reply
			String enryptedResponse = input.readUTF();
			String response = securityManager.decrypt(enryptedResponse);
			String [] s = response.split(SEPARATOR);
			if (s.length != 2) {
				throw new IOException("Illegal challenge response format");
			}
			if (s[0].equalsIgnoreCase(challenge)) {
				throw new GeneralSecurityException("Authentication failure: invalid challenge response");
			}
			// Send challenge response and data
			request = s[1] + SEPARATOR + message;
			encryptedMessage = securityManager.encrypt(request, publicKeyString);
			output.writeUTF(encryptedMessage);
			output.flush();
			succeeded = true;
		}
		
		private void receive() throws Exception {
			// Read public key fingerprint and challenge request
			String enryptedResponse = input.readUTF();
			String response = securityManager.decrypt(enryptedResponse);
			String [] s = response.split(SEPARATOR);
			if (s.length != 2) {
				throw new IOException("Illegal challenge response format");
			}
			String publicKeyString = securityManager.getPublicKeyString(s[0]);
			if (publicKeyString == null) {
				throw new GeneralSecurityException("Authentication failure: public key of the peer is not yet registered");
			}
			String challenge = SecureRandomGenerator.getRandomHexString();
			String challengeResponse = s[1] + SEPARATOR + challenge;
			String encryptedChallengeResponse = securityManager.encrypt(challengeResponse, publicKeyString);
			output.writeUTF(encryptedChallengeResponse);
			output.flush();
			String encryptedData = input.readUTF();
			response = securityManager.decrypt(encryptedData);
			s = response.split(SEPARATOR);
			if (s.length != 2) {
				throw new IOException("Illegal data format");
			}
			if (s[0].equalsIgnoreCase(challenge)) {
				throw new GeneralSecurityException("Authentication failure: invalid challenge response");
			}
			reply = s[1];
			succeeded = true;
		}
	}
}
