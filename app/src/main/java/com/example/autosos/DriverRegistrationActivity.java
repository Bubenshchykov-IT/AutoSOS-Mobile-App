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

public class DriverRegistrationActivity extends AppCompatActivity {

    EditText registrationEmailDriverEditText, registrationPasswordDriverEditText;
    Button registrationDriverButton;

    FirebaseAuth firebaseAuth;
    DatabaseReference driverDatabaseRef;
    String onlineDriverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_registration);

        registrationEmailDriverEditText = (EditText) findViewById(R.id.registrationEmailDriverEditText);
        registrationPasswordDriverEditText = (EditText) findViewById(R.id.registrationPasswordDriverEditText);
        registrationDriverButton = (Button) findViewById(R.id.registrationDriverButton);

        firebaseAuth = FirebaseAuth.getInstance();

        registrationDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(registrationEmailDriverEditText.getText().toString())) {
                    Toast.makeText(DriverRegistrationActivity.this, "Заповніть поле email-адреси",
                            Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(registrationPasswordDriverEditText.getText().toString())) {
                    Toast.makeText(DriverRegistrationActivity.this, "Заповніть поле з паролем",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    String email = registrationEmailDriverEditText.getText().toString();
                    String password = registrationPasswordDriverEditText.getText().toString();
                    RegistrationDriverMethod(email, password);
                }
            }
        });
    }

    private void RegistrationDriverMethod(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    onlineDriverID = firebaseAuth.getCurrentUser().getUid();
                    driverDatabaseRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Drivers").child(onlineDriverID);

                    Intent mainScreenIntent = new Intent(DriverRegistrationActivity.this, DriverInfoRegistrationActivity.class);
                    startActivity(mainScreenIntent);

                    Toast.makeText(DriverRegistrationActivity.this, "Реєстрація успішна",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(DriverRegistrationActivity.this, "Помилка реєстрації нового користувача",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}