package com.nikhil.expensetracker.datahelpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikhil.expensetracker.MainActivity;

import java.util.List;

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

    public static String getMonthStartDay() {
        return sharedPreferences.getString("monthStartDay", "1");
    }

    public static void setMonthStartDay(String monthStartDay) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("monthStartDay", monthStartDay);
        editor.apply();
    }

    public static void addCategory(String category) {
        String oldJsonString = getCategories();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            List<String> categories = objectMapper.readValue(oldJsonString, new TypeReference<List<String>>() {
            });
            categories.add(category);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String newJsonString = objectMapper.writeValueAsString(categories);
            editor.putString("categories", newJsonString);
            editor.apply();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
