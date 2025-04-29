package com.example.smartminutes;

import android.Manifest;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordingBottomSheetDialog extends BottomSheetDialogFragment {

    private Chronometer timer;
    private Button startRecordingBtn, stopRecordingBtn;
    private MediaRecorder mediaRecorder;
    private String recordingFilePath;
    private boolean isRecording = false;
    private long pauseOffset = 0;
    private static final int REQUEST_RECORD_AUDIO = 101;
    private static final String TAG = "RecordingBottomSheet"; // For logging

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recording_bottom_sheet, container, false);

        // Initialize views
        timer = view.findViewById(R.id.recording_timer);
        startRecordingBtn = view.findViewById(R.id.start_recording_button);
        stopRecordingBtn = view.findViewById(R.id.stop_recording_btn);
        ImageButton closeButton = view.findViewById(R.id.close_button);

        // Set up timer format
        timer.setFormat("%s");

        // Button states
        stopRecordingBtn.setEnabled(false);

        // Close button
        closeButton.setOnClickListener(v -> {
            if (isRecording) {
                Toast.makeText(getContext(), "Please stop recording first", Toast.LENGTH_SHORT).show();
            } else {
                dismiss();
            }
        });

        // Start Recording button
        startRecordingBtn.setOnClickListener(v -> {
            if (!isRecording) {
                if (checkAudioPermissions()) {
                    // Reset timer first
                    timer.setBase(SystemClock.elapsedRealtime() - pauseOffset);

                    // Start recording process
                    if (startRecording()) {  // Only proceed if recording started successfully
                        Log.d(TAG, "Starting timer");
                        timer.start(); // This should start the timer
                        isRecording = true;
                        startRecordingBtn.setEnabled(false);
                        stopRecordingBtn.setEnabled(true);

                        // Verify timer is running by logging
                        Log.d(TAG, "Timer started: " + (SystemClock.elapsedRealtime() - timer.getBase()));
                    }
                } else {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_RECORD_AUDIO);
                }
            }
        });

        // Stop Recording button
        stopRecordingBtn.setOnClickListener(v -> {
            if (isRecording) {
                Log.d(TAG, "Stopping timer");
                timer.stop();
                pauseOffset = SystemClock.elapsedRealtime() - timer.getBase(); // Save current time
                stopRecording();
                isRecording = false;
                startRecordingBtn.setEnabled(true);
                stopRecordingBtn.setEnabled(false);
                saveRecording();
            }
        });

        return view;
    }

    private boolean checkAudioPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Reset timer when permission granted
                timer.setBase(SystemClock.elapsedRealtime());
                pauseOffset = 0;

                if (startRecording()) {
                    timer.start();
                    isRecording = true;
                    startRecordingBtn.setEnabled(false);
                    stopRecordingBtn.setEnabled(true);
                }
            } else {
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Modified to return boolean indicating success
    private boolean startRecording() {
        // Save to public Downloads directory
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Toast.makeText(getContext(), "Failed to create directory", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        recordingFilePath = storageDir.getAbsolutePath() + "/SmartMinutes_" + timeStamp + ".mp3";

        try {
            mediaRecorder = new MediaRecorder();
            // High quality audio settings
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(recordingFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioSamplingRate(44100); // CD quality
            mediaRecorder.setAudioEncodingBitRate(192000); // High bitrate
            mediaRecorder.setAudioChannels(1); // Mono

            mediaRecorder.prepare();
            mediaRecorder.start();

            Toast.makeText(getContext(), "Recording started", Toast.LENGTH_SHORT).show();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Prepare failed", e);
            Toast.makeText(getContext(), "Recording failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            releaseMediaRecorder();
            return false;
        } catch (IllegalStateException e) {
            Log.e(TAG, "Start failed", e);
            Toast.makeText(getContext(), "Recording failed to start", Toast.LENGTH_LONG).show();
            releaseMediaRecorder();
            return false;
        }
    }

    private void stopRecording() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Stop failed", e);
            Toast.makeText(getContext(), "Recording was too short", Toast.LENGTH_SHORT).show();
        } finally {
            releaseMediaRecorder();
        }
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void saveRecording() {
        // Make the recording visible to other apps
        MediaScannerConnection.scanFile(
                getContext(),
                new String[]{recordingFilePath},
                new String[]{"audio/mpeg"},
                (path, uri) -> {
                    if (uri != null) {
                        Log.d(TAG, "File scanned successfully");
                    } else {
                        Log.e(TAG, "Failed to scan file");
                    }
                }
        );

        Toast.makeText(getContext(),
                "MP3 recording saved to Downloads: " + new File(recordingFilePath).getName(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRecording) {
            timer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - timer.getBase();
            stopRecording();
            isRecording = false;
        }
        releaseMediaRecorder();
    }
}