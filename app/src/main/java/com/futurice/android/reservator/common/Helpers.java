package com.futurice.android.reservator.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.util.Log;

import com.futurice.android.reservator.model.DateTime;

public class Helpers {
    /**
     * Read all from InputStream
     *
     * @param is   Stream to read from
     * @param size Guess the size of InputStream contents, give negative for the automatic
     * @return
     */
    public static String readFromInputStream(InputStream is, int size) {
        try {
            ByteArrayOutputStream os;
            if (size <= 0) {
                os = new ByteArrayOutputStream();
            } else {
                os = new ByteArrayOutputStream(size);
            }
            byte buffer[] = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            return os.toString("UTF-8");
        } catch (IOException e) {
            Log.e("SOAP", "readFromInputStream", e);
            return "";
        }
    }

    // TODO: an hour and a half - and other common expressions
    public static String humanizeTimeSpan(int minutes) {
        if (minutes < 30) {
            return Integer.toString(minutes) + " minutes";
        } else if (minutes < 45) {
            return "half an hour";
        } else if (minutes < 60) {
            return "45 minutes";
        } else if (minutes < 85) {
            return "an hour";
        } else if (minutes < 110) {
            return "an hour and a half";
        } else if (minutes < 24 * 60) {
            return Integer.toString((minutes + 10) / 60) + " hours";
        } else {
            return Integer.toString(minutes / (60 * 24)) + " days";
        }
    }

    // For use in traffic lights
    public static String humanizeTimeSpan2(int minutes) {
        int hours = minutes / 60;

        if (minutes < 15) {
            return getUnits(minutes, "minute", "minutes");
        } else if (minutes < 30) {
            return getUnits(roundTo(minutes, 5), "minute", "minutes");
        } else if (minutes < 60) {
            return getUnits(roundTo(minutes, 15), "minute", "minutes");
        } else if (minutes < 60 * 4) {
            int hourMins = roundTo(minutes - hours * 60, 15);
            if (hourMins == 60) {
                hours++;
                hourMins = 0;
            }

            if (hourMins == 0) {
                return getUnits(hours, "hour", "hours");
            } else {
                return String.format(Locale.getDefault(), "%dh:%02dmin", hours, hourMins);
            }
        } else if (minutes < 24 * 60) {
            return getUnits(hours, "hour", "hours");
        } else {
            return getUnits(hours / 24, "day", "days");
        }
    }

    public static String dateTimeTo24h(DateTime dt) {
        java.util.Date date = new java.util.Date(dt.getTimeInMillis());
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }


    public static String convertToHoursAndMinutes(int min) {
        int hours = min / 60;
        int minutes = min%60;
        if (hours > 0)
            return hours+"h "+minutes+"min";
        else
            return minutes+"min";
    }

    private static String getUnits(int amount, String unitName, String unitNamePlural) {
        if (amount == 1) return String.format("1 %s", unitName);
        return String.format(Locale.getDefault(), "%d %s", amount, unitNamePlural);
    }

    private static int roundTo(int in, int precision) {
        return precision * ((int) Math.floor(in / (1.0 * precision)));
    }
}
