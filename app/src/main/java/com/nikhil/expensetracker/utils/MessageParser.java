package com.nikhil.expensetracker.utils;

import com.fasterxml.uuid.Generators;
import com.nikhil.expensetracker.model.Transaction;

public class MessageParser {

    public static Transaction parseMessage(String bank, String message, Long createdAt) {
        if (bank.toLowerCase().contains("axis")) {
            return handleAxisBankTransactionMessage(message, createdAt);
        } else if (bank.toLowerCase().contains("hdfc")) {
            return handleHdfcBankTransactionMessage(message, createdAt);
        } else if (bank.toLowerCase().contains("sbi")) {
            System.out.println("SBI TRANSACTION MESSAGE RECEIVED");
            return handleSbiTransactionMessage(message, createdAt);
        } else {
            return null;
        }
    }

    private static Transaction handleAxisBankTransactionMessage(String message, Long createdAt) {
        try {
            Transaction transaction = new Transaction();

            //Set bank name
            transaction.setBank("Axis Bank");

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

        } catch (Exception ignored) {
        }
        return null;
    }

    private static Transaction handleSbiTransactionMessage(String message, Long createdAt) {
        try {
            Transaction transaction = new Transaction();

            //Set bank name
            transaction.setBank("SBI");

            //Set transaction id
            transaction.setId(Generators.timeBasedGenerator().generate().toString());

            //Get transaction type
            if (message.contains("debited by")) {
                transaction.setType("DEBIT");
                //Get payee name
                String payeeNamePrefixSeparator = "transfer to ";
                String payeeNameSuffixSeparator = "Ref No ";
                int payeeNameSepPrefixPos = message.indexOf(payeeNamePrefixSeparator);
                int payeeNameSepSuffixPos = message.indexOf(payeeNameSuffixSeparator);
                transaction.setName(message.substring(payeeNameSepPrefixPos + 12, payeeNameSepSuffixPos).trim());

                //Get amount debited
                String debitedAmountSeparatorPrefix = "debited by";
                String debitedAmountSeparatorSuffix = "on ";
                int debitedAmountSepPrefixPos = message.indexOf(debitedAmountSeparatorPrefix);
                int debitedAmountSepSuffixPos = message.indexOf(debitedAmountSeparatorSuffix);
                Double amount = Double.parseDouble((message.substring(debitedAmountSepPrefixPos + 10, debitedAmountSepSuffixPos)).split("Rs")[1]);
                transaction.setAmount(amount);

            } else if (message.contains("credited by")) {
                transaction.setType("CREDIT");

                //Set name as general
                transaction.setName("Unknown");

                //Get amount credited
                String creditedAmountSeparatorPrefix = "credited by";
                String creditedAmountSeparatorSuffix = "on ";
                int creditedAmountSepPrefixPos = message.indexOf(creditedAmountSeparatorPrefix);
                int creditedAmountSepSuffixPos = message.indexOf(creditedAmountSeparatorSuffix);
                Double amount = Double.parseDouble((message.substring(creditedAmountSepPrefixPos + 10, creditedAmountSepSuffixPos)).split("Rs")[1]);
                transaction.setAmount(amount);
            }

            //Fill transaction date and time
            transaction.setCreatedAt(createdAt);

            System.out.println("SBI TRANSACTION:" + transaction);
            return transaction;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Transaction handleHdfcBankTransactionMessage(String message, Long createdAt) {
        try {
            Transaction transaction = new Transaction();

            //Set bank name
            transaction.setBank("HDFC Bank");

            //Set transaction id
            transaction.setId(Generators.timeBasedGenerator().generate().toString());

            if (message.contains("debited from")) {
                transaction.setType("DEBIT");

                //Get payee name
                String payeeNamePrefixSeparator = "VPA";
                String payeeNameSuffixSeparator = "@";
                int payeeNameSepPrefixPos = message.indexOf(payeeNamePrefixSeparator);
                int payeeNameSepSuffixPos = message.indexOf(payeeNameSuffixSeparator);
                transaction.setName(message.substring(payeeNameSepPrefixPos + 3, payeeNameSepSuffixPos).trim());

                //Get amount debited
                String amountDebitedPrefixSeparator = "Rs ";
                String amountDebitedSuffixSeparator = "debited";
                int amountDebitedSepPrefixPos = message.indexOf(amountDebitedPrefixSeparator);
                int amountDebitedSepSuffixPos = message.indexOf(amountDebitedSuffixSeparator);
                transaction.setAmount(Double.valueOf(message.substring(amountDebitedSepPrefixPos + 3, amountDebitedSepSuffixPos)));


            } else if (message.contains("credited to")) {
                transaction.setType("CREDIT");

                //Set payee name
                String payeeNamePrefixSeparator = "VPA";
                String payeeNameSuffixSeparator = "@";
                int payeeNameSepPrefixPos = message.indexOf(payeeNamePrefixSeparator);
                int payeeNameSepSuffixPos = message.indexOf(payeeNameSuffixSeparator);
                transaction.setName(message.substring(payeeNameSepPrefixPos + 3, payeeNameSepSuffixPos).trim());

                //Get amount credited
                String amountCreditedPrefixSeparator = "Rs. ";
                String amountCreditedSuffixSeparator = "credited";
                int amountCreditedSepPrefixPos = message.indexOf(amountCreditedPrefixSeparator);
                int amountCreditedSepSuffixPos = message.indexOf(amountCreditedSuffixSeparator);
                transaction.setAmount(Double.valueOf(message.substring(amountCreditedSepPrefixPos + 4, amountCreditedSepSuffixPos)));
            }

            //Fill transaction date and time
            transaction.setCreatedAt(createdAt);

            return transaction;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
