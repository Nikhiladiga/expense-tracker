package com.nikhil.expensetracker.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

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
import com.nikhil.expensetracker.utils.DateUtils;
import com.nikhil.expensetracker.utils.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SingleTransactionActivity extends AppCompatActivity {

    ActivityTransactionBinding mBinding;
    private Intent intent;
    private Transaction transaction;
    List<String> categories = new ArrayList<>();
    ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_transaction);
        intent = this.getIntent();

        transaction = new Transaction(
                intent.getStringExtra("id"),
                intent.getStringExtra("type"),
                intent.getStringExtra("name"),
                intent.getDoubleExtra("amount", 0),
                intent.getStringExtra("category"),
                intent.getLongExtra("createdAt", 0),
                null,
                null,
                intent.getStringExtra("bankName"),
                intent.getStringExtra("emoji")
        );

        mBinding.setTransaction(transaction);
        mBinding.setEdit(false);
        mBinding.setIsCredit(transaction.getType().equals("CREDIT"));

        handleEdit();
        handleCategoryList();
        handleDatePickerWidget();
        handleTransactionType();
        handleDeleteTransaction();
        updateTransaction();

    }

    private void handleCategoryList() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String jsonString = sharedPreferences.getString("categories", null);
        if (jsonString != null) {
            try {
                categories.addAll(new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .readValue(jsonString, new TypeReference<List<String>>() {
                        }));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        categoryAdapter = new ArrayAdapter<>(this, R.layout.category_item, categories);
        mBinding.category.setAdapter(categoryAdapter);
        mBinding.category.setText(transaction.getCategory());
        categoryAdapter.getFilter().filter(null);
        mBinding.category.setOnItemClickListener((adapterView, view, i, l) -> {
            if (mBinding.category.getText().toString().equals("Custom")) {
                mBinding.category.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                mBinding.category.setInputType(InputType.TYPE_NULL);
            }
            categoryAdapter.getFilter().filter(null);
        });
    }

    private void handleEdit() {
        mBinding.editTransaction.setOnClickListener(view -> mBinding.setEdit(!mBinding.getEdit()));
    }

    private void handleTransactionType() {
        mBinding.typeDebit.setOnClickListener(view -> {
            mBinding.setIsCredit(false);
        });

        mBinding.typeCredit.setOnClickListener(view -> {
            mBinding.setIsCredit(true);
        });

    }

    private void handleDatePickerWidget() {
        TextInputEditText date = mBinding.date;

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
    }

    private void handleDeleteTransaction() {

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Delete transaction
                    if (intent != null) {
                        MainActivity.getInstance().database.deleteTransaction(intent.getStringExtra("id"));
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("success", true);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(this, "Unable to delete transaction", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };
        mBinding.deleteTransaction.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show();
        });
    }

    @SuppressLint("SimpleDateFormat")
    private void updateTransaction() {
        mBinding.updateTransaction.setOnClickListener(view -> {
            Long createdAt;
            Long currentTransactionTs = intent.getLongExtra("createdAt", 0);

            System.out.println("CURRENT TRANSACTION TIMESTAMP:" + currentTransactionTs);

            System.out.println("ACTIVITY DATE:" + mBinding.date.getText());
            System.out.println("CURRENT TS DATE:" + DateUtils.convertTimestampToDate(currentTransactionTs));

            if (Objects.requireNonNull(mBinding.date.getText()).toString().equalsIgnoreCase(DateUtils.convertTimestampToDate(currentTransactionTs))) {
                createdAt = currentTransactionTs;
            } else {
                createdAt = Objects.requireNonNull(DateUtils.convertStringToTimestamp(String.valueOf(mBinding.date.getText()))).getTime();
            }

            Transaction transaction = new Transaction(
                    intent.getStringExtra("id"),
                    mBinding.getIsCredit() ? "CREDIT" : "DEBIT",
                    String.valueOf(mBinding.payeeName.getText()),
                    StringUtils.convertStringAmountToDouble(Objects.requireNonNull(mBinding.amountPaid.getText()).toString()),
                    String.valueOf(mBinding.category.getText()),
                    createdAt,
                    createdAt,
                    null,
                    String.valueOf(mBinding.bankName.getText()),
                    String.valueOf(mBinding.emoji.getText())
            );

            mBinding.setEdit(false);
            MainActivity.getInstance().database.updateTransactionById(transaction);
            Toast.makeText(this, "Transaction details updated", Toast.LENGTH_SHORT).show();
            MainActivity.getInstance().refreshAdapterData();

        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

}
