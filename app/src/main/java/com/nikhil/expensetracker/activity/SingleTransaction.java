package com.nikhil.expensetracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.databinding.ActivityTransactionBinding;

public class SingleTransaction extends AppCompatActivity {

    ActivityTransactionBinding activity_transaction;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        activity_transaction = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(activity_transaction.getRoot());
        Intent intent = this.getIntent();
        if (intent != null) {
            MainActivity.database.deleteTransaction(intent.getStringExtra("id"));
            Intent returnIntent = new Intent();
            returnIntent.putExtra("success", "true");
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

}
