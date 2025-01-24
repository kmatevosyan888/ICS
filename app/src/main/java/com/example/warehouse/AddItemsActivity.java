package com.example.warehouse;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddItemsActivity extends AppCompatActivity {
    public EditText itemName, itemBarcode, itemPrice, itemQuantity;
    public Button saveButton, increaseButton, decreaseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        itemName = findViewById(R.id.itemName);
        itemBarcode = findViewById(R.id.itemBarcode);
        itemPrice = findViewById(R.id.itemPrice);
        itemQuantity = findViewById(R.id.itemQuantity);
        saveButton = findViewById(R.id.saveButton);
        increaseButton = findViewById(R.id.btnIncreaseQuantity);
        decreaseButton = findViewById(R.id.btnDecreaseQuantity);


        saveButton.setOnClickListener(v -> saveItemToDatabase());

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = getQuantityFromEditText();
                itemQuantity.setText(String.valueOf(quantity + 1));
            }
        });
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = getQuantityFromEditText();
                if (quantity > 0) {
                    itemQuantity.setText(String.valueOf(quantity - 1));
                }
            }
        });
    }

    public void saveItemToDatabase() {
        String name = itemName.getText().toString();
        String barcode = itemBarcode.getText().toString();
        String priceText = itemPrice.getText().toString();
        String quantityText = itemQuantity.getText().toString();

        if (name.isEmpty() || barcode.isEmpty() || priceText.isEmpty() || quantityText.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int quantity;

        try {
            price = Double.parseDouble(priceText);
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректные данные для цены и количества", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("barcode", barcode);
        values.put("price", price);
        values.put("quantity", quantity);

        long newRowId = db.insert("items", null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Товар успешно добавлен", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Ошибка добавления товара", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    private void clearFields() {
        itemName.setText("");
        itemBarcode.setText("");
        itemPrice.setText("");
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

    public static class DatabaseHelper extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = "warehouse.db";
        public static final int DATABASE_VERSION = 1;

        public DatabaseHelper(AddItemsActivity context) {
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