package com.nikhil.expensetracker.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.activity.SingleTransactionActivity;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.DateUtils;

import java.util.List;
import java.util.Objects;

public class TransactionListAdapter extends RecyclerView.Adapter<TransactionListAdapter.TransactionViewHolder> {

    private boolean isCheckBoxShown = false;
    private List<Transaction> transactions;
    private Context context;
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
        return new TransactionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_list_item, parent, false));
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
        holder.transactionDate.setText(DateUtils.convertTimestampToDate(transaction.getCreatedAt()));
        holder.transactionBank.setText(transaction.getBank());

        holder.parentView.setClickable(true);
        holder.parentView.setOnClickListener(view -> {
            Intent intent = new Intent(this.context, SingleTransactionActivity.class);
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
