package com.example.smartminutes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;


public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference usersRef, usernamesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        usersRef = db.getReference("Users");
        usernamesRef = db.getReference("Usernames");

        Button signUpButton = findViewById(R.id.signUpSubmitButton);
        TextView signInTextView = findViewById(R.id.signInText);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText usernameEditText = findViewById(R.id.usernameEditText);
                EditText emailEditText = findViewById(R.id.emailEditText);
                EditText passwordEditText = findViewById(R.id.passwordEditText);

                String username = usernameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();

                if (!isValidUsername(username)) {
                    Toast.makeText(SignUpActivity.this, "Username must be at least 3 characters long", Toast.LENGTH_SHORT).show();
                } else if (!isValidEmail(email)) {
                    Toast.makeText(SignUpActivity.this, "Invalid email format. Must start with a letter and end with @gmail.com", Toast.LENGTH_SHORT).show();
                } else if (!isValidPassword(password)) {
                    Toast.makeText(SignUpActivity.this, "Password must be at least 8 characters long and include at least one special character", Toast.LENGTH_SHORT).show();
                } else {
                    checkUsernameAvailability(username, email, password);
                }
            }
        });

        signInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                finish();
            }
        });
    }

    private void checkUsernameAvailability(String username, String email, String password) {
        usernamesRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(SignUpActivity.this, "Username is already taken. Choose another one.", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(username, email, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUpActivity.this, "Error checking username: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(String username, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();
                                UsersInfo user = new UsersInfo(username, email, password, uid);

                                // Store user data under /Users/{uid}
                                usersRef.child(uid).setValue(user)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Store username → UID mapping under /Usernames/{username}
                                                    usernamesRef.child(username).setValue(uid)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(SignUpActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                                                        Intent intent = new Intent(SignUpActivity.this, UserInterface.class);
                                                                        intent.putExtra("USERNAME", username);
                                                                        intent.putExtra("EMAIL", email);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    } else {
                                                                        Toast.makeText(SignUpActivity.this, "Error saving username", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(SignUpActivity.this, "Database Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidUsername(String username) {
        return username.length() >= 3;
    }

    private boolean isValidEmail(String email) {
        if (!email.endsWith("@gmail.com")) return false;
        if (!Character.isLetter(email.charAt(0))) return false;
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches(".*[!@#$%^&*()_+=-].*");
    }
}
