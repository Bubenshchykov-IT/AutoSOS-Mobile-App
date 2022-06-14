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

public class CustomerInfoRegistrationActivity extends AppCompatActivity {

    EditText customerInfoName, customerInfoPhone, customerInfoCarName, customerInfoCarNumber;
    Button customerCreateButton;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_info_registration);

        customerInfoName = (EditText) findViewById(R.id.customerInfoName);
        customerInfoPhone = (EditText) findViewById(R.id.customerInfoPhone);
        customerInfoCarName = (EditText) findViewById(R.id.customerInfoCarName);
        customerInfoCarNumber = (EditText) findViewById(R.id.customerInfoCarNumber);
        customerCreateButton = (Button) findViewById(R.id.customerCreateButton);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");

        customerCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveInfo();
            }
        });
    }

    private void validateAndSaveInfo() {
        if (TextUtils.isEmpty(customerInfoName.getText().toString())) {
            Toast.makeText(this, "Заповніть поле з Вашим іменем", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(customerInfoPhone.getText().toString())) {
            Toast.makeText(this, "Заповніть поле з номером телефону", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(customerInfoCarName.getText().toString())) {
            Toast.makeText(this, "Заповніть поле інформації про Ваше авто", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(customerInfoCarNumber.getText().toString())) {
            Toast.makeText(this, "Заповніть поле інформації про Ваш номер авто", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("UserID", mAuth.getCurrentUser().getUid());
            userMap.put("Name", customerInfoName.getText().toString());
            userMap.put("Phone", customerInfoPhone.getText().toString());
            userMap.put("CarName", customerInfoCarName.getText().toString());
            userMap.put("CarNumber", customerInfoCarNumber.getText().toString());
            userMap.put("Online Status", true);

            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
            startActivity(new Intent(CustomerInfoRegistrationActivity.this, CustomerMapsActivity.class));
        }
    }
}