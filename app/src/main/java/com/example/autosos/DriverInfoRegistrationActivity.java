package com.example.autosos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class DriverInfoRegistrationActivity extends AppCompatActivity {

    EditText driverInfoName, driverInfoPhone, driverInfoCarName, driverInfoCarNumber;
    Button driverCreateButton;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_info_registration);

        driverInfoName = (EditText) findViewById(R.id.driverInfoName);
        driverInfoPhone = (EditText) findViewById(R.id.driverInfoPhone);
        driverInfoCarName = (EditText) findViewById(R.id.driverInfoCarName);
        driverInfoCarNumber = (EditText) findViewById(R.id.driverInfoCarNumber);
        driverCreateButton = (Button) findViewById(R.id.driverCreateButton);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");

        driverCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveInfo();
            }
        });
    }

    private void validateAndSaveInfo() {
        if (TextUtils.isEmpty(driverInfoName.getText().toString())) {
            Toast.makeText(this, "Заповніть поле з Вашим іменем", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(driverInfoPhone.getText().toString())) {
            Toast.makeText(this, "Заповніть поле з номером телефону", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(driverInfoCarName.getText().toString())) {
            Toast.makeText(this, "Заповніть поле інформації про Ваш евакуатор", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(driverInfoCarNumber.getText().toString())) {
            Toast.makeText(this, "Заповніть поле інформації про Ваш номер авто", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("DriverID", mAuth.getCurrentUser().getUid());
            userMap.put("Name", driverInfoName.getText().toString());
            userMap.put("Phone", driverInfoPhone.getText().toString());
            userMap.put("CarName", driverInfoCarName.getText().toString());
            userMap.put("CarNumber", driverInfoCarNumber.getText().toString());
            userMap.put("Online Status", true);

            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
            startActivity(new Intent(DriverInfoRegistrationActivity.this, DriverMapsActivity.class));
        }
    }
}