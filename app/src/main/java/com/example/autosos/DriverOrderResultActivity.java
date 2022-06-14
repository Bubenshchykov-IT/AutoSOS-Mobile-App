package com.example.autosos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DriverOrderResultActivity extends AppCompatActivity {

    Button driverResultButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_order_result);

        driverResultButton = (Button) findViewById(R.id.driverResultButton);

        driverResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backMapIntent = new Intent(DriverOrderResultActivity.this, DriverMapsActivity.class);
                startActivity(backMapIntent);
            }
        });
    }
}