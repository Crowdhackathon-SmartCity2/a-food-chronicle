package com.afoodchronicle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Mayke on 27.02.2018.
 */

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Thread thread = new Thread()
        {
          @Override
            public void run()
          {
              try
              {
                  sleep(3000);
              }
              catch (InterruptedException e)
              {
                  e.printStackTrace();
              }
              finally
              {
                  Intent goToMainActivity = new Intent(WelcomeActivity.this, MainActivity.class);
                  goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                  startActivity(goToMainActivity);
                  finish();
              }
          }
        };
        thread.start();
    }
}
