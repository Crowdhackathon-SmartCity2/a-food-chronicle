package com.afoodchronicle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


public class InfoWindowDetails extends AppCompatActivity{
    public static final String markerKey = "MARKER_NAME";

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_info_window);
        Intent intent = getIntent();
        String markerName=intent.getExtras().getString(markerKey);
        if (markerName!= null){
            TextView title = findViewById(R.id.title);
            title.setText(markerName);

        }
    }
}
