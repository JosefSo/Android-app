package com.example.famileat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassActivity extends AppCompatActivity {

    private EditText email_text;
    private Button resetButton;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        email_text = (EditText) findViewById(R.id.email);
        resetButton = (Button) findViewById(R.id.reset);

        auth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }
    private void resetPassword() {
        String email = email_text.getText().toString().trim();

        if (email.isEmpty()) {
            email_text.setError("Email is empty!");
            email_text.requestFocus();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_text.setError("Please enter valid email");
            email_text.requestFocus();
        }
        else {
            auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPassActivity.this, "Check you email to reset your password", Toast.LENGTH_SHORT).show();
                        Intent n = new Intent(getApplicationContext(),StartActivity.class);
                        startActivity(n);
                    }
                    else {
                        Toast.makeText(ForgotPassActivity.this, " Reset failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}