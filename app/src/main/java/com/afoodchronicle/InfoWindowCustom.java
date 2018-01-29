package com.afoodchronicle;

import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class InfoWindowCustom extends AppCompatActivity implements GoogleMap.InfoWindowAdapter  {
    Context context;
    LayoutInflater inflater;

    public InfoWindowCustom(Context context) {
        this.context = context;
    }
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
    @Override
    public View getInfoWindow(Marker marker) {
        inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.custom_info_contents, null);

        return v;
    }
        //TextView title = (TextView) v.findViewById(R.id.info_window_title);
     //   TextView subtitle = (TextView) v.findViewById(R.id.info_window_subtitle);
        //title.setText(marker.getTitle());
       // subtitle.setText(marker.getSnippet());

    }