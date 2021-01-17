package com.technicallskillz.tracktogether.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technicallskillz.tracktogether.R;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    EditText inputEmail, inputPassword;
    Button btnLogin;
    TextView createNewAccount;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRef,admin;
    ProgressBar progressBar;
    DatabaseReference DataRef;
    boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        createNewAccount = findViewById(R.id.createNewAccount);
        progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);


        mAuth = FirebaseAuth.getInstance();
        admin = FirebaseDatabase.getInstance().getReference().child("Admin");
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        DataRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mUser=mAuth.getCurrentUser();
        createNewAccount.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });


        btnLogin.setOnClickListener(view -> {
            performAuth();
        });
    }

    public void itemClicked(View v) {
        if (((CheckBox) v).isChecked()) {
          isAdmin=true;
        }else {
           isAdmin=false;
        }
    }
    private void performAuth() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if (isAdmin)
        {
           admin.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists())
                    {
                        String emailFirebase=snapshot.child("email").getValue().toString();
                        String passwordFirebase=snapshot.child("password").getValue().toString();
                        if (email.length() == 0 || email.isEmpty() || !email.contains("@")) {
                            inputEmail.setError("Please Enter Proper Email");
                            inputEmail.requestFocus();
                        } else if (password.length() == 0 || password.isEmpty() ) {
                            inputPassword.setError("Please Enter password");
                            inputPassword.requestFocus();
                        }else
                        {
                            if (emailFirebase.equalsIgnoreCase(email) && passwordFirebase.equalsIgnoreCase(password))
                            {
                                SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
                                editor.putString("type", "admin");
                                editor.putString("profile", "completed");
                                editor.apply();

                                Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }else
                            {
                                Toast.makeText(LoginActivity.this, "Email OR Password is incorrect!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }else {

            if (email.length() == 0 || email.isEmpty() || !email.contains("@")) {
                inputEmail.setError("Please Enter Proper Email");
                inputEmail.requestFocus();
            } else if (password.length() == 0 || password.isEmpty() || password.length() < 6) {
                inputPassword.setError("Please Enter password with atleast 6 words");
                inputPassword.requestFocus();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                btnLogin.setEnabled(false);
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mUser = mAuth.getCurrentUser();
                            HashMap hashMap = new HashMap();
                            hashMap.put("email", mUser.getEmail());
                            mRef.child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        CheckSetupProfileDataExist();
                                        Toast.makeText(LoginActivity.this, "Login Successfully ", Toast.LENGTH_SHORT).show();
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        btnLogin.setEnabled(true);
                                        Toast.makeText(LoginActivity.this, "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            btnLogin.setEnabled(true);
                            Toast.makeText(LoginActivity.this, "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

    }

    private void CheckSetupProfileDataExist() {


        SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
        String type = prefs.getString("type", "no user");//"No name defined" is the default value.
        String profile = prefs.getString("profile", "uncomplete");//"No name defined" is the default value.

        if (type.equalsIgnoreCase("user") && profile.equalsIgnoreCase("completed")) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
//        {
//            Intent intent=new Intent(LoginActivity.this, SetupActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            finish();
//        }


            DataRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String exist=dataSnapshot.child("username").getValue().toString();
                        if (exist==null)
                        {
                            Toast.makeText(LoginActivity.this, "exist==nnukk", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, SetupActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }else
                        {
                            Toast.makeText(LoginActivity.this, "exist=!nnukk", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }


                    } else {
                        Intent intent = new Intent(LoginActivity.this, SetupActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    Toast.makeText(LoginActivity.this, databaseError.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}