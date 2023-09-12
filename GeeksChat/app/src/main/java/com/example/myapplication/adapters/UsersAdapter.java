package com.example.myapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.ChatScreen;
import com.example.myapplication.R;
import com.example.myapplication.listeners.UserListener;
import com.example.myapplication.models.Users;
import com.example.myapplication.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends BaseAdapter
{ private final List<Users> usersList;
    private final Context context;
    private final UserListener userListener;

    public UsersAdapter(Context context, ArrayList<Users> usersList,UserListener userListener) {
        this.context = context;
        this.usersList = usersList;
        this.userListener = userListener;
    }

    @Override
    public int getCount() {
        return usersList.size();
    }

    @Override
    public Object getItem(int position) {
        return usersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user_container_view, parent, false);
            holder = new ViewHolder();
            Users user = usersList.get(position);

            convertView.setOnClickListener(v ->{
                Intent intent = new Intent(context, ChatScreen.class);
                intent.putExtra(Constants.KEY_USER,user);
                context.startActivity(intent);
            });
            holder.nameTextView = convertView.findViewById(R.id.textName);
            holder.emailTextView = convertView.findViewById(R.id.textEmail);

            holder.nameTextView.setText(user.Name);
            holder.emailTextView.setText(user.Email);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }




        return convertView;
    }

    public void updateData(List<Users> newUsers) {
        usersList.addAll(newUsers);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
    }
}