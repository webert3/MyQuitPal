package com.example.webert3.MyQuitPal.Structs;

import android.annotation.TargetApi;
import android.os.Build;

import java.util.Date;

/**
 * Created by ted on 7/3/16.
 */
public class ScatterchartFirstLapseStruct implements Comparable<ScatterchartFirstLapseStruct> {
    public int id;
    public double activity;
    public double stress;
    public long timestamp;
    public Date date;
    public int day;
    public int session;
    public String time_formatted;
    public static final String TAG = "ScatterchartFirstLapseStruct";

    public ScatterchartFirstLapseStruct(int id, double stress, double activity, long timestamp, int session) {
        this.id = id;
        this.activity = activity;
        this.stress = stress;
        this.timestamp = timestamp;
        Date date = new Date(timestamp);
        this.date = date;
        this.day = session - 10;
        this.session = session;
        this.time_formatted = date.toString().substring(11, 19);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int compareTo(ScatterchartFirstLapseStruct another) {
        return Integer.compare(this.session, another.session);
    }

    @Override
    public String toString() {
        return "ScatterchartFirstLapseStruct{" +
                "id=" + id +
                ", activity=" + activity +
                ", stress=" + stress +
                ", timestamp=" + timestamp +
                ", strDate=" + date +
                ", day=" + day +
                ", session=" + session +
                '}';
    }
}
