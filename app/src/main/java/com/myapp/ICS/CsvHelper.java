package com.myapp.ICS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import androidx.core.content.FileProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CsvHelper {
    private static final String TAG = "CsvHelper";
    private final DatabaseHelper dbHelper;
    private final Context context;

    public CsvHelper(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    public Uri exportToCsv() {
        try {
            File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ICS_Exports");
            if (!exportDir.exists() && !exportDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
                return null;
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File file = new File(exportDir, "ICS_Export_" + timeStamp + ".csv");

            try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM items", null);
                 FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8")) {

                osw.append("Barcode,Name,Price,Quantity,Currency,Total\n");

                while (cursor.moveToNext()) {
                    osw.append(String.format(Locale.US, "%s,%s,%.2f,%d,%s,%.2f\n",
                            cursor.getString(2),  // Barcode
                            cursor.getString(1),   // Name
                            cursor.getDouble(3),   // Price
                            cursor.getInt(4),      // Quantity
                            cursor.getString(5),   // Currency
                            cursor.getDouble(6))); // Total
                }
            }

            return FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider",
                    file);

        } catch (Exception e) {
            Log.e(TAG, "Export failed", e);
            return null;
        }
    }

    public int importFromCsv(Uri uri) {
        int importedCount = 0;
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            try {
                db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_ITEMS);
                // 1. Удаляем все существующие записи
                dbHelper.deleteAllItems();

                // 2. Импортируем новые данные
                String line;
                boolean isFirstLine = true;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (columns.length != 6) continue;

                    try {
                        ContentValues values = new ContentValues();
                        values.put("barcode", columns[0].replace("\"", "").trim());
                        values.put("name", columns[1].replace("\"", "").trim());
                        values.put("price", Double.parseDouble(columns[2].trim()));
                        values.put("quantity", Integer.parseInt(columns[3].trim()));
                        values.put("currency", columns[4].replace("\"", "").trim());
                        values.put("total", Double.parseDouble(columns[5].trim()));

                        long id = db.insert(DatabaseHelper.TABLE_ITEMS, null, values);
                        if (id != -1) importedCount++;
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing line " + lineNumber, e);
                    }
                    lineNumber++;
                }

                db.setTransactionSuccessful();
                return importedCount;
            } finally {
                db.endTransaction();
            }

        } catch (Exception e) {
            Log.e(TAG, "Import failed", e);
            return -1;
        }
    }
}