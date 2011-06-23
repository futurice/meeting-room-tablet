package com.futurice.android.reservator;

import java.util.List;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DataUpdatedListener;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.rooms.RoomsInfo;
import com.futurice.android.reservator.view.WeekView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RoomInfo extends Activity implements OnMenuItemClickListener, DataUpdatedListener {
	public static final String ROOM_EMAIL_EXTRA = "roomEmail";
	Room currentRoom;
	WeekView weekView;
	DataProxy proxy;
	ArrayAdapter<Room> roomAdapter;
	TextView roomNameLabel;
	String roomEmail = null;

	MenuItem settingsMenu, refreshMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.room_browse_view);
		this.weekView = (WeekView) findViewById(R.id.weekView1);
		this.roomNameLabel = (TextView) findViewById(R.id.roomNameLabel);
		roomEmail = getIntent().getStringExtra(ROOM_EMAIL_EXTRA);
		if (roomEmail == null || roomEmail.length() == 0) {
			throw new IllegalArgumentException(
					"No room identifying e-mail address found as string extra 'roomEmail'");
		}
	}

	@Override
	public void onResume() {
		proxy = ((ReservatorApplication) getApplication()).getDataProxy();
		proxy.addDataUpdatedListener(this);
		proxy.refreshRooms();
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		((ReservatorApplication) getApplication()).getDataProxy()
				.removeDataUpdatedListener(this);
	}

	private void setRoom(Room r) {
			currentRoom = r;
			roomNameLabel.setText(RoomsInfo.getRoomsInfo(currentRoom).getRoomName());
			weekView.setRoom(currentRoom);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		refreshMenu = menu.add("Refresh").setOnMenuItemClickListener(this);
		refreshMenu.setIcon(android.R.drawable.ic_popup_sync);
		settingsMenu = menu.add("Settings").setOnMenuItemClickListener(this);
		settingsMenu.setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (item == settingsMenu) {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
		} else if (item == refreshMenu) {
			proxy.refreshRoomReservations(currentRoom);
		}
		return true;
	}

	@Override
	public void roomListUpdated(List<Room> rooms) {
		for (Room r : rooms) {
			if (r.getEmail().equals(roomEmail)) {
			final Room theRoom = r;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setRoom(theRoom );						
					}
				});
				return;
			}
		}
		//TODO what if the room is not in the list?
		throw new RuntimeException("Requested room not in the list.");
	}

	@Override
	public void roomReservationsUpdated(Room room,
			List<Reservation> reservations) {
		if (currentRoom != null && room.getEmail().equals(roomEmail)) {
			if (room.getEmail().equals(roomEmail)) {
				weekView.refreshData();
			}
		}
	}

	@Override
	public void refreshFailed(ReservatorException ex) {
		// TODO Auto-generated method stub
		throw new RuntimeException(ex.getMessage());
	}
}