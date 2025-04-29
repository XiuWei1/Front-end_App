package com.example.smartminutes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserInterface extends AppCompatActivity implements ProfileBottomSheetDialog.OnProfileOptionClickListener {

    private TextView userGreeting, sidebarUsername;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private DrawerLayout drawerLayout;
    private CircleImageView userProfile;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinterface);

        // Initialize UI Elements
        drawerLayout = findViewById(R.id.drawer_layout);
        userGreeting = findViewById(R.id.user_greeting);
        sidebarUsername = findViewById(R.id.sidebar_username);
        userProfile = findViewById(R.id.user_profile);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Retrieve username from Intent (if coming from SignUpActivity)
        Intent intent = getIntent();
        if (intent != null) {
            String usernameFromIntent = intent.getStringExtra("USERNAME");
            if (usernameFromIntent != null) {
                saveUsername(usernameFromIntent);
            }
        }

        // Load and update UI with username
        updateUsernameUI();

        // Set click listener for profile image
        userProfile.setOnClickListener(v -> showProfileOptions());

        // Initialize menu icon
        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> openDrawer());

        // Set up navigation view click listeners
        NavigationView navigationView = findViewById(R.id.navigation_view);
        setupNavigationItems(navigationView);

        // Set click listener for "View Minutes" TextView
        TextView viewMinutesText = findViewById(R.id.view_minutes_text);
        viewMinutesText.setOnClickListener(v -> {
            Intent viewMinutesIntent = new Intent(UserInterface.this, ViewMinutes.class);
            startActivity(viewMinutesIntent);
        });

        // Set click listener for "Start Recording" button
        MaterialButton startRecordingBtn = findViewById(R.id.start_recording_button);
        startRecordingBtn.setOnClickListener(v -> showRecordingBottomSheet());
    }

    // Method to save username in SharedPreferences
    private void saveUsername(String username) {
        if (username != null && !username.trim().isEmpty()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USERNAME, username);
            editor.apply();
            updateUsernameUI();
        }
    }

    // Method to load username from SharedPreferences
    private void updateUsernameUI() {
        String savedUsername = sharedPreferences.getString(KEY_USERNAME, "User");
        userGreeting.setText("Hello, " + savedUsername);
        sidebarUsername.setText(savedUsername);
    }

    // Show Profile Options (Bottom Sheet)
    private void showProfileOptions() {
        ProfileBottomSheetDialog bottomSheet = new ProfileBottomSheetDialog();
        bottomSheet.setOnProfileOptionClickListener(this);
        bottomSheet.show(getSupportFragmentManager(), "ProfileBottomSheet");
    }

    // Show Recording Bottom Sheet
    private void showRecordingBottomSheet() {
        RecordingBottomSheetDialog bottomSheet = new RecordingBottomSheetDialog();
        bottomSheet.show(getSupportFragmentManager(), "RecordingBottomSheet");
    }

    // Handle Upload Button Click (Profile Image)
    @Override
    public void onUploadClicked() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle Delete Button Click (Profile Image)
    @Override
    public void onDeleteClicked() {
        userProfile.setImageResource(R.drawable.userprofile);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_PROFILE_IMAGE);
        editor.apply();
        Toast.makeText(this, "Profile image deleted!", Toast.LENGTH_SHORT).show();
    }

    // Handle Image Selection Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                userProfile.setImageURI(selectedImageUri);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_PROFILE_IMAGE, selectedImageUri.toString());
                editor.apply();
                Toast.makeText(this, "Profile image updated!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Open Drawer
    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    // Close Drawer
    public void closeDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    // Set up navigation menu
    private void setupNavigationItems(NavigationView navigationView) {
        // Deleted Audio Message click listener
        TextView deletedAudioMessage = navigationView.findViewById(R.id.sidebar_delete_history);
        deletedAudioMessage.setOnClickListener(v -> {
            closeDrawer();
            showDeletedAudioMessage();
        });

        // Logout click listener
        TextView logoutText = navigationView.findViewById(R.id.sidebar_logout);
        logoutText.setOnClickListener(v -> logout());

        // Report Bug click listener
        TextView reportBugText = navigationView.findViewById(R.id.sidebar_report_bug);
        reportBugText.setOnClickListener(v -> {
            Toast.makeText(
                    UserInterface.this,
                    "Thank you for your feedback! Our team will review the issue.",
                    Toast.LENGTH_SHORT
            ).show();
            reportBug();
        });
    }

    private void showDeletedAudioMessage() {
        Toast.makeText(this, "No deleted audio found.", Toast.LENGTH_SHORT).show();
    }

    // Logout and clear session data
    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PROFILE_IMAGE);
        editor.apply();
        Toast.makeText(this, "Logout Successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(UserInterface.this, SignInActivity.class));
        finish();
        closeDrawer();
    }

    // Handle Back Press
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    // Handle Bug Reporting
    private void reportBug() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:echitan14@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report - Smart Minutes App");
        intent.putExtra(Intent.EXTRA_TEXT, "Please describe the bug here...");
        startActivity(Intent.createChooser(intent, "Report Bug via Email"));
    }
}