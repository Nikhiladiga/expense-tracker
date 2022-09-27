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
import com.nikhil.expensetracker.utils.Util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AddTransaction extends AppCompatActivity {

    private AddTransactionBinding addTransactionBinding;
    private boolean isDebit = true;
    private ArrayAdapter<String> categoryAdapter;
    private List<String> categories;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        addTransactionBinding = AddTransactionBinding.inflate(getLayoutInflater());
        setContentView(addTransactionBinding.getRoot());

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

        //Get transaction type
        TextView typeDebit = addTransactionBinding.typeDebit;
        TextView typeCredit = addTransactionBinding.typeCredit;

        typeDebit.setOnClickListener(v -> {
            isDebit = true;
            typeDebit.setBackgroundColor(Color.RED);
            typeCredit.setBackgroundColor(Color.TRANSPARENT);
            typeCredit.setTextColor(Color.WHITE);
        });

        typeCredit.setOnClickListener(v -> {
            isDebit = false;
            typeCredit.setBackgroundColor(Color.GREEN);
            typeCredit.setTextColor(Color.BLACK);
            typeDebit.setBackgroundColor(Color.TRANSPARENT);
        });

        //Set datepicker
        TextInputEditText date = addTransactionBinding.date;

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

        //Set category list items
        categoryAdapter = new ArrayAdapter<>(this, R.layout.category_item, categories);
        addTransactionBinding.category.setAdapter(categoryAdapter);

        //List item click for adapter
        addTransactionBinding.category.setOnItemClickListener((adapterView, view, i, l) -> {
            if (categories.get(i).contains("Custom")) {
                addTransactionBinding.category.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                addTransactionBinding.category.setInputType(InputType.TYPE_NULL);
            }
            categoryAdapter.getFilter().filter(null);
        });

        Button addTransactionBtn = addTransactionBinding.addTransaction;
        addTransactionBtn.setOnClickListener(v -> {

            //Check if date is null
            Editable dateText = addTransactionBinding.date.getText();
            if (dateText != null && dateText.length() < 1) {
                TextInputLayout dateLayout = addTransactionBinding.dateLayout;
                dateLayout.setError("Please enter a valid date");
                return;
            }

            //Check if payee name is null
            Editable payeeName = addTransactionBinding.payeeName.getText();
            if (payeeName != null && payeeName.length() < 1) {
                TextInputLayout payeeNameLayout = addTransactionBinding.payeeNameLayout;
                payeeNameLayout.setError("Please enter a valid payee name");
                return;
            }

            //Check if amount is null
            Editable amountPaid = addTransactionBinding.amountPaid.getText();
            if (amountPaid == null || String.valueOf(amountPaid).equals("0") || amountPaid.length() < 1) {
                TextInputLayout amountPaidLayout = addTransactionBinding.amountPaidLayout;
                amountPaidLayout.setError("Please enter a valid amount");
                return;
            }

            //Check if category is null
            Editable category = addTransactionBinding.category.getText();
            if (category == null || category.length() < 1) {
                TextInputLayout categoryLayout = addTransactionBinding.categoryLayout;
                categoryLayout.setError("Please select a category");
            }

            //Check if bank is null
            Editable bankName = addTransactionBinding.bankName.getText();
            if (bankName == null || bankName.length() < 1) {
                addTransactionBinding.bankNameLayout.setError("Please enter bank name");
            }

            Toast.makeText(this, "Transaction added!", Toast.LENGTH_SHORT).show();

            //Get transaction date in timestamp
            Timestamp createdAt = Util.convertStringToTimestamp(String.valueOf(date.getText()));

            //Save transaction to database
            Transaction transaction = new Transaction(
                    Generators.timeBasedGenerator().generate().toString(),
                    this.isDebit ? "DEBIT" : "CREDIT",
                    String.valueOf(payeeName),
                    Double.valueOf(amountPaid.toString()),
                    String.valueOf(category),
                    createdAt.getTime(),
                    createdAt.getTime(),
                    null,
                    String.valueOf(bankName)
            );
            MainActivity.getInstance().database.addTransaction(transaction);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("success", "true");
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
    }
}
