package com.example.myapplication.database;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.net.Uri;
import android.provider.DocumentsProvider;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Database {
    public Database() {}
    public void signOut(){
        FirebaseAuth.getInstance().signOut();
    }

    public void sendVerificationMail(String email, String password, String passAgain,
                                     OnSuccessListener<String> onSuccessListener, OnFailureListener onFailureListener){
        if(password.equals(passAgain)) {
            try {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        onSuccessListener.onSuccess("Lütfen mailinizi kontrol edin.");
                                    else
                                        onFailureListener.onFailure(new Exception("Bir hata oluştu."));
                                }
                            });
                        }
                        else if (password.length() < 6)
                            onFailureListener.onFailure(new Exception("Şifre uzunluğu 6 karakterden düşük olamaz."));
                        else{
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                onFailureListener.onFailure(new Exception("Email formatı hatalı."));
                            } catch (Exception e) {
                                onFailureListener.onFailure(new Exception("Bu mail ile kayıt zaten oluşturulmuş."));
                            }
                        }

                    }
                });
                signOut();
            }
            catch (IllegalArgumentException | NullPointerException npe){
                onFailureListener.onFailure(new Exception("Zorunlu alanları doldurun."));
            }
        }
        else
            onFailureListener.onFailure(new Exception("Şifreler eşleşmiyor."));
    }

    public void emailPasswordAuthentication(String email, String password,
                                            OnSuccessListener<String> onSuccessListener,
                                            OnFailureListener onFailureListener) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onSuccessListener.onSuccess("Giriş yapıldı.");
                    }
                    else {
                        onFailureListener.onFailure(new Exception("Kullanıcı adı ya da şifre yanlış."));
                    }
                });
    }

    public void changePassword(String email, Context context){
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Check Your Email",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void getAllUsers(OnSuccessListener<ArrayList<User>> onSuccessListener,
                            OnFailureListener onFailureListener) {
        ArrayList<User> list = new ArrayList<>();
        String currUserId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        User temp;
                        for (DocumentSnapshot document : task.getResult()) {
                            temp = document.toObject(User.class);
                            if (!currUserId.equals(temp.getuID()))
                                list.add(document.toObject(User.class));
                        }
                        onSuccessListener.onSuccess(list);
                    }
                    else {
                        onFailureListener.onFailure(task.getException());
                    }
                });
    }

    public ArrayList<User> getUsersViaFilter(ArrayList<User> allUsers, String filter) {
        ArrayList<User> list = new ArrayList<>();
        for(User u : allUsers)
            if(u.getFullName().toLowerCase().contains(filter))
                list.add(u);
        return list;
    }

    public ArrayList<User> getUsersViaFilter(ArrayList<User> allUsers, String kmMin, String kmMax,
                                             String dayMin, String dayMax, String department) {
        ArrayList<User> list = new ArrayList<>();
        for (User u : allUsers){
            if(isDepartmentEquals(u.getDepartment(), department)
                    && isNumberBigger(u.getKm(), kmMin) && isNumberSmaller(u.getKm(), kmMax)
                    && isNumberBigger(u.getDay(), dayMin) && isNumberSmaller(u.getDay(), dayMax))
                list.add(u);
        }

        return list;
    }

    private boolean isNumberSmaller(String str_num, String str_max) {
        if(str_max.isEmpty())
            return true;
        else if (str_num.isEmpty())
            return false;
        else return Integer.parseInt(str_num) <= Integer.parseInt(str_max);
    }

    private boolean isNumberBigger(String str_num, String str_min) {
        if(str_min.isEmpty())
            return true;
        else if (str_num.isEmpty())
            return false;
        else return Integer.parseInt(str_num) >= Integer.parseInt(str_min);
    }

    private boolean isDepartmentEquals(String dep1, String dep2) {
        if(dep2.equals("Hiçbiri"))
            return true;
        else return dep1.equals(dep2);
    }


    public void addUserMedia(String userID, Uri uri, OnSuccessListener<Boolean> onSuccessListener){
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss", Locale.getDefault());
        Date now = new Date();
        String date = formatter.format(now);
        StorageReference profileRef = FirebaseStorage.getInstance().getReference()
                .child("media/" + userID + "/" + date);
        UploadTask uploadTask = profileRef.putFile(uri);
        uploadTask.addOnCompleteListener(task_storage -> {
            if (task_storage.isSuccessful())
                onSuccessListener.onSuccess(true);
            else
                onSuccessListener.onSuccess(false);
        });
    }

    public void getUserMedia(String userID, OnSuccessListener<ArrayList<Uri>> onSuccessListener, OnFailureListener onFailureListener) {
        ArrayList<Uri> mediaList = new ArrayList<>();
        String UPPER_PATH = "media/" + userID + "/";
        FirebaseStorage.getInstance().getReference(UPPER_PATH).listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                if(task.isSuccessful()){
                    List<StorageReference> items = task.getResult().getItems();
                    for(int i = 0; i < items.size(); i++){
                        items.get(i).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mediaList.add(uri);
                                if (mediaList.size() == items.size()) {
                                    onSuccessListener.onSuccess(mediaList);
                                }
                            }
                        });
                    }
                    if (mediaList.size() == items.size()) {
                        onSuccessListener.onSuccess(mediaList);
                    }
                }
                else {
                    onFailureListener.onFailure(new Exception());
                }
            }
        });
    }

    public void getImageUri(String path, OnSuccessListener<Uri> onSuccessListener){
        FirebaseStorage.getInstance().getReference().
                child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                onSuccessListener.onSuccess(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onSuccessListener.onSuccess(null);
            }
        });
    }

    public void isEmailVerified(OnSuccessListener<Boolean> onSuccessListener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            onSuccessListener.onSuccess(user.isEmailVerified());
        }
        else{
            onSuccessListener.onSuccess(false);
        }
    }

    public void isUserCreated(OnSuccessListener<Boolean> onSuccessListener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists())
                            onSuccessListener.onSuccess(Boolean.TRUE);
                        else
                            onSuccessListener.onSuccess(Boolean.FALSE);
                    }
                });
    }

    public void saveUserInfos(Bitmap file, String str_fullname, String str_phone, String str_department,
                              String str_grade, String str_state, String str_day, String str_km, String str_adr,
                              boolean tel, boolean mail, double latitude, double longitude,
                              OnSuccessListener<String> onSuccessListener, OnFailureListener onFailureListener) {
        try {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference profileRef = FirebaseStorage.getInstance().getReference().child("users/"+uid+"/profile-photo.jpg");
            Map<String, Object> updateData = new HashMap<>();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            file.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();

            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("TAG", token);
                    profileRef.putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                User newUser = new User("users/"+uid+"/profile-photo.jpg", uid, token, email, str_fullname, str_phone,
                                        str_department, str_grade, str_state, str_day, str_adr, str_km, tel, mail, latitude, longitude);
                                FirebaseFirestore.getInstance().collection("users")
                                        .document(uid)
                                        .set(newUser)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    if(!str_adr.equals("")) {
                                                        updateData.put(uid, str_adr);
                                                        FirebaseFirestore.getInstance().collection("addresses")
                                                                .document("addresses")
                                                                .set(updateData)
                                                                .addOnCompleteListener(task1 -> {
                                                                    if (task1.isSuccessful())
                                                                        onSuccessListener.onSuccess("Kullanıcı başarıyla kaydedildi.");
                                                                    else {
                                                                        onFailureListener.onFailure(new Exception("Kullanıcı kaydı tamamlanamadı."));
                                                                        FirebaseAuth.getInstance().getCurrentUser().delete()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(Task<Void> task_delete_auth) {
                                                                                        if (task_delete_auth.isSuccessful()) {
                                                                                            Log.d("TAG", "User account deleted.");
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                });
                                                    }
                                                    else
                                                        onSuccessListener.onSuccess("Kullanıcı başarıyla kaydedildi.");
                                                }
                                                else{
                                                    onFailureListener.onFailure(new Exception("Kullanıcı kaydı tamamlanamadı."));
                                                    FirebaseAuth.getInstance().getCurrentUser().delete()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(Task<Void> task_delete_auth) {
                                                                    if (task_delete_auth.isSuccessful()) {
                                                                        Log.d("TAG", "User account deleted.");
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                            else {
                                onFailureListener.onFailure(new Exception("Kullanıcı kaydı tamamlanamadı."));
                                FirebaseAuth.getInstance().getCurrentUser().delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(Task<Void> task_delete_auth) {
                                                if (task_delete_auth.isSuccessful()) {
                                                    Log.d("TAG", "User account deleted.");
                                                }
                                            }
                                        });
                            }
                        }
                    });

                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
            onFailureListener.onFailure(e);
//            onFailureListener.onFailure(new Exception("Kullanıcı kaydı tamamlanamadı."));
        }
    }
    public void saveUserInfos(String str_fullname, String str_phone, String str_department,
                              String str_grade, String str_state, String str_day, String str_km, String str_adr,
                              boolean tel, boolean mail, double latitude, double longitude,
                              OnSuccessListener<String> onSuccessListener, OnFailureListener onFailureListener) {

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> updateData = new HashMap<>();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                    return;
                }
                String token = task.getResult();
                Log.d("TAG", token);
                User newUser = new User("users/"+uid+"/profile-photo.jpg", uid, token, email, str_fullname, str_phone,
                        str_department, str_grade, str_state, str_day, str_adr, str_km, tel, mail, latitude, longitude);
                FirebaseFirestore.getInstance().collection("users")
                        .document(uid)
                        .set(newUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    if (!str_adr.equals("")) {
                                        updateData.put(uid, str_adr);
                                        FirebaseFirestore.getInstance().collection("addresses")
                                                .document("addresses")
                                                .set(updateData)
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful())
                                                        onSuccessListener.onSuccess("Kullanıcı başarıyla kaydedildi.");
                                                    else {
                                                        onFailureListener.onFailure(new Exception("Kullanıcı kaydı tamamlanamadı."));
                                                        FirebaseAuth.getInstance().getCurrentUser().delete()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(Task<Void> task_delete_auth) {
                                                                        if (task_delete_auth.isSuccessful()) {
                                                                            Log.d("TAG", "User account deleted.");
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                    } else
                                        onSuccessListener.onSuccess("Kullanıcı başarıyla kaydedildi.");
                                }
                                else{
                                    onFailureListener.onFailure(new Exception("Kullanıcı kaydı tamamlanamadı."));
                                    FirebaseAuth.getInstance().getCurrentUser().delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(Task<Void> task_delete_auth) {
                                                    if (task_delete_auth.isSuccessful()) {
                                                        Log.d("TAG", "User account deleted.");
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            }

        });
    }

    public boolean isSigned() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public void getEnableLocations(OnSuccessListener<Map<String, Object>> onSuccessListener,
                                   OnFailureListener onFailureListener) {
        FirebaseFirestore.getInstance().collection("addresses")
                .document("addresses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        if (task.getResult().exists()){
                            Map<String, Object> map = task.getResult().getData();
                            onSuccessListener.onSuccess(map);
                        }
                        else{
                            onFailureListener.onFailure(task.getException());
                        }
                    }
                    else {
                        onFailureListener.onFailure(task.getException());
                    }
                });
    }

    public void getCurrentUser(OnSuccessListener<User> onSuccessListener, OnFailureListener onFailureListener) {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users")
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        User u = task.getResult().toObject(User.class);
                        onSuccessListener.onSuccess(u);
                    }
                    else {
                        onFailureListener.onFailure(task.getException());
                    }
                });
    }

    public void deleteCurrentUser(OnSuccessListener<String> onSuccessListener, OnFailureListener onFailureListener) {
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseAuth.getInstance().signOut();
        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FirebaseFirestore.getInstance().collection("users")
                            .document(uid)
                            .delete()
                            .addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    onSuccessListener.onSuccess("Hesap silindi.");
                                }
                                else {
                                    onFailureListener.onFailure(new Exception("Hesap doğru silinemedi."));
                                }
                            });
                }
                else {
                    Log.d("ERROR", task.getException().toString());
                    onFailureListener.onFailure(new Exception("Hesap silinemedi."));
                }
            }
        });
    }

    public void addNotification(String uid, Context context) {
        getCurrentUser(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                String senderToken = user.getNotificationToken();
                String title = "Ev Arkadaşın Olmak İsteyen Biri Var!";
                String body = user.getFullName() + " seninle ev arkadaşı olmak istiyor.";
                Boolean answered = false;
                Notifications notifications = new Notifications(title, body, senderToken, user.getuID(), answered);
                FirebaseFirestore.getInstance().collection(uid)
                        .document("notifications "+new Date().toString())
                        .set(notifications)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(context, "Talep iletildi.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Bir hata oluştu.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addRejectNotification(String uid, Context context) {
        getCurrentUser(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                String senderToken = user.getNotificationToken();
                String title = user.getFullName() + " isteğini reddetti.";
                String body = "Denizde bir sürü balık var. Aramaya devam et!";
                Notifications notifications = new Notifications(title, body, senderToken, user.getuID(), true);
                FirebaseFirestore.getInstance().collection(uid)
                        .document("notifications "+new Date().toString())
                        .set(notifications)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(context, "Cevabın iletildi.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Bir hata oluştu.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addAcceptNotification(String uid, Context context) {
        getCurrentUser(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                String senderToken = user.getNotificationToken();
                String title = user.getFullName() + " isteğini kabul etti.";
                String body = "Hayırlı olsun!";
                Notifications notifications = new Notifications(title, body, senderToken, user.getuID(), true);
                FirebaseFirestore.getInstance().collection(uid)
                        .document("notifications "+new Date().toString())
                        .set(notifications)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(context, "Cevabın iletildi.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Bir hata oluştu.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getAllNotifications(OnSuccessListener<List<Notifications>> onSuccessListener,
                                    OnFailureListener onFailureListener) {
        String uid = FirebaseAuth.getInstance().getUid();
        List<Notifications> notifications = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(uid).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                for(DocumentSnapshot doc : task.getResult().getDocuments()){
                    notifications.add(doc.toObject(Notifications.class));
                }
                onSuccessListener.onSuccess(notifications);
            }
            else{
                onFailureListener.onFailure(new Exception("Adresler alınamadı."));
            }
        });

    }


    public void setStateAfterMatch(String senderID) {
        String currentID = FirebaseAuth.getInstance().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("state", "Aramıyor");
        updates.put("day", "");
        updates.put("km", "");
        updates.put("address", "");

        CollectionReference colRef = FirebaseFirestore.getInstance()
                .collection("users");

        colRef.document(senderID)
                .update(updates)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        colRef.document(currentID)
                                .update(updates)
                                .addOnCompleteListener(task1 -> {
                                    return;
                                });
                    }
                });
    }

    public void getAllAddresses(OnSuccessListener<List<User>> onSuccessListener, OnFailureListener onFailureListener) {
        List<User> users = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("users").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                for(DocumentSnapshot doc : task.getResult().getDocuments()){
                    User u = doc.toObject(User.class);
                    if(!u.getAddress().equals("Adres Eklemek İçin Tıkla") && !u.getAddress().equals("")){
                        users.add(u);
                    }
                }
                onSuccessListener.onSuccess(users);
            }
            else{
                onFailureListener.onFailure(new Exception("Adresler alınamadı."));
            }
        });
    }
}