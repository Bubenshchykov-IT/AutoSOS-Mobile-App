package com.example.autosos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class RoleSelectionActivity extends AppCompatActivity {

    ImageView roleSelectionCustomerButton, roleSelectionDriverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        roleSelectionCustomerButton = (ImageView) findViewById(R.id.roleSelectionCustomerButton);
        roleSelectionDriverButton = (ImageView) findViewById(R.id.roleSelectionDriverButton);

        roleSelectionCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent welcomeIntent = new Intent(RoleSelectionActivity.this, CustomerWelcomeActivity.class);
                startActivity(welcomeIntent);
            }
        });

        roleSelectionDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent welcomeDriverIntent = new Intent(RoleSelectionActivity.this, DriverWelcomeActivity.class);
                startActivity(welcomeDriverIntent);
            }
        });
    }
}