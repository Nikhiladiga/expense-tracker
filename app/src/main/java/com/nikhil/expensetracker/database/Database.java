package com.nikhil.expensetracker.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.nikhil.expensetracker.model.Transaction;

import java.util.ArrayList;
import java.util.List;

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
                    + COL6 + " TEXT,"
                    + COL7 + " TEXT,"
                    + COL8 + " REAL)";
            System.out.println("Creating table:" + createTableQuery);
            sqLiteDatabase.execSQL(createTableQuery);
        } else {
            System.out.println("ALL TRANSACTIONS:" + getTransactions());
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
        database.insert(TABLE_NAME, null, contentValues);
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
                    data.getDouble(7)
            ));
        }
        return mArrayList;
    }

    public void deleteTransaction(String id) {
        SQLiteDatabase database = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE id='" + id + "'";
        database.execSQL(query);
    }

//    public boolean updateTransactionById() {
//        SQLiteDatabase database = this.getWritableDatabase();
//        long result = database.delete(TABLE_NAME,"");
//    }
//
//    public boolean deleteTransactionById() {
//
//    }

}
