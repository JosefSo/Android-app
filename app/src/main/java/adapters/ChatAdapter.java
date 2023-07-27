package adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.List;

import classes.Dinner;
import classes.Request;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    Context context;

    List<String> list;

    int proImage;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    Bitmap bitmap;
    public ChatAdapter(Context context, List<String> list , int proImage) {
        this.context = context;
        this.list = list;
        this.proImage = proImage;
    }

    @NonNull
    @Override
    public ChatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.message_view,parent,false);
        return new ChatAdapter.MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String[] message = list.get(position).split("\n");
        holder.l_type.setText(message[0]);
        holder.r_type.setText(message[0]);
        holder.l_name.setText(message[2]);
        holder.r_name.setText(message[2]);
        holder.l_date.setText(message[1]);
        holder.r_date.setText(message[1]);
        String msg="";
        for (int i = 3; i < message.length; i++) {
            msg+=message[i]+"\n";
        }
        holder.l_message.setText(msg);
        holder.r_message.setText(msg);
        if(holder.l_type.getText().equals("Host"))
            holder.leftSide();
        else
            holder.rightSide();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView l_message, l_type, l_name, l_date,r_message, r_type, r_name, r_date;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            l_message = itemView.findViewById(R.id.l_msgtxt);
            l_name = itemView.findViewById(R.id.l_nametxt);
            l_type = itemView.findViewById(R.id.l_typetxt);
            l_date = itemView.findViewById(R.id.l_datetxt);
            r_message = itemView.findViewById(R.id.r_msgtxt);
            r_name = itemView.findViewById(R.id.r_nametxt);
            r_type = itemView.findViewById(R.id.r_typetxt);
            r_date = itemView.findViewById(R.id.r_datetxt);


        }
        private void leftSide(){
            l_message.setVisibility(View.VISIBLE);
            l_name.setVisibility(View.VISIBLE);
            l_type.setVisibility(View.VISIBLE);
            l_date.setVisibility(View.VISIBLE);
            r_message.setVisibility(View.GONE);
            r_name.setVisibility(View.GONE);
            r_type.setVisibility(View.GONE);
            r_date.setVisibility(View.GONE);
        }
        private void rightSide() {
            r_message.setVisibility(View.VISIBLE);
            r_name.setVisibility(View.VISIBLE);
            r_type.setVisibility(View.VISIBLE);
            r_date.setVisibility(View.VISIBLE);
            l_message.setVisibility(View.GONE);
            l_name.setVisibility(View.GONE);
            l_type.setVisibility(View.GONE);
            l_date.setVisibility(View.GONE);

        }
    }
}
