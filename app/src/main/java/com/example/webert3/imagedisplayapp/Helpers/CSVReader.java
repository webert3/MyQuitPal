package com.example.webert3.imagedisplayapp.Helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.example.webert3.imagedisplayapp.Activities.Overview.MyCalendar;
import com.example.webert3.imagedisplayapp.Structs.CalendarUserDataStruct;
import com.example.webert3.imagedisplayapp.Structs.LapseStruct;
import com.example.webert3.imagedisplayapp.Structs.LinechartDataPointStruct;
import com.example.webert3.imagedisplayapp.Structs.ScatterchartFirstLapseStruct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

import hirondelle.date4j.DateTime;

/**
 * Created by webert3 on 4/24/16.
 */
public class CSVReader {
    private static final String TAG = "CSVReader";
    Context ctx;

    public CSVReader(Context ctx) {
        this.ctx = ctx;
    }

    //region Loaders
    /**
     * Parse csv file containing all of the data for one user. Separates data by day.
     * Also calculates a baseline average stress for each user by day.
     * */
    public void loadAllUserData() {
        Collection<ArrayList<LinechartDataPointStruct>> allUserData = new ArrayList<ArrayList<LinechartDataPointStruct>>();
        ArrayList<LinechartDataPointStruct> dataDay = new ArrayList<LinechartDataPointStruct>();
        String[] sLine;
        String line, time, date;
        Double stress, activity;
        int session;
        boolean isSmoking;

        // Get input stream and Buffered Reader for our data file.
        AssetManager am = ctx.getAssets();
        Scanner r = null;

        try {
            InputStream is = am.open("6012_hrAvgData.csv");
            r = new Scanner(new InputStreamReader(is));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String prevDate = "";
        while (r.hasNextLine()) {
            line = r.nextLine();
            sLine = line.split(",");
            isSmoking = Boolean.parseBoolean(sLine[0]);
            stress = Double.parseDouble(sLine[1]);
            activity = Double.parseDouble(sLine[2]);
            date = sLine[3];
            time = sLine[4];
            session = Integer.parseInt(sLine[5]);

            // Check if it's a new data day, and add LinechartDataPointStruct if so.
            if (!prevDate.equals("") && (!date.equals(prevDate))) {
                allUserData.add(dataDay);
                dataDay = new ArrayList<>();
            }

            LinechartDataPointStruct rtd = new LinechartDataPointStruct(isSmoking, stress, activity, date, time,
                    session);
            dataDay.add(rtd);
            prevDate = date;
        }
        // Edge case for last day.
        allUserData.add(dataDay);

        MyCalendar.allUserData = allUserData;
    }

    /**
    * Creates a CalendarUserDataStruct object for each line of the csv file and puts into ArrayList rows.
    *
     * @return: rows. An ArrayList of CalendarUserDataStruct
    * */
    public ArrayList<CalendarUserDataStruct> loadCalendarUserData() {
        ArrayList<CalendarUserDataStruct> rows = new ArrayList<CalendarUserDataStruct>();
        try {
            // Get input stream and Buffered Reader for our data file.
            AssetManager am = ctx.getAssets();
            InputStream is = am.open("6012_calendarData.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            //Read each line
            try {
                while ((!(line = reader.readLine()).equals(""))) {
                    String[] RowData = line.split(",");
                    CalendarUserDataStruct cur = new CalendarUserDataStruct(RowData[0], Long.parseLong
                            (RowData[1]),
                            Integer.parseInt(RowData[2]), (Double.parseDouble(RowData[3])),
                            (Double.parseDouble(RowData[4])), (Integer.parseInt(RowData[5])));
                    rows.add(cur);
                }
            } catch (NullPointerException e) {
                Log.v(TAG, "EOF has been reached");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rows;
    }

    /**
     * Loads first lapse data for each participant who lapsed over the course of the experiment.
     *
     * @return: rows. An ArrayList of ScatterchartFirstLapseStruct objects.
     * */
    public ArrayList<ScatterchartFirstLapseStruct> loadFirstLapseData() {
        ArrayList<ScatterchartFirstLapseStruct> rows = new ArrayList<ScatterchartFirstLapseStruct>();
        try {
            // Get input stream and Buffered Reader for our data file.
            AssetManager am = ctx.getAssets();
            InputStream is = am.open("first-lapses_allUsers.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            //Read each line
            try {
                while ((!(line = reader.readLine()).equals(""))) {
                    String[] RowData = line.split(",");
                    ScatterchartFirstLapseStruct cur = new ScatterchartFirstLapseStruct(Integer.parseInt(RowData[0]), (Double.parseDouble(RowData[1])),
                            (Double.parseDouble(RowData[2])), (Long.parseLong(RowData[3])), (Integer.parseInt(RowData[4])));
                    rows.add(cur);
                }
            } catch (NullPointerException e) {
                Log.v(TAG, "EOF has been reached");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(rows);
        return rows;
    }

    /**
     * Parses 'num-smoked_each_lapser.csv'. Data used in the compare interface.
     *
     * @return: numSmoked. A HashMap containing the total number of
     * cigarettes smoked during the post quit phase as the key, and the value being the total
     * number of participants who smoked that many cigarettes during the post-quit phase.
     * */
    public HashMap<Integer, ArrayList<Integer>> loadNumSmoked() {
        HashMap<Integer, ArrayList<Integer>> numSmoked = new HashMap<>();

        try {
            AssetManager am = ctx.getAssets();
            InputStream is = am.open("num-smoked_each_lapser.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            try {
                while ((!(line = reader.readLine()).equals(""))) {
                    String[] data = line.split(",");
                    int numCigs = Integer.parseInt(data[1]);
                    int pId = Integer.parseInt(data[0]);
                    if (!numSmoked.containsKey(numCigs)) {
                        ArrayList<Integer> participants = new ArrayList<>();
                        participants.add(pId);
                        numSmoked.put(numCigs, participants);
                    } else {
                        ArrayList<Integer> participants = numSmoked.get(numCigs);
                        participants.add(pId);
                        numSmoked.put(numCigs, participants);
                    }
                }
            } catch (NullPointerException e) {
                Log.v(TAG, "EOF has been reached");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return numSmoked;
    }

    /**
     * Parses 'all_lapses_postQuit.csv'. Data used in the compare interface.
     *
     * @return: relativeLapses. A HashMap containing the relative hours past the corresponding
     * users first lapse, and an ArrayList of users who lapsed that many hours past their first
     * lapse.
     * */
    public HashMap<Integer, ArrayList<LapseStruct>> loadRelativeLapses() {
        HashMap<Integer, ArrayList<LapseStruct>> relativeLapses = new HashMap<>();

        try {
            AssetManager am = ctx.getAssets();
            InputStream is = am.open("all_lapses_post-quit.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            try {
                ArrayList<ArrayList<LapseStruct>> allLapses = new ArrayList<>();
                ArrayList<LapseStruct> userLapses = null;
                int prevId = 0;
                while ((!(line = reader.readLine()).equals("end"))) {
                    String[] data = line.split(",");
                    int pId = Integer.parseInt(data[0]);
                    String date = data[1];
                    double stress = Double.parseDouble(data[2]);
                    if (pId != prevId) {
                        if (prevId != 0) {
                            allLapses.add(userLapses);
                        }
                        userLapses = new ArrayList<>();
                    }
                    userLapses.add(new LapseStruct(pId, date, stress));
                    prevId = pId;
                }

                for (ArrayList<LapseStruct> ul : allLapses) {
                    String fl = ul.get(0).date;
                    DateTime startDate = new DateTime(fl);
                    for (LapseStruct ls : ul) {
                        DateTime currDate = new DateTime(ls.date);
                        if (startDate.compareTo(currDate) != 0) {
                            long secDiff = startDate.numSecondsFrom(currDate);
                            int hrDiff = (int) (secDiff - (secDiff % 3600))/3600;
                            if (!relativeLapses.containsKey(hrDiff)) {
                                ArrayList<LapseStruct> participants = new ArrayList<>();
                                participants.add(ls);
                                relativeLapses.put(hrDiff, participants);
                            } else {
                                ArrayList<LapseStruct> participants = relativeLapses.get(hrDiff);
                                participants.add(ls);
                                relativeLapses.put(hrDiff, participants);
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                Log.v(TAG, "EOF has been reached");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return relativeLapses;
    }

    //endregion
}

