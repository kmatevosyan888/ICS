package com.myapp.ICS;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddItemsActivity extends AppCompatActivity {
    private static final int SCAN_REQUEST_CODE = 1001;

    private EditText itemName, itemBarcode, itemPrice, itemQuantity;
    private Spinner currencySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        initViews();
        setupCurrencySpinner();
        setupQuantityButtons();
    }

    private void initViews() {
        itemName = findViewById(R.id.itemName);
        itemBarcode = findViewById(R.id.itemBarcode);
        itemPrice = findViewById(R.id.itemPrice);
        itemQuantity = findViewById(R.id.itemQuantity);
        currencySpinner = findViewById(R.id.currencySpinner);
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveItemToDatabase());
        Button scanButton = findViewById(R.id.btnScanAdd);
        scanButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScannerActivity.class);
            startActivityForResult(intent, SCAN_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String result = data.getStringExtra(ScannerActivity.SCAN_RESULT);
                itemBarcode.setText(result);
            }
        }
    }
    private void setupCurrencySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);
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

    private void saveItemToDatabase() {
        if (!validateInput()) return;

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        boolean success = dbHelper.addItem(
                itemName.getText().toString(),
                itemBarcode.getText().toString(),
                Double.parseDouble(itemPrice.getText().toString()),
                getCurrentQuantity(),
                currencySpinner.getSelectedItem().toString().split(" ")[0]
        );

        showResult(success);
    }

    private boolean validateInput() {
        // Проверка названия
        if (TextUtils.isEmpty(itemName.getText())) {
            showError("Введите название");
            return false;
        }

        // Проверка штрих-кода
        if (TextUtils.isEmpty(itemBarcode.getText())) {
            showError("Введите штрих-код");
            return false;
        }

        // Проверка цены
        if (TextUtils.isEmpty(itemPrice.getText())) {
            showError("Введите цену");
            return false;
        }
        try {
            double price = Double.parseDouble(itemPrice.getText().toString());
            if (price < 0) {
                showError("Цена не может быть отрицательной");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Некорректная цена");
            return false;
        }

        // Проверка количества
        int quantity = getCurrentQuantity();
        if (quantity < 0) {
            showError("Количество не может быть отрицательным");
            return false;
        }
        return true;
    }
    private int getCurrentQuantity() {
        try {
            return Integer.parseInt(itemQuantity.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showResult(boolean success) {
        if (success) {
            Toast.makeText(this, "Товар добавлен", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Ошибка добавления", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        itemName.getText().clear();
        itemBarcode.getText().clear();
        itemPrice.getText().clear();
        itemQuantity.setText("0");
        currencySpinner.setSelection(0);
    }
}
