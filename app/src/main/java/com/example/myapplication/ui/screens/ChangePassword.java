package com.example.myapplication.ui.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.database.Database;


public class ChangePassword extends AppCompatActivity {
    Database db = new Database();
    Button reset;
    EditText mail;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_change);

        reset = findViewById(R.id.btn_send_email);

        mail = findViewById(R.id.et_email);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    db.changePassword(mail.getText().toString(), getApplicationContext());
                }
                catch(Exception e){
                    Toast.makeText(getApplicationContext(), "Kayıtlı olduğunuz maili doğru girdiğinize emin olun.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}