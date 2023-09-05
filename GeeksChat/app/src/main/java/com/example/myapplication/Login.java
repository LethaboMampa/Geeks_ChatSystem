package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.util.Util;

import java.util.HashMap;

public class Login extends AppCompatActivity {

    Button Lbutton;
    EditText LEmail, LPassword;
    ProgressBar progressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
        Lbutton = (Button) findViewById(R.id.Lbutton);
        LEmail = findViewById(R.id.LEmail);
        LPassword = findViewById(R.id.LPassword);
        progressBar = findViewById(R.id.progressBar3);
        preferenceManager = new PreferenceManager(getApplicationContext());

        if(preferenceManager.getBoolear(Constants.KEY_IS_SIGNED_IN))
        {
            startActivity(new Intent(getApplicationContext(),UserPage.class));
            finish();
        }

    }

    public void Registration(View view) {
        Intent intent = new Intent(Login.this, Registration.class);
        startActivity(intent);
    }

    public void ForgetPassword(View view) {
        Intent intent = new Intent(Login.this, Forget_Password.class);
        startActivity(intent);
    }

    public void setToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public Boolean isValidDetails() {
        if (LEmail.getText().toString().trim().isEmpty()) {
            setToast("Enter Email");
            return false;
        } else if (LPassword.getText().toString().trim().isEmpty()) {
            setToast("Enter Password");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(LEmail.getText().toString()).matches()) {
            setToast("Enter valid Email");
            return false;
        } else {
            return true;
        }
    }

    public void Login(View view) {
        if (isValidDetails()) {
            Signin();
        }
    }

    private void Signin() {

        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL, LEmail.getText().toString()).
                whereEqualTo(Constants.KEY_PASSWORD, LPassword.getText().toString()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        String name = documentSnapshot.getString(Constants.KEY_NAME);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME,name);

                        Intent intent = new Intent(getApplicationContext(), UserPage.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        setToast("Login Failed. Please try again");
                    }
                });
    }

    public void loading(Boolean loading) {
        if (loading) {
            Lbutton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Lbutton.setVisibility(View.VISIBLE);

        }
    }
}