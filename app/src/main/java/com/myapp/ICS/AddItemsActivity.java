package com.myapp.ICS;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddItemsActivity extends AppCompatActivity {
    public EditText itemName, itemBarcode, itemPrice, itemQuantity;
    public Button saveButton, increaseButton, decreaseButton;
    private Spinner currencySpinner;

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
        currencySpinner = findViewById(R.id.currencySpinner);


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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);

    }
    public void saveItemToDatabase() {
        String name = itemName.getText().toString();
        String barcode = itemBarcode.getText().toString();
        String priceText = itemPrice.getText().toString();
        String quantityText = itemQuantity.getText().toString();
        String selectedCurrency = currencySpinner.getSelectedItem().toString().split(" ")[0];

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(barcode) || TextUtils.isEmpty(priceText) || TextUtils.isEmpty(quantityText)) {
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
        boolean isInserted = databaseHelper.addItem(name, barcode, price, quantity, selectedCurrency);

        if (isInserted) {
            Toast.makeText(this, "Товар успешно добавлен", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Ошибка добавления товара", Toast.LENGTH_SHORT).show();
        }
    }



    private void clearFields() {
        itemName.setText("");
        itemBarcode.setText("");
        itemPrice.setText("");
        itemQuantity.setText("0");
        currencySpinner.setSelection(0);
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
}