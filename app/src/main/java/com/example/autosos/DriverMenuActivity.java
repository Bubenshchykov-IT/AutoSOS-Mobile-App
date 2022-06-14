package com.example.autosos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class DriverMenuActivity extends AppCompatActivity {

    EditText menuDriverName, menuDriverPhone, menuDriverCarInfo, menuDriverCarNumber;
    ImageView closeDriverMenuButton, saveDriverMenuButton;
    TextView latitudeDriverInfo, longitudeDriverInfo;
    RelativeLayout exitDriverButton, deleteDriverButton, siteDriverButton, aboutDriverButton;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference, driverAvailableRef;

    private Boolean currentLogOutDriverStatus = false;
    private Boolean currentDriverWorkingStatus = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_menu);

        menuDriverName = (EditText) findViewById(R.id.menuDriverName);
        menuDriverPhone = (EditText) findViewById(R.id.menuDriverPhone);
        menuDriverCarInfo = (EditText) findViewById(R.id.menuDriverCarInfo);
        menuDriverCarNumber = (EditText) findViewById(R.id.menuDriverCarNumber);

        closeDriverMenuButton = (ImageView) findViewById(R.id.closeDriverMenuButton);
        saveDriverMenuButton = (ImageView) findViewById(R.id.saveDriverMenuButton);

        latitudeDriverInfo = (TextView) findViewById(R.id.latitudeDriverInfo);
        latitudeDriverInfo.setText(DriverMapsActivity.menuDriverLatitude);
        longitudeDriverInfo = (TextView) findViewById(R.id.longitudeDriverInfo);
        longitudeDriverInfo.setText(DriverMapsActivity.menuDriverLongitude);

        exitDriverButton = (RelativeLayout) findViewById(R.id.exitDriverButton);
        deleteDriverButton = (RelativeLayout) findViewById(R.id.deleteDriverButton);
        siteDriverButton = (RelativeLayout) findViewById(R.id.siteDriverButton);
        aboutDriverButton = (RelativeLayout) findViewById(R.id.aboutDriverButton);

        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
        getUserInformation();

        closeDriverMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(DriverMenuActivity.this, DriverMapsActivity.class);
                startActivity(backIntent);
            }
        });

        saveDriverMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    validateAndSaveInfo();
                }
        });

        exitDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDriverWorkingStatus) {
                    Toast.makeText(DriverMenuActivity.this, "Завершіть Ваше замовлення", Toast.LENGTH_SHORT).show();
                }
                else {
                    currentLogOutDriverStatus = true;
                    LogoutDriver();
                    DisconnectDriver();
                    mAuth.signOut();
                }
            }
        });

        deleteDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDriverWorkingStatus) {
                    Toast.makeText(DriverMenuActivity.this, "Завершіть Ваше замовлення", Toast.LENGTH_SHORT).show();
                }
                else {
                    removeCurrentDriver();
                    LogoutDriver();
                }
            }
        });

        siteDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent siteIntent = new Intent(DriverMenuActivity.this, SiteActivity.class);
                siteIntent.putExtra("type", "Driver");
                startActivity(siteIntent);
            }
        });

        aboutDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aboutIntent = new Intent(DriverMenuActivity.this, AboutActivity.class);
                aboutIntent.putExtra("type", "Driver");
                startActivity(aboutIntent);
            }
        });
    }

    private void validateAndSaveInfo() {
        if (TextUtils.isEmpty(menuDriverName.getText().toString())) {
            Toast.makeText(this, "Заповніть поле Name", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(menuDriverPhone.getText().toString())) {
            Toast.makeText(this, "Заповніть поле Phone", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(menuDriverCarInfo.getText().toString())) {
            Toast.makeText(this, "Заповніть поле Auto", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(menuDriverCarNumber.getText().toString())) {
            Toast.makeText(this, "Заповніть поле Auto", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("DriverID", mAuth.getCurrentUser().getUid());
            userMap.put("Name", menuDriverName.getText().toString());
            userMap.put("Phone", menuDriverPhone.getText().toString());
            userMap.put("CarName", menuDriverCarInfo.getText().toString());
            userMap.put("CarNumber", menuDriverCarNumber.getText().toString());

            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
            startActivity(new Intent(DriverMenuActivity.this, DriverMapsActivity.class));
        }
    }

    private void getUserInformation() {

        databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    String name = snapshot.child("Name").getValue().toString();
                    String phone = snapshot.child("Phone").getValue().toString();
                    String carInfo = snapshot.child("CarName").getValue().toString();
                    String carNumber = snapshot.child("CarNumber").getValue().toString();

                    menuDriverName.setText(name);
                    menuDriverPhone.setText(phone);
                    menuDriverCarInfo.setText(carInfo);
                    menuDriverCarNumber.setText(carNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void DisconnectDriver() {
        String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference driverDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(driverID).child("Online Status");
        driverDatabaseRef.setValue(false);

        DatabaseReference availableDriversRef = FirebaseDatabase.getInstance().getReference().child("Available Drivers");
        GeoFire geoFire = new GeoFire(availableDriversRef);
        geoFire.removeLocation(driverID);
    }

    private void LogoutDriver() {
        Intent roleSelectionIntent = new Intent(DriverMenuActivity.this, RoleSelectionActivity.class);
        startActivity(roleSelectionIntent);
        finish();
    }

    private void CheckCurrentDriverWorking() {
        String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference().child("Driver Working").child(driverID);

        checkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentDriverWorkingStatus = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void removeCurrentDriver() {

        String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Task<Void> removeReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverID).removeValue();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUser.delete();
    }
}