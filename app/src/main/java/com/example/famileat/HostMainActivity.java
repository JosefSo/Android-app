package com.example.famileat;

import static com.example.famileat.StartActivity.SHARED_PREFS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import adapters.PastHostAdapter;
import adapters.RequestsAdapter;
import classes.Dinner;
import adapters.HostsAdapter;
import classes.Request;
import classes.User;

public class HostMainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private Button logout, editprofile, new_meal, btnRequests,search, past;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String ID;
    private Boolean history= false;
    private EditText text_date;
    private TextView name_text, historytxt, requestNumTxt;
    private RadioGroup radio_kosher;
//    private RadioButton kosher_r, meat_r, dairy_r, notkosher_r, all_r;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private DatabaseReference referenceD;
    private RecyclerView recyclerView;
    private HostsAdapter hostAdapter;
    private PastHostAdapter pastAdapter;
    private Spinner kosher_select;
    private String kosher_text, rate_text;
    private AlertDialog alertDialog;
    ArrayList<Dinner> dinnerList, pastlist;

    private static final String ONESIGNAL_APP_ID = "d165de36-ef1b-46ff-b69b-678b6637236e";

    TextView name,email;

    private boolean backPressed = false;
    private AlertDialog.Builder dialog_builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_main);


        //set user and ID
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        ID = user.getUid();

        //kosher filter
        kosher_select = findViewById(R.id.kosher_select);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.kosher ,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kosher_select.setAdapter(adapter);
        kosher_select.setOnItemSelectedListener(this);
        kosher_text = "All";
        historytxt = findViewById(R.id.mymealstxt);
        name_text = findViewById(R.id.name);

        name_text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    alertDialog.dismiss();
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HostMainActivity.this);
                    builder.setMessage(rate_text)
                            .setCancelable(false);
                    alertDialog = builder.create();
                    alertDialog.show();
                }
                return true;
            }
        });

        //set Date Button:
        text_date = findViewById(R.id.date);
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH)+1;
        int year = cal.get(Calendar.YEAR);
        final String[] date = {day + "/" + month + "/" + year};
        text_date.setText(date[0]);
        text_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dateDialog = new DatePickerDialog(HostMainActivity.this,
                        android.R.style.Theme_Holo_Dialog_MinWidth, mDateSetListener, year, month-1, day);
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

        //Dinner List: ...........................................................................
        recyclerView = findViewById(R.id.dinnerList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        referenceD = FirebaseDatabase.getInstance().getReference("Dinners");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dinnerList = new ArrayList<>();
        pastlist = new ArrayList<>();

        //Set host adapter..............................................
        dialog_builder = new AlertDialog.Builder(this);
        hostAdapter = new HostsAdapter(this,dinnerList, R.drawable.google, dialog_builder);
        recyclerView.setAdapter(hostAdapter);

        //Set past adapter..............................................
        pastAdapter = new PastHostAdapter(this,pastlist, R.drawable.google, dialog_builder);

        referenceD.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               refreshLists(snapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //.........................................................................................
        //set requests number

        DatabaseReference referenceR = FirebaseDatabase.getInstance().getReference("Requests");
        referenceR.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int sum = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Request request = dataSnapshot.getValue(Request.class);
                    assert request != null;
                    if (request.getHostUid().equals(ID)) {
                        sum++;
                      //  Toast.makeText(HostMainActivity.this,  "You have a new request!", Toast.LENGTH_SHORT).show();
                    }

                }
                if (sum == 0) {
                    requestNumTxt.setText("");
                    requestNumTxt.setVisibility(View.GONE);
                    requestNumTxt.setBackgroundColor(0x988E8F);
                    btnRequests.setBackgroundColor(0x988E8F);
                }
                else {
                    requestNumTxt.setVisibility(View.VISIBLE);
                    requestNumTxt.setText(" "+sum+" ");
                    requestNumTxt.setBackgroundColor(0xff99cc00);
                    btnRequests.setBackgroundColor(0xff99cc00);
                }
                sum = 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
            //.........................................................................................


        //Set past button
        past = findViewById(R.id.past_btn);
        past.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!history) {
                    history = true;
                    recyclerView.setAdapter(pastAdapter);
                    past.setText("my meals");
                    historytxt.setText("History");
                }
                else {
                    history = false;
                    recyclerView.setAdapter(hostAdapter);
                    past.setText("history");
                    historytxt.setText("My Meals");

                }
            }
        });

        //Requests button............................................
        btnRequests = findViewById(R.id.btnRequests);
        requestNumTxt = findViewById(R.id.request_txt);
        Context context;
        btnRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(requestNumTxt.getText().equals(""))
                    Toast.makeText(HostMainActivity.this,"You have no requests!",Toast.LENGTH_SHORT).show();
                else {
                    AlertDialog.Builder requestOptions = new AlertDialog.Builder(HostMainActivity.this);
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View contactPopuoView = inflater.inflate(R.layout.requests_list, null);
                    AlertDialog options;

                    DatabaseReference referenceR;


                    //Requests List: ...........................................................................
                    RecyclerView recyclerView = contactPopuoView.findViewById(R.id.requestsList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(contactPopuoView.getContext()));
                    referenceR = FirebaseDatabase.getInstance().getReference("Requests");
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(contactPopuoView.getContext()));
                    ArrayList<Request> requestList = new ArrayList<>();

                    //Set request adapter..............................................
                    RequestsAdapter requestAdapter = new RequestsAdapter(contactPopuoView.getContext(), requestList, R.drawable.google);
                    recyclerView.setAdapter(requestAdapter);

                    referenceR.addValueEventListener(new ValueEventListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            requestList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Request request = dataSnapshot.getValue(Request.class);
                                assert request != null;
                                if (request.getHostUid().equals(ID))
                                    requestList.add(request);
                            }
                            if (requestList.size() == 0) {
                                startActivity(new Intent(contactPopuoView.getContext(), HostMainActivity.class));
                            }
                            requestAdapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    requestOptions.setView(contactPopuoView);
                    options = requestOptions.create();
                    options.show();
                }
                //.........................................................................................




            }
        });

//.............................................................................................................................
        //Set search button
        search = findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg=Dinner.check_date_time(text_date.getText().toString(),"23:59",history);
                if(!msg.equals("accept")){
                    text_date.setText(date[0]);
                    Toast.makeText(HostMainActivity.this,msg,Toast.LENGTH_SHORT).show();
                }
                else {

                    referenceD.addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            refreshLists(snapshot);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }



            }
        });


        //logout .................................................
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("remember", "false");
                editor.apply();
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(HostMainActivity.this,"Logged out!",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HostMainActivity.this, StartActivity.class));
                finish();
            }
        });
        //........................................................
        //new meal............................................
        new_meal = findViewById(R.id.new_meal);
        new_meal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HostMainActivity.this, SubmitDinnerActivity.class));
            }
        });
        //........................................................
        //edit profile............................................
        editprofile = findViewById(R.id.edit_profile);
        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HostMainActivity.this, EditProfileActivity.class));
            }
        });
        //........................................................



        //........................................................
        //Set name and rating text
        final TextView fullname_text = (TextView) findViewById(R.id.name);
        reference.child(ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User profile = snapshot.getValue(User.class);
                if (profile != null) {
                    String fullname = profile.getFullName();
                    fullname_text.setText(fullname);
                    int rates=profile.getRates();
                    if(rates==0)
                        rate_text = "No rates yet";
                    else if(rates == 1)
                        rate_text = (int)(profile.getRating()*20)+"% rating, (1 rate)";
                    else
                        rate_text=(int)(profile.getRating()*20)+"% rating, ("+rates+" rates)";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void refreshLists(DataSnapshot snapshot) {
        dinnerList.clear();
        pastlist.clear();
        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
            Dinner dinner = dataSnapshot.getValue(Dinner.class);
            if(dinner.getHostUid().equals(ID) && (kosher_text.equals("All")||kosher_text.equals(dinner.getKosher()))) {
                if (Dinner.isRelevant(dinner, text_date.getText().toString(), false))
                    dinnerList.add(dinner);
                else if (Dinner.isRelevant(dinner, text_date.getText().toString(), true))
                    pastlist.add(dinner);
            }
        }
        dinnerList = sortDinnersByDate(dinnerList, 1);
        hostAdapter.notifyDataSetChanged();
        pastlist = sortDinnersByDate(pastlist, -1);
        pastAdapter.notifyDataSetChanged();

    }

    public ArrayList<Dinner> sortDinnersByDate(ArrayList<Dinner> list, int flip)
    {
        Collections.sort(list, new Comparator<Dinner>() {
            @Override
            public int compare(Dinner dinner, Dinner t1) {
                return flip*dinner.compareTo(t1);
            }
        });
        return list;
    }
    //Press twice "back" for exit .............................................................
    @Override
    public void onBackPressed() {
        if (backPressed) {
            super.onBackPressed();
            return;
        }
        Toast.makeText(this,"Press 'back' again to exit", Toast.LENGTH_SHORT).show();
        backPressed = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backPressed = false;
            }
        }, 2000);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        kosher_text = parent.getItemAtPosition(position).toString();
        ((TextView)parent.getChildAt(0)).setTextColor(Color.BLACK);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    //..........................................................................................
}