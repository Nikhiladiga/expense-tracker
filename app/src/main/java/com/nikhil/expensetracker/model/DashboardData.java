package com.nikhil.expensetracker.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DashboardData {
    private List<Transaction> transactions = new ArrayList<>();
    private Double balance;
    private Double expense;

    public DashboardData(List<Transaction> transactions, Double balance, Double expense) {
        this.transactions.addAll(transactions);
        this.balance = balance;
        this.expense = expense;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Double getExpense() {
        return expense;
    }

    public void setExpense(Double expense) {
        this.expense = expense;
    }

    @NonNull
    @Override
    public String toString() {
        return "DashboardData{" +
                "transactions=" + transactions.size() +
                ", balance=" + balance +
                ", expense=" + expense +
                '}';
    }
}
