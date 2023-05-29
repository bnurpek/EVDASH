package com.example.myapplication.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.database.Database;
import com.example.myapplication.database.Notifications;
import com.example.myapplication.notificationservice.NotificationSender;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<Notifications> dataset;
    Database db = new Database();
    int total_data;
    public NotificationsAdapter(List<Notifications> notifications) {
        dataset = notifications;
        total_data = dataset.size();
    }
    public static class NotificationsTypeViewHolder extends RecyclerView.ViewHolder {
        Button accept, reject;
        TextView title, body, answered;

        public NotificationsTypeViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_title);
            body = (TextView) itemView.findViewById(R.id.tv_body);
            answered = (TextView) itemView.findViewById(R.id.tv_answered);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_type, parent, false);
        return new NotificationsTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Notifications object = dataset.get(position);
        if (object != null) {
            ((NotificationsTypeViewHolder) holder).title.setText(object.title);
            ((NotificationsTypeViewHolder) holder).body.setText(object.body);
            if (object.answered && object.title.equals("Ev Arkadaşın Olmak İsteyen Biri Var!")) {
                ((NotificationsTypeViewHolder) holder).answered.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return total_data;
    }
}
