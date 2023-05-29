package com.example.myapplication.ui.screens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.database.Database;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class SignUp extends AppCompatActivity {
    Database db = new Database();
    EditText email, password, passAgain;
    Button verification;
    Switch show_hide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        passAgain = findViewById(R.id.et_password_check);
        verification = findViewById(R.id.btn_verify);
        show_hide = findViewById(R.id.show_hide);

        verification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strEmail = email.getText().toString() + "@gmail.com";
                String strPass = password.getText().toString();
                String strPassAgain = passAgain.getText().toString();
                Context context = getApplicationContext();
                db.sendVerificationMail(strEmail, strPass, strPassAgain, new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
                        db.signOut();
                        Intent intent = new Intent(context, SignIn.class);
                        startActivity(intent);
                        finish();
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

    }

}
