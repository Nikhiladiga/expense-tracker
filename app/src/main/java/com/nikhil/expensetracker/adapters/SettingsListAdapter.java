package com.nikhil.expensetracker.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.datahelpers.Database;
import com.nikhil.expensetracker.datahelpers.SharedPrefHelper;
import com.nikhil.expensetracker.utils.CustomDialog;

import java.util.List;

public class SettingsListAdapter extends RecyclerView.Adapter<SettingsListAdapter.SettingsViewHolder> {

    private List<String> settingsItems;
    private Context context;
    private FragmentManager supportFragmentManager;

    public SettingsListAdapter(@NonNull Context context, List<String> settingsItems, FragmentManager supportFragmentManager) {
        this.settingsItems = settingsItems;
        this.context = context;
        this.supportFragmentManager = supportFragmentManager;
    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SettingsListAdapter.SettingsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_list_item, parent, false));
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        String item = settingsItems.get(position);
        switch (item) {
            case "Username":
                holder.itemTitle.setText(item);
                holder.itemValue.setText(SharedPrefHelper.getUsername());
                break;
            case "Reset Data":
                holder.itemCard.setStrokeColor(Color.RED);
                holder.itemCard.setStrokeWidth(10);
                holder.itemTitle.setText(item);
                holder.itemValue.setText("❌");
                break;
            case "Month Start Day":
                holder.itemTitle.setText(item);
                holder.itemValue.setText(SharedPrefHelper.getMonthStartDay());
                break;
            case "Balance Limit":
                holder.itemTitle.setText(item);
                holder.itemValue.setText(SharedPrefHelper.getBalanceLimit());
                break;
            case "Expense Limit":
                holder.itemTitle.setText(item);
                holder.itemValue.setText(SharedPrefHelper.getExpenseLimit());
                break;
            case "Total Balance":
                holder.itemCard.setStrokeColor(Color.rgb(56, 146, 251));
                holder.itemCard.setStrokeWidth(10);
                holder.itemTitle.setText(item);
                holder.itemValue.setText("₹" + Math.floor(MainActivity.getInstance().database.getTotalBalance() * 100) / 100);

            default:
                break;
        }

        holder.parentView.setOnClickListener(view -> {
            switch (item) {
                case "Username":
                    CustomDialog usernameDialog = new CustomDialog(SharedPrefHelper.getUsername(), "Username", "Update your name", "text");
                    usernameDialog.show(supportFragmentManager, "Custom dialog");
                    break;

                case "Reset Data":
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Are you sure you want to clear app data?")
                            .setPositiveButton("Yes", (dialogInterface, i1) -> {
                                MainActivity.getInstance().database.deleteAllTransactions();
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear();
                                editor.apply();
                                Toast.makeText(context, "App data cleared", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                                MainActivity.getInstance().refreshAdapterData();
                            })
                            .setNegativeButton("No", (dialogInterface, i12) -> {
                                //Do nothing
                            })
                            .show();
                    break;

                case "Month Start Day":
                    CustomDialog monthStartDayDialog = new CustomDialog(SharedPrefHelper.getMonthStartDay(), "Month Start Day", "Set month start day", "number");
                    monthStartDayDialog.show(supportFragmentManager, "Custom dialog");
                    break;

                case "Balance Limit":
                    CustomDialog balanceLimitDialog = new CustomDialog(SharedPrefHelper.getBalanceLimit(), "Balance Limit", "Set balance limit", "number");
                    balanceLimitDialog.show(supportFragmentManager, "Custom dialog");
                    break;

                case "Expense Limit":
                    CustomDialog expenseLimitDialog = new CustomDialog(SharedPrefHelper.getExpenseLimit(), "Expense Limit", "Set expense limit", "number");
                    expenseLimitDialog.show(supportFragmentManager, "Custom dialog");
                    break;
            }
        });
    }

    @Override
    public int getItemCount() {
        return settingsItems.size();
    }

    public static class SettingsViewHolder extends RecyclerView.ViewHolder {
        private TextView itemTitle;
        private TextView itemValue;
        private View parentView;
        private MaterialCardView itemCard;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.parentView = itemView;
            this.itemCard = itemView.findViewById(R.id.itemCard);
            this.itemTitle = itemView.findViewById(R.id.itemTitle);
            this.itemValue = itemView.findViewById(R.id.itemValue);
        }
    }
}
