package com.example.myapplication.ui.screens;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.database.Database;
import com.example.myapplication.database.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SignIn extends AppCompatActivity {
    Database db;
    EditText email, password;
    TextView forget;
    Button login, sign_up;
    Switch show_hide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        String TAG = "Deneme";
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        db = new Database();
        FirebaseMessaging.getInstance().subscribeToTopic("match");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = token;
                        Log.d(TAG, msg);
                    }
                });
        boolean isCreated = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getBoolean("Created", false);
        if (db.isSigned() && isCreated){
            Intent intent = new Intent(SignIn.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        forget = findViewById(R.id.tv_pass_change);

        login = findViewById(R.id.btn_login);
        sign_up = findViewById(R.id.btn_sign);
        show_hide = findViewById(R.id.show_hide);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String entered_email = email.getText().toString();
                String entered_password = password.getText().toString();
                try{
                    entered_email = email.getText().toString();
                    entered_password = password.getText().toString();
                    db.emailPasswordAuthentication(entered_email, entered_password,
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    db.isUserCreated(new OnSuccessListener<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean aBoolean) {
                                            if (aBoolean){
                                                Toast.makeText(getApplicationContext(), s,
                                                        Toast.LENGTH_SHORT).show();
                                                getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit().putBoolean("Created", true).apply();
                                                Intent intent = new Intent(SignIn.this,
                                                        MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                            else{
                                                db.isEmailVerified(new OnSuccessListener<Boolean>() {
                                                    @Override
                                                    public void onSuccess(Boolean aBoolean) {
                                                        if(aBoolean){
                                                            Toast.makeText(getApplicationContext(), s,
                                                                    Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(SignIn.this,
                                                                    CreateProfile.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                        else{
                                                            Toast.makeText(SignIn.this,
                                                                    "Lütfen emailinizi onaylayın.", Toast.LENGTH_SHORT).show();
                                                            db.signOut();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Lütfen email ve şifrenizi girin.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(intent);
            }
        });

        show_hide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                    password.setInputType(InputType.TYPE_CLASS_TEXT);
                else
                    password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChangePassword.class);
                startActivity(intent);
            }
        });
    }


    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

}
