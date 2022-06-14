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

public class CustomerMenuActivity extends AppCompatActivity {

    EditText menuCustomerName, menuCustomerPhone, menuCustomerCarInfo, menuCustomerCarNumber;
    ImageView closeCustomerMenuButton, saveCustomerMenuButton;
    TextView latitudeCustomerInfo, longitudeCustomerInfo;
    RelativeLayout exitCustomerButton, deleteCustomerButton, siteCustomerButton, aboutCustomerButton;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference, driverAvailableRef;

    private Boolean currentLogOutCustomerStatus = false;
    private Boolean currentDriverWorkingStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_menu);

        menuCustomerName = (EditText) findViewById(R.id.menuCustomerName);
        menuCustomerPhone = (EditText) findViewById(R.id.menuCustomerPhone);
        menuCustomerCarInfo = (EditText) findViewById(R.id.menuCustomerCarInfo);
        menuCustomerCarNumber = (EditText) findViewById(R.id.menuCustomerCarNumber);

        closeCustomerMenuButton = (ImageView) findViewById(R.id.closeCustomerMenuButton);
        saveCustomerMenuButton = (ImageView) findViewById(R.id.saveCustomerMenuButton);

        latitudeCustomerInfo = (TextView) findViewById(R.id.latitudeCustomerInfo);
        latitudeCustomerInfo.setText(CustomerMapsActivity.menuCustomerLatitude);
        longitudeCustomerInfo = (TextView) findViewById(R.id.longitudeCustomerInfo);
        longitudeCustomerInfo.setText(CustomerMapsActivity.menuCustomerLongitude);

        exitCustomerButton = (RelativeLayout) findViewById(R.id.exitCustomerButton);
        deleteCustomerButton = (RelativeLayout) findViewById(R.id.deleteCustomerButton);
        siteCustomerButton = (RelativeLayout) findViewById(R.id.siteCustomerButton);
        aboutCustomerButton = (RelativeLayout) findViewById(R.id.aboutCustomerButton);

        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
        getUserInformation();
        CheckCurrentUserOrdering();

        closeCustomerMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(CustomerMenuActivity.this, CustomerMapsActivity.class);
                startActivity(backIntent);
            }
        });

        saveCustomerMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveInfo();
            }
        });

        exitCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDriverWorkingStatus) {
                    Toast.makeText(CustomerMenuActivity.this, "Завершіть Ваше замовлення", Toast.LENGTH_SHORT).show();
                }
                else {
                    currentLogOutCustomerStatus = true;
                    LogoutCustomer();
                    DisconnectCustomer();
                    mAuth.signOut();
                }
            }
        });

        deleteCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDriverWorkingStatus) {
                    Toast.makeText(CustomerMenuActivity.this, "Завершіть Ваше замовлення", Toast.LENGTH_SHORT).show();
                }
                else {
                    removeCurrentCustomer();
                    LogoutCustomer();
                }
            }
        });

        siteCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent siteIntent = new Intent(CustomerMenuActivity.this, SiteActivity.class);
                siteIntent.putExtra("type", "Customer");
                startActivity(siteIntent);
            }
        });

        aboutCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aboutIntent = new Intent(CustomerMenuActivity.this, AboutActivity.class);
                aboutIntent.putExtra("type", "Customer");
                startActivity(aboutIntent);
            }
        });

    }

    private void validateAndSaveInfo() {
        if (TextUtils.isEmpty(menuCustomerName.getText().toString())) {
            Toast.makeText(this, "Заповніть поле Name", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(menuCustomerPhone.getText().toString())) {
            Toast.makeText(this, "Заповніть поле Phone", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(menuCustomerCarInfo.getText().toString())) {
            Toast.makeText(this, "Заповніть поле Auto", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(menuCustomerCarNumber.getText().toString())) {
            Toast.makeText(this, "Заповніть поле Auto", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("UserID", mAuth.getCurrentUser().getUid());
            userMap.put("Name", menuCustomerName.getText().toString());
            userMap.put("Phone", menuCustomerPhone.getText().toString());
            userMap.put("CarName", menuCustomerCarInfo.getText().toString());
            userMap.put("CarNumber", menuCustomerCarNumber.getText().toString());

            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
            startActivity(new Intent(CustomerMenuActivity.this, CustomerMapsActivity.class));
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

                    menuCustomerName.setText(name);
                    menuCustomerPhone.setText(phone);
                    menuCustomerCarInfo.setText(carInfo);
                    menuCustomerCarNumber.setText(carNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void DisconnectCustomer() {
        String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference customerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customers").child(customerID).child("Online Status");
        customerDatabaseRef.setValue(false);

        DatabaseReference customerRequestRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests");
        GeoFire geoFire = new GeoFire(customerRequestRef);
        geoFire.removeLocation(customerID);
    }

    private void LogoutCustomer() {
        Intent roleSelectionIntent = new Intent(CustomerMenuActivity.this, RoleSelectionActivity.class);
        startActivity(roleSelectionIntent);
        finish();
    }

    private void removeCurrentCustomer() {

        String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Task<Void> removeReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Customers").child(customerID).removeValue();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUser.delete();
    }

    private void CheckCurrentUserOrdering() {
        String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference checkRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests")
                .child(customerID);

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
}