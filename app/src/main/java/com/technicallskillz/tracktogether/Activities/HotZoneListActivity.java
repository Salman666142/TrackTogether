package com.technicallskillz.tracktogether.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.technicallskillz.tracktogether.R;
import com.technicallskillz.tracktogether.Utills.Location;
import com.technicallskillz.tracktogether.Utills.User;

import java.util.HashMap;
import java.util.List;

public class HotZoneListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference HotZone;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_zone_list);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        HotZone = FirebaseDatabase.getInstance().getReference().child("HotZone");

        toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("HotZone List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.effectedRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LoadZone();
    }

    private void LoadZone() {
        FirebaseRecyclerAdapter<Location, LocationViewHolder> adapter;
        FirebaseRecyclerOptions<Location> options;


        options = new FirebaseRecyclerOptions.Builder<Location>().setQuery(HotZone, Location.class).build();
        adapter = new FirebaseRecyclerAdapter<Location, LocationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull LocationViewHolder holder, int position, @NonNull Location model) {
                holder.username.setText("Hot Zone");
                holder.email.setText("Lat : " + model.getLat() + "\nLong : " + model.getLong());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(HotZoneListActivity.this);
                        builder.setTitle("Do you want Remove this  Hot Zone");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                HotZone.child(getRef(position).getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            Toast.makeText(HotZoneListActivity.this, "Removed Succesfully" , Toast.LENGTH_SHORT).show();
                                        }else
                                        {
                                            Toast.makeText(HotZoneListActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show().create();
                    }
                });
            }

            @NonNull
            @Override
            public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sigle_zone, parent, false);
                return new LocationViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);


    }
}