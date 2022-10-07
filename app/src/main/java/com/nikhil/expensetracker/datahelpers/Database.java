package com.nikhil.expensetracker.datahelpers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.model.DashboardData;
import com.nikhil.expensetracker.model.ReportData;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.Util;

import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Database extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "transaction_table";
    private static final String COL1 = "id";
    private static final String COL2 = "type";
    private static final String COL3 = "name";
    private static final String COL4 = "amount";
    private static final String COL5 = "category";
    private static final String COL6 = "createdAt";
    private static final String COL7 = "updatedAt";
    private static final String COL8 = "balance";
    private static final String COL9 = "bank";
    private static final String COL10 = "emoji";

    private boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        boolean res = false;
        try {
            checkDB = SQLiteDatabase.openDatabase("expense.db", null, SQLiteDatabase.OPEN_READONLY);
            res = (checkDB.getVersion() > 0);
            checkDB.close();
        } catch (Exception e) {
            System.out.println("Database does not exist, creating new....");
            res = false;
        }
        return res;
    }

    public Database(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        if (!checkDatabase()) {
            String createTableQuery = "CREATE TABLE " + TABLE_NAME + " ("
                    + COL1 + " TEXT PRIMARY KEY ,"
                    + COL2 + " TEXT,"
                    + COL3 + " TEXT,"
                    + COL4 + " REAL,"
                    + COL5 + " TEXT,"
                    + COL6 + " REAL,"
                    + COL7 + " REAL,"
                    + COL8 + " REAL,"
                    + COL9 + " TEXT,"
                    + COL10 + " TEXT)";
            System.out.println("Creating table:" + createTableQuery);
            sqLiteDatabase.execSQL(createTableQuery);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String dropTableQuery = "DROP TABLE " + TABLE_NAME;
        sqLiteDatabase.execSQL(dropTableQuery);
        onCreate(sqLiteDatabase);
    }

    public void addTransaction(Transaction transaction) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, transaction.getId());
        contentValues.put(COL2, transaction.getType());
        contentValues.put(COL3, transaction.getName());
        contentValues.put(COL4, transaction.getAmount());
        contentValues.put(COL5, transaction.getCategory());
        contentValues.put(COL6, transaction.getCreatedAt());
        contentValues.put(COL7, transaction.getUpdatedAt());
        contentValues.put(COL8, transaction.getBalance());
        contentValues.put(COL9, transaction.getBank());

        if (transaction.getEmoji() == null || transaction.getEmoji().isEmpty()) {
            contentValues.put(COL10, "💵");
        } else {
            contentValues.put(COL10, transaction.getEmoji());
        }

        Log.i("Expense Tracker", "Added new transaction " + transaction);

        database.insert(TABLE_NAME, null, contentValues);
        database.close();
    }

    public List<Transaction> getTransactions() {
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        @SuppressLint("Recycle") Cursor data = database.rawQuery(query, null);
        ArrayList<Transaction> mArrayList = new ArrayList<>();
        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            mArrayList.add(new Transaction(
                    data.getString(0),
                    data.getString(1),
                    data.getString(2),
                    data.getDouble(3),
                    data.getString(4),
                    data.getLong(5),
                    data.getLong(6),
                    data.getDouble(7),
                    data.getString(8),
                    data.getString(9)
            ));
        }
        database.close();
        return mArrayList;
    }

    public void deleteTransaction(String id) {
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE id='" + id + "'";
        database.execSQL(query);
        database.close();
    }

    public void updateTransactionById(Transaction transaction) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME
                + " SET " + COL2 + "='" + transaction.getType() + "', "
                + COL3 + "='" + transaction.getName() + "', "
                + COL4 + "=" + transaction.getAmount() + ", "
                + COL5 + "='" + transaction.getCategory() + "', "
                + COL6 + "='" + transaction.getCreatedAt() + "', "
                + COL7 + "='" + transaction.getUpdatedAt() + "', "
                + COL8 + "=" + transaction.getBalance() + ", "
                + COL9 + "='" + transaction.getBank() + "', "
                + COL10 + "='" + transaction.getEmoji() + "'"
                + " WHERE id='" + transaction.getId() + "'";

        Log.i("Expense Tracker", "Updated transaction " + transaction);

        System.out.println("QUERY:" + query);
        sqLiteDatabase.execSQL(query);
        sqLiteDatabase.close();
    }

    @SuppressLint("SimpleDateFormat")
    public DashboardData getTransactionsByMonth(String month) {
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY createdAt DESC";
        @SuppressLint("Recycle") Cursor data = database.rawQuery(query, null);
        ArrayList<Transaction> mArrayList = new ArrayList<>();
        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            if (Util.fallsUnderCurrentMonth(data.getLong(5), month)) {
                mArrayList.add(new Transaction(
                        data.getString(0),
                        data.getString(1),
                        data.getString(2),
                        data.getDouble(3),
                        data.getString(4),
                        data.getLong(5),
                        data.getLong(6),
                        data.getDouble(7),
                        data.getString(8),
                        data.getString(9)
                ));
            }
        }

        //Get balance left in current month
        Double balance = (double) 0;
        for (Transaction transaction : mArrayList) {
            if (transaction.getType().equalsIgnoreCase("CREDIT")) {
                balance += transaction.getAmount();
            } else {
                balance -= transaction.getAmount();
            }
        }

        //Get amount spent in current month
        Double expense = (double) 0;
        Optional<Double> optionalDouble = mArrayList
                .stream()
                .filter(transaction -> transaction.getType().equalsIgnoreCase("DEBIT"))
                .map(Transaction::getAmount)
                .reduce(Double::sum);

        if (optionalDouble.isPresent()) {
            expense = optionalDouble.get();
        }

        DashboardData dashboardData = new DashboardData(
                mArrayList,
                balance,
                expense
        );
        database.close();
        return dashboardData;
    }

    public void deleteAllTransactions() {
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME;
        System.out.println(query);
        database.execSQL(query);
        database.close();
    }

    public Long getLatestTransactionDate() {
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "SELECT createdAt FROM " + TABLE_NAME + " ORDER BY createdAt DESC LIMIT 1";
        System.out.println(query);
        @SuppressLint("Recycle")
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();

        Long latestDate = null;
        try {
            latestDate = cursor.getLong(0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("LATEST TRANSACTION DATE:" + latestDate);
        database.close();
        if (latestDate != null) {
            return latestDate;
        } else {
            return 0L;
        }

    }

    public Double getTotalBalance() {
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "SELECT SUM(CASE WHEN type='CREDIT' THEN amount WHEN type='DEBIT' THEN -amount END) AS totalAmount FROM " + TABLE_NAME;
        System.out.println(query);
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        database.close();
        return cursor.getDouble(0);
    }

    public List<ReportData> getTransactionAmountSumByCategory(String month) {
        List<ReportData> reportDataList = new ArrayList<>();
        ConcurrentHashMap<String, Long> monthStartEndTs = Util.getMonthStartAndMonthEndTimestamp(month);
        SQLiteDatabase database = this.getReadableDatabase();
        String query = "SELECT category,createdAt,SUM(amount) AS total FROM " + TABLE_NAME + " WHERE type='DEBIT' AND createdAt >" + monthStartEndTs.get("start") + " AND createdAt <" + monthStartEndTs.get("end") + " GROUP BY category ORDER BY total DESC,createdAt DESC LIMIT 5";
        System.out.println("QUERY:" + query);
        @SuppressLint("Recycle") Cursor data = database.rawQuery(query, null);
        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            reportDataList.add(new ReportData(data.getString(0), data.getDouble(2)));
        }
        database.close();
        return reportDataList;
    }

    public void deleteMultipleTransactions(List<String> ids) {
    }

    public Double getTotalExpenseByMonth(String month) {
        SQLiteDatabase database = this.getReadableDatabase();
        ConcurrentHashMap<String, Long> monthStartEndTs = Util.getMonthStartAndMonthEndTimestamp(month);

        String query = "SELECT SUM(amount) FROM " + TABLE_NAME + " WHERE createdAt>" + monthStartEndTs.get("start") + " AND createdAt<" + monthStartEndTs.get("end") + " AND type='DEBIT'";
        @SuppressLint("Recycle") Cursor data = database.rawQuery(query, null);
        data.moveToFirst();
        database.close();
        return data.getDouble(0);
    }
}
