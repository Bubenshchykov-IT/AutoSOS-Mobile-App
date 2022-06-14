package com.example.autosos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerRegistrationActivity extends AppCompatActivity {

    EditText registrationEmailCustomerEditText, registrationPasswordCustomerEditText;
    Button registrationCustomerButton;

    FirebaseAuth firebaseAuth;
    DatabaseReference customerDatabaseRef;
    String onlineCustomerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_registration);

        registrationEmailCustomerEditText = (EditText) findViewById(R.id.registrationEmailCustomerEditText);
        registrationPasswordCustomerEditText = (EditText) findViewById(R.id.registrationPasswordCustomerEditText);
        registrationCustomerButton = (Button) findViewById(R.id.registrationCustomerButton);

        firebaseAuth = FirebaseAuth.getInstance();

        registrationCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(registrationEmailCustomerEditText.getText().toString())) {
                    Toast.makeText(CustomerRegistrationActivity.this, "Заповніть поле email-адреси",
                            Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(registrationPasswordCustomerEditText.getText().toString())) {
                    Toast.makeText(CustomerRegistrationActivity.this, "Заповніть поле з паролем",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    String email = registrationEmailCustomerEditText.getText().toString();
                    String password = registrationPasswordCustomerEditText.getText().toString();
                    RegistrationCustomerMethod(email, password);
                }
            }
        });
    }

    private void RegistrationCustomerMethod(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    onlineCustomerID = firebaseAuth.getCurrentUser().getUid();
                    customerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Customers").child(onlineCustomerID);

                    Intent mainScreenIntent = new Intent(CustomerRegistrationActivity.this, CustomerInfoRegistrationActivity.class);
                    startActivity(mainScreenIntent);

                    Toast.makeText(CustomerRegistrationActivity.this, "Реєстрація успішна",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(CustomerRegistrationActivity.this, "Помилка реєстрації нового користувача",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}