package com.futurice.android.reservator;

import com.futurice.android.reservator.ReservatorApplication;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class ReservatorActivity extends Activity {
	
	protected Boolean prehensible = true;
	
	private GoToFavouriteRoom goToFavouriteRoomRunable;
	class GoToFavouriteRoom implements Runnable {
		
		ReservatorActivity activity;
		
		public GoToFavouriteRoom(ReservatorActivity anAct){
			activity = anAct;
		}
		
		@Override
		public void run() {
			ReservatorApplication app = ((ReservatorApplication) activity.getApplication());
			String roomName = app.getSettingValue(R.string.PREFERENCES_ROOM_NAME, "");
			Room room;
			try {
				room = app.getDataProxy().getRoomWithName(roomName);
			} catch (ReservatorException ex) {
				Toast err = Toast.makeText(app, ex.getMessage(),
						Toast.LENGTH_LONG);
				err.show();
				return;
			}
			RoomActivity.startWith(activity, room);
			activity.onPrehended();
		}
		
	}
	
	private final ReservatorAppHandler handler = new ReservatorAppHandler();
	class ReservatorAppHandler extends Handler{
		@Override
		public void handleMessage(Message msg){
			return;
		}
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		goToFavouriteRoomRunable = new GoToFavouriteRoom(this);
	}
		
	public void onResume() {
		super.onResume();		
		startTimer();
	}
	
	public void onPause() {
		super.onPause();
		stopTimer();
	}
	
	public void onPrehended() {}
	
	public void onUserInteraction() {
		super.onUserInteraction();
		stopTimer();
		startTimer();
	}
	
	private void startTimer() {
		if (prehensible){
			handler.postDelayed(goToFavouriteRoomRunable, 60000);
		}
	}
	
	private void stopTimer() {
		if (prehensible){
			handler.removeCallbacks(goToFavouriteRoomRunable);
		}
	}
	
}
