package com.myapp.ICS;

import android.content.Context;
import java.util.LinkedList;
import java.util.HashMap;

import java.util.LinkedList;

public class InventoryManager {
    private static InventoryManager instance;
    private LinkedList<Item> itemList = new LinkedList<>();
    private final HashMap<String, Double> exchangeRates = new HashMap<>();

    private InventoryManager() {
        exchangeRates.put("RUB", 1.0);
        exchangeRates.put("USD", 0.013);
        exchangeRates.put("EUR", 0.011);
        exchangeRates.put("AMD", 5.0);
    }

    public static InventoryManager getInstance() {
        if (instance == null) {
            instance = new InventoryManager();
        }
        return instance;
    }

    public void addOrUpdateItem(Item newItem) {
        for (Item item : itemList) {
            if (item.getCode().equals(newItem.getCode())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                return;
            }
        }
        itemList.add(newItem);
    }

    public LinkedList<Item> getItemList() {
        return itemList;
    }

    public double getExchangeRate(String currency) {
        return exchangeRates.getOrDefault(currency, 1.0);
    }
}