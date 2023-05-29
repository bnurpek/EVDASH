package com.example.myapplication.ui.screens;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.database.Database;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class CreateProfile extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 103;
    public static final int GALLERY_PERM_CODE = 104;
    String currentPhotoPath;
    Uri currentImgUri;
    Bitmap currentImgBitmap;
    Database db = new Database();
    Button sign_up;
    ImageButton gallery, camera;
    ImageView profile;
    EditText name, surname, phone, day, km;
    TextView showDayHelp, address;
    Spinner state, grade, department;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        showDayHelp = findViewById(R.id.question_mark);

        sign_up = findViewById(R.id.btn_sign);
        gallery = findViewById(R.id.ib_file);
        camera = findViewById(R.id.ib_camera);

        profile = findViewById(R.id.iv_profile);
        name = findViewById(R.id.et_name);
        surname = findViewById(R.id.et_surname);
        phone = findViewById(R.id.et_phone);
        day = findViewById(R.id.et_day);
        km = findViewById(R.id.et_kmeters);
        address = findViewById(R.id.address);
        latitude = 41.028424044715415;
        longitude = 28.890713922958838;

        state = findViewById(R.id.spinner_state);
        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this,
                R.array.search_state_array, android.R.layout.simple_spinner_item);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        state.setAdapter(stateAdapter);

        state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).toString().equals("Aramıyor")){
                    day.setText("");
                    day.setEnabled(false);
                    km.setText("");
                    km.setEnabled(false);
                    address.setText("");
                    address.setEnabled(false);
                }
                else{
                    day.setEnabled(true);
                    km.setEnabled(true);
                    if(parent.getItemAtPosition(position).toString().equals("Ev/Oda Arkadaşı Arıyor"))
                        address.setEnabled(true);
                    else
                        address.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        grade = findViewById(R.id.spinner_edu);
        ArrayAdapter<CharSequence> classNumAdapter = ArrayAdapter.createFromResource(this,
                R.array.class_num_array, android.R.layout.simple_spinner_item);
        classNumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        grade.setAdapter(classNumAdapter);

        department = findViewById(R.id.spinner_dep);
        ArrayAdapter<CharSequence> depAdapter = ArrayAdapter.createFromResource(this,
                R.array.department_name_array, android.R.layout.simple_spinner_item);
        depAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        department.setAdapter(depAdapter);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPermissions(CAMERA_REQUEST_CODE);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPermissions(GALLERY_REQUEST_CODE);
            }
        });

        showDayHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Evde kalınacak/paylaşılacak gün sayısı.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_name = name.getText().toString();
                String str_surname = surname.getText().toString();
                String str_phone = phone.getText().toString().replaceAll(" ","");
                String str_department = department.getSelectedItem().toString();
                String str_grade = grade.getSelectedItem().toString();
                String str_state = state.getSelectedItem().toString();
                String str_day = day.getText().toString();
                String str_km = km.getText().toString();
                String str_adr = address.getText().toString();

                if(str_name.isEmpty() || str_surname.isEmpty() || (day.isEnabled() && str_day.isEmpty()) ||
                        (km.isEnabled() && str_km.isEmpty()) || (address.isEnabled() && str_adr.equals("Adres Eklemek İçin Tıkla"))){
                    Toast.makeText(getApplicationContext(), "Lütfen zorunlu alanları doldurunuz.",
                            Toast.LENGTH_SHORT).show();
                }
                else if(str_phone.length() > 0 && str_phone.length() < 10){
                    Toast.makeText(getApplicationContext(), "Telefon numaranızı doğru yazdığınıza emin olun.",
                            Toast.LENGTH_SHORT).show();
                }
                else if(currentImgUri != null) {
                    db.saveUserInfos(currentImgBitmap,str_name + " " + str_surname, str_phone,
                            str_department, str_grade, str_state, str_day, str_km, str_adr, false, false, latitude, longitude,
                            new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                            getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit().putBoolean("Created", true).apply();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }}, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                    Toast.makeText(CreateProfile.this, "Lütfen bir profil fotoğrafı seçin.",
                            Toast.LENGTH_SHORT).show();
            }
        });

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateProfile.this, SelectAddress.class);
                startActivityForResult(intent, 57);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        db.signOut();
    }

    private void verifyPermissions(int request_code){
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA};

        if(request_code == GALLERY_REQUEST_CODE){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    permissions[0]) == PackageManager.PERMISSION_GRANTED){
                Intent imageGallery = new Intent(Intent.ACTION_PICK);
                imageGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(imageGallery, GALLERY_REQUEST_CODE);
            }
            else{
                ActivityCompat.requestPermissions(this, permissions, GALLERY_PERM_CODE);
            }
        }
        else if(request_code == CAMERA_REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    permissions[0]) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    permissions[1]) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    permissions[2]) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    permissions[3]) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            }
            else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        CAMERA_PERM_CODE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions, grantResults);
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }
            else{
                Toast.makeText(this, "Kamera izni reddedildi.", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == GALLERY_PERM_CODE){
            Toast.makeText(this, "Dosya izni reddedildi.", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getCurrentImgBitmap(Uri uri){
        InputStream inputStream;
        try {
            inputStream = getContentResolver().openInputStream(uri);
        } catch (Exception e) {
            Toast.makeText(CreateProfile.this, "Kullanıcı kaydı tamamlanamadı..",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        int targetWidth = 800;
        int targetHeight = (int) (bitmap.getHeight() * (targetWidth / (double) bitmap.getWidth()));
        Matrix mtr = new Matrix();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
        if(bitmap.getHeight() > 2050){
            mtr.postRotate(90);
            resizedBitmap = Bitmap.createBitmap(resizedBitmap,0,0, targetWidth, targetHeight, mtr, false);
        }
        return resizedBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            File file = new File(currentPhotoPath);
            currentImgUri = Uri.fromFile(file);
            currentImgBitmap = getCurrentImgBitmap(currentImgUri);
            profile.setImageBitmap(currentImgBitmap);
        }
        else if(requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            currentImgUri = data.getData();
            currentImgBitmap = getCurrentImgBitmap(currentImgUri);
            profile.setImageBitmap(currentImgBitmap);
        }
        else if(requestCode == 57 && resultCode == Activity.RESULT_OK){
            if (resultCode == Activity.RESULT_OK) {
                String str_address = data.getStringExtra("address");
                latitude = data.getDoubleExtra("latitude",41.028424044715415);
                longitude = data.getDoubleExtra("longitude",28.890713922958838);
                if(!str_address.equals(""))
                    address.setText(str_address);
            }
        }
        else
            Toast.makeText(this, "Bir şeyler ters gitti. Lütfen tekrar deneyiniz.",
                    Toast.LENGTH_SHORT).show();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "EVDAS_image_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Bir şeyler ters gitti. Lütfen tekrar deneyiniz.",
                        Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                        getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }
}
