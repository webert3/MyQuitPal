package com.example.webert3.imagedisplayapp.Structs;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by webert3 on 5/16/16.
 */
@SuppressWarnings("serial")
public class CalendarUserDataStruct implements Serializable {
    public double stressValue, activityValue;
    public int numSmokes;
    public String strDate;
    public Date javaDate;
    public int session;

    public CalendarUserDataStruct(String date, long timestamp, int session, double stressValue, double
            activityValue, int
            smokes) {
        this.session = session;
        this.stressValue = stressValue;
        this.activityValue = activityValue;
        this.strDate = date;
        this.numSmokes = smokes;
        this.javaDate = new Date(timestamp);
    }
}
