package com.futurice.android.reservator.model.rooms;

import com.futurice.android.reservator.model.Room;

public class RoomsInfo {
	private int roomSize;

	private RoomsInfo(int roomSize) {
		this.roomSize = roomSize;
	}

	// TODO: just a stub
	public static RoomsInfo getRoomsInfo(Room room) {
		if (room.getName().contains("404")) {
			return new RoomsInfo(42);
		} else {
			return new RoomsInfo(6);
		}
	}

	public int getRoomSize() {
		return roomSize;
	}
}
