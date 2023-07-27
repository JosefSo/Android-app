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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import classes.User;

public class EditProfileActivity extends AppCompatActivity {

    private EditText text_fullName;
    private EditText text_date;
    private RadioGroup radio_gender;
    private RadioButton male_r, female_r, gender_r;
    private Button submit;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String ID;
    private User profile;
    //    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        text_fullName = findViewById(R.id.fullName);
        text_date = findViewById(R.id.date);
        submit = findViewById(R.id.submit);
        radio_gender = findViewById(R.id.gender);
        male_r = findViewById(R.id.male);
        female_r = findViewById(R.id.female);
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        ID = user.getUid();
        reference.child(ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                profile = snapshot.getValue(User.class);
                if (profile != null) {
                    text_fullName.setText(profile.getFullName());
                    text_date.setText(profile.getDate());
                    if(profile.getGender().equals("Male"))
                        male_r.setChecked(true);
                    else
                        female_r.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        auth = FirebaseAuth.getInstance();

        //Date Button:
        text_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                DatePickerDialog dateDialog = new DatePickerDialog(EditProfileActivity.this,
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




        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullName = text_fullName.getText().toString();
                String date = text_date.getText().toString();
                int select_gender = radio_gender.getCheckedRadioButtonId();

                String validans="";
                if (!(validans= User.check_fullName(fullName)).equals("accept")) {
                    Toast.makeText(EditProfileActivity.this, validans, Toast.LENGTH_SHORT).show();
                    text_fullName.requestFocus();}
                else if (!(validans= User.check_date(date)).equals("accept")) {
                    Toast.makeText(EditProfileActivity.this, validans, Toast.LENGTH_SHORT).show();
                    text_date.requestFocus();
                }
                else if (select_gender == -1) {
                    Toast.makeText(EditProfileActivity.this, "Please choose gender.", Toast.LENGTH_SHORT).show();
                    radio_gender.requestFocus();
                }
                else {
                    gender_r = (RadioButton) findViewById(select_gender);
                    String gender = gender_r.getText().toString();
                    updateUser(fullName, date, gender);
                }
            }
        });
    }

    private void updateUser(String fullName, String date, String gender)
    {
        profile.setFullName(fullName);
        profile.setGender(gender);
        profile.setDate(date);
        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(EditProfileActivity.this, fullName + "Edit profile successfully!", Toast.LENGTH_SHORT).show();
                        Intent n;
                        if(profile.getType().equals("Host"))
                            n = new Intent(getApplicationContext(),HostMainActivity.class);
                        else
                            n = new Intent(getApplicationContext(),GuestMainActivity.class);
                        startActivity(n);
                    }
                });
    }
}