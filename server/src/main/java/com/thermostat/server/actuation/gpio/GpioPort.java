package com.thermostat.server.actuation.gpio;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Manages one given GPIO port. 
 */
public class GpioPort {

	private static final File BASE_PROC_DIR = new File("/sys/class/gpio");
	
	private File gpioDir;
	
	private int id;
	
	private GpioDirection direction;
	
	/**
	 * 
	 * @param id
	 * @param direction
	 * @throws IOException
	 */
	public GpioPort(int id, GpioDirection direction) throws IOException {
		this.id = id;
		this.direction = direction;
		
		gpioDir = new File(BASE_PROC_DIR, "gpio" + id);
		if (!gpioDir.exists()) {
			// Export port if it has not been exported yet
			PrintWriter portExporter = new PrintWriter(new File(BASE_PROC_DIR, "export"));
			portExporter.print(id);
			portExporter.close();
		}
		// Set port direction
		PrintWriter directionSetter = new PrintWriter(new File(gpioDir, "direction"));
		directionSetter.print(direction.getName());
		directionSetter.close();
	}
	
	/**
	 * Set GPIO output
	 * 
	 * @param value true to turn on and false to turn off
	 */
	public void set(boolean value) throws IOException {
		if (direction != GpioDirection.OUT) {
			throw new IllegalArgumentException("GPIO direction has to be set to \"out\" before setting port output value");
		}
		PrintWriter valueSetter = new PrintWriter(new File(gpioDir, "value"));
		valueSetter.print(value ? 1 : 0);
		valueSetter.close();
	}

	// TODO: implement get()
	
	public int getId() {
		return id;
	}
	
	public GpioDirection getDirection() {
		return direction;
	}
}
