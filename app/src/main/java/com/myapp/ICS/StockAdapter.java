package com.myapp.ICS;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {
    private List<Item> itemList;

    public StockAdapter(List<Item> itemList) {
        this.itemList = itemList;
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvCode.setText("Код: " + item.getCode());
        holder.tvPrice.setText(formatPrice(item));
        holder.tvQuantity.setText("Количество: " + item.getQuantity());
    }

    @SuppressLint("DefaultLocale")
    private String formatPrice(Item item) {
        String symbol = getCurrencySymbol(item.getCurrency());
        return String.format("Цена: %s%,.2f", symbol, item.getUnitPrice());
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvPrice, tvQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvCode = itemView.findViewById(R.id.tvItemCode);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
        }
    }
}