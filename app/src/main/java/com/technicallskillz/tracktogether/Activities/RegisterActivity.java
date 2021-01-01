package com.technicallskillz.tracktogether.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.technicallskillz.tracktogether.R;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {


    EditText inputEmail, inputPassword, inputConfirmPassword,inputUsername;
    Button btnRegister;
    TextView loginActivity;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRef;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConformPassword);
        btnRegister = findViewById(R.id.btnRegister);
        loginActivity = findViewById(R.id.loginActivity);
        progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);


        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");


        loginActivity.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });


        btnRegister.setOnClickListener(view -> {
            performAuth();
        });
    }

    private void performAuth() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        if (email.length() == 0 || email.isEmpty() || !email.contains("@")) {
            inputEmail.setError("Please Enter Proper Email");
            inputEmail.requestFocus();
        } else if (password.length() == 0 || password.isEmpty() || password.length() < 6) {
            inputPassword.setError("Please Enter password with atleast 6 words");
            inputPassword.requestFocus();
        } else if (confirmPassword.length() == 0 || confirmPassword.isEmpty() || !confirmPassword.equals(password)) {
            inputConfirmPassword.setError("Password not match with First Password");
            inputConfirmPassword.requestFocus();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mUser = mAuth.getCurrentUser();
                        HashMap hashMap = new HashMap();
                        hashMap.put("email", mUser.getEmail());
                        hashMap.put("username","");
                        hashMap.put("Lat",0.0);
                        hashMap.put("Long",0.0);
                        hashMap.put("city","");
                        hashMap.put("effected","no");
                        hashMap.put("status","offline");
                        hashMap.put("profileImageUrl","");

                        mRef.child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Registration Successfully Completed", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();

                                }else
                                {
                                    progressBar.setVisibility(View.GONE);
                                    btnRegister.setEnabled(true);
                                    Toast.makeText(RegisterActivity.this, "Error"+task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Error"+task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
}