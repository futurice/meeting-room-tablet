package com.futurice.android.reservator.view;

import java.util.List;

import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.TimeSpan;
/**
 * Interface for visualizing reservation data. getSelectedTime and getSelectedTimeSpan can be called after onClick to get
 * DateTime and TimeSpan associated with the click event
 * @author vman
 */
public interface ReservatorVisualizer {

	/**
	 * Set reservations. The reservation references should be copied to an internal array.
	 * @param reservations
	 */
	public void setReservations(List<Reservation> reservations);
	/**
	 * Get selected DateTime after onClick
	 * @return
	 */
	public DateTime getSelectedTime();
	/**
	 * Get selected TimeSpan after onClick
	 * @return
	 */
	public TimeSpan getSelectedTimeSpan();
	/**
	 * Get selected Reservation after onClick. Returns null if the click was on empty space.
	 * @return
	 */
	public Reservation getSelectedReservation();
}
