package com.example.myapplication.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.database.Database;
import com.example.myapplication.database.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<User> dataset;
    Database db = new Database();
    int total_data;
    public SearchResultAdapter(ArrayList<User> users) {
        dataset = users;
        total_data = dataset.size();
    }
    public static class SearchResultTypeViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView fullname, department, state;

        public SearchResultTypeViewHolder(View itemView) {
            super(itemView);
            fullname = (TextView) itemView.findViewById(R.id.tv_full_name);
            img = (ImageView) itemView.findViewById(R.id.iv_icon);
            department = (TextView) itemView.findViewById(R.id.tv_department);
            state = (TextView) itemView.findViewById(R.id.tv_state);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_type, parent, false);
        return new SearchResultTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        User object = dataset.get(position);
        if (object != null) {
            db.getImageUri(object.getProfilePhotoUrl(), new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if(uri != null)
                            Picasso.get()
                                    .load(uri)
                                    .into(((SearchResultTypeViewHolder) holder).img);
                    }
                });
            ((SearchResultTypeViewHolder) holder).fullname
                    .setText(object.getFullName());

            ((SearchResultTypeViewHolder) holder).department
                    .setText("Bölümü: "+object.getDepartment());

            ((SearchResultTypeViewHolder) holder).state.setText("Durumu: "+object.getState());
        }
    }

    @Override
    public int getItemCount() {
        return total_data;
    }
}
