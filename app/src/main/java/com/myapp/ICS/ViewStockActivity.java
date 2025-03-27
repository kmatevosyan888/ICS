package com.myapp.ICS;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
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

        loadItems();
    }

    private void loadItems() {
        List<Item> items = dbHelper.getAllItemsList();

        if (adapter == null) {
            adapter = new StockAdapter(items, this::refreshData);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(items);
        }

        if (items.isEmpty()) {
            Toast.makeText(this, "Склад пуст. Добавьте товары!", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshData() {
        loadItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }
}