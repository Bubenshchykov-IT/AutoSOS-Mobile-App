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

public class CustomerWelcomeActivity extends AppCompatActivity {

    EditText editTextCustomerEmail, editTextCustomerPassword;
    Button signInCustomerButton, roleBackCustomerButton, signUpCustomerButton;

    FirebaseAuth firebaseAuth;
    DatabaseReference customerDatabaseRef;
    String onlineCustomerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_welcome);

        editTextCustomerEmail = (EditText) findViewById(R.id.editTextCustomerEmail);
        editTextCustomerPassword = (EditText) findViewById(R.id.editTextCustomerPassword);
        signInCustomerButton = (Button) findViewById(R.id.signInCustomerButton);
        signUpCustomerButton = (Button) findViewById(R.id.signUpCustomerButton);
        roleBackCustomerButton = (Button) findViewById(R.id.roleBackCustomerButton);

        firebaseAuth = FirebaseAuth.getInstance();

        signInCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextCustomerEmail.getText().toString();
                String password = editTextCustomerPassword.getText().toString();
                SignInCustomerMethod(email, password);
            }
        });

        signUpCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registrationIntent = new Intent(CustomerWelcomeActivity.this, CustomerRegistrationActivity.class);
                startActivity(registrationIntent);
            }
        });

        roleBackCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent roleSelectionIntent = new Intent(CustomerWelcomeActivity.this, RoleSelectionActivity.class);
                startActivity(roleSelectionIntent);
            }
        });
    }

    private void SignInCustomerMethod(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CustomerWelcomeActivity.this, "Вхід успішний", Toast.LENGTH_SHORT).show();

                    onlineCustomerID = firebaseAuth.getCurrentUser().getUid();
                    customerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Customers").child(onlineCustomerID).child("Online Status");
                    customerDatabaseRef.setValue(true);

                    Intent mainScreenIntent = new Intent(CustomerWelcomeActivity.this, CustomerMapsActivity.class);
                    startActivity(mainScreenIntent);
                }
                else {
                    Toast.makeText(CustomerWelcomeActivity.this, "Помилка входу",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}