package com.futurice.android.reservator;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.rule.ActivityTestRule;
import android.widget.TextView;

import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.TimeSpan;
import com.futurice.android.reservator.model.platformcalendar.PlatformCalendarRoom;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;
import java.util.Vector;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

public class ReservationActivityTest {

    @Rule
    public ActivityTestRule<ReservationActivity> mActivityRule = new ActivityTestRule<>(ReservationActivity.class);
    private Context context;
    private Date currentTime = new Date();

    @Before
    public void setUp(){
        context = mActivityRule.getActivity().getBaseContext();
        PlatformCalendarRoom room = new PlatformCalendarRoom("room", "fake@sdff.de", 504, "loc");
        Vector<Reservation> reservationVector = new Vector<>();
        Vector<String> attendees = new Vector<>();
        attendees.add("room");
        reservationVector.add(new Reservation("id","meeting", new TimeSpan(new DateTime(currentTime.getTime()-1800000),new DateTime(currentTime.getTime()+1800000)), attendees));
        room.setReservations(reservationVector);

        SharedPreferences.Editor editor = mActivityRule.getActivity().getSharedPreferences(context.getString(R.string.PREFERENCES_NAME), mActivityRule.getActivity().MODE_PRIVATE).edit();
        editor.putString("roomName", room.getName().trim());
        editor.putString("roomShownName", room.getShownRoomName().trim());
        editor.putLong("resTimestart",currentTime.getTime()+1800000);
        editor.putLong("resTimeend",currentTime.getTime()+(4*1800000));
        editor.apply();

        mActivityRule.getActivity().getRoom().setReservations(reservationVector);
    }

    @Test
    public void isTimeLabelRefreshed() throws Exception {
        TextView view = (TextView)mActivityRule.getActivity().findViewById(R.id.startTimeAlternative);
        String startTime = view.getText().toString();

        onView(withId(R.id.startTimeAlternative)).perform(click());
        onView(withId(R.id.plus15button)).perform(click());
        onView(withId(R.id.startTimeAlternative)).check(matches(not(withText(startTime))));
    }

    @Test
    public void buttonsAreEnabled() throws Exception {
        onView(withId(R.id.startTimeAlternative)).perform(click());
        onView(withId(R.id.plus15button)).check(matches(isEnabled()));
        onView(withId(R.id.plus30button)).check(matches(isEnabled()));
        onView(withId(R.id.plus60button)).check(matches(isEnabled()));
        onView(withId(R.id.minus15button)).check(matches(isEnabled()));
        onView(withId(R.id.minus30button)).check(matches(isEnabled()));
        onView(withId(R.id.minus60button)).check(matches(isEnabled()));
    }

    @Test
    public void isStarttimeLowerEndtime() throws Exception {
        onView(withId(R.id.startTimeAlternative)).perform(click());
        for (int i= 4; i > 0; i--){
            onView(withId(R.id.plus60button)).perform(click());
        }
        onView(withId(R.id.plus15button)).perform(click());
        onView(withId(R.id.plus60button)).check(matches(not(isEnabled())));
    }

    @Test
    public void isEndtimeGreaterStarttime() throws Exception{
        isStarttimeLowerEndtime();
        onView(withId(R.id.endTimeAlternative)).perform(click());
        onView(withId(R.id.minus15button)).check(matches(not(isEnabled())));
        onView(withId(R.id.minus30button)).check(matches(not(isEnabled())));
        onView(withId(R.id.minus60button)).check(matches(not(isEnabled())));
    }

}