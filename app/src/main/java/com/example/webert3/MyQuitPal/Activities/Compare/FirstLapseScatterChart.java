package com.example.webert3.MyQuitPal.Activities.Compare;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.webert3.MyQuitPal.Activities.Analytics.AllDaysListView;
import com.example.webert3.MyQuitPal.Activities.Map.MapsActivity;
import com.example.webert3.MyQuitPal.Activities.Overview.MyCalendar;
import com.example.webert3.MyQuitPal.Fragments.FirstLapseMarkerView;
import com.example.webert3.MyQuitPal.Helpers.CSVReader;
import com.example.webert3.MyQuitPal.Helpers.EntryComparator;
import com.example.webert3.MyQuitPal.R;
import com.example.webert3.MyQuitPal.Structs.ScatterchartFirstLapseStruct;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ted on 7/7/16.
 *
 * Visualization that displays the time and relative day of each participant's first lapse.
 *
 */
public class FirstLapseScatterChart extends AppCompatActivity implements OnChartValueSelectedListener {
    private static final String TAG = "FirstLapseScatterChart";
    private ScatterChart mChart;
    private CSVReader reader;
    private ArrayList<ScatterchartFirstLapseStruct> rows;
    private FirstLapseMarkerView mv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_lapse_scatterchart);
        setToolbar();

        //Get csv rows
        reader = new CSVReader(getApplicationContext());
        rows = reader.loadFirstLapseData();

        // Gen Data and configure chart.
        configureChart();
        setXAxis();
        ScatterData sd = generateData(rows);
        setYAxis(sd);

        mChart.setData(sd);
        highlightCurrUser(sd);
        mChart.animateXY(2000, 2000);

        setLegend();
        mChart.invalidate();
    }

    private void setXAxis() {
        XAxis xl = mChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawGridLines(false);
        xl.setAxisMinValue((float)-0.25);
        xl.setTextSize(15);
    }

    private void setYAxis(ScatterData sd) {
        YAxis yl = mChart.getAxisLeft();
        yl.setAxisMinValue(sd.getYMin() - 50);
        yl.setAxisMaxValue(sd.getYMax() + 100);
        yl.setTextSize(12);
        yl.setValueFormatter(new MyYAxisValueFormatter());
    }

    private void setLegend() {
        Legend lgnd = mChart.getLegend();
        lgnd.setTextSize(15f);
        lgnd.setXOffset(-30);
        lgnd.setYOffset(10);
        lgnd.setFormToTextSpace(5);
        lgnd.setXEntrySpace(15);
    }

    private void configureChart() {
        mChart = (ScatterChart) findViewById(R.id.first_lapse_scatter);
        mChart.setDescription("");
        mChart.setOnChartValueSelectedListener(this);
        mChart.getAxisRight().setEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setTouchEnabled(true);
        mChart.setBackgroundColor(Color.LTGRAY);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setMaxVisibleValueCount(200);
        mChart.setPinchZoom(false);
        // Grab toolbar and textview heights to calculate markerview offset.
        int tbHeight = findViewById(R.id.first_lapse_toolbar).getHeight();
        int tvHeight = findViewById(R.id.first_lapse_title).getHeight();
        mv = new FirstLapseMarkerView(getApplicationContext(), R.layout.analytics_markerview,
                tbHeight, tvHeight);
    }

    private void setToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.first_lapse_toolbar);
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
                i = new Intent(FirstLapseScatterChart.this, MyCalendar.class);
                break;
            case R.id.analytics_toolbar_icon:
                i = new Intent(FirstLapseScatterChart.this, AllDaysListView.class);
                break;
            case R.id.compare_toolbar_icon:
                i = new Intent(FirstLapseScatterChart.this, CompareInterfaceMenu.class);
                break;
            case R.id.map_toolbar_icon:
                i = new Intent(FirstLapseScatterChart.this, MapsActivity.class);
                break;
        }
        if (i != null) {
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    public ScatterData generateData(ArrayList<ScatterchartFirstLapseStruct> rows) {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();
        ArrayList<Entry> yVals3 = new ArrayList<Entry>();
        // Load csv data into dataSets.
        for (ScatterchartFirstLapseStruct d : rows) {
            int day = d.day;
            String xString = d.time_formatted;

            float y = dateToMinutes(xString);
            double stress = d.stress;
            // todo: Use stress thresholds defined by the Minnesota Group
            Entry e = new Entry(y, day - 1, d);
            if (stress < 0.1) {
                yVals1.add(e);
            } else if (stress < 0.4) {
                yVals2.add(e);
            } else {
                yVals3.add(e);
            }
        }

        ArrayList<IScatterDataSet> dataSets = configureDatasets(yVals1, yVals2, yVals3);

        String[] xVals = new String[] {"Day 1", "Day 2", "Day 3"};

        ScatterData data = new ScatterData(xVals, dataSets);
        return data;
    }

    @NonNull
    private ArrayList<IScatterDataSet> configureDatasets(ArrayList<Entry> yVals1,
                                                         ArrayList<Entry> yVals2,
                                                         ArrayList<Entry> yVals3) {
        Collections.sort(yVals1, new EntryComparator());
        Collections.sort(yVals2, new EntryComparator());
        Collections.sort(yVals3, new EntryComparator());

        ScatterDataSet set1 = new ScatterDataSet(yVals1, "Low Stress");
        set1.setColor((getResources().getColor(R.color.STRESS_0_5)), 130);
        set1.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        set1.setScatterShapeSize(30);
        set1.setDrawValues(false);
        set1.setHighLightColor(getResources().getColor(R.color.md_blue_grey_800));

        ScatterDataSet set2 = new ScatterDataSet(yVals2, "Moderate Stress");
        set2.setColor((getResources().getColor(R.color.STRESS_0_8)), 130);
        set2.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        set2.setScatterShapeSize(30);
        set2.setDrawValues(false);
        set2.setHighLightColor(getResources().getColor(R.color.md_blue_grey_800));

        ScatterDataSet set3 = new ScatterDataSet(yVals3, "High Stress");
        set3.setColor((getResources().getColor(R.color.colorAccent)), 130);
        set3.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        set3.setScatterShapeSize(30);
        set3.setDrawValues(false);
        set3.setHighLightColor(getResources().getColor(R.color.md_blue_grey_800));

        ArrayList<IScatterDataSet> dataSets = new ArrayList<IScatterDataSet>();
        dataSets.add(set1); // add the datasets
        dataSets.add(set2);
        dataSets.add(set3);
        return dataSets;
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

    private void highlightCurrUser(ScatterData sd) {
        List<IScatterDataSet> sets = sd.getDataSets();
        int setsLength = sets.size();
        for (int i = 0; i < setsLength; i++) {
            IScatterDataSet sds = sets.get(i);
            int setLength = sets.size();
            for (int j = 0; j < setLength; j++) {
                Entry e = sds.getEntryForIndex(j);
                ScatterchartFirstLapseStruct fs = (ScatterchartFirstLapseStruct) e.getData();
                if (fs.id == 6012) {
                    mChart.setMarkerView(mv);
                    mChart.highlightValue(e.getXIndex(), i);
                }
            }
        }
    }

    /**
    * Converts formatted strDate to int to work with MPAndroid Entry class.
     *
     * MPAndroid indexes data points based on floats and integers. Thus, I have to
     * convert x and y-values to this format to get data points placed appropriately
     * in relation to each other.
     *
     * @param date: Formatted strDate string representing the time of the first lapse.
     * @return x: Integer index based on time in minutes.
    * */
    public static int dateToMinutes(String date) {
        int x = 0;
        int hrs = Integer.parseInt(date.substring(0,2));
        x += hrs * 60;
        int mins = Integer.parseInt(date.substring(3,5));
        x += mins;
        return x;
    }

    @Override
    public void onValueSelected(Entry e, int i, Highlight h) {
        mChart.setMarkerView(mv);
        Log.i("VAL SELECTED",
                "\nParticipant: "+e.getData().toString()+ "\nValue: " + e.getVal() + ", xIndex: " + e.getXIndex()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {

    }

}


