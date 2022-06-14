package com.example.autosos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.autosos.databinding.ActivityDriverMapsBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private GoogleMap mMap;
    private ActivityDriverMapsBinding binding;

    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    Marker driverPickUpMarker;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Boolean currentLogOutDriverStatus = false;
    private DatabaseReference assignedCustomerRef, assignedCustomerPositionRef;
    private String driverID, customerID = "";

    private ValueEventListener assignedCustomerPositionListener;

    public static String menuDriverLatitude, menuDriverLongitude;
    public static Location menuCurrentDriverCoordinates;

    TextView locationDriverTextView, customerMapsName, customerMapsPhoneNumber, customerMapsCarInfo, customerMapsCarNumber;
    Button driverMenuButton, calCurrentCustomerButton;
    RelativeLayout customerInfoRelativeLayout;

    private Boolean currentUserOrderingStatus = false;

    public static String currentCustomerPhoneNumber;
    public static float roadDriverCustomerDistance;

    private LatLng driverCurrentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        driverID = mAuth.getCurrentUser().getUid();

        locationDriverTextView = (TextView) findViewById(R.id.locationDriverTextView);
        customerMapsName = (TextView) findViewById(R.id.customerMapsName);
        customerMapsPhoneNumber = (TextView) findViewById(R.id.customerMapsPhoneNumber);
        customerMapsCarInfo = (TextView) findViewById(R.id.customerMapsCarInfo);
        customerMapsCarNumber = (TextView) findViewById(R.id.customerMapsCarNumber);

        calCurrentCustomerButton = (Button) findViewById(R.id.calCurrentCustomerButton);
        calCurrentCustomerButton.setVisibility(View.INVISIBLE);
        driverMenuButton = (Button) findViewById(R.id.driverMenuButton);

        customerInfoRelativeLayout = (RelativeLayout) findViewById(R.id.customerInfoRelativeLayout);
        customerInfoRelativeLayout.setVisibility(View.INVISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        CheckCurrentUserOrdering();
        driverMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUserOrderingStatus) {
                    Toast toast = Toast.makeText(DriverMapsActivity.this, "Завершіть Ваше замовлення",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else {
                    Intent intent = new Intent(DriverMapsActivity.this, DriverMenuActivity.class);
                    startActivity(intent);
                }
            }
        });

        calCurrentCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(DriverMapsActivity.this,
                        Manifest.permission.CALL_PHONE);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            DriverMapsActivity.this, new String[]{Manifest.permission.CALL_PHONE},
                            123);
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: " + currentCustomerPhoneNumber));
                    startActivity(intent);
                }
            }
        });

        getAssignedCustomerRequest();
    }

    private void getAssignedCustomerRequest() {
        assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverID).child("CustomerRideID");

        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    customerID = snapshot.getValue().toString();

                    getAssignedCustomerPosition();
                }
                else {
                    customerID = "";
                    customerInfoRelativeLayout.setVisibility(View.INVISIBLE);
                    calCurrentCustomerButton.setVisibility(View.INVISIBLE);
                    locationDriverTextView.setText("Приємного користування");
                    if (driverPickUpMarker != null) {
                        driverPickUpMarker.remove();
                    }
                    if (assignedCustomerPositionListener != null) {
                        assignedCustomerPositionRef.removeEventListener(assignedCustomerPositionListener);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAssignedCustomerPosition() {
        assignedCustomerPositionRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests")
                .child(customerID).child("l");

        assignedCustomerPositionListener = assignedCustomerPositionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Object> customerPositionMap = (List<Object>) snapshot.getValue();
                    double locationLat = Double.parseDouble(customerPositionMap.get(0).toString());
                    double locationLng = Double.parseDouble(customerPositionMap.get(1).toString());

                    LatLng customerLatLng = new LatLng(locationLat, locationLng);
                    driverPickUpMarker = mMap.addMarker(new MarkerOptions().position(customerLatLng).title("Забрати клієнта тут")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_image)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(customerLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                    customerInfoRelativeLayout.setVisibility(View.VISIBLE);
                    calCurrentCustomerButton.setVisibility(View.VISIBLE);
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Customers").child(customerID);

                    reference.addValueEventListener(new ValueEventListener() {
                        @SuppressLint({"DefaultLocale", "SetTextI18n"})
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                String name = dataSnapshot.child("Name").getValue().toString();
                                String phone = dataSnapshot.child("Phone").getValue().toString();
                                String carName = dataSnapshot.child("CarName").getValue().toString();
                                String carNumber = dataSnapshot.child("CarNumber").getValue().toString();

                                customerMapsName.setText(name);
                                customerMapsPhoneNumber.setText(phone);
                                customerMapsCarInfo.setText(carName);
                                customerMapsCarNumber.setText(carNumber);

                                currentCustomerPhoneNumber = phone;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });

                    Location location1 = new Location("");
                    location1.setLatitude(driverCurrentPosition.latitude);
                    location1.setLongitude(driverCurrentPosition.longitude);

                    Location location2 = new Location("");
                    location2.setLatitude(customerLatLng.latitude);
                    location2.setLongitude(customerLatLng.longitude);

                    float Distance = location1.distanceTo(location2);
                    float roadDistance = Distance / 1000;
                    locationDriverTextView.setText("Відстань до авто " + String.format("%.2f", roadDistance) + " км");


                    if (Distance == 0) {

                        Intent resultIntent = new Intent(DriverMapsActivity.this, DriverOrderResultActivity.class);
                        startActivity(resultIntent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(100000);
        locationRequest.setFastestInterval(100000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {
            lastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

            driverCurrentPosition = new LatLng(location.getLatitude(), location.getLongitude());

            menuCurrentDriverCoordinates = location;
            menuDriverLatitude = String.format("%.3f", location.getLatitude());
            menuDriverLongitude = String.format("%.3f", location.getLongitude());

            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference availableDriversRef = FirebaseDatabase.getInstance().getReference().child("Available Drivers");
            GeoFire geoFireAvailability = new GeoFire(availableDriversRef);

            DatabaseReference driverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Driver Working");
            GeoFire geoFireWorking = new GeoFire(driverWorkingRef);

            switch (customerID) {
                case "":
                    geoFireWorking.removeLocation(userID);
                    geoFireAvailability.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailability.removeLocation(userID);
                    geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!currentLogOutDriverStatus) {
            DisconnectDriver();
        }
    }

    private void DisconnectDriver() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference availableDriversRef = FirebaseDatabase.getInstance().getReference().child("Available Drivers");
        GeoFire geoFire = new GeoFire(availableDriversRef);
        geoFire.removeLocation(userID);
    }

    private void LogoutDriver() {
        Intent welcomeIntent = new Intent(DriverMapsActivity.this, RoleSelectionActivity.class);
        startActivity(welcomeIntent);
        finish();
    }

    private void CheckCurrentUserOrdering() {
        String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference().child("Driver Working")
                .child(driverID);

        checkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserOrderingStatus = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}