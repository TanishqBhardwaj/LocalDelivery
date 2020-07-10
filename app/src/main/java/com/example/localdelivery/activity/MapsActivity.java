package com.example.localdelivery.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.example.localdelivery.R;
import com.example.localdelivery.utils.PrefUtils;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity {

    private PrefUtils prefUtils;
    private Context mContext;
    private Activity mActivity;
    private SupportMapFragment mapFragment;
    private ImageView imageViewProceed;
    private GoogleMap mMap;

    OnMapReadyCallback callback = new OnMapReadyCallback() {

        @SuppressLint("MissingPermission")
        @Override
        public void onMapReady(GoogleMap googleMap) {
            Log.e("Maps Fragment", "onMapReady: " + prefUtils.getLatitude() + "KK  " +
                    prefUtils.getLongitude());
            LatLng yourLocation = new LatLng(Double.parseDouble(prefUtils.getLatitude()),
                    Double.parseDouble(prefUtils.getLongitude()));
            googleMap.addMarker(new MarkerOptions().position(yourLocation).title("My Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yourLocation, 15));
            googleMap.setMyLocationEnabled(true);
            setMapLongClick(googleMap);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        prefUtils = new PrefUtils(MapsActivity.this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        imageViewProceed = findViewById(R.id.imageViewProceed);
        imageViewProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        setGoogleSearch();
    }

    private void setGoogleSearch() {
        String apiKey = getString(R.string.google_maps_key);

        if (!Places.isInitialized()) {
            Places.initialize(MapsActivity.this, apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(MapsActivity.this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                Log.e("Maps Fragment", "Place: " + place.getName() + ", " + place.getId());
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            }


            @Override
            public void onError(@NotNull Status status) {
                Log.e("Maps Fragment", "An error occurred: " + status);
            }
        });
    }

    private void setMapLongClick(final GoogleMap map) {
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Dropped Pin")
                        .icon(BitmapDescriptorFactory.defaultMarker
                                (BitmapDescriptorFactory.HUE_BLUE))
                        .snippet(getCompleteAddressString(latLng.latitude, latLng.longitude)));

                setAlertBox(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude),
                        getCompleteAddressString(latLng.latitude, latLng.longitude));
            }

            //converting lat long to address string
            private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
                String strAdd = "";
                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
                    if (addresses != null) {
                        Address returnedAddress = addresses.get(0);
                        StringBuilder strReturnedAddress = new StringBuilder("");

                        for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                        }
                        strAdd = strReturnedAddress.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return strAdd;
            }

            private void setAlertBox(final String latitude, final String longitude, final String address) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setCancelable(false);
                builder.setTitle("New Location");
                builder.setMessage("Do you want to make this location as your new location?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefUtils.setLatitude(latitude);
                        prefUtils.setLongitude(longitude);
                        prefUtils.setAddress(address);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }
}