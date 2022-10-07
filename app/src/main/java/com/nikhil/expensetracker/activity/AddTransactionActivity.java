package com.nikhil.expensetracker.activity;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.databinding.AddTransactionBinding;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.DateUtils;
import com.nikhil.expensetracker.utils.StringUtils;
import com.nikhil.expensetracker.utils.Util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;


public class AddTransactionActivity extends AppCompatActivity {

    private AddTransactionBinding mBinding;
    private ArrayAdapter<String> categoryAdapter;
    private List<String> categories;
    private Transaction transaction;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        mBinding = DataBindingUtil.setContentView(this, R.layout.add_transaction);

        //Create transaction object
        transaction = new Transaction();
        transaction.setType("DEBIT");

        //Set data binding variable values
        mBinding.setTransaction(transaction);
        mBinding.setIsCredit(false);

        handleCategoryList();
        handleDatePickerWidget();
        handleTransactionType();
        handleAddTransaction();
    }

    private void handleCategoryList() {
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
        mBinding.category.setAdapter(categoryAdapter);
        mBinding.category.setOnItemClickListener((adapterView, view, i, l) -> {
            if (mBinding.category.getText().toString().equals("Custom")) {
                mBinding.category.setText("");
                mBinding.category.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                mBinding.category.setInputType(InputType.TYPE_NULL);
            }
            categoryAdapter.getFilter().filter(null);
        });
    }

    private void handleTransactionType() {
        mBinding.typeDebit.setOnClickListener(view -> {
            mBinding.setIsCredit(false);
            transaction.setType("DEBIT");
        });

        mBinding.typeCredit.setOnClickListener(view -> {
            mBinding.setIsCredit(true);
            transaction.setType("CREDIT");
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
                    java.util.Date date1 = new java.util.Date(selection);
                    @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                    date.setText(df.format(date1));
                    date.clearFocus();
                });
            }
        });
    }

    private void handleAddTransaction() {
        mBinding.addTransaction.setOnClickListener(v -> {

            if (validateTransaction()) {

                //Set id
                transaction.setId(Generators.timeBasedGenerator().generate().toString());

                //TODO - learn two way binding and change this
                transaction.setAmount(StringUtils.convertStringAmountToDouble(mBinding.amountPaid.getText().toString()));
                transaction.setCreatedAt(DateUtils.convertStringToTimestamp(mBinding.date.getText().toString()).getTime());

                //Trim values
                transaction.setName(transaction.getName().trim());
                transaction.setBank(transaction.getBank().trim());
                transaction.setEmoji(transaction.getEmoji().trim());

                //Save transaction to database
                MainActivity.getInstance().database.addTransaction(transaction);
                Toast.makeText(this, "Transaction added!", Toast.LENGTH_SHORT).show();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("success", "true");
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    private boolean validateTransaction() {
        //Check if date is null
        Editable dateText = mBinding.date.getText();
        if (dateText != null && dateText.length() < 1) {
            TextInputLayout dateLayout = mBinding.dateLayout;
            dateLayout.setError("Please enter a valid date");
            return false;
        }

        //Check if payee name is null
        Editable payeeName = mBinding.payeeName.getText();
        if (payeeName != null && payeeName.length() < 1) {
            TextInputLayout payeeNameLayout = mBinding.payeeNameLayout;
            payeeNameLayout.setError("Please enter a valid payee name");
            return false;
        }

        //Check if amount is null
        Editable amountPaid = mBinding.amountPaid.getText();
        if (amountPaid == null || String.valueOf(amountPaid).equals("0") || amountPaid.length() < 1) {
            TextInputLayout amountPaidLayout = mBinding.amountPaidLayout;
            amountPaidLayout.setError("Please enter a valid amount");
            return false;
        }

        //Check if category is null
        Editable category = mBinding.category.getText();
        if (category == null || category.length() < 1) {
            TextInputLayout categoryLayout = mBinding.categoryLayout;
            categoryLayout.setError("Please select a category");
            return false;
        }

        //Check if bank is null
        Editable bankName = mBinding.bankName.getText();
        if (bankName == null || bankName.length() < 1) {
            mBinding.bankNameLayout.setError("Please enter bank name");
            return false;
        }

        //Check if emoji is null
        Editable emoji = mBinding.emoji.getText();
        if (emoji == null || emoji.length() < 1) {
            mBinding.emojiLayout.setError("Please enter an emoji");
            return false;
        }

        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
    }
}
