package com.futurice.android.reservator.model.dummy;

import java.util.Vector;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

public class FailingDataProxy extends DataProxy {
	DataProxy dataProxy;

	private boolean failGetRooms = false;
	private boolean failGetRoomReservations = false;
	private boolean failReserve = false;

	public FailingDataProxy(DataProxy dataProxy) {
		this.dataProxy = dataProxy;
	}

	@Override
	public void deinit() {
		dataProxy.deinit();
	}

	@Override
	public Vector<Reservation> getRoomReservations(Room r)
			throws ReservatorException {

		if (failGetRoomReservations) {
			throw new ReservatorException("FailingDataProxy -- getRoomReservations");
		}

		return dataProxy.getRoomReservations(r);
	}

	@Override
	public Vector<Room> getRooms() throws ReservatorException {
		if (failGetRooms) {
			throw new ReservatorException("FailingDataProxy -- getRooms");
		}

		return dataProxy.getRooms();
	}

	@Override
	public void reserve(Room room, TimeSpan timeSpan, String ownerEmail)
			throws ReservatorException {
		if (failReserve) {
			throw new ReservatorException("FailingDataProxy -- reserve");
		}

		dataProxy.reserve(room, timeSpan, ownerEmail);
	}

	@Override
	public void setCredentials(String user, String password) {
		dataProxy.setCredentials(user, password);
	}

	@Override
	public void setServer(String server) {
		dataProxy.setServer(server);
	}

	public void setFailGetRoomReservations(boolean failGetRoomReservations) {
		this.failGetRoomReservations = failGetRoomReservations;
	}

	public void setFailGetRooms(boolean failGetRooms) {
		this.failGetRooms = failGetRooms;
	}

	public void setFailReserve(boolean failReserve) {
		this.failReserve = failReserve;
	}
}
