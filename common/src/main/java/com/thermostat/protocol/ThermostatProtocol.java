package com.thermostat.protocol;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Provides a generic asynchronous request - reply message sequence protocol implementation.
 * Message sequence is closed automatically whenever an exception occurs during the communication. 
 * 
 * TODO: manage authentication and encryption here in a way transparent to upper layers.
 * On option is to have ThermostatSecurityMessage objects exchanged transparently in this superclass. 
 * Use existing standards (e.g., TLS)
 */
public class ThermostatProtocol {

	private static final int DEFAULT_BUFFER_SIZE = 2048;
	
	private ThermostatSocket socket;
	
	private ObjectInputStream input = null;
	
	private ObjectOutputStream output = null;
	
	private ThermostatMessageListener listener = null;
	
	/** Last request message that had been sent successfully */
	private ThermostatMessage lastRequest = null;
	
	private boolean closing = false;
	
	private boolean closed = false;
	
	/**
	 * Creates a new protocol instance
	 * 
	 * @param socket the ThermostatSocket containing Java input and output streams for bi-directional communication
	 */
	public ThermostatProtocol(ThermostatSocket socket) {
		this.socket = socket;
	}
	
	private ObjectInputStream getInput() throws IOException {
		if (input == null) {
			input = new ObjectInputStream(socket.getInputStream());
		}
		return input;
	}
	
	private ObjectOutputStream getOutput() throws IOException {
		if (output == null) {
			output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream(), DEFAULT_BUFFER_SIZE));
		}
		return output;
	}

	public void registerMessageListener(ThermostatMessageListener listener) throws IllegalStateException {
		if (this.listener != null) {
			throw new IllegalStateException("ThermostatMessageListener already registered");
		}
		if (closing) {
			throw new IllegalStateException("ThermostatProtocol instance already closed");
		}
		this.listener = listener;
		// start listening for reply message
		Thread t = new ThermostatProtocolInputThread();
		t.start();	
	}
	
	public void send(ThermostatMessage message) throws IOException {
		try {
			getOutput().writeObject(message);
			lastRequest = message;
			getOutput().flush();
		} catch (IOException e) {
			close(true);
			throw e;
		}
	}
	
	/**
	 * Close this protocol sequence.
	 * 
	 * @param failure true if closing due to an error 
	 * false when close is after successfully ending the message sequence
	 */
	private void close(boolean failure) {
		/* Exceptions are ignored in this method */
		
		/* Make sure that close is performed only once */
		synchronized (this) {
			if (closing) {
				return;
			}
			closing = true;
		}

		/* Wait 0.5 second before closing socket just to be safe... */ 
		if (!failure) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
		}

		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {}
		}
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {}
		}
		try {
			socket.close();
		} catch (IOException e) {}
		
		closed = true;
	}
	
	/**
	 * Close this protocol sequence when communication sequence completed normally 
	 */
	public void close() {
		close(false);
	}
	
	public boolean isClosing() {
		return closing;
	}
	
	public boolean isClosed() {
		return closed;
	}
	
	@Override
	public String toString() {
		return "Protocol instance with " + socket.toString();
	}
	
	private class ThermostatProtocolInputThread extends Thread {
		
		@Override
		public void run() {
			while (!closing) {
				try {
					Object message = getInput().readObject();
					listener.receiveMessage((ThermostatMessage)message, lastRequest);
				} catch (Exception e) {
					listener.failure(e, lastRequest);
					close(true);
				}
			}
		}
	}
}
