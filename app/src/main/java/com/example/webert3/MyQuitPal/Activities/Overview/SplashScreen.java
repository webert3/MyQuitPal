package com.example.webert3.MyQuitPal.Activities.Overview;

/**
 * Created by ted on 6/19/16.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.webert3.MyQuitPal.Helpers.LoadInBackground;
import com.example.webert3.MyQuitPal.R;

public class SplashScreen extends Activity {

    protected static LoadInBackground lib;
    private static int SPLASH_TIME_OUT = 3000;
    private static final String TAG = "SplashScreen";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, MyCalendar.class);
                startActivity(i);
                // Parse User Data in the background
                Log.v(TAG, "Loading user data and generating time strings from MyCalendar.java");
                parseUserData(getApplication(), 0);
                parseUserData(getApplication(), 1);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    public static void parseUserData(Context ctx, int arg) {
        lib = new LoadInBackground(ctx);
        lib.execute(arg);
    }
}