package com.futurice.android.reservator.common;


public abstract class LedHelper {

    private static LedHelper instance;

    public abstract void setGreenBrightness(int number);
    public abstract void setRedBrightness(int number);


    public static LedHelper getInstance() {
        if (instance == null)
            instance = new ProDvxLedHelper();
        return instance;
    }
}
