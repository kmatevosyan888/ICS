package com.myapp.ICS;

public class Item {
    private String code;
    private String name;
    private double unitPrice;
    private int quantity;
    private String currency;

    public Item(String code, String name, double unitPrice, int quantity, String currency) {
        this.code = code;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.currency = currency;
    }
    public String getCode() { return code; }
    public String getName() { return name; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public String getCurrency() { return currency; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}