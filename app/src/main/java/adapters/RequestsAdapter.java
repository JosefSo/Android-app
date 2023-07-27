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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Calendar;

import classes.Dinner;
import classes.Request;
import classes.User;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.MyViewHolder> {

    Context context;
    ArrayList<Request> list;


    int proImage;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    public static String currUid;
    private AlertDialog.Builder dialog;

    //    private AlertDialog options;
    Bitmap bitmap;






    public RequestsAdapter(Context context, ArrayList<Request> list, int proImage) {
        this.context = context;
        this.list = list;
        this.proImage = proImage;
        this.currUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.dialog = null;
        //this.options = options;

    }

    @NonNull
    @Override
    public RequestsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.host_requests_view, parent, false);
        return new RequestsAdapter.MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RequestsAdapter.MyViewHolder holder, int position) {
        //Set request details
        Request request = list.get(position);
        holder.date.setText(request.getDate());
        holder.time.setText(request.getTime());

        //Set dinner details
        final Dinner[] dinner = new Dinner[1];
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Dinners");;
        reference.child(request.getDinnerid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dinner[0] = snapshot.getValue(Dinner.class);
                if(dinner[0] !=null){
                    holder.title.setText("Request for: '"+dinner[0].getTitle()+"'");
                    holder.availables.setText(Integer.toString(Dinner.numOfAvailables(dinner[0])));
                    holder.dinnerImage.setImageBitmap(bitmap);

                    //Set dinner picture
                    storage = FirebaseStorage.getInstance();
                    storageReference = storage.getReference("images/" + dinner[0].getPicture());
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

                    final String[] Rid = new String[1];
                }
//                if (list.size()==0){
//                    Intent editIntent = new Intent(context.getApplicationContext(), HostMainActivity.class);
//                    context.startActivity(editIntent);
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Set guest details
        final User[] guest = new User[1];
        DatabaseReference ureference = FirebaseDatabase.getInstance().getReference("Users");
        ureference.child(request.getGuestUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                guest[0] = snapshot.getValue(User.class);
                if(guest[0] !=null){
                    final Calendar cal = Calendar.getInstance();
                    int month = cal.get(Calendar.MONTH)+1;
                    int year = cal.get(Calendar.YEAR);
                    int age = year-Integer.parseInt(guest[0].getDate().split("/")[2]);
                    if(month<Integer.parseInt(guest[0].getDate().split("/")[1]))
                        age--;
                    holder.requester.setText(guest[0].getFullName()+"\n"+guest[0].getGender()+", "+age+" years old.");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //set accept button
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dinner.acceptUser(dinner[0],request);
                FirebaseDatabase.getInstance().getReference("Dinners")
                        .child(dinner[0].getID()).setValue(dinner[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                              //  Toast.makeText(context.getApplicationContext(),  "user accepted!", Toast.LENGTH_SHORT).show();
//                                if (list.size()==0){
//                                    Intent editIntent = new Intent(context.getApplicationContext(), HostMainActivity.class);
//                                    context.startActivity(editIntent);
//                                }

                            }
                        });
            }
        });
        //set delete button
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dinner.cancelRequest(dinner[0],request.getGuestUid());
                FirebaseDatabase.getInstance().getReference("Dinners")
                        .child(dinner[0].getID()).setValue(dinner[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                            //    Toast.makeText(context.getApplicationContext(),  "request deleted!", Toast.LENGTH_SHORT).show();
//                                if (list.size()==0){
//                                    Intent editIntent = new Intent(context.getApplicationContext(), HostMainActivity.class);
//                                    context.startActivity(editIntent);
//                                }

                            }
                        });
            }
        });


        //Set meal button
     //   holder.mealbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                currUid=dinner.getID();
//                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                View contactPopuoView = inflater.inflate(R.layout.host_dinner_options, null);
//                AlertDialog options;
//                TextView title, address, date, time, availables, kosher, details, photoname;
//                ImageView dinnerImage;
//                Button editBtn,deleteBtn;
//
//                title = contactPopuoView.findViewById(R.id.tvTitle);
//                address = contactPopuoView.findViewById(R.id.tvAddress);
//                date = contactPopuoView.findViewById(R.id.tvDate);
//                time = contactPopuoView.findViewById(R.id.tvTime);
//                availables = contactPopuoView.findViewById(R.id.tvAvailables);
//                kosher = contactPopuoView.findViewById(R.id.tvKosher);
//                dinnerImage = contactPopuoView.findViewById(R.id.dinnerImage);
//                details = contactPopuoView.findViewById(R.id.tvDetails);
//                deleteBtn = contactPopuoView.findViewById(R.id.deleteDinner);
//                editBtn = contactPopuoView.findViewById(R.id.editDinner);
//
//                title.setText(dinner.getTitle());
//                address.setText(dinner.getAddress());
//                date.setText(dinner.getDate());
//                time.setText(dinner.getTime());
//                availables.setText(Integer.toString(Dinner.numOfAvailables(dinner)));
//                kosher.setText(dinner.getKosher());
//                details.setText(dinner.getDetails());
//
//                //set image
//                storage = FirebaseStorage.getInstance();
//                storageReference = storage.getReference("images/" + dinner.getPicture());
//                try {
//                    File file = File.createTempFile("temp", ".png");
//                    storageReference.getFile(file).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
//                            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//                            dinnerImage.setImageBitmap(bitmap);
//                            holder.dinnerImage.setImageBitmap(bitmap);
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//
//                            System.out.println("Failed");
//                        }
//                    });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                dialog.setView(contactPopuoView);
//                options = dialog.create();
//                options.show();
//
//                //Set edit button
//                editBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent editIntent = new Intent(context.getApplicationContext(), EditDinnerActivity.class);
//                        context.startActivity(editIntent);
//                        options.cancel();
//                    }
//                });
//
//                //Set delete button
//                deleteBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Dinner.deleteDinnerById(dinner.getID());
//                        Request.deleteRequstsByDinnerId(dinner.getID());
//                        options.cancel();
//                    }
//                });
//
//            }
//        });





    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void createDialog() {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CardView reqbtn;
        TextView title, time, date, requester, availables;
        Button accept,delete;
        ImageView dinnerImage;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            accept = itemView.findViewById(R.id.accept);
            delete = itemView.findViewById(R.id.delete);
            reqbtn = itemView.findViewById(R.id.reqbtn);
            title = itemView.findViewById(R.id.tvTitle);
            date = itemView.findViewById(R.id.tvDate);
            time = itemView.findViewById(R.id.tvTime);
            requester = itemView.findViewById(R.id.tvRequester);
            availables = itemView.findViewById(R.id.tvAvailables);
            dinnerImage = itemView.findViewById(R.id.dinnerImage);


        }
    }
}
