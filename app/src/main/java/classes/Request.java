package classes;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

/**
 * class Request
 */
public class Request {
    private String requestId, hostUid, guestUid, dinnerid,date, time;

    public Request(){

    }
    public Request(String requestId, String hostUid,String guestUid, String dinnerid){
        this.requestId=requestId;
        this.hostUid = hostUid;
        this.guestUid = guestUid;
        this.dinnerid = dinnerid;
        //set date
        this.date= getCurrDate();

        //set time
        this.time = getCurrTime();
    }
    // ---- GET(ERS) & SET(ERS) ---- //
    public String getRequestId() {return this.requestId;}
    public void setId(String ID){
        this.requestId=requestId;
    }
    public String getHostUid(){
        return this.hostUid;
    }
    public void setHostUid(String hostUid){
        this.hostUid=hostUid;
    }
    public String getGuestUid(){
        return this.guestUid;
    }
    public void setGuestUid(String guestUid){
        this.guestUid=guestUid;
    }
    public String getDinnerid(){
        return this.dinnerid;
    }
    public void setDinnerid(String dinnerid){
        this.dinnerid=dinnerid;
    }
    public String getDate(){
        return this.date;
    }
    public void setDate(String date){
        this.date=date;
    }
    public String getTime(){
        return this.time;
    }
    public void setTime(String time){
        this.time=time;
    }
    // ---- END OF GET(ERS) & SET(ERS) ---- //



    /**
     * returns Current Date
     *
     * @return         (String) date
     */
    public static String getCurrDate(){
        final Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH)+1;
        int year = cal.get(Calendar.YEAR);
        return day+"/"+month+"/"+year;
    }
    /**
     * returns Current Time
     *
     * @return         (String) time
     */
    public static String getCurrTime(){
        String time;
        final Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        time = hour + ":" + minute;
        if (time.length() < 5) {
            if (hour<10)
                time = "0" + time;
            if (minute <10){
                String[] spl = time.split(":");
                time = spl[0] + ":0" + spl[1];
            }

        }
        return time;
    }
    /**
     * delete specific Request (from FirebaseDatabase) By (Dinner Id And Guest Id)
     *
     * @param  dinID     dinner ID
     * @param  guestID    quest ID
     */
    public static void deleteRequstByDinnerIdAndGuestId(String dinID,String guestID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Requests");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Request request = dataSnapshot.getValue(Request.class);
                    if (request.dinnerid.equals(dinID) && request.guestUid.equals(guestID)) {
                        FirebaseDatabase.getInstance().getReference().child("Requests").child(request.requestId).removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * delete all Requests (from FirebaseDatabase) By (Dinner Id)
     *
     * @param  dinID     dinner ID
     * @param  guestID    quest ID
     */
    public static void deleteRequstsByDinnerId(String dinID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Requests");;
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Request request = dataSnapshot.getValue(Request.class);
                    if(request.dinnerid.equals(dinID)){
                        FirebaseDatabase.getInstance().getReference().child("Requests").child(request.requestId).removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}


