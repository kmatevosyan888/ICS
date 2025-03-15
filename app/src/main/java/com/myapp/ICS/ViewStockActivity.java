package com.myapp.ICS;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.LinkedList;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class ViewStockActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private StockAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stock);

        dbHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateStockList();
    }

    private void updateStockList() {
        LinkedList<Item> items = dbHelper.getAllItemsList();

        if (adapter == null) {
            adapter = new StockAdapter(items);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(items);
        }

        if (items.isEmpty()) {
            Toast.makeText(this, "Склад пуст. Добавьте товары!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStockList();
    }
}