package com.myapp.ICS;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {
    private List<Item> itemList;
    private final DataUpdateListener dataUpdateListener;

    public interface DataUpdateListener {
        void onDataUpdated();
    }

    public StockAdapter(List<Item> itemList, DataUpdateListener listener) {
        this.itemList = itemList;
        this.dataUpdateListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvCode.setText("Код: " + item.getCode());
        holder.tvPrice.setText(formatPrice(item));
        holder.tvQuantity.setText("Количество: " + item.getQuantity() + " " + getUnitWithCorrectForm(item.getUnit(), item.getQuantity()));
        holder.tvUnit.setText("");
        holder.tvTotal.setText(formatTotal(item));
        holder.tvCurrency.setText("");
    }

    private String getUnitWithCorrectForm(String unit, int quantity) {
        if (!unit.equals("штук")) return unit;
        if (quantity % 10 == 1 && quantity % 100 != 11) return "штука";
        if (quantity % 10 >= 2 && quantity % 10 <= 4 && (quantity % 100 < 10 || quantity % 100 >= 20)) return "штуки";
        return "штук";
    }
    private String formatPrice(Item item) {
        return String.format("Цена: %s %s",
                formatDecimal(item.getUnitPrice()),
                getCurrencySymbol(item.getCurrency()));
    }

    private String formatTotal(Item item) {
        return String.format("Сумма: %s %s",
                formatDecimal(item.getTotal()),
                getCurrencySymbol(item.getCurrency()));
    }

    private String getCurrencySymbol(String currency) {
        if (currency == null) return "֏";
        switch (currency) {
            case "USD": return "$";
            case "EUR": return "€";
            case "AMD": return "֏";
            case "RUB": return "₽";
            default: return "֏";
        }
    }

    private String formatDecimal(double value) {
        return String.format("%.2f", value).replace('.', ',');
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvPrice, tvQuantity, tvTotal, tvCurrency, tvUnit;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvCode = itemView.findViewById(R.id.tvItemCode);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvUnit = itemView.findViewById(R.id.tvItemUnit);
            tvTotal = itemView.findViewById(R.id.tvItemTotal);
            tvCurrency = itemView.findViewById(R.id.tvItemCurrency);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            setupClickListeners();
        }

        private void setupClickListeners() {
            btnDelete.setOnClickListener(v -> deleteItem(getBindingAdapterPosition()));
            btnEdit.setOnClickListener(v -> editItem());
        }

        private void editItem() {
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            Item item = itemList.get(position);
            Intent intent = new Intent(itemView.getContext(), EditItemActivity.class);
            intent.putExtra("BARCODE", item.getCode());
            intent.putExtra("NAME", item.getName());
            intent.putExtra("PRICE", item.getUnitPrice());
            intent.putExtra("QUANTITY", item.getQuantity());
            intent.putExtra("CURRENCY", item.getCurrency());
            intent.putExtra("TOTAL", item.getTotal());
            intent.putExtra("UNIT", item.getUnit());
            itemView.getContext().startActivity(intent);
        }

        private void deleteItem(int position) {
            if (position == RecyclerView.NO_POSITION) return;

            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Удаление товара")
                    .setMessage("Вы уверены, что хотите удалить этот товар?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        Item item = itemList.get(position);
                        DatabaseHelper dbHelper = new DatabaseHelper(itemView.getContext());
                        if (dbHelper.deleteItem(item.getCode())) {
                            itemList.remove(position);
                            notifyItemRemoved(position);
                            dataUpdateListener.onDataUpdated();
                        } else {
                            showError("Ошибка удаления");
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        }

        private void showError(String message) {
            Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void updateList(List<Item> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }
}