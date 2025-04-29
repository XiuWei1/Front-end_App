package com.example.smartminutes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    private Button signUpButton; // Declare SignUp button
    private Button signInButton; // Declare SignIn button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second); // This is where your second layout is set

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // This hides the action bar
        }

        // Initialize the Sign Up and Sign In buttons
        signUpButton = findViewById(R.id.signUpButtonInSecondActivity);
        signInButton = findViewById(R.id.signInButtonInSecondActivity);

        // Set the OnClickListener for the Sign Up button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When clicked, navigate to SignUpActivity
                Intent intent = new Intent(SecondActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // Set the OnClickListener for the Sign In button
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When clicked, navigate to SignInActivity
                Intent intent = new Intent(SecondActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }
}
