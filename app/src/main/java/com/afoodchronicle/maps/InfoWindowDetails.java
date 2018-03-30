package com.afoodchronicle.maps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.afoodchronicle.R;


public class InfoWindowDetails extends AppCompatActivity{
    private static final String markerKey = "MARKER_NAME";

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_info_window);
        Intent intent = getIntent();
        String markerName=intent.getExtras().getString(markerKey);
        TextView title = findViewById(R.id.title);
        TextView snippet = findViewById(R.id.snippet);
        ImageView badge = findViewById(R.id.badge);

        if (markerName!= null){
            if (markerName.equals("yoleni")) {
                snippet.setText(R.string.lorem_ipsum_detail);
                title.setText(markerName);
                badge.setImageResource(R.drawable.yoleni_restaurant);
            }
            if (markerName.equals("vorria")) {
                snippet.setText(R.string.lorem_ipsum_detail);
                title.setText(markerName);
                badge.setImageResource(R.drawable.yoleni_restaurant);
            }
            if (markerName.equals("ariana")) {
                snippet.setText(R.string.lorem_ipsum_detail);
                title.setText(markerName);
                badge.setImageResource(R.drawable.yoleni_restaurant);
            }
            if (markerName.equals("pantopoleio")) {
                snippet.setText(R.string.lorem_ipsum_detail);
                title.setText(markerName);
                badge.setImageResource(R.drawable.yoleni_restaurant);
            }
        }
    }
}
