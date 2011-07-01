package com.futurice.android.reservator.view;

import java.util.List;

import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.TimeSpan;

public interface ReservatorVisualizer {
	public void setReservations(List<Reservation> reservations);
	public DateTime getSelectedTime();
	public TimeSpan getSelectedTimeSpan();
}
