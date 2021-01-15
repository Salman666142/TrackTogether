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
import android.widget.EditText;
import android.widget.ImageView;
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
import com.technicallskillz.tracktogether.Utills.User;

import java.util.HashMap;
import java.util.List;

public class EffectedUserActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference UserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    List<User> list;

    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effected_user);

        toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Affected List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setHomeButtonEnabled(true);

        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView=findViewById(R.id.effectedRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LoadUser();
    }

    private void LoadUser() {
        FirebaseRecyclerAdapter<User, PostViewHolder> adapter;
        FirebaseRecyclerOptions<User> options;

        options = new FirebaseRecyclerOptions.Builder<User>().setQuery(UserRef, User.class).build();
        adapter = new FirebaseRecyclerAdapter<User, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull User model) {

                AlertDialog.Builder builder = new AlertDialog.Builder(EffectedUserActivity.this);
                builder.setCancelable(false);

                HashMap hashMap=new HashMap();

                holder.email.setText(model.getEmail());
                holder.username.setText(model.getUsername());

                Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImageView);
                if (model.getEffected().equals("yes"))
                {
                    holder.effected.setImageResource(R.drawable.ic_effected);
                    builder.setTitle("Do you want to Remove Effected");
                    hashMap.put("effected","no");
                }else if (model.getEffected().equals("no"))
                {
                    builder.setTitle("Do you want to Add into Effected List");
                    holder.effected.setImageResource(R.drawable.not_effected);
                    hashMap.put("effected","yes");

                }else
                {
                    holder.effected.setImageResource(R.drawable.ic_notconfirm);
                    builder.setTitle("Do you want to Remove Effected");
                    hashMap.put("effected","yes");
                }
                holder.effected.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                UserRef.child(getRef(position).getKey()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful())
                                        {
                                            notifyDataSetChanged();
                                            Toast.makeText(EffectedUserActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                        }else
                                        {
                                            Toast.makeText(EffectedUserActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
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


                holder.email.setText(model.getEmail());
            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_layout_user,parent,false);
                return new PostViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }
}