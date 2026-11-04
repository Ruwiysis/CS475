package com.cs475.salahcompanion;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ConfirmDeleteDialogFragment extends DialogFragment {

    public interface ConfirmDeleteListener {
        void onDeleteConfirmed(int logId);
    }

    private static final String ARG_LOG_ID = "log_id";

    public static ConfirmDeleteDialogFragment newInstance(int logId) {
        ConfirmDeleteDialogFragment dialog = new ConfirmDeleteDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LOG_ID, logId);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        int logId = requireArguments().getInt(ARG_LOG_ID);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Delete Prayer Record")
                .setMessage("Are you sure you want to delete this record?")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (getActivity() instanceof ConfirmDeleteListener) {
                        ((ConfirmDeleteListener) getActivity())
                                .onDeleteConfirmed(logId);
                    }
                })
                .create();
    }
}
