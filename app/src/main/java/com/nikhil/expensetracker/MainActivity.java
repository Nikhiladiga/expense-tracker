package com.nikhil.expensetracker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nikhil.expensetracker.activity.AddTransaction;
import com.nikhil.expensetracker.activity.SingleTransaction;
import com.nikhil.expensetracker.adapters.TransactionListAdapter;
import com.nikhil.expensetracker.database.Database;
import com.nikhil.expensetracker.databinding.ActivityMainBinding;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.receiver.SmsReceiver;
import com.nikhil.expensetracker.services.SMSReaderService;
import com.nikhil.expensetracker.utils.Util;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static Database database;
    private TransactionListAdapter transactionListAdapter;
    private List<Transaction> transactions = new ArrayList<>();

    public static WeakReference<MainActivity> mainActivityWeakReference;

    //Activity binding
    private ActivityMainBinding activityMainBinding;

    ActivityResultLauncher<Intent> singleTransactionActivity;
    ActivityResultLauncher<Intent> addTransactionActivity;

    RelativeLayout noTransactionsLayer;
    ListView transactionsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        mainActivityWeakReference = new WeakReference<>(MainActivity.this);
        checkSmsPermissions();
        database = new Database(this, "expense.db", null, 1);

        //GET total balance
        refreshMainDashboardBalance();

        //GET current month
        Calendar calendar = Calendar.getInstance();
        activityMainBinding.currentMonth.setText(new SimpleDateFormat("MMMM").format(calendar.getTime()));

        //Get transactions for this month from database
        transactions = database.getTransactions();
        transactionListAdapter = new TransactionListAdapter(this, transactions);
        noTransactionsLayer = activityMainBinding.noTransactionsLayer;
        transactionsList = activityMainBinding.transactionList;
        transactionsList.setAdapter(transactionListAdapter);
        activityMainBinding.transactionList.setClickable(true);
        activityMainBinding.transactionList.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(this, SingleTransaction.class);
            intent.putExtra("id", transactions.get(i).getId());
            intent.putExtra("type", transactions.get(i).getId());
            intent.putExtra("name", transactions.get(i).getId());
            intent.putExtra("amount", transactions.get(i).getId());
            intent.putExtra("category", transactions.get(i).getId());
            intent.putExtra("createdAt", transactions.get(i).getId());
            singleTransactionActivity.launch(intent);
        });

        registerActivities();

        if (transactions != null && transactions.size() < 1) {
            noTransactionsLayer.setVisibility(View.VISIBLE);
            transactionsList.setVisibility(View.VISIBLE);
        } else {
            noTransactionsLayer.setVisibility(View.GONE);
            transactionsList.setVisibility(View.VISIBLE);
        }

        //Add transaction btn
        FloatingActionButton addTransactionBtn = activityMainBinding.addTransactionBtn;
        addTransactionBtn.setOnClickListener((view) -> {
            Intent intent = new Intent(this, AddTransaction.class);
            addTransactionActivity.launch(intent);
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean isPermissionGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isPermissionGranted = false;
                    break;
                }
            }
            if (isPermissionGranted) {
                IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                registerReceiver(new SmsReceiver(), intentFilter);
            } else {
                Toast.makeText(this, "Please give sms permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkSmsPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS
            }, 1);
        } else {
            Intent intent = new Intent(this, SMSReaderService.class);
            startForegroundService(intent);
        }
    }

    public void refreshAdapterData() {
        transactions.clear();
        transactions.addAll(database.getTransactions());

        if (transactions.size() > 0) {
            transactionsList.setVisibility(View.VISIBLE);
            noTransactionsLayer.setVisibility(View.GONE);
        } else {
            transactionsList.setVisibility(View.GONE);
            noTransactionsLayer.setVisibility(View.VISIBLE);
        }

        transactionListAdapter.notifyDataSetChanged();
        refreshMainDashboardBalance();
    }

    private void refreshMainDashboardBalance() {
        activityMainBinding.currentAmount.setText("₹" + database.getBalance().toString());
    }

    private void registerActivities() {
        //Activity register for single transaction class
        singleTransactionActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        boolean success = Boolean.parseBoolean(data != null ? data.getStringExtra("success") : "false");
                        if (success) {
                            refreshAdapterData();
                        }
                    }
                }
        );

        //Activity register for add transaction class
        addTransactionActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        boolean success = Boolean.parseBoolean(data != null ? data.getStringExtra("success") : "false");
                        if (success) {
                            refreshAdapterData();
                        }
                    }
                }
        );

    }

    public static MainActivity getInstance() {
        return mainActivityWeakReference.get();
    }

}