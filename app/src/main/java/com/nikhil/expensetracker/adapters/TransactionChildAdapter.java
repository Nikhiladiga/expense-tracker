package com.nikhil.expensetracker.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.activity.SingleTransactionActivity;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.DateUtils;

import java.util.List;
import java.util.Objects;

public class TransactionChildAdapter extends RecyclerView.Adapter<TransactionChildAdapter.TransactionViewHolder> {

    List<Transaction> transactions;
    private boolean isCheckBoxShown = false;
    private Context context;
    private ActivityResultLauncher<Intent> singleTransactionActivity;

    public TransactionChildAdapter(List<Transaction> transactions, Context context, ActivityResultLauncher<Intent> singleTransactionActivity) {
        this.transactions = transactions;
        this.context = context;
        this.singleTransactionActivity = singleTransactionActivity;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.transaction_list_item, parent, false);
        return new TransactionViewHolder(view);
    }

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

        if (transaction.isCustom() == 1) {
            @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = MainActivity.getInstance().getResources().getDrawable(R.drawable.circle);
            holder.transactionName.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }

        holder.transactionName.setText(transaction.getName());

        holder.transactionCategory.setText(transaction.getCategory());
        holder.transactionDate.setText(DateUtils.convertTimestampToDate(transaction.getCreatedAt()));
        holder.transactionBank.setText(transaction.getBank());

        holder.parentView.setClickable(true);
        holder.parentView.setOnClickListener(view -> {
            Intent intent = new Intent(this.context, SingleTransactionActivity.class);
            intent.putExtra("id", transaction.getId());
            intent.putExtra("type", transaction.getType());
            intent.putExtra("name", transaction.getName());
            intent.putExtra("amount", transaction.getAmount());
            intent.putExtra("category", transaction.getCategory());
            intent.putExtra("createdAt", transaction.getCreatedAt());
            intent.putExtra("bankName", transaction.getBank());
            intent.putExtra("emoji", transaction.getEmoji());
            intent.putExtra("isCustom", transaction.isCustom());
            singleTransactionActivity.launch(intent);
            MainActivity.getInstance().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
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
}
