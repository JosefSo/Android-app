package adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famileat.ChatActivity;
import com.example.famileat.EditDinnerActivity;
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
import java.util.List;

import classes.Dinner;
import classes.Request;
import classes.User;

public class PastHostAdapter extends RecyclerView.Adapter<PastHostAdapter.MyViewHolder> {

    Context context;
    ArrayList<Dinner> list;

    int proImage;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    public static String currUid;
    private AlertDialog.Builder dinnerOptions;
    //    private AlertDialog options;
    Bitmap bitmap;
    private List<String> acceptedNames;
    String participantstr = "";






    public PastHostAdapter(Context context, ArrayList<Dinner> list, int proImage, AlertDialog.Builder dinnerOptions) {
        this.context = context;
        this.list = list;
        this.proImage = proImage;
        this.currUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.dinnerOptions = dinnerOptions;
        this.acceptedNames = new ArrayList<String>();

        //this.options = options;

    }

    @NonNull
    @Override
    public PastHostAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.dinner_view_host, parent, false);
        return new PastHostAdapter.MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PastHostAdapter.MyViewHolder holder, int position) {
        Dinner dinner = list.get(position);
        holder.title.setText(dinner.getTitle());
        holder.address.setText(dinner.getAddress());
        holder.date.setText(dinner.getDate());
        holder.time.setText(dinner.getTime());
        int av = Dinner.numOfAvailables(dinner);
        if (av == 0)
            holder.availables.setText("FULL");
        else
            holder.availables.setText(Integer.toString(av));
        holder.kosher.setText(dinner.getKosher());
        holder.dinnerImage.setImageBitmap(bitmap);

        //Set picture
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
        DatabaseReference ureference = FirebaseDatabase.getInstance().getReference("Users");
        final String[] Rid = new String[1];
        //Set meal button
        holder.mealbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currUid=dinner.getID();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View contactPopuoView = inflater.inflate(R.layout.host_past_options, null);
                AlertDialog options;
                TextView title, address, date, time, rating, kosher, details;
                Spinner accepted;
                ImageView dinnerImage;
                Button chatbtn, kick;


                title = contactPopuoView.findViewById(R.id.tvTitle);
                address = contactPopuoView.findViewById(R.id.tvAddress);
                date = contactPopuoView.findViewById(R.id.tvDate);
                time = contactPopuoView.findViewById(R.id.tvTime);
                rating = contactPopuoView.findViewById(R.id.tvGrade);
                kosher = contactPopuoView.findViewById(R.id.tvKosher);
                dinnerImage = contactPopuoView.findViewById(R.id.dinnerImage);
                details = contactPopuoView.findViewById(R.id.tvDetails);
                chatbtn = contactPopuoView.findViewById(R.id.chatbtn);
                accepted = contactPopuoView.findViewById(R.id.tvAccepted);


                //Set participants
                DatabaseReference referenceD = FirebaseDatabase.getInstance().getReference("Dinners").child(dinner.getID());
                referenceD.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Dinner d = snapshot.getValue(Dinner.class);
                        if (d != null) {
                            if (d.getAcceptedUid().isEmpty()) {
                                String[] participants = {"no participant"};
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, participants);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                accepted.setAdapter(adapter);
                            } else {
                                ArrayList<String> participants = new ArrayList<String>();
                                //participants.add("view participants");
                                for (int i = 0; i < d.getAcceptedUid().size(); i++) {
                                    String id =d.getAcceptedUid().get(i);
                                    ureference.child(d.getAcceptedUid().get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            User profile = snapshot.getValue(User.class);
                                            if (profile != null) {
                                                String c = "Not Commended.";
                                                try {
                                                    for (String msg : d.getCommands()) {
                                                        if (msg.split("@")[0].equals(id))
                                                            c = msg.split("@")[1];
                                                    }
                                                }
                                                catch (Exception e){System.out.println(e);}
                                                participants.add(profile.getFullName()+": "+c);

                                            }

                                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, participants);
                                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            accepted.setAdapter(adapter);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError error){

                    }

                });

                title.setText(dinner.getTitle());
                address.setText(dinner.getAddress());
                date.setText(dinner.getDate());
                time.setText(dinner.getTime());
                rating.setText((dinner.getRating()*20)+" ("+dinner.getRatersUid().size()+" votes).");
                kosher.setText(dinner.getKosher());
                details.setText(dinner.getDetails());

                //set image
                storage = FirebaseStorage.getInstance();
                storageReference = storage.getReference("images/" + dinner.getPicture());
                try {
                    File file = File.createTempFile("temp", ".png");
                    storageReference.getFile(file).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            dinnerImage.setImageBitmap(bitmap);
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

                dinnerOptions.setView(contactPopuoView);
                options = dinnerOptions.create();
                options.show();

                //Set chat button
                chatbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent chatIntent = new Intent(context.getApplicationContext(), ChatActivity.class);
                        chatIntent.putExtra("Did",dinner.getID());
                        chatIntent.putExtra("State","Past");
                        context.startActivity(chatIntent);
                        options.cancel();
                    }
                });
                //show image
                dinnerImage.setOnClickListener((new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View contactPopuoView = inflater.inflate(R.layout.single_image, null);
                        AlertDialog options;
                        ImageView dinnerImage;
                        dinnerImage = contactPopuoView.findViewById(R.id.imageview);
                        dinnerImage.setImageBitmap(bitmap);
                        dinnerOptions.setView(contactPopuoView);
                        options = dinnerOptions.create();
                        options.show();
                    }
                }));

            }
        });





    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void createDialog() {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CardView mealbtn;
        TextView title, address, date, time, availables, kosher, details, photoname;
        ImageView dinnerImage;
        AlertDialog.Builder dinnerOptions;
        AlertDialog options;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mealbtn = itemView.findViewById(R.id.reqbtn);
            title = itemView.findViewById(R.id.tvTitle);
            address = itemView.findViewById(R.id.tvAddress);
            date = itemView.findViewById(R.id.tvDate);
            time = itemView.findViewById(R.id.tvTime);
            availables = itemView.findViewById(R.id.tvAvailables);
            kosher = itemView.findViewById(R.id.tvKosher);
            dinnerImage = itemView.findViewById(R.id.dinnerImage);


        }
    }
}
