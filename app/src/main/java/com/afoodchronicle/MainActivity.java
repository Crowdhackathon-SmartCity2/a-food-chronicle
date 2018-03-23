package com.afoodchronicle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afoodchronicle.chat.AllUsersActivity;
import com.afoodchronicle.chat.fragments.ChatFragmentsListActivity;
import com.afoodchronicle.firebase.LogInActivity;
import com.afoodchronicle.firebase.ProfileDetailsActivity;
import com.afoodchronicle.maps.InfoWindowCustom;
import com.afoodchronicle.maps.InfoWindowDetails;
import com.afoodchronicle.utilities.FacebookUtils;
import com.afoodchronicle.utilities.ImageLoadedCallback;
import com.afoodchronicle.utilities.PermissionUtils;
import com.afoodchronicle.utilities.Utils;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static com.afoodchronicle.utilities.Static.ATHENS;
import static com.afoodchronicle.utilities.Static.EMAIL_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_LAST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.FACEBOOK_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_LAST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.LOCATION_PERMISSION_REQUEST_CODE;
import static com.afoodchronicle.utilities.Static.MARKER_NAME;
import static com.afoodchronicle.utilities.Static.ONLINE;
import static com.afoodchronicle.utilities.Static.THUMB_PHOTO_URL;
import static com.afoodchronicle.utilities.Static.USERS;
import static com.afoodchronicle.utilities.Static.mPantopoleio;
import static com.afoodchronicle.utilities.Static.mPnyka;
import static com.afoodchronicle.utilities.Static.mVorria;
import static com.afoodchronicle.utilities.Static.mYoleni;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnInfoWindowClickListener {


    // Markers

    private final HashMap<String, String> markerMap = new HashMap<>();


    // Maps

    private boolean mPermissionDenied = false;
    private SupportMapFragment sMapFragment;
    private GoogleMap mMap;


// --Commented out by Inspection START (22.03.2018 14:17):
//    // Views
//    private TextView logIn;
// --Commented out by Inspection STOP (22.03.2018 14:17)
    private ImageView profileImage;
    private TextView profileName;
    private TextView editProfile;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Firebase
    private DatabaseReference mDatabase;
    private String profileImageLink;
    private DatabaseReference userReference;
    private LoginManager mAuthFacebook;


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

        profileImage = parentView.findViewById(R.id.user_profile_image);
        profileName = parentView.findViewById(R.id.profile_name);
        editProfile = parentView.findViewById(R.id.edit_profile);
        ProgressBar progressBar;
        progressBar = parentView.findViewById(R.id.progressBar);

        mAuthFacebook = LoginManager.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        final ProgressBar finalProgressBar = progressBar;
        mAuthListener = new FirebaseAuth.AuthStateListener() {


            @SuppressLint("SetTextI18n")
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    final String online_user_id = user.getUid();
                    userReference = mDatabase.child(USERS).child(online_user_id);
                    userReference.child(ONLINE).setValue(true);

                    finalProgressBar.setVisibility(View.VISIBLE);
                    ///Facebook
                    if (FacebookUtils.isLoggedIn() && online_user_id != null) {

                        String firstName = Utils.getPreferences(FACEBOOK_FIRST_NAME, MainActivity.this);
                        String lastName = Utils.getPreferences(FACEBOOK_LAST_NAME, MainActivity.this);
                        profileName.setText(firstName + " " + lastName);

                        mDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(USERS).child(online_user_id).child(THUMB_PHOTO_URL).exists() && online_user_id != null) {
                                    profileImageLink = dataSnapshot.child(USERS).child(online_user_id).child(THUMB_PHOTO_URL).getValue().toString();
                                    Picasso.with(MainActivity.this).load(profileImageLink).networkPolicy(NetworkPolicy.OFFLINE)
                                                .into(profileImage, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError() {
                                                        Picasso.with(MainActivity.this).load(profileImageLink).into(profileImage,
                                                        new ImageLoadedCallback(finalProgressBar) {
                                                             @Override
                                                             public void onSuccess() {
                                                                     if (this.progressBar != null) {
                                                                     this.progressBar.setVisibility(View.GONE);
                                                        }
                                                    }
                                                });
                                                    }
                                                });
                                }
                                else
                                {
                                    if (mAuth.getUid() != null)
                                    {
                                        profileImageLink= Utils.getPreferences(FACEBOOK_PROFILE_PIC, MainActivity.this);
                                        if (profileImageLink.equals(""))
                                        {
                                            Picasso.with(MainActivity.this).load(R.drawable.default_profile).into(profileImage);

                                        }
                                        else
                                        {
                                        Picasso.with(MainActivity.this).load(profileImageLink).networkPolicy(NetworkPolicy.OFFLINE)
                                                .into(profileImage, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError() {
                                                        Picasso.with(MainActivity.this).load(profileImageLink).into(profileImage,
                                                                new ImageLoadedCallback(finalProgressBar) {
                                                                    @Override
                                                                    public void onSuccess() {
                                                                        if (this.progressBar != null) {
                                                                            this.progressBar.setVisibility(View.GONE);
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                        }
                                    }
                                }
                            }
                            ///
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
                    ///Email
                    else if (online_user_id != null){


                        String firstName = Utils.getPreferences(EMAIL_FIRST_NAME, MainActivity.this);
                        String lastName = Utils.getPreferences(EMAIL_LAST_NAME, MainActivity.this);
                        profileName.setText(firstName + " " + lastName);
                        mDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(USERS).child(mAuth.getUid()).child(THUMB_PHOTO_URL).exists() && mAuth.getUid() != null) {
                                    profileImageLink = dataSnapshot.child(USERS).child(mAuth.getUid()).child(THUMB_PHOTO_URL).getValue().toString();
                                    Picasso.with(MainActivity.this).load(profileImageLink).networkPolicy(NetworkPolicy.OFFLINE)
                                            .into(profileImage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(MainActivity.this).load(profileImageLink).into(profileImage,
                                                            new ImageLoadedCallback(finalProgressBar) {
                                                                @Override
                                                                public void onSuccess() {
                                                                    if (this.progressBar != null) {
                                                                        this.progressBar.setVisibility(View.GONE);
                                                                    }
                                                                }
                                                            });
                                                }
                                            });
                                } else {

                                    profileImageLink = Utils.getPreferences(EMAIL_PROFILE_PIC, MainActivity.this);
                                    if (profileImageLink.equals("")) {
                                        Picasso.with(MainActivity.this).load(R.drawable.default_profile).into(profileImage);
                                    } else {
                                        Picasso.with(MainActivity.this).load(profileImageLink).networkPolicy(NetworkPolicy.OFFLINE)
                                                .into(profileImage, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError() {
                                                        Picasso.with(MainActivity.this).load(profileImageLink).into(profileImage,
                                                                new ImageLoadedCallback(finalProgressBar) {
                                                                    @Override
                                                                    public void onSuccess() {
                                                                        if (this.progressBar != null) {
                                                                            this.progressBar.setVisibility(View.GONE);
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                }
                            }



                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

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
                //FirebaseUser signed out
                else {
                    logIn.setVisibility(View.VISIBLE);
                    profileName.setVisibility(View.GONE);
                    editProfile.setVisibility(View.GONE);
                }
            }
        };
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(mAuth.getUid() != null)
        {
            userReference.child(ONLINE).setValue(ServerValue.TIMESTAMP);
        }
        if (mAuthListener != null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }


    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout)
        {
         logOutUser();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOutUser()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if (user != null)
        {
            userReference.child(USERS).child(mAuth.getUid()).child(ONLINE).setValue(ServerValue.TIMESTAMP);
            mAuth.signOut();
            mAuthFacebook.logOut();
            Utils.setPreferences(EMAIL_PROFILE_PIC, "", MainActivity.this);
            Utils.setPreferences(FACEBOOK_PROFILE_PIC, "", MainActivity.this);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.edit_profile)
        {

        }
        if (id == R.id.chat)
        {
            Intent listIntent = new Intent(MainActivity.this, ChatFragmentsListActivity.class);

            startActivity(listIntent);
            // Handle the camera action
        }

         else if (id == R.id.favorites)
        {

        } else if (id == R.id.news)
        {

        } else if (id == R.id.settings)
        {
            Intent listIntent = new Intent(MainActivity.this, AllUsersActivity.class);

            startActivity(listIntent);

        }
        else if (id == R.id.logout)
        {
            logOutUser();

        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    ////////   MAPS


    private void initializeMaps()
    {

        sMapFragment = SupportMapFragment.newInstance();
        sMapFragment.getMapAsync(this);
        android.support.v4.app.FragmentManager sFm = getSupportFragmentManager();

        if (!sMapFragment.isAdded())
            sFm.beginTransaction().add(R.id.map, sMapFragment).commit();
        else
            sFm.beginTransaction().show(sMapFragment).commit();

    }
    @Override
    public void onMapReady(GoogleMap googleMap)
    {

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

    private void enableMyLocation()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this
            );
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    private void addMarkersToMap()
    {

        String id;

        Marker yoleni = mMap.addMarker(new MarkerOptions()
                .position(mYoleni)
                .title(getString(R.string.yoleni))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap())));

        id = yoleni.getId();
        markerMap.put(id, "yoleni");

        Marker vorria = mMap.addMarker(new MarkerOptions()
                .position(mVorria)
                .title(getString(R.string.vorria))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap())));

        id = vorria.getId();
        markerMap.put(id, "vorria");

        Marker pnyka = mMap.addMarker(new MarkerOptions()
                .position(mPnyka)
                .title(getString(R.string.pnyka))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap())));

        id = pnyka.getId();
        markerMap.put(id, "pnyka");

        Marker pantopoleio = mMap.addMarker(new MarkerOptions()
                .position(mPantopoleio)
                .title(getString(R.string.pantopoleio))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap())));

        id = pantopoleio.getId();
        markerMap.put(id, "pantopoleio");

    }
    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location)
    {
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        String m = markerMap.get(marker.getId());

        if (m.equals("yoleni"))
        {
            Intent i = new Intent(MainActivity.this, InfoWindowDetails.class);
            i.putExtra(MARKER_NAME,m);
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

    private Bitmap resizeBitmap()
    {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("marker", "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, 160, 160, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE)
        {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults
        ))
        {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        }
        else
        {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments()
    {
        super.onResumeFragments();
        if (mPermissionDenied)
        {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError()
    {
        PermissionUtils.PermissionDeniedDialog
                .newInstance().show(getSupportFragmentManager(), "dialog");
    }
}

