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

public class EditItemActivity extends AppCompatActivity {
    private EditText etEditName, etEditPrice, etEditQuantity, etEditBarcode, etEditTotal;
    Button btnSave;
    private Spinner currencySpinner;
    private String originalBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        initViews();
        setupCurrencySpinner();
        setupTextWatchers();
        loadItemData();
    }

    private void initViews() {
        etEditName = findViewById(R.id.etEditName);
        etEditPrice = findViewById(R.id.etEditPrice);
        etEditQuantity = findViewById(R.id.etEditQuantity);
        etEditBarcode = findViewById(R.id.etEditBarcode);
        etEditTotal = findViewById(R.id.etEditTotal);
        etEditTotal.setEnabled(true);
        etEditTotal.setFocusable(true);
        etEditPrice.setEnabled(false);
        etEditPrice.setFocusable(false);
        currencySpinner = findViewById(R.id.spinnerEditCurrency);
        btnSave = findViewById(R.id.btnSaveChanges);

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadItemData() {
        Intent intent = getIntent();
        etEditName.setText(intent.getStringExtra("NAME"));
        etEditBarcode.setText(intent.getStringExtra("BARCODE"));
        originalBarcode = intent.getStringExtra("BARCODE");
        etEditQuantity.setText(String.valueOf(intent.getIntExtra("QUANTITY", 0)));

        // ВАЖНО: Сначала получить сумму
        double total = intent.hasExtra("TOTAL") ? intent.getDoubleExtra("TOTAL", 0) : 0;
        etEditTotal.setText(total > 0 ? String.format("%.2f", total) : "");

        String currency = intent.getStringExtra("CURRENCY");
        setCurrencySelection(currency);

        // Пересчитать цену (если сумма и количество > 0)
        calculatePrice();
    }

    private void setCurrencySelection(String currency) {
        if (currency == null) return;
        for (int i = 0; i < currencySpinner.getCount(); i++) {
            if (currencySpinner.getItemAtPosition(i).toString().startsWith(currency)) {
                currencySpinner.setSelection(i);
                break;
            }
        }
    }

    private void setupCurrencySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.currencies,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { calculatePrice(); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        etEditTotal.addTextChangedListener(textWatcher);
        etEditQuantity.addTextChangedListener(textWatcher);
    }

    private void calculatePrice() {
        try {
            double total = parseDoubleLocale(etEditTotal.getText().toString());
            int quantity = Integer.parseInt(etEditQuantity.getText().toString());
            double price = (quantity > 0) ? total / quantity : 0.0;
            etEditPrice.setText(String.format("%.2f", price));
        } catch (Exception e) {
            etEditPrice.setText("0.00");
        }
    }

    private void saveChanges() {
        if (!validateInput()) return;

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        String newBarcode = etEditBarcode.getText().toString().trim();
        String selectedCurrency = currencySpinner.getSelectedItem().toString().split(" ")[0];

        boolean success = dbHelper.updateItem(
                originalBarcode,
                newBarcode,
                etEditName.getText().toString(),
                parseDoubleLocale(etEditPrice.getText().toString()),
                Integer.parseInt(etEditQuantity.getText().toString()),
                selectedCurrency,
                parseDoubleLocale(etEditTotal.getText().toString())
        );

        if (success) {
            Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etEditBarcode.getText())) {
            showError("Введите штрих-код");
            return false;
        }
        if (TextUtils.isEmpty(etEditName.getText())) {
            showError("Введите название");
            return false;
        }
        try {
            double total = parseDoubleLocale(etEditTotal.getText().toString());
            if (total < 0) {
                showError("Сумма не может быть отрицательной");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Некорректная сумма");
            return false;
        }
        try {
            int quantity = Integer.parseInt(etEditQuantity.getText().toString());
            if (quantity <= 0) {
                showError("Количество должно быть больше нуля");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Некорректное количество");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private double parseDoubleLocale(String str) {
        if (str == null) return 0.0;
        str = str.replace(",", ".").replaceAll("[^\\d.]", "");
        if (str.isEmpty()) return 0.0;
        return Double.parseDouble(str);
    }
}