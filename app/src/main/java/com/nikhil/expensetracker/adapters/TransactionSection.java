package com.nikhil.expensetracker.adapters;

import androidx.annotation.NonNull;

import com.nikhil.expensetracker.model.Transaction;

import java.util.List;

public class TransactionSection {
    private String date;
    private List<Transaction> transactions;

    public TransactionSection(String date, List<Transaction> transactions) {
        this.date = date;
        this.transactions = transactions;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public String toString() {
        return "TransactionSection{" +
                "date='" + date + '\'' +
                ", transactions=" + transactions +
                '}';
    }
}
