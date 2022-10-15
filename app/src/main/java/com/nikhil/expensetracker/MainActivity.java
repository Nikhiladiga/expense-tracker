package com.nikhil.expensetracker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import com.nikhil.expensetracker.activity.ReportActivity;
import com.nikhil.expensetracker.activity.SettingsActivity;
import com.nikhil.expensetracker.activity.UsernameActivity;
import com.nikhil.expensetracker.adapters.TransactionMainAdapter;
import com.nikhil.expensetracker.adapters.TransactionSection;
import com.nikhil.expensetracker.datahelpers.Database;
import com.nikhil.expensetracker.databinding.ActivityMainBinding;
import com.nikhil.expensetracker.datahelpers.SharedPrefHelper;
import com.nikhil.expensetracker.model.DashboardData;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.DateUtils;
import com.nikhil.expensetracker.utils.Util;

import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private final String APP_DOWNLOAD_URL = "https://github.com/Nikhiladiga/LcFX/releases/tag/v1.0.0";

    public Database database;
    private List<Transaction> transactions = new ArrayList<>();
    private List<TransactionSection> transactionSections = new ArrayList<>();
    private TransactionMainAdapter transactionMainAdapter;

    private static WeakReference<MainActivity> mainActivityWeakReference;

    public ActivityMainBinding mBinding;
    private boolean isMultiSelectEnabled = false;

    private ActivityResultLauncher<Intent> addTransactionActivity;
    private ActivityResultLauncher<Intent> settingsActivity;
    private ActivityResultLauncher<Intent> usernameActivity;
    private ActivityResultLauncher<Intent> reportActivity;
    private ActivityResultLauncher<Intent> singleTransactionActivity;

    private RelativeLayout noTransactionsLayer;
    private RecyclerView transactionsList;

    private String currentMonth;
    private Double balance = (double) 0;
    private Double expense = (double) 0;

    @SuppressLint({"SimpleDateFormat", "NonConstantResourceId", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
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

        //Set menu items to bottom app bar left and right
        setSupportActionBar(mBinding.bottomAppBar);
        getMenuInflater().inflate(R.menu.left_menu, mBinding.leftMenu.getMenu());

        //GET current month
        currentMonth = DateUtils.getCurrentMonth();
        mBinding.currentMonth.setText(currentMonth);

        //Set months in dropdown
        PopupMenu popupMenu = new PopupMenu(this, mBinding.currentMonthIcon);
        popupMenu.inflate(R.menu.months);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            currentMonth = menuItem.getTitle().toString();
            mBinding.currentMonth.setText(currentMonth);
            refreshAdapterData();
            return true;
        });

        //Show month dropdown on icon click
        mBinding.currentMonthIcon.setOnClickListener(view -> {
            popupMenu.show();
        });

        //Get transactions,balance and expense for this month from database
        DashboardData dashboardData = database.getTransactionsByMonth(currentMonth);
        transactions = dashboardData.getTransactions();
        balance = dashboardData.getBalance();
        expense = dashboardData.getExpense();

        //GET total balance
        refreshMainDashboardData();

        //Init data for obtaining sectional data
        initData(dashboardData.getTransactions());

        //Set transactions list adapter and functionality
        transactionsList = mBinding.transactionList;
        noTransactionsLayer = mBinding.noTransactionsLayer;

        transactionMainAdapter = new TransactionMainAdapter(transactionSections, getBaseContext(), singleTransactionActivity);
        transactionsList.setAdapter(transactionMainAdapter);
        transactionsList.setLayoutManager(new LinearLayoutManager(this));
        transactionsList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        //Show checkbox indicating multi select mode
        mBinding.multiSelect.setOnClickListener(view -> {
            isMultiSelectEnabled = !isMultiSelectEnabled;
            showUnshowCheckBox();
        });

        //Close toolbar and undo edit mode on close btn click
        mBinding.clearMultiSelect.setOnClickListener(view -> {
            isMultiSelectEnabled = false;
            showUnshowCheckBox();
        });

        if (transactions != null && transactions.size() < 1) {
            noTransactionsLayer.setVisibility(View.VISIBLE);
            transactionsList.setVisibility(View.GONE);
        } else {
            noTransactionsLayer.setVisibility(View.GONE);
            transactionsList.setVisibility(View.VISIBLE);
        }

        //Add transaction btn
        FloatingActionButton addTransactionBtn = mBinding.addTransactionBtn;
        addTransactionBtn.setOnClickListener((view) -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            addTransactionActivity.launch(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
        });

        //Add listener to bottom app bar items
        mBinding.bottomAppBar.setOnMenuItemClickListener(item -> {
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

        mBinding.leftMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.shareApp:
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    String body = "Download expense tracker here.";
                    intent.putExtra(Intent.EXTRA_TEXT, body);
                    intent.putExtra(Intent.EXTRA_TEXT, APP_DOWNLOAD_URL);
                    startActivity(Intent.createChooser(intent, "Share using"));
                    break;

                default:
                    break;
            }
            return true;
        });


        //Refresh transaction list
        mBinding.refreshTransactionList.setOnRefreshListener(() -> {
            Util.readAllSms(currentMonth);
            mBinding.refreshTransactionList.setRefreshing(false);
            Snackbar.make(
                            mBinding.transactionList,
                            "Transactions have been refreshed",
                            Snackbar.LENGTH_SHORT
                    ).setBackgroundTint(Color.BLACK)
                    .setTextColor(Color.WHITE)
                    .show();
            isMultiSelectEnabled = false;
            showUnshowCheckBox();
        });

        //Searchview for transaction filtering
        mBinding.transactionSearch.clearFocus();
        mBinding.transactionSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextChange(String newText) {
                List<Transaction> filteredItems = transactions.stream().filter(transaction -> transaction.getName().toLowerCase().contains(newText)).collect(Collectors.toList());
                initData(filteredItems);

                if (filteredItems.size() < 1) {
                    transactionMainAdapter.updateData(new ArrayList<>());
                } else {
                    transactionMainAdapter.updateData(transactionSections);
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
        getMenuInflater().inflate(R.menu.right_menu, menu);
        return true;
    }

    private void storeCategories() {
        if (SharedPrefHelper.getCategories() == null) {
            List<String> categories = new ArrayList<>();
            categories.add("Food");
            categories.add("Grocery");
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

        initData(dashboardData.getTransactions());

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

        transactionMainAdapter.updateData(transactionSections);

        refreshMainDashboardData();
    }

    @SuppressLint("SetTextI18n")
    private void refreshMainDashboardData() {
        //Set current balance
        Long balance = (Math.round(this.balance) * 100) / 100;
        mBinding.currentAmount.setText(MessageFormat.format("₹{0}", balance));
        if (balance < Long.parseLong(SharedPrefHelper.getBalanceLimit() == null ? String.valueOf(0) : SharedPrefHelper.getBalanceLimit())) {
            mBinding.currentAmount.setTextColor(Color.RED);
        } else {
            mBinding.currentAmount.setTextColor(Color.WHITE);
        }

        //Set expense amount
        Long expense = (Math.round(this.expense) * 100) / 100;
        mBinding.amountSpent.setText(MessageFormat.format("₹{0}", expense));
        if (expense > Long.parseLong(SharedPrefHelper.getExpenseLimit() == null ? String.valueOf(0) : SharedPrefHelper.getExpenseLimit())) {
            mBinding.amountSpent.setTextColor(Color.RED);
        } else {
            mBinding.amountSpent.setTextColor(Color.WHITE);
        }

        if (SharedPrefHelper.getUsername() != null) {
            mBinding.greeting.setText("Hello " + SharedPrefHelper.getUsername());
        } else {
            mBinding.greeting.setText("Hello");
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
                            mBinding.greeting.setText("Hello " + username);
                        }
                    }
                }
        );


        //Activity register for report activity class
        reportActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    //Do nothing
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
            mBinding.greeting.setText("Hello " + username);
        }
    }

    private void showUnshowCheckBox() {
        if (isMultiSelectEnabled) {
            mBinding.multiEditToolbar.setVisibility(View.VISIBLE);
            mBinding.multiEditToolbar.animate().translationY(0).setDuration(300L).start();
            mBinding.dashboardCard.setVisibility(View.GONE);
            mBinding.refreshTransactionList.setPadding(0, mBinding.multiEditToolbar.getHeight(), 0, 0);
            mBinding.header.setVisibility(View.GONE);
        } else {
            if (mBinding.multiEditToolbar.getVisibility() == View.VISIBLE) {
                mBinding.multiEditToolbar.animate().translationY(-112).setDuration(300L).withEndAction(() -> mBinding.multiEditToolbar.setVisibility(View.GONE)).start();
            }
            mBinding.multiSelect.setImageResource(R.drawable.ic_baseline_search_24);
            mBinding.dashboardCard.setVisibility(View.VISIBLE);
            mBinding.refreshTransactionList.setPadding(0, 0, 0, 0);
            mBinding.header.setVisibility(View.VISIBLE);
        }
    }

    private void initData(List<Transaction> currentTransactions) {
        LinkedHashMap<String, List<Transaction>> transactionConcurrentHashMap = new LinkedHashMap<>();
        for (Transaction transaction : currentTransactions) {
            String currentTransactionDate = DateUtils.convertTimestampToDate(transaction.getCreatedAt());
            //If an array is already present
            if (transactionConcurrentHashMap.containsKey(currentTransactionDate)) {
                Objects.requireNonNull(transactionConcurrentHashMap.get(currentTransactionDate)).add(transaction);
            } else {
                //If adding element for the first time
                List<Transaction> transactionsForDate = new ArrayList<>();
                transactionsForDate.add(transaction);
                transactionConcurrentHashMap.put(currentTransactionDate, transactionsForDate);
            }
        }

        transactionSections.clear();

        for (Map.Entry<String, List<Transaction>> entry : transactionConcurrentHashMap.entrySet()) {
            TransactionSection transactionSection = new TransactionSection(entry.getKey(), entry.getValue());
            transactionSections.add(transactionSection);
        }

    }

    public static MainActivity getInstance() {
        return mainActivityWeakReference.get();
    }

}