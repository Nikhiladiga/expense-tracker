package com.nikhil.expensetracker.model;

import androidx.annotation.NonNull;

import java.math.BigInteger;

public class Transaction {
    private String id;
    private String type;
    private String name;
    private Double amount;
    private String category;
    private String createdAt;
    private String updatedAt;
    private Double balance;

    public Transaction() {
    }

    public Transaction(String id, String type, String name, Double amount, String category, String createdAt, String updatedAt, Double balance) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    @NonNull
    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", balance=" + balance +
                '}';
    }
}
