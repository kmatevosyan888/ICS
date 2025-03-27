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

public class EditItemActivity extends AppCompatActivity {
    private EditText etName, etPrice, etQuantity;
    private Spinner currencySpinner;
    private String originalBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        etName = findViewById(R.id.etEditName);
        etPrice = findViewById(R.id.etEditPrice);
        etQuantity = findViewById(R.id.etEditQuantity);
        currencySpinner = findViewById(R.id.spinnerEditCurrency);
        Button btnSave = findViewById(R.id.btnSaveChanges);

        Intent intent = getIntent();
        etName.setText(intent.getStringExtra("NAME"));
        etPrice.setText(String.valueOf(intent.getDoubleExtra("PRICE", 0)));
        etQuantity.setText(String.valueOf(intent.getIntExtra("QUANTITY", 0)));
        originalBarcode = intent.getStringExtra("BARCODE");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);

        String currency = intent.getStringExtra("CURRENCY");
        for (int i = 0; i < currencySpinner.getCount(); i++) {
            if(currencySpinner.getItemAtPosition(i).toString().startsWith(currency)) {
                currencySpinner.setSelection(i);
                break;
            }
        }

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        if (!validateInput()) return;

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        String selectedCurrency = currencySpinner.getSelectedItem().toString().split(" ")[0];

        boolean success = dbHelper.updateItem(
                originalBarcode,
                etName.getText().toString(),
                Double.parseDouble(etPrice.getText().toString()),
                Integer.parseInt(etQuantity.getText().toString()),
                selectedCurrency
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
        if (TextUtils.isEmpty(etName.getText())) {
            showError("Введите название");
            return false;
        }

        if (TextUtils.isEmpty(etPrice.getText())) {
            showError("Введите цену");
            return false;
        }
        try {
            double price = Double.parseDouble(etPrice.getText().toString());
            if (price < 0) {
                showError("Цена не может быть отрицательной");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Некорректная цена");
            return false;
        }

        if (TextUtils.isEmpty(etQuantity.getText())) {
            showError("Введите количество");
            return false;
        }
        try {
            int quantity = Integer.parseInt(etQuantity.getText().toString());
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