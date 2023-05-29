package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.myapplication.database.Database;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.ui.HomeFragment;
import com.example.myapplication.ui.MyProfileFragment;
import com.example.myapplication.ui.NotificationsFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.ui.screens.SignIn;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                CharSequence title = item.getTitle();
                if ("Ana Sayfa".equals(title)) {
                    replaceFragment(new HomeFragment());
                    return true;
                } else if ("Bildirimler".equals(title)) {
                    replaceFragment(new NotificationsFragment());
                    return true;
                } else if ("Profilim".equals(title)) {
                    replaceFragment(new MyProfileFragment());
                    return true;
                } else if ("Çıkış Yap".equals(title)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Çıkış Yap");
                    builder.setMessage("Çıkış yapmak istediğinize emin misiniz?");
                    builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Database db = new Database();
                            db.signOut();
                            getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit().putBoolean("Created", false).apply();
                            Toast.makeText(getApplicationContext(), "Çıkış Yapıldı.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, SignIn.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    builder.setNegativeButton("Hayır", null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                return false;
            }
        });
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

}