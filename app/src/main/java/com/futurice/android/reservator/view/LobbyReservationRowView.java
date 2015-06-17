package com.futurice.android.reservator.view;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.ReservatorApplication;
import com.futurice.android.reservator.RoomActivity;
import com.futurice.android.reservator.model.AddressBookAdapter;
import com.futurice.android.reservator.model.AddressBookEntry;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

public class LobbyReservationRowView extends FrameLayout implements
		OnClickListener, OnItemClickListener {

	View cancelButton, bookNowButton, reserveButton, calendarButton,
			bookingMode, normalMode, titleView;
	AutoCompleteTextView nameField;
	CustomTimeSpanPicker2 timePicker2;
	TextView roomNameView, roomInfoView, roomStatusView;
	ReservatorApplication application;
	ViewSwitcher modeSwitcher;
	OnReserveListener onReserveCallback = null;
	OnCancellListener onCancellListener = null;
	private Room room;
	private int animationDuration = 300;
	private ReservatorException reservatorException;
	
	public LobbyReservationRowView(Context context) {
		this(context, null);
	}

	private OnFocusChangeListener userNameFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				reserveButton.setEnabled(false);
			}

		}
	};

	public LobbyReservationRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.lobby_reservation_row, this);
		cancelButton = findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(this);
		bookNowButton = findViewById(R.id.bookNowButton);
		bookNowButton.setOnClickListener(this);
		titleView = findViewById(R.id.titleLayout);
		titleView.setOnClickListener(this);
		reserveButton = findViewById(R.id.reserveButton);
		reserveButton.setOnClickListener(this);
		calendarButton = findViewById(R.id.calendarButton);
		calendarButton.setOnClickListener(this);
		bookingMode = findViewById(R.id.bookingMode);
		normalMode = findViewById(R.id.normalMode);
		nameField = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
		timePicker2 = (CustomTimeSpanPicker2) findViewById(R.id.timeSpanPicker2);
		roomNameView = (TextView) findViewById(R.id.roomNameLabel);
		roomInfoView = (TextView) findViewById(R.id.roomInfoLabel);
		roomStatusView = (TextView) findViewById(R.id.roomStatusLabel);
		modeSwitcher = (ViewSwitcher) findViewById(R.id.modeSwitcher);
		switchToNormalModeContent();
		
		application = (ReservatorApplication) this.getContext()
				.getApplicationContext();
		nameField.setOnFocusChangeListener(userNameFocusChangeListener);
		nameField.setOnItemClickListener(this);
		nameField.setOnClickListener(this);
		if (nameField.getAdapter() == null) {
			try {
				nameField.setAdapter(new AddressBookAdapter(this.getContext(),
					application.getAddressBook()));
			} catch (ReservatorException e) {
				reservatorException = e;
			}
		}
	}
	public void setAnimationDuration(int millis){
		animationDuration = millis;
	}
	
	public ReservatorException getException() {
		return reservatorException;
	}
	
	public void setRoom(Room room) {
		this.room = room;

		// Room stuff
		roomNameView.setText(room.getName());
		if (room.getCapacity() >= 0) {
			roomInfoView.setText("for " + room.getCapacity());
		} else {
			roomInfoView.setText("");
		}

		// Reservation stuff
		TimeSpan nextFreeTime = room.getNextFreeTime();

		timePicker2.reset();
		if (nextFreeTime != null) {
			timePicker2.setMinimumTime(nextFreeTime.getStart());
			timePicker2.setMaximumTime(nextFreeTime.getEnd());
		} else {
			timePicker2.setMinimumTime(new DateTime());
		}
		timePicker2.setEndTimeRelatively(60); // let book the room for an hour

		roomStatusView.setText(room.getStatusText());
		if (room.isBookable()) {
			roomStatusView.setTextColor(getResources().getColor(
					R.color.StatusFreeColor));
			bookNowButton.setVisibility(View.VISIBLE);
		} else {
			roomStatusView.setTextColor(getResources().getColor(
					R.color.StatusReservedColor));
			bookNowButton.setVisibility(View.INVISIBLE);
		}
	}

	public Room getRoom() {
		return room;
	}

	@Override
	public void onClick(View v) {

		if (v == bookNowButton) {
			setReserveMode();
		} else if (v == cancelButton) {
			setNormalMode();
			if(this.onCancellListener != null){
				this.onCancellListener.onCancel(this);
			}
		} else if (v == reserveButton) {
			reserveButton.setEnabled(false);
			new MakeReservationTask().execute();
		} else if (v == calendarButton || v == titleView) {
			RoomActivity.startWith(getContext(), getRoom());
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		reserveButton.setEnabled(true);
		nameField.setSelected(false);
		InputMethodManager imm = (InputMethodManager) getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(nameField.getRootView().getWindowToken(), 0);
	}
	
	private class MakeReservationTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... arg0) {
			AddressBookEntry entry = application.getAddressBook().getEntryByName(
					nameField.getText().toString());
			if (entry == null) {
				reservatorError(new ReservatorException("No such user, try again"));
			}
			try {
				application.getDataProxy().reserve(room, timePicker2.getTimeSpan(),
						entry.getName(), entry.getEmail());
			} catch (ReservatorException e) {
				reservatorError(e);
			}
			
			// Void requires "return null;". Java blah.
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v){
			onReserveCallback.call(LobbyReservationRowView.this);
		}
	}
	
	private void reservatorError(ReservatorException e) {
		Builder alertBuilder = new AlertDialog.Builder(getContext());
		alertBuilder.setTitle("Failed to put reservation").setMessage(
				e.getMessage());
		alertBuilder.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				if (onReserveCallback != null) {
					onReserveCallback.call(LobbyReservationRowView.this);
				}
			}
		});

		alertBuilder.show();
	}

	public void setNormalMode() {
		if(animationDuration > 0){
		Animation scale = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f,  ScaleAnimation.RELATIVE_TO_SELF, 0.5f,  ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		scale.setDuration(animationDuration);
		scale.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationEnd(Animation animation) {
				switchToNormalModeContent();
				Animation scale = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f,  ScaleAnimation.RELATIVE_TO_SELF, 0.5f,  ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
				scale.setDuration(animationDuration);
				startAnimation(scale);
			}
		});
		startAnimation(scale);
		}else{
			switchToNormalModeContent();
		}
	}

	private void switchToNormalModeContent(){
		modeSwitcher.setDisplayedChild(modeSwitcher.indexOfChild(normalMode));
		if(modeSwitcher.indexOfChild(bookingMode) >= 0){
			modeSwitcher.removeView(bookingMode);
		}
		reserveButton.setEnabled(false);
		setBackgroundColor(getResources().getColor(R.color.Transparent));
	}
	public void setReserveMode() {
		if(animationDuration > 0){
		Animation scale = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f,  ScaleAnimation.RELATIVE_TO_SELF, 0.5f,  ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		scale.setDuration(animationDuration);
		scale.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationEnd(Animation animation) {
				switchToReserveModeContent();
				Animation scale = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f,  ScaleAnimation.RELATIVE_TO_SELF, 0.5f,  ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
				scale.setDuration(animationDuration);
				startAnimation(scale);
			}
		});
		startAnimation(scale);
		}else{
			switchToReserveModeContent();
		}
	}
	private void switchToReserveModeContent(){
		if(modeSwitcher.indexOfChild(bookingMode) < 0){
			modeSwitcher.addView(bookingMode);
		}
		modeSwitcher.setDisplayedChild(modeSwitcher.indexOfChild(bookingMode));
		setBackgroundColor(getResources().getColor(R.color.ReserveBackground));
		reserveButton.setEnabled(false);
		
	}
	public void resetTimeSpan() {
		timePicker2.reset();
	}

	public void setMinTime(DateTime time) {
		timePicker2.setMinimumTime(time);
	}

	public void setMaxTime(DateTime time) {
		timePicker2.setMaximumTime(time);
	}

	public void setEndTimeRelatively(int minutes) {
		timePicker2.setEndTimeRelatively(minutes);
	}

	public void setOnReserveCallback(OnReserveListener onReserveCallback) {
		this.onReserveCallback = onReserveCallback;
	}

	public void setOnCancellListener(OnCancellListener l){
		this.onCancellListener = l;
	}

	public interface OnCancellListener{
		public void onCancel(LobbyReservationRowView view);
	}

	public interface OnReserveListener {
		public void call(LobbyReservationRowView v);
	}
}
