package com.nikhil.expensetracker.activity;

import static com.nikhil.expensetracker.R.drawable.ic_baseline_close_24;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.databinding.ActivityTransactionBinding;
import com.nikhil.expensetracker.model.Transaction;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class SingleTransaction extends AppCompatActivity {

    ActivityTransactionBinding activity_transaction;
    private boolean isEdit = false;
    private boolean isDebit = true;
    private Intent returnIntent;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        activity_transaction = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(activity_transaction.getRoot());
        returnIntent = this.getIntent();
        handleFormEdit();

        //Fill transaction details
        if (returnIntent != null) {
            fillValues();
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }

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
        long createdAt = returnIntent.getLongExtra("createdAt", 0);
        String payeeName = returnIntent.getStringExtra("name");
        Double amountPaid = returnIntent.getDoubleExtra("amount", 0);

        if (type != null) {
            isDebit = !type.equals("CREDIT");
            handleTransactionType();
        }

        activity_transaction.category.setText(category);

        Date date = new Date(createdAt);
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        activity_transaction.date.setText(dateFormat.format(date));
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
        Transaction transaction = null;
        try {
            transaction = new Transaction(
                    returnIntent.getStringExtra("id"),
                    this.isDebit ? "DEBIT" : "CREDIT",
                    String.valueOf(activity_transaction.payeeName.getText()),
                    Double.valueOf(Objects.requireNonNull(activity_transaction.amountPaid.getText()).toString()),
                    String.valueOf(activity_transaction.category.getText()),
                    new Timestamp(Objects.requireNonNull(new SimpleDateFormat("dd/MM/yyyy").parse(String.valueOf(activity_transaction.date.getText()))).getTime()).getTime(),
                    new Timestamp(Objects.requireNonNull(new SimpleDateFormat("dd/MM/yyyy").parse(String.valueOf(activity_transaction.date.getText()))).getTime()).getTime(),
                    null
            );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println("UPDATED TRANSACTION:" + transaction);

        if (transaction != null) {
            MainActivity.getInstance().database.updateTransactionById(transaction);
            Toast.makeText(this, "Transaction details updated", Toast.LENGTH_SHORT).show();
            isEdit = false;
            handleFormEdit();
            MainActivity.getInstance().refreshAdapterData();
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }

}
