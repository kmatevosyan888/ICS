package com.myapp.ICS;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RemoveItemsActivity extends AppCompatActivity {
    private EditText itemBarcodeToRemove, itemName, itemQuantity;
    private DatabaseHelper dbHelper;
    private Item currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_items);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupQuantityButtons();
        setupBarcodeListener();
    }

    private void initViews() {
        itemBarcodeToRemove = findViewById(R.id.itemBarcodeToRemove);
        itemName = findViewById(R.id.itemName);
        itemQuantity = findViewById(R.id.itemQuantity);

        Button removeButton = findViewById(R.id.removeButton);
        removeButton.setOnClickListener(v -> removeItem());
    }

    private void setupBarcodeListener() {
        itemBarcodeToRemove.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                checkBarcodeAndLoadName();
            }
        });
    }

    private void checkBarcodeAndLoadName() {
        String barcode = itemBarcodeToRemove.getText().toString().trim();
        if (!barcode.isEmpty()) {
            currentItem = dbHelper.getItemByBarcode(barcode);
            if (currentItem != null) {
                itemName.setText(currentItem.getName());
                itemQuantity.setText("1");
            } else {
                itemName.setText("");
                itemQuantity.setText("0");
                showError("Товар не найден");
            }
        }
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
        String enteredName = itemName.getText().toString().trim();

        if (barcode.isEmpty()) {
            showError("Введите штрих-код");
            return;
        }

        if (currentItem == null) {
            showError("Сначала найдите товар по штрих-коду");
            return;
        }

        if (!currentItem.getName().equalsIgnoreCase(enteredName)) {
            showError("Название не совпадает!\nОжидается: " + currentItem.getName());
            return;
        }

        if (currentItem.getQuantity() < quantity) {
            showError("Недостаточно товара\nДоступно: " + currentItem.getQuantity());
            return;
        }

        boolean success = dbHelper.removeItem(barcode, quantity);
        if (success) {
            showSuccess("Товар списан");
            clearFields();
        } else {
            showError("Ошибка списания");
        }
    }

    private int getCurrentQuantity() {
        try {
            return Integer.parseInt(itemQuantity.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void clearFields() {
        itemBarcodeToRemove.getText().clear();
        itemName.getText().clear();
        itemQuantity.setText("0");
        currentItem = null;
    }
}