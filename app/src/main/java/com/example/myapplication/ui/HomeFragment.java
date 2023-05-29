package com.example.myapplication.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.SearchResultAdapter;
import com.example.myapplication.database.Database;
import com.example.myapplication.database.User;
import com.example.myapplication.databinding.FragmentHomeBinding;
import com.example.myapplication.ui.screens.MapActivity;
import com.example.myapplication.ui.screens.Profile;
import com.example.myapplication.ui.screens.SearchMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    Database db = new Database();
    private ArrayList<User> allUsers;
    private ArrayList<User> queryResult;
    RecyclerView mRecyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SearchView searchView = binding.searchView;
        ImageButton filter = binding.filter;
        ImageButton map = binding.map;
        mRecyclerView = binding.recyclerView;

        db.getAllUsers(new OnSuccessListener<ArrayList<User>>() {
            @Override
            public void onSuccess(ArrayList<User> users) {
                allUsers = users;
                SearchResultAdapter adapter = new SearchResultAdapter(users);
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
                            User user;
                            if (queryResult != null)
                                user = queryResult.get(position);
                            else
                                user = allUsers.get(position);

                            try {
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                ObjectOutputStream oos = new ObjectOutputStream(bos);
                                oos.writeObject(user);
                                oos.flush();
                                byte[] bytes = bos.toByteArray();
                                Intent intent = new Intent(getActivity(), Profile.class);
                                intent.putExtra("userBytes", bytes);
                                startActivity(intent);
                                return true;
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.equals("")) {
                    queryResult = db.getUsersViaFilter(allUsers, newText.toLowerCase());
                    SearchResultAdapter adapter = new SearchResultAdapter(queryResult);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                            RecyclerView.VERTICAL, false);

                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    mRecyclerView.setLayoutManager(linearLayoutManager);
                    mRecyclerView.setAdapter(adapter);
                    return true;
                }
                return false;
            }
        });

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery("", false);
                searchView.clearFocus();
                showNumberInputDialog();
            }
        });


        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchMap.class);
                startActivity(intent);
            }
        });

        return root;
    }

    private void showNumberInputDialog() {
        EditText kmMin, kmMax, dayMin, dayMax;
        Spinner department;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filtrele");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.filter_users, null);
        builder.setView(dialogView);

        kmMin = dialogView.findViewById(R.id.et_km_min);
        kmMax = dialogView.findViewById(R.id.et_km_max);
        dayMin = dialogView.findViewById(R.id.et_day_min);
        dayMax = dialogView.findViewById(R.id.et_day_max);

        department = dialogView.findViewById(R.id.spinner_dep);
        ArrayAdapter<CharSequence> depAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.filter_department, android.R.layout.simple_spinner_item);
        depAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        department.setAdapter(depAdapter);

        builder.setPositiveButton("Uygula", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String str_kmMin = kmMin.getText().toString();
                String str_kmMax = kmMax.getText().toString();
                String str_dayMin = dayMin.getText().toString();
                String str_dayMax = dayMax.getText().toString();
                String str_department = department.getSelectedItem().toString();
                queryResult = db.getUsersViaFilter(allUsers, str_kmMin, str_kmMax, str_dayMin, str_dayMax, str_department);
                SearchResultAdapter adapter = new SearchResultAdapter(queryResult);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                        RecyclerView.VERTICAL, false);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setLayoutManager(linearLayoutManager);
                mRecyclerView.setAdapter(adapter);
            }
        });

        builder.setNegativeButton("Vazgeç", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });

        builder.create().show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}