package com.nikhil.expensetracker.model;

public class ReportData {
    private String category;
    private Double amount;

    public ReportData(String category, Double amount) {
        this.category = category;
        this.amount = amount;
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

    @Override
    public String toString() {
        return "ReportData{" +
                "category='" + category + '\'' +
                ", amount=" + amount +
                '}';
    }
}
