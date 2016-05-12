package com.futurice.android.reservator.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.DateTime;

import java.util.Calendar;

/**
 * Created by martha on 5/12/16.
 */
public class QuickBookReservation extends RelativeLayout implements View.OnClickListener {

    Button fifteenMinutes;
    Button thirtyMinutes;
    Button fortyFiveMinutes;
    Button sixtyMinutes;

    int currentTimeStart;
    int currentTimeEnd;

    int minimumTime;
    int maximumTime;

    int minimumDuration;
    int timeStep;

    DateTime currentDay;

    DateTime t = new DateTime();

    public QuickBookReservation(Context context) {
        this(context, null);
    }

    public QuickBookReservation(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.quickbook_reservation, this);

        fifteenMinutes = (Button) findViewById(R.id.fifteenMinutes);
        thirtyMinutes = (Button) findViewById(R.id.thirtyMinutes);
        fortyFiveMinutes = (Button) findViewById(R.id.fortyFiveMinutes);
        sixtyMinutes = (Button) findViewById(R.id.sixtyMinutes);

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
        }


    }

    protected int quantize(int m) {
        return (m / timeStep) * timeStep;
    }



}
