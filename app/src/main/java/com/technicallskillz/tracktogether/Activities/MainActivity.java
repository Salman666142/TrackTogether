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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import com.google.maps.android.SphericalUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;
import com.technicallskillz.tracktogether.R;
import com.technicallskillz.tracktogether.Utills.DangerPeopleZone;
import com.technicallskillz.tracktogether.Utills.DangerZone;
import com.technicallskillz.tracktogether.Utills.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener {
    Toolbar toolbar;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    CircleImageView profileImage;
    TextView Username;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRefUser, DangerZone;
    boolean isPersmissionGranter;
    LocationManager locationManager;

    String profileImageUrl;
    String username;
    GoogleMap googleMap;
    List<DangerZone> listDangerZone;
    List<DangerPeopleZone> listDangerPeople;
    List<User> listUser;
    Marker marker;
    MarkerOptions markerOptions;
    String status = "no";
    LatLng myCurrentLocation;
    double distance = 0.0;
    boolean isAnimateMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        DangerZone = FirebaseDatabase.getInstance().getReference().child("DangerZone");
        mRefUser = FirebaseDatabase.getInstance().getReference().child("Users");

        View v = navigationView.inflateHeaderView(R.layout.drawer_header_user);
        profileImage = v.findViewById(R.id.profile_image_header);
        Username = v.findViewById(R.id.username);

        navigationView.setNavigationItemSelectedListener(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        chceckPermission();

        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.contaner, supportMapFragment).commit();

        if (isPersmissionGranter) {
            if (checkGooglePlaServices()) {

                supportMapFragment.getMapAsync(this);
                getLocationUpdate();

            } else {
                Toast.makeText(this, "Google Play Services Not Available ", Toast.LENGTH_SHORT).show();
            }
        }
        LoadUserProfile();
    }

    private void Users() {
        mRefUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listUser = new ArrayList<>();
                    if (listUser != null) {
                        listUser.clear();
                    }
                    googleMap.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        listUser.add(user);
                    }
                    for (int i = 0; i < listUser.size(); i++) {

                        addMarker(new LatLng(listUser.get(i).getLat(), listUser.get(i).getLong()), "user", listUser.get(i).getEffected());
                       // addCircle(new LatLng(listUser.get(i).getLat(), listUser.get(i).getLong()), 100);
                        CalculatedDistance(new LatLng(listUser.get(i).getLat(), listUser.get(i).getLong()),"user");

                    }
                    LOadDangerZone();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void CalculatedDistance(LatLng otherLatLong,String type) {
        if (myCurrentLocation != null) {
            Double dis = SphericalUtil.computeDistanceBetween(myCurrentLocation, otherLatLong);
            if (dis != 0.0) {
                distance = dis;
                if (dis < 40.00) {
                     updateMyStatus(type);

                    Toast.makeText(this, "Your are passing from Effected Place or Person", Toast.LENGTH_SHORT).show();
                } else {

                }
            }
        }
    }

    private void LOadDangerZone() {
        DangerZone.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listDangerZone = new ArrayList<>();
                if (listDangerZone != null) {
                    listDangerZone.clear();
                    listDangerZone = new ArrayList<>();
                }
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        com.technicallskillz.tracktogether.Utills.DangerZone dangerZone = dataSnapshot.getValue(DangerZone.class);
                        listDangerZone.add(dangerZone);
                    }
                    for (int i = 0; i < listDangerZone.size(); i++) {
                        addMarker(new LatLng(listDangerZone.get(i).getLat(), listDangerZone.get(i).getLong()), "place","");
                      //  addCircle(new LatLng(listDangerZone.get(i).getLat(), listDangerZone.get(i).getLong()), 100);
                        CalculatedDistance(new LatLng(listDangerZone.get(i).getLat(), listDangerZone.get(i).getLong()),"zone");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
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

    private BitmapDescriptor bitmapDescriptorFromVectorUser(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_effected);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor bitmapDescriptorFromVectorUserNotConfirm(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_notconfirm);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor bitmapDescriptorFromVectorNormalUser(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.not_effected);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void addMarker(LatLng latLng, String type,String effect) {
        MarkerOptions markerOptions1 = new MarkerOptions();

        if (type.equalsIgnoreCase("user") ) {
            if (effect.equals("yes"))
            {
                markerOptions1.position(latLng);
                markerOptions1.title("User");
                markerOptions1.icon(bitmapDescriptorFromVectorUser(this, R.drawable.danger_icon_bg));
                googleMap.addMarker(markerOptions1);
            } else  if (effect.equals("no"))
            {
                markerOptions1.position(latLng);
                markerOptions1.title("User");
                markerOptions1.icon(bitmapDescriptorFromVectorNormalUser(this, R.drawable.danger_icon_bg));
                googleMap.addMarker(markerOptions1);
            }else
            {
                markerOptions1.position(latLng);
                markerOptions1.title("User");
                markerOptions1.icon(bitmapDescriptorFromVectorUserNotConfirm(this, R.drawable.danger_icon_bg));
                googleMap.addMarker(markerOptions1);
            }

            //   googleMap.animateCamera(cameraUpdate1);
        } else  {

            markerOptions1.position(latLng);
            markerOptions1.icon(bitmapDescriptorFromVectorPlace(this, R.drawable.danger_icon_bg));
            googleMap.addMarker(markerOptions1.title("Hot Zone"));
        }
    }

//    private void addCircle(LatLng latLng, float radius) {
//        CircleOptions circleOptions = new CircleOptions();
//        circleOptions.center(latLng);
//        circleOptions.radius(20);
//        circleOptions.fillColor(Color.TRANSPARENT);
//        circleOptions.strokeWidth(6);
//        circleOptions.strokeColor(Color.RED);
//        googleMap.addCircle(circleOptions);
//    }

//    private void addCircleNormalPerson(LatLng latLng, float radius) {
//        CircleOptions circleOptions1 = new CircleOptions();
//        circleOptions1.center(latLng);
//        circleOptions1.radius(20);
//        circleOptions1.fillColor(Color.TRANSPARENT);
//        circleOptions1.strokeWidth(6);
//        circleOptions1.strokeColor(Color.GREEN);
//        googleMap.addCircle(circleOptions1);
//    }

    private void LoadUserProfile() {
        mRefUser.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profileImageUrl = snapshot.child("profileImageUrl").getValue().toString();
                    username = snapshot.child("username").getValue().toString();
                    status = snapshot.child("effected").getValue().toString();
                    Picasso.get().load(profileImageUrl).into(profileImage);
                    Username.setText(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getLocationUpdate() {
        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, MainActivity.this);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, MainActivity.this);
            } else {
                Toast.makeText(this, "No Provider Enabled ", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MainActivity.this, "User Cancel Dialoge ", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(MainActivity.this, "Permission Granter", Toast.LENGTH_SHORT).show();
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
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        //  googleMap.setMinZoomPreference(10f);
        Users();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.home:
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                return true;



            case R.id.logout:
                mAuth.signOut();
                sendUserToLoginActivity();
                return true;
        }
        return false;
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        HashMap hashMap = new HashMap();
        hashMap.put("Lat", location.getLatitude());
        hashMap.put("Long", location.getLongitude());
        mRefUser.child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
//                Toast.makeText(MainActivity.this, "Updating...", Toast.LENGTH_SHORT).show();
            }
        });
        if (marker != null) {
            marker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10f);
        markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("My Location");
        if (status.equals("no")) {
            markerOptions.icon(bitmapDescriptorFromVectorNormalUser(this, R.drawable.danger_icon_bg));
        } else if (status.equals("yes")) {
            markerOptions.icon(bitmapDescriptorFromVectorUser(this, R.drawable.danger_icon_bg));
        } else {
            markerOptions.icon(bitmapDescriptorFromVectorUserNotConfirm(this, R.drawable.danger_icon_bg));
        }

        if (googleMap != null) {
            marker = googleMap.addMarker(markerOptions);
            //addCircleNormalPerson(latLng, 100);

            if (!isAnimateMap) {
                isAnimateMap = true;
                googleMap.animateCamera(cameraUpdate);
            }
        }
        myCurrentLocation = latLng;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }

    private void updateMyStatus(String type) {
        HashMap hashMap = new HashMap();
        if (type.equals("user"))
        {
            hashMap.put("effected", "yesOrNo");
        }else
        {
            hashMap.put("effected", "yes");
        }

        mRefUser.child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Toast.makeText(MainActivity.this, "Updating...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}