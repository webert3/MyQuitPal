package com.example.webert3.MyQuitPal.Activities.Overview;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webert3.MyQuitPal.Activities.Analytics.AllDaysListView;
import com.example.webert3.MyQuitPal.Activities.Compare.CompareInterfaceMenu;
import com.example.webert3.MyQuitPal.Activities.Analytics.DataByDay;
import com.example.webert3.MyQuitPal.Activities.Map.MapsActivity;
import com.example.webert3.MyQuitPal.Helpers.AnalyticsApplication;
import com.example.webert3.MyQuitPal.Helpers.CSVReader;
import com.example.webert3.MyQuitPal.Fragments.CaldroidCustomFragment;
import com.example.webert3.MyQuitPal.R;
import com.example.webert3.MyQuitPal.Structs.CalendarUserDataStruct;
import com.example.webert3.MyQuitPal.Structs.LinechartDataPointStruct;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import hirondelle.date4j.DateTime;

public class MyCalendar extends AppCompatActivity {

    public Tracker mTracker;
    public CaldroidCustomFragment caldroidFragment;
    public static ArrayList<CalendarUserDataStruct> calData;
    public static Collection<ArrayList<LinechartDataPointStruct>> allUserData;
    public static ArrayList<String> timeStrings;
    public static boolean dataLoaded = false, timeStringsGenerated = false;

    protected Toast toast;

    private static final String TAG = "MyCalendar",  CURRENT_DATE = "12/19/2014";
    private static final Date currDate = new Date(Long.parseLong("1418976000000"));
    private TextView daysClean, dayStats, dateToday;
    private CSVReader reader;
    private Intent i;
    private Date prevDateSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_acitivty);
        setToolbar();
        setTextViews();

        // Obtain the shared Tracker instance.
        mTracker = ((AnalyticsApplication) getApplication()).getDefaultTracker();

        // Check if AsyncTask is loading the by-day data.
        if (SplashScreen.lib == null) {
            Log.v(TAG, "Loading user data and generating time strings from MyCalendar.java");
            SplashScreen.parseUserData(getApplication(), 0);
            SplashScreen.parseUserData(getApplication(), 1);
        }

        // Setup My Calendar
        loadCalendar();
    }

    private void setTextViews() {
        dateToday = (TextView) findViewById(R.id.date_today_ID);
        dayStats = (TextView) findViewById(R.id.dayStats_ID);
        dateToday.setText(getString(R.string.TODAY_DATE, CURRENT_DATE));
        dayStats.setText("• ");
        dayStats.append(getString(R.string.ON_TOUCH_INSTRUCTIONS));
        dayStats.append("\n• ");
        dayStats.append(getString(R.string.ON_HOLD_INSTRUCTIONS));
    }

    private void setToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("myCalendar");
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
                break;
            case R.id.analytics_toolbar_icon:
                i = new Intent(MyCalendar.this, AllDaysListView.class);
                break;
            case R.id.compare_toolbar_icon:
                i = new Intent(MyCalendar.this, CompareInterfaceMenu.class);
                break;
            case R.id.map_toolbar_icon:
                i = new Intent(MyCalendar.this, MapsActivity.class);
                break;
        }
        if (i != null) {
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles the creation and population of 'My Calendar'
     */
    public void loadCalendar() {
        reader = new CSVReader(getApplicationContext());
        caldroidFragment = new CaldroidCustomFragment();
        Bundle args = new Bundle();
        setCaldroidFragment(args);

        // Get current user data
        calData = reader.loadCalendarUserData();
        setDaysCleanTextview();

        // Parse stress data to determine calendar call color for smoking days.
        parseStressData();

        final CaldroidListener listener = new CaldroidListener() {
            CalendarUserDataStruct cud;

            @Override
            public void onLongClickDate(Date date, View view) {
                mTracker = ((AnalyticsApplication) getApplication()).getDefaultTracker();
                Intent i = new Intent(MyCalendar.this, DataByDay.class);
                cud = getDataByDate(date);

                // Cancel any active toast.
                if (toast != null) {
                    toast.cancel();
                }

                if (cud != null) {
                    i.putExtra("user_data_object", cud);

                    // Send hit to google analytics
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("Calendar " +
                            "Interaction").setAction("User chose day:"+cud.strDate).setLabel(String
                            .valueOf(cud.strDate))
                            .build());
                    // Check if data has been loaded in background task.
                    if (!(dataLoaded && timeStringsGenerated)) {
                        toast = Toast.makeText(getBaseContext(), "User data is still loading..."
                                , Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        startActivity(i);
                    }
                } else {
                    toast = Toast.makeText(getBaseContext(), "There is no data for the selected" +
                            " date!", Toast.LENGTH_SHORT);
                    toast.show();
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("Calendar " +
                            "Interaction").setAction("Chose Day With No Data").setLabel(date
                            .toString()).build());
                }
            }

            @Override
            public void onSelectDate(Date date, View view) {
                super.onLongClickDate(date, view);
                String dateFormatted = dateToFormattedString(date);
                cud = getDataByDate(date);

                highlightDateCell(date);
                displayCurrentDayText(dateFormatted);
            }

            private void displayCurrentDayText(String dateFormatted) {
                // Hard-coded the current strDate for the prototype...
                if (dateFormatted.equals(CURRENT_DATE)) {
                    dateToday.setText("");
                    dayStats.setText("");
                    dateToday.setText(getString(R.string.TODAY_DATE, dateFormatted));
                    dayStats.setText("\u2022 ");
                    dayStats.append(getString(R.string.HOLD_TODAY));
                } else {
                    if (cud != null) {
                        setDayStats(cud);
                    } else {
                        setNoDataDay(dateFormatted);
                    }
                }
            }
        };
        caldroidFragment.setCaldroidListener(listener);
    }

    private void highlightDateCell(Date date) {
        if (prevDateSelected != null && date.compareTo(prevDateSelected) != 0) {
            if (getDataByDate(prevDateSelected) == null && !dateToFormattedString
                    (currDate).equals
                    (dateToFormattedString(prevDateSelected))) {
                setBackgroundColorDate(getResources().getDrawable(R.drawable
                        .non_data_day_background), prevDateSelected);
            }
            setBackgroundColorDate(getResources().getDrawable(R.drawable
                    .today_background), currDate);

            parseStressData();
            setBackgroundColorDate(getSelectedDateBackground(date), date);
        } else {
            setBackgroundColorDate(getSelectedDateBackground(date), date);
        }
        prevDateSelected = date;
        caldroidFragment.refreshView();
    }

    private void setCaldroidFragment(Bundle args) {
        // WARNING: Hardcoding month and year to start on because data is from 2014.
        args.putInt(CaldroidFragment.MONTH, 12);
        args.putInt(CaldroidFragment.YEAR, 2014);
        args.putBoolean("sixWeeksInCalendar", false);
        caldroidFragment.setArguments(args);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.caldroid_fragment, caldroidFragment);
        t.commit();
    }

    /**
     * Change background color of dates.
     * */
    public void setBackgroundColorDate(Drawable drawable, Date date) {
        caldroidFragment.setBackgroundDrawableForDate(drawable, date);
    }

    /**
     * Set TextView R.id.daysClean based on strDate difference of current strDate and most recent lapse
     * strDate.
     * */
    private void setDaysCleanTextview() {
        // Hard-coded current strDate for prototype.
        DateTime curr = new DateTime(2014, 12, 19, 0, 0, 0, 0);
        DateTime lastLapse;
        int end = (calData.size() - 1), daysSince = 0;
        for (int i = end; i >= 0; i--) {
            if (calData.get(i).numSmokes > 0) {
                lastLapse = new DateTime(calData.get(i).strDate);
                daysSince = lastLapse.numDaysFrom(curr);
                break;
            }
        }

        daysClean = (TextView) findViewById(R.id.daysClean);
        if (daysSince == 0) {
            daysClean.setText(getString(R.string.CLEAN_0_DAYS));
        } else if (daysSince == 1) {
            daysClean.setText(getString(R.string.CLEAN_1_DAY));
        } else {
            daysClean.setText(getString(R.string.CLEAN_N_DAYS, daysSince));
        }
    }

    private void setDayStats(CalendarUserDataStruct cud) {
        int numSmoked = cud.numSmokes;
        double stress = cud.stressValue;
        String stressVerbose;
        String stressStr = String.format("%.2f", stress);
        String dateStr = dateToFormattedString(cud.javaDate);

        // Get verbose stress description
        if (stress < 0.3) {
            stressVerbose = "Low";
        } else if (stress > 0.3 && stress < 0.6) {
            stressVerbose = "Moderate";
        } else {
            stressVerbose = "High";
        }
        dateToday.setText("");
        dayStats.setText("");
        dateToday.setText(getString(R.string.DATE_SELECTED, dateStr));
        dayStats.setText("• ");
        dayStats.append(getString(R.string.CIGS_SMOKED, numSmoked));
        dayStats.append("\n• ");
        dayStats.append(getString(R.string.STRESS_DESCRIPTION, stressVerbose, stressStr));
    }

    private void setNoDataDay(String date) {
        dateToday.setText("");
        dayStats.setText("");
        dateToday.setText(getString(R.string.DATE_SELECTED, date));
        dayStats.setText("• ");
        dayStats.append(getString(R.string.NON_DATA_DAY));
    }

    /**
     * parses the csv data and sets the strDate's color based on the stress level.
     * */
    public void parseStressData() {
        Map<String, Object> extraData = caldroidFragment.getExtraData();
        HashMap<String, Integer> dates = new HashMap<>();
        ColorDrawable color;
        Date user_date;
        double stress;
        int numSmokes;

        for (CalendarUserDataStruct datum : calData) {
            numSmokes = datum.numSmokes;
            user_date = datum.javaDate;

            /* Each calendar cell is colored based on the average stress level for that day. Have
             to cast this value to a float so Math.round() returns an int.*/
            stress = datum.stressValue;
            float stressFloat = (float) stress*10;
            int switch_case = Math.round(stressFloat);

            switch(switch_case) {
                case 1:
                    color = new ColorDrawable(getResources().getColor(R.color.STRESS_0_1));
                    break;
                case 2:
                    color = new ColorDrawable(getResources().getColor(R.color.STRESS_0_2));
                    break;
                case 3:
                    color = new ColorDrawable(getResources().getColor(R.color.STRESS_0_3));
                    break;
                case 4:
                    color = new ColorDrawable(getResources().getColor(R.color.STRESS_0_4));
                    break;
                case 5:
                    color = new ColorDrawable(getResources().getColor(R.color.STRESS_0_5));
                    break;
                case 6:
                    color = new ColorDrawable(getResources().getColor(R.color.STRESS_0_6));
                    break;
                case 7:
                    color = new ColorDrawable(getResources().getColor(R.color.STRESS_0_7));
                    break;
                case 8:
                    color = new ColorDrawable(getResources().getColor(R.color.STRESS_0_8));
                    break;
                case 9:
                    color = new ColorDrawable(getResources().getColor(R.color.STRESS_0_9));
                    break;
                default:
                    color = new ColorDrawable(getResources().getColor(R.color.DEFAULT_WHITE));
            }
            // Set the strDate color...
            setBackgroundColorDate(color, user_date);

            // Populate extraData HashMap
            String dateFormatted = dateToFormattedString(user_date);
            dates.put(dateFormatted, numSmokes);
        }
        //Pass numSmokes data to CaldroidCustomAdapter
        extraData.put("DATES", dates);
    }

    /**
     * Retrieve appropriate background color for selected strDate.
     * */
    private Drawable getSelectedDateBackground(Date date) {
        Drawable background;
        double stress = 0.0;
        CalendarUserDataStruct cud = getDataByDate(date);
        if (cud != null) {
            stress = cud.stressValue;
        }
        float stressFloat = (float) stress*10;
        int switch_case = Math.round(stressFloat);

        switch(switch_case) {
            case 1:
                background = getResources().getDrawable(R.drawable.red_border_0_1);
                break;
            case 2:
                background = getResources().getDrawable(R.drawable.red_border_0_2);
                break;
            case 3:
                background = getResources().getDrawable(R.drawable.red_border_0_3);
                break;
            case 4:
                background = getResources().getDrawable(R.drawable.red_border_0_4);
                break;
            case 5:
                background = getResources().getDrawable(R.drawable.red_border_0_5);
                break;
            case 6:
                background = getResources().getDrawable(R.drawable.red_border_0_6);
                break;
            case 7:
                background = getResources().getDrawable(R.drawable.red_border_0_7);
                break;
            case 8:
                background = getResources().getDrawable(R.drawable.red_border_0_8);
                break;
            case 9:
                background = getResources().getDrawable(R.drawable.red_border_0_9);
                break;
            default:
                background = getResources().getDrawable(R.drawable.red_border_default);
        }
        return background;
    }


    /**
     * Convert java.util.Date to a simple String format.
     * */
    public static String dateToFormattedString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String stringF = dateFormat.format(date);
        return stringF;
    }

    /**
     * Retrieves CalendarUserDataStruct object.
     * */
    public CalendarUserDataStruct getDataByDate(Date date) {
        CalendarUserDataStruct cud = null;
        String dateStr = dateToFormattedString(date);
        for (CalendarUserDataStruct datum : calData) {
            if (dateStr.equals(dateToFormattedString(datum.javaDate))) {
                cud = datum;
                break;
            }
        }

        return cud;
    }
}
