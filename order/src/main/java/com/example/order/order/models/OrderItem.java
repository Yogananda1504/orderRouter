package com.example.order.order.models;

public class OrderItem {

    private Long id;
    private int quantity;
    private double x;
    private double y;

    public OrderItem() {
    }

    public OrderItem(Long id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public OrderItem(Long id, int quantity, double x, double y) {
        this(id, quantity);
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
