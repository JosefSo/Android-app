package com.example.famileat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class HostRequestsActivity extends AppCompatActivity {
//    private Button logout, editprofile, new_meal, requests;
//    private FirebaseUser user;
//    private DatabaseReference reference;
//    private String ID;
//
//    private DatabaseReference referenceD,referenceR;
//    private RecyclerView recyclerView;
//    private RequestsAdapter requestAdapter;
//    ArrayList<Request> requestList;
//
//    TextView name,email;
//
//    private boolean backPressed = false;
//    private AlertDialog.Builder dialog_builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_dinner_view_guest);


        //set user and ID
//        user = FirebaseAuth.getInstance().getCurrentUser();
//        reference = FirebaseDatabase.getInstance().getReference("Users");
//        ID = user.getUid();

//        //Requests List: ...........................................................................
//        recyclerView = findViewById(R.id.requestsList);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        referenceR = FirebaseDatabase.getInstance().getReference("Requests");
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        requestList = new ArrayList<>();

        //Set request adapter..............................................
      //  dialog_builder = new AlertDialog.Builder(this);
  //      requestAdapter = new RequestsAdapter(this,requestList, R.drawable.google, dialog_builder);
//        recyclerView.setAdapter(requestAdapter);
//
//        referenceR.addValueEventListener(new ValueEventListener() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                requestList.clear();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                    Request request = dataSnapshot.getValue(Request.class);
//                    assert request != null;
//                    if(request.getHostUid().equals(ID))
//                        requestList.add(request);
//                }
//                requestAdapter.notifyDataSetChanged();
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        //.........................................................................................


    }
    //Press twice "back" for exit .............................................................
//    @Override
//    public void onBackPressed() {
//        if (backPressed) {
//            super.onBackPressed();
//            return;
//        }
//        Toast.makeText(this,"Press 'back' again to exit", Toast.LENGTH_SHORT).show();
//        backPressed = true;
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                backPressed = false;
//            }
//        }, 2000);
//    }
    //..........................................................................................
}