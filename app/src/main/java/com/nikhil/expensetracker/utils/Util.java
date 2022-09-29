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
import com.nikhil.expensetracker.datahelpers.SharedPrefHelper;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.receiver.SmsReceiver;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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

    public static Boolean fallsUnderCurrentMonth(Long createdAt, String currentMonth) {
        try {
            Calendar calendar = Calendar.getInstance();
            Month month = Month.valueOf(currentMonth.toUpperCase(Locale.ROOT));
            int monthIndex = month.getValue();
            int currentYear = calendar.get(Calendar.YEAR);
            int noOfDaysInMonth = month.length(checkIfLeapYear(currentYear));
            int monthStartDay = Integer.parseInt(SharedPrefHelper.getMonthStartDay());

            LocalDate start = null;
            LocalDate end = null;

            //Check if startofMonthDate is more than number of days in current month
            if (monthStartDay > noOfDaysInMonth) {

                if (monthIndex == 11) { //Check if november
                    //Start date
                    start = LocalDate.of(currentYear, monthIndex + 1, monthStartDay - noOfDaysInMonth);
                    //End date
                    end = LocalDate.of(currentYear + 1, 1, monthStartDay - noOfDaysInMonth);
                } else if (monthIndex == 12) { //Check if december
                    //Start date
                    start = LocalDate.of(currentYear + 1, 1, monthStartDay - noOfDaysInMonth);
                    //End date
                    end = LocalDate.of(currentYear + 1, 2, monthStartDay - noOfDaysInMonth);
                } else { //Other months
                    //Start date
                    start = LocalDate.of(currentYear, monthIndex + 1, monthStartDay - noOfDaysInMonth);
                    //End date
                    end = LocalDate.of(currentYear, monthIndex + 2, monthStartDay - noOfDaysInMonth);
                }
            } else {
                if (monthIndex == 12 || monthIndex == 11) {
                    start = LocalDate.of(currentYear + 1, monthIndex, monthStartDay);
                    end = LocalDate.of(currentYear + 1, 1, monthStartDay);
                } else {
                    start = LocalDate.of(currentYear, monthIndex, monthStartDay);
                    if (monthIndex == 1 && (Month.of(monthIndex + 2).length(checkIfLeapYear(currentYear)) < monthStartDay)) { //January
                        if (checkIfLeapYear(currentYear)) {
                            end = LocalDate.of(currentYear, monthIndex + 2, monthStartDay - 29);
                        } else {
                            end = LocalDate.of(currentYear, monthIndex + 2, monthStartDay - 28);
                        }
                    } else if (Month.of(monthIndex + 1).length(checkIfLeapYear(currentYear)) < monthStartDay) {
                        end = LocalDate.of(currentYear, monthIndex + 2, monthStartDay - Month.of(monthIndex + 1).length(checkIfLeapYear(currentYear)));
                    } else {
                        end = LocalDate.of(currentYear, monthIndex + 1, monthStartDay);
                    }
                }
            }

//            System.out.println("#############################################################################");
//            System.out.println();
//            System.out.println("START DATE:" + start);
//            System.out.println("END DATE:" + end);
//            System.out.println();
//            System.out.println("#############################################################################");

            String date = Util.convertTimestampToDate(createdAt);
            LocalDate today = LocalDate.of(
                    Integer.parseInt(date.split("-")[2]),
                    Integer.parseInt(date.split("-")[1]),
                    Integer.parseInt(date.split("-")[0])
            );
            return (!today.isBefore(start)) && (today.isBefore(end));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkIfLeapYear(int year) {
        return (year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0));
    }

    private static boolean isEmoji(String message){
        return message.matches("(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|" +
                "[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|" +
                "[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|" +
                "[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|" +
                "[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|" +
                "[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|" +
                "[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|" +
                "[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|" +
                "[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|" +
                "[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|" +
                "[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)+");
    }

    public static int detectEmojis(String text) {
        int len = text.length(), NumEmoji = 0;
        // if the the given String is only emojis.
        if (isEmoji(text)) {
            for (int i = 0; i < len; i++) {
                // if the charAt(i) is an emoji by it self -> ++NumEmoji
                if (isEmoji(text.charAt(i) + "")) {
                    NumEmoji++;
                } else {
                    // maybe the emoji is of size 2 - so lets check.
                    if (i < (len - 1)) { // some Emojis are two characters long in java, e.g. a rocket emoji is "\uD83D\uDE80";
                        if (Character.isSurrogatePair(text.charAt(i), text.charAt(i + 1))) {
                            i += 1; //also skip the second character of the emoji
                            NumEmoji++;
                        }
                    }
                }
            }
            return NumEmoji;
        }
        return 0;
    }
}
