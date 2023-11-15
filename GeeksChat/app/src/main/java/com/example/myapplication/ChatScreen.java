package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.adapters.ChatAdapter;
import com.example.myapplication.databinding.ActivityChatScreenBinding;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.Users;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatScreen extends AppCompatActivity {

    private ActivityChatScreenBinding binding;
    private Users receiveUser;
    private List<ChatMessage> chatMessageList;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    private String ConversationId = null;

    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private String encodedImage;





    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        binding = ActivityChatScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        binding.imagerecord.setVisibility(View.VISIBLE);
        binding.sendImage.setVisibility(View.VISIBLE);
        binding.typedMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Toggle button visibility based on message input
               
                if (TextUtils.isEmpty(s)) {
                    binding.imagesend.setVisibility(View.GONE);
                    binding.imagerecord.setVisibility(View.VISIBLE);
                    binding.sendImage.setVisibility(View.VISIBLE);
                } else {
                    binding.imagesend.setVisibility(View.VISIBLE);
                    binding.imagerecord.setVisibility(View.GONE);
                    binding.sendImage.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        loadReceiverDetails();
        init();
        listenMessage();



    }

    private void init()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessageList,getBitmapFromEncodedString(receiveUser.image),preferenceManager.getString(Constants.KEY_USER_ID),this
        );
        binding.chatting.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();

    }

    private Bitmap getBitmapFromEncodedString(String encodedImage)
    {
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
    private Bitmap getUserImage(String encodedImage)
    {
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleRatio = Math.min((float) maxWidth / width, (float) maxHeight / height);

        int newWidth = Math.round(width * scaleRatio);
        int newHeight = Math.round(height * scaleRatio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private void sendMessage()
    {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiveUser.id);
        message.put(Constants.KEY_MESSAGE, binding.typedMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_CHAT).add(message);
        if(ConversationId != null)
        {
            updateConversion(binding.typedMessage.getText().toString());
        }
        else
        {
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiveUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiveUser.Name);
            conversion.put(Constants.KEY_RECEIVER_Email,receiveUser.Email);
            conversion.put(Constants.KEY_RECEIVER_NUMBER,receiveUser.PhoneNumber);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiveUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.typedMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }

        binding.typedMessage.setText(null);
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
                            binding.Imageselected.setImageBitmap(bitmap);
                            binding.sendImage.setVisibility(View.GONE);
                            binding.imagerecord.setVisibility(View.GONE);

                            encodedImage = encodedImage(bitmap);
                            binding.imagesend.setVisibility(View.VISIBLE);



                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Firestore operation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private String encodedImage(Bitmap bitmap)
    {
        int previewWith = 450;
        int previewHeight = 400;
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWith, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    public void listenMessage()
    {
        database.collection(Constants.KEY_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiveUser.id)
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiveUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            // Handle the error
            return;
        }
        if (value != null) {
            int count = chatMessageList.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDate(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    chatMessageList.add(chatMessage);

                    if (chatMessage.isRead) {
                        chatAdapter.notifyDataSetChanged(); // Notify adapter to reflect the changes
                    }

                }
            }
            Collections.sort(chatMessageList, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if(count == 0)
            {
                chatAdapter.notifyDataSetChanged();
            }
            else
            {
                chatAdapter.notifyItemRangeInserted(chatMessageList.size(),chatMessageList.size());
                binding.chatting.smoothScrollToPosition(chatMessageList.size() - 1);
            }
            binding.chatting.setVisibility(View.VISIBLE);
        }
        binding.progesschat.setVisibility(View.GONE);
        if (ConversationId == null)
        {
            checkforConversion();
        }
    };
    private void loadReceiverDetails()
    {
        receiveUser = (Users)getIntent().getSerializableExtra(Constants.KEY_USER);

        binding.chatUser.setText(receiveUser.Surname);
        binding.chatUser.setText(receiveUser.Name);
    }

    private void setListeners()
    {
        binding.backimage.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),UserPage.class)));
        binding.layoutsend.setOnClickListener(v -> sendMessage());
        binding.infoimage.setOnClickListener(v -> openContactInfo());
        binding.sendImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });


    }


    private String getReadableDate (Date date){

        long currentTimeMillis = System.currentTimeMillis();
        long timeDifferenceMillis = currentTimeMillis - date.getTime();

        if(timeDifferenceMillis <= 24 * 60 * 60 * 1000)
        {
            return "Today, "+ new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
        }
        else if(timeDifferenceMillis > 24 * 60 * 60 * 1000 && timeDifferenceMillis <= 48 * 60 * 60 * 1000)
        {
            return "Yesterday, " + new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
        }
        else
        {
            return new SimpleDateFormat("MMMM dd, - hh:mm a", Locale.getDefault()).format(date);
        }

    }

    private void addConversion(HashMap<String,Object> conversion)
    {
        database.collection(Constants.KEY_CONVERSATION).add(conversion)
                .addOnSuccessListener(documentReference -> ConversationId = documentReference.getId());
    }


   private void updateConversion(String message)
   {
       DocumentReference documentReference =
               database.collection(Constants.KEY_CONVERSATION).document(ConversationId);
       documentReference.update(Constants.KEY_LAST_MESSAGE,message,Constants.KEY_TIMESTAMP,new Date());
   }
    private void checkforConversion()
    {
        if(chatMessageList.size() != 0)
        {
            checkForConversationRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiveUser.id
            );
            checkForConversationRemotely(
                    receiveUser.id, preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void checkForConversationRemotely(String senderId,String receiverId)
    {
       database.collection(Constants.KEY_CONVERSATION)
               .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
               .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
               .get().addOnCompleteListener(conversationCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0)

        {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            ConversationId = documentSnapshot.getId();
        }

    };

    public void openContactInfo()
    {
        Intent intent = new Intent(getApplicationContext(), ContactInfo.class);
        intent.putExtra(Constants.KEY_USER, receiveUser); // Pass the user details
        startActivity(intent);
    }
}
