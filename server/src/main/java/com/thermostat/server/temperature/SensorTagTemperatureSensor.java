package com.thermostat.server.temperature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Temperature sensor based on the TI SensorTag.
 * It requires bluepy (https://github.com/IanHarvey/bluepy).
 * 
 * Since reading data from SensorTag might require a couple of seconds,
 * reading is done by a background thread and latest reading is immediately returned
 * by getTemperature to avoid blocking for a long time.
 */
public class SensorTagTemperatureSensor implements TemperatureSensor {

	private static Logger logger = Logger.getLogger(SensorTagTemperatureSensor.class.getSimpleName());

	private static final String SCRIPT_PATH = "/usr/local/lib/python2.7/site-packages/sensortag.py";
	
	private static final int REFRESH_PERIOD_SEC = 120;
	
	private static final double REFRESH_PERIOD_MAX_JITTER = 0.2;
	
	private static final int REFRESH_TIMEOUT_SEC = 600;
	
	private Float lastTemperature;
	
	private Date lastUpdate;
	
	private PythonExecutor executor;
	
	private String mac;
	
	public SensorTagTemperatureSensor(String mac) {
		this.mac = mac;
		lastTemperature = null;
		lastUpdate = new Date();		
		executor = new PythonExecutor();

		// First temperature reading, blocks until success
		while (lastTemperature == null) {
			try {
				executor.getTemperature();
			} catch (IOException e) {
				logger.log(Level.WARNING, "", e);
			}
		}
		
		// Launch background update thread
		executor.start();
	}
	
	public float getTemperature() throws IOException {
		Date now = new Date();
		if ((now.getTime()-lastUpdate.getTime())/1000 < REFRESH_TIMEOUT_SEC) {
			return lastTemperature;
		} else {
			throw new IOException("No successfull SensorTag temperature readings from " + mac + 
					" in the past " + (now.getTime()-lastUpdate.getTime())/1000/60 + " minutes");
		}
	}

	private class PythonExecutor extends Thread {

		// TODO: sanity check on MAC address
		private ProcessBuilder procBuilder;

		public PythonExecutor() {
			procBuilder = new ProcessBuilder("python", SCRIPT_PATH, "-n", "1", "-T", mac);
		}
		
		@Override
		public void run() {
			while (true) {
				// Wait REFRESH_PERIOD_SEC +- random jitter 
				try {
					double jitter = REFRESH_PERIOD_MAX_JITTER * (Math.random() - 0.5) * 2; 
					Thread.sleep((long)(REFRESH_PERIOD_SEC*1000*(1+jitter)));
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "Interrupted during sleep between SensorTag temparature readings", e);
				}

				// Read temperature
				try {
					getTemperature();
				} catch (IOException e) {
					logger.log(Level.WARNING, "", e);
				}
			}
		}

		public void getTemperature() throws IOException {
			Process p = procBuilder.start();
			InputStream out = p.getInputStream();
			try {
				int result = p.waitFor();
				if (result != 0) {
					throw new IOException("SensorTag (" + mac + ") python script exited with error code " + result);
				}
			} catch (InterruptedException e) {
				throw new IOException("Interrupted while waiting for SensorTag python script", e);
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(out));
			String line = null;

			while((line = in.readLine()) != null) {
				if (line.contains("Temp: ")) {
					lastTemperature = Float.parseFloat(line.substring(12).split(",")[0]);
					lastUpdate = new Date();
					logger.info("Temparature read from " + mac + ": " + lastTemperature);
					return;
				}
			}
			throw new IOException("SensorTag (" + mac + ") python script output did not contain temperature info");
		}
	}
}
