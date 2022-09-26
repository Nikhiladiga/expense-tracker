package com.nikhil.expensetracker.utils;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.uuid.Generators;
import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.receiver.SmsReceiver;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Util {
//    public static SmsReceiver smsReceiver;

    public static Transaction parseSMS(String message, Long createdAt) {
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

    @SuppressLint("SimpleDateFormat")
    public static void readAllSms(String currentMonth) {
        Long latestTransactionDate = MainActivity.getInstance().database.getLatestTransactionDate();
        StringBuilder smsBuilder = new StringBuilder();
        final String SMS_URI_INBOX = "content://sms/inbox";
        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
            String axisStringPattern = "%Axis%";
            String sbiStringPattern = "%SBIUPI%";
            String hdfcStringPattern = "%HDFCBK%";
            Cursor cur = MainActivity.getInstance().getContentResolver().query(uri, projection, "address LIKE ? OR address LIKE ? OR address LIKE?", new String[]{axisStringPattern, sbiStringPattern, hdfcStringPattern}, "date desc");
//            Cursor cur = MainActivity.getInstance().getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
            if (cur.moveToFirst()) {
                int index_Body = cur.getColumnIndex("body");
                int dateIndex = cur.getColumnIndex("date");
                int addressIndex = cur.getColumnIndex("address");
                do {
                    String msgBody = cur.getString(index_Body);
                    long msgDate = cur.getLong(dateIndex);
                    String address = cur.getString(addressIndex);

                    Transaction transaction = null;
                    if (address.contains("Axis")) {
                        if ((msgBody.contains("Debit") || msgBody.contains("credited to"))) {
                            transaction = MessageParser.parseMessage("axis", msgBody, msgDate);
                        }
                    } else if (address.contains("SBIUPI")) {
                        if ((msgBody.contains("debited by") || msgBody.contains("credited by"))) {
                            transaction = MessageParser.parseMessage("sbi", msgBody, msgDate);
                        }
                    } else if (address.contains("HDFCBK")) {
                        if (msgBody.contains("debited from") || msgBody.contains("credited to")) {
                            transaction = MessageParser.parseMessage("hdfc", msgBody, msgDate);
                        }
                    }

                    if (transaction != null && transaction.getCreatedAt() != null
                            && transaction.getCreatedAt() > latestTransactionDate
                            && new SimpleDateFormat("MMMM").format(new Date(transaction.getCreatedAt())).equalsIgnoreCase(currentMonth)
                    ) {
                        MainActivity.getInstance().database.addTransaction(transaction);
                    }
                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            } else {
                smsBuilder.append("no result!");
            } // end if
        } catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
        MainActivity.getInstance().refreshAdapterData();
    }

    public static Timestamp convertStringToTimestamp(String strDate) {
        try {
            @SuppressLint("SimpleDateFormat")
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            // you can change format of date
            Date date = formatter.parse(strDate);
            return new Timestamp(Objects.requireNonNull(date).getTime());
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String convertTimestampToDate(Long timestamp) {
        return new SimpleDateFormat("dd-MM-yyyy").format(new Date(timestamp));
    }
}
