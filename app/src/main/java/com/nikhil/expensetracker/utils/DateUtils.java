package com.nikhil.expensetracker.utils;

import android.annotation.SuppressLint;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        return new SimpleDateFormat("dd-MM-yyyy").format(new Date(timestamp));
    }

}
