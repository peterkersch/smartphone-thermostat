package com.thermostat.server.temperature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Holds temperature history for a given zone.
 */
public class TemperatureHistory {

	private static final long DEFAULT_MAX_HISTORY_SEC = 60*60*24;

	private static Logger logger = Logger.getLogger(TemperatureHistory.class.getSimpleName());

	private long maxHistorySec;
	
	private TemperatureRecord lastRecord = null;
	
	public TemperatureHistory(long maxHistorySec) {
		this.maxHistorySec = maxHistorySec;
	}
	
	public TemperatureHistory() {
		this(DEFAULT_MAX_HISTORY_SEC);
	}
	
	/** minute start timestamp -> list of temperature readings from this minute */
	private SortedMap<Long, List<TemperatureRecord>> history = new TreeMap<Long, List<TemperatureRecord>>();
	
	/**
	 * Store recorded temperature values.
	 * 
	 * @param ambientTemperature current ambient temperature for the given zone
	 * @param targetTemperature current target temperature for the given zone
	 */
	public synchronized void add(float ambientTemperature, float targetTemperature) {
		long timestampSecond = new Date().getTime()/1000;
		long timestampMinute = timestampSecond-timestampSecond%60;
		List<TemperatureRecord> l = history.get(timestampMinute);
		if (l == null) {
			l = new ArrayList<TemperatureRecord>();
			history.put(timestampMinute, l);
		}
		lastRecord = new TemperatureRecord(timestampSecond, ambientTemperature, targetTemperature);
		l.add(lastRecord);
		
		// Remove old entries outside the history window
		while (timestampMinute - history.firstKey() > maxHistorySec) {
			history.remove(history.firstKey());
		}
	}
	
	/**
	 * Get temperature trends for the specified time period.
	 * Temperature difference is calculated by simple linear regression of samples
	 * between now and periodMin minutes ago.
	 * 
	 * @param periodMin the number of minutes to look back
	 * @return average temperature changes in °C / minute
	 */
	public synchronized float getTrend(long periodMin) {
		if (lastRecord == null) {
			throw new IllegalStateException("No historical records available yet");
		}

		return getTrend(getRecentRecords(periodMin));
	}

	/**
	 * Compute average ambient temperature for the specified time period
	 * 
	 * @param periodMin the number of minutes to look back
	 * @return average temperature °C for the given period
	 */
	public synchronized float getAverage(long periodMin) {
		if (lastRecord == null) {
			throw new IllegalStateException("No historical records available yet");
		}

		return getAverage(getRecentRecords(periodMin));
	}

	public Float getCurrentAmbientTemperature() {
		return lastRecord == null ? null : lastRecord.ambientTemperature;
	}
	
	public Float getCurrentTargetTemperature() {
		return lastRecord == null ? null : lastRecord.targetTemperature;
	}
	
	private List<TemperatureRecord> getRecords(long minute) {
		List<TemperatureRecord> readings = history.get(minute);
		return readings == null ? new ArrayList<TemperatureRecord>() : Collections.unmodifiableList(readings);
	}
	
	/**
	 * Get temperature records from the past periodMin minutes.
	 * 
	 * @param periodMin the number of minutes to look back
	 * @return a list of temperature records
	 */
	private List<TemperatureRecord> getRecentRecords(long periodMin) {
		List<TemperatureRecord> records = new ArrayList<TemperatureRecord>();
		long currentTimestamp = new Date().getTime()/1000;
		long currentMinute = currentTimestamp-currentTimestamp%60;
		for (long min = currentMinute-periodMin*60; min<=currentMinute; min+=60) {
			List<TemperatureRecord> l = getRecords(min);
			if (l != null) {
				for (TemperatureRecord r : l) {
					if (r.timestamp > currentTimestamp-periodMin*60) {
						records.add(r);
					}
				}
			}
		}
		return records;
	}
	
	private float getAverage(Collection<TemperatureRecord> records) {
		if (records.size() < 1) {
			throw new IllegalArgumentException("At least 1 record is required to compute average");
		}

		int n = records.size();
		float sum = 0;
		for (TemperatureRecord r : records) {
			sum += r.ambientTemperature;
		}
		return sum / n;
	}
	
	private float getTrend(Collection<TemperatureRecord> records) {
		if (records.size() < 2) {
			throw new IllegalArgumentException("At least 2 records are required to compute beta coefficient of simple linear regression");
		}
		
		int n = records.size();
		float corrXY = 0;
		float sumX = 0;
		float sumY = 0;
		float sqrSumX = 0;
		for (TemperatureRecord r : records) {
			corrXY += r.timestamp*r.ambientTemperature;
			sumX += r.timestamp;
			sumY += r.ambientTemperature;
			sqrSumX += r.timestamp*r.timestamp;
		}
		return (corrXY-sumX*sumY/n)/(sqrSumX-sumX*sumX/n)*60; 
	}

	
	private class TemperatureRecord {
		
		long timestamp;
		float ambientTemperature;
		float targetTemperature;
		
		public TemperatureRecord(long timestamp, float ambientTemperature, float targetTemperature) {
			this.timestamp = timestamp;
			this.ambientTemperature = ambientTemperature;
			this.targetTemperature = targetTemperature;
		}
	}
}
