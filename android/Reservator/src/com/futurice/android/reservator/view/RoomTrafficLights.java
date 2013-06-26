package com.futurice.android.reservator.view;

import com.futurice.android.reservator.R;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

import com.futurice.android.reservator.common.Helpers;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.Reservation;

public class RoomTrafficLights extends RelativeLayout {
	TextView roomStatusView;
	TextView roomTitleView;
	TextView roomStatusInfoView;
	TextView reservationInfoView;
	Timer touchTimeoutTimer;
	final long TOUCH_TIMEOUT = 30 * 1000; 
	final long TOUCH_TIMER = 10 * 1000;
	boolean enabled = true;

	public RoomTrafficLights(Context context) {
		this(context, null);
	}
	
	public RoomTrafficLights(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.room_traffic_lights, this);
		roomTitleView = (TextView) findViewById(R.id.roomTitle);
		roomStatusView = (TextView) findViewById(R.id.roomStatus);
		roomStatusInfoView = (TextView) findViewById(R.id.roomStatusInfo);
		reservationInfoView = (TextView) findViewById(R.id.reservationInfo);
		
		setClickable(true);
		setVisibility(INVISIBLE);
		lastTouched = new Date().getTime();
		touchTimeoutTimer = new Timer();
		touchTimeoutTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (RoomTrafficLights.this.enabled && 
						new Date().getTime() >= RoomTrafficLights.this.lastTouched + TOUCH_TIMEOUT) {
					RoomTrafficLights.this.post(new Runnable() {
						public void run() {
							RoomTrafficLights.this.setVisibility(VISIBLE);
						}
					});
				}
			}
		}, TOUCH_TIMER, TOUCH_TIMEOUT);
	}
	
	final int ABSOLUTELY_FREE_MINUTES_THRESHOLD = 8*60; // 8 hours
	public void update(Room room) {
		roomTitleView.setText(room.getName());
		roomStatusView.setText(room.getStatusText());
		
		if (room.isBookable()) {
			if (room.isLongBookable()) {
				this.setBackgroundColor(getResources().getColor(R.color.TrafficLightFree));
				int freeMinutes = room.minutesFreeFromNow();
				if (freeMinutes > ABSOLUTELY_FREE_MINUTES_THRESHOLD) {
					roomStatusInfoView.setVisibility(INVISIBLE);
				} else {
					roomStatusInfoView.setText("for " + freeMinutes);
					roomStatusInfoView.setVisibility(VISIBLE);
				}
			} else {
				this.setBackgroundColor(getResources().getColor(R.color.TrafficLightYellow));
				reservationInfoView.setVisibility(INVISIBLE);
			}
			reservationInfoView.setVisibility(INVISIBLE);
		} else {
			this.setBackgroundColor(getResources().getColor(R.color.TrafficLightReserved));
			setReservationInfo(room.getCurrentReservation());
		}
	}
	
	private void setReservationInfo(Reservation r) {
		if (r == null) {
			reservationInfoView.setVisibility(INVISIBLE);
		}
		
		roomStatusInfoView.setText(r.getSubject());
		roomStatusInfoView.setVisibility(VISIBLE);
		
		DateTime from = new DateTime();
		DateTime to = r.getEndTime();
		to = to.add(Calendar.MINUTE, 5);
		int freeMinutes = (int) (to.getTimeInMillis() - from.getTimeInMillis()) / 60000;
		
		reservationInfoView.setText(Html.fromHtml(
				String.format(getResources().getString(R.string.freeIn), 
						TextUtils.htmlEncode(Helpers.humanizeTimeSpan(freeMinutes)))));
		reservationInfoView.setVisibility(VISIBLE);
	}
	
	private long lastTouched = 0;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean capture = false;
		if (getVisibility() == VISIBLE) {
			setVisibility(INVISIBLE);
			capture = true;
		}
		lastTouched = new Date().getTime();
		return capture;
	}
	
	public void disable() {
		enabled = false;
		setVisibility(INVISIBLE);
		lastTouched = new Date().getTime();
	}
	
	public void enable() {
		enabled = true;
		lastTouched = new Date().getTime();
	}
}