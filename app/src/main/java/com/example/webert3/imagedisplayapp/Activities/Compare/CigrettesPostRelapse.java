package com.example.webert3.imagedisplayapp.Activities.Compare;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.webert3.imagedisplayapp.Activities.Analytics.AllDaysListView;
import com.example.webert3.imagedisplayapp.Activities.Map.MapsActivity;
import com.example.webert3.imagedisplayapp.Activities.Overview.MyCalendar;
import com.example.webert3.imagedisplayapp.Helpers.CSVReader;
import com.example.webert3.imagedisplayapp.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ted on 9/27/16.
 */
public class CigrettesPostRelapse extends AppCompatActivity implements OnChartValueSelectedListener {
    public static final String TAG = "CigrettesPostRelapse";
    private BarChart mChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cigs_post_relapse);

        setToolbar();
        configureChart();
        configureAxes();

        // Load barchart data
        CSVReader csv = new CSVReader(getApplicationContext());
        mChart.setData(generateBarData(csv.loadNumSmoked()));
        mChart.highlightValue(2,0);
        mChart.animateXY(0, 2000);

    }

    private void configureAxes() {
        YAxis yAxis = mChart.getAxisLeft();
        mChart.getAxisRight().setEnabled(false);
        yAxis.setDrawLabels(true);
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setSpaceTop(15f);
        yAxis.setAxisMinValue(0f);
        yAxis.setTextSize(15);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinValue(-0.5f);
        xAxis.setTextSize(15);
    }

    private void configureChart() {
        mChart = (BarChart) findViewById(R.id.cigs_smoked_barchart);
        mChart.setDrawValueAboveBar(true);
        mChart.setPinchZoom(false);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setBackgroundColor(Color.LTGRAY);
        mChart.setDescription("");
        mChart.setHighlightPerTapEnabled(false);
        mChart.setTouchEnabled(false);
        Legend l = mChart.getLegend();
        l.setEnabled(false);
        mChart.setViewPortOffsets(30f, 20f, 45f, 40f);
    }

    private void setToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.num_cigs_toolbar);
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

    public BarData generateBarData(HashMap<Integer, ArrayList<Integer>> map) {
        ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
        int maxCigs = 0;
        for (Map.Entry<Integer, ArrayList<Integer>> entry : map.entrySet()) {
            ArrayList<Integer> users = entry.getValue();
            int y = users.size();
            int x = entry.getKey();
            if (x > maxCigs) {
                maxCigs = x;
            }
            yVals.add(new BarEntry(y, x - 1, users));
        }

        // styling
        MyBarDataSet set1 = new MyBarDataSet(yVals, "");
        //todo: Make dynamic, or find use a different color scheme at least.
        set1.setColors(new int[] {getResources().getColor(R.color.blue_bar),getResources()
                .getColor(R.color.darker_blue_bar)});
        set1.setDrawValues(false);
        set1.setHighlightEnabled(false);

        // load x-values (SUBJECT TO CHANGE)
        List<String> xVals = new ArrayList<>();
        for (int i = 1; i <= maxCigs; i++) {
            xVals.add(Integer.toString(i));
        }

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(xVals, dataSets);

        // styling
        data.setValueTextSize(10f);

        return data;
    }

    private class MyBarDataSet extends BarDataSet {

        public MyBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            ArrayList<Integer> users = (ArrayList<Integer>) getEntryForIndex(index).getData();
            int color;
            if (users.contains(6012)) {
                color = mColors.get(1);
            } else {
                color = mColors.get(0);
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
                i = new Intent(CigrettesPostRelapse.this, MyCalendar.class);
                break;
            case R.id.analytics_toolbar_icon:
                i = new Intent(CigrettesPostRelapse.this, AllDaysListView.class);
                break;
            case R.id.compare_toolbar_icon:
                i = new Intent(CigrettesPostRelapse.this, CompareInterfaceMenu.class);
                break;
            case R.id.map_toolbar_icon:
                i = new Intent(CigrettesPostRelapse.this, MapsActivity.class);
                break;
        }
        if (i != null) {
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onValueSelected(Entry entry, int i, Highlight highlight) {

    }

    @Override
    public void onNothingSelected() {

    }
}
