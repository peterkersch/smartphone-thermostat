package com.thermostat.server.temperature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SensorTagTemperatureSensor implements TemperatureSensor {

	private static final String SCRIPT_PATH = "/usr/local/lib/python2.7/site-packages/sensortag.py";
	
	private ProcessBuilder procBuilder;
	
	public SensorTagTemperatureSensor(String mac) {
		// TODO: sanity check on MAC address
		procBuilder = new ProcessBuilder("python", SCRIPT_PATH, "-n", "1", "-T", mac);
	}
	
	public float getTemperature() throws IOException {
		Process p = procBuilder.start();
		InputStream out = p.getInputStream();
		try {
			int result = p.waitFor();
			if (result != 0) {
				throw new IOException("SensorTag python script exited with error code " + result);
			}
		} catch (InterruptedException e) {
			throw new IOException("Interrupted while waiting for SensorTag python script", e);
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(out));
		String line = null;

		while((line = in.readLine()) != null) {
			if (line.contains("Temp: ")) {
				return Float.parseFloat(line.substring(12).split(",")[0]);
			}
		}
		throw new IOException("SensorTag python script output did not contain temperature info");
	}

}
