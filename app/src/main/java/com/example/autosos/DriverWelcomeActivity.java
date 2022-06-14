package com.example.autosos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

public class DriverWelcomeActivity extends AppCompatActivity {

    EditText editTextDriverEmail, editTextDriverPassword;
    Button signInDriverButton, roleBackDriverButton, signUpDriverButton;

    FirebaseAuth firebaseAuth;
    DatabaseReference driverDatabaseRef, driverAvailableRef;
    String onlineDriverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_welcome);

        editTextDriverEmail = (EditText) findViewById(R.id.editTextDriverEmail);
        editTextDriverPassword = (EditText) findViewById(R.id.editTextDriverPassword);
        signInDriverButton = (Button) findViewById(R.id.signInDriverButton);
        signUpDriverButton = (Button) findViewById(R.id.signUpDriverButton);
        roleBackDriverButton = (Button) findViewById(R.id.roleBackDriverButton);

        firebaseAuth = FirebaseAuth.getInstance();

        signInDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextDriverEmail.getText().toString();
                String password = editTextDriverPassword.getText().toString();
                SignInDriverMethod(email, password);
            }
        });

        signUpDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registrationIntent = new Intent(DriverWelcomeActivity.this, DriverRegistrationActivity.class);
                startActivity(registrationIntent);
            }
        });

        roleBackDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent roleSelectionIntent = new Intent(DriverWelcomeActivity.this, RoleSelectionActivity.class);
                startActivity(roleSelectionIntent);
            }
        });
    }

    private void SignInDriverMethod(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(DriverWelcomeActivity.this, "Вхід успішний", Toast.LENGTH_SHORT).show();

                    onlineDriverID = firebaseAuth.getCurrentUser().getUid();
                    driverDatabaseRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Drivers").child(onlineDriverID).child("Online Status");
                    driverDatabaseRef.setValue(true);

                    Intent mainScreenIntent = new Intent(DriverWelcomeActivity.this, DriverMapsActivity.class);
                    startActivity(mainScreenIntent);
                }
                else {
                    Toast.makeText(DriverWelcomeActivity.this, "Помилка входу",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}