package com.cs475.salahcompanion;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AthkarDetailDialogFragment extends DialogFragment {

    public static AthkarDetailDialogFragment newInstance(Dhikr d) {
        AthkarDetailDialogFragment f = new AthkarDetailDialogFragment();
        Bundle b = new Bundle();
        b.putString("arabic", d.getArabicText());
        b.putString("translit", d.getTransliteration());
        b.putString("meaning", d.getMeaning());
        b.putString("category", d.getCategory());
        b.putInt("repeat", d.getRepeatCount());
        f.setArguments(b);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle b = requireArguments();
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_athkar_detail, null);

        ((TextView) view.findViewById(R.id.tvDialogArabic)).setText(b.getString("arabic"));
        ((TextView) view.findViewById(R.id.tvDialogTranslit)).setText(b.getString("translit"));
        ((TextView) view.findViewById(R.id.tvDialogMeaning)).setText(b.getString("meaning"));

        String category = "morning".equals(b.getString("category")) ? "Morning" : "Evening";
        int repeat = b.getInt("repeat");
        ((TextView) view.findViewById(R.id.tvDialogInfo)).setText(
                category + " athkar  •  Repeat " + repeat + (repeat == 1 ? " time" : " times")
                        + "\nSource: Hisn al-Muslim");

        return new AlertDialog.Builder(requireContext())
                .setTitle("Dhikr details")
                .setView(view)
                .setPositiveButton("Close", null)
                .create();
    }
}
