package com.example.webert3.MyQuitPal.Activities.Compare;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.webert3.MyQuitPal.R;

/**
 * Created by ted on 9/24/16.
 */
public class CompareInterfaceMenu extends AppCompatActivity {
    public static final String TAG = "CompareInterfaceMenu";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compare_menu);

        // Setting up toolbar
        setToolbar();

    }

    private void setToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.compare_menu_toolbar);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;
        switch (item.getItemId()) {
            case R.id.calendar_toolbar_icon:
                i = new Intent(CompareInterfaceMenu.this, MyCalendar.class);
                break;
            case R.id.analytics_toolbar_icon:
                i = new Intent(CompareInterfaceMenu.this, AllDaysListView.class);
                break;
            case R.id.compare_toolbar_icon:
                break;
            case R.id.map_toolbar_icon:
                i = new Intent(CompareInterfaceMenu.this, MapsActivity.class);
                break;
        }
        if (i != null) {
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_buttons, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onButtonClicked(View v) {
        if (v.getId() == R.id.time_of_first_lapse_button) {
            Intent i = new Intent(CompareInterfaceMenu.this, FirstLapseScatterChart.class);
            startActivity(i);
        }
        if (v.getId() == R.id.num_smoked_button) {
            Intent i = new Intent(CompareInterfaceMenu.this, CigrettesPostRelapse.class);
            startActivity(i);
        }
        if (v.getId() == R.id.hours_til_next_lapse_button) {
            Intent i = new Intent(CompareInterfaceMenu.this, RelativeRelapses.class);
            startActivity(i);
        }
    }
}
