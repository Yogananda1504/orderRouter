package com.example.order.order.models;

import java.util.HashMap;
import java.util.Map;

public class Order {

    private Long id;
    private volatile OrderStatus status = OrderStatus.UNASSIGNED;
    private volatile Center assignedCenter;
    private double x;
    private double y;
    private Map<Long, Integer> items = new HashMap<>();

    public Order() {
    }

    public Order(Long id, Map<Long, Integer> items) {
        this.id = id;
        this.items = items;
    }

    public Order(Long id, Map<Long, Integer> items, double x, double y) {
        this(id, items);
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

    public OrderStatus getStatus() {
        return status;
    }

    public Center getAssignedCenter() {
        return assignedCenter;
    }

    public void setAssignedCenter(Center assignedCenter) {
        this.assignedCenter = assignedCenter;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<Long, Integer> getItems() {
        return items;
    }

    public void setItems(Map<Long, Integer> items) {
        this.items = items;
    }
}
