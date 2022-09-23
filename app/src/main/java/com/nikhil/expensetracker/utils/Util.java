package com.nikhil.expensetracker.utils;

import android.util.Log;

import com.fasterxml.uuid.Generators;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.receiver.SmsReceiver;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Util {
    public static SmsReceiver smsReceiver;

    public static Transaction parseSMS(String message) {
        try {

            Transaction transaction = new Transaction();

            //Set transaction id
            transaction.setId(Generators.timeBasedGenerator().generate().toString());

            //Get transaction type
            if (message.contains("Debit")) {
                transaction.setType("DEBIT");
            } else if (message.contains("Credit")) {
                transaction.setType("CREDIT");
            } else {
                transaction.setType("UNKNOWN");
            }

            //Get transaction amount
            String transactionSeparator = "INR ";
            int transSepPos = message.indexOf(transactionSeparator);
            transaction.setAmount(Double.parseDouble(message.substring(transSepPos + transactionSeparator.length()).split("\n")[0]));

            //Fill transaction date and time
            LocalDate today = LocalDate.now();
            String formattedDate = today.format(DateTimeFormatter.ofPattern("dd-MMM-yy"));
            transaction.setCreatedAt(formattedDate);

            //Get payee name
            String payeeString = message.split("\n")[4];
            String payeeName = payeeString.split("/")[3];
            transaction.setName(payeeName);

            //Get remaining balance in account after transaction
            String balanceSeparator = "Bal INR ";
            int balSepPor = message.indexOf(balanceSeparator);
            transaction.setBalance(Double.parseDouble(message.substring(balSepPor + balanceSeparator.length()).split("\n")[0]));

            System.out.println("EXPENSE TRACKER" + transaction);

            return transaction;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTransactionCategoryEmoji(String category) {
        if (category != null) {
            switch (category) {
                case "Food":
                    return "🍔";
                case "Entertainment":
                    return "😆";
                case "Investment":
                    return "📈";
                case "Sports":
                    return "🏋";
                case "Fuel":
                    return "⛽";
                case "General":
                    return "💁";
                case "Holidays":
                    return "😛";
                case "Travel":
                    return "🚍";
                case "Gifts":
                    return "🎁";
                case "Shopping":
                    return "🛒";
                case "Clothes":
                    return "👕";
                case "Movies":
                    return "🎬";
                case "Salary":
                    return "💸";
                default:
                    return "⚙";
            }
        } else {
            return "⚙";
        }
    }
}
