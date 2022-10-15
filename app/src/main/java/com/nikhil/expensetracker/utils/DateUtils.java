package com.nikhil.expensetracker.utils;

import android.annotation.SuppressLint;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DateUtils {

    public static boolean checkIfLeapYear(int year) {
        return (year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0));
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
        if (timestamp != null) {
            return new SimpleDateFormat("dd-MM-yyyy").format(new Date(timestamp));
        } else {
            return "";
        }
    }

    public static String getDay(String date) {
        return LocalDate.parse(
                date,
                DateTimeFormatter.ofPattern("dd-MM-uuuu")
        ).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("MMMM").format(calendar.getTime());
    }

}
