package com.nikhil.expensetracker.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.Util;

import java.util.List;
import java.util.Objects;

public class TransactionListAdapter extends ArrayAdapter<Transaction> {

    private boolean isLongPressed = false;

    public TransactionListAdapter(@NonNull Context context, List<Transaction> transactions) {
        super(context, R.layout.list_item, transactions);
    }

    public void showCheckBox() {
        isLongPressed = true;
        notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Transaction transaction = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        TextView categoryEmoji = convertView.findViewById(R.id.transactionImg);
        TextView transactionName = convertView.findViewById(R.id.transactionName);
        TextView transactionCategory = convertView.findViewById(R.id.transactionCategory);
        TextView transactionAmount = convertView.findViewById(R.id.transactionAmount);
        TextView transactionDate = convertView.findViewById(R.id.transactionDate);
        TextView transactionBank = convertView.findViewById(R.id.transactionBank);

        if (Objects.equals(transaction.getType(), "CREDIT")) {
            transactionAmount.setTextColor(Color.GREEN);
            transactionAmount.setText("+ ₹" + transaction.getAmount().toString());
        } else {
            transactionAmount.setTextColor(Color.RED);
            transactionAmount.setText("- ₹" + transaction.getAmount().toString());
        }

        categoryEmoji.setText(Util.getTransactionCategoryEmoji(transaction.getCategory()));

        transactionName.setText(transaction.getName());
        transactionCategory.setText(transaction.getCategory());
        transactionDate.setText(Util.convertTimestampToDate(transaction.getCreatedAt()));
        transactionBank.setText(transaction.getBank());

        System.out.println(transaction.getBank());

        return convertView;
    }
}
