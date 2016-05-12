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
public class quickBookReservation extends RelativeLayout implements View.OnClickListener {

    Button fifteenMinutes;
    Button thirtyMinutes;
    Button fortyFiveMinutes;
    Button sixtyMinutes;


    int minimumTime;
    int maximumTime;

    int timeStep;


    DateTime currentDay;

    DateTime t = new DateTime();



    public quickBookReservation(Context context) {
        super(context);
    }

    public quickBookReservation(Context context, AttributeSet attrs) {
        super(context, attrs);

        fifteenMinutes = (Button) findViewById(R.id.fifteenMinutes);
        thirtyMinutes = (Button) findViewById(R.id.thirtyMinutes);
        fortyFiveMinutes = (Button) findViewById(R.id.fortyFiveMinutes);
        sixtyMinutes = (Button) findViewById(R.id.sixtyMinutes);


        fifteenMinutes.setOnClickListener(this);
        thirtyMinutes.setOnClickListener(this);
        fortyFiveMinutes.setOnClickListener(this);
        sixtyMinutes.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        if (v == fifteenMinutes) {
            DateTime endTime = t.add(Calendar.MINUTE, 15);

        } else if(v == thirtyMinutes){
            DateTime endTime = t.add(Calendar.MINUTE, 30);
        } else if(v == fortyFiveMinutes){
            DateTime endTime = t.add(Calendar.MINUTE, 45);

        } else if(v == sixtyMinutes){
            DateTime endTime = t.add(Calendar.MINUTE, 60);
        }


    }



}
