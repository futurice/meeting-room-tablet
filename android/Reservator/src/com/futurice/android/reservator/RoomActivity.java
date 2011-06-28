package com.futurice.android.reservator;

import java.util.Calendar;
import java.util.Vector;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DataUpdatedListener;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;
import com.futurice.android.reservator.model.rooms.RoomsInfo;
import com.futurice.android.reservator.view.RoomReservationPopup;
import com.futurice.android.reservator.view.WeekView;
import com.futurice.android.reservator.view.WeekView.OnFreeTimeClickListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

public class RoomActivity extends Activity implements OnMenuItemClickListener,
		DataUpdatedListener {
	public static final String ROOM_EMAIL_EXTRA = "roomEmail";

	DataProxy proxy;
	Room currentRoom;
	String roomEmail = null;

	WeekView weekView;
	TextView roomNameLabel;

	MenuItem settingsMenu, refreshMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.room_activity);
		this.weekView = (WeekView) findViewById(R.id.weekView1);
		this.roomNameLabel = (TextView) findViewById(R.id.roomNameLabel);
		roomEmail = getIntent().getStringExtra(ROOM_EMAIL_EXTRA);
		if (roomEmail == null || roomEmail.length() == 0) {
			throw new IllegalArgumentException(
					"No room identifying e-mail address found as string extra 'roomEmail'");
		}
		findViewById(R.id.seeAllRoomsButton).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						RoomActivity.this.finish();
					}
				});

		weekView.setOnFreeTimeClickListener(new OnFreeTimeClickListener() {
			@Override
			public void onFreeTimeClick(View v, TimeSpan timeSpan, Calendar touchTime) {

				RoomReservationPopup d;

				Calendar start = timeSpan.getStart();
				Calendar end = timeSpan.getEnd();

				// if time span is less than hour, select it all
				if (timeSpan.getLength() <= 60*60000) {
					d = new RoomReservationPopup(RoomActivity.this, timeSpan, timeSpan, currentRoom);
				} else {
					Calendar now = Calendar.getInstance();
					Calendar touch = (Calendar) touchTime.clone();

					touch.set(Calendar.MINUTE, 0);
					touch.set(Calendar.SECOND, 0);
					touch.set(Calendar.MILLISECOND, 0);

					if (touch.before(start)) {
						touch = start;
					}
					if (touch.before(now) && now.before(end)) {
						touch = now;
					}

					TimeSpan presetTimeSpan = new TimeSpan(touch, Calendar.HOUR, 1);
					Calendar touchend = presetTimeSpan.getEnd();

					// quantize end to 15min steps
					touchend.set(Calendar.MINUTE, (touchend.get(Calendar.MINUTE) / 15) * 15);

					if (touchend.after(end)) {
						presetTimeSpan.setEnd((Calendar)end.clone()); // TODO: i really dislike this cloning
					}

					d = new RoomReservationPopup(RoomActivity.this, timeSpan, presetTimeSpan, currentRoom);
				}

				d.show();
				d.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						((ReservatorApplication)getApplicationContext()).getDataProxy().refreshRoomReservations(currentRoom);
					}
				});
			}
		});
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
		roomNameLabel
				.setText(RoomsInfo.getRoomsInfo(currentRoom).getRoomName());
		weekView.refreshData(currentRoom);
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
	public void roomListUpdated(Vector<Room> rooms) {
		for (Room r : rooms) {
			if (r.getEmail().equals(roomEmail)) {
				final Room theRoom = r;
				setRoom(theRoom);
				return;
			}
		}
		// TODO what if the room is not in the list?
		throw new RuntimeException("Requested room not in the list.");
	}

	@Override
	public void roomReservationsUpdated(Room room) {
		if (currentRoom != null && room.getEmail().equals(roomEmail)) {
			weekView.refreshData(room);
		}
	}

	@Override
	public void refreshFailed(ReservatorException e) {
		Builder alertBuilder = new AlertDialog.Builder(getApplicationContext());
		alertBuilder.setTitle("Error")
			.setMessage(e.getMessage())
			.show();
	}
}