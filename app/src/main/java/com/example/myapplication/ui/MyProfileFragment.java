package com.example.myapplication.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myapplication.database.Database;
import com.example.myapplication.database.User;
import com.example.myapplication.databinding.FragmentMyProfileBinding;
import com.example.myapplication.ui.screens.Update;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class MyProfileFragment extends Fragment {

    private FragmentMyProfileBinding binding;
    Database db = new Database();
    ImageButton update;
    ImageView profile;
    TextView email, fullName, phone, day, km, adr, state, grade, department, detail;
    LinearLayout stateInfo;
    User user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMyProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        profile = binding.ivProfile;
        email = binding.tvEmail;
        fullName = binding.tvFullName;
        phone = binding.tvTel;
        day = binding.tvDay;
        km = binding.tvKm;
        adr = binding.address;
        state = binding.tvState;
        grade = binding.tvGradeChange;
        department = binding.tvDepartment;
        detail = binding.tvDetailedState;
        stateInfo = binding.stateInformation;
        update = binding.ibUpdate;

        db.getCurrentUser(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User u) {
                user = u;
                db.getImageUri(user.getProfilePhotoUrl(), new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profile);
                        email.setText(user.getEmail());
                        fullName.setText(user.getFullName());
                        state.setText(user.getState());
                        department.setText(user.getDepartment());
                        grade.setText(user.getGrade());
                        detail.setText(user.getState());
                        day.setText("Gün sayısı: " + user.getDay());
                        km.setText("Maksimum uzaklık (km): " + user.getKm());
                        adr.setText("Adres: " + user.getAddress());
                        if (!user.getPhone().equals(""))
                            phone.setText(user.getPhone());
                        if (user.getState().equals("Kalacak Ev/Oda Arıyor")) {
                            stateInfo.setVisibility(View.VISIBLE);
                            adr.setVisibility(View.INVISIBLE);
                        } else if (user.getState().equals("Ev/Oda Arkadaşı Arıyor")) {
                            stateInfo.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                onDestroyView();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(user);
                    oos.flush();
                    byte[] bytes = bos.toByteArray();
                    Intent intent = new Intent(getActivity(), Update.class);
                    intent.putExtra("currentUser", bytes);
                    startActivity(intent);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        db.getCurrentUser(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User u) {
                user = u;
                db.getImageUri(user.getProfilePhotoUrl(), new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profile);
                        fullName.setText(user.getFullName());
                        state.setText(user.getState());
                        department.setText(user.getDepartment());
                        grade.setText(user.getGrade());
                        detail.setText(user.getState());
                        day.setText("Gün sayısı: " + user.getDay());
                        km.setText("Maksimum uzaklık (km): " + user.getKm());
                        adr.setText("Adres: " + user.getAddress());
                        if (!user.getPhone().isEmpty())
                            phone.setText(user.getPhone());
                        if (user.getState().equals("Kalacak Ev/Oda Arıyor")) {
                            adr.setVisibility(View.INVISIBLE);
                            stateInfo.setVisibility(View.VISIBLE);
                        } else if (user.getState().equals("Ev/Oda Arkadaşı Arıyor")) {
                            stateInfo.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                onDestroyView();
            }
        });
    }
}