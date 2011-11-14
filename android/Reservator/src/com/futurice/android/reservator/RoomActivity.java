package com.futurice.android.reservator;

import java.util.Calendar;
import java.util.Vector;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DataUpdatedListener;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;
import com.futurice.android.reservator.model.rooms.RoomsInfo;
import com.futurice.android.reservator.view.LobbyReservationRowView;
import com.futurice.android.reservator.view.RoomReservationPopup;
import com.futurice.android.reservator.view.WeekView;
import com.futurice.android.reservator.view.LobbyReservationRowView.OnReserveListener;
import com.futurice.android.reservator.view.WeekView.OnFreeTimeClickListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class RoomActivity extends ReservatorActivity implements OnMenuItemClickListener,
		DataUpdatedListener {
	public static final String ROOM_EXTRA = "room";

	DataProxy proxy;
	Room currentRoom;

	WeekView weekView;
	TextView roomNameLabel;

	MenuItem settingsMenu, refreshMenu;

	private ProgressDialog progressDialog = null;
	int showLoadingCount = 0;

	final Handler handler = new Handler();

	final Runnable updateRoomsRunnable = new Runnable() {
		@Override
		public void run() {
			Log.v("Refresh", "refreshing room info");
			refreshData();
			handler.postDelayed(updateRoomsRunnable, 10*60000); // update after 10minutes
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.room_activity);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.weekView = (WeekView) findViewById(R.id.weekView1);
		this.roomNameLabel = (TextView) findViewById(R.id.roomNameLabel);
		try {
			currentRoom = (Room) getIntent().getSerializableExtra(ROOM_EXTRA);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("No room found as Serializable extra " + ROOM_EXTRA);
		}
		if (currentRoom == null) {
			throw new IllegalArgumentException(
					"No room found as Serializable extra " + ROOM_EXTRA);
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
			public void onFreeTimeClick(View v, TimeSpan timeSpan, DateTime touch) {


				TimeSpan reservationTimeSpan = timeSpan;

				// if time span is greater than hour do stuff
				if (timeSpan.getLength() > 60*60000) {
					DateTime start = timeSpan.getStart();
					DateTime end = timeSpan.getEnd();

					DateTime now = new DateTime();

					touch = touch.stripMinutes();

					if (touch.before(start)) {
						touch = start;
					}
					if (touch.before(now) && now.before(end)) {
						touch = now;
					}

					reservationTimeSpan = new TimeSpan(touch, Calendar.HOUR, 1);
					DateTime touchend = reservationTimeSpan.getEnd();

					// quantize end to 15min steps
					touchend = touchend.set(Calendar.MINUTE, (touchend.get(Calendar.MINUTE) / 15) * 15);

					if (touchend.after(end)) {
						reservationTimeSpan.setEnd(end);
					}
				}

				final RoomReservationPopup d = new RoomReservationPopup(RoomActivity.this, timeSpan, reservationTimeSpan, currentRoom);
				d.setOnReserveCallback(new OnReserveListener() {
					@Override
					public void call(LobbyReservationRowView v) {
						d.dismiss();
						refreshData();
					}
				});

				d.show();
			}
		});
		// We don't want to automatically change room if this is already the right activity
		ReservatorApplication app = ((ReservatorApplication) getApplication());
		String favouriteRoomName = app.getSettingValue(R.string.PREFERENCES_ROOM_NAME, "");
		if (currentRoom.getName().equals(favouriteRoomName)){
			prehensible = false;
		}
	}

	public static void startWith(Context context, Room room) {
		Intent i = new Intent(context, RoomActivity.class);
		i.putExtra(ROOM_EXTRA, room);
		context.startActivity(i);
	}
	
	@Override
	public void onResume() {
		proxy = ((ReservatorApplication) getApplication()).getDataProxy();
		proxy.addDataUpdatedListener(this);
		refreshData();
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		((ReservatorApplication) getApplication()).getDataProxy()
				.removeDataUpdatedListener(this);
	}

	@Override
	public void onPrehended() {
		this.finish();
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
			refreshData();
		}
		return true;
	}

	private void refreshData() {
		showLoading();
		proxy.refreshRoomReservations(currentRoom);
	}

	private void showLoading() {
		showLoadingCount++;
		if (this.progressDialog == null) {
			this.progressDialog = ProgressDialog.show(this, "Loading",
					"Refreshing reservations", true, false);
		}

	}

	private void hideLoading() {
		showLoadingCount--;
		if (showLoadingCount <= 0) {
			if (this.progressDialog != null) {
				this.progressDialog.dismiss();
				this.progressDialog = null;
			}
		}
	}

	@Override
	public void roomListUpdated(Vector<Room> rooms) {
		// XXX: todo
		/*
		for (Room r : rooms) {
			if (r.getEmail().equals(roomEmail)) {
				final Room theRoom = r;
				setRoom(theRoom);
				return;
			}
		}
		// TODO what if the room is not in the list?
		throw new RuntimeException("Requested room not in the list.");
		*/
	}

	@Override
	public void roomReservationsUpdated(Room room) {
		if (currentRoom != null && room.equals(currentRoom)) {
			setRoom(room);
		}
		hideLoading();
	}

	@Override
	public void refreshFailed(ReservatorException e) {
		Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle("Error")
			.setMessage(e.getMessage())
			.show();
		hideLoading();
	}
}