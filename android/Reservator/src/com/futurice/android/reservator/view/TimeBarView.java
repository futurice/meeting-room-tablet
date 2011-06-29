package com.futurice.android.reservator.view;

import java.util.Calendar;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.TimeSpan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

public class TimeBarView extends FrameLayout{
	int animStep = 60000;
	private long startDelta = 0, endDelta = 0;
	private TimeSpan targetTimeSpan = null;
	TextView durationLabel;
	boolean animationEnabled = false;
	Thread animatorThread = null;
	TimeSpan limits, span;
	private static final int MIN_SPAN_LENGTH = 60*120*1000;

	Drawable background;
	Drawable reservationOwn;
	Drawable reservationOther;
	int tickColor;

	public TimeBarView(Context context) {
		this(context, null);
	}
	public TimeBarView(Context context, AttributeSet attrs){
		super(context, attrs);
		inflate(context, R.layout.time_bar, this);
		durationLabel = (TextView)findViewById(R.id.textView1);
		this.setTimeLimits(new TimeSpan(null,Calendar.HOUR, 2));
		this.setSpan(new TimeSpan(null,Calendar.MINUTE, 90));
		// span.getStart().add(Calendar.MINUTE, 30); // XXX: check why

		background = getResources().getDrawable(R.drawable.timeline);
		reservationOwn = getResources().getDrawable(R.drawable.oma_varaus);
		reservationOther = getResources().getDrawable(R.drawable.muu_varaus);
		tickColor = getResources().getColor(R.color.TimeBarTickColor);
	}
	public void setTimeLimits(TimeSpan span){
		this.limits = span.clone();
		invalidate();
	}
	public void enableAnimation(){
		animationEnabled = true;
	}
	public void disableAnimation(){
		animationEnabled = false;
	}
	public void setSpan(TimeSpan span){
		if(this.span == null || !animationEnabled){
			this.span = span;
			this.targetTimeSpan = span;
			return;
		}
		targetTimeSpan = span;
		startDelta = span.getStart().getTimeInMillis() - this.span.getStart().getTimeInMillis();
		endDelta = span.getEnd().getTimeInMillis() - this.span.getEnd().getTimeInMillis() ;
		animStep = (int)Math.max(Math.max(Math.abs(endDelta), Math.abs(startDelta)) / 10, 60000);


		if (animatorThread == null){
			animatorThread = new Thread(){
				public void run(){
					while(Math.abs(startDelta) <  animStep || Math.abs(endDelta) < animStep){
						TimeBarView.this.span = new TimeSpan(
								TimeBarView.this.span.getStart().add(Calendar.MILLISECOND, (int)Math.signum(startDelta) * animStep),
								TimeBarView.this.span.getEnd().add(Calendar.MILLISECOND, (int)Math.signum(endDelta) * animStep));

						startDelta -= Math.signum(startDelta) * animStep;
						endDelta -= Math.signum(endDelta) * animStep;
						postInvalidate();
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					TimeBarView.this.span = targetTimeSpan;
					postInvalidate();
					animatorThread = null;
				}
			};
			animatorThread.start();
		}
		invalidate();
	}
	@Override
	public void dispatchDraw(Canvas c){



		super.dispatchDraw(c);
		Paint p = new Paint();
		p.setColor(getResources().getColor(R.color.ReserveLine));

		int startCenterX = getWidth() / 4;
		int endCenterX = getWidth() / 4 * 3;
		int w = 10;
		int left = 0;
		int bottom = durationLabel.getTop();
		int right = getWidth();
		int y = 0;

		final int padding = durationLabel.getTop() / 6;

		//The horizontal lines
		c.drawLine(startCenterX - w, y, startCenterX + w, y, p);
		c.drawLine(endCenterX - w, y, endCenterX + w, y, p);
		//Static vertical lines
		c.drawLine(startCenterX , y, startCenterX, y + padding + 1, p);
		c.drawLine(endCenterX, y, endCenterX, y + padding * 2 + 1, p); // 2* to shift the other line to bit lower
		y += padding;

		int width = getWidth();
		int startX = (int)(width * getProportional(span.getStart()));
		int endX = (int)(width * getProportional(span.getEnd()));
		//dynamic horizontal lines
		c.drawLine(startCenterX, y, startX, y, p);
		c.drawLine(endCenterX, y + padding, endX, y + padding , p);
		//and the vertical ones
		c.drawLine(startX, y, startX, bottom, p);
		c.drawLine(endX, y + padding, endX, bottom, p);

		y += 2*padding;

		// Background
		background.setBounds(left, y, right, bottom);
		background.draw(c);

		// Reservation
		reservationOwn.setBounds(startX, y, endX, bottom);
		reservationOwn.draw(c);

		// Other reservation
		if(span.getLength() < MIN_SPAN_LENGTH){
			reservationOther.setBounds((int) (width * getProportional(limits.getEnd())), y, width, bottom);
			reservationOther.draw(c);
		}

		// Ticks
		p.setStyle(Style.STROKE);
		p.setColor(tickColor);
		DateTime time = limits.getStart();
		DateTime end = getMaximum();

		while(time.before(end)){
			if(time.get(Calendar.MINUTE) % 30 != 0){
				time = time.add(Calendar.MINUTE, 30 - time.get(Calendar.MINUTE) % 30);
			}
			int x = (int)(width * getProportional(time));
			c.drawLine(x, y, x, bottom, p);

			time = time.add(Calendar.MINUTE, 30);
		}

		// Duration label
		durationLabel.setText(span.getLength() / 60000 + " minutes");

		/*p.setColor(getResources().getColor(R.color.TimeSpanTextColor));
		String durationText = span.getLength() / 60000 + " minutes";
		int textWidth = (int) p.measureText(durationText);
		int textX = startX + (endX - startX - textWidth ) / 2;

		c.drawText( durationText, textX > startX ? textX : startX, bottom + padding + p.getTextSize(), p);*/
	}

	private DateTime getMaximum() {
		return limits.getStart().add(Calendar.MILLISECOND, (int) (limits.getLength() >  MIN_SPAN_LENGTH ? limits.getLength() : MIN_SPAN_LENGTH));
	}

	private float getProportional(DateTime time){
		return (time.getTimeInMillis() - limits.getStart().getTimeInMillis()) / (float)(limits.getLength() >  MIN_SPAN_LENGTH ? limits.getLength() : MIN_SPAN_LENGTH);
	}
}
