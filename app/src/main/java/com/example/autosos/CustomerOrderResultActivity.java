package com.example.autosos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CustomerOrderResultActivity extends AppCompatActivity {

    Button customerResultButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_result);

        customerResultButton = (Button) findViewById(R.id.customerResultButton);

        customerResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backMapIntent = new Intent(CustomerOrderResultActivity.this, CustomerMapsActivity.class);
                startActivity(backMapIntent);
            }
        });
    }
}