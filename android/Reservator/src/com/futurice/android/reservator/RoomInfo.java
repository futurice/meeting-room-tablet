package com.futurice.android.reservator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;

public class RoomInfo extends Activity {
	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar_view);
		findViewById(R.id.buttonPrev).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setData();
				
			}
		});
	}

	private void setData() {
		DataProxy proxy = ((ReservatorApplication) getApplication())
				.getDataProxy();
		Room room = proxy.getRooms().get(0);
		setTitle(room.getName());
		
		TableRow row = (TableRow)findViewById(R.id.tableRow1);
		Calendar day = Calendar.getInstance();
		day.set(Calendar.HOUR_OF_DAY, 8);
		day.set(Calendar.MINUTE, 0);
		for (int i = 0; i < 5; i++){
			List<Reservation> daysReservations = getReservationsForDay(room.getReservations(), day);
			if(daysReservations.isEmpty()){
				continue;
			}
			LinearLayout column = (LinearLayout)row.getChildAt(i);
 			Reservation first = daysReservations.get(0);
 			if(first.getBeginTime().after(day)){
 				TextView leadingView = getViewForTimeSpan(day, first.getBeginTime());
				leadingView.setBackgroundColor(Color.CYAN);
				column.addView(leadingView);
 			}
			for(int j = 0; j < daysReservations.size(); j++){
				Reservation current = daysReservations.get(j);
				if(j == 0 && day.before(current.getBeginTime())){
					
				}
				TextView reservedView = getViewForTimeSpan(current.getBeginTime(), current.getEndTime());
				reservedView.setBackgroundColor(Color.MAGENTA);
				column.addView(reservedView);
				
				if(j < daysReservations.size() - 1){
					Reservation next = daysReservations.get(j+1);
					TextView freeView = getViewForTimeSpan(current.getEndTime(), next.getBeginTime());
					freeView.setBackgroundColor(Color.CYAN);
					column.addView(freeView);
				}
			}
		}
	}
	private List<Reservation> getReservationsForDay(List<Reservation> reservations, Calendar day){
		List<Reservation> daysReservations = new ArrayList<Reservation>();
		for(Reservation r : reservations){
			if(r.getBeginTime().get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)
					&& r.getBeginTime().get(Calendar.YEAR) == day.get(Calendar.YEAR)){
				daysReservations.add(r);
			}
		}
		return daysReservations;
	}
	private TextView getViewForTimeSpan(Calendar begin, Calendar end){
		TextView v = new TextView(this);
		v.setText(dateToTime(begin.getTime()) + "-"+ dateToTime(end.getTime()));
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,1,(float)(end.getTimeInMillis() - begin.getTimeInMillis()));
		v.setLayoutParams(lp);
		return v;
	}
	private String dateToTime(Date d){
		return d.getHours() + ":" + d.getMinutes();
	}
}
