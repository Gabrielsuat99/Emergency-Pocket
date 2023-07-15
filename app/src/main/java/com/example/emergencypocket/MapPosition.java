package com.example.emergencypocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.internal.ICameraUpdateFactoryDelegate;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Build;
import java.security.Permission;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;

public class MapPosition extends AppCompatActivity implements OnMapReadyCallback {

    private final int FINE_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    private DatabaseReference db;
    private String username;

    SimpleDateFormat dateFormat;
    String currentDateAndTime;

    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_position);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        username = getIntent().getStringExtra("name");
        db = FirebaseDatabase.getInstance().getReferenceFromUrl("https://emergency-pocket-98c73-default-rtdb.firebaseio.com/");

        Button buttonFindLocation = findViewById(R.id.buttonFindLocation);
        buttonFindLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastLocation();
            }
        });

        // Initialize Places API
        String apiKey = getString(R.string.MAPS_API_KEY);
        Places.initialize(getApplicationContext(), apiKey);

        // Create a Places client
        placesClient = Places.createClient(this);

    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MapPosition.this);

                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        BitmapDescriptor markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);

        LatLng sydney = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        myMap.addMarker(new MarkerOptions().position(sydney).title("My location").icon(markerIcon));
        myMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentDateAndTime = dateFormat.format(new Date());

        db = FirebaseDatabase.getInstance().getReference("users");

        db.child(username).child("GPS Current Location").child("Latitude").setValue(currentLocation.getLatitude());
        db.child(username).child("GPS Current Location").child("Longitude").setValue(currentLocation.getLongitude());
        db.child(username).child("GPS Current Location").child("Time reported").setValue(currentDateAndTime);
        db.child(username).child("User-Agent").setValue(Build.MANUFACTURER + " " + Build.MODEL);
        Toast.makeText(this, "User Current Location updated", Toast.LENGTH_SHORT).show();

        fetchNearbyHospitals();
    }



    private void fetchNearbyHospitals() {
        // Create the request parameters
        String placeType = "hospital";
        int radius = 5000; // in meters
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        // Create the request
        String locationQuery = currentLatLng.latitude + "," + currentLatLng.longitude;
        String apikey = getString(R.string.MAPS_API_KEY);
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                locationQuery + "&radius=" + radius + "&type=" + placeType + "&key=" + apikey;

        // Perform the request
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        int markerCount = 0; // Counter variable
                        for (int i = 0; i < results.length(); i++) {
                            if (markerCount >= 15) {
                                break; // Break the loop if the marker limit is reached
                            }
                            JSONObject result = results.getJSONObject(i);
                            JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
                            double lat = location.getDouble("lat");
                            double lng = location.getDouble("lng");
                            String name = result.getString("name");

                            LatLng hospitalLatLng = new LatLng(lat, lng);
                            myMap.addMarker(new MarkerOptions().position(hospitalLatLng).title(name));
                            markerCount++; // Increment the counter variable
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching nearby hospitals: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });

        // Add the request to the queue
        queue.add(request);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission is denied, please allow the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
