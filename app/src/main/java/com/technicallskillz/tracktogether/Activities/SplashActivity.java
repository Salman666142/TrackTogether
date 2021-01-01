package com.technicallskillz.tracktogether.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technicallskillz.tracktogether.R;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference DataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();

        DataRef= FirebaseDatabase.getInstance().getReference().child("Users");

        SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
        String type = prefs.getString("type", "no user");//"No name defined" is the default value.
        String profile = prefs.getString("profile", "uncomplete");//"No name defined" is the default value.


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mUser!=null)
                {
                  if (type.equalsIgnoreCase("user") && profile.equalsIgnoreCase("completed"))
                  {
                      Intent intent=new Intent(SplashActivity.this, MainActivity.class);
                      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                      startActivity(intent);
                      finish();
                  }else
                  {
                     CheckFirebase();
                  }
                }else
                {
                    if (type.equalsIgnoreCase("admin") && profile.equalsIgnoreCase("completed"))
                    {
                        Intent intent=new Intent(SplashActivity.this, AdminHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else
                    {
                        Intent intent=new Intent(SplashActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }

            }
        };
        new Handler().postDelayed(runnable, 5000);

    }

    private void CheckFirebase() {
        DataRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String exist=dataSnapshot.child("username").getValue().toString();
                    if (exist==null)
                    {
                        Toast.makeText(SplashActivity.this, "exist==nnukk", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SplashActivity.this, SetupActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }else
                    {
                        Toast.makeText(SplashActivity.this, "exist=!nnukk", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }


                } else {
                    Intent intent = new Intent(SplashActivity.this, SetupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(SplashActivity.this, databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}