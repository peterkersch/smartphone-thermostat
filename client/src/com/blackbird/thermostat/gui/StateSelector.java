package com.blackbird.thermostat.gui;

import java.util.ArrayList;
import java.util.List;

import com.blackbird.thermostat.Constants;
import com.blackbird.thermostat.ThermostatBackgroundService;
import com.blackbird.thermostat.ThermostatController;
import com.thermostat.protocol.data.ResidentState;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class StateSelector extends LinearLayout {

	private ThermostatController controller;
	
	private CheckListener listener;
	
	List<ToggleButton> buttons = new ArrayList<ToggleButton>();

	LinearLayout.LayoutParams layoutParams;
	
	public StateSelector(Context context, AttributeSet attrs) {
		super(context, attrs);
		layoutParams = new LayoutParams(context, attrs);
	}

	public StateSelector(Context context) {
		super(context, null);
	}

	public void setController(ThermostatController controller) {
		this.controller = controller;
		layoutParams.weight = 1.0f/controller.getStates().size();
		listener = new CheckListener(); 
		int i = 0;
		int selected = controller.getCurrentStateIndex();
		for (ResidentState state : controller.getStates()) {
			ToggleButton button = new ToggleButton(getContext());
//			button.setLayoutParams(layoutParams);
			button.setTextOn(state.getName());
			button.setTextOff(state.getName());
			button.setTypeface(null, selected == i ? Typeface.BOLD : Typeface.NORMAL);
			button.setChecked(selected == i);
			button.setEnabled(selected != i);
			button.setOnCheckedChangeListener(listener);
			buttons.add(button);
			addView(button);
			++i;
		}
	}
	
	private class CheckListener implements CompoundButton.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton button, boolean checked) {
			if (!checked) {
				// Should not happen
				return;
			}
			
			/* Deactivate previous selection */
			ToggleButton previousButton = buttons.get(controller.getCurrentStateIndex());
			if (previousButton != null) {
				previousButton.setChecked(false);
				previousButton.setTypeface(null, Typeface.NORMAL);
				previousButton.setEnabled(true);
			}
			
			/* Activate new selection */
			button.setTypeface(null, Typeface.BOLD);
			button.setEnabled(false);
			
			/* Notify controller */
			controller.setCurrentStateIndex(buttons.indexOf(button));
			
			/* Send update to thermostat controller */
		    Intent intent = new Intent(Constants.STATE_UPDATE_ACTION);
			LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
		}
		
	}
}
