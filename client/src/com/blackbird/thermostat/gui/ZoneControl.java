package com.blackbird.thermostat.gui;

import com.blackbird.thermostat.Constants;
import com.blackbird.thermostat.ThermostatController;
import com.thermostat.protocol.data.ZoneData;

import android.os.Parcelable;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * UI component holding zone control elements in a table structure.
 * Each zone corresponds to one row in this table.
 */
public class ZoneControl extends TableLayout {

	private static final String TAG = ZoneControl.class.getSimpleName();

	/* Constants to define unique local view IDs for UI components */
	private static final int ID_FACTOR = 256;
	private static final int ID_OFFSET_ZONE_NAME = 1;
	private static final int ID_OFFSET_CURRENT_TEMPERATURE = 2;
	private static final int ID_OFFSET_TARGET_TEMPERATURE = 3;
	private static final int ID_OFFSET_BOOST = 4;
	
	/* Boost settings */
	private static final float BOOST_STEP = 0.5f;
	private static final float MAX_BOOST = 4.0f;
		
	private static TableRow.LayoutParams params2;
	private static TableRow.LayoutParams params3;
	
	private ThermostatController controller;
	
	private BoostListener boostListener;
	
	static {
		params2 = new TableRow.LayoutParams();
		params2.weight = 0.2f;
		params3 = new TableRow.LayoutParams();
		params3.weight = 0.3f;
	}
	
	public ZoneControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TableRow header = new TableRow(context);
		TextView label1 = new TextView(context);
		label1.setText("zone");
		label1.setLayoutParams(params3);
		header.addView(label1);
		TextView label2 = new TextView(context);
		label2.setText("current");
		label2.setLayoutParams(params2);
		header.addView(label2);
		TextView label3 = new TextView(context);
		label3.setText("target");
		label3.setLayoutParams(params2);
		header.addView(label3);
		TextView label4 = new TextView(context);
		label4.setText("boost");
		label4.setLayoutParams(params3);
		header.addView(label4);
		addView(header);
	}
	
	public ZoneControl(Context context) {
		this(context, null);
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
	}
	
	public void setController(ThermostatController controller) {
		this.controller = controller;
		for (ZoneData zone : controller.getZones()) {
			updateZone(zone);
		}
	}
	
	public synchronized void updateZone(ZoneData zone) {
		int id = controller.indexOf(zone);
		ZoneControlRow row = (ZoneControlRow)findViewById(id*ID_FACTOR);
		if (row == null) {
			row = new ZoneControlRow(getContext(), id);
			addView(row);
		}
		
		if (zone != null) {
			TextView zoneName = (TextView)findViewById(id*ID_FACTOR + ID_OFFSET_ZONE_NAME);
			zoneName.setText("" + zone.displayName);
			TextView currentTemperature = (TextView)findViewById(id*ID_FACTOR + ID_OFFSET_CURRENT_TEMPERATURE);
			currentTemperature.setText("" + zone.currentTemperature);
			setNetworkBoost(id, zone);
		}
	}
	
	private synchronized void setUserBoost(int zoneId, int progress, boolean completed) {
		TextView targetText = (TextView)findViewById(zoneId*ID_FACTOR+ID_OFFSET_TARGET_TEMPERATURE);
		ZoneData zoneData = controller.getZones().get(zoneId);
		if (progress == 0) {
			targetText.setTextColor(Color.BLACK);
			zoneData.boostTemperature = null;
			targetText.setText("" + zoneData.targetTemperature);
		} else {
			targetText.setTextColor(Color.RED);
			zoneData.boostTemperature = zoneData.targetTemperature + BOOST_STEP * progress;
			targetText.setText("" + zoneData.boostTemperature);
		}
		
		if (completed) {
			Intent localIntent = new Intent(Constants.ZONE_UPDATE_ACTION);
			localIntent.putExtra(Constants.EXTENDED_ZONE_DATA, zoneData);
			LocalBroadcastManager.getInstance(getContext()).sendBroadcast(localIntent);
		}
		Log.d(TAG, "User boost input: " + zoneData.boostTemperature + ", target: " + zoneData.targetTemperature);
	}
	
	private synchronized void setNetworkBoost(int zoneId, ZoneData zoneData) {
		TextView targetText = (TextView)findViewById(zoneId*ID_FACTOR+ID_OFFSET_TARGET_TEMPERATURE);
		SeekBar boost = (SeekBar)findViewById(zoneId*ID_FACTOR+ID_OFFSET_BOOST);
		if (zoneData.boostTemperature == null) {
			targetText.setTextColor(Color.BLACK);
			targetText.setText("" + zoneData.targetTemperature);
			boost.setProgress(0);
		} else {
			targetText.setTextColor(Color.RED);
			targetText.setText("" + zoneData.boostTemperature);
			int progress = (int)((zoneData.boostTemperature - zoneData.targetTemperature)/BOOST_STEP);
			boost.setProgress(progress);
		}
	}
	
	private BoostListener getBoostListener() {
		if (boostListener == null) {
			boostListener = new BoostListener();
		}
		return boostListener;
	}
	
	private class ZoneControlRow extends TableRow {

		public ZoneControlRow(Context context, int id) {
			super(context);
			int baseId = id*ID_FACTOR;
			setId(baseId);

			// Create components and set view IDs
			TextView zoneName = new TextView(context);
			zoneName.setId(baseId+ID_OFFSET_ZONE_NAME);
			TextView currentTemperature = new TextView(context);
			currentTemperature.setId(baseId+ID_OFFSET_CURRENT_TEMPERATURE);
			TextView targetTemperature = new TextView(context);
			targetTemperature.setId(baseId+ID_OFFSET_TARGET_TEMPERATURE);
			SeekBar boost = new SeekBar(context);
			boost.setProgress(0);
			boost.setMax((int)(MAX_BOOST/BOOST_STEP));
			boost.setId(baseId+ID_OFFSET_BOOST);

			// Set layout parameters
			zoneName.setLayoutParams(params3);
			currentTemperature.setLayoutParams(params2);
			targetTemperature.setLayoutParams(params2);
			boost.setLayoutParams(params3);
			
			// add components to parent view
			addView(zoneName);
			addView(currentTemperature);
			addView(targetTemperature);
			addView(boost);

			boost.setOnSeekBarChangeListener(getBoostListener());
		}
	}
	
	private class BoostListener implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				int zoneId = seekBar.getId()/ID_FACTOR;
				setUserBoost(zoneId, progress, false);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int zoneId = seekBar.getId()/ID_FACTOR;
			int progress = seekBar.getProgress();
			setUserBoost(zoneId, progress, true);
		}
	}
}
