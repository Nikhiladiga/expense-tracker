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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class Util {

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
            String iciciStringPattern = "%ICICIB%";
            Cursor cur = MainActivity.getInstance().getContentResolver().query(uri, projection, "address LIKE ? OR address LIKE ? OR address LIKE? OR address LIKE?", new String[]{axisStringPattern, sbiStringPattern, hdfcStringPattern, iciciStringPattern}, "date desc");
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
                    } else if (address.contains("ICICIB") || address.contains("icici")) {
                        if (msgBody.contains("debited for") || msgBody.contains("credited with")) {
                            transaction = MessageParser.parseMessage("icici", msgBody, msgDate);
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
        } catch (Exception ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
        MainActivity.getInstance().refreshAdapterData();
    }

    public static ConcurrentHashMap<String, Long> getMonthStartAndMonthEndTimestamp(Integer currentYear, String currentMonth) {
        ConcurrentHashMap<String, Long> monthStartEndTs = new ConcurrentHashMap<>();
        Calendar calendar = Calendar.getInstance();
        Month month;
        if (currentMonth.equals("All")) {
            month = Month.valueOf("JANUARY");
        } else {
            month = Month.valueOf(currentMonth.toUpperCase(Locale.ROOT));
        }

        int monthIndex = month.getValue();
        int year;
        if (currentYear == null) {
            year = calendar.get(Calendar.YEAR);
        } else {
            year = currentYear;
        }
        int noOfDaysInMonth = month.length(DateUtils.checkIfLeapYear(year));
        int monthStartDay = Integer.parseInt(SharedPrefHelper.getMonthStartDay());

        LocalDate start = null;
        LocalDate end = null;

        //Check if startofMonthDate is more than number of days in current month
        if (monthStartDay > noOfDaysInMonth) {
            if (monthIndex == 11) { //Check if november
                //Start date
                start = LocalDate.of(year, monthIndex + 1, monthStartDay - noOfDaysInMonth);
                //End date
                end = LocalDate.of(year + 1, 1, monthStartDay - noOfDaysInMonth);
            } else if (monthIndex == 12) { //Check if december
                //Start date
                start = LocalDate.of(year + 1, 1, monthStartDay - noOfDaysInMonth);
                //End date
                end = LocalDate.of(year + 1, 2, monthStartDay - noOfDaysInMonth);
            } else { //Other months
                //Start date
                start = LocalDate.of(year, monthIndex + 1, monthStartDay - noOfDaysInMonth);
                //End date
                end = LocalDate.of(year, monthIndex + 2, monthStartDay - noOfDaysInMonth);
            }
        } else {

            if (currentMonth.equals("All")) {
                start = LocalDate.of(year, 1, monthStartDay);
                end = LocalDate.of(year + 1, 1, monthStartDay);
            } else if (monthIndex == 12 || monthIndex == 11) {
                start = LocalDate.of(year + 1, monthIndex, monthStartDay);
                end = LocalDate.of(year + 1, 1, monthStartDay);
            } else {
                start = LocalDate.of(year, monthIndex, monthStartDay);
                if (monthIndex == 1 && (Month.of(monthIndex + 2).length(DateUtils.checkIfLeapYear(year)) < monthStartDay)) { //January
                    if (DateUtils.checkIfLeapYear(year)) {
                        end = LocalDate.of(year, monthIndex + 2, monthStartDay - 29);
                    } else {
                        end = LocalDate.of(year, monthIndex + 2, monthStartDay - 28);
                    }
                } else if (Month.of(monthIndex + 1).length(DateUtils.checkIfLeapYear(year)) < monthStartDay) {
                    end = LocalDate.of(year, monthIndex + 2, monthStartDay - Month.of(monthIndex + 1).length(DateUtils.checkIfLeapYear(year)));
                } else {
                    end = LocalDate.of(year, monthIndex + 1, monthStartDay);
                }
            }
        }

        monthStartEndTs.put("start", start.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000);
        monthStartEndTs.put("end", end.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000);

        return monthStartEndTs;
    }

    public static void saveCategory(String category) {
        if (!SharedPrefHelper.getCategories().contains(category)) {
            SharedPrefHelper.addCategory(category);
        }
    }

}
