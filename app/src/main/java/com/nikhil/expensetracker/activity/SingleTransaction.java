package com.nikhil.expensetracker.activity;

import static com.nikhil.expensetracker.R.drawable.ic_baseline_close_24;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.databinding.ActivityTransactionBinding;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.Util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SingleTransaction extends AppCompatActivity {

    ActivityTransactionBinding activity_transaction;
    private boolean isEdit = false;
    private boolean isDebit = true;
    private Intent returnIntent;
    private ArrayAdapter<String> categoryAdapter;
    private List<String> categories;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        activity_transaction = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(activity_transaction.getRoot());
        returnIntent = this.getIntent();
        handleFormEdit();

        //Get categories
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String jsonString = sharedPreferences.getString("categories", null);
        if (jsonString != null) {
            try {
                categories = new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .readValue(jsonString, new TypeReference<List<String>>() {
                        });
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        //Fill transaction details
        if (returnIntent != null) {
            fillValues();
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Set datepicker
        TextInputEditText date = activity_transaction.date;

        //Fix to hide keyboard
        date.setInputType(InputType.TYPE_NULL);
        date.setKeyListener(null);

        date.setOnFocusChangeListener((v, b) -> {
            if (b) {
                MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                        .datePicker()
                        .setTitleText("Transaction date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

                datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
                datePicker.addOnPositiveButtonClickListener(selection -> {
                    Date date1 = new Date(selection);
                    @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                    date.setText(df.format(date1));
                    date.clearFocus();
                });
            }
        });

        //Edit transaction details
        activity_transaction.editTransaction.setOnClickListener(view -> {
            isEdit = !isEdit;
            handleFormEdit();
            if (!isEdit) {
                fillValues();
            }
        });

        activity_transaction.typeDebit.setOnClickListener(v -> {
            if (isEdit) {
                isDebit = true;
                handleTransactionType();
            }
        });

        activity_transaction.typeCredit.setOnClickListener(v -> {
            if (isEdit) {
                isDebit = false;
                handleTransactionType();
            }
        });

        //Set category list items
        categoryAdapter = new ArrayAdapter<>(this, R.layout.category_item, categories);
        activity_transaction.category.setAdapter(categoryAdapter);
        categoryAdapter.getFilter().filter(null);

        //List item click for adapter
        activity_transaction.category.setOnItemClickListener((adapterView, view, i, l) -> {
            if (categories.get(i).contains("Custom")) {
                activity_transaction.category.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                activity_transaction.category.setInputType(InputType.TYPE_NULL);
            }
            categoryAdapter.getFilter().filter(null);
        });

        //Delete transaction
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Delete transaction
                    if (returnIntent != null) {
                        deleteTransaction();
                    } else {
                        Toast.makeText(this, "Unable to delete transaction", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };
        activity_transaction.deleteTransaction.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show();
        });

        //Update transaction
        activity_transaction.updateTransaction.setOnClickListener(view -> updateTransaction());

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    private void handleFormEdit() {
        if (isEdit) {
            activity_transaction.editTransaction.setImageDrawable(ContextCompat.getDrawable(this, ic_baseline_close_24));
            activity_transaction.category.setEnabled(true);
            activity_transaction.category.setTextColor(Color.WHITE);

            activity_transaction.date.setEnabled(true);
            activity_transaction.date.setTextColor(Color.WHITE);

            activity_transaction.payeeName.setEnabled(true);
            activity_transaction.payeeName.setTextColor(Color.WHITE);

            activity_transaction.amountPaid.setEnabled(true);
            activity_transaction.amountPaid.setTextColor(Color.WHITE);

            activity_transaction.updateTransaction.setVisibility(View.VISIBLE);

        } else {
            activity_transaction.editTransaction.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_edit_24));
            activity_transaction.category.setEnabled(false);
            activity_transaction.category.setTextColor(Color.rgb(235, 235, 228));

            activity_transaction.date.setEnabled(false);
            activity_transaction.date.setTextColor(Color.rgb(235, 235, 228));

            activity_transaction.payeeName.setEnabled(false);
            activity_transaction.payeeName.setTextColor(Color.rgb(235, 235, 228));

            activity_transaction.amountPaid.setEnabled(false);
            activity_transaction.amountPaid.setTextColor(Color.rgb(235, 235, 228));

            activity_transaction.updateTransaction.setVisibility(View.GONE);
        }
    }

    private void handleTransactionType() {
        if (isDebit) {
            activity_transaction.typeDebit.setBackgroundColor(Color.RED);
            activity_transaction.typeCredit.setBackgroundColor(Color.TRANSPARENT);
            activity_transaction.typeCredit.setTextColor(Color.WHITE);
        } else {
            activity_transaction.typeCredit.setBackgroundColor(Color.GREEN);
            activity_transaction.typeCredit.setTextColor(Color.BLACK);
            activity_transaction.typeDebit.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void fillValues() {
        String type = returnIntent.getStringExtra("type");
        String category = returnIntent.getStringExtra("category");
        Long createdAt = returnIntent.getLongExtra("createdAt", 0);
        String payeeName = returnIntent.getStringExtra("name");
        Double amountPaid = returnIntent.getDoubleExtra("amount", 0);

        if (type != null) {
            isDebit = !type.equals("CREDIT");
            handleTransactionType();
        }

        activity_transaction.category.setText(category);
        activity_transaction.date.setText(Util.convertTimestampToDate(createdAt));
        activity_transaction.payeeName.setText(payeeName);
        activity_transaction.amountPaid.setText(String.valueOf(amountPaid));
    }

    private void deleteTransaction() {
        MainActivity.getInstance().database.deleteTransaction(returnIntent.getStringExtra("id"));
        Intent resultIntent = new Intent();
        resultIntent.putExtra("success", "true");
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @SuppressLint("SimpleDateFormat")
    private void updateTransaction() {

        Timestamp createdAt = Util.convertStringToTimestamp(String.valueOf(activity_transaction.date.getText()));

        Transaction transaction = new Transaction(
                returnIntent.getStringExtra("id"),
                this.isDebit ? "DEBIT" : "CREDIT",
                String.valueOf(activity_transaction.payeeName.getText()),
                Double.valueOf(Objects.requireNonNull(activity_transaction.amountPaid.getText()).toString()),
                String.valueOf(activity_transaction.category.getText()),
                createdAt.getTime(),
                createdAt.getTime(),
                null
        );

        MainActivity.getInstance().database.updateTransactionById(transaction);
        Toast.makeText(this, "Transaction details updated", Toast.LENGTH_SHORT).show();
        isEdit = false;
        handleFormEdit();
        MainActivity.getInstance().refreshAdapterData();

    }

}
