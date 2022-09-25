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

                //Get transaction amount
                String transactionSeparator = "INR ";
                int transSepPos = message.indexOf(transactionSeparator);
                transaction.setAmount(Double.parseDouble(message.substring(transSepPos + transactionSeparator.length()).split("\n")[0]));

                //Fill transaction date and time
                transaction.setCreatedAt(createdAt);

                //Get payee name
                String payeeString = message.split("\n")[4];
                String payeeName;
                try {
                    payeeName = payeeString.split("/")[3];
                } catch (ArrayIndexOutOfBoundsException e) {
                    payeeName = payeeString.split("/")[2];
                }
                transaction.setName(payeeName);

                //Get remaining balance in account after transaction
                String balanceSeparator = "Bal INR ";
                int balSepPor = message.indexOf(balanceSeparator);
                transaction.setBalance(Double.parseDouble(message.substring(balSepPor + balanceSeparator.length()).split("\n")[0]));

            } else if (message.contains("credited to")) {
                transaction.setType("CREDIT");

                //Get transaction amount
                String transactionAmtPrefixSeparator = "INR";
                String transactionAmtSuffixSeparator = "credited";
                int transactionAmtSepPrefixPos = message.indexOf(transactionAmtPrefixSeparator);
                int transactionAmtSepSuffixPos = message.indexOf(transactionAmtSuffixSeparator);
                transaction.setAmount(Double.valueOf(message.substring(transactionAmtSepPrefixPos + 3, transactionAmtSepSuffixPos).trim()));

                //Fill transaction date and time
                transaction.setCreatedAt(createdAt);

                //Get payee name
                String payeeNamePrefixSeparator = "Info-";
                int payeeNameSepPrefixPos = message.indexOf(payeeNamePrefixSeparator);
                transaction.setName(message.substring(payeeNameSepPrefixPos + 5).split("/")[0].trim());

                //Check if company and set category (Only for me)
                if (message.contains("ACCESS RESEARCH")) {
                    transaction.setCategory("Salary");
                }
            } else {
                transaction.setType("UNKNOWN");
            }

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
