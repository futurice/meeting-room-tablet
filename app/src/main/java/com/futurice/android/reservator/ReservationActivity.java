package com.futurice.android.reservator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.futurice.android.reservator.model.AddressBookAdapter;
import com.futurice.android.reservator.model.AddressBookEntry;
import com.futurice.android.reservator.model.CachedDataProxy;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class ReservationActivity extends ReservatorActivity implements View.OnClickListener, AdapterView.OnItemClickListener{
    private long minToMS = 60000;
    private final int MIN_MEETING_TIME = 15;

    private Room room;
    private ReservatorApplication application;
    private DateTime startTime, endTime, changeTime;
    private DateTime endeMeeting;
    private TextView startTimeView;
    private TextView endTimeView;
    private TextView infoLabel;
    private AutoCompleteTextView meetingField, nameField;
    private View reserveButton, plusButton, minusButton;
    private List<Reservation> reservationList;

    private SharedPreferences settings;
    private ReservatorException reservatorException;
    private View.OnFocusChangeListener userNameFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Boolean addressBookOption = settings.getBoolean("addressBookOption", false);
            if (hasFocus && addressBookOption) {
                reserveButton.setEnabled(false);
            }
        }
    };
    private DataProxy proxy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby_reservation_row_alternativ);
        proxy = this.getResApplication().getDataProxy();
        settings = this.getSharedPreferences(this.getString(R.string.PREFERENCES_NAME), this.MODE_PRIVATE);
        application = (ReservatorApplication) this.getApplicationContext();

        setRoom(settings.getString("roomName",""));
        setBothTimes(settings.getLong("resTimestart",0),settings.getLong("resTimeend",0));

        TextView roomLabel = (TextView) findViewById(R.id.roomNameLabelAlternat);
        roomLabel.setText(room.getShownRoomName());

        infoLabel = (TextView) findViewById(R.id.infoText_reservationPopup);
        plusButton = findViewById(R.id.plusButtonsGroup);
        minusButton = findViewById(R.id.minusButtonGroup);

        final View cancelButton = findViewById(R.id.cancelButtonAlternativ);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        reserveButton = findViewById(R.id.reserveButtonAlternativ);
        reserveButton.setOnClickListener(this);
        meetingField = (AutoCompleteTextView) findViewById(R.id.meetingTitle);
        nameField = (AutoCompleteTextView) findViewById(R.id.yourNameAutoComplete);

        setButtons();

        nameField.setOnFocusChangeListener(userNameFocusChangeListener);
        nameField.setOnItemClickListener(this);
        nameField.setOnClickListener(this);
        if (nameField.getAdapter() == null) {
            try {
                nameField.setAdapter(new AddressBookAdapter(this, application.getAddressBook()));
            } catch (ReservatorException e) {
                reservatorException = e;
            }
        }

        refreshTimeLabels();
        startTimeView.setOnClickListener(this);
        endTimeView.setOnClickListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cancelButton.performClick();
            }
        }, minToMS);
    }

    public void setRoom(String roomName) {
        Room iteratorRoom = null;
        try {
            Vector <Room> rooms = application.getDataProxy().getRooms();

            for (Room room1 : rooms) {
                iteratorRoom = room1;
                if (iteratorRoom.getName().equals(roomName)) {
                    break;
                }
            }

        } catch (ReservatorException e) {
            e.printStackTrace();
        }
        this.getResApplication().getDataProxy().refreshRoomReservations(iteratorRoom);

        this.room = iteratorRoom;
    }

    private void setBothTimes(long startTimeInput, long endTimeInput){
        setTimetoCurrentTime();

        // Reservation stuff
        TimeSpan nextFreeTime = room.getNextFreeTime();
        Reservation reservation = room.getCurrentReservation();
        if (reservation != null) {
            this.endeMeeting = reservation.getEndTime();
        }

        if (startTimeInput != 0 && endTimeInput != 0) {
            this.startTime = new DateTime(startTimeInput);
            this.endTime = new DateTime(endTimeInput);
        } else {
            if (nextFreeTime != null) {
                long time = nextFreeTime.getStart().getTimeInMillis();
                Date date = new Date(time);
                this.startTime = new DateTime(date);
                this.endTime = new DateTime(startTime.getDate(1,true));
            } else {
                setTimetoCurrentTime();
            }
        }
        reservationList = room.getReservationsForTimeSpan(new TimeSpan(new DateTime(startTime.getTime()).setTime(0,0,0),new DateTime(endTime.getTime()).setTime(23,0,0)));
    }

    private void setButtons(){
        findViewById(R.id.plus15button).setOnClickListener(this);
        findViewById(R.id.plus30button).setOnClickListener(this);;
        findViewById(R.id.plus60button).setOnClickListener(this);

        findViewById(R.id.minus15button).setOnClickListener(this);
        findViewById(R.id.minus30button).setOnClickListener(this);
        findViewById(R.id.minus60button).setOnClickListener(this);

        plusButton.setVisibility(View.INVISIBLE);
        minusButton.setVisibility(View.INVISIBLE);
        infoLabel.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reserveButtonAlternativ:
                if (ableToReserve()){
                    reserveButton.setEnabled(false);
                    new MakeReservationTask().execute();
                    this.finish();
                }
                break;

            case R.id.startTimeAlternative:
                changeTime = startTime;
                enableButton(true,true);
                enableButton(true,false);
                setButtonGroupVisible();
                break;

            case R.id.endTimeAlternative:
                changeTime = endTime;
                enableButton(false,true);
                enableButton(false,false);
                setButtonGroupVisible();
                break;
            case R.id.plus15button:
                setChangedTime(addTime(15));
                changeDependentTime(true,15);
                break;
            case R.id.plus30button:
                setChangedTime(addTime(30));
                changeDependentTime(true,30);
                break;
            case R.id.plus60button:
                setChangedTime(addTime(60));
                changeDependentTime(true,60);
                break;
            case R.id.minus15button:
                setChangedTime(reduzeTime(15));
                changeDependentTime(false,15);
                break;
            case R.id.minus30button:
                setChangedTime(reduzeTime(30));
                changeDependentTime(false,30);
                break;
            case R.id.minus60button:
                setChangedTime(reduzeTime(60));
                changeDependentTime(false,60);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        reserveButton.setEnabled(true);
        nameField.setSelected(false);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nameField.getRootView().getWindowToken(), 0);
    }

    private boolean ableToReserve(){
        return !meetingField.getText().toString().equals("");
    }

    private void setTimetoCurrentTime(){
        Date currentTime = new Date();
        startTime = new DateTime(currentTime);
        endTime = new DateTime(startTime.getDate(1,true));
    }

    private void setChangedTime(long time){
        if (time != -1){
            if (startTime == changeTime){
                startTime = new DateTime(time);
                changeTime = startTime;
                enableButton(true,true);
                enableButton(true,false);
            }
            else if (endTime == changeTime){
                endTime = new DateTime(time);
                changeTime = endTime;
                enableButton(false,true);
                enableButton(false,false);
            }
            refreshTimeLabels();
        }
    }

    private void changeDependentTime(boolean isAdded, int value){
        if (checkSecondTimeChange(isAdded)){
             changeTime = endTime;
            setChangedTime(addTime(value));
            changeTime = startTime;
        }
    }

    private boolean checkSecondTimeChange(boolean isAdded){
        return startTime == changeTime && isAdded;
    }

    private long addTime(int value) {
        return changeTime.getTimeInMillis()+(value*minToMS);
    }

    private long reduzeTime(int value){
        return changeTime.getTimeInMillis() - (value*minToMS);
    }

    private class MakeReservationTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            publishProgress();
            AddressBookEntry entry = application.getAddressBook().getEntryByName(nameField.getText().toString());
            Boolean addressBookOption = application.getBooleanSettingsValue("addressBookOption", false);

            if (entry == null && addressBookOption && room == null) {
                reservatorError(new ReservatorException(getResources().getString(R.string.faildUser)));
            }

            try {
                if (entry != null) {
                    application.getDataProxy().reserve(room, new TimeSpan(startTime,endTime),
                            meetingField.getText().toString(), entry.getEmail());
                } else {
                    // Address book option is off so reserve the room with the selected account in settings.
                    String accountEmail = application.getSettingValue(R.string.accountForServation, "");
                    if (accountEmail.equals("")) {
                        reservatorError(new ReservatorException(getResources().getString(R.string.faildCheckSettings)));
                    }

                    String title =  nameField.getText().toString();
                    if ( !meetingField.getText().toString().isEmpty()) {
                        title = meetingField.getText().toString();
                    }
                    application.getDataProxy().reserve(room, new TimeSpan(startTime,endTime),title, accountEmail);
                }
            } catch (ReservatorException e) {
                reservatorError(e);
            }

            // Void requires "return null;". Java blah.
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (proxy instanceof CachedDataProxy) {
                ((CachedDataProxy) proxy).forceRefreshRoomReservations(room);
            } else {
                proxy.refreshRoomReservations(room);
            }
            finish();
        }
    }

    private void reservatorError(ReservatorException e) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(this.getString(R.string.faildReservation)).setMessage(e.getMessage());

        alertBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        alertBuilder.show();
    }

    protected void refreshTimeLabels() {
        Date date = new Date(startTime.getTimeInMillis());
        startTimeView = (TextView) findViewById(R.id.startTimeAlternative);
        startTimeView.setText(String.format("%02d:%02d",date.getHours(),date.getMinutes()));

        date = new Date(endTime.getTimeInMillis());
        endTimeView = (TextView) findViewById(R.id.endTimeAlternative);
        endTimeView.setText(String.format("%02d:%02d",date.getHours(),date.getMinutes()));
    }

    private void setButtonGroupVisible(){
        plusButton.setVisibility(View.VISIBLE);
        minusButton.setVisibility(View.VISIBLE);
        infoLabel.setVisibility(View.INVISIBLE);

        Timer buttonTimer = new Timer();
        buttonTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        plusButton.setVisibility(View.INVISIBLE);
                        minusButton.setVisibility(View.INVISIBLE);
                        infoLabel.setVisibility(View.VISIBLE);
                        changeTime = null;
                    }
                });
            }
        }, 5000);
    }

    private void enableButton(boolean isStartTime, boolean isAdded){
        long diff;
        if (isStartTime && !isAdded){
            if (endeMeeting!=null){
                    diff = startTime.subtract(endeMeeting,Calendar.MILLISECOND)/minToMS;
                    setButtonEnable(false, (int) diff);
            }
        }

        else if (!isAdded){
            diff = endTime.subtract(startTime,Calendar.MILLISECOND)/minToMS;
            setButtonEnable(false,(int)diff-MIN_MEETING_TIME);
        }

        if (reservationList.size() >= 0){
            for (Reservation r : reservationList){
                if (isStartTime){
                    if (!isAdded){
                        if (r.getEndTime().getTimeInMillis() <= startTime.getTimeInMillis()){
                            diff = startTime.subtract(r.getEndTime(),Calendar.MILLISECOND)/minToMS;
                            setButtonEnable(false, (int) diff);
                        }
                    }else {
                        if (r.getStartTime().getTimeInMillis() >= endTime.getTimeInMillis()) {
                            diff = r.getStartTime().subtract(endTime, Calendar.MILLISECOND) / minToMS;
                            setButtonEnable(true, (int) diff);
                        }
                    }
                }

                if (!isStartTime && isAdded){
                    if (r.getStartTime().getTimeInMillis() >= endTime.getTimeInMillis()) {
                        diff = r.getEndTime().subtract(endTime,Calendar.MILLISECOND)/minToMS;
                        setButtonEnable(true,(int)diff);
                    }
                }
            }
        }

    }

    private void setButtonEnable(boolean isPlusButton, int value){
        switch (value){
            case 15:
                if (isPlusButton){
                    findViewById(R.id.plus15button).setEnabled(false);
                }else {
                    findViewById(R.id.minus15button).setEnabled(false);
                }
            case 30:
                if (isPlusButton){
                    findViewById(R.id.plus30button).setEnabled(false);
                }else {
                    findViewById(R.id.minus30button).setEnabled(false);
                }
            case 60:
                if (isPlusButton){
                    findViewById(R.id.plus60button).setEnabled(false);
                }else {
                    findViewById(R.id.minus60button).setEnabled(false);
                }
                break;
            default:
                if (isPlusButton){
                    findViewById(R.id.plus15button).setEnabled(true);
                    findViewById(R.id.plus30button).setEnabled(true);
                    findViewById(R.id.plus60button).setEnabled(true);
                }else {
                    findViewById(R.id.minus15button).setEnabled(true);
                    findViewById(R.id.minus30button).setEnabled(true);
                    findViewById(R.id.minus60button).setEnabled(true);
                }
        }
    }
}
