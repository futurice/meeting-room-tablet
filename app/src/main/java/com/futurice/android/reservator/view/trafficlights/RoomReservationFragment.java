package com.futurice.android.reservator.view.trafficlights;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import static java.lang.Math.toIntExact;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.PresenterView;
import com.futurice.android.reservator.model.DateTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Date;

import butterknife.BindView;

public class RoomReservationFragment extends Fragment {

    public interface RoomReservationPresenter {
        void setRoomReservationFragment(RoomReservationFragment fragment);
        void onReservationRequestMade(long reservationDuration, String reservationName);
    }

    private RoomReservationPresenter presenter;


    //private TextView textViewBarBegin;
    //private TextView textViewBarEnd;

    private TextView textViewBarDuration;
    private android.widget.SeekBar seekBar;

    String nameGiven;
    int meetingDuration;
    EditText nameInput;

    @BindView(R.id.reserveButton)
    Button reserveButton;

    private int maxMinutes = 0;
    private long minTime = 0;
    private long maxTime = 0;

    private void updateTimeLimitsToUi() {
        if (this.seekBar != null) {
            this.seekBar.setMax(this.maxMinutes);
        }
        /*
        if (this.seekBar != null && this.textViewBarBegin != null) {
            this.seekBar.setMax(this.maxMinutes);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(this.minTime);
            this.textViewBarBegin.setText(cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE));
        }
        */
    }

    public void setPresenter(RoomReservationPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setRoomReservationFragment(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*
        try {
            presenter = (RoomReservationPresenter) (((PresenterView)context).getPresenter());
            presenter.setRoomReservationFragment(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RoomReservationPresenter");
        }
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.room_reservation_fragment, container, false);

        //this.textViewBarBegin = (TextView) view.findViewById(R.id.TextViewBarBegin);
        //this.textViewBarEnd = (TextView) view.findViewById(R.id.TextViewBarEnd);

        this.textViewBarDuration = (TextView) view.findViewById(R.id.TextViewBarDuration);
        this.seekBar = (SeekBar) view.findViewById(R.id.seekBar);


        this.updateTimeLimitsToUi();
        this.seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        int progress = seekBar.getProgress();



        nameInput = (EditText) view.findViewById(R.id.nameInput);
        reserveButton = (Button) view.findViewById(R.id.reserveButton);

        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameGiven = nameInput.getText().toString();
                //meetingDuration = get current time + duration of meeting


                if (nameGiven.equals("")) { //Throw error message or change text to give error
                    nameInput.setText("Enter a valid name");
                } else {
                    presenter.onReservationRequestMade(getCurrentTime(), nameGiven);
                }
            }
        });
        return view;
    }


    public void setTimeLimits(long minTime, long maxTime) {
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.maxMinutes = (int)(maxTime-minTime)/1000/60;
        this.updateTimeLimitsToUi();
    }

    public long getCurrentTime(){
        com.futurice.android.reservator.model.DateTime currentTime = new com.futurice.android.reservator.model.DateTime();
        currentTime.getTime();

        long currentTimeMinutes = currentTime.getTimeInMillis();

        /*int correctionToClosestFiveMinutes = currentMinutes % 5;
        LocalDateTime quantizedDate = now.withMinute(currentMinutes - correctionToClosestFiveMinutes).withSecond(0).withNano(0);
        ZonedDateTime zdt = quantizedDate.atZone(ZoneId.systemDefault());*/

        return currentTimeMinutes;
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        int minutesIncrement = 5;
        //int currentTime = Integer.valueOf(getCurrentTime());

        @Override
        public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean b) {
            progress = ((int) Math.round(progress / minutesIncrement)) * minutesIncrement; //This should now move in 5 (minute) increments
            //For some reason, progress shows up as a string saying progress instead of immediately changing to the int

            //textViewBarBegin.setText(getCurrentTime() + "");

            //progress += Integer.valueOf(getCurrentTime());
            seekBar.setProgress(progress);
            textViewBarDuration.setText(progress + " min.");
            //textViewBarEnd.setText("" + progress); //Add amount to current time
        }

        @Override
        public void onStartTrackingTouch(android.widget.SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(android.widget.SeekBar seekBar) {

        }
    };
}
