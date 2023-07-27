package adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famileat.ChatActivity;
import com.example.famileat.HostMainActivity;
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

public class GuestMyAdapter extends RecyclerView.Adapter<GuestMyAdapter.MyViewHolder> {

    Context context;

    ArrayList<Dinner> list;

    int proImage, rates;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String rate_text;

    public GuestMyAdapter(Context context, ArrayList<Dinner> list, int proImage) {
        this.context = context;
        this.list = list;
        this.proImage = proImage;
    }

    @NonNull
    @Override
    public GuestMyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.my_dinner_view_guest, parent, false);

        return new GuestMyAdapter.MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull GuestMyAdapter.MyViewHolder holder, int position) {
        Dinner dinner = list.get(position);
        String currUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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


        //Set chat button
        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatIntent = new Intent(context.getApplicationContext(), ChatActivity.class);
                chatIntent.putExtra("Did", dinner.getID());
                chatIntent.putExtra("State", "Alive");
                context.startActivity(chatIntent);
            }
        });
        //Set exit button
        holder.exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dinner newdinner = Dinner.removeGuest(dinner, currUid);
                if (newdinner != null) {
                    DatabaseReference dinnerReference = FirebaseDatabase.getInstance().getReference().child("Dinners").child(newdinner.getID());
                    dinnerReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            dinnerReference.setValue(newdinner);
                            // Toast.makeText(context.getApplicationContext(),  "You are out of "+newdinner.getTitle(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            System.out.println("Failed exit the dinner.");
                        }
                    });
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
        Button exit, chat;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            chat = itemView.findViewById(R.id.chatbtn);
            exit = itemView.findViewById(R.id.exit);
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