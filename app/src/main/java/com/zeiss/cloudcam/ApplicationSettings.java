package com.zeiss.cloudcam;

import android.content.SharedPreferences;

/**
 * Created by zoafro on 12.12.2016.
 */

public class ApplicationSettings {

    private static ApplicationSettings instance;

    public CameraSettings CameraSettings;

    public static ApplicationSettings getInstance() {
        if (instance == null) {
            instance = new ApplicationSettings();
        }

        return instance;
    }

    private ApplicationSettings() {
        CameraSettings = new CameraSettings();
    }

    public void load(SharedPreferences preferences) {
        CameraSettings.load(preferences);
    }

    public void save(SharedPreferences preferences) {
        CameraSettings.save(preferences);
    }

}
