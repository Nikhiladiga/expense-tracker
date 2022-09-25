package com.nikhil.expensetracker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nikhil.expensetracker.activity.AddTransaction;
import com.nikhil.expensetracker.activity.SettingsActivity;
import com.nikhil.expensetracker.activity.SingleTransaction;
import com.nikhil.expensetracker.activity.UsernameActivity;
import com.nikhil.expensetracker.adapters.TransactionListAdapter;
import com.nikhil.expensetracker.datahelpers.Database;
import com.nikhil.expensetracker.databinding.ActivityMainBinding;
import com.nikhil.expensetracker.datahelpers.SharedPrefHelper;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.receiver.SmsReceiver;
import com.nikhil.expensetracker.utils.Util;

import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public Database database;
    private TransactionListAdapter transactionListAdapter;
    private List<Transaction> transactions = new ArrayList<>();

    private static WeakReference<MainActivity> mainActivityWeakReference;

    private ActivityMainBinding activityMainBinding;

    private ActivityResultLauncher<Intent> singleTransactionActivity;
    private ActivityResultLauncher<Intent> addTransactionActivity;
    private ActivityResultLauncher<Intent> settingsActivity;
    private ActivityResultLauncher<Intent> usernameActivity;

    private RelativeLayout noTransactionsLayer;
    private ListView transactionsList;

    private String currentMonth;

    @SuppressLint({"SimpleDateFormat", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        mainActivityWeakReference = new WeakReference<>(MainActivity.this);

        //Create shared prefs helper class
        SharedPrefHelper.initSharedPrefHelper();

        //Register launchers for all activities
        registerActivities();

        //Check if username is present and show/hide activity
        showUsernameActivity();

        database = new Database(this, "expense.db", null, 1);

        //Store categories in shared prefs
        storeCategories();

        //Register receiver for reading SMS
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        Util.smsReceiver = new SmsReceiver();
        getBaseContext().registerReceiver(Util.smsReceiver, intentFilter);

        //Set menu items to bottom app bar
        setSupportActionBar(activityMainBinding.bottomAppBar);

        //GET total balance
        refreshMainDashboardData();

        //GET current month
        Calendar calendar = Calendar.getInstance();
        currentMonth = new SimpleDateFormat("MMMM").format(calendar.getTime());
        activityMainBinding.currentMonth.setText(currentMonth);

        //Set months in dropdown
        PopupMenu popupMenu = new PopupMenu(this, activityMainBinding.currentMonthIcon);
        popupMenu.inflate(R.menu.months);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            currentMonth = menuItem.getTitle().toString();
            activityMainBinding.currentMonth.setText(currentMonth);
            refreshAdapterData();
            return true;
        });

        //Show month dropdown on icon click
        activityMainBinding.currentMonthIcon.setOnClickListener(view -> {
            popupMenu.show();
        });

        //Get transactions for this month from database
        transactions = database.getTransactionsByMonth(currentMonth);
        transactionListAdapter = new TransactionListAdapter(this, transactions);
        noTransactionsLayer = activityMainBinding.noTransactionsLayer;
        transactionsList = activityMainBinding.transactionList;
        transactionsList.setAdapter(transactionListAdapter);
        activityMainBinding.transactionList.setClickable(true);
        activityMainBinding.transactionList.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(this, SingleTransaction.class);
            intent.putExtra("id", transactions.get(i).getId());
            intent.putExtra("type", transactions.get(i).getType());
            intent.putExtra("name", transactions.get(i).getName());
            intent.putExtra("amount", transactions.get(i).getAmount());
            intent.putExtra("category", transactions.get(i).getCategory());
            intent.putExtra("createdAt", transactions.get(i).getCreatedAt());
            singleTransactionActivity.launch(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        });

        if (transactions != null && transactions.size() < 1) {
            noTransactionsLayer.setVisibility(View.VISIBLE);
            transactionsList.setVisibility(View.GONE);
        } else {
            noTransactionsLayer.setVisibility(View.GONE);
            transactionsList.setVisibility(View.VISIBLE);
        }

        //Add transaction btn
        FloatingActionButton addTransactionBtn = activityMainBinding.addTransactionBtn;
        addTransactionBtn.setOnClickListener((view) -> {
            Intent intent = new Intent(this, AddTransaction.class);
            addTransactionActivity.launch(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
        });

        //Read all sms
        Util.readAllSms(currentMonth);

        //Add listener to bottom app bar items
        activityMainBinding.bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.settingsTab:
                    Intent intent = new Intent(this, SettingsActivity.class);
                    settingsActivity.launch(intent);
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
                    break;

                case R.id.reportTab:
                    break;

                default:
                    break;

            }
            return true;
        });


        //Refresh transaction list
        activityMainBinding.refreshTransactionList.setOnRefreshListener(() -> {
            Util.readAllSms(currentMonth);
            activityMainBinding.refreshTransactionList.setRefreshing(false);
            Snackbar.make(
                            activityMainBinding.transactionList,
                            "Transactions have been refreshed",
                            Snackbar.LENGTH_SHORT
                    ).setBackgroundTint(Color.BLACK)
                    .setTextColor(Color.WHITE)
                    .show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.bottom_bar_items, menu);
        return true;
    }

    private void storeCategories() {
        if (SharedPrefHelper.getCategories() == null) {
            List<String> categories = new ArrayList<>();
            categories.add("Food");
            categories.add("Entertainment");
            categories.add("Investment");
            categories.add("Sports");
            categories.add("Fuel");
            categories.add("General");
            categories.add("Holidays");
            categories.add("Travel");
            categories.add("Gifts");
            categories.add("Shopping");
            categories.add("Clothes");
            categories.add("Movies");
            categories.add("Salary");
            categories.add("Custom");

            try {
                String jsonString = new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .writeValueAsString(categories);

                SharedPrefHelper.setCategories(jsonString);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
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
        }
    }

    public void refreshAdapterData() {
        transactions.clear();
        transactions.addAll(database.getTransactionsByMonth(currentMonth));

        if (transactions.size() > 0) {
            transactionsList.setVisibility(View.VISIBLE);
            noTransactionsLayer.setVisibility(View.GONE);
        } else {
            transactionsList.setVisibility(View.GONE);
            noTransactionsLayer.setVisibility(View.VISIBLE);
        }

        transactionListAdapter.notifyDataSetChanged();
        refreshMainDashboardData();
    }

    @SuppressLint("SetTextI18n")
    private void refreshMainDashboardData() {
        //Set current balance
        Long balance = (Math.round(database.getBalance() * 100) / 100);
        activityMainBinding.currentAmount.setText(MessageFormat.format("₹{0}", balance));
        if (balance < Long.parseLong(SharedPrefHelper.getBalanceLimit())) {
            activityMainBinding.currentAmount.setTextColor(Color.RED);
        } else {
            activityMainBinding.currentAmount.setTextColor(Color.WHITE);
        }


        //Set expense amount
        Long expense = (Math.round(database.getAmountSpent(currentMonth) * 100) / 100);
        activityMainBinding.amountSpent.setText(MessageFormat.format("₹{0}", expense));
        if (expense > Long.parseLong(SharedPrefHelper.getExpenseLimit())) {
            activityMainBinding.amountSpent.setTextColor(Color.RED);
        } else {
            activityMainBinding.amountSpent.setTextColor(Color.WHITE);
        }

        activityMainBinding.greeting.setText("Hello " + SharedPrefHelper.getUsername());
    }

    @SuppressLint("SetTextI18n")
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

        //Activity register for settings activity class
        settingsActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    //Do nothing for now
                }
        );

        //Activity register for username activity class
        usernameActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        boolean success = Boolean.parseBoolean(data != null ? data.getStringExtra("success") : "false");
                        if (success) {
                            checkSmsPermissions();
                            String username = SharedPrefHelper.getUsername();
                            activityMainBinding.greeting.setText("Hello " + username);
                        }
                    }
                }
        );
    }

    @SuppressLint("SetTextI18n")
    private void showUsernameActivity() {
        String username = SharedPrefHelper.getUsername();
        if (username == null) {
            Intent intent = new Intent(this, UsernameActivity.class);
            usernameActivity.launch(intent);
        } else {
            activityMainBinding.greeting.setText("Hello " + username);
        }
    }

    public static MainActivity getInstance() {
        return mainActivityWeakReference.get();
    }

}