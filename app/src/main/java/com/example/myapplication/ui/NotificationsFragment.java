package com.example.myapplication.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.NotificationsAdapter;
import com.example.myapplication.database.Database;
import com.example.myapplication.database.Notifications;
import com.example.myapplication.databinding.FragmentNotificationsBinding;
import com.example.myapplication.notificationservice.NotificationSender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private List<Notifications> all;
    RecyclerView mRecyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mRecyclerView = binding.recyclerView;

        Database db = new Database();
        db.getAllNotifications(new OnSuccessListener<List<Notifications>>(){
            @Override
            public void onSuccess(List<Notifications> notifications) {
                Collections.reverse(notifications);
                NotificationsAdapter adapter = new NotificationsAdapter(notifications);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                        RecyclerView.VERTICAL, false);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setLayoutManager(linearLayoutManager);
                mRecyclerView.setAdapter(adapter);
                mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                        View item = rv.findChildViewUnder(e.getX(), e.getY());
                        if(item != null) {
                            int position = rv.getChildAdapterPosition(item);
                            Notifications notification = notifications.get(position);
                            if(!notification.answered){
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("İsteği Yanıtla!");
                                builder.setMessage("Bu isteği onaylamak istiyor musunuz?");

                                builder.setPositiveButton("Kabul Et", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        NotificationSender.pushRejectNotification(notification.senderToken, getContext());
                                        db.addAcceptNotification(notification.senderID, getContext());
                                        db.setStateAfterMatch(notification.senderID);
                                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                        fragmentTransaction.detach(NotificationsFragment.this);
                                        fragmentTransaction.attach(NotificationsFragment.this);
                                        fragmentTransaction.commit();
                                    }
                                });

                                builder.setNegativeButton("Reddet", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        NotificationSender.pushRejectNotification(notification.senderToken, getContext());
                                        db.addRejectNotification(notification.senderID, getContext());
                                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                        fragmentTransaction.detach(NotificationsFragment.this);
                                        fragmentTransaction.attach(NotificationsFragment.this);
                                        fragmentTransaction.commit();
                                    }
                                });

                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "There is an error happened.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}