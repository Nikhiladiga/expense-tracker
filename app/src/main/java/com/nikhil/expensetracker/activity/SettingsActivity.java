package com.nikhil.expensetracker.activity;

import androidx.appcompat.app.AppCompatActivity;

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
        settingsOpt.add("Account");
        settingsOpt.add("Reset Data");
        settingsOpt.add("Set Balance Limit");
        settingsOpt.add("Set Expense Limit");
        settingsListAdapter = new SettingsListAdapter(this, settingsOpt);
        activity_settings.settingsItemList.setAdapter(settingsListAdapter);
        activity_settings.settingsItemList.setClickable(true);

        //Clear app data dialogue box
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };


        activity_settings.settingsItemList.setOnItemClickListener((adapterView, view, i, l) -> {
            switch (settingsOpt.get(i)) {
                case "Account":
                    CustomDialog usernameDialog = new CustomDialog(SharedPrefHelper.getUsername(), "Username", "Update your name", "text");
                    usernameDialog.show(getSupportFragmentManager(), "Custom dialog");
                    break;

                case "Reset Data":
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Are you sure you want to clear app data?")
                            .setPositiveButton("Yes", (dialogInterface, i1) -> {
                                MainActivity.getInstance().database.deleteAllTransactions();
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear();
                                editor.apply();
                                Toast.makeText(this, "App data cleared", Toast.LENGTH_SHORT).show();
                                settingsListAdapter.notifyDataSetChanged();
                                MainActivity.getInstance().refreshAdapterData();
                            })
                            .setNegativeButton("No", (dialogInterface, i12) -> {
                                //Do nothing
                            })
                            .show();
                    break;

                case "Set Balance Limit":
                    CustomDialog balanceLimitDialog = new CustomDialog(SharedPrefHelper.getBalanceLimit(), "Balance Limit", "Set balance limit", "number");
                    balanceLimitDialog.show(getSupportFragmentManager(), "Custom dialog");
                    break;

                case "Set Expense Limit":
                    CustomDialog expenseLimitDialog = new CustomDialog(SharedPrefHelper.getExpenseLimit(), "Expense Limit", "Set expense limit", "number");
                    expenseLimitDialog.show(getSupportFragmentManager(), "Custom dialog");
                    break;
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
    }

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