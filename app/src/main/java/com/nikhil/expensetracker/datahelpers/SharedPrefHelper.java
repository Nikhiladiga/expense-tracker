package com.nikhil.expensetracker.datahelpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nikhil.expensetracker.MainActivity;

public class SharedPrefHelper {

    private static SharedPreferences sharedPreferences;

    public static void initSharedPrefHelper(SharedPreferences sharedPreferences) {
        SharedPrefHelper.sharedPreferences = sharedPreferences;
    }

    public static String getUsername() {
        return sharedPreferences.getString("username", null);
    }

    public static void setUsername(String username, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        System.out.println("SHAREDPREF:" + sp);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sp.edit();
        editor.putString("username", username);
        editor.apply();
    }

    public static String getCategories() {
        return sharedPreferences.getString("categories", null);
    }

    public static void setCategories(String categories) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("categories", categories);
        editor.apply();
    }

    public static String getBalanceLimit() {
        return sharedPreferences.getString("balanceLimit", null);
    }

    public static void setBalanceLimit(String balanceLimit) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("balanceLimit", balanceLimit);
        editor.apply();
    }

    public static String getExpenseLimit() {
        return sharedPreferences.getString("expenseLimit", null);
    }

    public static void setExpenseLimit(String expenseLimit) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("expenseLimit", expenseLimit);
        editor.apply();
    }
}
