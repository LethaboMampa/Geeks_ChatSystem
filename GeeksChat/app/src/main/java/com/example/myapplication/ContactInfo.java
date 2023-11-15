package com.example.myapplication;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.models.Users;
import com.example.myapplication.utilities.Constants;

public class ContactInfo extends AppCompatActivity {

    private TextView textName, textPhone, textEmail;
    private Users receiveUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_contact_info);
        setData();


    }

    public void setData()
    {
        Users user = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);

        try {


        if (user != null) {
            textName.setText(user.Name);
//            // Change this to the appropriate user's field (e.g., user.Phone)
//            textPhone.setText(user.PhoneNumber);
//            textEmail.setText(user.Email);


        } else {
            Toast.makeText(this, "User details not found", Toast.LENGTH_SHORT).show();
        }

    }catch(Exception e){
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
    }


    }
