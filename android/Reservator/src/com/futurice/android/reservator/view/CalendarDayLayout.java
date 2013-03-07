package com.futurice.android.reservator.view;

import java.util.Calendar;

import com.futurice.android.reservator.model.DateTime;


import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class CalendarDayLayout extends ViewGroup {
	int minuteStart = 8 * 60, minuteEnd = 20 * 60;
	public CalendarDayLayout(Context context) {
		this(context, null);
	}
	public CalendarDayLayout(Context context, AttributeSet attrs){
		super(context, attrs);
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed) {
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				CalendarMarker marker = (CalendarMarker) getChildAt(i);
				marker.layout(0,
						(int) ((b - t) * getProportionalYPosition(marker
								.getTimeSpan().getStart())), r - l,
						(int) ((b - t) * getProportionalYPosition(marker
								.getTimeSpan().getEnd())));
			}
		}

	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightmeasureSpec) {

		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightmeasureSpec);

		int count = getChildCount();
		int maxWidth = 10;
		for (int i = 0; i < count; i++) {
			CalendarMarker marker = (CalendarMarker) getChildAt(i);
			int markerWidth = width;
			int markerHeight = (int) (height * (getProportionalYPosition(marker
					.getTimeSpan().getEnd()) - getProportionalYPosition(marker
					.getTimeSpan().getStart())));
			marker.measure(MeasureSpec.makeMeasureSpec(markerWidth,
					MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(
					markerHeight, MeasureSpec.EXACTLY));

			maxWidth = Math.max(maxWidth, MeasureSpec.getSize(marker.getMeasuredWidth()));
		}
		if(getLayoutParams().width == LayoutParams.WRAP_CONTENT){
			setMeasuredDimension(Math.min(width, maxWidth), height);
		}else{
			setMeasuredDimension(width, height);
		}
	}

	private float getProportionalYPosition(DateTime time) {
		return (time.get(Calendar.MINUTE) + time.get(Calendar.HOUR_OF_DAY) * 60 - minuteStart)
				/ (float) (minuteEnd - minuteStart);
	}
}
