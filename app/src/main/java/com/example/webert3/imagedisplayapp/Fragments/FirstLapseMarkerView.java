package com.example.webert3.imagedisplayapp.Fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.example.webert3.imagedisplayapp.R;
import com.example.webert3.imagedisplayapp.Structs.ScatterchartFirstLapseStruct;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

/**
 * Created by ted on 9/14/16.
 */// todo: Implement this to display stress levels and current user.

public class FirstLapseMarkerView extends MarkerView {
    private TextView marker;
    private int screenWidth, screenHeight, toolbarHeight, titleHeight;

    public FirstLapseMarkerView(Context context, int layoutResource, int tbHeight, int tvHeight) {
        super(context, layoutResource);
        marker = (TextView) findViewById(R.id.lapseMarkerTV);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight= getResources().getDisplayMetrics().heightPixels;
        toolbarHeight = tbHeight;
        titleHeight = tvHeight;
    }

    @Override
    public void draw(Canvas canvas, float posx, float posy)
    {
        posx = screenWidth - (marker.getWidth()*1.05f);
        posy = (toolbarHeight + titleHeight)+10;

        canvas.translate(posx, posy);
        draw(canvas);
        canvas.translate(-posx, -posy);
    }

    @Override
    public int getXOffset(float xpos) {
        return (int) xpos;
    }

    @Override
    public int getYOffset(float ypos) {

        return (int) ypos;
    }

    @Override
    public void refreshContent(Entry entry, Highlight highlight) {
        ScatterchartFirstLapseStruct fs = (ScatterchartFirstLapseStruct) entry.getData();

        if (fs.id == 6012) {
            marker.setText("My Lapse - ");
            marker.append(militaryToStandard(fs.time_formatted.substring(0,5)));
        } else {
            marker.setText("Lapsed at ");
            marker.append(militaryToStandard(fs.time_formatted.substring(0,5)));
        }
    }

    private String militaryToStandard(String time) {
        int hr = Integer.parseInt(time.substring(0,2));
        int min = Integer.parseInt(time.substring(3,5));

        if (hr > 12) {
            hr -= 12;
            time = String.format("%02d", hr)+":"+String.format("%02d", min)+" PM";
        } else if (hr == 12) {
            time = String.format("%02d", hr)+":"+String.format("%02d", min)+" PM";
        } else if (hr == 0) {
            hr = 12;
            time = String.format("%02d", hr)+":"+String.format("%02d", min)+" AM";
        } else {
            time = String.format("%02d", hr)+":"+String.format("%02d", min)+" AM";
        }

        return time;
    }
}
