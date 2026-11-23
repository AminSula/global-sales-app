package com.aminsula.pilot.sync;

public class SyncResult {

    private int products;
    private int productsNew;
    private int productsChanged;
    private int productsRemoved;
    private int customers;
    private int customersNew;
    private int customersChanged;
    private int customersRemoved;

    public int getProducts() {
        return products;
    }

    public void setProducts(int products) {
        this.products = products;
    }

    public int getProductsNew() {
        return productsNew;
    }

    public void setProductsNew(int productsNew) {
        this.productsNew = productsNew;
    }

    public int getProductsChanged() {
        return productsChanged;
    }

    public void setProductsChanged(int productsChanged) {
        this.productsChanged = productsChanged;
    }

    public int getProductsRemoved() {
        return productsRemoved;
    }

    public void setProductsRemoved(int productsRemoved) {
        this.productsRemoved = productsRemoved;
    }

    public int getCustomers() {
        return customers;
    }

    public void setCustomers(int customers) {
        this.customers = customers;
    }

    public int getCustomersNew() {
        return customersNew;
    }

    public void setCustomersNew(int customersNew) {
        this.customersNew = customersNew;
    }

    public int getCustomersChanged() {
        return customersChanged;
    }

    public void setCustomersChanged(int customersChanged) {
        this.customersChanged = customersChanged;
    }

    public int getCustomersRemoved() {
        return customersRemoved;
    }

    public void setCustomersRemoved(int customersRemoved) {
        this.customersRemoved = customersRemoved;
    }
}
