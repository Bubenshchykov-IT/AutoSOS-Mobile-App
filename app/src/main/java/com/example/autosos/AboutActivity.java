package com.example.autosos;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutActivity extends AppCompatActivity {

    Button aboutBackMenuButton;

    private String getType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getType = getIntent().getStringExtra("type");

        aboutBackMenuButton = (Button) findViewById(R.id.aboutBackMenuButton);

        aboutBackMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backMenuIntent;
                if (getType.equals("Customer")) {
                    backMenuIntent = new Intent(AboutActivity.this, CustomerMenuActivity.class);
                    startActivity(backMenuIntent);
                }
                else if (getType.equals("Driver")) {
                    backMenuIntent = new Intent(AboutActivity.this, DriverMenuActivity.class);
                    startActivity(backMenuIntent);
                }
            }
        });
    }
}