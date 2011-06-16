package com.futurice.android.reservator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.futurice.android.reservator.model.AddressBookEntry;
import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.rooms.RoomsInfo;
import com.futurice.android.reservator.view.TimeSpanPicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity implements OnMenuItemClickListener {
	LinearLayout container = null;
	private Map<View, Room> roomMap;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.lobby_view);
	}

	@Override
	public void onResume(){
		super.onResume();
		container = (LinearLayout)findViewById(R.id.linearLayout1);
		container.removeAllViews();

		try {
			List<Room> rooms = ((ReservatorApplication)getApplication()).getDataProxy().getRooms();
			roomMap = new HashMap<View, Room>(rooms.size());
			for(final Room r : rooms){
				r.getReservations(true);
				View v = getLayoutInflater().inflate(R.layout.lobby_reservation_row, null);
				final View bookingMode = v.findViewById(R.id.bookingMode);
				final View normalMode = v.findViewById(R.id.normalMode);

				TextView roomNameLabel = (TextView)v.findViewById(R.id.roomNameLabel);
				roomNameLabel.setText(r.getName());

				TextView roomInfoLabel = (TextView)v.findViewById(R.id.roomInfoLabel);
				RoomsInfo info = RoomsInfo.getRoomsInfo(r);
				roomInfoLabel.setText("(" + Integer.toString(info.getRoomSize()) + " persons)" ); // r.getEmail()

				roomMap.put(v.findViewById(R.id.calendarButton), r);
				v.findViewById(R.id.bookNowButton).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						bookingMode.setVisibility(View.VISIBLE);
						normalMode.setVisibility(View.GONE);
						Reservation nextFreeTime = r.getNextFreeTime();
						TimeSpanPicker timePicker = (TimeSpanPicker)((ViewGroup)((ViewGroup)v.getParent()).getParent()).findViewById(R.id.timeSpanPicker1);
						timePicker.setStartTime(nextFreeTime.getBeginTime());
						timePicker.setMaxTime(nextFreeTime.getEndTime());
					}
				});
				v.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						bookingMode.setVisibility(View.GONE);
						normalMode.setVisibility(View.VISIBLE);
					}
				});
				v.findViewById(R.id.reserveButton).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						TimeSpanPicker tp = (TimeSpanPicker)((ViewGroup)v.getParent()).findViewById(R.id.timeSpanPicker1);
						Toast t = Toast.makeText(HomeActivity.this, tp.getStartTime().toLocaleString() + "-" + tp.getEndTime().toLocaleString(), Toast.LENGTH_LONG);
						t.show();
					}
				});

				// TODO: move so only one adapter is created
				AddressBook addressBook = ((ReservatorApplication)getApplication()).getAddressBookProxy();
				List<String> names = new ArrayList<String>();
				for (AddressBookEntry entry : addressBook.getEntries()) {
					names.add(entry.getName());
				}

				AutoCompleteTextView reservator = (AutoCompleteTextView) v.findViewById(R.id.nameAutoEditText);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, names);
			    reservator.setAdapter(adapter);

				v.findViewById(R.id.calendarButton).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View vi) {
						Intent i = new Intent(HomeActivity.this, RoomInfo.class);
						i.putExtra(RoomInfo.ROOM_EMAIL_EXTRA, r.getEmail());
						startActivity(i);
					}
				});
				container.addView(v);
			}
		} catch (ReservatorException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add("Settings").setOnMenuItemClickListener(this);
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
		return true;
	}
}
