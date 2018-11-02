package com.futurice.android.reservator.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

import butterknife.BindView;

public class WeekView extends RelativeLayout implements OnClickListener {

    public static final int DAY_START_TIME = 60 * 8; // minutes from midnight
    public static final int DAY_END_TIME = 60 * 20;
    public static final int NORMALIZATION_START_HOUR = 20;
    private int numberOfDaysToShow;

    private OnFreeTimeClickListener onFreeTimeClickListener = null;
    private OnReservationClickListener onReservationClickListener = null;

    private FrameLayout calendarFrame;

    public WeekView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public WeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeekView, defStyle, 0);
        numberOfDaysToShow = a.getInteger(R.styleable.WeekView_number_of_days_to_show, 0);
        a.recycle();
    }

    public void refreshData(Room room) {
        //calendarFrame = (FrameLayout) findViewById(R.id.frameLayout1);
        calendarFrame =(FrameLayout) ((ViewGroup)this).getChildAt(0);

        calendarFrame.removeAllViews();
        List<Reservation> reservations = new ArrayList<Reservation>();

        DateTime startOfToday = new DateTime().setTime(0, 0, 0);
        TimeSpan day = new TimeSpan(
            startOfToday.add(Calendar.MINUTE, DAY_START_TIME),
            startOfToday.add(Calendar.MINUTE, DAY_END_TIME));

        for (int i = 0; i < numberOfDaysToShow; i++) {
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

    public interface OnFreeTimeClickListener {
        void onFreeTimeClick(View v, TimeSpan timeSpan, DateTime clickTime);
    }

    public interface OnReservationClickListener {
        void onReservationClick(View v, Reservation r);
    }
}
