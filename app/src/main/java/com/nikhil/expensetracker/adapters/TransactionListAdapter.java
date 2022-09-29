package com.nikhil.expensetracker.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.activity.SingleTransaction;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.Util;

import java.util.List;
import java.util.Objects;

public class TransactionListAdapter extends RecyclerView.Adapter<TransactionListAdapter.TransactionViewHolder> {

    private boolean isCheckBoxShown = false;
    private List<Transaction> transactions;
    private Context context;
    private int activeTransactionPosition = 0;
    private ActivityResultLauncher<Intent> singleTransactionActivity;

    public TransactionListAdapter(@NonNull Context context, List<Transaction> transactions, ActivityResultLauncher<Intent> singleTransactionActivity) {
        this.context = context;
        this.transactions = transactions;
        this.singleTransactionActivity = singleTransactionActivity;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void showCheckBox(boolean isShown) {
        isCheckBoxShown = isShown;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFilteredTransactions(List<Transaction> filteredTransactions) {
        this.transactions = filteredTransactions;
        notifyDataSetChanged();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryEmoji;
        private TextView transactionName;
        private TextView transactionCategory;
        private TextView transactionAmount;
        private TextView transactionDate;
        private TextView transactionBank;
        private View parentView;
        private CheckBox isSelected;

        TransactionViewHolder(@NonNull View view) {
            super(view);
            this.parentView = view;
            categoryEmoji = view.findViewById(R.id.transactionImg);
            transactionName = view.findViewById(R.id.transactionName);
            transactionCategory = view.findViewById(R.id.transactionCategory);
            transactionAmount = view.findViewById(R.id.transactionAmount);
            transactionDate = view.findViewById(R.id.transactionDate);
            transactionBank = view.findViewById(R.id.transactionBank);
            isSelected = view.findViewById(R.id.isSelected);
        }

    }

    @NonNull
    @Override //Method called when view is created for the very first time
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransactionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        if (Objects.equals(transaction.getType(), "CREDIT")) {
            holder.transactionAmount.setTextColor(Color.GREEN);
            holder.transactionAmount.setText("+ ₹" + transaction.getAmount().toString());
        } else {
            holder.transactionAmount.setTextColor(Color.RED);
            holder.transactionAmount.setText("- ₹" + transaction.getAmount().toString());
        }

        holder.categoryEmoji.setText(transaction.getEmoji());

        holder.transactionName.setText(transaction.getName());
        holder.transactionCategory.setText(transaction.getCategory());
        holder.transactionDate.setText(Util.convertTimestampToDate(transaction.getCreatedAt()));
        holder.transactionBank.setText(transaction.getBank());

        //Show/unshow checkbox
        if (isCheckBoxShown) {
            holder.isSelected.setVisibility(View.VISIBLE);
        } else {
            holder.isSelected.setVisibility(View.GONE);
        }

        holder.isSelected.setOnCheckedChangeListener(null);

        holder.isSelected.setChecked(transactions.get(position).isSelected());

        holder.isSelected.setOnCheckedChangeListener((compoundButton, b) -> {
            boolean isChecked = !transactions.get(position).isSelected();
            transactions.get(position).setSelected(isChecked);
        });

        holder.parentView.setClickable(true);
        holder.parentView.setOnClickListener(view -> {
            Intent intent = new Intent(this.context, SingleTransaction.class);
            intent.putExtra("id", transactions.get(position).getId());
            intent.putExtra("type", transactions.get(position).getType());
            intent.putExtra("name", transactions.get(position).getName());
            intent.putExtra("amount", transactions.get(position).getAmount());
            intent.putExtra("category", transactions.get(position).getCategory());
            intent.putExtra("createdAt", transactions.get(position).getCreatedAt());
            intent.putExtra("bankName", transactions.get(position).getBank());
            intent.putExtra("emoji", transactions.get(position).getEmoji());
            singleTransactionActivity.launch(intent);
            MainActivity.getInstance().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        });

    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }
}
