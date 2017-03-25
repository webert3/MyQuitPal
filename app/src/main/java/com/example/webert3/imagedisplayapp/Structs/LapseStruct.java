package com.example.webert3.imagedisplayapp.Structs;

/**
 * Created by webert3 on 10/5/16.
 */
public class LapseStruct {
    public int pId;
    public String date;
    public double stress;

    public LapseStruct(int pId, String date, double stress) {
        this.pId = pId;
        this.date = date;
        this.stress = stress;
    }
}
