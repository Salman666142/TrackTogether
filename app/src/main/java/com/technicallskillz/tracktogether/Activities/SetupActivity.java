package com.technicallskillz.tracktogether.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.technicallskillz.tracktogether.R;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private static final int IMAGE_PICKER_SELECT =1010 ;

    CircleImageView profileImage;
    EditText inputUsername,inputCity,inputCountry,inputProfession;
    Button btnSave;
    Uri imageUri=null;
    ProgressDialog mLoadingBar;
    FirebaseAuth mAuth;
    DatabaseReference DataRef;
    StorageReference StorageRef;
    FirebaseUser mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        profileImage=findViewById(R.id.profile_image);
        inputUsername=findViewById(R.id.inputUsername);
        inputCity=findViewById(R.id.inputCity);
        inputCountry=findViewById(R.id.inputCountry);
        btnSave=findViewById(R.id.btnSave);


        mLoadingBar=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        DataRef= FirebaseDatabase.getInstance().getReference().child("Users");
        StorageRef= FirebaseStorage.getInstance().getReference().child("ProfileImages");

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                pickIntent.setType("image/*");
//                startActivityForResult(pickIntent, IMAGE_PICKER_SELECT);

                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, IMAGE_PICKER_SELECT);//
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveProfile();
            }
        });


    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICKER_SELECT && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);

            //handle video
        }
    }

    private void SaveProfile() {
        final String username=inputUsername.getText().toString();
        final String city=inputCity.getText().toString();
        final String country=inputCountry.getText().toString();

        if (username.isEmpty() || username.length()<3)
        {
            showError(inputUsername,"username must be greater the 3 latter");
        }
        else if(city.isEmpty() || city.length()<3)
        {
            showError(inputCity,"city must be greater the 3 latter");
        }
        else if (imageUri==null)
        {
            Toast.makeText(this, "Please Select an Profile Image", Toast.LENGTH_SHORT).show();
        }
        else
        {
            mLoadingBar.setTitle("Saving Profile");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            StorageRef.child(mUser.getUid()).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    StorageRef.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            HashMap hashMap=new HashMap();
                            hashMap.put("username",username);
                            hashMap.put("city",city);
                            hashMap.put("status","offline");
                            hashMap.put("profileImageUrl",uri.toString());

                            DataRef.child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful())
                                    {
                                        SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
                                        editor.putString("type", "user");
                                        editor.putString("profile", "completed");
                                        editor.apply();

                                        mLoadingBar.dismiss();
                                        Intent intent=new Intent(SetupActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else
                                    {
                                        mLoadingBar.dismiss();
                                        Toast.makeText(SetupActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });
                }
            });
        }
    }

    private void showError(EditText field, String s) {
        field.setError(s);
        field.requestFocus();
    }
}