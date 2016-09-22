package com.futurice.android.reservator.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WeekView extends RelativeLayout implements OnClickListener {

    public static final int NUMBER_OF_DAYS_TO_SHOW = 10;
    public static final int DAY_START_TIME = 60 * 8; // minutes from midnight
    public static final int DAY_END_TIME = 60 * 20;

    public static final int NORMALIZATION_START_HOUR = 20;

    private FrameLayout calendarFrame = null;
    private OnFreeTimeClickListener onFreeTimeClickListener = null;
    private OnReservationClickListener onReservationClickListener = null;
    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void refreshData(Room room) {
        calendarFrame = (FrameLayout) findViewById(R.id.frameLayout1);
        calendarFrame.removeAllViews();
        List<Reservation> reservations = new ArrayList<Reservation>();

        DateTime startOfToday = new DateTime().setTime(0, 0, 0);
        TimeSpan day = new TimeSpan(
            startOfToday.add(Calendar.MINUTE, DAY_START_TIME),
            startOfToday.add(Calendar.MINUTE, DAY_END_TIME));

        for (int i = 0; i < NUMBER_OF_DAYS_TO_SHOW; i++) {
            List<Reservation> dayReservations = room.getReservationsForTimeSpan(day);
            List<Reservation> boundDayReservations = new ArrayList<Reservation>(dayReservations.size());

            // Change multi-day reservations to span only this day
            for (Reservation res : dayReservations) {
                if (res.getStartTime().before(day.getStart()) || res.getEndTime().after(day.getEnd())) {
                    boundDayReservations.add(new Reservation(
                        res.getId() + "-" + day.getStart(),
                        res.getSubject(),
                        new TimeSpan(
                            res.getStartTime().before(day.getStart()) ? day.getStart() : res.getStartTime(),
                            res.getEndTime().after(day.getEnd()) ? day.getEnd() : res.getEndTime())));
                } else {
                    boundDayReservations.add(res);
                }
            }

            reservations.addAll(boundDayReservations);

            // Advance to next day
            day = new TimeSpan(
                day.getStart().add(Calendar.DAY_OF_YEAR, 1),
                day.getEnd().add(Calendar.DAY_OF_YEAR, 1));
        }

        CalendarVisualizer cv = new CalendarVisualizer(getContext(), DAY_START_TIME, DAY_END_TIME);
        cv.setReservations(reservations);
        calendarFrame.addView(cv, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        cv.setOnClickListener(this);
        return;
    }

    public void setOnFreeTimeClickListener(OnFreeTimeClickListener onFreeTimeClickListener) {
        this.onFreeTimeClickListener = onFreeTimeClickListener;
    }

    public void setOnReservationClickListener(OnReservationClickListener onReservationClickListener) {
        this.onReservationClickListener = onReservationClickListener;
    }

    @Override
    public void onClick(final View v) {

        if (v instanceof ReservatorVisualizer) {
            ReservatorVisualizer visualizer = (ReservatorVisualizer) v;

            final Reservation clickedReservation = visualizer.getSelectedReservation();
            if (clickedReservation != null) {
                // User clicked a reservation
                if (onReservationClickListener != null) {
                    onReservationClickListener.onReservationClick(v, clickedReservation);
                }
            } else {
                // User clicked a free time slot
                if (onFreeTimeClickListener != null) {
                    onFreeTimeClickListener.onFreeTimeClick(v,
                        visualizer.getSelectedTimeSpan(), visualizer.getSelectedTime());
                }
            }
        }
    }

    // TODO What is this? If this is unusable, remove the commented section when cleaning up
    /*
        calendarView = new CalendarView(getContext());

        DateTime today = new DateTime().stripTime();

        DateTime day = today.set(Calendar.HOUR_OF_DAY, 8);
        for (int i = 0; i < NUMBER_OF_DAYS_TO_SHOW; i++, day = day.add(Calendar.DAY_OF_YEAR, 1)) {
            // Skip weekend days
            if (day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                i -= 1;
                continue;
            }

            DateTime endOfDay = day.set(Calendar.HOUR_OF_DAY, 18);

            calendarView.addDay(day);

            List<Reservation> daysReservations = room.getReservationsForDay(day);

            if (daysReservations.isEmpty()) {
                addFreeMarker(day, endOfDay);
            } else {
                Reservation first = daysReservations.get(0);
                if (first.getStartTime().after(day)) {
                    addFreeMarker(day, first.getStartTime());
                }
                for (int j = 0; j < daysReservations.size(); j++) {
                    Reservation current = daysReservations.get(j);
                    addReservedMarker(current);
                    if (j < daysReservations.size() - 1) {
                        Reservation next = daysReservations.get(j + 1);
                        if(next.getStartTime().after(current.getEndTime())){
                            addFreeMarker(current.getEndTime(), next.getStartTime());
                        }
                    }
                }
                Reservation last = daysReservations.get(daysReservations.size() - 1);
                if(last.getEndTime().before(endOfDay)){
                    addFreeMarker(last.getEndTime(), endOfDay);
                }
            }
        }
        addDisabledMarker(today, new DateTime());
        calendarFrame.addView(calendarView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

    }
*/
    public static interface OnFreeTimeClickListener {
        abstract void onFreeTimeClick(View v, TimeSpan timeSpan, DateTime clickTime);
    }

    public static interface OnReservationClickListener {
        abstract void onReservationClick(View v, Reservation r);
    }
/*
    private void addFreeMarker(DateTime startTime, DateTime endTime) {
        if(startTime.after(endTime)){
            throw new IllegalArgumentException("starTime must be before endTime");
        }
        CalendarMarker marker = calendarView.addMarker(startTime, endTime);
        marker.setOnClickListener(this);
        marker.setReserved(false);
    }
    private void addDisabledMarker(DateTime startTime, DateTime endTime){
        if(startTime.after(endTime)){
            throw new IllegalArgumentException("starTime must be before endTime");
        }
        CalendarMarker marker = calendarView.addMarker(startTime, endTime);
        marker.setClickable(true); //blocks clicks from views it covers
        marker.setReserved(true);
        marker.setBackgroundColor(getResources().getColor(R.color.CalendarDisabledColor));
    }
    private void addReservedMarker(Reservation r) {
        CalendarMarker marker = calendarView.addMarker(r.getStartTime(),
                r.getEndTime());
        marker.setText(r.getSubject());
        marker.setReserved(true);
    }
*/
}
