package com.nikhil.expensetracker.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.databinding.ActivityMultiTransactionUpdateBinding;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.Util;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MultiTransactionUpdate extends AppCompatActivity {

    private ActivityMultiTransactionUpdateBinding activityMultiTransactionUpdateBinding;
    private List<Transaction> selectedTransactions = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;
    private List<String> categories;
    private boolean isDebit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMultiTransactionUpdateBinding = ActivityMultiTransactionUpdateBinding.inflate(getLayoutInflater());
        setContentView(activityMultiTransactionUpdateBinding.getRoot());

        //Get array of selected transactions as parcelable in intent
        Intent intent = this.getIntent();
        String selectedTransactionsJSON = intent.getStringExtra("selectedTransactions");
        try {
            selectedTransactions.addAll(
                    new ObjectMapper()
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .readValue(selectedTransactionsJSON, new TypeReference<List<Transaction>>() {
                            })
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        categoryAdapter = new ArrayAdapter<>(this, R.layout.category_item, categories);
        activityMultiTransactionUpdateBinding.category.setAdapter(categoryAdapter);
        activityMultiTransactionUpdateBinding.category.setOnItemClickListener((adapterView, view, i, l) -> {
            if (categories.get(i).contains("Custom")) {
                activityMultiTransactionUpdateBinding.category.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                activityMultiTransactionUpdateBinding.category.setInputType(InputType.TYPE_NULL);
            }
            categoryAdapter.getFilter().filter(null);
        });

        //Set datepicker
        TextInputEditText date = activityMultiTransactionUpdateBinding.date;

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

        //Set credit/debit
        activityMultiTransactionUpdateBinding.typeDebit.setOnClickListener(v -> {
            isDebit = true;
            handleTransactionType();
        });

        activityMultiTransactionUpdateBinding.typeCredit.setOnClickListener(v -> {
            isDebit = false;
            handleTransactionType();
        });

        //Update transactions
        activityMultiTransactionUpdateBinding.updateTransaction.setOnClickListener(v -> updateSelectedTransactions());


    }

    private void handleTransactionType() {
        if (isDebit) {
            activityMultiTransactionUpdateBinding.typeDebit.setBackgroundColor(Color.RED);
            activityMultiTransactionUpdateBinding.typeCredit.setBackgroundColor(Color.TRANSPARENT);
            activityMultiTransactionUpdateBinding.typeCredit.setTextColor(Color.WHITE);
        } else {
            activityMultiTransactionUpdateBinding.typeCredit.setBackgroundColor(Color.GREEN);
            activityMultiTransactionUpdateBinding.typeCredit.setTextColor(Color.BLACK);
            activityMultiTransactionUpdateBinding.typeDebit.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void deleteSelectedTransactions() {
        List<String> ids = selectedTransactions.stream().map(Transaction::getId).collect(Collectors.toList());
        MainActivity.getInstance().database.deleteMultipleTransactions(ids);
        successIntent();
    }

    private void updateSelectedTransactions() {

        //Fill all values from fields
        String isDebit = this.isDebit ? "DEBIT" : "CREDIT";
        String payeeName = String.valueOf(activityMultiTransactionUpdateBinding.payeeName.getText());
        Double amount;

        if (activityMultiTransactionUpdateBinding.amountPaid.getText() == null || activityMultiTransactionUpdateBinding.amountPaid.getText().toString().isEmpty()) {
            amount = Double.valueOf("0.00");
        } else {
            amount = Double.valueOf(activityMultiTransactionUpdateBinding.amountPaid.getText().toString());
        }

        String category = String.valueOf(activityMultiTransactionUpdateBinding.category.getText());
        Long createdAt;

        if (activityMultiTransactionUpdateBinding.date.getText() == null || activityMultiTransactionUpdateBinding.date.getText().toString().isEmpty()) {
            //Do nothing
        } else {
            createdAt = Objects.requireNonNull(Util.convertStringToTimestamp(String.valueOf(activityMultiTransactionUpdateBinding.date.getText()))).getTime();
        }


        String bankName = String.valueOf(activityMultiTransactionUpdateBinding.bankName.getText());
        String emoji = String.valueOf(activityMultiTransactionUpdateBinding.emoji.getText());

        for (Transaction transaction : selectedTransactions) {
            //Set debit/credit
            transaction.setType(isDebit);

            //Set payeeName
            if (!payeeName.isEmpty()) {
                transaction.setName(payeeName);
            }

            //Set amount
            if (!amount.equals(0.00)) {
                transaction.setAmount(amount);
            }

            //Set category
            if (!category.isEmpty()) {
                transaction.setCategory(category);
            }

            //Set createdAt
//            Long currentTransactionTs = transaction.getCreatedAt();
//            if (!activityMultiTransactionUpdateBinding.date.getText().toString().equalsIgnoreCase(Util.convertTimestampToDate(currentTransactionTs))) {
//                transaction.setCreatedAt(createdAt);
//            }

            //Set bank name
            if (!bankName.isEmpty()) {
                transaction.setBank(bankName);
            }

            //Set emoji
            if (!emoji.isEmpty()) {
                transaction.setEmoji(emoji);
            }

            //Finally update transaction
            MainActivity.getInstance().database.updateTransactionById(transaction);

        }

        Toast.makeText(this, "Transactions have been updated", Toast.LENGTH_SHORT).show();
        successIntent();
    }

    private void successIntent() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("success", true);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

}