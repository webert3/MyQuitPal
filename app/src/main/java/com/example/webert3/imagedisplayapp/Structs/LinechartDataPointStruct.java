package com.example.webert3.imagedisplayapp.Structs;

import android.util.Log;

/**
 * Created by ted on 7/13/16.
 */
public class LinechartDataPointStruct {
    public double stress, activity;
    public int session;
    public String time, date;
    public boolean isSmoking;
    public static final String TAG = "LinechartDataPointStruct";

    public LinechartDataPointStruct(boolean isSmoking, double stress, double activity, String date, String
            time, int session) {
        this.isSmoking = isSmoking;
        this.stress = stress;
        this.activity = activity;
        this.date = date;
        this.time = time;
        this.session = session;
    }
}
