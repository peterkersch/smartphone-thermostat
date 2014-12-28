package com.thermostat.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ThermostatSocket {

	InputStream getInputStream() throws IOException;
	
	OutputStream getOutputStream() throws IOException;
	
	/**
	 * Close the underlaying physical connection
	 */
	void close() throws IOException;
}
