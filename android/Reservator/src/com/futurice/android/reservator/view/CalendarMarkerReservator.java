package com.futurice.android.reservator.view;

import com.futurice.android.reservator.R;

import android.content.Context;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;

public class CalendarMarkerReservator extends FrameLayout implements
		OnTouchListener {
	View beginDragger, endDragger, okButton, cancelButton, innerLayout;
	int bottomLimit, topLimit, minPaddingTop, minPaddingBot;

	public CalendarMarkerReservator(Context ctx) {
		this(ctx, null);
	}
	
	public CalendarMarkerReservator(Context context, AttributeSet attrs) {
		super(context, attrs);
		innerLayout = inflate(context, R.layout.calendar_marker_reservation,
				this);
		beginDragger = findViewById(R.id.beginDragger);
		beginDragger.setOnTouchListener(this);
		endDragger = findViewById(R.id.endDragger);
		endDragger.setOnTouchListener(this);
		okButton = findViewById(R.id.okButton);
		cancelButton = findViewById(R.id.cancelButton);
	}

	@Override
	protected void onSizeChanged(int w, int h, int ow, int oh){
		//setLimits(topLimit, bottomLimit);
	}
	
	public void setLimits(int topLimit, int bottomLimit) {
		this.topLimit = topLimit;
		this.bottomLimit = bottomLimit;
		this.minPaddingTop = this.topLimit - beginDragger.getHeight();
		this.minPaddingBot = getHeight() - bottomLimit - okButton.getHeight() - endDragger.getHeight();
		this.setPadding(0, this.minPaddingTop, 0, minPaddingBot);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == beginDragger) {
			int padding = innerLayout.getPaddingTop()
					- beginDragger.getHeight() + (int) event.getY();
			if (getHeight() < padding + innerLayout.getPaddingBottom()
					+ endDragger.getHeight() + beginDragger.getHeight()
					+ okButton.getHeight()) {
				padding = getHeight()
						- (innerLayout.getPaddingBottom()
								+ endDragger.getHeight()
								+ beginDragger.getHeight() + okButton
								.getHeight());
			}
			innerLayout.setPadding(0, padding > minPaddingTop ? padding : minPaddingTop,
					0, innerLayout.getPaddingBottom());

		} else if (v == endDragger) {
			int padding = innerLayout.getPaddingBottom()
					+ endDragger.getHeight() - (int) event.getY();
			if (getHeight() < padding + innerLayout.getPaddingTop()
					+ endDragger.getHeight() + beginDragger.getHeight()
					+ okButton.getHeight()) {
				padding = getHeight()
						- (innerLayout.getPaddingTop() + endDragger.getHeight()
								+ beginDragger.getHeight() + okButton
								.getHeight());
			}
			innerLayout.setPadding(0, innerLayout.getPaddingTop(), 0,
					padding > minPaddingBot ? padding : minPaddingBot);
		}
		return true;
	}
}
