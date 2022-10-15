package com.nikhil.expensetracker.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.android.material.textfield.TextInputLayout;
import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.adapters.SettingsListAdapter;
import com.nikhil.expensetracker.adapters.TransactionListAdapter;
import com.nikhil.expensetracker.databinding.ActivitySettingsBinding;
import com.nikhil.expensetracker.datahelpers.SharedPrefHelper;
import com.nikhil.expensetracker.utils.CustomDialog;
import com.nikhil.expensetracker.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements CustomDialog.CustomDialogListener {

    private ActivitySettingsBinding activity_settings;
    private SettingsListAdapter settingsListAdapter;
    private List<String> settingsOpt = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity_settings = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(activity_settings.getRoot());

        //Set adapter to settings list element
        settingsOpt.add("Username");
        settingsOpt.add("Month Start Day");
        settingsOpt.add("Balance Limit");
        settingsOpt.add("Expense Limit");
        settingsOpt.add("Custom Expense");
        settingsOpt.add("Total Balance");
        settingsOpt.add("Reset Data");
        settingsListAdapter = new SettingsListAdapter(this, settingsOpt, getSupportFragmentManager());
        activity_settings.settingsItemList.setLayoutManager(new GridLayoutManager(getBaseContext(), 2));
        activity_settings.settingsItemList.setAdapter(settingsListAdapter);
        activity_settings.settingsItemList.setClickable(true);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void applyValues(String customInputValue, String customInputTitle) {
        if (customInputTitle.equalsIgnoreCase("Username")) {
            SharedPrefHelper.setUsername(customInputValue, this);
            settingsListAdapter.notifyDataSetChanged();
        } else if (customInputTitle.equalsIgnoreCase("Balance Limit")) {
            SharedPrefHelper.setBalanceLimit(customInputValue);
            settingsListAdapter.notifyDataSetChanged();
        } else if (customInputTitle.equalsIgnoreCase("Expense Limit")) {
            SharedPrefHelper.setExpenseLimit(customInputValue);
            settingsListAdapter.notifyDataSetChanged();
        }
        MainActivity.getInstance().refreshAdapterData();
    }
}