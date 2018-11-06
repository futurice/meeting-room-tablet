package com.futurice.android.reservator.common;

import android.util.Log;

import java.io.PrintWriter;
import java.io.File;

public class ProDvxLedHelper extends LedHelper {

    public ProDvxLedHelper() {
        super();
    }
    @Override
    public void setGreenBrightness(int number) {
        try {
            File file = new File("/sys/class/leds/led-front-green/brightness");
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.print(""+number);
            printWriter.close();
        } catch (Exception e) {
            Log.d("Reservator",e.toString());
        }
    }

    @Override
    public void setRedBrightness(int number) {
        try {
            File file = new File("/sys/class/leds/led-front-red/brightness");
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.print(""+number);
            printWriter.close();
        } catch (Exception e) {
            Log.d("Reservator",e.toString());
        }
    }
}
