package com.nikhil.expensetracker.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.DateUtils;
import com.nikhil.expensetracker.utils.StringUtils;

import java.util.List;

public class TransactionMainAdapter extends RecyclerView.Adapter<TransactionMainAdapter.TransactionViewHolder> {

    List<TransactionSection> sections;
    private Context context;
    private ActivityResultLauncher<Intent> singleTransactionActivity;
    private TransactionChildAdapter transactionChildAdapter;

    public TransactionMainAdapter(List<TransactionSection> transactionSections, Context context, ActivityResultLauncher<Intent> singleTransactionActivity) {
        this.sections = transactionSections;
        this.context = context;
        this.singleTransactionActivity = singleTransactionActivity;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.transaction_section_row, parent, false);
        return new TransactionViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {

        TransactionSection transactionSection = sections.get(position);

        String date = transactionSection.getDate();
        List<Transaction> items = transactionSection.getTransactions();
        double amount = items.stream().filter(transaction -> transaction.getType().equals("DEBIT")).mapToDouble(Transaction::getAmount).sum();

        holder.transactionHeader.setText(date + " - " + DateUtils.getDay(date));
        holder.transactionHeaderAmount.setText("₹" + amount);

        transactionChildAdapter = new TransactionChildAdapter(items, context, singleTransactionActivity);
        holder.childRecyclerView.setAdapter(transactionChildAdapter);

    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<TransactionSection> transactionSections) {
        System.out.println("TRANSACTION SECTIONS:" + transactionSections);
        sections = transactionSections;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {

        private TextView transactionHeader;
        private TextView transactionHeaderAmount;
        private RecyclerView childRecyclerView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionHeader = itemView.findViewById(R.id.transactionHeader);
            transactionHeaderAmount = itemView.findViewById(R.id.transactionHeaderAmount);
            childRecyclerView = itemView.findViewById(R.id.transactionSectionList);
        }
    }

}
