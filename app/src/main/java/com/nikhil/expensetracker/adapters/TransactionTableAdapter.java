package com.nikhil.expensetracker.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.model.TransactionRow;

import java.util.ArrayList;
import java.util.List;

public class TransactionTableAdapter extends RecyclerView.Adapter<TransactionTableAdapter.TransactionTableViewHolder> {

    private List<TransactionRow> transactionRows = new ArrayList<>();
    private Context context;

    public TransactionTableAdapter(List<TransactionRow> transactionRows, Context context) {
        this.transactionRows.addAll(transactionRows);
        this.context = context;
    }

    @NonNull
    @Override
    public TransactionTableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.transaction_table_item, parent, false);
        return new TransactionTableViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TransactionTableViewHolder holder, int position) {
        TransactionRow transactionRow = transactionRows.get(position);

        holder.tableRowCategory.setText(transactionRow.getCategory());
        holder.tableRowAmount.setText("₹" + transactionRow.getAmount());
        holder.tableRowPercent.setText(transactionRow.getContribution() + "%");
    }

    @Override
    public int getItemCount() {
        return transactionRows.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<TransactionRow> transactionRows) {
        this.transactionRows.clear();
        this.transactionRows.addAll(transactionRows);
        notifyDataSetChanged();
    }

    static class TransactionTableViewHolder extends RecyclerView.ViewHolder {

        private final TextView tableRowCategory, tableRowAmount, tableRowPercent;

        public TransactionTableViewHolder(@NonNull View itemView) {
            super(itemView);
            tableRowCategory = itemView.findViewById(R.id.tableRowCategory);
            tableRowAmount = itemView.findViewById(R.id.tableRowAmount);
            tableRowPercent = itemView.findViewById(R.id.tableRowPercent);
        }
    }
}
