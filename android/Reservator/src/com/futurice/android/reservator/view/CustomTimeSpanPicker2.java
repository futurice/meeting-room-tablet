package com.futurice.android.reservator.view;

import java.util.Calendar;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.TimeSpan;

import android.widget.FrameLayout;
import android.widget.TextView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

public class CustomTimeSpanPicker2 extends FrameLayout implements OnClickListener {
	View startMinus, startPlus, endMinus, endPlus;
	TextView startLabel, endLabel;
	TimeBarView timeBar;
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
		timeBar = (TimeBarView)findViewById(R.id.timeBarView);

		minimumDuration = 15;
		timeStep = 15;



		reset();
	}

	public void reset() {
		minimumTime = 0;
		maximumTime = 24*60;

		currentDay = Calendar.getInstance();
		currentTimeStart = minimumTime;
		currentTimeEnd = maximumTime;
		refreshLabels();
	}

	protected int quantize(int m) {
		return (m / timeStep) * timeStep;
	}

	@Override
	public void onClick(View v) {
		if (v == startMinus) {
			int start = quantize(currentTimeStart - timeStep);

			currentTimeStart = Math.max(start, minimumTime);
			refreshLabels();
		}
		else if (v == startPlus) {
			int start = quantize(currentTimeStart + timeStep);

			currentTimeStart = Math.min(start, currentTimeEnd - minimumDuration);
			refreshLabels();
		}
		else if (v == endMinus) {
			int end = quantize(currentTimeEnd - timeStep);

			currentTimeEnd = Math.max(end, currentTimeStart + minimumDuration);
			refreshLabels();
		}
		else if (v == endPlus) {
			int end = quantize(currentTimeEnd + timeStep);

			currentTimeEnd = Math.min(end, maximumTime);
			refreshLabels();
		}
	}

	protected void refreshLabels() {
		startLabel.setText(String.format("%02d:%02d", currentTimeStart / 60, currentTimeStart % 60));
		endLabel.setText(String.format("%02d:%02d", currentTimeEnd / 60, currentTimeEnd % 60));

		Calendar start = (Calendar)currentDay.clone();
		start.add(Calendar.MINUTE, minimumTime);

		Calendar end = (Calendar)currentDay.clone();
		end.add(Calendar.MINUTE, maximumTime);
		timeBar.setTimeLimits(new TimeSpan(start, end));
		timeBar.setSpan(new TimeSpan(getStartTime(), getEndTime()));
	}

	public void setMinimumDuration(int minimumDuration) {
		this.minimumDuration = minimumDuration;
	}

	public void setTimeStep(int timeStep) {
		this.timeStep = timeStep;
	}


	public void setMinimumTime(Calendar cal) {
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

		currentDay = (Calendar) cal.clone(); // set current day

		refreshLabels();
	}

	public void setMaximumTime(Calendar cal) {
		int max = cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);
		boolean tomorrow = false;

		if (max == 0) {
			max = 24*60; // full day
			tomorrow =  true;
		}

		if (max < minimumTime)
			throw new IllegalArgumentException("setting maximumTime to be before the minimum");

		maximumTime = max;
		if (currentTimeEnd > maximumTime) {
			currentTimeEnd = maximumTime;
		}

		if (currentTimeStart > maximumTime) {
			currentTimeStart = Math.max(minimumTime, maximumTime - minimumDuration);
		}

		currentDay = (Calendar) cal.clone(); // set current day
		if (tomorrow) {
			currentDay.add(Calendar.DAY_OF_YEAR, -1);
		}

		refreshLabels();
	}

	public void setStartTime(Calendar cal) {
		int start = cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);

		if (start < minimumTime || start > maximumTime) {
			throw new IllegalArgumentException("setting startTime outside of minmax");
		}

		if (start > currentTimeEnd) {
			throw new IllegalArgumentException("setting startTime after endTime");
		}

		currentTimeStart = start;
		currentDay = (Calendar) cal.clone();

		refreshLabels();
	}

	public void setEndTime(Calendar cal) {
		int end = cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);

		if (end < minimumTime || end > maximumTime) {
			throw new IllegalArgumentException("setting endTime outside of minmax");
		}

		if (end < currentTimeStart) {
			throw new IllegalArgumentException("setting endTime before startTime");
		}

		currentTimeEnd = end;
		currentDay = (Calendar) cal.clone();

		refreshLabels();
	}

	public void setEndTimeRelatively(int minutes) {
		int end = quantize(currentTimeStart + minutes);

		currentTimeEnd = Math.min(end, maximumTime);
		refreshLabels();
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
		ret.set(Calendar.MINUTE, currentTimeStart % 60);
		ret.set(Calendar.SECOND, 0);
		ret.set(Calendar.MILLISECOND, 0);

		return ret;
	}

	public Calendar getEndTime() {
		Calendar ret = (Calendar) currentDay.clone();

		ret.set(Calendar.HOUR_OF_DAY, currentTimeEnd / 60);
		ret.set(Calendar.MINUTE, currentTimeEnd % 60);
		ret.set(Calendar.SECOND, 0);
		ret.set(Calendar.MILLISECOND, 0);

		return ret;
	}

}
