<<<<<<<< HEAD:app/src/main/java/com/myapp/ICS/MainActivity.java
package com.myapp.ICS;
========
package com.ICS.myapp;
>>>>>>>> origin/main:app/src/main/java/com/ICS/myapp/MainActivity.java

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnAddItems = findViewById(R.id.btnAddItems);
        Button btnRemoveItems = findViewById(R.id.btnRemoveItems);
        Button btnViewStock = findViewById(R.id.btnViewStock);

        btnAddItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent add = new Intent(MainActivity.this, AddItemsActivity.class);
                startActivity(add);
            }
        });

        btnRemoveItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent remove = new Intent(MainActivity.this, RemoveItemsActivity.class);
                startActivity(remove);
            }
        });

        btnViewStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent stock = new Intent(MainActivity.this, ViewStockActivity.class);
                startActivity(stock);
            }
        });

    }

}