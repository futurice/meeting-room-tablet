package com.futurice.android.reservator.model.rooms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.futurice.android.reservator.common.Helpers;
import com.futurice.android.reservator.model.Room;

public class RoomsInfo {
	private int roomSize;
	private int roomNumber;
	private String roomName;
	private String roomType;

	private static Map<String, RoomsInfo> rooms = null;

	private RoomsInfo(String roomName, int roomNumber, int roomSize, String roomType) {
		this.roomSize = roomSize;
		this.roomNumber = roomNumber;
		this.roomName = roomName;
		this.roomType = roomType;
	}

	public String getRoomName() {
		return roomName;
	}

	public int getRoomNumber() {
		return roomNumber;
	}

	public int getRoomSize() {
		return roomSize;
	}

	public String getRoomType() {
		return roomType;
	}

	public boolean isProjectRoom() {
		return getRoomType().equals("project");
	}

	@SuppressWarnings("unchecked")
	private static void loadRooms() {
		rooms = new HashMap<String, RoomsInfo>();

		try {
			JSONObject object = (JSONObject) new JSONTokener(Helpers.readFromInputStream(RoomsInfo.class.getResourceAsStream("rooms.json"), -1)).nextValue();
			Iterator<String> iter = object.keys();
			while (iter.hasNext()) {
				String key = iter.next();
				try {
					JSONObject room = object.getJSONObject(key);

					// default size of six is better than nothing
					rooms.put(key.toLowerCase(), new RoomsInfo(room.getString("name"), room.optInt("number"), room.optInt("size", 6), room.optString("type", "room")));
				} catch (JSONException e) {
					// do nothing
				}
			}
		} catch (JSONException e) {
			// do nothing
		}
	}

	// TODO: just a stub
	public static RoomsInfo getRoomsInfo(Room room) {
		if (rooms == null) loadRooms();

		String email = room.getEmail();
		int atindex = email.indexOf('@');

		RoomsInfo info;

		// Log.v("roomsinfo", email.substring(0, atindex));

		if (atindex == -1 || (info = rooms.get(email.substring(0, atindex).toLowerCase())) == null) {
			return new RoomsInfo(room.getName(), 0, 6, "unknown");
		} else {
			return info;
		}
	}

}
