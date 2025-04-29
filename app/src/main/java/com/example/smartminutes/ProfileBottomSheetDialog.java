package com.example.smartminutes;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ProfileBottomSheetDialog extends BottomSheetDialogFragment {

    private OnProfileOptionClickListener listener;

    // Interface for Click Listeners
    public interface OnProfileOptionClickListener {
        void onUploadClicked();
        void onDeleteClicked();
    }

    // Attach Listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnProfileOptionClickListener) {
            listener = (OnProfileOptionClickListener) context;
        }
    }

    // Set Listener Manually (Fix for UserInterface.java)
    public void setOnProfileOptionClickListener(OnProfileOptionClickListener listener) {
        this.listener = listener;
    }

    // Inflate Bottom Sheet Layout
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_profile, container, false);
    }

    // Handle Button Clicks
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button uploadButton = view.findViewById(R.id.upload_button);
        Button deleteButton = view.findViewById(R.id.delete_button);

        uploadButton.setOnClickListener(v -> {
            if (listener != null) listener.onUploadClicked();
            dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClicked();
            dismiss();
        });
    }
}
