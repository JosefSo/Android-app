package com.example.famileat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import classes.Dinner;
import classes.User;

public class StartActivity extends AppCompatActivity {
    private EditText text_email;
    private EditText text_password;
    private Button login;
    private TextView register, forgotPass;
    private DatabaseReference reference;
    private String ID;
    private FirebaseAuth auth;

    private boolean backPressed = false;

    private CheckBox rememberMe;
    public static final String SHARED_PREFS = "sharedPrefs";

//    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;
    ImageView google_b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        text_email = findViewById(R.id.email);
        text_password = findViewById(R.id.password);
        login = findViewById(R.id.signin);

        rememberMe = findViewById(R.id.rememberMe);

        auth = FirebaseAuth.getInstance();
        //Google Sign in: .........................................
        google_b = findViewById(R.id.googleb);
        GoogleSignInOptions  gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this,gso);
        // ........................................................

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email= text_email.getText().toString();
                String password= text_password.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(StartActivity.this, "Please enter email.", Toast.LENGTH_SHORT).show();
                    text_email.requestFocus();
                }
                else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(StartActivity.this, "Please enter valid email.", Toast.LENGTH_SHORT).show();
                    text_email.requestFocus();
                }
                else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(StartActivity.this, "Please enter password.", Toast.LENGTH_SHORT).show();
                    text_password.requestFocus();
                }
                else if (password.length() < 6) {
                    Toast.makeText(StartActivity.this, "Please enter at least 6 characters.", Toast.LENGTH_SHORT).show();
                    text_password.requestFocus();
                }
                else {
                    loginUser(email, password);
                }
            }
        });
        register = (TextView) findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent n = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(n);
            }
        });

        forgotPass = (TextView) findViewById(R.id.forgotPass);
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent n = new Intent(getApplicationContext(), ForgotPassActivity.class);
                startActivity(n);
            }
        });

        //remember me: ................................................................

        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String rememberBox = preferences.getString("remember", "");
        if (rememberBox.equals("true")) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance().getReference("Users");
            ID = user.getUid();
            reference.child(ID).addListenerForSingleValueEvent(new ValueEventListener() {


                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User profile = snapshot.getValue(User.class);
                    if (profile != null) {
                        if (profile.getType().equals("Host"))
                            startActivity(new Intent(StartActivity.this, HostMainActivity.class));
                        else
                            startActivity(new Intent(StartActivity.this, GuestMainActivity.class));
                        Toast.makeText(StartActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    }
                }

            });
        }

        //set remember my
        rememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    //Toast.makeText(StartActivity.this,"Checked", Toast.LENGTH_SHORT).show();
                }
                else if (!buttonView.isChecked()) {
                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    //Toast.makeText(StartActivity.this,"Unchecked", Toast.LENGTH_SHORT).show();

                }
            }
        });
        //......................................................................................

// Google ...................................................................
        google_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    void signIn() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, 1234);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1234){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Intent intent = new Intent(getApplicationContext(), HostMainActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(StartActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            } catch (ApiException e) {
                System.out.println("expt: " + e);
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }
//.............................................................................................


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
    //..........................................................................................

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user.isEmailVerified()) {
                        reference = FirebaseDatabase.getInstance().getReference("Users");
                        ID = user.getUid();
                        reference.child(ID).addListenerForSingleValueEvent(new ValueEventListener() {


                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User profile = snapshot.getValue(User.class);
                                if (profile != null) {
                                    if (profile.getType().equals("Host"))
                                        startActivity(new Intent(StartActivity.this, HostMainActivity.class));
                                    else
                                        startActivity(new Intent(StartActivity.this, GuestMainActivity.class));
                                    Toast.makeText(StartActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }

                        });
                    }
                    else {
                        user.sendEmailVerification();
                        Toast.makeText(StartActivity.this, "Check your email to verify  ", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(StartActivity.this, "Wrong email or password!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}