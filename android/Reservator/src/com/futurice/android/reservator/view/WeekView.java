package com.futurice.android.reservator.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TableRow;

import com.futurice.android.reservator.ReserveActivity;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

public class WeekView extends TableRow implements OnClickListener{
	public static final int NUMBER_OF_DAYS_TO_SHOW = 5;
	private Room currentRoom = null;
	
	public WeekView(Context context){
		super(context);
	}
	
	public WeekView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setRoom(Room room) {
		currentRoom = room;
		this.removeAllViews();
		
		//TableRow row = (TableRow)findViewById(R.id.tableRow1);
		Calendar day = Calendar.getInstance();
		day.set(Calendar.HOUR_OF_DAY, 8);
		day.set(Calendar.MINUTE, 0);
		for (int i = 0; i < NUMBER_OF_DAYS_TO_SHOW; i++){
			LinearLayout column = new LinearLayout(getContext());
			column.setOrientation(LinearLayout.VERTICAL);
			column.setPadding(3,3,3,3);
			LinearLayout.LayoutParams lp = new LayoutParams();
			lp.weight = 0.2f;
			lp.height = LayoutParams.FILL_PARENT;
			column.setLayoutParams(lp);
			this.addView(column);
			
			List<Reservation> daysReservations;
			try {
				daysReservations = getReservationsForDay(currentRoom.getReservations(true), day);
			} catch (ReservatorException e) {
				// TODO: XXX
				Log.e("DataProxy", "getReservations", e);
				return;
			}
			if(daysReservations.isEmpty()){
				continue;
			}
 			Reservation first = daysReservations.get(0);
 			if(first.getBeginTime().after(day)){
 				CalendarMarkerView leadingView = getViewForTimeSpan(day, first.getBeginTime());
				column.addView(leadingView);
 			}
			for(int j = 0; j < daysReservations.size(); j++){
				Reservation current = daysReservations.get(j);
				CalendarMarkerView reservedView = getViewForTimeSpan(current.getBeginTime(), current.getEndTime());
				reservedView.setReservation(current);
				column.addView(reservedView);
				
				if(j < daysReservations.size() - 1){
					Reservation next = daysReservations.get(j+1);
					CalendarMarkerView freeView = getViewForTimeSpan(current.getEndTime(), next.getBeginTime());
					column.addView(freeView);
				}
			}
			day.add(Calendar.DAY_OF_YEAR, 1);
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
	private CalendarMarkerView getViewForTimeSpan(Calendar begin, Calendar end){
		CalendarMarkerView v = new CalendarMarkerView(this.getContext());
		v.setText(dateToTime(begin.getTime()) + "-"+ dateToTime(end.getTime()));
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,1,(float)(end.getTimeInMillis() - begin.getTimeInMillis()));
		lp.setMargins(0,0,1,1);
		v.setReservation(new Reservation(currentRoom, begin, end));
		v.setLayoutParams(lp);
		v.setOnClickListener(this);
		return v;
	}
	private String dateToTime(Date d){
		return d.getHours() + ":" + d.getMinutes();
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent(getContext(), ReserveActivity.class);
		Reservation r = ((CalendarMarkerView)v).getReservation();
		i.putExtra("roomMail", currentRoom.getEmail());
		i.putExtra("begin", r.getBeginTime().getTimeInMillis());
		i.putExtra("end", r.getEndTime().getTimeInMillis());
		getContext().startActivity(i);
	}

}
