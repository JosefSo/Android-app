package com.example.famileat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import classes.Dinner;
import adapters.HostsAdapter;

public class EditDinnerActivity extends AppCompatActivity {

    private Button submit, btnTime, uploadImage;
    private EditText text_title, text_details, text_date;
    private TextView location_text;
    private RadioGroup radio_kosher;
    private RadioButton kosher_r, meat_r, dairy_r, notkosher_r;
    private NumberPicker amont_t;
    private ImageView imgGallery;
    private Dinner dinner;
    int PLACE_PICKER_REQUEST = 1, SELECT_PICTURE = 200;
    private final int GALLERY_REQ_CODE = 1000;
//    private  Uri selectedImageUri;

    // Upload Image: .................................
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    String picName="";
    //...............................................

    //    private ProgressBar progressBar;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private boolean uploading=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dinner);

        text_title = findViewById(R.id.title);
        text_date = findViewById(R.id.date);
        btnTime = findViewById(R.id.time);
        amont_t = findViewById(R.id.amount_p);
        amont_t.setMinValue(1);
        amont_t.setMaxValue(500);
        meat_r = findViewById(R.id.meat);
        dairy_r = findViewById(R.id.dairy);
        notkosher_r = findViewById(R.id.noKosher);
        radio_kosher = findViewById(R.id.kosher);
        text_details = findViewById(R.id.details);
        submit = findViewById(R.id.submit);
        imgGallery = findViewById(R.id.imgGallery);
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        location_text = findViewById(R.id.location);

        reference = FirebaseDatabase.getInstance().getReference("Dinners");
        reference.child(HostsAdapter.currUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dinner = snapshot.getValue(Dinner.class);
                if (dinner != null) {
                    text_title.setText(dinner.getTitle());
                    picName = dinner.getPicture();
                    text_date.setText(dinner.getDate());
                    btnTime.setText(dinner.getTime());
                    amont_t.setValue(dinner.getAmount());
                    location_text.setText(dinner.getAddress());
                    if(dinner.getKosher().equals("Kosher dairy"))
                        dairy_r.setChecked(true);
                    else if(dinner.getKosher().equals("Kosher meat"))
                        meat_r.setChecked(true);
                    else if(dinner.getKosher().equals("Not kosher"))
                        notkosher_r.setChecked(true);
                    text_details.setText(dinner.getDetails());

                    //View the dinner image.
                    storageReference = storage.getReference("images/" + picName);
                    try {
                        File file = File.createTempFile("temp", ".png");
                        storageReference.getFile(file).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                imgGallery.setImageBitmap(bitmap);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("Failed");
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //Pick Picture Button
        imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        //Date Button:
        text_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                DatePickerDialog dateDialog = new DatePickerDialog(EditDinnerActivity.this,
                        android.R.style.Theme_Holo_Dialog_MinWidth, mDateSetListener, year, month, day);
                dateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dateDialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                text_date.setText(date);
            }
        };

        //Time Button
        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                // on below line we are initializing our Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(EditDinnerActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                // on below line we are setting selected time
                                // in our text view.
                                String time = hourOfDay + ":" + minute;
                                if (time.length() < 5) {
                                    if (hourOfDay<10)
                                        time = "0" + time;
                                    if (minute <10){
                                        String[] spl = time.split(":");
                                        time = spl[0] + ":0" + spl[1];
                                    }

                                }
                                btnTime.setText(time);
                            }
                        }, hour, minute, true);
                // at last we are calling show to
                // display our time picker dialog.
                timePickerDialog.show();
            }
        });


        //Submit button: ..................................................
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = text_title.getText().toString();
                String date = text_date.getText().toString();
                String time = btnTime.getText().toString();
                String location = location_text.getText().toString();
                String address = "Ariel University";
                int amount = amont_t.getValue();
                int select_kosher = radio_kosher.getCheckedRadioButtonId();
                String details = text_details.getText().toString();

                //validation input checking...............................
                String valid_ans = "";
                if (!(valid_ans = Dinner.check_title(title)).equals("accept")) {
                    Toast.makeText(EditDinnerActivity.this, valid_ans, Toast.LENGTH_SHORT).show();
                    text_title.requestFocus();
                } else if (!(valid_ans = Dinner.check_date_time(date, time,false)).equals("accept")) {
                    Toast.makeText(EditDinnerActivity.this, valid_ans, Toast.LENGTH_SHORT).show();
                    text_date.requestFocus();
                } else if (!(valid_ans = Dinner.check_amount(amount, dinner.getAcceptedUid().size())).equals("accept")) {
                    Toast.makeText(EditDinnerActivity.this, valid_ans, Toast.LENGTH_SHORT).show();
                    text_date.requestFocus();
                } else {
                    String kosher = "Unspecified";
                    if (select_kosher != -1) {
                        kosher_r = (RadioButton) findViewById(select_kosher);
                        kosher = kosher_r.getText().toString();
                    }
                    if(!picName.equals(dinner.getPicture())) {
                        Dinner.deletePicture(dinner.getPicture());
                    }

                    updateDinner(title, date, time, address, amount, kosher, details, picName, location);
                }
            }
        });
    }

    private void updateDinner(String title, String date, String time, String address, int amount, String kosher, String details, String picture, String location) {
        dinner.setTitle(title);
        dinner.setDate(date);
        dinner.setTime(time);
        dinner.setAddress(address);
        dinner.setAmount(amount);
        dinner.setKosher(kosher);
        dinner.setDetails(details);
        dinner.setPicture(picture);
        dinner.setAddress(location);
        FirebaseDatabase.getInstance().getReference("Dinners")
                .child(dinner.getID()).setValue(dinner).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(EditDinnerActivity.this, title + "edited successfully!", Toast.LENGTH_SHORT).show();
                        Intent n;
                        n = new Intent(getApplicationContext(),HostMainActivity.class);
                        startActivity(n);
                    }
                });
    }
    //...................................................................

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);
    }

    private void uploadImage() {
        if (!picName.equals("default_dinner.jpg")) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading file....");
            progressDialog.show();
            storageReference = storage.getReference();
            storageReference = FirebaseStorage.getInstance().getReference("images/" + picName);
            storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    imgGallery.setImageURI(imageUri);
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditDinnerActivity.this, "image upload failed!", Toast.LENGTH_SHORT).show();
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            });
        }

    }
    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 100 && data != null && data.getData() != null) {
                if (!picName.equals(dinner.getPicture()))
                    Dinner.deletePicture(picName);
                imageUri = data.getData();
                imgGallery.setImageURI(imageUri);
                setUri(imageUri);
                setPicName();
                uploadImage();
            }

    }
    private void setPicName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        String picName = format.format(now);
        this.picName = picName;
    }

    private void setUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}