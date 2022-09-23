package com.nikhil.expensetracker.adapters;

import static com.nikhil.expensetracker.R.drawable.ic_launcher_background;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.Util;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TransactionListAdapter extends ArrayAdapter<Transaction> {

    public TransactionListAdapter(@NonNull Context context, List<Transaction> transactions) {
        super(context, R.layout.list_item, transactions);
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

        return convertView;
    }
}
