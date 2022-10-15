package com.nikhil.expensetracker.model;

import androidx.annotation.NonNull;

import java.math.BigInteger;

public class Transaction {
    private String id;
    private String type;
    private String name;
    private Double amount;
    private String category;
    private Long createdAt;
    private Long updatedAt;
    private Double balance;
    private String bank;
    private String emoji;
    private Integer isCustom = 1;

    public Transaction() {
    }

    public Transaction(String id, String type, String name, Double amount, String category, Long createdAt, Long updatedAt, Double balance, String bank, String emoji, Integer isCustom) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.balance = balance;
        this.bank = bank;
        this.emoji = emoji;
        this.isCustom = isCustom;
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

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public Integer isCustom() {
        return isCustom;
    }

    public void setCustom(Integer custom) {
        isCustom = custom;
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
                ", bank='" + bank + '\'' +
                ", emoji='" + emoji + '\'' +
                ", isCustom=" + isCustom +
                '}';
    }
}
