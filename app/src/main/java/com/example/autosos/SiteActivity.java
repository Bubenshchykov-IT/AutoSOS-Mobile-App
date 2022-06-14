package com.example.autosos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SiteActivity extends AppCompatActivity {

    Button siteBackMenuButton;

    private String getType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site);

        getType = getIntent().getStringExtra("type");

        siteBackMenuButton = (Button) findViewById(R.id.siteBackMenuButton);

        siteBackMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backMenuIntent;
                if (getType.equals("Customer")) {
                    backMenuIntent = new Intent(SiteActivity.this, CustomerMenuActivity.class);
                    startActivity(backMenuIntent);
                }
                else if (getType.equals("Driver")) {
                    backMenuIntent = new Intent(SiteActivity.this, DriverMenuActivity.class);
                    startActivity(backMenuIntent);
                }
            }
        });
    }
}