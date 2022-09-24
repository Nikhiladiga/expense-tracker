package com.nikhil.expensetracker.utils;

import com.fasterxml.uuid.Generators;
import com.nikhil.expensetracker.model.Transaction;

public class MessageParser {

    public static Transaction parseMessage(String bank, String message, Long createdAt) {
        if (bank.toLowerCase().contains("axis")) {
            return handleAxisBankTransactionMessage(message, createdAt);
        } else if (bank.toLowerCase().contains("hdfc")) {
            return null;
        } else if (bank.toLowerCase().contains("sbi")) {
            return null;
        } else {
            return null;
        }
    }

    private static Transaction handleAxisBankTransactionMessage(String message, Long createdAt) {
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
            transaction.setCreatedAt(createdAt);

            //Get payee name
            String payeeString = message.split("\n")[4];
            String payeeName = payeeString.split("/")[3];
            transaction.setName(payeeName);

            //Get remaining balance in account after transaction
            String balanceSeparator = "Bal INR ";
            int balSepPor = message.indexOf(balanceSeparator);
            transaction.setBalance(Double.parseDouble(message.substring(balSepPor + balanceSeparator.length()).split("\n")[0]));

            return transaction;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Transaction handleSBITransactionMessage(String message, Long createdAt) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Transaction handleHdfcBankTransactionMessage(String message, Long createdAt) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
