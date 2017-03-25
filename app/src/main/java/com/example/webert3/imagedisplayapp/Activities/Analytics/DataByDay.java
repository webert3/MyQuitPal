package com.example.webert3.imagedisplayapp.Activities.Analytics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.webert3.imagedisplayapp.Activities.Compare.CompareInterfaceMenu;
import com.example.webert3.imagedisplayapp.Activities.Map.MapsActivity;
import com.example.webert3.imagedisplayapp.Activities.Overview.MyCalendar;
import com.example.webert3.imagedisplayapp.Fragments.AnalyticsMarkerView;
import com.example.webert3.imagedisplayapp.Helpers.EntryComparator;
import com.example.webert3.imagedisplayapp.R;
import com.example.webert3.imagedisplayapp.Structs.CalendarUserDataStruct;
import com.example.webert3.imagedisplayapp.Structs.LinechartDataPointStruct;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ted on 7/17/16.
 */

public class DataByDay extends AppCompatActivity implements OnChartValueSelectedListener {
    public static final String TAG = "DataByDay";
    private LineChart mChart;
    private CalendarUserDataStruct cud;
    private AnalyticsMarkerView mv;

    @SuppressLint("PrivateResource")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_day_linechart);

        // General component set up
        cud = (CalendarUserDataStruct) getIntent().getSerializableExtra("user_data_object");
        setToolbar();
        mv = new AnalyticsMarkerView(getApplicationContext(), R.layout
                .analytics_markerview);
        String dateStr = setHeaderTextView();
        configureChart(dateStr);
        YAxis yAxis = configureAxes();

        // Load data sets
        ArrayList<LinechartDataPointStruct> dataDay = findDataDay();
        LineData data = loadDataSets(dataDay, getApplicationContext());
        setLineLimits(yAxis);
        mChart.getLegend().setEnabled(false);
        mChart.setData(data);
        mChart.animateY(1500);
    }

    @NonNull
    private YAxis configureAxes() {
        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMaxValue(xAxis.getAxisMaximum());
        xAxis.setAxisMinValue(-0.01f);
        xAxis.setTextSize(12);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());

        YAxis yAxis = mChart.getAxisLeft();
        mChart.getAxisRight().setEnabled(false);
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setDrawLabels(true);
        yAxis.setDrawGridLines(true);
        yAxis.setDrawLimitLinesBehindData(false);
        yAxis.setTextSize(15f);
        yAxis.setAxisMinValue(-0.05f);
        yAxis.setValueFormatter(new MyYAxisValueFormatter());
        return yAxis;
    }

    private void setToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.single_linechart_activity_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Analytics");
        myToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private String setHeaderTextView() {
        TextView date = (TextView) findViewById(R.id.selected_date_analytics);
        String dayAndDate = getSelectedDate(cud);
        String day = dayAndDate.split("-")[0];
        String dateStr = dayAndDate.split("-")[1];
        date.setText(day);

        if (Build.VERSION.SDK_INT < 23) {
            date.setTextAppearance(getApplicationContext(), R.style.bold_italic);
        } else {
            date.setTextAppearance(R.style.bold_italic);
        }
        date.setTextColor(getResources().getColor(R.color.md_blue_grey_50));
        if (cud.session > 10) {
            date.append(getString(R.string.postquit_header));
            date.append(" "+Integer.toString(cud.session - 10));
        } else {
            date.append(getString(R.string.prequit_header));
            date.append(" "+Integer.toString(cud.session));
        }
        return dateStr;
    }

    private void setLineLimits(YAxis yAxis) {
        double avgStress = cud.stressValue; //todo: Calculate avg dynamically
        LimitLine baseline = new LimitLine((float) avgStress *100, "Your Average " +
                "Stress");
        baseline.setLineWidth(2f);
        baseline.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        baseline.setTextSize(12f);
        yAxis.addLimitLine(baseline);

        if (avgStress < 0.3) {
            baseline.setLineColor(R.color.md_blue_grey_800);
        } else if (avgStress < 0.6) {
            baseline.setLineColor(R.color.STRESS_0_6);
        } else {
            baseline.setLineColor(R.color.STRESS_0_9);
        }
        yAxis.addLimitLine(baseline);
    }

    @NonNull
    private void configureChart(String dateStr) {
        mChart = (LineChart) findViewById(R.id.linechart);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDescription(dateStr);
        mChart.setDescriptionTextSize(15);
        mChart.setDescriptionTypeface(Typeface.DEFAULT_BOLD);
        mChart.setViewPortOffsets(70f, 20f, 45f, 37f);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleXEnabled(true);
        mChart.setScaleYEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(false);
        mChart.setBackgroundColor(Color.WHITE);
    }

    public static String getSelectedDate(CalendarUserDataStruct cud) {
        String javaDateF = cud.javaDate.toString();
        String day = javaDateF.substring(0,3);
        String date = MyCalendar.dateToFormattedString(cud.javaDate);

        switch (day) {
            case "Sun":
                day = "Sunday";
                break;
            case "Mon":
                day = "Monday";
                break;
            case "Tue":
                day = "Tuesday";
                break;
            case "Wed":
                day = "Wednesday";
                break;
            case "Thu":
                day = "Thursday";
                break;
            case "Fri":
                day = "Friday";
                break;
            case "Sat":
                day = "Saturday";
                break;
        }
        javaDateF = day+" - "+date;
        return javaDateF;
    }

    /**
     * MyYAxisValueFormatter: Formats y-values (stress values) to two decimal points
     *
     * Modified from PhilJay's MPAndroid library
     * */
    private class MyYAxisValueFormatter implements YAxisValueFormatter {

        @Override
        public String getFormattedValue(float v, YAxis yAxis) {
            if (v != 0.0) {
                return String.format("%.2f", (v / 100));
            } else {
                return "";
            }
        }
    }

    private class MyXAxisValueFormatter implements XAxisValueFormatter {
        @Override
        public String getXValue(String s, int i, ViewPortHandler viewPortHandler) {
            if (s.substring(0,5).equals("Lapse")) {
                return "";
            }else if (s.substring(0,2).equals("0")) {
                return s.substring(1,7);
            } else {
                return s;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_buttons, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;
        switch (item.getItemId()) {
            case R.id.calendar_toolbar_icon:
                i = new Intent(DataByDay.this, MyCalendar.class);
                break;
            case R.id.analytics_toolbar_icon:
                i = new Intent(DataByDay.this, AllDaysListView.class);
                break;
            case R.id.compare_toolbar_icon:
                i = new Intent(DataByDay.this, CompareInterfaceMenu.class);
                break;
            case R.id.map_toolbar_icon:
                i = new Intent(DataByDay.this, MapsActivity.class);
                break;
        }
        if (i != null) {
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onValueSelected(Entry entry, int i, Highlight highlight) {
        mChart.setMarkerView(mv);
    }

    @Override
    public void onNothingSelected() {

    }

    /**
     * Prepares data sets for the lines plotted.
     * */
    public static LineData loadDataSets(ArrayList<LinechartDataPointStruct> dataDay, Context ctx) {
        ArrayList<Entry> stressEntries = new ArrayList<Entry>();
        Map<Integer, Integer> lapses = new HashMap<>();

        List<String> xVals;
        float y;

        xVals = genXValues(dataDay);

        for (LinechartDataPointStruct dps : dataDay) {
            if (!dps.isSmoking) {
                y = (float) Double.parseDouble(String.format("%1$,.2f", dps.stress));
                y = y*100; // Must be and int > 0 to be used as a y-value index
                String time = dps.time;
                int x = getXIndex(xVals, time);
                Entry e = new Entry(y, x);
                stressEntries.add(e);
            } else {
                String time = dps.time;
                // Set time string minutes to zero, to match on the appropriate x index
                time = "Lapse,"+time.substring(0,2)+":00"+time.substring(5,7);
                int x = getXIndex(xVals, time);
                Integer numSmoked = lapses.get(x);
                if (numSmoked == null) {
                    lapses.put(x, 1);
                } else {
                    lapses.put(x, (numSmoked + 1));
                }
            }
        }

        // Sort each dataset by x-value. This is a library fix to ensure all datapoints can be
        // selected via the onClickListener.
        Collections.sort(stressEntries, new EntryComparator());
        LineDataSet set1 = new LineDataSet(stressEntries, "");
        ArrayList<Entry> lapseEntries = getLapseDataPoints(lapses, set1);
        Collections.sort(lapseEntries, new EntryComparator());
        LineDataSet set2 = new LineDataSet(lapseEntries, "");

        ArrayList<ILineDataSet> dataSets = configureDataSets(ctx, set1, set2);
        LineData data = new LineData(xVals, dataSets);
        return data;
    }

    private static ArrayList<ILineDataSet> configureDataSets(Context ctx, LineDataSet set1, LineDataSet set2) {
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(true);
        set1.setDrawValues(false);
        set1.setHighLightColor(ctx.getResources().getColor(R.color.md_blue_grey_800));
        if (Utils.getSDKInt() >= 18) {
            Drawable drawable = ContextCompat.getDrawable(ctx, R.drawable.fade_blue);
            set1.setFillDrawable(drawable);
        }
        else {
            set1.setFillColor(Color.BLUE);
        }

        set2.setColor(ctx.getResources().getColor(R.color.transparent));
        set2.setCircleColor(ctx.getResources().getColor(R.color.STRESS_0_9));
        set2.setLineWidth(0f);
        set2.setDrawCircleHole(true);
        set2.setCircleRadius(10f);
        set2.setDrawValues(false);
        set2.setHighLightColor(ctx.getResources().getColor(R.color.md_blue_grey_800));

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        dataSets.add(set2);
        return dataSets;
    }

    private static ArrayList<Entry> getLapseDataPoints(Map<Integer, Integer> lapses, LineDataSet set1) {
        ArrayList<Entry> values = new ArrayList<Entry>();
        float rightP, leftP, avg;
        for (Integer xIndex : lapses.keySet()) {
            // Find average of data points to the right and left of x-index
            rightP = set1.getEntryForXIndex(xIndex+1).getVal();
            leftP = set1.getEntryForXIndex(xIndex-1).getVal();
            avg = (float) Math.round((rightP + leftP)/2f);
            // Add new entry, with numSmoked as the extra object data.
            values.add(new Entry(avg, xIndex, lapses.get(xIndex)));
        }
        return values;
    }

    public static int getXIndex(List<String> xVals, String time) {
        int x = 0;
        for (String xTime : xVals) {
            if (time.equals(xTime)) {
                break;
            }
            x++;
        }
        return x;
    }

    public static List<String> genXValues(ArrayList<LinechartDataPointStruct> data) {
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<String> timeStrings = MyCalendar.timeStrings;
        String minTime = data.get(0).time;
        String maxTime = data.get(data.size() - 1).time;
        int minIndex = MyCalendar.timeStrings.indexOf(minTime);
        int maxIndex = MyCalendar.timeStrings.indexOf(maxTime);

        // Iterate through timeStrings to acquire necessary x-values
        int i = minIndex;
        int tsMax = timeStrings.size()-1;
        String curr = timeStrings.get(i);
        while (i <= maxIndex) {
            if (timeStrings.get(i).substring(3,5).equals("00")) {
                xVals.add(timeStrings.get(i));
                if (i+1 < maxIndex) {
                    xVals.add("Lapse,"+timeStrings.get(i));
                }
            }
            i++;
            if (i > tsMax) {
                i = 0;
            }
        }
        return xVals;
    }

    private ArrayList<LinechartDataPointStruct> findDataDay() {
        ArrayList<LinechartDataPointStruct> dayData = new ArrayList<>();

        for (ArrayList<LinechartDataPointStruct> arr: MyCalendar.allUserData) {
            if ((arr.get(0).date).equals
                    ((MyCalendar.dateToFormattedString(cud.javaDate)))) {
                dayData = arr;
                break;
            }
        }

        return dayData;
    }
}