package com.futurice.android.reservator.view.trafficlights;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.Helpers;

import butterknife.BindView;

public class RoomReservationFragment extends Fragment {

    public interface RoomReservationPresenter {
        void setRoomReservationFragment(RoomReservationFragment fragment);
        void onReservationRequestMade(int minutes, String description);
        void onMinutesUpdated(int minutes);
    }

    private RoomReservationPresenter presenter;

    private TextView textViewBarDuration;
    private android.widget.SeekBar seekBar;

    EditText nameInput;
    Button reserveButton;

    private int maxMinutes = 0;
    private int minutes = 0;

    private void updateTimeLimitsToUi() {
        if (this.seekBar != null) {
            this.seekBar.setMax(this.maxMinutes);
        }
    }

    private void updateMinutesToUi() {
        if (this.seekBar != null) {
            textViewBarDuration.setText(Helpers.convertToHoursAndMinutes(this.minutes));
            this.seekBar.setProgress(this.minutes);
        }
    }
    public void setPresenter(RoomReservationPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setRoomReservationFragment(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.room_reservation_fragment, container, false);

        this.textViewBarDuration = (TextView) view.findViewById(R.id.TextViewBarDuration);
        this.seekBar = (SeekBar) view.findViewById(R.id.seekBar);

        this.updateTimeLimitsToUi();
        this.seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        nameInput = (EditText) view.findViewById(R.id.nameInput);
        reserveButton = (Button) view.findViewById(R.id.reserveButton);

        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onReservationRequestMade(seekBar.getProgress(), nameInput.getText().toString());
            }
        });
        return view;
    }

    public void setMaxMinutes(int maxMinutes) {
       this.maxMinutes = maxMinutes;
       this.updateTimeLimitsToUi();
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
        this.updateMinutesToUi();
    }

    public void clearDescription() {
        if (this.nameInput != null)
            this.nameInput.setText("");
    }


    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        int minutesIncrement = 5;

        @Override
        public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean b) {
            //progress = ((int) Math.round(progress / minutesIncrement)) * minutesIncrement;
            //seekBar.setProgress(progress);

            minutes = progress;

            if (progress==0){
                reserveButton.setEnabled(false);
                reserveButton.setBackgroundColor(getResources().getColor(R.color.ReserveButtonUnavailable));
            } else if (!reserveButton.isEnabled() && progress > 0){
                reserveButton.setEnabled(true);
                reserveButton.setBackgroundColor(getResources().getColor(R.color.ReserveButtonAvailable));
            }

            textViewBarDuration.setText(Helpers.convertToHoursAndMinutes(progress));
            presenter.onMinutesUpdated(minutes);
        }

        @Override
        public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(android.widget.SeekBar seekBar) {

        }
    };

    @Override
    public void onResume(){
        super.onResume();
        this.updateTimeLimitsToUi();
        this.updateMinutesToUi();
    }
}
