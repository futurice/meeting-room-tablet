package com.futurice.android.reservator;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {

    @Rule
    public ActivityTestRule<SettingsActivity> mActivityRule = new ActivityTestRule<>(SettingsActivity.class);
    private Context context;

    @Before
    public void setUp(){
        context = mActivityRule.getActivity().getBaseContext();
    }

    @Test
    public void checkSelectetLanguageSpinner() {
        Locale locale = mActivityRule.getActivity().getResources().getConfiguration().locale;
        String testLanguage;

        if (locale.toString().contains("de")){
            testLanguage = "Deutsch";
        } else {
            testLanguage = "english";
        }

        onView(withId(R.id.languageSpinner)).check(matches(withSpinnerText(testLanguage)));
    }

    @Test
    public void isEnglishLocale() {
        onView(withId(R.id.languageSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(1).perform(click());
        onView(withId(R.id.language)).check(matches(withText("Language:")));
    }

    @Test
    public void isGermanLocale() {
        onView(withId(R.id.languageSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(0).perform(click());
        onView(withId(R.id.language)).check(matches(withText("Sprache:")));
    }

    @Test
    public void isMeetingDesignationPersonName() {
        onView(withId(R.id.meetingDesignationSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(0).perform(click());
        onView(withId(R.id.meetingDesignationSpinner)).check(matches(withSpinnerText(context.getString(R.string.meetingTitlePersonName))));
    }

    @Test
    public void isMeetingDesignationMeetingName() {
        onView(withId(R.id.meetingDesignationSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(1).perform(click());
        onView(withId(R.id.meetingDesignationSpinner)).check(matches(withSpinnerText(context.getString(R.string.meetingTitleMeetingName))));
    }


}