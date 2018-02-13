package com.afoodchronicle;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends FacebookActivity implements View.OnClickListener {

    private EditText emailField;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        emailField = findViewById(R.id.etEmail);
        mAuth = FirebaseAuth.getInstance();


        findViewById(R.id.mLoginTextView).setOnClickListener(this);
        findViewById(R.id.reset_button).setOnClickListener(this);
    }

    private void resetPassword(){

    String email = emailField.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
        Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
        return;
    }

    showProgressDialog();

    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()) {
                Toast.makeText(ResetPasswordActivity.this,
                        "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ResetPasswordActivity.this,
                        "Failed to send reset email!", Toast.LENGTH_SHORT).show();
            }
            hideProgressDialog();
        }
    });
   }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.reset_button) {
            resetPassword();
            Intent intent = new Intent(ResetPasswordActivity.this, LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
       else if (i == R.id.mLoginTextView) {
            Intent intent = new Intent(ResetPasswordActivity.this, LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}


