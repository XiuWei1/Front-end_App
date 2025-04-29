package com.example.smartminutes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ViewMinutes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewminutes);

        // Handle the Back Button
        Button backButton = findViewById(R.id.upload_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewMinutes.this, UserInterface.class);
            startActivity(intent);
            finish(); // Optional: Call finish() if you don't want to keep this activity in the back stack
        });
    }
}