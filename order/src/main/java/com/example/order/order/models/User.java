package com.example.order.order.models;

import java.util.ArrayList;
import java.util.List;

public class User {

    private List<Order> orders = new ArrayList<>();
    private DeliveryChoice choice = DeliveryChoice.DISTANCE;

    public User() {
    }

    public User(List<Order> orders) {
        this.orders = orders;
    }

    public User(List<Order> orders, DeliveryChoice choice) {
        this(orders);
        setChoice(choice);
    }

    public DeliveryChoice getChoice() {
        return choice;
    }

    public void setChoice(DeliveryChoice choice) {
        if (choice == null) {
            throw new IllegalArgumentException("Delivery choice must be COST or DISTANCE");
        }
        this.choice = choice;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
