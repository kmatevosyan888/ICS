package com.myapp.ICS;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RemoveItemsActivity extends AppCompatActivity {
    private static final int SCAN_REQUEST_CODE = 1002;

    private EditText itemBarcodeToRemove, itemName, itemQuantity, itemTotal;
    private DatabaseHelper dbHelper;
    private Item currentItem;
    private boolean notFoundShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_items);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupQuantityButtons();
        setupBarcodeListener();

        itemTotal.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) formatEditTextToTwoDecimals(itemTotal);
        });
    }

    private void initViews() {
        itemBarcodeToRemove = findViewById(R.id.itemBarcodeToRemove);
        itemName = findViewById(R.id.itemNameRemove);
        itemQuantity = findViewById(R.id.itemQuantity);
        itemTotal = findViewById(R.id.itemTotalRemove);
        itemTotal.setEnabled(false);

        Button removeButton = findViewById(R.id.removeButton);
        Button scanButton = findViewById(R.id.btnScanRemove);

        removeButton.setOnClickListener(v -> removeItem());
        scanButton.setOnClickListener(v -> startScanner());

        itemQuantity.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { calculateTotal(); }
        });
    }

    private void setupBarcodeListener() {
        itemBarcodeToRemove.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    checkBarcodeAndLoadDetails();
                } else {
                    clearItemDetails();
                    notFoundShown = false;
                }
            }
        });
    }

    private void startScanner() {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, SCAN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String result = data.getStringExtra(ScannerActivity.SCAN_RESULT);
            itemBarcodeToRemove.setText(result);
        }
    }

    private void checkBarcodeAndLoadDetails() {
        String barcode = itemBarcodeToRemove.getText().toString().trim();
        if (!TextUtils.isEmpty(barcode)) {
            currentItem = dbHelper.getItemByBarcode(barcode);
            if (currentItem != null) {
                updateItemDetails();
                notFoundShown = false;
            } else {
                clearItemDetails();
                if (!notFoundShown) {
                    showError("Товар не найден");
                    notFoundShown = true;
                }
            }
        } else {
            clearItemDetails();
            notFoundShown = false;
        }
    }

    private void updateItemDetails() {
        itemName.setText(currentItem.getName());
        itemQuantity.setText("1");
        calculateTotal();
    }

    private void clearItemDetails() {
        itemName.setText("");
        itemQuantity.setText("0");
        itemTotal.setText("0,00");
        currentItem = null;
    }

    private void setupQuantityButtons() {
        Button increaseButton = findViewById(R.id.btnIncreaseQuantity);
        Button decreaseButton = findViewById(R.id.btnDecreaseQuantity);

        increaseButton.setOnClickListener(v -> adjustQuantity(1));
        decreaseButton.setOnClickListener(v -> adjustQuantity(-1));
    }

    private void adjustQuantity(int delta) {
        try {
            int current = Integer.parseInt(itemQuantity.getText().toString());
            int newQuantity = current + delta;
            if (newQuantity >= 0) {
                itemQuantity.setText(String.valueOf(newQuantity));
            }
        } catch (NumberFormatException e) {
            itemQuantity.setText("0");
        }
    }

    private void calculateTotal() {
        if (currentItem != null) {
            try {
                int quantity = Integer.parseInt(itemQuantity.getText().toString());
                double total = currentItem.getUnitPrice() * quantity;
                itemTotal.setText(formatDecimal(total) + " " + getCurrencySymbol());
            } catch (NumberFormatException e) {
                itemTotal.setText("0,00");
            }
        }
    }

    private String formatDecimal(double value) {
        return String.format("%.2f", value).replace('.', ',');
    }

    private String getCurrencySymbol() {
        if (currentItem == null) return "";
        switch (currentItem.getCurrency()) {
            case "USD": return "$";
            case "EUR": return "€";
            case "AMD": return "֏";
            default: return "₽";
        }
    }

    private void removeItem() {
        String barcode = itemBarcodeToRemove.getText().toString().trim();
        String enteredName = itemName.getText().toString().trim();

        if (validateInput(barcode, enteredName)) {
            try {
                int quantityToRemove = Integer.parseInt(itemQuantity.getText().toString());

                if (currentItem.getQuantity() < quantityToRemove) {
                    showError("Недостаточно товара\nДоступно: " + currentItem.getQuantity());
                    return;
                }

                boolean success = dbHelper.removeItem(barcode, quantityToRemove);
                handleRemoveResult(success);
            } catch (NumberFormatException e) {
                showError("Некорректное количество");
            }
        }
    }

    private boolean validateInput(String barcode, String enteredName) {
        if (TextUtils.isEmpty(barcode)) {
            showError("Введите штрих-код");
            return false;
        }
        if (currentItem == null) {
            showError("Товар не найден");
            return false;
        }
        if (!currentItem.getName().equalsIgnoreCase(enteredName)) {
            showError("Название не совпадает!\nОжидается: " + currentItem.getName());
            return false;
        }
        return true;
    }

    private void handleRemoveResult(boolean success) {
        if (success) {
            showSuccess("Товар списан");
            clearFields();
        } else {
            showError("Ошибка списания");
        }
    }

    private void clearFields() {
        itemBarcodeToRemove.getText().clear();
        itemName.getText().clear();
        itemQuantity.setText("0");
        itemTotal.setText("0,00");
        currentItem = null;
        notFoundShown = false;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void formatEditTextToTwoDecimals(EditText editText) {
        String input = editText.getText().toString().replace(',', '.');
        try {
            double value = Double.parseDouble(input);
            editText.setText(formatDecimal(value));
        } catch (Exception ignored) {}
    }
}