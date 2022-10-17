package com.nikhil.expensetracker.model;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;

public class TransactionRow {
    private String category;
    private Double amount;
    private Float contribution;
    private final DecimalFormat decimalFormat = new DecimalFormat();

    public TransactionRow(String category, Double amount, Float contribution) {
        this.decimalFormat.setMaximumFractionDigits(2);
        this.category = category;
        this.amount = amount;
        this.contribution = Float.valueOf(this.decimalFormat.format(contribution));
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Float getContribution() {
        return contribution;
    }

    public void setContribution(Float contribution) {
        this.contribution = Float.valueOf(decimalFormat.format(contribution));
    }

    public static int compare(TransactionRow t1, TransactionRow t2) {
        if (t1.getContribution().equals(t2.getContribution())) {
            return 0;
        } else if (t1.getContribution() < t2.getContribution()) {
            return 1;
        } else {
            return -1;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "TransactionRow{" +
                "category='" + category + '\'' +
                ", amount=" + amount +
                ", contribution=" + contribution +
                '}';
    }

}
