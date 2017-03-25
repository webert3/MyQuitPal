package com.example.webert3.imagedisplayapp.Activities.Analytics;

import com.example.webert3.imagedisplayapp.Activities.Compare.CompareInterfaceMenu;
import com.example.webert3.imagedisplayapp.Activities.Map.MapsActivity;
import com.example.webert3.imagedisplayapp.Activities.Overview.MyCalendar;
import com.example.webert3.imagedisplayapp.R;
import com.example.webert3.imagedisplayapp.Structs.CalendarUserDataStruct;
import com.example.webert3.imagedisplayapp.Structs.LinechartDataPointStruct;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ted on 8/15/16.
 */
public class AllDaysListView extends AppCompatActivity {
    public static final String TAG = "AllDaysListView";
    private ArrayList<String> dates;
    private ListView lv;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analytics_listview_all_days);
        setToolbar();

        lv = (ListView) findViewById(R.id.analytics_chart_listview);
        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(R.layout.listview_header, lv, false);
        lv.addHeaderView(header, null, false);

        // Load DayData into list
        dates = new ArrayList<>();
        ArrayList<LineData> list = new ArrayList<>();
        for (ArrayList<LinechartDataPointStruct> d: MyCalendar.allUserData) {
            LineData ld = DataByDay.loadDataSets(d, getApplicationContext());
            dates.add(d.get(0).date);
            list.add(ld);
        }

        ChartDataAdapter cda = new ChartDataAdapter(getApplicationContext(), list);
        lv.setAdapter(cda);
    }

    private void setToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.linechart_activity_toolbar);
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

    /**
     * ChartDataAdapter: Holds line charts that will appear in the list view.
     *
     * Modified from PhilJay's MPAndroid library
     * */
    private class ChartDataAdapter extends ArrayAdapter<LineData> {

        public ChartDataAdapter(Context context, List<LineData> objects) {
            super(context, 0, objects);
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LineData ld = getItem(position);
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.list_item_linechart, null);
                holder.chart = (LineChart) convertView.findViewById(R.id.linechart_item);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            setDateTextView(position, convertView, holder);
            configureChart(position, holder);

            YAxis yAxis = setAxes(holder);
            setLineLimits(position, yAxis);


            holder.chart.setData(ld);
            holder.chart.animateY(700);

            // Remove default legend, Add padding around charts
            holder.chart.getLegend().setEnabled(false);
            holder.chart.setViewPortOffsets(85f, 20f, 45f, 50f);

            // Refresh zoom factor when redrawing fragment.
            holder.chart.fitScreen();

            return convertView;
        }

        private void setLineLimits(int position, YAxis yAxis) {
            double avgStress = getAvgStress(dates.get(position));
            LimitLine baseline = new LimitLine((float) avgStress * 100,
                    "Your Average Stress");
            baseline.setLineWidth(2f);
            baseline.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
            baseline.setTextSize(12f);
            if (avgStress < 0.3) {
                baseline.setLineColor(R.color.md_blue_grey_800);
            } else if (avgStress < 0.6) {
                baseline.setLineColor(R.color.STRESS_0_6);
            } else {
                baseline.setLineColor(R.color.STRESS_0_9);
            }
            if (yAxis.getLimitLines().size() == 0) {
                yAxis.addLimitLine(baseline);
            }
        }

        @NonNull
        private YAxis setAxes(ViewHolder holder) {
            XAxis xAxis = holder.chart.getXAxis();
            xAxis.setDrawGridLines(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextSize(12f);
            xAxis.setValueFormatter(new MyXAxisValueFormatter());

            YAxis yAxis = holder.chart.getAxisLeft();
            holder.chart.getAxisRight().setEnabled(false);
            yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            yAxis.setDrawLabels(true);
            yAxis.setDrawLimitLinesBehindData(false);
            yAxis.setDrawGridLines(true);
            yAxis.setTextSize(15f);
            yAxis.setValueFormatter(new MyYAxisValueFormatter());
            return yAxis;
        }

        private void configureChart(int position, ViewHolder holder) {
            holder.chart.setDescription(dates.get(position));
            holder.chart.setDescriptionTextSize(15);
            holder.chart.setDescriptionTypeface(Typeface.DEFAULT_BOLD);
            holder.chart.setBackgroundColor(Color.WHITE);
            holder.chart.setNoDataTextDescription("You need to provide data for the chart.");
            holder.chart.setVisibleXRangeMaximum(15);
            holder.chart.setHighlightPerTapEnabled(false);
            holder.chart.setHighlightPerDragEnabled(false);
            holder.chart.setTouchEnabled(true);
            holder.chart.setDragEnabled(true);
            holder.chart.setPinchZoom(false);
            holder.chart.setScaleYEnabled(false);
            holder.chart.setScaleXEnabled(true);

            // On click troubleshooting
            holder.chart.setFocusable(false);
            holder.chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

                @Override
                public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                    Toast.makeText(getBaseContext(), "data " + e.getData().toString()
                            , Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected() { }
            });

        }

        private void setDateTextView(int position, View convertView, ViewHolder holder) {
            // set date display
            holder.currDate = (TextView) convertView.findViewById(R.id.linechart_date);
            String day = DataByDay.getSelectedDate(MyCalendar.calData.get
                    (position));
            day = day.split("-")[0];
            holder.currDate.setText(day);
            int session = MyCalendar.calData.get(position).session;
            if (session < 10) {
                holder.currDate.append(getString(R.string.prequit_header));
                holder.currDate.append(" "+Integer.toString(session));
            } else {
                holder.currDate.append(getString(R.string.postquit_header));
                holder.currDate.append(" "+Integer.toString(session - 10));
            }
        }

        private class ViewHolder {
            TextView currDate;
            LineChart chart;
        }
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
                return String.format("%.1f", (v / 100));
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
                i = new Intent(AllDaysListView.this, MyCalendar.class);
                break;
            case R.id.analytics_toolbar_icon:
                break;
            case R.id.compare_toolbar_icon:
                i = new Intent(AllDaysListView.this, CompareInterfaceMenu.class);
                break;
            case R.id.map_toolbar_icon:
                i = new Intent(AllDaysListView.this, MapsActivity.class);
                break;
        }
        if (i != null) {
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private double getAvgStress(String date) {
        double sAvg = 0.0;
        for (CalendarUserDataStruct c : MyCalendar.calData) {
            if (date.equals((MyCalendar.dateToFormattedString(c.javaDate)))) {
                sAvg = c.stressValue;
            }
        }
        return sAvg;
    }
}
