package com.example.autosos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.example.autosos.databinding.ActivityCustomerMapsBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private ActivityCustomerMapsBinding binding;

    TextView locationEditText;
    TextView driverMapsName, driverMapsPhoneNumber, driverMapsCarInfo, driverMapsCarNumber;
    RelativeLayout driverInfoRelativeLayout, customerUpdateDriverPosition;
    Button customerMenuButton, callDriverButton, calCurrentDriverButton, customerCancelOrderButton;

    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    Marker driverMarker, pickUpMarker;

    private String customerID;
    private LatLng customerPosition;
    private int radius = 1;
    private Boolean driverFound = false, requestType = false;

    private String driverFoundID;
    private DatabaseReference customerDatabaseRef;
    private DatabaseReference driversAvailableRef;

    private DatabaseReference driversLocationRef;
    private DatabaseReference driversRef;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Boolean currentLogOutDriverStatus;

    private ValueEventListener driverLocationRefListener;
    GeoQuery geoQuery;

    private Boolean currentUserOrderingStatus = false;

    public static String menuCustomerLatitude, menuCustomerLongitude;
    public static Location menuCurrentCustomerCoordinates;
    public static String currentDriverPhoneNumber;

    private String cancelOrderDriverID;
    private LatLng DriverLatLng;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locationEditText = (TextView) findViewById(R.id.locationEditText);
        driverMapsName = (TextView) findViewById(R.id.driverMapsName);
        driverMapsPhoneNumber = (TextView) findViewById(R.id.driverMapsPhoneNumber);
        driverMapsCarInfo = (TextView) findViewById(R.id.driverMapsCarInfo);
        driverMapsCarNumber = (TextView) findViewById(R.id.driverMapsCarNumber);
        driverInfoRelativeLayout = (RelativeLayout) findViewById(R.id.driverInfoRelativeLayout);
        driverInfoRelativeLayout.setVisibility(View.INVISIBLE);
        calCurrentDriverButton = (Button) findViewById(R.id.calCurrentDriverButton);
        calCurrentDriverButton.setVisibility(View.INVISIBLE);
        callDriverButton = (Button) findViewById(R.id.callDriverButton);
        customerMenuButton = (Button) findViewById(R.id.customerMenuButton);
        customerCancelOrderButton = (Button) findViewById(R.id.customerCancelOrderButton);
        customerCancelOrderButton.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        customerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests");
        driversAvailableRef = FirebaseDatabase.getInstance().getReference().child("Available Drivers");
        driversLocationRef = FirebaseDatabase.getInstance().getReference().child("Driver Working");

        customerUpdateDriverPosition = (RelativeLayout) findViewById(R.id.customerUpdateDriverPosition);
        customerUpdateDriverPosition.setVisibility(View.INVISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        callDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestType) {
                    requestType = false;
                    GeoFire geofire = new GeoFire(customerDatabaseRef);
                    geofire.removeLocation(customerID);
                    if (pickUpMarker != null) {
                        pickUpMarker.remove();
                    }
                    if (driverMarker != null) {
                        driverMarker.remove();
                    }

                    callDriverButton.setText("Викликати євакуатор");
                    if (driverFound != null) {
                        driversRef = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child("Drivers").child(driverFoundID).child("CustomerRideID");
                        driversRef.removeValue();
                        driverFoundID = null;
                    }

                    driverFound = false;
                    radius = 1;
                } else {
                    requestType = true;

                    GeoFire geofire = new GeoFire(customerDatabaseRef);
                    geofire.setLocation(customerID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                    customerPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    pickUpMarker = mMap.addMarker(new MarkerOptions().position(customerPosition).title("Я тут")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_image)));

                    callDriverButton.setText("Пошук...");
                    getNearbyDrivers();
                }
            }
        });

        CheckCurrentUserOrdering();
        customerMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUserOrderingStatus) {
                    Toast toast = Toast.makeText(CustomerMapsActivity.this, "Завершіть замовлення або відмініть замовлення",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else {
                    Intent intent = new Intent(CustomerMapsActivity.this, CustomerMenuActivity.class);
                    startActivity(intent);
                }
            }
        });

        customerCancelOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverInfoRelativeLayout.setVisibility(View.INVISIBLE);
                calCurrentDriverButton.setVisibility(View.INVISIBLE);
                customerCancelOrderButton.setVisibility(View.INVISIBLE);
                customerUpdateDriverPosition.setVisibility(View.INVISIBLE);
                callDriverButton.setText("Викликати евакуатор");
                locationEditText.setText("Приємного користування");
                CancelCurrentOrder();
                driverMarker.remove();
                pickUpMarker.remove();
            }
        });

        customerUpdateDriverPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNearbyDrivers();
            }
        });

        calCurrentDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(CustomerMapsActivity.this,
                        Manifest.permission.CALL_PHONE);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            CustomerMapsActivity.this, new String[]{Manifest.permission.CALL_PHONE},
                            123);
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: " + currentDriverPhoneNumber));
                    startActivity(intent);
                }
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
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @SuppressLint("DefaultLocale")
    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        menuCustomerLatitude = String.format("%.3f", lastLocation.getLatitude());
        menuCustomerLongitude = String.format("%.3f", lastLocation.getLongitude());
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
    }

    private void getNearbyDrivers() {
        GeoFire geoFire = new GeoFire(driversAvailableRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(customerPosition.latitude, customerPosition.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestType) {
                    driverFound = true;
                    final String driverFoundID = key;

                    cancelOrderDriverID = key;

                    driversRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideID", customerID);
                    driversRef.updateChildren(driverMap);

                    driverLocationRefListener = driversLocationRef.child(driverFoundID).child("l").
                            addValueEventListener(new ValueEventListener() {
                                @SuppressLint({"SetTextI18n", "DefaultLocale"})
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists() && requestType) {
                                        List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                                        double locationLat = 0;
                                        double locationLng = 0;

                                        callDriverButton.setText("Знайдено");

                                        driverInfoRelativeLayout.setVisibility(View.VISIBLE);
                                        calCurrentDriverButton.setVisibility(View.VISIBLE);
                                        customerCancelOrderButton.setVisibility(View.VISIBLE);
                                        customerUpdateDriverPosition.setVisibility(View.VISIBLE);
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                                .child("Users").child("Drivers").child(driverFoundID);

                                        reference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                                    String name = dataSnapshot.child("Name").getValue().toString();
                                                    String phone = dataSnapshot.child("Phone").getValue().toString();
                                                    String carName = dataSnapshot.child("CarName").getValue().toString();
                                                    String carNumber = dataSnapshot.child("CarNumber").getValue().toString();

                                                    driverMapsName.setText(name);
                                                    driverMapsPhoneNumber.setText(phone);
                                                    driverMapsCarInfo.setText(carName);
                                                    driverMapsCarNumber.setText(carNumber);

                                                    currentDriverPhoneNumber = phone;
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                                        });

                                        if (driverLocationMap.get(0) != null) {
                                            locationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                                        }
                                        if (driverLocationMap.get(1) != null) {
                                            locationLng = Double.parseDouble(driverLocationMap.get(1).toString());
                                        }
                                        DriverLatLng = new LatLng(locationLat, locationLng);

                                        if (driverMarker != null) {
                                            driverMarker.remove();
                                        }

                                        Location location1 = new Location("");
                                        location1.setLatitude(customerPosition.latitude);
                                        location1.setLongitude(customerPosition.longitude);

                                        Location location2 = new Location("");
                                        location2.setLatitude(DriverLatLng.latitude);
                                        location2.setLongitude(DriverLatLng.longitude);

                                        float Distance = location1.distanceTo(location2);
                                        if (Distance < 100 && Distance != 0) {
                                            callDriverButton.setText("Майже на місці");
                                        } else {
                                            float roadDistance = Distance / 1000;
                                            locationEditText.setText("Відстань до авто " + String.format("%.2f", roadDistance) + " км");
                                        }

                                        driverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng)
                                                .title("Ваше такси тут").icon(BitmapDescriptorFactory.fromResource(R.drawable.tow_truck_image)));

                                        if (Distance == 0) {
                                            Intent resultIntent = new Intent(CustomerMapsActivity.this, CustomerOrderResultActivity.class);
                                            startActivity(resultIntent);
                                            CancelCurrentOrder();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {}
                            });
                }
            }

            @Override
            public void onKeyExited(String key) {}

            @Override
            public void onKeyMoved(String key, GeoLocation location) {}

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius = radius + 1;
                    getNearbyDrivers();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {}
        });
    }

    private void CheckCurrentUserOrdering() {
        String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests")
                .child(customerID);

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

    private void CancelCurrentOrder() {

        DatabaseReference workingDriversRef = FirebaseDatabase.getInstance().getReference().child("Driver Working");
        GeoFire geoFireWorking = new GeoFire(workingDriversRef);
        geoFireWorking.removeLocation(cancelOrderDriverID);

        String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerRequestRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests");
        GeoFire geoFireRequest = new GeoFire(customerRequestRef);
        geoFireRequest.removeLocation(customerID);

        Task<Void> deleteCustomerID = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(cancelOrderDriverID).child("CustomerRideID").setValue(null);

        DatabaseReference availableDriversRef = FirebaseDatabase.getInstance().getReference().child("Available Drivers");
        GeoFire geoFireAvailability = new GeoFire(availableDriversRef);
        geoFireAvailability.setLocation(cancelOrderDriverID, new GeoLocation(DriverLatLng.latitude, DriverLatLng.longitude));

        driverFound = false;
        requestType = false;
        currentUserOrderingStatus = false;
    }
}