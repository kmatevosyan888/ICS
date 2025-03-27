package com.myapp.ICS;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RemoveItemsActivity extends AppCompatActivity {
    private EditText itemBarcodeToRemove, itemName, itemQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_items);

        initViews();
        setupQuantityButtons();
    }

    private void initViews() {
        itemBarcodeToRemove = findViewById(R.id.itemBarcodeToRemove);
        itemName = findViewById(R.id.itemName);
        itemQuantity = findViewById(R.id.itemQuantity);
        Button removeButton = findViewById(R.id.removeButton);
        removeButton.setOnClickListener(v -> removeItem());
    }

    private void setupQuantityButtons() {
        Button increaseButton = findViewById(R.id.btnIncreaseQuantity);
        Button decreaseButton = findViewById(R.id.btnDecreaseQuantity);

        increaseButton.setOnClickListener(v -> adjustQuantity(1));
        decreaseButton.setOnClickListener(v -> adjustQuantity(-1));
    }

    private void adjustQuantity(int delta) {
        int quantity = getCurrentQuantity() + delta;
        if (quantity >= 0) {
            itemQuantity.setText(String.valueOf(quantity));
        }
    }

    private void removeItem() {
        String barcode = itemBarcodeToRemove.getText().toString().trim();
        int quantity = getCurrentQuantity();

        if (barcode.isEmpty()) {
            Toast.makeText(this, "Введите штрих-код", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        boolean success = dbHelper.removeItem(barcode, quantity);

        if (success) {
            Toast.makeText(this, "Товар списан", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Ошибка списания", Toast.LENGTH_SHORT).show();
        }
    }

    private int getCurrentQuantity() {
        try {
            return Integer.parseInt(itemQuantity.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void clearFields() {
        itemBarcodeToRemove.getText().clear();
        itemQuantity.setText("0");
        itemName.getText().clear();
    }
}