package com.technicallskillz.tracktogether.Activities;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.technicallskillz.tracktogether.R;
import com.technicallskillz.tracktogether.Utills.DangerPeopleZone;
import com.technicallskillz.tracktogether.Utills.DangerZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminHomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    Toolbar toolbar;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRef;
    boolean isPersmissionGranter;
    GoogleMap map;
    List<DangerZone> listDangerZone;
    List<DangerPeopleZone> listDangerPeople;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Admin");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("DangerZone");

        chceckPermission();
        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.contaner, supportMapFragment).commit();
        supportMapFragment.getMapAsync(this);
        if (isPersmissionGranter) {
            if (checkGooglePlaServices()) {

            } else {
                Toast.makeText(this, "Google Playservices Not Available ", Toast.LENGTH_SHORT).show();
            }
        }

    }
    private boolean checkGooglePlaServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError(result)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, 201, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    Toast.makeText(AdminHomeActivity.this, "User Cancel Dialoge ", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }
        return false;
    }

    private void chceckPermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                isPersmissionGranter = true;
                Toast.makeText(AdminHomeActivity.this, "Permission Granter", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Toast.makeText(AdminHomeActivity.this, "", Toast.LENGTH_SHORT).show();

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title("My position");
                markerOptions.position(latLng);
                googleMap.addMarker(markerOptions);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 5);
                googleMap.animateCamera(cameraUpdate);


                AlertDialog.Builder builder = new AlertDialog.Builder(AdminHomeActivity.this);
                builder.setTitle("Do you want to Add it as HotZone");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        HashMap hashMap=new HashMap();
                        hashMap.put("Lat",latLng.latitude);
                        hashMap.put("Long",latLng.longitude);

                        mRef.push().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful())
                                {
                                  map.clear();
                                    LoadHotZone();
                                    Toast.makeText(AdminHomeActivity.this, " Added!", Toast.LENGTH_SHORT).show();
                                }else
                                {
                                    map.clear();
                                    LoadHotZone();
                                    Toast.makeText(AdminHomeActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        map.clear();
                    }
                }).show().create();
            }
        });

        LoadHotZone();


    }

    private void LoadHotZone() {

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listDangerZone = new ArrayList<>();
             map.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        com.technicallskillz.tracktogether.Utills.DangerZone dangerZone = dataSnapshot.getValue(DangerZone.class);
                        listDangerZone.add(dangerZone);
                    }
                    for (int i = 0; i < listDangerZone.size(); i++) {
                        addMarker(new LatLng(listDangerZone.get(i).getLat(), listDangerZone.get(i).getLong()), "place","");
                      //  addCircle(new LatLng(listDangerZone.get(i).getLat(), listDangerZone.get(i).getLong()), 100);
                        //CalculatedDistance(new LatLng(listDangerZone.get(i).getLat(), listDangerZone.get(i).getLong()),"zone");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void addMarker(LatLng latLng, String type,String effect) {
        MarkerOptions markerOptions1 = new MarkerOptions();

            markerOptions1.position(latLng);
            markerOptions1.icon(bitmapDescriptorFromVectorPlace(this, R.drawable.danger_icon_bg));
        map.addMarker(markerOptions1.title("Hot Zone"));

    }
    private BitmapDescriptor bitmapDescriptorFromVectorPlace(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_danger);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_drawer_admin,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.logout:
                ClearSharePrefernce();
                sendUserToLoginActivity();
                break;

            case R.id.HotZone:
                sendAdminToHotZon();
                break;

            case R.id.EffectedUser:
                sendAdminTOEffectedList();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void sendAdminToHotZon() {
        Intent intent = new Intent(AdminHomeActivity.this, HotZoneListActivity.class);
        startActivity(intent);

    }

    private void sendAdminTOEffectedList() {
        Intent intent = new Intent(AdminHomeActivity.this, EffectedUserActivity.class);
        startActivity(intent);

    }

    private void ClearSharePrefernce() {
        SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
        editor.putString("type", "");
        editor.putString("profile", "");
        editor.apply();
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(AdminHomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}