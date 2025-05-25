package com.myapp.ICS;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddItemsActivity extends AppCompatActivity {
    private static final int SCAN_REQUEST_CODE = 1001;

    private EditText itemName, itemBarcode, itemPrice, itemQuantity, itemTotal;
    private Spinner currencySpinner;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupCurrencySpinner();
        setupQuantityButtons();
        setupTextWatchers();
        setupBarcodeListener();

        itemTotal.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) formatEditTextToTwoDecimals(itemTotal);
        });
        itemPrice.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) formatEditTextToTwoDecimals(itemPrice);
        });
    }

    private void initViews() {
        itemName = findViewById(R.id.itemNameAdd);
        itemBarcode = findViewById(R.id.itemBarcode);
        itemPrice = findViewById(R.id.itemPrice);
        itemQuantity = findViewById(R.id.itemQuantity);
        itemTotal = findViewById(R.id.itemTotal);
        itemTotal.setEnabled(true);
        itemPrice.setEnabled(false);
        currencySpinner = findViewById(R.id.currencySpinner);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveItemToDatabase());

        Button scanButton = findViewById(R.id.btnScanAdd);
        scanButton.setOnClickListener(v -> startScanner());
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

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { calculatePrice(); }
        };

        itemTotal.addTextChangedListener(textWatcher);
        itemQuantity.addTextChangedListener(textWatcher);
    }

    private void calculatePrice() {
        try {
            double total = parseDoubleLocale(itemTotal.getText().toString());
            int quantity = Integer.parseInt(itemQuantity.getText().toString());
            double price = (quantity > 0) ? total / quantity : 0.0;
            itemPrice.setText(formatDecimal(price));
        } catch (NumberFormatException e) {
            itemPrice.setText("0,00");
        }
    }

    private void setupBarcodeListener() {
        itemBarcode.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) checkBarcodeAndFillName();
        });
        itemBarcode.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.length() >= 1) checkBarcodeAndFillName();
            }
        });
    }

    private void checkBarcodeAndFillName() {
        String barcode = itemBarcode.getText().toString().trim();
        if (!TextUtils.isEmpty(barcode)) {
            Item found = dbHelper.getItemByBarcode(barcode);
            if (found != null) {
                itemName.setText(found.getName());
            }
        }
    }

    private void adjustQuantity(int delta) {
        int quantity = getCurrentQuantity() + delta;
        if (quantity >= 0) {
            itemQuantity.setText(String.valueOf(quantity));
            calculatePrice();
        }
    }

    private void startScanner() {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, SCAN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            itemBarcode.setText(data.getStringExtra(ScannerActivity.SCAN_RESULT));
        }
    }

    private void saveItemToDatabase() {
        if (!validateInput()) return;

        String barcode = itemBarcode.getText().toString().trim();
        String selectedCurrency = currencySpinner.getSelectedItem().toString().split(" ")[0];
        Item found = dbHelper.getItemByBarcode(barcode);

        if (found != null && !found.getCurrency().equals(selectedCurrency)) {
            showError("Этот товар добавлен в валюте: " + found.getCurrency() + ". Добавлять в другой валюте нельзя!");
            return;
        }

        boolean success = dbHelper.addItem(
                itemName.getText().toString(),
                barcode,
                parseDoubleLocale(itemPrice.getText().toString()),
                getCurrentQuantity(),
                selectedCurrency,
                parseDoubleLocale(itemTotal.getText().toString())
        );
        showResult(success);
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(itemName.getText())) {
            showError("Введите название");
            return false;
        }
        if (TextUtils.isEmpty(itemBarcode.getText())) {
            showError("Введите штрих-код");
            return false;
        }
        if (TextUtils.isEmpty(itemTotal.getText())) {
            showError("Введите сумму");
            return false;
        }
        try {
            double total = parseDoubleLocale(itemTotal.getText().toString());
            if (total < 0) {
                showError("Сумма не может быть отрицательной");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Некорректная сумма");
            return false;
        }
        int quantity = getCurrentQuantity();
        if (quantity <= 0) {
            showError("Количество должно быть больше нуля");
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
        itemTotal.setText("0,00");
        itemPrice.setText("0,00");
        itemQuantity.setText("0");
        currencySpinner.setSelection(0);
    }

    private double parseDoubleLocale(String str) {
        if (str == null) return 0.0;
        str = str.replace(",", ".").replaceAll("[^\\d.]", "");
        if (str.isEmpty()) return 0.0;
        return Double.parseDouble(str);
    }

    private void formatEditTextToTwoDecimals(EditText editText) {
        String input = editText.getText().toString().replace(',', '.');
        try {
            double value = Double.parseDouble(input);
            editText.setText(formatDecimal(value));
        } catch (Exception ignored) {}
    }

    private String formatDecimal(double value) {
        return String.format("%.2f", value).replace('.', ',');
    }
}