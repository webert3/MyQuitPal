package com.example.webert3.imagedisplayapp.Fragments;

        import android.content.Context;
        import android.graphics.Canvas;
        import android.widget.TextView;

        import com.example.webert3.imagedisplayapp.R;
        import com.example.webert3.imagedisplayapp.Structs.LapseStruct;
        import com.github.mikephil.charting.components.MarkerView;
        import com.github.mikephil.charting.data.Entry;
        import com.github.mikephil.charting.highlight.Highlight;

/**
 * Created by ted on 10/6/16.
 */

public class RelativeRelapseMarkerView extends MarkerView {
    private TextView lapseMarker;
    private int screenWidth;

    public RelativeRelapseMarkerView (Context context, int layoutResource) {
        super(context, layoutResource);
        lapseMarker = (TextView) findViewById(R.id.lapseMarkerTV);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        LapseStruct ls = (LapseStruct) e.getData();
        String stress = String.format("%1$,.2f", ls.stress);
        if (ls.pId == 6012) {
            lapseMarker.setText(getContext().getResources().getString(R.string.my_stress_level));
            lapseMarker.append(" "+stress);
        } else {
            lapseMarker.setText(getContext().getResources().getString(R.string.stress_level));
            lapseMarker.append(" "+stress);
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
