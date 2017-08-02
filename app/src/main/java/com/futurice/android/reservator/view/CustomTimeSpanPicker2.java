package com.futurice.android.reservator.view;

import java.util.Calendar;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.TimeSpan;

import android.widget.FrameLayout;
import android.widget.TextView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomTimeSpanPicker2 extends FrameLayout implements OnClickListener {
    DateTime currentDay;

    int currentTimeStart;
    int currentTimeEnd;

    int minimumTime;
    int maximumTime;

    int minimumDuration;
    int timeStep;

    @BindView(R.id.startMinus)
    View startMinus;
    @BindView(R.id.startPlus)
    View startPlus;
    @BindView(R.id.endMinus)
    View endMinus;
    @BindView(R.id.endPlus)
    View endPlus;
    @BindView(R.id.startTimeLabel)
    TextView startLabel;
    @BindView(R.id.endTimeLabel)
    TextView endLabel;
    @BindView(R.id.timeBarView)
    TimeBarView timeBar;

    public CustomTimeSpanPicker2(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CustomTimeSpanPicker2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    /*
     * Invariants:
     *
     * minimumTime <= currentTimeStart <= currentTimeEnd <= maximumTime
     *
     * Assumptions:
     * 24 hours in day, 60 minutes in hour!
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        inflate(context, R.layout.timespan_picker2, this);
        ButterKnife.bind(this);

        startMinus.setOnClickListener(this);
        startPlus.setOnClickListener(this);
        endMinus.setOnClickListener(this);
        endPlus.setOnClickListener(this);

        minimumDuration = 30;
        timeStep = 30;

        reset();
    }

    public void reset() {
        minimumTime = 0;
        maximumTime = 24 * 60;

        currentDay = new DateTime().stripTime();
        currentTimeStart = minimumTime;
        currentTimeEnd = maximumTime;
        refreshLabels();
    }

    protected int quantize(int m) {
        return (m / timeStep) * timeStep;
    }

    @Override
    public void onClick(View v) {
        //start doing the animations
        timeBar.enableAnimation();

        if (v == startMinus) {
            int start = quantize(currentTimeStart - timeStep);

            currentTimeStart = Math.max(start, minimumTime);

            // refreshing texts
            refreshLabels();
        } else if (v == startPlus) {
            int start = quantize(currentTimeStart + timeStep);

            currentTimeStart = Math.min(start, currentTimeEnd - minimumDuration);
            refreshLabels();
        } else if (v == endMinus) {
            int end = quantize(currentTimeEnd - timeStep);

            currentTimeEnd = Math.max(end, currentTimeStart + minimumDuration);
            refreshLabels();
        } else if (v == endPlus) {
            int end = quantize(currentTimeEnd + timeStep);

            currentTimeEnd = Math.min(end, maximumTime);
            refreshLabels();
        }
    }

    protected void refreshButtonStates() {
        startMinus.setEnabled(currentTimeStart != minimumTime);
        startPlus.setEnabled(currentTimeStart < currentTimeEnd - minimumDuration);
        endMinus.setEnabled(currentTimeEnd > currentTimeStart + minimumDuration);
        endPlus.setEnabled(currentTimeEnd != maximumTime);
    }

    protected void refreshLabels() {
        startLabel.setText(String.format("%02d:%02d", currentTimeStart / 60, currentTimeStart % 60));
        endLabel.setText(String.format("%02d:%02d", currentTimeEnd / 60, currentTimeEnd % 60));

        timeBar.setTimeLimits(new TimeSpan(getMinimumTime(), getMaximumTime()));
        timeBar.setSpan(new TimeSpan(getStartTime(), getEndTime()));

        refreshButtonStates();
    }

    public void setMinimumDuration(int minimumDuration) {
        this.minimumDuration = minimumDuration;
    }

    public void setTimeStep(int timeStep) {
        this.timeStep = timeStep;
    }

    public void setEndTimeRelatively(int minutes) {
        int end = quantize(currentTimeStart + minutes);

        currentTimeEnd = Math.min(end, maximumTime);

        refreshLabels();
    }

    /**
     * Duration of the selected time span in minutes.
     *
     * @return duration in minutes.
     */
    public int getDuration() {
        return currentTimeEnd - currentTimeStart;
    }

    public DateTime getStartTime() {
        return currentDay.setTime(currentTimeStart / 60, currentTimeStart % 60, 0);
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

        refreshLabels();
    }

    public DateTime getEndTime() {
        return currentDay.setTime(currentTimeEnd / 60, currentTimeEnd % 60, 0);
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

        refreshLabels();
    }

    private DateTime getMaximumTime() {
        return currentDay.setTime(maximumTime / 60, maximumTime % 60, 0);
    }

    public void setMaximumTime(DateTime time) {
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

        refreshLabels();
    }

    private DateTime getMinimumTime() {
        return currentDay.setTime(minimumTime / 60, minimumTime % 60, 0);
    }

    public void setMinimumTime(DateTime time) {

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

        refreshLabels();
    }

    public TimeSpan getTimeSpan() {
        return new TimeSpan(getStartTime(), getEndTime());
    }

}
