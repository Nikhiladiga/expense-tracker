package com.nikhil.expensetracker.utils;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import com.fasterxml.uuid.Generators;
import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.datahelpers.SharedPrefHelper;
import com.nikhil.expensetracker.model.Transaction;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

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
            if (cur.moveToFirst()) {
                int index_Body = cur.getColumnIndex("body");
                int dateIndex = cur.getColumnIndex("date");
                int addressIndex = cur.getColumnIndex("address");
                do {
                    String msgBody = cur.getString(index_Body);
                    long msgDate = cur.getLong(dateIndex);
                    String address = cur.getString(addressIndex);

                    Transaction transaction = null;
                    if (address.contains("Axis") || address.contains("AXIS")) {
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

//                    if (transaction != null) {
//                        System.out.println("-------------------------------------------");
//                        System.out.println("PAYEE NAME:" + transaction.getName());
//                        System.out.println("TRANSACTION CREATED AT:" + transaction.getCreatedAt());
//                        System.out.println("LATEST TRANSACTION DATE:" + latestTransactionDate);
//                        System.out.println("TRANSACTION DATE EQUALS CURRENT MONTH:" + new SimpleDateFormat("MMMM").format(new Date(transaction.getCreatedAt())).equalsIgnoreCase(currentMonth));
//                        System.out.println("-------------------------------------------");
//                    }

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
        } catch (Exception ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
        MainActivity.getInstance().refreshAdapterData();
    }

    public static Boolean fallsUnderCurrentMonth(Long createdAt, String currentMonth) {
        try {
            Calendar calendar = Calendar.getInstance();
            Month month = Month.valueOf(currentMonth.toUpperCase(Locale.ROOT));
            int monthIndex = month.getValue();
            int currentYear = calendar.get(Calendar.YEAR);
            int noOfDaysInMonth = month.length(DateUtils.checkIfLeapYear(currentYear));
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
                    if (monthIndex == 1 && (Month.of(monthIndex + 2).length(DateUtils.checkIfLeapYear(currentYear)) < monthStartDay)) { //January
                        if (DateUtils.checkIfLeapYear(currentYear)) {
                            end = LocalDate.of(currentYear, monthIndex + 2, monthStartDay - 29);
                        } else {
                            end = LocalDate.of(currentYear, monthIndex + 2, monthStartDay - 28);
                        }
                    } else if (Month.of(monthIndex + 1).length(DateUtils.checkIfLeapYear(currentYear)) < monthStartDay) {
                        end = LocalDate.of(currentYear, monthIndex + 2, monthStartDay - Month.of(monthIndex + 1).length(DateUtils.checkIfLeapYear(currentYear)));
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

            String date = DateUtils.convertTimestampToDate(createdAt);
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

    public static ConcurrentHashMap<String, Long> getMonthStartAndMonthEndTimestamp(String currentMonth) {
        Calendar calendar = Calendar.getInstance();
        Month month = Month.valueOf(currentMonth.toUpperCase(Locale.ROOT));
        int monthIndex = month.getValue();
        int currentYear = calendar.get(Calendar.YEAR);
        int noOfDaysInMonth = month.length(DateUtils.checkIfLeapYear(currentYear));
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
                if (monthIndex == 1 && (Month.of(monthIndex + 2).length(DateUtils.checkIfLeapYear(currentYear)) < monthStartDay)) { //January
                    if (DateUtils.checkIfLeapYear(currentYear)) {
                        end = LocalDate.of(currentYear, monthIndex + 2, monthStartDay - 29);
                    } else {
                        end = LocalDate.of(currentYear, monthIndex + 2, monthStartDay - 28);
                    }
                } else if (Month.of(monthIndex + 1).length(DateUtils.checkIfLeapYear(currentYear)) < monthStartDay) {
                    end = LocalDate.of(currentYear, monthIndex + 2, monthStartDay - Month.of(monthIndex + 1).length(DateUtils.checkIfLeapYear(currentYear)));
                } else {
                    end = LocalDate.of(currentYear, monthIndex + 1, monthStartDay);
                }
            }
        }

        ConcurrentHashMap<String, Long> monthStartEndTs = new ConcurrentHashMap<>();
        monthStartEndTs.put("start", start.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000);
        monthStartEndTs.put("end", end.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000);

        return monthStartEndTs;
    }
}
