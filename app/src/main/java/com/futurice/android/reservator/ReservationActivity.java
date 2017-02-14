package com.futurice.android.reservator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

public class ReservationActivity extends ReservatorActivity implements View.OnClickListener{
    private long minToMS = 60000;
    private final int MIN_MEETING_TIME = 15;

    private Room room;
    private ReservatorApplication application;
    private DateTime startTime, endTime, changeTime;
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
    private int plusDiff;
    private int negDiff;

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
        roomLabel.setText(settings.getString("roomShownName",""));

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

    public Room getRoom() {
        return room;
    }

    private void setBothTimes(long startTimeInput, long endTimeInput){
        setTimetoCurrentTime();

        // Reservation stuff
        TimeSpan nextFreeTime = room.getNextFreeTime();

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
        if (ableToReserve()) {
            reserveButton.setEnabled(true);
        };

        switch (view.getId()) {
            case R.id.reserveButtonAlternativ:
                if (ableToReserve()){
                    new MakeReservationTask().execute();
                    this.finish();
                } else {
                    infoLabel.setText(R.string.infoMeetingName);
                    infoLabel.setTextColor(getResources().getColor(R.color.TrafficLightReserved));
                }
                break;

            case R.id.startTimeAlternative:
                refreshButtonEnabled();
                changeTime = startTime;
                checkBookable(true);
                setButtonGroupVisible();
                break;

            case R.id.endTimeAlternative:
                refreshButtonEnabled();
                changeTime = endTime;
                checkBookable(false);
                setButtonGroupVisible();
                break;

            case R.id.plus15button:
                setChangedTime(addTime(15));
                break;

            case R.id.plus30button:
                setChangedTime(addTime(30));
                break;

            case R.id.plus60button:
                setChangedTime(addTime(60));
                break;

            case R.id.minus15button:
                setChangedTime(addTime(-15));
                break;

            case R.id.minus30button:
                setChangedTime(addTime(-30));
                break;

            case R.id.minus60button:
                setChangedTime(addTime(-60));
                break;
        }
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
            }
            else if (endTime == changeTime){
                endTime = new DateTime(time);
                changeTime = endTime;
            }
            refreshTimeLabels();
        }
    }

    private long addTime(int value) {
        if (plusDiff != 0){
            return changeTime.getTimeInMillis()+(plusDiff*minToMS);
        }

        else if (negDiff != 0){
            return changeTime.getTimeInMillis()+(negDiff*minToMS);
        }

        return changeTime.getTimeInMillis()+(value*minToMS);
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
                            entry.getName(), entry.getEmail(), meetingField.getText().toString());
                } else {
                    // Address book option is off so reserve the room with the selected account in settings.

                    String accountEmail = settings.getString(getString(R.string.accountForServation), "");
                    if (accountEmail.equals("")) {
                        reservatorError(new ReservatorException(getResources().getString(R.string.faildCheckSettings)));
                    }

                    String title =  meetingField.getText().toString();
                    if ( !meetingField.getText().toString().isEmpty()) {
                        title = meetingField.getText().toString();
                    }
                    application.getDataProxy().reserve(room, new TimeSpan(startTime,endTime),nameField.getText().toString(), accountEmail,title);
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

        negDiff = 0;
        plusDiff = 0;
        checkBookable(true);
        checkBookable(false);
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

    public void checkBookable(boolean isStartTime){
        long diff;
        if (isStartTime){
            //test end of meeting <= startTime
            Reservation reservation = room.getCurrentReservation();
            if (reservation!=null){
                diff = startTime.subtract(reservation.getEndTime(),Calendar.MILLISECOND)/minToMS;
                setButtonEnable(false, (int) diff);
            }
            //test timediff between end and start time if startTime add time not greater endtime
            diff = endTime.subtract(startTime,Calendar.MILLISECOND)/minToMS;
            setButtonEnable(true,(int)diff-MIN_MEETING_TIME);

        } else {
            //test timediff between end and start time if endtimeTime add time not smaller starttime
            diff = endTime.subtract(startTime,Calendar.MILLISECOND)/minToMS;
            setButtonEnable(false,(int)diff-MIN_MEETING_TIME);
        }

        //test end or starttime of mieetings, endMeeting >= startTime or startMeeting >= endTime
        reservationList = room.getReservationsForTimeSpan(new TimeSpan(new DateTime(startTime.getTime()).setTime(0,0,0),new DateTime(endTime.getTime()).setTime(23,0,0)));
        if (reservationList.size() >= 0){
            for (Reservation r : reservationList){
                if (isStartTime){
                    if (r.getEndTime().getTimeInMillis() <= startTime.getTimeInMillis()){
                            diff = startTime.subtract(r.getEndTime(),Calendar.MILLISECOND)/minToMS;
                            setButtonEnable(false, (int) diff);
                    }

                    if (r.getStartTime().getTimeInMillis() >= endTime.getTimeInMillis()) {
                            diff = r.getStartTime().subtract(endTime, Calendar.MILLISECOND) / minToMS;
                            setButtonEnable(true, (int) diff);
                    }
                }

                if (!isStartTime){
                    if (r.getStartTime().getTimeInMillis() >= endTime.getTimeInMillis()) {
                        diff = r.getStartTime().subtract(endTime,Calendar.MILLISECOND)/minToMS;
                        setButtonEnable(true,(int)diff);
                    }
                }
            }
        }

    }

    private void setButtonEnable(boolean isPlusButton, int value){
        if (isPlusButton){
            if (value == 0){
                findViewById(R.id.plus15button).setEnabled(false);
                findViewById(R.id.plus30button).setEnabled(false);
                findViewById(R.id.plus60button).setEnabled(false);
            } else if (value < 15) {
                plusDiff = value;
                findViewById(R.id.plus30button).setEnabled(false);
                findViewById(R.id.plus60button).setEnabled(false);
            } else  if (value < 30){
                findViewById(R.id.plus30button).setEnabled(false);
                findViewById(R.id.plus60button).setEnabled(false);
            } else if (value < 60) {
                findViewById(R.id.plus60button).setEnabled(false);
            }
        } else {
            if (value == 0){
                findViewById(R.id.minus15button).setEnabled(false);
                findViewById(R.id.minus30button).setEnabled(false);
                findViewById(R.id.minus60button).setEnabled(false);
            } else if (value < 15) {
                negDiff = - value;
                findViewById(R.id.minus30button).setEnabled(false);
                findViewById(R.id.minus60button).setEnabled(false);
            } else  if (value < 30){
                findViewById(R.id.minus30button).setEnabled(false);
                findViewById(R.id.minus60button).setEnabled(false);
            } else if (value < 60) {
                findViewById(R.id.minus60button).setEnabled(false);
            }
        }
    }

    public void refreshButtonEnabled(){
        findViewById(R.id.plus15button).setEnabled(true);
        findViewById(R.id.plus30button).setEnabled(true);
        findViewById(R.id.plus60button).setEnabled(true);
        findViewById(R.id.minus15button).setEnabled(true);
        findViewById(R.id.minus30button).setEnabled(true);
        findViewById(R.id.minus60button).setEnabled(true);
    }
}
