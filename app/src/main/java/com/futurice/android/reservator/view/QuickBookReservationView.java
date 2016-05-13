package com.futurice.android.reservator.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.ReservatorApplication;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

import java.util.Calendar;

/**
 * Created by martha on 5/12/16.
 */
public class QuickBookReservationView extends RelativeLayout implements View.OnClickListener {

    ReservatorApplication application;

    OnQuickReserveListener onQuickReserveCallback = null;
    OnQuickCancelListener onQuickCancelListener = null;
    SharedPreferences settings;

    private Room room;
    private int animationDuration = 300;

    private ReservatorException reservatorException;

    Button fifteenMinutes;
    Button thirtyMinutes;
    Button fortyFiveMinutes;
    Button sixtyMinutes;
    Button submitQuickMeeting;

    int currentTimeStart;
    int currentTimeEnd;

    int minimumTime;
    int maximumTime;

    int minimumDuration;
    int timeStep;

    DateTime currentDay;

    DateTime t = new DateTime();

    View.OnClickListener quickBookListener;

    public QuickBookReservationView(Context context) {
        this(context, null);
    }

    public QuickBookReservationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.quickbook_reservation_popup, this);

        // roomNameView = (TextView) findViewById(R.id.roomNameLabel);

        fifteenMinutes = (Button) findViewById(R.id.fifteenMinutes);
        thirtyMinutes = (Button) findViewById(R.id.thirtyMinutes);
        fortyFiveMinutes = (Button) findViewById(R.id.fortyFiveMinutes);
        sixtyMinutes = (Button) findViewById(R.id.sixtyMinutes);
        submitQuickMeeting = (Button) findViewById(R.id.submitQuickMeeting);

        fifteenMinutes.setOnClickListener(this);
        thirtyMinutes.setOnClickListener(this);
        fortyFiveMinutes.setOnClickListener(this);
        sixtyMinutes.setOnClickListener(this);

        timeStep = 15;

        minimumDuration = 15;
        minimumTime = 0;

        currentTimeStart = minimumTime;
        currentTimeEnd = maximumTime;
    }

    @Override
    public void onClick(View v) {

        if (v == fifteenMinutes) {

            int start = quantize(currentTimeStart);
            currentTimeStart = Math.max(start, minimumTime);

            int end = quantize(currentTimeEnd);
            currentTimeEnd = Math.max(end, currentTimeStart + minimumDuration);

            DateTime endTime = t.add(Calendar.MINUTE, 15);

        } else if(v == thirtyMinutes){

            int start = quantize(currentTimeStart);
            currentTimeStart = Math.max(start, minimumTime);

            int end = quantize(currentTimeEnd);
            currentTimeEnd = Math.max(end, currentTimeStart + minimumDuration * 2);

            DateTime endTime = t.add(Calendar.MINUTE, 30);
        } else if(v == fortyFiveMinutes){

            int start = quantize(currentTimeStart);
            currentTimeStart = Math.max(start, minimumTime);

            int end = quantize(currentTimeEnd);
            currentTimeEnd = Math.max(end, currentTimeStart + minimumDuration * 3);

            DateTime endTime = t.add(Calendar.MINUTE, 45);

        } else if(v == sixtyMinutes){

            int start = quantize(currentTimeStart);
            currentTimeStart = Math.max(start, minimumTime);

            int end = quantize(currentTimeEnd);
            currentTimeEnd = Math.max(end, currentTimeStart + minimumDuration * 4);

            DateTime endTime = t.add(Calendar.MINUTE, 60);
        } else if(v == submitQuickMeeting){

        }
    }

    protected int quantize(int m) {
        return (m / timeStep) * timeStep;
    }

    public void setAnimationDuration(int millis) {
        animationDuration = millis;
    }

    public void setRoom(Room room) {
        this.room = room;

          // Room stuff
//        roomNameView.setText(room.getName());
//
//        if (room.getCapacity() >= 0) {
//            roomInfoView.setText("for " + room.getCapacity());
//        } else {
//            roomInfoView.setText("");
//        }

        // Reservation stuff
        TimeSpan nextFreeTime = room.getNextFreeTime();

//        timePicker2.reset();
//        if (nextFreeTime != null) {
//            timePicker2.setMinimumTime(nextFreeTime.getStart());
//            timePicker2.setMaximumTime(nextFreeTime.getEnd());
//        } else {
//            timePicker2.setMinimumTime(new DateTime());
//        }
//        timePicker2.setEndTimeRelatively(60); // let book the room for an hour

        //roomStatusView.setText(room.getStatusText());

    }

    public void resetTimeSpan() {
        minimumTime = 0;
        maximumTime = 24 * 60;

        currentDay = new DateTime().stripTime();
        currentTimeStart = minimumTime;
        currentTimeEnd = maximumTime;
        refreshButtonStates();
    }

    public void setMinTime(DateTime time) {
        int min = time.get(Calendar.HOUR_OF_DAY) * 60 + time.get(Calendar.MINUTE);
        if (min > maximumTime)
            throw new IllegalArgumentException("setting minimumTime to be after the maximum");
        minimumTime = min;
        if (currentTimeStart < minimumTime) {
            currentTimeStart = minimumTime;
        }

        if (currentTimeEnd < minimumTime) {
            currentTimeEnd = Math.min(maximumTime, minimumTime + minimumDuration);
        }

        currentDay = time.stripTime();

        refreshButtonStates();
    }

    public void setMaxTime(DateTime time) {
        int max = time.get(Calendar.HOUR_OF_DAY) * 60 + time.get(Calendar.MINUTE);
        boolean tomorrow = false;

        if (max == 0) {
            max = 24 * 60; // full day
            tomorrow = true;
        }

        if (max < minimumTime)
            throw new IllegalArgumentException("setting maximumTime to be before the minimum");

        maximumTime = max;
        if (currentTimeEnd > maximumTime) {
            currentTimeEnd = maximumTime;
        }

        if (currentTimeStart > maximumTime) {
            currentTimeStart = Math.max(minimumTime, maximumTime - minimumDuration);
        }

        currentDay = time.stripTime();
        if (tomorrow) {
            currentDay = currentDay.add(Calendar.DAY_OF_YEAR, -1);
        }
    }

    public void setEndTimeRelatively(int minutes) {
        int end = quantize(currentTimeStart + minutes);

        currentTimeEnd = Math.min(end, maximumTime);

        refreshButtonStates();
    }

    public void setStartTime(DateTime time) {
        int start = time.get(Calendar.HOUR_OF_DAY) * 60 + time.get(Calendar.MINUTE);

        if (start < minimumTime || start > maximumTime) {
            throw new IllegalArgumentException("setting startTime outside of minmax");
        }

        if (start > currentTimeEnd) {
            throw new IllegalArgumentException("setting startTime after endTime");
        }

        currentTimeStart = start;
        currentDay = time.stripTime();

        refreshButtonStates();
    }

    public void setEndTime(DateTime time) {
        int end = time.get(Calendar.HOUR_OF_DAY) * 60 + time.get(Calendar.MINUTE);

        if (end < minimumTime || end > maximumTime) {
            throw new IllegalArgumentException("setting endTime outside of minmax");
        }

        if (end < currentTimeStart) {
            throw new IllegalArgumentException("setting endTime before startTime");
        }

        currentTimeEnd = end;
        currentDay = time.stripTime();

        refreshButtonStates();
    }

    protected void refreshButtonStates() {


//        startLabel.setText(String.format("%02d:%02d", currentTimeStart / 60, currentTimeStart % 60));
//        endLabel.setText(String.format("%02d:%02d", currentTimeEnd / 60, currentTimeEnd % 60));
//
//        timeBar.setTimeLimits(new TimeSpan(getMinimumTime(), getMaximumTime()));
//        timeBar.setSpan(new TimeSpan(getStartTime(), getEndTime()));
//
//        startMinus.setEnabled(currentTimeStart != minimumTime);
//        startPlus.setEnabled(currentTimeStart < currentTimeEnd - minimumDuration);
//        endMinus.setEnabled(currentTimeEnd > currentTimeStart + minimumDuration);
//        endPlus.setEnabled(currentTimeEnd != maximumTime);
    }

    public void setOnQuickCancelListener(OnQuickCancelListener l) {
        this.onQuickCancelListener = l;
    }

    public void setOnQuickReserveCallback(OnQuickReserveListener onQuickReserveCallback) {
        this.onQuickReserveCallback = onQuickReserveCallback;
    }

    public interface OnQuickCancelListener {
        public void onCancel(QuickBookReservationView view);
    }

    public interface OnQuickReserveListener {
        public void call(QuickBookReservationView v);
    }

    public void setReserveMode() {
        if (animationDuration > 0) {
            Animation scale = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(animationDuration);
            scale.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    switchToReserveModeContent();
                    Animation scale = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                    scale.setDuration(animationDuration);
                    startAnimation(scale);
                }
            });
            startAnimation(scale);
        } else {
            switchToReserveModeContent();
        }
    }

    private void switchToReserveModeContent() {


//        if (modeSwitcher.indexOfChild(bookingMode) < 0) {
//            modeSwitcher.addView(bookingMode);
//        }
//        modeSwitcher.setDisplayedChild(modeSwitcher.indexOfChild(bookingMode));
//        setBackgroundColor(getResources().getColor(R.color.ReserveBackground));
//
//        // Initial state for the "Reserve" button.
//        if (application.getBooleanSettingsValue("addressBookOption", false)) {
//            reserveButton.setEnabled(false);
//            findViewById(R.id.hintText).setVisibility(View.GONE);
//        } else {
//            reserveButton.setEnabled(true);
//            findViewById(R.id.hintText).setVisibility(View.VISIBLE);
//        }

    }

}
