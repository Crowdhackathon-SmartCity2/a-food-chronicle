package com.afoodchronicle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnInfoWindowClickListener {


    public static final String PROFILE_USER_ID = "USER_ID";
    public static final String PROFILE_FIRST_NAME = "PROFILE_FIRST_NAME";
    public static final String PROFILE_LAST_NAME = "PROFILE_LAST_NAME";
    public static final String PROFILE_IMAGE_URL = "PROFILE_IMAGE_URL";

    // Markers
    HashMap<String, String> markerMap = new HashMap<String, String>();
    private static final LatLng mYoleni = new LatLng(37.9776514, 23.7388241);
    private static final LatLng mVorria = new LatLng(37.9797024, 23.7281983);
    private static final LatLng mPnyka = new LatLng(37.9685393, 23.7478882);
    private static final LatLng mPantopoleio = new LatLng(38.0056227, 23.7826411);

    // Maps
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    SupportMapFragment sMapFragment;
    GoogleMap mMap;
    boolean mapReady=false;
    static final CameraPosition ATHENS = CameraPosition.builder()
            .target(new LatLng(37.9838096, 23.7275388))
            .zoom(15)
            .build();

    // Views
    private TextView logIn;
    private ImageView profileImage;
    private TextView profileName;
    private TextView editProfile;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Acquire a reference to the system Location Manager
                LocationManager locationManager =
                        (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                //Acquire the user's location
                @SuppressLint("MissingPermission")
                Location selfLocation = locationManager
                        .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                //Move the map to the user's location
                LatLng selfLoc = new LatLng(selfLocation.getLatitude(), selfLocation.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(selfLoc, 15);
                mMap.animateCamera(update);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initializeMaps();

        View parentView = navigationView.getHeaderView(0);
        final TextView logIn = parentView.findViewById(R.id.logIn);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent listIntent = new Intent(MainActivity.this, LogInActivity.class);

                startActivity(listIntent);
            }

        });

        profileImage = parentView.findViewById(R.id.profile_image);
        profileName = parentView.findViewById(R.id.profile_name);
        editProfile = parentView.findViewById(R.id.edit_profile);

//        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {


            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    ///Facebook
                    if (isLoggedIn()) {

                        profileName.setText(getPreferences("FACEBOOK_FIRST_NAME", MainActivity.this)
                        + " " + getPreferences("FACEBOOK_LAST_NAME", MainActivity.this));
                        Picasso.with(MainActivity.this).load(getPreferences("FACEBOOK_PROFILE_PIC",
                                MainActivity.this)).into(profileImage);
//
                        logIn.setVisibility(View.GONE);
                        profileName.setVisibility(View.VISIBLE);

                        editProfile.setVisibility(View.VISIBLE);
                        editProfile.setText(R.string.edit_profile);
                        editProfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent listIntent = new Intent(MainActivity.this, ProfileDetailsActivity.class);

                                startActivity(listIntent);
                            }
                        });
                        }
                ///Email
                    else{

                        DatabaseReference facebookRef = FirebaseDatabase.getInstance().getReference("email_users");
                        facebookRef.addValueEventListener(new ValueEventListener()

                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for(DataSnapshot ds : dataSnapshot.getChildren())

                                {
                                    String firstName = ds.child("firstName").getValue(String.class);
                                    String lastName = ds.child("lastName").getValue(String.class);
                                    String profileImageLink = ds.child("photoUrl").getValue(String.class);
                                    profileName.setText(firstName + " " + lastName);
                                    Picasso.with(MainActivity.this).load(profileImageLink).into(profileImage);

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        logIn.setVisibility(View.GONE);
                        profileName.setVisibility(View.VISIBLE);

                        editProfile.setVisibility(View.VISIBLE);
                        editProfile.setText(R.string.edit_profile);
                        editProfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent listIntent = new Intent(MainActivity.this, ProfileDetailsActivity.class);

                                startActivity(listIntent);
                            }
                        });


                    }

                }
                //User signed out
                else{
                    logIn.setVisibility(View.VISIBLE);
                    profileName.setVisibility(View.GONE);
                    editProfile.setVisibility(View.GONE);
                }
            }
        };
    }

    public static void setPreferences(String key, String value, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getPreferences(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        super.onStop();
    }

    public static boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    ////////   MAPS


    public void initializeMaps(){

        sMapFragment = SupportMapFragment.newInstance();
        sMapFragment.getMapAsync(this);
        android.support.v4.app.FragmentManager sFm = getSupportFragmentManager();

        if (!sMapFragment.isAdded())
            sFm.beginTransaction().add(R.id.map, sMapFragment).commit();
        else
            sFm.beginTransaction().show(sMapFragment).commit();

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mapReady = true;

        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(ATHENS));

        // Add lots of markers to the map.
        addMarkersToMap();

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new InfoWindowCustom(markerMap, this));


        enableMyLocation();

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    private void addMarkersToMap() {

        String id = null;

        Marker yoleni = mMap.addMarker(new MarkerOptions()
                .position(mYoleni)
                .title(getString(R.string.yoleni))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("marker",160,160))));

        id = yoleni.getId();
        markerMap.put(id, "yoleni");

        Marker vorria = mMap.addMarker(new MarkerOptions()
                .position(mVorria)
                .title(getString(R.string.vorria))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("marker",160,160))));

        id = vorria.getId();
        markerMap.put(id, "vorria");

        Marker pnyka = mMap.addMarker(new MarkerOptions()
                .position(mPnyka)
                .title(getString(R.string.pnyka))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("marker",160,160))));

        id = pnyka.getId();
        markerMap.put(id, "pnyka");

        Marker pantopoleio = mMap.addMarker(new MarkerOptions()
                .position(mPantopoleio)
                .title(getString(R.string.pantopoleio))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("marker",160,160))));

        id = pantopoleio.getId();
        markerMap.put(id, "pantopoleio");

    }
    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String m = markerMap.get(marker.getId());

        if (m.equals("yoleni")){
            Intent i = new Intent(MainActivity.this, InfoWindowDetails.class);
            i.putExtra("MARKER_NAME",m);
            startActivity(i);
        }
        else {
            Context context = getApplicationContext();
            CharSequence text = "Hello toast!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }


    //// MISC

    public Bitmap resizeBitmap(String drawableName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(drawableName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

 }

