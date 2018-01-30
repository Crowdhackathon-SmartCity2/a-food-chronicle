package com.afoodchronicle;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

public class InfoWindowCustom extends AppCompatActivity implements GoogleMap.InfoWindowAdapter  {

    private HashMap<String, String> markerMap;
    LayoutInflater inflater;
    private View view;
    Context context;


    public InfoWindowCustom(HashMap<String, String> markerMap, Context context) {
        this.markerMap = markerMap;
        this.context = context;
    }
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        String m = markerMap.get(marker.getId());
        if (m != null) {
            inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.custom_info_contents, null);
            if (m == "yoleni"){
                TextView title = view.findViewById(R.id.title);
                title.setText(m);
                ImageView badge = view.findViewById(R.id.badge);
                badge.setImageResource(R.drawable.yoleni_restaurant);
            }
            return view;
        }
        return view;
    }
    }