package com.example.webert3.imagedisplayapp.Helpers;

import android.content.Context;
import android.os.AsyncTask;

import com.example.webert3.imagedisplayapp.Activities.Overview.MyCalendar;

import java.util.ArrayList;

/**
 * Created by ted on 8/11/16.
 */
public class LoadInBackground extends AsyncTask<Integer, Integer, String> {
    private Context ctx;

    public LoadInBackground(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected String doInBackground(Integer... params) {
        String result = null;
        switch (params[0]) {
            case 0:
                CSVReader reader = new CSVReader(ctx);
                reader.loadAllUserData();
                result = "user data loaded";
                break;
            case 1:
                genTimeStrings();
                result = "generated time strings";
                break;
        }
        return result;
    }

    private static void genTimeStrings() {
        ArrayList<String> timeStrings = new ArrayList<>();
        String time, pf;
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                pf = "am";
            } else {
                pf = "pm";
            }
            for (int j= 0; j< 12; j++) {
                for (int k = 0; k < 60; k++) {
                    if (j== 0) {
                        time = "12:"+String.format("%02d", k)+pf;
                    } else {
                        time = String.format("%02d", j)+":"+String.format("%02d", k)+pf;
                    }
                    timeStrings.add(time);
                }
            }
        }
        MyCalendar.timeStrings = timeStrings;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {}

    @Override
    protected void onPostExecute(String result) {
        switch (result) {
            case "user data loaded":
                MyCalendar.dataLoaded = true;
                break;
            case "generated time strings":
                MyCalendar.timeStringsGenerated = true;
                break;
        }

        super.onPostExecute(result);
    }

}
