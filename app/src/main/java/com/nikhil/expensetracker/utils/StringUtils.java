package com.nikhil.expensetracker.utils;

public class StringUtils {

    public static String convertDoubleToStringAmount(Double d) {
        return "₹" + d;
    }

    public static Double convertStringAmountToDouble(String amount) {
        return Double.valueOf(amount.split("₹")[1]);
    }

    public static boolean checkIfCustomCategory(String category) {
        return category.equals("Custom");
    }

}
