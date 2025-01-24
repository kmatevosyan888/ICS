package com.example.warehouse;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RemoveItemsActivity extends AppCompatActivity {
    public EditText itemBarcodeToRemove, itemQuantity;
    public Button removeButton, increaseButton, decreaseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_items);

        itemBarcodeToRemove = findViewById(R.id.itemBarcodeToRemove);
        itemQuantity = findViewById(R.id.itemQuantity);
        removeButton = findViewById(R.id.removeButton);
        increaseButton = findViewById(R.id.btnIncreaseQuantity);
        decreaseButton = findViewById(R.id.btnDecreaseQuantity);

        removeButton.setOnClickListener(v -> removeItemFromDatabase());

        increaseButton.setOnClickListener(v -> {
            int quantity = getQuantityFromEditText();
            itemQuantity.setText(String.valueOf(quantity + 1));
        });

        decreaseButton.setOnClickListener(v -> {
            int quantity = getQuantityFromEditText();
            if (quantity > 0) {
                itemQuantity.setText(String.valueOf(quantity - 1));
            }
        });
    }

    public void removeItemFromDatabase() {
        String barcode = itemBarcodeToRemove.getText().toString();
        String quantityText = itemQuantity.getText().toString();

        if (TextUtils.isEmpty(barcode) || TextUtils.isEmpty(quantityText)) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректное количество", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        String[] args = {barcode};
        String query = "SELECT quantity FROM items WHERE barcode = ?";
        Cursor cursor = db.rawQuery(query, args);

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") int currentQuantity = cursor.getInt(cursor.getColumnIndex("quantity"));

            if (currentQuantity >= quantity) {
                ContentValues values = new ContentValues();
                values.put("quantity", currentQuantity - quantity);

                int rowsUpdated = db.update("items", values, "barcode = ?", new String[]{barcode});

                if (rowsUpdated > 0) {
                    Toast.makeText(this, "Товар выведен", Toast.LENGTH_SHORT).show();
                    clearFields();
                } else {
                    Toast.makeText(this, "Ошибка при обновлении товара", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Недостаточно товара на складе", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Товар с таким штрих-кодом не найден", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();
    }

    private void clearFields() {
        itemBarcodeToRemove.setText("");
        itemQuantity.setText("0");
    }

    public int getQuantityFromEditText() {
        String quantityText = itemQuantity.getText().toString();
        if (TextUtils.isEmpty(quantityText)) {
            return 0;
        }
        try {
            return Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static class DatabaseHelper extends android.database.sqlite.SQLiteOpenHelper {
        public static final String DATABASE_NAME = "warehouse.db";
        public static final int DATABASE_VERSION = 1;

        public DatabaseHelper(RemoveItemsActivity context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "barcode TEXT NOT NULL UNIQUE, " +
                    "price REAL NOT NULL, " +
                    "quantity INTEGER NOT NULL);";
            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS items");
            onCreate(db);
        }
    }
}