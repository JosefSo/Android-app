package com.example.famileat;

import static com.example.famileat.StartActivity.SHARED_PREFS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.List;

import adapters.ChatAdapter;
import adapters.HostsAdapter;
import classes.Dinner;
import classes.User;

public class ChatActivity extends AppCompatActivity {
    private FirebaseUser user;
    private DatabaseReference referenceU;
    private String ID;
    private String Did, State;
    private TextView title,fullname_text;
    private EditText msgtxt;
    private Button logout,editprofile, clearchat;
    private AppCompatImageView sendbtn;
    private RecyclerView recyclerView;
    private AlertDialog.Builder dialog_builder;
    private ChatAdapter chatAdapter;
    List<String> messageList;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Set user and ID
        user = FirebaseAuth.getInstance().getCurrentUser();
        referenceU = FirebaseDatabase.getInstance().getReference("Users");
        ID = user.getUid();
        //Set title
        title = findViewById(R.id.title);
        //Set full name
        fullname_text = findViewById(R.id.name);
        referenceU.child(ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User profile = snapshot.getValue(User.class);
                if (profile != null) {
                    type=profile.getType();
                    if (type.equals("Guest"))
                        clearchat.setVisibility(View.GONE);
                    fullname_text.setText(profile.getFullName());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //set Did
        Bundle bundle = getIntent().getExtras();
        this.Did = (String) bundle.get("Did");
        this.State = (String) bundle.get("State");



        //Set logout .................................................
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("remember", "false");
                editor.apply();
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(ChatActivity.this,"Logged out!",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ChatActivity.this, StartActivity.class));
                finish();
            }
        });
        //........................................................

        //Set edit profile............................................
        editprofile = findViewById(R.id.edit_profile);
        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatActivity.this, EditProfileActivity.class));
            }
        });

        //Message List: ...........................................................................
        recyclerView = findViewById(R.id.chatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        //........................................................

        //Set chat adapter..............................................
        dialog_builder = new AlertDialog.Builder(this);
        chatAdapter = new ChatAdapter(ChatActivity.this,messageList, R.drawable.google);
        recyclerView.setAdapter(chatAdapter);

        DatabaseReference referenceD = FirebaseDatabase.getInstance().getReference("Dinners").child(Did);
        referenceD.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Dinner dinner = snapshot.getValue(Dinner.class);
                title.setText(dinner.getTitle());
                messageList.clear();
                messageList.addAll(dinner.getChat());
                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size()-1);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Set message edit text
        msgtxt = findViewById(R.id.message_input);

        //Set send button
        sendbtn = findViewById(R.id.btnSend);
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Dinners");
                reference.child(Did).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Dinner dinner = snapshot.getValue(Dinner.class);
                        if (dinner != null) {
                            String fullname = fullname_text.getText().toString();
                            String message = msgtxt.getText().toString();
                            Dinner newdinner = Dinner.sendGroupMessage(dinner,fullname,message,type);
                            FirebaseDatabase.getInstance().getReference("Dinners")
                                    .child(Did).setValue(newdinner).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            msgtxt.setText("");
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        //Set clear button
        clearchat = findViewById(R.id.clearbtn);
        clearchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msgtxt.setText("");
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Dinners");
                reference.child(Did).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Dinner dinner = snapshot.getValue(Dinner.class);
                        if (dinner != null) {
                            Dinner newdinner = Dinner.clearChat(dinner);
                            FirebaseDatabase.getInstance().getReference("Dinners")
                                    .child(Did).setValue(newdinner).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(ChatActivity.this,  "Chat cleared!", Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        //set disable for view only
        if(this.State.equals("Past")){
            clearchat.setVisibility(View.GONE);
            sendbtn.setVisibility(View.GONE);
            msgtxt.setVisibility(View.GONE);
        }






    }
}