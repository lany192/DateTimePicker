package com.lany.picker.samples;

import android.app.Application;

import com.lany.box.Box;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Box.of().init(this, BuildConfig.DEBUG);
    }
}
