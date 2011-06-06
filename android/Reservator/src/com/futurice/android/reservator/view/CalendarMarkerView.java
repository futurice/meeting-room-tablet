package com.futurice.android.reservator.view;

import com.futurice.android.reservator.model.Reservation;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

public class CalendarMarkerView extends TextView {
	private Reservation reservation = null;
	public CalendarMarkerView(Context context) {
		super(context);
	}
	public CalendarMarkerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public CalendarMarkerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public void setReservation(Reservation reservation){
		if(reservation.getRoom().getReservations().contains(reservation)){
			this.setBackgroundColor(Color.RED);
		}else{
			this.setBackgroundColor(Color.GREEN);
		}
		this.reservation = reservation;
	}
	public Reservation getReservation(){
		return this.reservation;
	}
	@Override
	public String toString(){
		return reservation.getRoom().getName() +": " + reservation.getBeginTime().getTime().toGMTString() + "-" +reservation.getEndTime().getTime().toGMTString(); 
	}
}
