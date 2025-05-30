package com.myapp.ICS;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.LinkedList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ICS.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_ITEMS = "items";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_BARCODE = "barcode";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_CURRENCY = "currency";
    private static final String COLUMN_TOTAL = "total";
    private static final String COLUMN_UNIT = "unit";

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
                COLUMN_TOTAL + " REAL NOT NULL)" +
                COLUMN_UNIT + " TEXT NOT NULL);";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_TOTAL + " REAL DEFAULT 0");

            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);
            int idxId = cursor.getColumnIndex(COLUMN_ID);
            int idxPrice = cursor.getColumnIndex(COLUMN_PRICE);
            int idxQuantity = cursor.getColumnIndex(COLUMN_QUANTITY);

            if (idxId == -1 || idxPrice == -1 || idxQuantity == -1) {
                Log.e("DBUpgrade", "One or more columns not found in table!");
                cursor.close();
                return;
            }

            while (cursor.moveToNext()) {
                double price = cursor.getDouble(idxPrice);
                int quantity = cursor.getInt(idxQuantity);
                double total = price * quantity;

                ContentValues values = new ContentValues();
                values.put(COLUMN_TOTAL, total);

                db.update(
                        TABLE_ITEMS,
                        values,
                        COLUMN_ID + " = ?",
                        new String[]{cursor.getString(idxId)}
                );
            }
            cursor.close();
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_UNIT + " TEXT DEFAULT 'штук'");
        }
    }

    public boolean addItem(String name, String barcode, double price, int quantity, String currency, double total, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_QUANTITY + ", " + COLUMN_TOTAL + ", " + COLUMN_CURRENCY + ", " + COLUMN_UNIT +
                " FROM " + TABLE_ITEMS + " WHERE " + COLUMN_BARCODE + " = ?", new String[]{barcode});
        boolean updated = false;

        try {
            if (cursor.moveToFirst()) {
                String existingCurrency = cursor.getString(2);
                String existingUnit = cursor.getString(3);
                if (!existingCurrency.equals(currency) || !existingUnit.equals(unit)) {
                    return false;
                }
                int oldQuantity = cursor.getInt(0);
                double oldTotal = cursor.getDouble(1);

                int newQuantity = oldQuantity + quantity;
                double newTotal = oldTotal + total;
                double newPrice = (newQuantity > 0) ? newTotal / newQuantity : 0.0;

                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME, name);
                values.put(COLUMN_PRICE, newPrice);
                values.put(COLUMN_QUANTITY, newQuantity);
                values.put(COLUMN_TOTAL, newTotal);

                int result = db.update(TABLE_ITEMS, values, COLUMN_BARCODE + " = ?", new String[]{barcode});
                updated = (result > 0);
            } else {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME, name);
                values.put(COLUMN_BARCODE, barcode);
                values.put(COLUMN_PRICE, price);
                values.put(COLUMN_QUANTITY, quantity);
                values.put(COLUMN_CURRENCY, currency);
                values.put(COLUMN_TOTAL, total);
                values.put(COLUMN_UNIT, unit);
                long result = db.insert(TABLE_ITEMS, null, values);
                updated = (result != -1);
            }
        } finally {
            cursor.close();
            db.close();
        }
        return updated;
    }

    public boolean updateItem(String oldBarcode, String newBarcode, String newName, double newPrice,
                              int newQuantity, String newCurrency, double newTotal, String newUnit) {
        if (!oldBarcode.equals(newBarcode)) {
            if (isBarcodeExists(newBarcode)) {
                return false;
            }
        }
        double autoPrice = (newQuantity > 0) ? newTotal / newQuantity : 0.0;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BARCODE, newBarcode);
        values.put(COLUMN_NAME, newName);
        values.put(COLUMN_PRICE, autoPrice);
        values.put(COLUMN_QUANTITY, newQuantity);
        values.put(COLUMN_CURRENCY, newCurrency);
        values.put(COLUMN_TOTAL, newTotal);
        values.put(COLUMN_UNIT, newUnit);

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
                    @SuppressLint("Range") String unit = cursor.getString(cursor.getColumnIndex(COLUMN_UNIT));
                    items.add(new Item(code, name, price, quantity, currency, total, unit));
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
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_QUANTITY + ", " + COLUMN_PRICE + " FROM " + TABLE_ITEMS + " WHERE " + COLUMN_BARCODE + " = ?", new String[]{barcode});
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
                @SuppressLint("Range") String unit = cursor.getString(cursor.getColumnIndex(COLUMN_UNIT));
                return new Item(code, name, price, quantity, currency, total, unit);
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