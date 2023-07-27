package com.example.famileat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import classes.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText text_password;
    private EditText text_fullName;
    private EditText text_email;
    private EditText text_date;
    private RadioGroup radio_gender;
    private RadioGroup radio_type;
    private RadioButton gender_r;
    private RadioButton type_r;
    private Button signup;

//    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        text_fullName = findViewById(R.id.fullName);
        text_email = findViewById(R.id.email);
        text_password = findViewById(R.id.password);
        text_date = findViewById(R.id.date);
        signup = findViewById(R.id.signup);
        radio_gender = findViewById(R.id.gender);
        radio_type = findViewById(R.id.type);


        auth = FirebaseAuth.getInstance();

        //Date Button:
        text_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                DatePickerDialog dateDialog = new DatePickerDialog(RegisterActivity.this,
                        android.R.style.Theme_Holo_Dialog_MinWidth,mDateSetListener,year,month,day);
                dateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dateDialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = dayOfMonth + "/" + (month+1) + "/" + year;
                text_date.setText(date);
            }
        };

        //Sign up button: ..............................................
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullName = text_fullName.getText().toString();
                String email = text_email.getText().toString();
                String password = text_password.getText().toString();
                String date = text_date.getText().toString();
                int select_gender = radio_gender.getCheckedRadioButtonId();
                int select_type = radio_type.getCheckedRadioButtonId();

                //validation input checking...............................
                String valid_ans= "";
                if (!(valid_ans=User.check_fullName(fullName)).equals("accept")) {
                    Toast.makeText(RegisterActivity.this, valid_ans, Toast.LENGTH_SHORT).show();
                    text_fullName.requestFocus();
                } else if (!(valid_ans= User.check_email(email)).equals("accept")) {
                    Toast.makeText(RegisterActivity.this, valid_ans, Toast.LENGTH_SHORT).show();
                    text_email.requestFocus();
                } else if (!(valid_ans=User.check_pass(password)).equals("accept")) {
                    Toast.makeText(RegisterActivity.this, valid_ans, Toast.LENGTH_SHORT).show();
                    text_password.requestFocus();
                } else if (!(valid_ans= User.check_date(date)).equals("accept")) {
                    Toast.makeText(RegisterActivity.this, valid_ans, Toast.LENGTH_SHORT).show();
                    text_date.requestFocus();
                }
                else if (select_gender == -1) {
                    Toast.makeText(RegisterActivity.this, "Please choose gender.", Toast.LENGTH_SHORT).show();
                    radio_gender.requestFocus();
                }
                else if (select_type == -1) {
                    Toast.makeText(RegisterActivity.this, "Please choose if you want to be Host or Guest.", Toast.LENGTH_SHORT).show();
                    radio_gender.requestFocus();
                }
                else {
                    gender_r = (RadioButton) findViewById(select_gender);
                    type_r = (RadioButton) findViewById(select_type);
                    String gender = gender_r.getText().toString();
                    String type = type_r.getText().toString();
                    registerUser(fullName, email, password , date, gender, type);
                }

            }
        });
    }
    //...................................................................

    private void registerUser(String fullName, String email, String password, String date, String gender, String type) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    ArrayList<Dinner> dinners=new ArrayList<Dinner>();
                    User user = new User(fullName, email, date, gender, type);
                    String Uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(Uid)
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        if (type.equals("Host")) {
                                            Toast.makeText(RegisterActivity.this, fullName + " registered successfully as a Host!", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            Toast.makeText(RegisterActivity.this, fullName + " registered successfully as a guest!", Toast.LENGTH_SHORT).show();
                                        }

                                        FirebaseUser user2 = FirebaseAuth.getInstance().getCurrentUser();
                                        user2.sendEmailVerification();
                                        Intent n = new Intent(getApplicationContext(),StartActivity.class);
                                        startActivity(n);
                                    } else {
                                        Toast.makeText(RegisterActivity.this, " Registration failed!", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                }
                else {
                    Toast.makeText(RegisterActivity.this, " Registration failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}