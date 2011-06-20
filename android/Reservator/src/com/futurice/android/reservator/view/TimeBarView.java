package com.futurice.android.reservator.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.TimeSpan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class TimeBarView extends FrameLayout{
	
	TimeSpan limits, span;
	List<TimeSpan> reservations = new ArrayList<TimeSpan>();
	public TimeBarView(Context context) {
		this(context, null);
	}
	public TimeBarView(Context context, AttributeSet attrs){
		super(context, attrs);
		this.setTimeLimits(new TimeSpan(null,Calendar.HOUR, 2));
		
		this.setSpan(new TimeSpan(null,Calendar.MINUTE, 90));
		span.getStart().add(Calendar.MINUTE, 30);
	}
	public void setTimeLimits(TimeSpan span){
		int minSpanLength = 60*120*1000;
		if(span.getLength() < minSpanLength){
			Calendar end = (Calendar)span.getStart().clone();
			end.add(Calendar.MILLISECOND, minSpanLength);
			this.limits = new TimeSpan(span.getStart(), end);
			return;
		}
		this.limits = span.clone();
		invalidate();
	}
	public void setSpan(TimeSpan span){
		this.span = span;
		invalidate();
	}
	public void addReservation(TimeSpan span){
		reservations.add(span);
		invalidate();
	}
	@Override
	public void dispatchDraw(Canvas c){
		super.dispatchDraw(c);
		Paint p = new Paint();
		p.setColor(Color.argb(255, 0, 128, 0));
		
		int startCenterX = getWidth() / 4;
		int endCenterX = getWidth() / 4 * 3;
		int w = 10;
		int left = 0;
		int right = getWidth();
		int y = 0;
		
		 
		final int padding = getHeight() / 5;;

		//The static lines
		c.drawLine(startCenterX - w, y, startCenterX + w, y, p);
		c.drawLine(endCenterX - w, y, endCenterX + w, y, p);
		c.drawLine(startCenterX , y, startCenterX, y + padding, p);
		c.drawLine(endCenterX, y, endCenterX, y + padding, p);
		y += padding;
		
		
		int width = getWidth(); 
		int startX = (int)(width * getProportional(span.getStart()));
		int endX = (int)(width * getProportional(span.getEnd()));
		//dynamic horizontal lines
		c.drawLine(startCenterX, y, startX, y, p);
		c.drawLine(endCenterX, y, endX, y, p);
		//and the vertical ones
		c.drawLine(startX, y, startX, y + padding, p);
		c.drawLine(endX, y, endX, y + padding, p);
		
		y += padding;
		
		int bottom = y + 2 * padding;
		int radius = 2 * padding;
		p.setStyle(Style.FILL);
		p.setColor(Color.LTGRAY);
		c.drawRoundRect(new RectF(left, y, right, bottom), radius, radius, p);
		p.setColor(getResources().getColor(R.color.TimeSpanTextColor));
		c.drawRoundRect(new RectF(startX, y, endX, bottom), radius, radius, p);
		p.setColor(Color.GRAY);
		c.drawRoundRect(new RectF(left, y, startX, bottom), radius, radius, p);
		c.drawRoundRect(new RectF(endX, y, right, bottom), radius, radius, p);
		p.setStyle(Style.STROKE);
		p.setColor(Color.argb(255, 40, 40, 40));
		Calendar time = (Calendar)limits.getStart().clone();
		
		while(time.before(limits.getEnd())){
			if(time.get(Calendar.MINUTE) % 30 != 0){
				time.add(Calendar.MINUTE, 30 - time.get(Calendar.MINUTE) % 30);
			}
			int x = (int)(width * getProportional(time));
			c.drawLine(x, y, x, bottom, p);
			time.add(Calendar.MINUTE, 30);
		}
		p.setColor(getResources().getColor(R.color.TimeSpanTextColor));
		String durationText = span.getLength() / 60000 + " minutes"; 
		int textWidth = (int) p.measureText(durationText);
		int textX = startX + (endX - startX - textWidth ) / 2;
		
		c.drawText( durationText, textX > startX ? textX : startX, bottom + padding + p.getTextSize(), p);
	}
	private double getProportional(Calendar time){
		return (time.getTimeInMillis() - limits.getStart().getTimeInMillis()) / (double)limits.getLength();
	}
}
