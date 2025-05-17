package com.myapp.ICS;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.LinkedList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ICS.db";
    private static final int DATABASE_VERSION = 3;

    // Константы таблицы
    public static final String TABLE_ITEMS = "items";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_BARCODE = "barcode";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_CURRENCY = "currency";
    private static final String COLUMN_TOTAL = "total";

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
                COLUMN_CURRENCY + " TEXT NOT NULL, " +
                COLUMN_TOTAL + " REAL NOT NULL);";
        db.execSQL(createTable);
    }

    @SuppressLint("Range")
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_TOTAL + " REAL DEFAULT 0");

            ContentValues values = new ContentValues();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);

            while (cursor.moveToNext()) {
                @SuppressLint("Range") double price = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE));
                @SuppressLint("Range") int quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
                values.put(COLUMN_TOTAL, price * quantity);

                db.update(TABLE_ITEMS,
                        values,
                        COLUMN_ID + " = ?",
                        new String[]{cursor.getString(cursor.getColumnIndex(COLUMN_ID))}
                );
            }
            cursor.close();
        }
    }

    public boolean addItem(String name, String barcode, double price,
                           int quantity, String currency, double total) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, name);
        values.put(COLUMN_BARCODE, barcode);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_QUANTITY, quantity);
        values.put(COLUMN_CURRENCY, currency);
        values.put(COLUMN_TOTAL, total);

        long result = db.insertWithOnConflict(TABLE_ITEMS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
        return result != -1;
    }

    public boolean updateItem(String oldBarcode, String newBarcode, String newName, double newPrice,
                              int newQuantity, String newCurrency, double newTotal) {
        if (!oldBarcode.equals(newBarcode)) {
            // Проверка существования нового кода
            if (isBarcodeExists(newBarcode)) {
                return false;
            }
        }

            SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_BARCODE, newBarcode);
        values.put(COLUMN_NAME, newName);
        values.put(COLUMN_PRICE, newPrice);
        values.put(COLUMN_QUANTITY, newQuantity);
        values.put(COLUMN_CURRENCY, newCurrency);
        values.put(COLUMN_TOTAL, newTotal);

        try {
            int result = db.update(TABLE_ITEMS, values,
                    COLUMN_BARCODE + " = ?", new String[]{oldBarcode});
            return result > 0;
        } finally {
            db.close();
        }

    }

    public boolean isBarcodeExists(String barcode) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_ITEMS + " WHERE " + COLUMN_BARCODE + " = ?",
                new String[]{barcode}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean deleteAllItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int result = db.delete(TABLE_ITEMS, null, null);
            return result > 0;
        } finally {
            db.close();
        }
    }

    public LinkedList<Item> getAllItemsList() {
        LinkedList<Item> items = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String code = cursor.getString(cursor.getColumnIndex(COLUMN_BARCODE));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                    @SuppressLint("Range") double price = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE));
                    @SuppressLint("Range") int quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
                    @SuppressLint("Range") String currency = cursor.getString(cursor.getColumnIndex(COLUMN_CURRENCY));
                    @SuppressLint("Range") double total = cursor.getDouble(cursor.getColumnIndex(COLUMN_TOTAL));

                    items.add(new Item(code, name, price, quantity, currency, total));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return items;
    }

    public boolean removeItem(String barcode, int quantityToRemove) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_QUANTITY + ", " + COLUMN_PRICE +
                        " FROM " + TABLE_ITEMS + " WHERE " + COLUMN_BARCODE + " = ?",
                new String[]{barcode});

        try {
            if (cursor.moveToFirst()) {
                int currentQuantity = cursor.getInt(0);
                double price = cursor.getDouble(1);

                if (currentQuantity >= quantityToRemove) {
                    int newQuantity = currentQuantity - quantityToRemove;
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_QUANTITY, newQuantity);
                    values.put(COLUMN_TOTAL, price * newQuantity);

                    int rowsUpdated = db.update(TABLE_ITEMS,
                            values,
                            COLUMN_BARCODE + " = ?",
                            new String[]{barcode});
                    return rowsUpdated > 0;
                }
            }
            return false;
        } finally {
            cursor.close();
            db.close();
        }
    }

    public Item getItemByBarcode(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS +
                            " WHERE " + COLUMN_BARCODE + " = ?",
                    new String[]{barcode});

            if (cursor.moveToFirst()) {
                @SuppressLint("Range") String code = cursor.getString(cursor.getColumnIndex(COLUMN_BARCODE));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                @SuppressLint("Range") double price = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE));
                @SuppressLint("Range") int quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
                @SuppressLint("Range") String currency = cursor.getString(cursor.getColumnIndex(COLUMN_CURRENCY));
                @SuppressLint("Range") double total = cursor.getDouble(cursor.getColumnIndex(COLUMN_TOTAL));

                return new Item(code, name, price, quantity, currency, total);
            }
            return null;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    public boolean deleteItem(String barcode) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int result = db.delete(TABLE_ITEMS, COLUMN_BARCODE + " = ?", new String[]{barcode});
            return result > 0;
        } finally {
            db.close();
        }
    }
    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }
}