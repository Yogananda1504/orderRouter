package com.example.order.order.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.order.order.models.Center;
import com.example.order.order.models.DeliveryOption;
import com.example.order.order.models.Order;
import com.example.order.order.utils.ManhattanDistance;

public abstract class DeliveryStrategy<T> {

    private final List<Center> centers;
    private final ManhattanDistance manhattanDistance = new ManhattanDistance();

    protected DeliveryStrategy(List<Center> centers) {
        this.centers = List.copyOf(centers);
    }

    public abstract T calculate(Order order);

    // Returns fully stocked centers without ranking them or reserving stock.
    protected List<DeliveryOption> availableCenters(Order order) {
        validateOrder(order);
        List<DeliveryOption> options = new ArrayList<>();
        for (Center center : centers) {
            if (!hasStock(center, order.getItems())) {
                continue;
            }
            double distance = manhattanDistance.calculateDistance(
                    center.getX(), center.getY(), order.getX(), order.getY());
            if (!Double.isFinite(distance) || distance < 0) {
                throw new IllegalArgumentException("Distance must be finite and non-negative");
            }
            options.add(new DeliveryOption(center, distance));
        }
        return List.copyOf(options);
    }

    private boolean hasStock(Center center, Map<Long, Integer> items) {
        if (center.getItems() == null) {
            return false;
        }
        for (Map.Entry<Long, Integer> item : items.entrySet()) {
            Integer available = center.getItems().get(item.getKey());
            if (available == null || available < item.getValue()) {
                return false;
            }
        }
        return true;
    }

    private void validateOrder(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        if (!Double.isFinite(order.getX()) || !Double.isFinite(order.getY())) {
            throw new IllegalArgumentException("Delivery coordinates must be finite");
        }
        for (Map.Entry<Long, Integer> item : order.getItems().entrySet()) {
            if (item.getKey() == null || item.getValue() == null || item.getValue() <= 0) {
                throw new IllegalArgumentException("Items must have an ID and a positive quantity");
            }
        }
    }
}
