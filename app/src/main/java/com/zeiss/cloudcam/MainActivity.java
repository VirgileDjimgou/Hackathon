package com.zeiss.cloudcam;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    public static MainActivity Instance;

    public MainActivity() {
        Instance = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new CaptureFragment())
                    .commit();
        }

        loadApplicationSettings();
        notify("onCreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
        notify("onPause");

        saveApplicationSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notify("onResume");

        loadApplicationSettings();
    }

    @Override
    protected void onStop() {
        super.onStop();
        notify("onStop");

        saveApplicationSettings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notify("onDestroy");

        saveApplicationSettings();

        //LedModule.disconnect();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        notify("onRestoreInstanceState");

        loadApplicationSettings();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        notify("onSaveInstanceState");

        saveApplicationSettings();
    }

    public void loadApplicationSettings() {
        Log.d(TAG, "Loading application settings");
        ApplicationSettings.getInstance().load(getSharedPreferences("settings", 0));
    }

    public void saveApplicationSettings() {
        Log.d(TAG, "Saving application settings");
        ApplicationSettings.getInstance().save(getSharedPreferences("settings", 0));
    }

    public void toast(String message) {
        this.toast(message, false);
    }

    public void toast(final String message, final boolean longLength) {
        final Activity activity = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notify(String methodName) {
        String name = this.getClass().getName();
        String[] strings = name.split("\\.");
        Log.d(TAG, methodName + " " + strings[strings.length - 1]);

        /*Notification noti = new Notification.Builder(this)
                .setContentTitle(methodName + " " + strings[strings.length - 1]).setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText(name).build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), noti);*/
    }
}
