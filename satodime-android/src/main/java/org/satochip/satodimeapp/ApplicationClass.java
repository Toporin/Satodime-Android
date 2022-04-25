package org.satochip.satodimeapp;
import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import io.paperdb.Paper;

public class ApplicationClass extends Application {
    private static final boolean DEBUG= BuildConfig.DEBUG;
    private static final String TAG = "SATODIME_APP";
    private String darkTheme;
    @Override
    public void onCreate() {
        super.onCreate();
        Paper.init(getApplicationContext());
        setupDarkTheme();
    }

    private void setupDarkTheme() {
        // using SharedPreferences
        darkTheme= Paper.book().read("darkmode","off");
        if(darkTheme.equals("on")){
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_YES);
            if (DEBUG) Log.d(TAG,"always on");
            Utils.isDark = true;
        }
        else if(darkTheme.equals("off")){
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_NO);
            if (DEBUG) Log.d(TAG,"always off");
            Utils.isDark = false;
        }
    }
}
