package com.futurice.android.reservator.view.trafficlightsactivity;

import android.app.Fragment;
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

    private ReservationRequestListener listener;
    private TextView textViewBarBegin;
    private TextView textViewBarEnd;
    private TextView textViewBarDuration;
    private android.widget.SeekBar seekBar;

    String nameGiven;
    int meetingDuration;
    EditText nameInput;

    @BindView(R.id.reserveButton)
    Button reserveButton;

    public interface ReservationRequestListener {
        public void onReservationRequestMade(long reservationDuration, String reservationName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        textViewBarBegin = (TextView) getActivity().findViewById(R.id.TextViewBarBegin);
        textViewBarEnd = (TextView) getActivity().findViewById(R.id.TextViewBarEnd);
        textViewBarDuration = (TextView) getActivity().findViewById(R.id.TextViewBarDuration);
        seekBar = (SeekBar) getActivity().findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        int progress = seekBar.getProgress();
        textViewBarBegin.setText("Progress: " + progress);

        nameInput = (EditText) getActivity().findViewById(R.id.nameInput);
        reserveButton = (Button) getActivity().findViewById(R.id.reserveButton);

        reserveButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 nameGiven = nameInput.getText().toString();
                 //meetingDuration = get current time + duration of meeting


                 if (nameGiven.equals("")) { //Throw error message or change text to give error
                     nameInput.setText("Enter a valid name");
                 } else {
                     listener.onReservationRequestMade(getCurrentTime(),nameGiven);
                 }
             }
        });

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.room_reservation_fragment, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ReservationRequestListener) (((PresenterView)context).getPresenter());
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ReservationRequestListener");
        }
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

            textViewBarBegin.setText(getCurrentTime() + "");

            //progress += Integer.valueOf(getCurrentTime());
            seekBar.setProgress(progress);
            textViewBarDuration.setText(progress + " min.");
            textViewBarEnd.setText("" + progress); //Add amount to current time
        }

        @Override
        public void onStartTrackingTouch(android.widget.SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(android.widget.SeekBar seekBar) {

        }
    };
}
