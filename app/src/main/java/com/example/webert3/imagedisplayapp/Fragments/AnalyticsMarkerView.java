package com.example.webert3.imagedisplayapp.Fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

import com.example.webert3.imagedisplayapp.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

/**
 * Created by ted on 9/6/16.
 */

public class AnalyticsMarkerView extends MarkerView {
    private TextView lapseMarker;
    private int screenWidth;

    public AnalyticsMarkerView (Context context, int layoutResource) {
        super(context, layoutResource);
        lapseMarker = (TextView) findViewById(R.id.lapseMarkerTV);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
}

    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        if (e.getData() == null) {
            lapseMarker.setText("Stress Level: " + e.getVal()/100);
        } else {
            lapseMarker.setText("Cigarettes Smoked: "+e.getData());
        }
    }

    @Override
    public void draw(Canvas canvas, float posx, float posy)
    {
        // Check marker position and update offsets.
        int w = getWidth();
        if((screenWidth-posx-w) < w) {
            posx -= w;
        }
        posy -= 65;
        // translate to the correct position and draw
        canvas.translate(posx, posy);
        draw(canvas);
        canvas.translate(-posx, -posy);
    }

    @Override
    public int getXOffset(float xpos) {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return -getHeight() - 20;
    }

}
