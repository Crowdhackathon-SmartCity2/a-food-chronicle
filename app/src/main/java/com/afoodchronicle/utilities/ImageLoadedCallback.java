package com.afoodchronicle.utilities;

import android.widget.ProgressBar;

import com.squareup.picasso.Callback;

public class ImageLoadedCallback implements Callback {

    public final ProgressBar progressBar;

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
