package com.afoodchronicle.maps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afoodchronicle.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

@SuppressLint("Registered")
public class InfoWindowCustom extends AppCompatActivity implements GoogleMap.InfoWindowAdapter  {

    private final HashMap<String, String> markerMap;
    private LayoutInflater inflater;
    private View view;
    private final Context context;


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
            final ViewGroup nullParent = null;
            view = inflater.inflate(R.layout.custom_info_contents, nullParent);
            TextView title = view.findViewById(R.id.title);
            TextView snippet = view.findViewById(R.id.snippet);
            ImageView badge = view.findViewById(R.id.badge);
            if (m.equals("yoleni")) {

                title.setText(m);
                snippet.setText(R.string.yoleni_snippet);
                badge.setImageResource(R.drawable.yoleni_restaurant);
            }
            if (m.equals("vorria")) {
                title.setText(m);
                snippet.setText(R.string.vorria_snippet);
                badge.setImageResource(R.drawable.yoleni_restaurant);
            }
            if (m.equals("ariana")){
                title.setText(m);
                snippet.setText(R.string.ariana_snippet);
                badge.setImageResource(R.drawable.yoleni_restaurant);
           }
            if (m.equals("pantopoleio")){
                title.setText(m);
                snippet.setText(R.string.pantopoleio_snippet);
                badge.setImageResource(R.drawable.yoleni_restaurant);
            }


            return view;
        }
        return view;
    }
    }