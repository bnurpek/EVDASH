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
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myapplication.R;
import com.example.myapplication.database.Database;
import com.example.myapplication.database.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Update extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 103;
    public static final int GALLERY_PERM_CODE = 104;
    String currentPhotoPath;
    Uri currentImgUri;
    Bitmap currentImgBitmap;
    Database db = new Database();
    Button update, delete;
    ImageButton gallery, camera;
    ImageView profile;
    EditText phone, day, km, fullname;
    TextView showDayHelp, email, address;
    Spinner state, grade, department;
    CheckBox telCheck, mailCheck;
    User user;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        byte[] bytes = getIntent().getByteArrayExtra("currentUser");
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bis);
            user = (User) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            finish();
        }

        db.getImageUri(user.getProfilePhotoUrl(), new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                currentImgUri = uri;
                Picasso.get().load(currentImgUri).into(profile);
            }
        });

        showDayHelp = findViewById(R.id.question_mark);

        update = findViewById(R.id.btn_update);
        delete = findViewById(R.id.btn_delete_account);
        gallery = findViewById(R.id.ib_file);
        camera = findViewById(R.id.ib_camera);

        profile = findViewById(R.id.iv_profile);
        fullname = findViewById(R.id.et_full_name);
        fullname.setText(user.getFullName());
        email = findViewById(R.id.tv_email);
        email.setText(user.getEmail());
        day = findViewById(R.id.et_day);
        day.setText(user.getDay());
        km = findViewById(R.id.et_kmeters);
        km.setText(user.getKm());
        address = findViewById(R.id.address);
        address.setText(user.getAddress());

        telCheck = findViewById(R.id.cb_tel);
        mailCheck = findViewById(R.id.cb_mail);
        telCheck.setChecked(user.isPhoneStatue());
        mailCheck.setChecked(user.isMailStatue());
        latitude = 41.028424044715415;
        longitude = 28.890713922958838;

        phone = findViewById(R.id.et_tel);
        phone.setText(user.getPhone());
        if(user.getPhone().isEmpty()){
            telCheck.setEnabled(false);
        }
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String tel = s.toString();
                if(tel.length() > 9){
                    telCheck.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String tel = s.toString();
                if(tel.length() > 9){
                    telCheck.setEnabled(true);
                }
            }
        });

        state = findViewById(R.id.spinner_state);
        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this,
                R.array.search_state_array, android.R.layout.simple_spinner_item);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        state.setAdapter(stateAdapter);
        state.post(new Runnable() {
            @Override
            public void run() {
                state.setSelection(stateAdapter.getPosition(user.getState()));
            }
        });

        state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).toString().equals("Aramıyor")){
                    day.setText("");
                    day.setEnabled(false);
                    km.setText("");
                    km.setEnabled(false);
                    address.setText("Adres Eklemek İçin Tıkla");
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
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        grade = findViewById(R.id.spinner_grade);
        ArrayAdapter<CharSequence> classNumAdapter = ArrayAdapter.createFromResource(this,
                R.array.class_num_array, android.R.layout.simple_spinner_item);
        classNumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        grade.setAdapter(classNumAdapter);
        grade.post(new Runnable() {
            @Override
            public void run() {
                grade.setSelection(classNumAdapter.getPosition(user.getGrade()));
            }
        });

        department = findViewById(R.id.spinner_dep);
        ArrayAdapter<CharSequence> depAdapter = ArrayAdapter.createFromResource(this,
                R.array.department_name_array, android.R.layout.simple_spinner_item);
        depAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        department.setAdapter(depAdapter);
        department.post(new Runnable() {
            @Override
            public void run() {
                department.setSelection(depAdapter.getPosition(user.getDepartment()));
            }
        });

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

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Update.this, SelectAddress.class);
                startActivityForResult(intent, 57);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_name = fullname.getText().toString();
                String str_phone = phone.getText().toString().replaceAll(" ","");
                String str_department = department.getSelectedItem().toString();
                String str_grade = grade.getSelectedItem().toString();
                String str_state = state.getSelectedItem().toString();
                String str_day = day.getText().toString();
                String str_km = km.getText().toString();
                String str_adr = address.getText().toString();
                boolean tel = telCheck.isChecked();
                boolean mail = mailCheck.isChecked();

                if(str_name.isEmpty() || (day.isEnabled() && str_day.isEmpty()) ||
                        (address.isEnabled() && str_adr.equals("Adres Eklemek İçin Tıkla")) || (km.isEnabled() && str_km.isEmpty())){
                    Toast.makeText(getApplicationContext(), "Lütfen zorunlu alanları doldurunuz.",
                            Toast.LENGTH_SHORT).show();
                }
                else if(str_phone.length() > 0 && str_phone.length() < 10){
                    Toast.makeText(getApplicationContext(), "Telefon numaranızı doğru yazdığınıza emin olun.",
                            Toast.LENGTH_SHORT).show();
                }
                else if(currentImgUri != null) {
                    if (currentImgBitmap != null) {
                        db.saveUserInfos(currentImgBitmap, str_name, str_phone, str_department,
                                str_grade, str_state, str_day, str_km, str_adr, tel, mail, latitude, longitude,
                                new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                                    }
                                }, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Update.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else{
                        db.saveUserInfos(str_name, str_phone,
                                str_department, str_grade, str_state, str_day, str_km, str_adr, tel, mail, latitude, longitude,
                                new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                                    }
                                }, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Update.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
                else
                    Toast.makeText(Update.this, "Lütfen bir profil fotoğrafı seçin.",
                            Toast.LENGTH_SHORT).show();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Update.this);
                builder.setTitle("Hesabı Sil");
                builder.setMessage("Hesabınızı silmek istediğinize emin misiniz?");
                builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.deleteCurrentUser(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                                getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit().putBoolean("Created", false).apply();
                                Intent intent = new Intent(getApplicationContext(), SignIn.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Hayır", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
            Toast.makeText(Update.this, "Kullanıcı kaydı tamamlanamadı..",
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
