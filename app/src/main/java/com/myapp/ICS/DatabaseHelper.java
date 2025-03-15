package com.myapp.ICS;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ICS.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_ITEMS = "items";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_BARCODE = "barcode";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_QUANTITY = "quantity";

    private static final String COLUMN_CURRENCY = "currency";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ITEMS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_BARCODE + " TEXT NOT NULL UNIQUE, " +
                COLUMN_PRICE + " REAL NOT NULL, " +
                COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                COLUMN_CURRENCY + " TEXT NOT NULL);";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    public boolean addItem(String name, String barcode, double price, int quantity, String currency) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS + " WHERE " + COLUMN_BARCODE + " = ?", new String[]{barcode});

        ContentValues values = new ContentValues();
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") int currentQuantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
            values.put(COLUMN_QUANTITY, currentQuantity + quantity);
            int rowsUpdated = db.update(TABLE_ITEMS, values, COLUMN_BARCODE + " = ?", new String[]{barcode});
            cursor.close();
            db.close();
            return rowsUpdated > 0;
        } else {
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_BARCODE, barcode);
            values.put(COLUMN_PRICE, price);
            values.put(COLUMN_QUANTITY, quantity);
            values.put(COLUMN_CURRENCY, currency);

            long result = db.insert(TABLE_ITEMS, null, values);
            cursor.close();
            db.close();
            return result != -1;
        }
    }
    public boolean removeItem(String barcode, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_QUANTITY + " FROM " + TABLE_ITEMS + " WHERE " + COLUMN_BARCODE + " = ?", new String[]{barcode});

        if (cursor.moveToFirst()) {
            int currentQuantity = cursor.getInt(0);
            cursor.close();

            if (currentQuantity >= quantity) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_QUANTITY, currentQuantity - quantity);
                db.update(TABLE_ITEMS, values, COLUMN_BARCODE + " = ?", new String[]{barcode});
                db.close();
                return true;
            }
        }
        db.close();
        return false;
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);
    }
    public LinkedList<Item> getAllItemsList() {
        LinkedList<Item> items = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String code = cursor.getString(cursor.getColumnIndex(COLUMN_BARCODE));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                @SuppressLint("Range") double price = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE));
                @SuppressLint("Range") int quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
                @SuppressLint("Range") String currency = cursor.getString(cursor.getColumnIndex(COLUMN_CURRENCY));

                items.add(new Item(code, name, price, quantity, currency));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return items;
    }
}
