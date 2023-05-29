package com.example.myapplication.ui.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.database.Database;
import com.example.myapplication.database.User;
import com.example.myapplication.notificationservice.NotificationSender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class Profile extends AppCompatActivity {
    Database db = new Database();
    User user;
    ImageView profile;
    TextView fullname, state, department, grade;
    LinearLayout detail;
    TextView detailState, detailKm, detailDay, address;
    ImageButton phone, mail;
    Button sendMatch;
    String str_phone, str_mail;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        byte[] bytes = getIntent().getByteArrayExtra("userBytes");
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bis);
            user = (User) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            finish();
        }

        phone = findViewById(R.id.ib_tel);
        mail = findViewById(R.id.ib_email);
        fullname = findViewById(R.id.tv_full_name);
        profile = findViewById(R.id.iv_profile);
        state = findViewById(R.id.tv_state);
        department = findViewById(R.id.tv_lisans);
        grade = findViewById(R.id.tv_grade_change);
        detail = findViewById(R.id.state_information);
        detailState = findViewById(R.id.tv_detailed_state);
        detailKm = findViewById(R.id.tv_detailed_km);
        detailDay = findViewById(R.id.tv_detailed_day);
        address = findViewById(R.id.address);
        sendMatch = findViewById(R.id.send_match);

        fullname.setText(user.getFullName());
        state.setText(user.getState());
        department.setText(user.getDepartment());
        grade.setText(user.getGrade());
        if (user.getState().equals("Kalacak Ev/Oda Arıyor")){
            detailState.setText(user.getState());
            detailKm.setText(String.format("Aranan uzaklık (max): %s km", user.getKm()));
            detailDay.setText(String.format("Kalınacak gün: %s", user.getDay()));
            address.setVisibility(View.INVISIBLE);
            detail.setVisibility(View.VISIBLE);
            sendMatch.setVisibility(View.VISIBLE);
        }
        else if (user.getState().equals("Ev/Oda Arkadaşı Arıyor")){
            detailState.setText(user.getState());
            detailKm.setText(String.format("Kampüse uzaklık: %s km", user.getKm()));
            detailDay.setText(String.format("Müsait gün: %s", user.getDay()));
            address.setText(String.format("Adres: %s", user.getAddress()));
            address.setVisibility(View.VISIBLE);
            detail.setVisibility(View.VISIBLE);
            sendMatch.setVisibility(View.VISIBLE);
        }
        if(user.isPhoneStatue())    phone.setVisibility(View.VISIBLE);
        if(user.isMailStatue())     mail.setVisibility(View.VISIBLE);

        db.getImageUri(user.getProfilePhotoUrl(), new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profile);
            }
        });
        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("sms:" + str_phone));
                startActivity(Intent.createChooser(intent, "Bir uygulama seçiniz."));
            }
        });
        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + str_mail));
                startActivity(Intent.createChooser(intent, "Bir uygulama seçiniz."));
            }
        });
        sendMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationSender.pushNotification(user.getNotificationToken(), getApplicationContext());
                db.addNotification(user.getuID(), Profile.this);
            }
        });
    }
}

