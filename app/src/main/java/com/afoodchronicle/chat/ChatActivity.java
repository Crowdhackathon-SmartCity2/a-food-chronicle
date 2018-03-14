package com.afoodchronicle.chat;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afoodchronicle.R;

import static com.afoodchronicle.utilities.Static.FULL_NAME;
import static com.afoodchronicle.utilities.Static.VISIT_USER_ID;


public class ChatActivity extends AppCompatActivity {
    private String messageReceiverId;
    private String messageReceiverName;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        messageReceiverId= getIntent().getExtras().get(VISIT_USER_ID).toString();
        messageReceiverName= getIntent().getExtras().get(FULL_NAME).toString();

       
    }
}
