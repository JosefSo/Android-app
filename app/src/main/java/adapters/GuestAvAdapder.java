package adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famileat.R;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import classes.Dinner;
import classes.Request;
import classes.User;

public class GuestAvAdapder extends RecyclerView.Adapter<GuestAvAdapder.MyViewHolder> {

    Context context;

    ArrayList<Dinner> list;

    int proImage, rates;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String rate_text;

    public GuestAvAdapder(Context context, ArrayList<Dinner> list , int proImage) {
        this.context = context;
        this.list = list;
        this.proImage = proImage;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.av_dinner_view_guest,parent,false);

        return new MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Dinner dinner = list.get(position);
        String currUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        holder.title.setText(dinner.getTitle());

        //Set host
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(dinner.getHostUid());
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User host = snapshot.getValue(User.class);
                holder.host.setText(host.getFullName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.address.setText(dinner.getAddress());
        holder.date.setText(dinner.getDate());
        holder.time.setText(dinner.getTime());
        holder.availables.setText(Integer.toString(Dinner.numOfAvailables(dinner)));
        holder.kosher.setText(dinner.getKosher());
        final String[] Rid = new String[1];

        //rate
        holder.host.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(dinner.getHostUid());
                userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User host = snapshot.getValue(User.class);
                        rates = host.getRates();
                        if (rates == 0)
                            rate_text = host.getFullName() + ": No rates yet";
                        else if (rates == 1)
                            rate_text = host.getFullName() + ": " + (int) (host.getRating() * 20) + "% rating, (1 rate)";
                        else
                            rate_text = host.getFullName() + ": " + (int) (host.getRating() * 20) + "% rating, (" + rates + " rates)";
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                if(!TextUtils.isEmpty(rate_text)) {
                    Toast.makeText(context.getApplicationContext(), rate_text, Toast.LENGTH_SHORT).show();

                }
                rate_text = "";




            }
        });

        //Set join button
        DatabaseReference referenceR = FirebaseDatabase.getInstance().getReference("Requests");
        referenceR.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(Dinner.isRequested(dinner,currUid))
                    holder.join.setText("Cancel request");
                else
                    holder.join.setText("send request");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.join.getText().equals("send request")) {
                        Dinner newdinner=Dinner.requestUser(dinner,currUid);
                        if (newdinner!=null) {
                            DatabaseReference reqReference = FirebaseDatabase.getInstance().getReference("Requests").push();
                            DatabaseReference dinnerReference = FirebaseDatabase.getInstance().getReference().child("Dinners").child(newdinner.getID());
                            Rid[0] = reqReference.getKey();
                            Request request = new Request(Rid[0],dinner.getHostUid(), currUid, dinner.getID());
                            reqReference.setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        holder.join.setText("Cancel request");
                                        dinnerReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                dinnerReference.setValue(newdinner);
                                               // Toast.makeText(context.getApplicationContext(),  "Request sent to the host of "+newdinner.getTitle(), Toast.LENGTH_SHORT).show();
                                                holder.join.setText("Cancel request");
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                System.out.println("Failed on send request. ");
                                            }
                                        });


                                    } else {
                                        System.out.println("Failed on send request. ");
                                    }
                                }
                            });
                        }


                }
                else {

                    Dinner newdinner=Dinner.cancelRequest(dinner,currUid);
                    if (newdinner!=null) {
                        //DatabaseReference reqReference = FirebaseDatabase.getInstance().getReference("Requests");
                        DatabaseReference dinnerReference = FirebaseDatabase.getInstance().getReference().child("Dinners").child(newdinner.getID());
                        holder.join.setText("Cancel request");
                        dinnerReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                              //  Toast.makeText(context.getApplicationContext(),  "Request canceled.", Toast.LENGTH_SHORT).show();
                                holder.join.setText("send request");
                                dinnerReference.setValue(newdinner);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                System.out.println("Failed on send request. ");
                            }
                        });
                    }



                }
            }
        });

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("images/" + dinner.getPicture());
        try {
            File file = File.createTempFile("temp", ".png");
            storageReference.getFile(file).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    holder.dinnerImage.setImageBitmap(bitmap);
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

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title, address, date, time, availables, kosher, host, photoname;
        ImageView dinnerImage;
        Button join;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            join = itemView.findViewById(R.id.join);
            title = itemView.findViewById(R.id.tvTitle);
            host = itemView.findViewById(R.id.tvHost);
            address = itemView.findViewById(R.id.tvAddress);
            date = itemView.findViewById(R.id.tvDate);
            time = itemView.findViewById(R.id.tvTime);
            availables = itemView.findViewById(R.id.tvAvailables);
            kosher = itemView.findViewById(R.id.tvKosher);
            dinnerImage = itemView.findViewById(R.id.dinnerImage);
        }
    }
}
