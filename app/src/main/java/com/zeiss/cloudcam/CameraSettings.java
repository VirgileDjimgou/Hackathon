package com.zeiss.cloudcam;

import android.content.SharedPreferences;

/**
 * Created by zoafro on 12.12.2016.
 */
public class CameraSettings {

    public int Iso;

    public boolean AutoFocus = true;

    public float FocusDistance;

    public long ExposureTime;

    public void load(SharedPreferences preferences) {
        Iso = preferences.getInt("Camera.Iso", 0);
        AutoFocus = preferences.getBoolean("Camera.AutoFocus", true);
        FocusDistance = preferences.getFloat("Camera.FocusDistance", 0.0f);
        ExposureTime = preferences.getLong("Camera.ExposureTime", 0L);
    }

    // shared preferences for the   camera  ..
    public void save(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("Camera.Iso", Iso);
        editor.putBoolean("Camera.AutoFocus", AutoFocus);
        editor.putFloat("Camera.FocusDistance", FocusDistance);
        editor.putLong("Camera.ExposureTime", ExposureTime);

        editor.commit();
    }

}
