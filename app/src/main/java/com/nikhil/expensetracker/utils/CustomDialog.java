package com.nikhil.expensetracker.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nikhil.expensetracker.R;

public class CustomDialog extends AppCompatDialogFragment {

    private TextInputLayout textInputLayout;
    private TextInputEditText textInputEditText;
    private final String currentValue;
    private final String hintText;
    private String updatedValue;
    private final String dialogTitle;
    private CustomDialogListener customDialogListener;
    private String inputType;

    public CustomDialog(String currentValue, String hintText, String title, String inputType) {
        this.currentValue = currentValue;
        this.hintText = hintText;
        this.dialogTitle = title;
        this.inputType = inputType;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = layoutInflater.inflate(R.layout.custom_dialog_input, null);
        builder.setView(view)
                .setTitle(dialogTitle)
                .setNegativeButton("Cancel", (dialogInterface, i) -> {

                })
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    if (textInputEditText.getText() != null && textInputEditText.getText().length() < 1) {
                        if (hintText.equalsIgnoreCase("username")) {
                            textInputLayout.setError("Please enter your name");
                        }
                    } else {
                        updatedValue = textInputEditText.getText().toString();
                        customDialogListener.applyValues(updatedValue, hintText);
                    }
                });

        textInputLayout = view.findViewById(R.id.customInputLayout);
        textInputLayout.setHint(hintText);

        textInputEditText = view.findViewById(R.id.customInput);
        textInputEditText.setText(currentValue);

        if(inputType.equalsIgnoreCase("text")){
            textInputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        }else{
            textInputEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            customDialogListener = (CustomDialogListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public interface CustomDialogListener {
        void applyValues(String customInputValue, String customInputTitle);
    }

}
