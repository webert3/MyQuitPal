package com.example.webert3.MyQuitPal.Activities.Compare;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.webert3.MyQuitPal.Activities.Analytics.AllDaysListView;
import com.example.webert3.MyQuitPal.Activities.Map.MapsActivity;
import com.example.webert3.MyQuitPal.Activities.Overview.MyCalendar;
import com.example.webert3.MyQuitPal.Fragments.RelativeRelapseMarkerView;
import com.example.webert3.MyQuitPal.Helpers.CSVReader;
import com.example.webert3.MyQuitPal.Helpers.EntryComparator;
import com.example.webert3.MyQuitPal.R;
import com.example.webert3.MyQuitPal.Structs.LapseStruct;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by webert3 on 10/5/16.
 */
public class RelativeRelapses extends AppCompatActivity implements OnChartValueSelectedListener {
    public static final String TAG = "CigrettesPostRelapse";
    private ScatterChart mChart;
    private RelativeRelapseMarkerView mv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.relative_relapses_scatterchart);
        mv = new RelativeRelapseMarkerView(getApplicationContext(), R.layout.analytics_markerview);
        setToolbar();

        // Load data
        CSVReader csv = new CSVReader(getApplicationContext());
        HashMap<Integer, ArrayList<LapseStruct>> relativeLapses = csv.loadRelativeLapses();

        configureChart();
        setUpAxes();

        ScatterData sd = generateData(relativeLapses);
        mChart.setData(sd);
        mChart.animateXY(2000, 2000);
        mChart.getLegend().setEnabled(false);

    }

    private void setUpAxes() {
        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawGridLines(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinValue((float)-1);
        xAxis.setTextSize(15);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setTextSize(12);
        yAxis.setValueFormatter(new MyYAxisValueFormatter());
    }

    private void configureChart() {
        mChart = (ScatterChart) findViewById(R.id.relative_relapses_scatterchart);
        mChart.setDescription("");
        mChart.setOnChartValueSelectedListener(this);
        mChart.getAxisRight().setEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setTouchEnabled(true);
        mChart.setBackgroundColor(Color.LTGRAY);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setMaxVisibleValueCount(200);
        mChart.setPinchZoom(true);
    }

    private void setToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.relative_relapses_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Compare");
        myToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * This class puts the y-values (time in minutes) into Standard AM/PM time.
     * */
    private class MyYAxisValueFormatter implements YAxisValueFormatter {

        public String minutesToStandardTime(float index) {
            String date;
            float mins, hrs;
            mins = index % 60;
            hrs = (index - mins)/60;
            if (hrs < 12) {
                if (hrs == 0) {
                    hrs = 12;
                }
                date = String.format("%02d", (int) hrs) + ":" + String.format("%02d", (int) mins)
                        + " AM";
            } else {
                float pmHr = (hrs - 12);
                if (pmHr == 0) {
                    pmHr = 12;
                }
                date = String.format("%02d", (int) pmHr) + ":" + String.format("%02d", (int)
                        mins) + " PM";
            }
            return date;
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            String date = minutesToStandardTime(value);
            return date;
        }
    }

    /**
     * MyXAxisValueFormatter: Formats x-values (relative hours past first lapse) to two decimal
     * points
     *
     * Modified from PhilJay's MPAndroid library
     * */
    private class MyXAxisValueFormatter implements XAxisValueFormatter {
        @Override
        public String getXValue(String s, int i, ViewPortHandler viewPortHandler) {
            s = s + " hrs";
            return s;
        }
    }

    public ScatterData generateData(HashMap<Integer, ArrayList<LapseStruct>> relativeLapses) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<Entry> currUserEntries = new ArrayList<>();
        int hrMax = 0;
        for (Map.Entry<Integer, ArrayList<LapseStruct>> entry : relativeLapses.entrySet()) {
            int xHour = entry.getKey();
            if (xHour > hrMax) {
                hrMax = xHour;
            }
            ArrayList<LapseStruct> lapses = entry.getValue();
            for (LapseStruct ls : lapses) {
                // Trying with time on y-axis
                int yMinutes = FirstLapseScatterChart.dateToMinutes(ls.date.substring(11,16));
                if (ls.pId == 6012) {
                    currUserEntries.add(new Entry(yMinutes, xHour - 1, ls));
                } else {
                    entries.add(new Entry(yMinutes, xHour - 1, ls));
                }
            }
        }

        ArrayList<IScatterDataSet> dataSets = configureDatasets(entries, currUserEntries);
        ArrayList<String> xVals = genXValues(hrMax);
        ScatterData data = new ScatterData(xVals, dataSets);
        return data;
    }

    @NonNull
    private ArrayList<IScatterDataSet> configureDatasets(ArrayList<Entry> entries,
                                                         ArrayList<Entry> currUserEntries) {
        Collections.sort(entries, new EntryComparator());
        Collections.sort(currUserEntries, new EntryComparator());


        MyScatterDataSet set1 = new MyScatterDataSet(entries, "");
        set1.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        set1.setScatterShapeSize(25);
        set1.setDrawValues(false);
        set1.setHighLightColor(getResources().getColor(R.color.md_blue_grey_800));
        set1.setColors(new int[]{getResources().getColor(R.color.opaque_yellow), getResources()
                .getColor
                (R.color.opaque_orange), getResources()
                .getColor
                (R.color.opaque_red)});

        MyScatterDataSet set2 = new MyScatterDataSet(currUserEntries, "");
        set2.setScatterShape(ScatterChart.ScatterShape.TRIANGLE);
        set2.setScatterShapeSize(25);
        set2.setDrawValues(false);
        set2.setHighLightColor(getResources().getColor(R.color.md_blue_grey_800));
        set2.setColors(new int[]{getResources().getColor(R.color.less_opaque_yellow), getResources()
                .getColor
                (R.color.less_opaque_orange), getResources()
                .getColor
                (R.color.less_opaque_red)});

        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        dataSets.add(set2);
        return dataSets;
    }

    @NonNull
    private ArrayList<String> genXValues(int hrMax) {
        ArrayList<String> xVals = new  ArrayList<>();
        for (int i = 0; i < hrMax + 2; i++) {
            xVals.add(Integer.toString(i));
        }
        return xVals;
    }

    public class MyScatterDataSet extends ScatterDataSet {

        public MyScatterDataSet(List<Entry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            LapseStruct ls = (LapseStruct) getEntryForIndex(index).getData();

            double stress = ls.stress;
            int color;
            if (stress < 0.3) {
                 color = mColors.get(0);
            } else if (stress < 0.6) {
                color = mColors.get(1);
            } else {
                color = mColors.get(2);
            }
            return color;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;
        switch (item.getItemId()) {
            case R.id.calendar_toolbar_icon:
                i = new Intent(RelativeRelapses.this, MyCalendar.class);
                break;
            case R.id.analytics_toolbar_icon:
                i = new Intent(RelativeRelapses.this, AllDaysListView.class);
                break;
            case R.id.compare_toolbar_icon:
                i = new Intent(RelativeRelapses.this, CompareInterfaceMenu.class);
                break;
            case R.id.map_toolbar_icon:
                i = new Intent(RelativeRelapses.this, MapsActivity.class);
                break;
        }
        if (i != null) {
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onValueSelected(Entry e, int i, Highlight h) {
        mChart.setMarkerView(mv);
    }

    @Override
    public void onNothingSelected() {

    }
}