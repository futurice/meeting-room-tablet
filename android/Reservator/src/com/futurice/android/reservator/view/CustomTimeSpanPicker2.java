package com.futurice.android.reservator.view;

import java.util.Calendar;

import com.futurice.android.reservator.R;

import android.widget.FrameLayout;
import android.widget.TextView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

public class CustomTimeSpanPicker2 extends FrameLayout implements OnClickListener {
	View startMinus, startPlus, endMinus, endPlus;
	TextView startLabel, endLabel;

	Calendar currentDay;

	int currentTimeStart;
	int currentTimeEnd;

	int minimumTime;
	int maximumTime;

	int minimumDuration;
	int timeStep;

	public CustomTimeSpanPicker2(Context ctx){
		this(ctx, null);
	}

	/*
	 * Invariants:
	 *
	 * minimumTime <= currentTimeStart <= currentTimeEnd <= maximumTime
	 *
	 * Assumptions:
	 * 24 hours in day, 60 minutes in hour!
	 */

	public CustomTimeSpanPicker2(Context ctx, AttributeSet attrs){
		super(ctx, attrs);
		inflate(ctx, R.layout.timespan_picker2, this);

		startMinus = findViewById(R.id.startMinus);
		startPlus = findViewById(R.id.startPlus);
		endMinus = findViewById(R.id.endMinus);
		endPlus = findViewById(R.id.endPlus);

		startMinus.setOnClickListener(this);
		startPlus.setOnClickListener(this);
		endMinus.setOnClickListener(this);
		endPlus.setOnClickListener(this);

		startLabel = (TextView) findViewById(R.id.startTimeLabel);
		endLabel = (TextView) findViewById(R.id.endTimeLabel);

		minimumTime = 0;
		maximumTime = 24*60;

		minimumDuration = 15;
		timeStep = 15;

		currentDay = Calendar.getInstance();
		currentTimeStart = minimumTime;
		currentTimeEnd = maximumTime;

		refreshLabels();
	}

	@Override
	public void onClick(View v) {
		if (v == startMinus) {
			int start = currentTimeStart - timeStep;

			currentTimeStart = Math.max(start, minimumTime);
			refreshLabels();
		}
		else if (v == startPlus) {
			int start = currentTimeStart + timeStep;

			currentTimeStart = Math.min(start, currentTimeEnd-minimumDuration);
			refreshLabels();
		}
		else if (v == endMinus) {

		}
		else if (v == endPlus) {

		}
	}

	protected void refreshLabels() {
		startLabel.setText(String.format("%02d:%02d", currentTimeStart / 60, currentTimeStart % 60));
		endLabel.setText(String.format("%02d:%02d", currentTimeEnd / 60, currentTimeEnd % 60));
	}

	public void setMinimumDuration(int minimumDuration) {
		this.minimumDuration = minimumDuration;
	}

	public void setTimeStep(int timeStep) {
		this.timeStep = timeStep;
	}


	public void setMinimumTime(Calendar cal) {
		currentDay = (Calendar) cal.clone(); // set the current day

		int min = cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);
		if (min > maximumTime)
			throw new IllegalArgumentException("setting minimumTime to be after the maximum");

		minimumTime = min;
		if (currentTimeStart < minimumTime) {
			currentTimeStart = minimumTime;
		}

		if (currentTimeEnd < minimumTime) {
			currentTimeEnd = Math.min(maximumTime, minimumTime + minimumDuration);
		}

		refreshLabels();
	}

	public void setMaximumTime(Calendar cal) {
		throw new RuntimeException("unimplemented");
	}


	/**
	 * Duration of the selected time span in minutes.
	 *
	 * @return duration in minutes.
	 */
	public int getDuration() {
		return currentTimeEnd - currentTimeStart;
	}

	public Calendar getStartTime() {
		Calendar ret = (Calendar) currentDay.clone();

		ret.set(Calendar.HOUR_OF_DAY, currentTimeStart / 60);
		ret.set(Calendar.MINUTE, currentTimeEnd % 60);
		ret.set(Calendar.SECOND, 0);
		ret.set(Calendar.MILLISECOND, 0);

		return ret;
	}

	public Calendar getEndTime() {
		throw new RuntimeException("unimplemented");
	}

}
