package com.nikhil.expensetracker.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.datahelpers.Database;
import com.nikhil.expensetracker.datahelpers.SharedPrefHelper;

import java.util.List;

public class SettingsListAdapter extends ArrayAdapter<String> {

    public SettingsListAdapter(@NonNull Context context, List<String> settingsItems) {
        super(context, R.layout.list_item, settingsItems);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String setting = getItem(position);
        System.out.println("SETTING:" + setting);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_list_item, parent, false);
        }

        TextView settingsTitle = convertView.findViewById(R.id.itemTitle);
        TextView settingsValue = convertView.findViewById(R.id.itemValue);

        System.out.println("SETTINGS TITLE:" + settingsTitle);
        System.out.println("SETTINGS VALUE:" + settingsValue);

        switch (setting) {
            case "Account":
                settingsTitle.setText(setting);
                settingsValue.setText(SharedPrefHelper.getUsername());
                break;
            case "Reset Data":
                settingsTitle.setText(setting);
                settingsValue.setText("");
                break;
            case "Month Start Day":
                settingsTitle.setText(setting);
                settingsValue.setText(SharedPrefHelper.getMonthStartDay());
                break;
            case "Balance Limit":
                settingsTitle.setText(setting);
                settingsValue.setText(SharedPrefHelper.getBalanceLimit());
                break;
            case "Expense Limit":
                settingsTitle.setText(setting);
                settingsValue.setText(SharedPrefHelper.getExpenseLimit());
                break;
            case "Total Balance":
                settingsTitle.setText(setting);
                settingsValue.setText("₹" + Math.floor(MainActivity.getInstance().database.getTotalBalance() * 100) / 100);

            default:
                break;
        }

        return convertView;
    }
}
