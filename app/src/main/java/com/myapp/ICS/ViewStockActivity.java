package com.myapp.ICS;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ViewStockActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private StockAdapter adapter;
    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<String> importLauncher;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stock);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progressBar);
        Button btnExport = findViewById(R.id.btnExport);
        Button btnImport = findViewById(R.id.btnImport);

        btnExport.setOnClickListener(v -> exportData());
        btnImport.setOnClickListener(v -> checkStoragePermission());

        importLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) handleImport(uri);
                });
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
        if (adapter == null) {
            adapter = new StockAdapter(items, new StockAdapter.DataUpdateListener() {
                @Override
                public void onDataUpdated() {
                    loadItems();
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(items);
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
    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    101
            );
        } else {
            importLauncher.launch("text/comma-separated-values");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            importLauncher.launch("text/comma-separated-values");
        }
    }

    private void exportData() {
        showProgress(true);
        new Thread(() -> {
            Uri uri = new CsvHelper(this).exportToCsv();
            runOnUiThread(() -> {
                showProgress(false);
                if (uri != null) {
                    shareExportedFile(uri);
                } else {
                    Toast.makeText(this, "Ошибка экспорта", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void shareExportedFile(Uri uri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Экспортировать в..."));
    }

    private void handleImport(Uri uri) {
        showProgress(true);
        new Thread(() -> {
            int result = new CsvHelper(ViewStockActivity.this).importFromCsv(uri);
            runOnUiThread(() -> {
                showProgress(false);
                showImportResult(result);
                loadItems();
            });
        }).start();
    }

    private void showImportResult(int result) {
        String message;
        switch (result) {
            case -1: message = "Ошибка чтения файла"; break;
            case -2: message = "Ошибка в числовых полях"; break;
            case -3: message = "Некорректный формат файла"; break;
            default: message = "Импортировано записей: " + result;
        }

        new AlertDialog.Builder(this)
                .setTitle("Результат импорта")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}