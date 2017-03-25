package com.example.webert3.imagedisplayapp.Helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.webert3.imagedisplayapp.R;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

import java.util.Map;

import hirondelle.date4j.DateTime;

/**
 * Class is part of the Caldroid library on github: https://github.com/roomorama/Caldroid
 *
 * Modified by webert3.
 */
public class CaldroidCustomAdapter extends CaldroidGridAdapter {
    private static final String TAG = "Caldroid Custom Adapter";
    private Map<String, Integer> dates= (Map<String, Integer>) extraData.get("DATES");
    private String currentDate;

    public CaldroidCustomAdapter(Context context, int month, int year,
                                 Map<String, Object> caldroidData,
                                 Map<String, Object> extraData) {
        super(context, month, year, caldroidData, extraData);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View cellView = convertView;

        // For reuse
        if (convertView == null) {
            cellView = inflater.inflate(R.layout.custom_cell, null);
        }

        int topPadding = cellView.getPaddingTop();
        int leftPadding = cellView.getPaddingLeft();
        int bottomPadding = cellView.getPaddingBottom();
        int rightPadding = cellView.getPaddingRight();

        TextView dateText = (TextView) cellView.findViewById(R.id.dateText);
        ImageView smokeIcon = (ImageView) cellView.findViewById(R.id.smoking_icon);

        dateText.setTextColor(Color.BLACK);

        // Get dateTime of this cell
        DateTime dateTime = this.datetimeList.get(position);
        Resources resources = context.getResources();

        // Set color of the dates in previous / next month
        if (dateTime.getMonth() != month) {
            dateText.setTextColor(resources
                    .getColor(com.caldroid.R.color.caldroid_darker_gray));
        }

        DateTime curr = new DateTime(2014, 12, 19, 0, 0, 0, 0);
        boolean shouldResetDiabledView = false;
        boolean shouldResetSelectedView = false;

        // Customize for disabled dates and strDate outside min/max dates
        if ((minDateTime != null && dateTime.lt(minDateTime))
                || (maxDateTime != null && dateTime.gt(maxDateTime))
                || (disableDates != null && disableDates.indexOf(dateTime) != -1)) {

            dateText.setTextColor(CaldroidFragment.disabledTextColor);
            if (CaldroidFragment.disabledBackgroundDrawable == -1) {
                cellView.setBackgroundResource(com.caldroid.R.drawable.disable_cell);
            } else {
                cellView.setBackgroundResource(CaldroidFragment.disabledBackgroundDrawable);
            }

        } else {
            shouldResetDiabledView = true;
        }

        // Customize for selected dates
        if (selectedDates != null && selectedDates.indexOf(dateTime) != -1) {
            cellView.setBackgroundColor(resources
                    .getColor(com.caldroid.R.color.caldroid_sky_blue));

            dateText.setTextColor(Color.BLACK);

        } else {
            shouldResetSelectedView = true;
        }

        if (shouldResetDiabledView && shouldResetSelectedView) {
            if (dateTime.equals(curr)) {
                cellView.setBackgroundResource(R.color.cell_gray_bg);
                dateText.setTextSize(20);
            } else {
                cellView.setBackgroundResource(com.caldroid.R.drawable.cell_bg);
            }
        }

        dateText.setText("" + dateTime.getDay());

        // Set number of cigs smoked per day and flame icon if data exists.
        currentDate = formatDateTime(dateTime);
        if (dates.containsKey(currentDate)) {
            int numSmoked = dates.get(currentDate);
            if (numSmoked >0) {
                if (numSmoked < 3) {
                    smokeIcon.setImageResource(R.drawable.cig_short);
                    Log.v(TAG, "Number of cigarettes smoked on "+currentDate.toString()+": "+dates
                            .get(currentDate)+"\n Icon used: cig_short");
                } else if (numSmoked < 5) {
                    smokeIcon.setImageResource(R.drawable.cig_medium);
                    Log.v(TAG, "Number of cigarettes smoked on "+currentDate.toString()+": "+dates
                            .get(currentDate)+"\n Icon used: cig_medium");
                } else {
                    smokeIcon.setImageResource(R.drawable.cig_long);
                    Log.v(TAG, "Number of cigarettes smoked on "+currentDate.toString()+": "+dates
                            .get(currentDate)+"\n Icon used: cig_long");
                }
            }
        }

        // Somehow after setBackgroundResource, the padding collapses.
        // This is to recover the padding
        cellView.setPadding(leftPadding, topPadding, rightPadding,
                bottomPadding);

        // Set custom color if required
        setCustomResources(dateTime, cellView, dateText);

        return cellView;
    }

    /**
    * Formats Joda-Time DateTime object so a simple string.
     *
     * @param dateTime: The DateTime object.
*      @return fDate: strDate as a formatted string (MM.dd.yy)
    * */
    public String formatDateTime(DateTime dateTime) {
        String fDate = dateTime.getMonth() +"/"+String.format("%02d", dateTime.getDay()) + "/" +
                dateTime.getYear();
        return fDate;
    }



}
