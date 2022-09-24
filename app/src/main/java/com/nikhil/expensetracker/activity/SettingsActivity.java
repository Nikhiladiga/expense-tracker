package com.nikhil.expensetracker.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.databinding.ActivitySettingsBinding;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding activity_settings;
    private ArrayAdapter<String> settingsAdapter;
    private final List<String> settingsItems = new ArrayList<>();

    public SettingsActivity() {
        settingsItems.add("Account");
        settingsItems.add("Reset Data");
        settingsItems.add("Set Balance Limit");
        settingsItems.add("Set Expense Limit");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity_settings = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(activity_settings.getRoot());

        //Set adapter to settings list element
        settingsAdapter = new ArrayAdapter<>(this, androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item, settingsItems);
        activity_settings.settingsItems.setAdapter(settingsAdapter);

        //Clear app data dialogue box
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    MainActivity.getInstance().database.deleteAllTransactions();
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    Toast.makeText(this, "App data cleared", Toast.LENGTH_SHORT).show();
                    MainActivity.getInstance().refreshAdapterData();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        activity_settings.settingsItems.setOnItemClickListener((adapterView, view, i, l) -> {
            switch (settingsItems.get(i)) {
                case "Account":
                    Toast.makeText(this, "Make changes to account", Toast.LENGTH_SHORT).show();
                    break;

                case "Reset Data":
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Are you sure you want to clear app data?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener)
                            .show();
                    break;

                case "Set Balance Limit":
                    break;

                case "Set Expense Limit":
                    break;
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
    }
}