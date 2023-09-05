package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class UserPage extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    TextView uName;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_user_page);
        image = findViewById(R.id.logout);
        preferenceManager = new PreferenceManager(getApplicationContext());
        uName = findViewById(R.id.UName);
        loadUserDetails();
        getToken();
    }

    private void loadUserDetails()
    {
         uName.setText(preferenceManager.getString(Constants.KEY_NAME));
    }

    public void setToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void updateToken(String token)
    {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_TOKEN,token).addOnSuccessListener(unused -> setToast(""))
                .addOnFailureListener(e -> setToast("Unable to update token"));
    }

    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    public void SignOut()
    {
        setToast("Logging out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constants.KEY_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(),Login.class));
            finish();
        }).addOnFailureListener(e -> setToast("Unable to Logout"));
    }

    public void Logout(View view)
    {
        SignOut();
    }
}