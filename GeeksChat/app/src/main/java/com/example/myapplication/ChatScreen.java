package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.adapters.ChatAdapter;
import com.example.myapplication.databinding.ActivityChatScreenBinding;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.Users;
import com.example.myapplication.utilities.Constants;
import com.example.myapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        binding = ActivityChatScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessage();

    }

    private void init()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessageList,preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatting.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();

    }

    private void sendMessage()
    {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiveUser.id);
        message.put(Constants.KEY_MESSAGE, binding.typedMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_CHAT).add(message);
        binding.typedMessage.setText(null);
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
                .orderBy(Constants.KEY_TIMESTAMP)
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
    };
    private void loadReceiverDetails()
    {
        receiveUser = (Users)getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.chatUser.setText(receiveUser.Name);
    }

    private void setListeners()
    {
        binding.backimage.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),UserPage.class)));
        binding.layoutsend.setOnClickListener(v -> sendMessage());
    }

    private String getReadableDate (Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}
