package classes;

import android.os.Build;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.famileat.RegisterActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.Date;


/**
 * class Request
 */
public class User {

    private String fullName, email, date, gender, type;
    private double rating;
    private int rates;

    public User() {
        this.rates = 0;
        this.rating = 0;

    }
    /**
     * constructor
     */
    public User(String fullName, String email, String date, String gender, String type) {
        this.fullName = fullName;
        this.email = email;
        this.date = date;
        this.gender = gender;
        this.type = type;
        this.rates = 0;
        this.rating = 0;
    }

    // ---- GET(ERS) & SET(ERS) ---- //
    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullname) {
        this.fullName = fullname;
    }

    public String getEmail() {
        return this.email;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getType() {
        return this.type;
    }

    public double getRating() {
        return this.rating;
    }

    public int getRates() {
        return this.rates;
    }
    // ---- END OF GET(ERS) & SET(ERS) ---- //


    /**
     * check full Name of User (need be correct)
     *
     * @param  fullName    full name of user
     * @return         (String) accept/not
     */
    public static String check_fullName(String fullName) {
        if (TextUtils.isEmpty(fullName))
            return "Please enter full name.";
        for (int i = 0; i < fullName.length(); i++) {
            if (fullName.charAt(i)=='\n')
                return "Full name not valid!";
        }
        return "accept";
    }

    /**
     * check if email of User is valid (need be correct)
     *
     * @param  email    email of user
     * @return         (String) accept/not
     */
    public static String check_email(String email) {
        if (TextUtils.isEmpty(email))
            return "Please enter email.";
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return "Please enter valid email.";
        return "accept";
    }

    /**
     * check if password of User is valid (need be correct)
     *
     * @param  password    password of user
     * @return         (String) accept/not
     */
    public static String check_pass(String password) {
        if (TextUtils.isEmpty(password))
            return "Please enter password.";
        if (password.length() < 6)
            return "Please enter at least 6 characters.";
        return "accept";
    }

    /**
     * check if user's date of birth is valid (need be correct)
     *
     * @param  date    date of user
     * @return         (String) accept/not
     */
    public static String check_date(String date) {
        int minage = 16;
        if (TextUtils.isEmpty(date))
            return "Please enter date (dd/mm/yyyy).";
        String[] splited = date.split("/");
        if (splited.length != 3)
            return "Please ender valid date (dd/mm/yyyy)";
        int day, month, year;
        try {
            day = Integer.parseInt(splited[0]);
            month = Integer.parseInt(splited[1]);
            year = Integer.parseInt(splited[2]);
        } catch (NumberFormatException nfe) {
            return "Date must be numbers (dd/mm/yyyy)";
        }
        int nowy = 2023, nowm = 1, nowd = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nowy = LocalDate.now().getYear();
            nowm = LocalDate.now().getMonth().getValue();
            nowd = LocalDate.now().getDayOfMonth();
        }
        if (nowy - 120 > year || year > nowy)
            return "Year not valid";
        if (month < 1 || month > 12)
            return "Month not valid";
        if (day < 1 || day > 31 || (day == 31 && (month == 4 || month == 6 || month == 9 || month == 11)) || (month == 2 && (day > 29 || (day == 29 && (year % 4 != 0 || (year % 100 == 0 && year % 400 != 0))))))
            return "Day not valid";
        if (year > nowy - minage || (year == nowy - minage && (month < nowm || (month == nowm && day < nowd))))
            return "Minimum age for using this app is " + minage + ".";
        return "accept";

    }
    /**
     * return full name by User ID (from database)
     *
     * @param  Uid    id of user
     * @return         (String) name
     */
    public static String getFullnameByUid(String Uid) {
        final String[] s = {""};
        DatabaseReference dinnerReference = FirebaseDatabase.getInstance().getReference().child("Users").child(Uid);
        dinnerReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                s[0] = (String)snapshot.child("fullName").getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        return s[0];
    }
    public static User rate(User user, double rate)
    {
        double rating= user.rating* user.rates;
        rating+=rate;
        user.rates++;
        user.rating=rating/ user.rates;
        return user;
    }


}