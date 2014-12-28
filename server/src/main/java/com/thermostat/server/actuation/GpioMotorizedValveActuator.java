package com.thermostat.server.actuation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thermostat.server.actuation.gpio.GpioDirection;
import com.thermostat.server.actuation.gpio.GpioPort;
import com.thermostat.server.temperature.TemperatureHistory;

/**
 * Radiator control stepping up and down manual temperature settings of a motorized radiator valve via GPIO.
 * This is a "blind" control with no direct radiator valve status feedback.
 * Slow feedback is available only via the external temperature sensor.
 * 
 * TODO: sanity check on GPIO input
 */
public class GpioMotorizedValveActuator extends Actuator {

	private static Logger logger = Logger.getLogger(GpioMotorizedValveActuator.class.getSimpleName());

	/** Time to wait after startup */
	private static final int WARMUP_TIMER_MINUTES = 1;

	/** Time to wait for checking temperature trends after having changed valve target temperature */
	private static final int CHANGE_TIMER_MINUTES = 10;	

	/** Timer to emulate turn knob switches */
	private static final int TURN_KNOB_EMULATION_TIMER_MS = 50;
	
	/** Temperature change unit */
	private static final double TEMPERATURE_STEP = 0.5;
	
	/** Minimum temperature difference of ambient temperature compared to target temperature to start valve temperature adjustment */
	private static final double TEMPERATURE_ADJUSTMENT_THRESHOLD = 0.25;
	
	private static final int GPIO_OUTPUT_1 = 17;
	private static final int GPIO_OUTPUT_2 = 27;
//	private static final int GPIO_INPUT_1 = 9;
//	private static final int GPIO_INPUT_2 = 11;

	/** Assumed temperature currently set on the motorized radiator valve, measured in TEMPERATURE_STEP units */
	private Long valveTemperatureUnits = null;
	
	private long startTimestamp;
	
	private long lastUpdate;
	
	private GpioPort gpioOut1;
	
	private GpioPort gpioOut2;
	
	/** Port output value sequence to turn up target temperature by one unit */
	private List<GpioPortValue> upSequence;

	/** Port output value sequence to turn down target temperature by one unit */
	private List<GpioPortValue> downSequence;

	public GpioMotorizedValveActuator() throws IOException {
		startTimestamp = new Date().getTime()/1000;
		lastUpdate = startTimestamp;

		// Initialize GPIO ports and output value sequences
		gpioOut1 = new GpioPort(GPIO_OUTPUT_1, GpioDirection.OUT);
		gpioOut2 = new GpioPort(GPIO_OUTPUT_2, GpioDirection.OUT);
		upSequence = Arrays.asList(new GpioPortValue(gpioOut2, true),
				new GpioPortValue(gpioOut1, true),
				new GpioPortValue(gpioOut2, false),
				new GpioPortValue(gpioOut1, false));
		downSequence = Arrays.asList(new GpioPortValue(gpioOut1, true),
				new GpioPortValue(gpioOut2, true),
				new GpioPortValue(gpioOut1, false),
				new GpioPortValue(gpioOut2, false));
	}
	
	public synchronized void update(TemperatureHistory temperatureHistory) throws IOException {
		double ambientTemperature = temperatureHistory.getCurrentAmbientTemperature();
		double targetTemperature = temperatureHistory.getCurrentTargetTemperature();
		
		long timestamp = new Date().getTime()/1000;
		if (timestamp - startTimestamp < WARMUP_TIMER_MINUTES*60) {
			return;
		}
		
		long ambientTemperatureUnits = Math.round(ambientTemperature / TEMPERATURE_STEP);
		long targetTemperatureUnits = Math.round(targetTemperature / TEMPERATURE_STEP);
		if (valveTemperatureUnits == null) {
			// Assume that the radiator valve is already in an stabilized state
			valveTemperatureUnits = ambientTemperatureUnits;
		}

		long delta = valveTemperatureUnits - targetTemperatureUnits;
		logger.info("Estimated valve temperature: " + valveTemperatureUnits*TEMPERATURE_STEP);
		if (delta < 0) {
			for (; valveTemperatureUnits < targetTemperatureUnits; ++valveTemperatureUnits) {
				playSequence(upSequence);
			}
			lastUpdate = timestamp;
		} else if (delta > 0) {
			for (; valveTemperatureUnits > targetTemperatureUnits; --valveTemperatureUnits) {
				playSequence(downSequence);
			}
			lastUpdate = timestamp;
		} else if (timestamp - lastUpdate > CHANGE_TIMER_MINUTES*60) {
			verifyTemperatureTrends(temperatureHistory);
		}
	}

	/**
	 * Play a GPIO output sequence to increase or decrease target temperature by one unit
	 * @throws IOException 
	 */
	private void playSequence(List<GpioPortValue> sequence) throws IOException {
		logger.info((sequence == upSequence ? "Increasing" : "Decreasing") + " valve temperature from " + (valveTemperatureUnits * TEMPERATURE_STEP));
		for (GpioPortValue gpio : sequence) {
			gpio.port.set(gpio.value);
			try {
				Thread.sleep(TURN_KNOB_EMULATION_TIMER_MS);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, "Interrupted while emulating turn knob sequence.", e);
			}
		}
	}
	
	private void verifyTemperatureTrends(TemperatureHistory temperatureHistory) throws IOException {
		double targetTemperature = temperatureHistory.getCurrentTargetTemperature();

		double tenMinTrends = temperatureHistory.getTrend(10);
		double tenMinAvg = temperatureHistory.getAverage(10);
		logger.info("Temperature statistics in the past 10 minutes: average: " + tenMinAvg + ", trends: " + tenMinTrends + "/min");
		long timestamp = new Date().getTime()/1000;
		
		// Below target temperature with at least 1°C and still cooling 
		if (targetTemperature - tenMinAvg > TEMPERATURE_ADJUSTMENT_THRESHOLD && tenMinTrends < 0) {
			playSequence(upSequence);
			lastUpdate = timestamp;
		}
		// Above target temperature with at least 1°C and still warming up 
		if (targetTemperature - tenMinAvg < -TEMPERATURE_ADJUSTMENT_THRESHOLD && tenMinTrends > 0) {
			playSequence(downSequence);
			lastUpdate = timestamp;
		}
	}
	
	public double getStatus() throws IOException {
		return 0;
	}

	private class GpioPortValue {
		GpioPort port;
		boolean value;
		
		public GpioPortValue(GpioPort port, boolean value) {
			this.port = port;
			this.value = value;
		}
	}
}
