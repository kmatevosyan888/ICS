package com.myapp.ICS;

import android.annotation.SuppressLint;
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
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

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

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Item> newList) {
        itemList = newList;
        notifyDataSetChanged();
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
        holder.tvQuantity.setText("Количество: " + item.getQuantity());
        holder.tvTotal.setText(formatTotal(item)); // Новое поле
    }

    private String formatPrice(Item item) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        return String.format("Цена: %s%s",
                getCurrencySymbol(item.getCurrency()),
                format.format(item.getUnitPrice()));
    }

    private String formatTotal(Item item) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setCurrency(Currency.getInstance(item.getCurrency()));
        return "Сумма: " + format.format(item.getTotal());
    }

    private String getCurrencySymbol(String currency) {
        switch (currency) {
            case "USD": return "$";
            case "EUR": return "€";
            case "AMD": return "֏";
            default: return "₽";
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvPrice, tvQuantity, tvTotal;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvCode = itemView.findViewById(R.id.tvItemCode);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvTotal = itemView.findViewById(R.id.tvItemTotal);
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
}