package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityRegistrationBinding;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registration extends AppCompatActivity {

    private ActivityRegistrationBinding binding;

    EditText rName, rSurname, rEmail, rCallNumber, rPassword, rConfirmPassword;
    Button rButton;
    ProgressBar progressBar;

    String encodedImage;
    private static final int DEFAULT_PROFILE_IMAGE_RES = R.drawable.user;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

        rName = findViewById(R.id.RName);
        rSurname = findViewById(R.id.RSurname);
        rButton = findViewById(R.id.Rbutton);
        rCallNumber = findViewById(R.id.RCallNumber);
        rPassword = findViewById(R.id.RPassword);
        rConfirmPassword = findViewById(R.id.RConfirmPassword);
        rEmail = findViewById(R.id.REmail);
        progressBar = findViewById(R.id.progressBar2);



        preferenceManager = new PreferenceManager(getApplicationContext());


    }

    public void Register(View view) {
        if (Authonticate()) {
            Register();
        }

    }

    private void Register() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userEmail = rEmail.getText().toString();

        // Check if the email already exists in Firestore
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Email doesn't exist, proceed with registration
                        HashMap<String, Object> users = new HashMap<>();
                        users.put(Constants.KEY_NAME, rName.getText().toString());
                        users.put(Constants.KEY_SURNAME, rSurname.getText().toString());
                        users.put(Constants.KEY_EMAIL, userEmail);
                        users.put(Constants.KEY_PHONENUMBER, rCallNumber.getText().toString());
                        users.put(Constants.KEY_PASSWORD, rPassword.getText().toString());

                        if(encodedImage==null)
                        {
                            binding.ProfilePhoto.setImageResource(DEFAULT_PROFILE_IMAGE_RES);
                            encodedImage = encodedImage(BitmapFactory.decodeResource(getResources(), DEFAULT_PROFILE_IMAGE_RES));
                            users.put(Constants.KEY_IMAGE,encodedImage);

                        }
                        else {
                            users.put(Constants.KEY_IMAGE,encodedImage);
                        }

                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .add(users)
                                .addOnSuccessListener(documentReference -> {
                                    setToast("Registration Successful");
                                    loading(false);
                                    startActivity(new Intent(Registration.this,Login.class));

                                })
                                .addOnFailureListener(exception -> {
                                    loading(false);
                                    setToast("Registration failed: " + exception.getMessage());
                                });
                    } else {
                        // Email already exists, show an error message
                        loading(false);
                        setToast("User with this email already exists");
                    }
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    setToast("Error checking email existence: " + exception.getMessage());
                });
    }
    private String encodedImage(Bitmap bitmap)
    {
        int previewWith = 200;
        int previewHeight = 150;
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWith, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode()==RESULT_OK)
                {
                    if(result.getData() != null)
                    {
                        Uri imageUri = result.getData().getData();
                        try {

                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.ProfilePhoto.setImageBitmap(bitmap);
                            binding.ProfileText.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Firestore operation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        binding.ProfilePhoto.setImageResource(R.drawable.user);
                        encodedImage = encodedImage(BitmapFactory.decodeResource(getResources(), R.drawable.user));
                    }
                }
            }
    );
    public Boolean Authonticate()
    {
        if(binding.ProfilePhoto == null)
        {
            binding.ProfilePhoto.setImageResource(R.drawable.user);
            encodedImage = encodedImage(BitmapFactory.decodeResource(getResources(), R.drawable.user));
            return true;
        }
        if (rName.getText().toString().trim().isEmpty()) {
            setToast("Enter Name");
            return false;
        } else if (rSurname.getText().toString().trim().isEmpty()) {
            setToast("Enter Surname");
            return false;
        } else if (rEmail.getText().toString().trim().isEmpty()) {
            setToast("Enter Email");
            return false;
        } else if (rCallNumber.getText().toString().trim().isEmpty()) {
            setToast("Enter Phone Number");
            return false;
        } else if (rPassword.getText().toString().trim().isEmpty()) {
            setToast("Enter Password");
            return false;
        } else if (rConfirmPassword.getText().toString().trim().isEmpty()) {
            setToast("Confirm your Password");
            return false;
        } else if (!validateEmail(rEmail.getText().toString())) {
            setToast("Enter valid Email");
            return false;
        } else if (!(rPassword.getText().toString().trim().equals(rConfirmPassword.getText().toString().trim()))) {
            setToast("Password & Confirm Password must be the same");
            return false;
        } else {
            return true;
        }
    }
    public boolean validateEmail(String email) {
        // Define a regular expression pattern for the email format you want
        String emailPattern = "^[A-Za-z0-9+_.-]+@geeks4learning\\.com$";

        // Compile the pattern
        Pattern pattern = Pattern.compile(emailPattern);

        // Create a Matcher object
        Matcher matcher = pattern.matcher(email);

        // Check if the email matches the pattern
        return matcher.matches();
    }

    public void setToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void loading(Boolean loading) {
        if (loading) {
            rButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            rButton.setVisibility(View.VISIBLE);

        }

    }

    public void setListeners()
    {
        binding.LoginBackText.setOnClickListener(view ->
                onBackPressed());
        binding.ProfilePhoto.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }
}