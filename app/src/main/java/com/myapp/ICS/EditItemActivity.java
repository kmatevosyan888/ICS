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
        currencySpinner = findViewById(R.id.spinnerEditCurrency);
        btnSave = findViewById(R.id.btnSaveChanges);

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadItemData() {
        Intent intent = getIntent();
        etEditName.setText(intent.getStringExtra("NAME"));
        etEditPrice.setText(String.valueOf(intent.getDoubleExtra("PRICE", 0)));
        etEditQuantity.setText(String.valueOf(intent.getIntExtra("QUANTITY", 0)));
        etEditBarcode.setText(intent.getStringExtra("BARCODE"));
        originalBarcode = intent.getStringExtra("BARCODE");

        String currency = intent.getStringExtra("CURRENCY");
        setCurrencySelection(currency);
        calculateTotal();
    }

    private void setCurrencySelection(String currency) {
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
            @Override
            public void afterTextChanged(Editable s) {
                calculateTotal();
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        etEditPrice.addTextChangedListener(textWatcher);
        etEditQuantity.addTextChangedListener(textWatcher);
        etEditBarcode.addTextChangedListener(textWatcher);
    }

    private void calculateTotal() {
        try {
            double price = Double.parseDouble(etEditPrice.getText().toString());
            int quantity = Integer.parseInt(etEditQuantity.getText().toString());
            double total = price * quantity;
            etEditTotal.setText(String.format("%.2f", total));
        } catch (NumberFormatException e) {
            etEditTotal.setText("0.00");
        }
    }
    private void saveChanges() {
        if (!validateInput()) return;

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        String newBarcode = etEditBarcode.getText().toString().trim();
        String selectedCurrency = currencySpinner.getSelectedItem().toString().split(" ")[0];

        boolean success = dbHelper.updateItem(
                originalBarcode, // Старый штрих-код для поиска
                newBarcode,      // Новый штрих-код
                etEditName.getText().toString(),
                Double.parseDouble(etEditPrice.getText().toString()),
                Integer.parseInt(etEditQuantity.getText().toString()),
                selectedCurrency,
                Double.parseDouble(etEditTotal.getText().toString())
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
        // Проверка штрих-кода
        if (TextUtils.isEmpty(etEditBarcode.getText())) {
            showError("Введите штрих-код");
            return false;
        }

        // Проверка названия
        if (TextUtils.isEmpty(etEditName.getText())) {
            showError("Введите название");
            return false;
        }

        // Проверка цены
        try {
            double price = Double.parseDouble(etEditPrice.getText().toString());
            if (price < 0) {
                showError("Цена не может быть отрицательной");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Некорректная цена");
            return false;
        }

        // Проверка количества
        try {
            int quantity = Integer.parseInt(etEditQuantity.getText().toString());
            if (quantity < 0) {
                showError("Количество не может быть отрицательным");
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
}