package com.nikhil.expensetracker.utils;

public class StringUtils {

    public static String convertDoubleToStringAmount(Double d) {
        String amount = String.valueOf(d);
        if (!amount.equals("null")) {
            return amount;
        } else {
            return "";
        }
    }

    public static Double convertStringAmountToDouble(String amount) {
        return Double.valueOf(amount);
    }

    public static boolean checkIfCustomCategory(String category) {
        return category.equals("Custom");
    }

    public static boolean checkIfCustomTransaction(String type) {
        return type.equals("DEBIT");
    }

}
