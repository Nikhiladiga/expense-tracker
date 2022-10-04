package com.nikhil.expensetracker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nikhil.expensetracker.activity.AddTransactionActivity;
import com.nikhil.expensetracker.activity.MultiTransactionActivity;
import com.nikhil.expensetracker.activity.ReportActivity;
import com.nikhil.expensetracker.activity.SettingsActivity;
import com.nikhil.expensetracker.activity.UsernameActivity;
import com.nikhil.expensetracker.adapters.TransactionListAdapter;
import com.nikhil.expensetracker.datahelpers.Database;
import com.nikhil.expensetracker.databinding.ActivityMainBinding;
import com.nikhil.expensetracker.datahelpers.SharedPrefHelper;
import com.nikhil.expensetracker.model.DashboardData;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.Util;

import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    public Database database;
    private TransactionListAdapter transactionListAdapter;
    private List<Transaction> transactions = new ArrayList<>();

    private static WeakReference<MainActivity> mainActivityWeakReference;

    public ActivityMainBinding activityMainBinding;
    private boolean isMultiSelectEnabled = false;

    private ActivityResultLauncher<Intent> addTransactionActivity;
    private ActivityResultLauncher<Intent> settingsActivity;
    private ActivityResultLauncher<Intent> usernameActivity;
    private ActivityResultLauncher<Intent> reportActivity;
    private ActivityResultLauncher<Intent> multiEditActivity;
    private ActivityResultLauncher<Intent> singleTransactionActivity;

    private RelativeLayout noTransactionsLayer;
    private RecyclerView transactionsList;

    private String currentMonth;
    private Double balance = (double) 0;
    private Double expense = (double) 0;

    @SuppressLint({"SimpleDateFormat", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        mainActivityWeakReference = new WeakReference<>(MainActivity.this);

        //Create shared prefs helper class
        SharedPrefHelper.initSharedPrefHelper(PreferenceManager.getDefaultSharedPreferences(this));

        //Register launchers for all activities
        registerActivities();

        //Check if username is present and show/hide activity
        showUsernameActivity();

        database = new Database(this, "expense.db", null, 1);

        //Store categories in shared prefs
        storeCategories();

        //Set menu items to bottom app bar
        setSupportActionBar(activityMainBinding.bottomAppBar);

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

        //Get transactions,balance and expense for this month from database
        DashboardData dashboardData = database.getTransactionsByMonth(currentMonth);
        transactions = dashboardData.getTransactions();
        balance = dashboardData.getBalance();
        expense = dashboardData.getExpense();

        //GET total balance
        refreshMainDashboardData();

        //Set transactions list adapter and functionality
        transactionListAdapter = new TransactionListAdapter(this, transactions, singleTransactionActivity);
        noTransactionsLayer = activityMainBinding.noTransactionsLayer;
        transactionsList = activityMainBinding.transactionList;
        transactionsList.setAdapter(transactionListAdapter);
        transactionsList.setLayoutManager(new LinearLayoutManager(this));

        //Show checkbox indicating multi select mode
        activityMainBinding.multiSelect.setOnClickListener(view -> {
            isMultiSelectEnabled = !isMultiSelectEnabled;
            showUnshowCheckBox();
        });

        //Close toolbar and undo edit mode on close btn click
        activityMainBinding.clearMultiSelect.setOnClickListener(view -> {
            isMultiSelectEnabled = false;
            showUnshowCheckBox();
        });

        //TODO Open new activity if multi edit button is clicked
        activityMainBinding.startMultiEdit.setOnClickListener(view -> {
            Intent intent = new Intent(this, MultiTransactionActivity.class);
            try {
                List<Transaction> selectedTransactions = new ArrayList<>();

                //Get transactions which have been selected
                for (int i = 0; i < transactionListAdapter.getItemCount(); i++) {
                    System.out.println("IS SELECTED:" + transactions.get(i).isSelected());
                    if (transactions.get(i).isSelected()) {
                        selectedTransactions.add(transactions.get(i));
                    }
                }

                //Stringify transactions and send it to multi edit activity
                String selectedTransactionsJSON = new ObjectMapper().
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .writeValueAsString(selectedTransactions);

                intent.putExtra("selectedTransactions", selectedTransactionsJSON);
                multiEditActivity.launch(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);

            } catch (Exception e) {
                e.printStackTrace();
            }
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
            Intent intent = new Intent(this, AddTransactionActivity.class);
            addTransactionActivity.launch(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
        });

        //Add listener to bottom app bar items
        activityMainBinding.bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.settingsTab:
                    Intent intent = new Intent(this, SettingsActivity.class);
                    settingsActivity.launch(intent);
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
                    break;

                case R.id.reportTab:
                    Intent intent1 = new Intent(this, ReportActivity.class);
                    reportActivity.launch(intent1);
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
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
            isMultiSelectEnabled = false;
            showUnshowCheckBox();
        });

        //Searchview for transaction filtering
        activityMainBinding.transactionSearch.clearFocus();
        activityMainBinding.transactionSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextChange(String newText) {
                List<Transaction> filteredItems = transactions.stream().filter(transaction -> transaction.getName().toLowerCase().contains(newText)).collect(Collectors.toList());

                if (filteredItems.size() < 1) {
                    transactionListAdapter.setFilteredTransactions(new ArrayList<>());
                } else {
                    transactionListAdapter.setFilteredTransactions(filteredItems);
                }

                return true;
            }
        });

        checkSmsPermissions();

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
                Util.readAllSms(currentMonth);
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
        } else {
            Util.readAllSms(currentMonth);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshAdapterData() {
        transactions.clear();
        isMultiSelectEnabled = false;
        showUnshowCheckBox();

        DashboardData dashboardData = database.getTransactionsByMonth(currentMonth);
        transactions.addAll(dashboardData.getTransactions());
        balance = dashboardData.getBalance();
        expense = dashboardData.getExpense();

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
        Long balance = (Math.round(this.balance) * 100) / 100;
        activityMainBinding.currentAmount.setText(MessageFormat.format("₹{0}", balance));
        if (balance < Long.parseLong(SharedPrefHelper.getBalanceLimit() == null ? String.valueOf(0) : SharedPrefHelper.getBalanceLimit())) {
            activityMainBinding.currentAmount.setTextColor(Color.RED);
        } else {
            activityMainBinding.currentAmount.setTextColor(Color.WHITE);
        }

        //Set expense amount
        Long expense = (Math.round(this.expense) * 100) / 100;
        activityMainBinding.amountSpent.setText(MessageFormat.format("₹{0}", expense));
        if (expense > Long.parseLong(SharedPrefHelper.getExpenseLimit() == null ? String.valueOf(0) : SharedPrefHelper.getExpenseLimit())) {
            activityMainBinding.amountSpent.setTextColor(Color.RED);
        } else {
            activityMainBinding.amountSpent.setTextColor(Color.WHITE);
        }

        if (SharedPrefHelper.getUsername() != null) {
            activityMainBinding.greeting.setText("Hello " + SharedPrefHelper.getUsername());
        } else {
            activityMainBinding.greeting.setText("Hello");
        }
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void registerActivities() {
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

        //Activity register for multi edit class
        multiEditActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        boolean success = false;
                        if (data != null) {
                            success = data.getBooleanExtra("success", false);
                        }
                        System.out.println("MULTI TRANSACTION UPDATE:" + success);
                        if (success) {
                            refreshAdapterData();
                        }
                    }
                }
        );


        //Activity register for report activity class
        reportActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        Intent data = result.getData();
//                        boolean success = Boolean.parseBoolean(data != null ? data.getStringExtra("success") : "false");
//                        if (success) {
//                            checkSmsPermissions();
//                            String username = SharedPrefHelper.getUsername();
//                            activityMainBinding.greeting.setText("Hello " + username);
//                        }
                    }
                }

        );

        singleTransactionActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        boolean success = false;
                        if (data != null) {
                            success = data.getBooleanExtra("success", false);
                        }
                        if (success) {
                            refreshAdapterData();
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

    private void showUnshowCheckBox() {
        if (isMultiSelectEnabled) {
            activityMainBinding.multiEditToolbar.setVisibility(View.VISIBLE);
            activityMainBinding.multiEditToolbar.animate().translationY(0).setDuration(300L).start();
            transactionListAdapter.showCheckBox(true);
            activityMainBinding.dashboardCard.setVisibility(View.GONE);
            activityMainBinding.refreshTransactionList.setPadding(0, activityMainBinding.multiEditToolbar.getHeight(), 0, 0);
            activityMainBinding.header.setVisibility(View.GONE);
        } else {
            if (activityMainBinding.multiEditToolbar.getVisibility() == View.VISIBLE) {
                activityMainBinding.multiEditToolbar.animate().translationY(-112).setDuration(300L).withEndAction(() -> activityMainBinding.multiEditToolbar.setVisibility(View.GONE)).start();
            }
            activityMainBinding.multiSelect.setImageResource(R.drawable.ic_selectmultiple);
            transactionListAdapter.showCheckBox(false);
            activityMainBinding.dashboardCard.setVisibility(View.VISIBLE);
            activityMainBinding.refreshTransactionList.setPadding(0, 0, 0, 0);
            activityMainBinding.header.setVisibility(View.VISIBLE);
        }
    }

    public static MainActivity getInstance() {
        return mainActivityWeakReference.get();
    }

}