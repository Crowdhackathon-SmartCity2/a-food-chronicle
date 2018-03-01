package com.afoodchronicle.utilities;

import android.widget.ProgressBar;

import com.squareup.picasso.Callback;

/**
 * Created by Mayke on 01.03.2018.
 */
public class ImageLoadedCallback implements Callback {

    public ProgressBar progressBar;

    protected ImageLoadedCallback(ProgressBar progBar){
        progressBar = progBar;
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError() {

    }
}
